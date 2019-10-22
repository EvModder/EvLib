package net.evmodder.EvLib.extras;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

public class CommandUtils {
	//map<command name, String[]{permission, description}>
	static Map<String, String[]> pluginCommands = null;
	static void initFancyHelp(){
		//load commands from all plugins
		pluginCommands = new HashMap<String, String[]>();
		for(Plugin plugin : Bukkit.getPluginManager().getPlugins()){
			if(plugin.getDescription().getCommands() == null) continue;
			for(String cmdName : plugin.getDescription().getCommands().keySet()){
				PluginCommand cmd = plugin.getServer().getPluginCommand(cmdName);
				if(cmdName.endsWith("horse")){
					continue;
					//cmdName = "hm "+cmdName.substring(0, cmdName.length()-5);
				}
				if(cmd != null) pluginCommands.put(cmdName, new String[]{
					(cmd.getPermission() != null ? cmd.getPermission() : plugin.getName().toLowerCase()+'.'+cmdName),
					cmd.getDescription()
				});
			}
		}
		String[] hmCmd = pluginCommands.remove("horsemanager");
		if(hmCmd != null) pluginCommands.put("hm", hmCmd);
	}

	static void runCommand(String command){Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);}

	public static void showFancyHelp(CommandSender sender, int pageNum){
		if (pluginCommands == null) initFancyHelp();
		List<String> commandNames = new ArrayList<String>();
		for(String cmdName : pluginCommands.keySet()){
			if(sender.hasPermission(pluginCommands.get(cmdName)[0])) commandNames.add(cmdName);
		}
		Collections.sort(commandNames);

		int totalPages = (commandNames.size()-2)/9 + 1;
		if(pageNum > totalPages) pageNum = totalPages;

		//essentials-style help, minus the plugins.
		StringBuilder helpPage = new StringBuilder("").append(ChatColor.YELLOW).append(" ---- ")
				.append(ChatColor.GOLD).append("Help").append(ChatColor.YELLOW).append(" -- ").append(ChatColor.GOLD)
				.append("Page").append(ChatColor.RED).append(" ").append(pageNum).append(ChatColor.GOLD).append("/")
				.append(ChatColor.RED).append(totalPages).append(ChatColor.YELLOW).append(" ----");
		
		int i, startingVal = (pageNum-1)*9;
		for(i = startingVal; i < startingVal+9 && i < commandNames.size(); ++i){
			helpPage.append("\n").append(ChatColor.GOLD).append("/").append(commandNames.get(i)).append(ChatColor.WHITE)
					.append(": ").append(pluginCommands.get(commandNames.get(i))[1]);
		}
		if(pageNum != totalPages){
			helpPage.append("\n").append(ChatColor.GOLD).append("Type ").append(ChatColor.RED).append("/help ")
					.append(pageNum+1).append(ChatColor.GOLD).append(" to read the next page.");
		}
		else if(i < commandNames.size()){
			helpPage.append("\n").append(ChatColor.GOLD).append("/").append(commandNames.get(i)).append(ChatColor.WHITE)
					.append(": ").append(pluginCommands.get(commandNames.get(i))[1]);
		}
		sender.sendMessage(helpPage.toString());
	}

	public static void showCommandHelp(CommandSender sender, Command cmd){
		sender.sendMessage(new StringBuilder(ChatColor.GOLD+"Help for command ")
			.append(ChatColor.RED).append(cmd.getName()).append(ChatColor.GOLD).append(":\n").append(ChatColor.GOLD)

			.append("Description: ").append(ChatColor.WHITE).append(cmd.getDescription()).append('\n').append(ChatColor.GOLD)
			.append("Usage: ").append(ChatColor.WHITE).append(cmd.getUsage()).append('\n').append(ChatColor.GOLD)
			.append("Aliases: ").append(ChatColor.WHITE).append(cmd.getAliases()).append('\n').append(ChatColor.GOLD)
			.append("Permission: ").append(ChatColor.WHITE).append(cmd.getPermission())
			.toString());
	}
}