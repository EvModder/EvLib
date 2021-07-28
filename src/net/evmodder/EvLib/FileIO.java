package net.evmodder.EvLib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Vector;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class FileIO{// version = X1.0
	static private final String EV_DIR = "./plugins/EvFolder/";
	static public String DIR = EV_DIR;//TODO: remove public? (only user: EvLib/extras/WebUtils.java)
	static final int MERGE_EV_DIR_THRESHOLD = 4;

	public static void moveDirectoryContents(File srcDir, File destDir){
		if(srcDir.isDirectory()){
			for(File file : srcDir.listFiles()){
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
		Vector<String> evPlugins = new Vector<String>();
		for(Plugin pl : Bukkit.getServer().getPluginManager().getPlugins()){
			try{
				@SuppressWarnings("unused")
				String ver = pl.getClass().getDeclaredField("EvLib_ver").get(null).toString();
				evPlugins.add(pl.getName());
				//TODO: potentially return list of different EvLib versions being used
			}
			catch(Throwable e){}
		}
		return evPlugins;
	}

	static void verifyDir(JavaPlugin evPl){
		Vector<String> evPlugins = FileIO.installedEvPlugins();
		final String CUSTOM_DIR = "./plugins/"+evPl.getName()+"/";
		if(!new File(EV_DIR).exists() && (evPl.getName().equals("DropHeads") || evPlugins.size() < MERGE_EV_DIR_THRESHOLD)){
			DIR = CUSTOM_DIR;
		}
		else if(new File(CUSTOM_DIR).exists()){//merge with EvFolder
			//Bukkit.getLogger().info("EvPlugins installed: "+String.join(", ", evPlugins));
			evPl.getLogger().warning("Relocating data in "+CUSTOM_DIR+", this might take a minute..");
			File evFolder = new File(EV_DIR);
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
			File dir = new File(DIR);
			if(!dir.exists())dir.mkdir();

			//Create the file
			File conf = new File(DIR+filename);
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
		StringBuilder file = new StringBuilder();
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
		BufferedReader reader = null;
		try{reader = new BufferedReader(new FileReader(DIR+filename));}
		catch(FileNotFoundException e){
			if(defaultContent == null || defaultContent.isEmpty()) return defaultContent;

			//Create Directory
			File dir = new File(DIR);
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
			return defaultContent;
		}
		StringBuilder file = new StringBuilder();
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
			BufferedWriter writer = new BufferedWriter(new FileWriter(DIR+filename, append));
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

	public static YamlConfiguration loadConfig(JavaPlugin pl, String configName, InputStream defaultConfig, boolean notifyIfNew){
		if(!configName.endsWith(".yml")){
			pl.getLogger().severe("Invalid config file!");
			pl.getLogger().severe("Configuation files must end in .yml");
			return null;
		}
		File file = new File(DIR+configName);
		if(!file.exists() && defaultConfig != null){
			try{
				//Create Directory
				File dir = new File(DIR);
				if(!dir.exists())dir.mkdir();

				//Read contents of defaultConfig
				BufferedReader reader = new BufferedReader(new InputStreamReader(defaultConfig));
				String line = reader.readLine();
				StringBuilder builder = new StringBuilder(line);
				while((line = reader.readLine()) != null) builder.append('\n').append(line);
				reader.close();

				//Create new config from contents of defaultConfig
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write(builder.toString()); writer.close();
			}
			catch(IOException ex){
				pl.getLogger().severe(ex.getStackTrace().toString());
				pl.getLogger().severe("Unable to locate a default config!");
				pl.getLogger().severe("Could not find /config.yml in plugin's .jar");
			}
			if(notifyIfNew) pl.getLogger().info("Could not locate configuration file!");
			if(notifyIfNew) pl.getLogger().info("Generating a new one with default settings.");
		}
		return YamlConfiguration.loadConfiguration(file);
	}

	public static String loadResource(Object pl, String filename/*, boolean keepComments*/){
		try{
			InputStream inputStream = pl.getClass().getResourceAsStream("/"+filename);
			if(inputStream == null) inputStream = pl.getClass().getClassLoader().getResourceAsStream("/"+filename);
			if(inputStream == null) return "";
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

			StringBuilder file = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null){
				line = line.trim().replace("//", "#");
				int cut = /*keepComments ? -1 :*/ line.indexOf('#');
				if(cut == -1) file.append('\n').append(line);
				else if(cut > 0) file.append('\n').append(line.substring(0, cut).trim());
			}
			reader.close();
			return file.substring(1);
		}
		catch(IOException ex){ex.printStackTrace();}
		return "";
	}

	public static YamlConfiguration loadYaml(String filename, String defaultContent){
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new File(DIR+filename));
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
}