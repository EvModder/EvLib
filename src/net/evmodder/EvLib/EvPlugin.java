package net.evmodder.EvLib;

import java.io.InputStream;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

//Export -> JAVADOC -> javadoc sources:
// * Bukkit-latest: https://hub.spigotmc.org/javadocs/bukkit/
// Include files:
// * EvCommand, EvPlugin, EvUtils, FileIO, util/*, extras/ActionBar,Entity,Head,MethodMocker,NBTTag,Packet,Reflection,Selector,TabText,Tellraw,Text,Type,Web

// Search > File... > containing text X > replace Y
// * "extras.TellrawUtils." -> "extras.tellraw."
// * "extras/TellrawUtils/" -> "extras/tellraw/"
// * "TellrawUtils." -> ""

/** JavaPlugin with more config file handling features */
public abstract class EvPlugin extends JavaPlugin{
	protected FileConfiguration config;
	@Override public FileConfiguration getConfig(){return config;}
	@Override public void saveConfig(){
		if(config != null && !FileIO.saveConfig("config-"+getName()+".yml", config)){
			getLogger().severe("Error while saving plugin configuration file!");
		}
	}
	@Override public void reloadConfig(){
		InputStream defaultConfig = getClass().getResourceAsStream("/config.yml");
		if(defaultConfig != null){
			FileIO.verifyDir(this);
			//new BukkitRunnable(){@Override public void run(){FileIO.verifyDir(EvPlugin.this);}}.runTaskLater(this, 1);
			config = FileIO.loadConfig(this, "config-"+getName()+".yml", defaultConfig, /*notifyIfNew=*/true);
		}
	}

	@Override public final void onEnable(){
//		getLogger().info("Loading " + getDescription().getFullName());
		reloadConfig();
		onEvEnable();
	}

	@Override public final void onDisable(){
		onEvDisable();
	}

	/** Called when this plugin is enabled */ public void onEvEnable(){}
	/** Called when this plugin is disabled */ public void onEvDisable(){}
}
