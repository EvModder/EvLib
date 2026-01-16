package net.evmodder.EvLib.bukkit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import net.evmodder.EvLib.util.FileIO;

public final class ConfigUtils{// version = X1.1

	public static List<String> installedEvPlugins(){
		return Stream.of(Bukkit.getServer().getPluginManager().getPlugins()).filter(EvPlugin.class::isInstance).map(Plugin::getName).toList();
	}

	private static final String DEFAULT_DIR_NAME = "./plugins/EvFolder/";
	private static final int MERGE_EV_DIR_THRESHOLD = 4;
	public static void updateConfigDirName(Plugin evPl){
		final List<String> evPlugins = ConfigUtils.installedEvPlugins();
		final String CUSTOM_DIR = "./plugins/"+evPl.getName()+"/";
		if(!new File(DEFAULT_DIR_NAME).exists() && (evPl.getName().equals("DropHeads") || evPlugins.size() < MERGE_EV_DIR_THRESHOLD)){
			FileIO.DIR = CUSTOM_DIR;
		}
		else if(new File(CUSTOM_DIR).exists()){//merge with EvFolder
			//Bukkit.getLogger().info("EvPlugins installed: "+String.join(", ", evPlugins));
			evPl.getLogger().warning("Relocating data in "+CUSTOM_DIR+", this might take a minute..");
			final File evFolder = new File(DEFAULT_DIR_NAME);
			if(!evFolder.exists()) evFolder.mkdir();
			FileIO.moveDirectoryContents(new File(CUSTOM_DIR), evFolder);
		}
	}
	
	public static YamlConfiguration loadConfig(Plugin pl, String configName, InputStream defaultConfig, boolean notifyIfNew){
		if(!configName.endsWith(".yml")){
			pl.getLogger().severe("Invalid config file!");
			pl.getLogger().severe("Configuation files must end in .yml");
			return null;
		}
		final File file = new File(FileIO.DIR+configName);
		final boolean loadNewConfig = !file.exists() && defaultConfig != null;
		if(loadNewConfig){
			try{
				//Create Directory
				final File dir = new File(FileIO.DIR);
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

	public static YamlConfiguration loadYaml(String filename, String defaultContent){
		final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new File(FileIO.DIR+filename));
		if(yaml == null){
			if(defaultContent == null || defaultContent.isEmpty()) return null;

			//Create Directory and file
			File dir = new File(FileIO.DIR);
			if(!dir.exists()) dir.mkdir();
			File file = new File(FileIO.DIR+filename);
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
			if(!new File(FileIO.DIR).exists()) new File(FileIO.DIR).mkdir();
			content.save(FileIO.DIR+filename);
		}
		catch(IOException e){return false;}
		return true;
	}
	public static boolean saveConfig(String configName, FileConfiguration config){
		try{
			if(!new File(FileIO.DIR).exists()) new File(FileIO.DIR).mkdir();
			config.save(FileIO.DIR+configName);
		}
		catch(IOException ex){ex.printStackTrace(); return false;}
		return true;
	}
}