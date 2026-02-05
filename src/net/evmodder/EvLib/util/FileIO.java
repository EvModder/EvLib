package net.evmodder.EvLib.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public final class FileIO{
	// Modifiers: FabricEntryPoint(mods), ConfigUtils(plugins)
	// (ServerLogic(DB) leaves it unchanged)
	public static String DIR = "./";

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
//		final File file = new File(FileIO.DIR+filename);
//		try{
//			final FileInputStream fis = new FileInputStream(file);
////			final FileLock lock = fis.getChannel().tryLock();
////			if(lock == null) return null;
//			final byte[] data = fis.readAllBytes();
//			fis.close();
////			lock.release();
//			return data;
//		}
//		catch(FileNotFoundException e){return null;}
//		catch(IOException e){e.printStackTrace(); return null;}
		final Path path = Paths.get(FileIO.DIR+filename);
		try{return Files.readAllBytes(path);}
		catch(IOException e){e.printStackTrace(); return null;}
		//B
//		try(final FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
//				final FileLock lock = channel.tryLock()) { // Acquire a shared lock
//				final byte[] data = Files.readAllBytes(path);
//				lock.release();
//				return data;
//		}
//		catch(IOException e){e.printStackTrace(); return null;}
//		catch (OutOfMemoryError e){e.printStackTrace(); return null;}
	}
	public static final boolean saveFileBytes(String filename, byte[] data, int start, int end, boolean append){
		assert start >= 0 && end <= data.length && start < end;
		final File file = new File(FileIO.DIR+filename);
		try{
//			FileOutputStream fos;
//			try{fos = new FileOutputStream(file, append);}
//			catch(FileNotFoundException e){
//				file.createNewFile();
//				fos = new FileOutputStream(file, append);
//			}
//			fos.write(data);
//			fos.close();
			RandomAccessFile raf;
			try{raf = new RandomAccessFile(file, "rw");}
			catch(FileNotFoundException e){
				file.createNewFile();
				raf = new RandomAccessFile(file, "rw");
			}
			final FileLock lock = raf.getChannel().tryLock();
			if(lock == null){/*Log.error("FileIO: unable to acquire lock for "+filename)*/return false;}
			if(append) raf.seek(raf.length());
			raf.write(data, start, end);
			if(!append) raf.setLength(end-start); // Truncate any un-overwritten data
			lock.release();
		}
		catch(IOException e){e.printStackTrace(); return false;}
		return true;
	}
	// Convenience overloads
	public static final boolean saveFileBytes(String filename, byte[] data, boolean append){return saveFileBytes(filename, data, 0, data.length, append);}
	public static final boolean saveFileBytes(String filename, byte[] data){return saveFileBytes(filename, data, 0, data.length, /*append=*/false);}

	public static final boolean saveFile(String filename, String content, boolean append){
		if(content == null || content.isEmpty()) return new File(DIR+filename).delete();
		try{
			final BufferedWriter writer = new BufferedWriter(new FileWriter(DIR+filename, append));
			writer.write(content); writer.close();
			return true;
		}
		catch(IOException e){return false;}
	}
	public static final boolean saveFile(String filename, String content){return saveFile(filename, content, /*append=*/false);}

	public static boolean deleteFile(String filename){
		return new File(DIR+filename).delete();
	}

	public static boolean moveFile(String oldName, String newName){
		return new File(DIR+oldName).renameTo(new File(DIR+newName));
	}

	public static final Object readObject(String filename){
		try(FileInputStream fis = new FileInputStream(DIR+filename); ObjectInputStream ois = new ObjectInputStream(fis)){
			return ois.readObject();
		}
		catch(FileNotFoundException e){return null;}
		catch(EOFException e){} // Hasn't been cached yet
		catch(IOException | ClassNotFoundException e){e.printStackTrace();}
		return null;
	}
	public static final void writeObject(String filename, Object obj){
		try(FileOutputStream fos = new FileOutputStream(DIR+filename); ObjectOutputStream oos = new ObjectOutputStream(fos)){
			oos.writeObject(obj);
		}
		catch(IOException e){e.printStackTrace();}
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