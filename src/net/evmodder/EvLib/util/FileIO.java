package net.evmodder.EvLib.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Logger;

public final class FileIO{
	private static final Logger LOGGER;

	// TODO: Make final (only modifier: ConfigUtils)
	public static String DIR;//= FabricLoader.getInstance().getConfigDir().toString()+"/";
	static{
		String tempDir = "./";
		try{
			Class.forName("org.bukkit.Bukkit");
			tempDir = "./plugins/EvFolder/";
		}
		catch(ClassNotFoundException e1){
			try{
				Object fabricLoader = Class.forName("net.fabricmc.loader.api.FabricLoader").getMethod("getInstance").invoke(null);
				tempDir = fabricLoader.getClass().getMethod("getConfigDir").invoke(fabricLoader).toString()+"/"+
							// TODO: dynamic config dir for specific mod?
							"evmod"
//							Class.forName("net.evmodder.evmod.Main").getField("MOD_ID").get(null)
							+"/";
			}
//			catch(IllegalArgumentException | NoSuchFieldException e){e.printStackTrace();}
			catch(ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e){
			}
		}
		DIR = tempDir;

		Logger tempLogger = Logger.getLogger("EvLibMod-FileIO");
		try{tempLogger = (Logger)Class.forName("net.evmodder.ServerMain").getField("LOGGER").get(null);}
		catch(IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException | ClassNotFoundException e){}
		LOGGER = tempLogger;
	}

	public static final String loadFile(String filename, String defaultValue){
		BufferedReader reader = null;
		try{reader = new BufferedReader(new FileReader(DIR+filename));}
		catch(FileNotFoundException e){
			if(defaultValue == null) return null;

			//Create Directory
			final File dir = new File(DIR);
			if(!dir.exists())dir.mkdir();

			//Create the file
			final File conf = new File(DIR+filename);
			try{
				conf.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(conf));
				writer.write(defaultValue); writer.close();
				reader = new BufferedReader(new FileReader(DIR+filename));
			}
			catch(IOException e1){e1.printStackTrace();}
		}
		final StringBuilder file = new StringBuilder();
		if(reader != null){
			try{
				String line = reader.readLine();
				while(line != null){
					line = line.trim().replace("//", "#");
					int cut = line.indexOf('#');
					if(cut == -1) file.append('\n').append(line);
					else if(cut > 0) file.append('\n').append(line.substring(0, cut).trim());
					line = reader.readLine();
				}
				reader.close();
			}catch(IOException e){}
		}
		return file.length() == 0 ? "" : file.substring(1);
	}
	public static final String loadFile(String filename, InputStream defaultValue){
		BufferedReader reader = null;
		try{reader = new BufferedReader(new FileReader(DIR+filename));}
		catch(FileNotFoundException e){
			if(defaultValue == null) return null;

			//Create Directory
			final File dir = new File(DIR);
			if(!dir.exists())dir.mkdir();

			//Create the file
			final File conf = new File(DIR+filename);
			try{
				conf.createNewFile();
				reader = new BufferedReader(new InputStreamReader(defaultValue));

				String line = reader.readLine();
				StringBuilder builder = new StringBuilder(line);
				while((line = reader.readLine()) != null) builder.append('\n').append(line);
				reader.close();

				BufferedWriter writer = new BufferedWriter(new FileWriter(conf));
				writer.write(builder.toString()); writer.close();
				reader = new BufferedReader(new FileReader(DIR+filename));
			}
			catch(IOException e1){e1.printStackTrace();}
		}
		final StringBuilder file = new StringBuilder();
		if(reader != null){
			try{
				String line = reader.readLine();
				while(line != null){
					line = line.trim().replace("//", "#");
					int cut = line.indexOf('#');
					if(cut == -1) file.append('\n').append(line);
					else if(cut > 0) file.append('\n').append(line.substring(0, cut).trim());
					line = reader.readLine();
				}
				reader.close();
			}catch(IOException e){}
		}
		return file.length() == 0 ? "" : file.substring(1);
	}
	public static final byte[] loadFileBytes(String filename){
		try{
			final FileInputStream fis = new FileInputStream(FileIO.DIR+filename);
			final byte[] data = fis.readAllBytes();
			fis.close();
			return data;
		}
		catch(FileNotFoundException e){return null;}
		catch(IOException e){e.printStackTrace(); return null;}
	}
	public static final boolean saveFileBytes(String filename, byte[] data){
		File file = new File(FileIO.DIR+filename);
		FileOutputStream fos;
		try{
			try{fos = new FileOutputStream(file);}
			catch(FileNotFoundException e){
				file.createNewFile();
				fos = new FileOutputStream(file);
			}
			fos.write(data);
			fos.close();
		}
		catch(IOException e){e.printStackTrace(); return false;}
		return true;
	}

	public static boolean saveFile(String filename, String content, boolean append){
		if(content == null || content.isEmpty()) return new File(DIR+filename).delete();
		try{
			final BufferedWriter writer = new BufferedWriter(new FileWriter(DIR+filename, append));
			writer.write(content); writer.close();
			return true;
		}
		catch(IOException e){return false;}
	}
	public static boolean saveFile(String filename, String content){
		return saveFile(filename, content, /*append=*/false);
	}

	public static boolean deleteFile(String filename){
		return new File(DIR+filename).delete();
	}

	public static boolean moveFile(String oldName, String newName){
		return new File(DIR+oldName).renameTo(new File(DIR+newName));
	}

	public static String loadResource(Class<?> clazz, String filename, String defaultContent){
		try{
			InputStream inputStream = clazz.getResourceAsStream("/"+filename);
			if(inputStream == null) inputStream = clazz.getClassLoader().getResourceAsStream("/"+filename);
			final BufferedReader reader = new BufferedReader(inputStream == null
					? new InputStreamReader(new ByteArrayInputStream(defaultContent.getBytes()))
					: new InputStreamReader(inputStream));

			final StringBuilder file = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null){
				line = line.trim().replace("//", "#");
				int cut = /*keepComments ? -1 :*/ line.indexOf('#');
				if(cut == -1) file.append('\n').append(line);
				else if(cut > 0) file.append('\n').append(line.substring(0, cut).trim());
			}
			reader.close();
			return file.length() > 0 ? file.substring(1) : "";
		}
		catch(IOException ex){ex.printStackTrace();}
		return defaultContent;
	}

	public static void moveDirectoryContents(File srcDir, File destDir){
		if(srcDir.isDirectory()){
			for(final File file : srcDir.listFiles()){
				try{Files.move(file.toPath(), new File(destDir.getPath()+"/"+file.getName()).toPath(),
						StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);}
				catch(IOException e){e.printStackTrace();}
			}
			srcDir.delete();
		}
		else try{
			Files.move(srcDir.toPath(), new File(destDir.getPath()+"/"+srcDir.getName()).toPath(),
					StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
		}
		catch(IOException e){e.printStackTrace();}
	}



	// element size = 16+16+4+4 = 40
	/*public static final Tuple3<UUID, Integer, Integer> lookupInClientFile(String filename, UUID pearlUUID){
		FileInputStream is = null;
		try{is = new FileInputStream(FileIO.DIR+filename);}
		catch(FileNotFoundException e){return null;}
		final byte[] data;
		try{data = is.readAllBytes(); is.close();}
		catch(IOException e){e.printStackTrace(); return null;}
		if(data.length % 40 != 0){
			LOGGER.severe("Corrupted/invalid ePearlDB file!");
			return null;
		}
		final long mostSig = pearlUUID.getMostSignificantBits(), leastSig = pearlUUID.getLeastSignificantBits();
		final ByteBuffer bb = ByteBuffer.wrap(data);
		int i = 0; while(i < data.length && bb.getLong(i) != mostSig && bb.getLong(i+8) != leastSig) i += 40;
//		int lo = 0, hi = data.length/40;
//		while(hi-lo > 1){
//			int m = (lo + hi)/2;
//			long v = bb.getLong(m*40);
//			if(v > mostSig || (v == mostSig && bb.getLong(m*40+8) > pearlUUID.getLeastSignificantBits())) hi = m;
//			else lo = m;
//		}
//		final int i = lo*40;
//		final UUID keyUUID = new UUID(bb.getLong(i), bb.getLong(i+8));
//		if(!keyUUID.equals(pearlUUID)){
		if(i >= data.length){
			LOGGER.fine("pearlUUID not found in localDB file: "+pearlUUID);
			return null;
		}
		final UUID ownerUUID = new UUID(bb.getLong(i+16), bb.getLong(i+24));
		final int x = bb.getInt(i+32), z = bb.getInt(i+36);
		return new Tuple3<>(ownerUUID, x, z);
	}*/

	public static final synchronized boolean appendToClientFile(String filename, UUID pearlUUID, PearlDataClient pdc){
		File file = new File(FileIO.DIR+filename);
		try{
			FileOutputStream fos = null;
			try{fos = new FileOutputStream(file, true);}
			catch(FileNotFoundException e){
				LOGGER.info("ePearlDB file not found, creating one");
				file.createNewFile();
				fos = new FileOutputStream(file, true);
			}
			ByteBuffer bb = ByteBuffer.allocate(16+16+4+4+4);
			bb.putLong(pearlUUID.getMostSignificantBits());
			bb.putLong(pearlUUID.getLeastSignificantBits());
			bb.putLong(pdc.owner().getMostSignificantBits());
			bb.putLong(pdc.owner().getLeastSignificantBits());
			bb.putInt(pdc.x()).putInt(pdc.y()).putInt(pdc.z());
			fos.write(bb.array());
			fos.close();
			LOGGER.fine("saved pearlUUID->ownerUUID to file: "+pearlUUID+"->"+pdc.owner());
		}
		catch(IOException e){e.printStackTrace();return false;}
		return true;
	}

	public static final synchronized HashMap<UUID, PearlDataClient> loadFromClientFile(String filename){
		final byte[] data;
		try{
			FileInputStream fis = new FileInputStream(FileIO.DIR+filename);
			data = fis.readAllBytes();
			fis.close();
		}
		catch(FileNotFoundException e){
			LOGGER.warning("DB file not found, attempting to create it");
			try{new File(filename).createNewFile();} catch(IOException e1){e1.printStackTrace();}
			return new HashMap<>();
		}
		catch(IOException e){
			e.printStackTrace();
			return new HashMap<>();
		}
		if(data.length % 40 == 0){
			final int numRows = data.length/40;
			final ByteBuffer bb = ByteBuffer.wrap(data);
			HashMap<UUID, PearlDataClient> entries = new HashMap<>(numRows);
			for(int i=0; i<numRows; ++i){
				UUID pearl = new UUID(bb.getLong(), bb.getLong());
				UUID owner = new UUID(bb.getLong(), bb.getLong());
				int x = bb.getInt(), z = bb.getInt();
				entries.put(pearl, new PearlDataClient(owner, x, -999, z));
			}
			return entries;
		}
		if(data.length % 44 != 0){
			LOGGER.severe("Corrupted/invalid ePearlDB file!");
			return new HashMap<>();
		}
		final int numRows = data.length/44;
		final ByteBuffer bb = ByteBuffer.wrap(data);
		HashMap<UUID, PearlDataClient> entries = new HashMap<>(numRows);
		for(int i=0; i<numRows; ++i){
			UUID pearl = new UUID(bb.getLong(), bb.getLong());
			UUID owner = new UUID(bb.getLong(), bb.getLong());
			int x = bb.getInt(), y = bb.getInt(), z = bb.getInt();
			entries.put(pearl, new PearlDataClient(owner, x, y, z));
		}
		return entries;
	}

	public static final synchronized HashSet<UUID> removeMissingFromClientFile(String filename, int playerX, int playerY, int playerZ,
			double affectedDistSq, HashSet<UUID> keep){
		FileInputStream is = null;
		try{is = new FileInputStream(FileIO.DIR+filename);}
		catch(FileNotFoundException e){/*e.printStackTrace(); */return null;}
		final byte[] data;
		try{data = is.readAllBytes(); is.close();}
		catch(IOException e){e.printStackTrace(); return null;}
		if(data.length % 44 != 0){
			LOGGER.severe("Corrupted/invalid ePearlDB file!");
			return null;
		}
		final ByteBuffer bbIn = ByteBuffer.wrap(data);
		final ByteBuffer bbOut = ByteBuffer.allocate(data.length);
		final HashSet<UUID> deletedKeys = new HashSet<>();
		int kept = 0;
		while(bbIn.hasRemaining()){
			final long k1 = bbIn.getLong(), k2 = bbIn.getLong();//16
			final long o1 = bbIn.getLong(), o2 = bbIn.getLong();//16
			final int x = bbIn.getInt(), y = bbIn.getInt(), z = bbIn.getInt();//4+4+4

			final double diffX = playerX-x, diffY = playerY-y, diffZ = playerZ-z; // Intentional use of double (to avoid overflow)
			final double distSq = diffX*diffX + diffY*diffY + diffZ*diffZ;
			if(distSq < affectedDistSq){
				final UUID key = new UUID(k1, k2);
				if(!keep.contains(key)){deletedKeys.add(key); continue;}
			}
			//else
			++kept;
			bbOut.putLong(k1).putLong(k2).putLong(o1).putLong(o2).putInt(x).putInt(z);
		}
		if(kept*44 == data.length) return deletedKeys; // Nothing was deleted

		final byte[] rowsLeft = new byte[kept*44];
		bbOut.get(0, rowsLeft);
		try{
			FileOutputStream fos = new FileOutputStream(FileIO.DIR+filename);
			fos.write(rowsLeft);
			fos.close();
		}
		catch(IOException e){e.printStackTrace(); deletedKeys.clear();}
		return deletedKeys;
	}

	/**
	 * List directory contents for a resource folder. Not recursive.
	 * This is basically a brute-force implementation.
	 * Works for regular files and also JARs.
	 * 
	 * @author Greg Briggs
	 * @param clazz Any java class that lives in the same place as the resources you want.
	 * @param path Should end with "/", but not start with one.
	 * @return Just the name of each member item, not the full paths.
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	// NOTE: Currently unused, but thought it would be cool to have.
	/*String[] getResourceListing(Class<?> clazz, String path) throws URISyntaxException, IOException{
		URL dirURL = clazz.getClassLoader().getResource(path);
		if(dirURL != null && dirURL.getProtocol().equals("file")){
			// A file path: easy enough
			return new File(dirURL.toURI()).list();
		}
		if(dirURL == null){
			// In case of a jar file, we can't actually find a directory. Have to assume the same jar as clazz.
			final String me = clazz.getName().replace(".", "/") + ".class";
			dirURL = clazz.getClassLoader().getResource(me);
		}
		if(dirURL.getProtocol().equals("jar")){
			// A JAR path
			final String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); // strip out only the JAR file
			final JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			final Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar
			final Set<String> result = new HashSet<>(); // avoid duplicates in case it is a subdirectory
			while(entries.hasMoreElements()){
				final String name = entries.nextElement().getName();
				if(name.startsWith(path)){ // filter according to the path
					String entry = name.substring(path.length());
					int checkSubdir = entry.indexOf("/");
					if(checkSubdir >= 0){
						// if it is a subdirectory, we just return the directory name
						entry = entry.substring(0, checkSubdir);
					}
					result.add(entry);
				}
			}
			jar.close();
			return result.toArray(new String[result.size()]);
		}
		throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
	}*/
}