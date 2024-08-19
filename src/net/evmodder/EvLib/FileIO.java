package net.evmodder.EvLib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class FileIO{// version = X1.0
	static private final String EV_DIR = "./plugins/EvFolder/";
	/** Defaults to <code>"./plugins/&lt;EvPluginName&gt;/"</code>
	 */
	static public String DIR = EV_DIR;//TODO: remove public? (only user: EvLib/extras/WebUtils.java)
	static final int MERGE_EV_DIR_THRESHOLD = 4;

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

	public static Vector<String> installedEvPlugins(){
		final Vector<String> evPlugins = new Vector<>();
		for(final Plugin pl : Bukkit.getServer().getPluginManager().getPlugins()){
			if(pl instanceof EvPlugin) evPlugins.add(pl.getName());
		}
		return evPlugins;
	}

	public static void verifyDir(Plugin evPl){
		final Vector<String> evPlugins = FileIO.installedEvPlugins();
		final String CUSTOM_DIR = "./plugins/"+evPl.getName()+"/";
		if(!new File(EV_DIR).exists() && (evPl.getName().equals("DropHeads") || evPlugins.size() < MERGE_EV_DIR_THRESHOLD)){
			DIR = CUSTOM_DIR;
		}
		else if(new File(CUSTOM_DIR).exists()){//merge with EvFolder
			//Bukkit.getLogger().info("EvPlugins installed: "+String.join(", ", evPlugins));
			evPl.getLogger().warning("Relocating data in "+CUSTOM_DIR+", this might take a minute..");
			final File evFolder = new File(EV_DIR);
			if(!evFolder.exists()) evFolder.mkdir();
			moveDirectoryContents(new File(CUSTOM_DIR), evFolder);
		}
	}

	public static String loadFile(String filename, InputStream defaultValue){
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

	public static String loadFile(String filename, String defaultContent/*, boolean exactContent*/){
		BufferedReader reader;
		try{reader = new BufferedReader(new FileReader(DIR+filename));}
		catch(FileNotFoundException e){
			if(defaultContent == null || defaultContent.isEmpty()) return defaultContent;

			//Create Directory
			final File dir = new File(DIR);
			if(!dir.exists())dir.mkdir();

			//Create the file
			File conf = new File(DIR+filename);
			try{
				conf.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(conf));
				writer.write(defaultContent);
				writer.close();
			}
			catch(IOException e1){e1.printStackTrace();}
			reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(defaultContent.getBytes())));
		}
		final StringBuilder file = new StringBuilder();
		if(reader != null){
			try{
				String line;
				while((line = reader.readLine()) != null){
					line = line.trim().replace("//", "#");
					int cut = line.indexOf('#');
					if(cut == -1) file.append('\n').append(line);
					else if(cut > 0) file.append('\n').append(line.substring(0, cut).trim());
				}
				reader.close();
			}catch(IOException e){}
		}
		return file.length() == 0 ? "" : file.substring(1);//Hmm; return "" or defaultContent
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

	public static YamlConfiguration loadConfig(JavaPlugin pl, String configName, InputStream defaultConfig, boolean notifyIfNew){
		if(!configName.endsWith(".yml")){
			pl.getLogger().severe("Invalid config file!");
			pl.getLogger().severe("Configuation files must end in .yml");
			return null;
		}
		final File file = new File(DIR+configName);
		final boolean loadNewConfig = !file.exists() && defaultConfig != null;
		if(loadNewConfig){
			try{
				//Create Directory
				final File dir = new File(DIR);
				if(!dir.exists())dir.mkdir();

				//Read contents of defaultConfig
				final BufferedReader reader = new BufferedReader(new InputStreamReader(defaultConfig));
				String line = reader.readLine();
				final StringBuilder builder = new StringBuilder(line);
				while((line = reader.readLine()) != null) builder.append('\n').append(line);
				reader.close();

				//Create new config from contents of defaultConfig
				final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write(builder.toString()); writer.close();
			}
			catch(IOException ex){
				pl.getLogger().severe(ex.getStackTrace().toString());
				pl.getLogger().severe("Unable to locate a default config!");
				pl.getLogger().severe("Could not find /config.yml in plugin's .jar");
			}
			if(notifyIfNew) pl.getLogger().info("Could not locate "+configName+" file!");
			if(notifyIfNew) pl.getLogger().info("Generating a new one with default settings.");
		}
		final YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(file);
		newConfig.set("new", loadNewConfig);
		return newConfig;
	}

	public static String loadResource(Object pl, String filename, String defaultContent){
		try{
			InputStream inputStream = pl.getClass().getResourceAsStream("/"+filename);
			if(inputStream == null) inputStream = pl.getClass().getClassLoader().getResourceAsStream("/"+filename);
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

	public static YamlConfiguration loadYaml(String filename, String defaultContent){
		final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new File(DIR+filename));
		if(yaml == null){
			if(defaultContent == null || defaultContent.isEmpty()) return null;

			//Create Directory and file
			File dir = new File(DIR);
			if(!dir.exists()) dir.mkdir();
			File file = new File(DIR+filename);
			try{
				file.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write(defaultContent);
				writer.close();
			}
			catch(IOException e){e.printStackTrace();}
			return YamlConfiguration.loadConfiguration(file);
		}
		return yaml;
	}

	public static boolean saveYaml(String filename, YamlConfiguration content){
		try{
			if(!new File(DIR).exists()) new File(DIR).mkdir();
			content.save(DIR+filename);
		}
		catch(IOException e){return false;}
		return true;
	}
	public static boolean saveConfig(String configName, FileConfiguration config){
		try{
			if(!new File(DIR).exists()) new File(DIR).mkdir();
			config.save(DIR+configName);
		}
		catch(IOException ex){ex.printStackTrace(); return false;}
		return true;
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
	// NOTE: Currently unused by any EvPlugin, but thought it would be cool to have.
	String[] getResourceListing(Class<?> clazz, String path) throws URISyntaxException, IOException{
		URL dirURL = clazz.getClassLoader().getResource(path);
		if(dirURL != null && dirURL.getProtocol().equals("file")){
			/* A file path: easy enough */
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
			return result.toArray(new String[result.size()]);
		}
		throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
	}
}