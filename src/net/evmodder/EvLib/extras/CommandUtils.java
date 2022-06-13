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
import org.bukkit.plugin.PluginDescriptionFile;

public class CommandUtils {
	//map<command name, String[]{permission, description}>
	static Map<String, String[]> pluginCommands = null;
	static void initFancyHelp(){
		//load commands from all plugins
		pluginCommands = new HashMap<>();
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
		List<String> commandNames = new ArrayList<>();
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

	private static final String graySep = ChatColor.GOLD+", "+ChatColor.GRAY;
	private static final String helpHeader = ChatColor.YELLOW+" ---- "+ChatColor.GOLD+"Help"+ChatColor.YELLOW+" ----------------\n";
	public static void showCommandHelp(CommandSender sender, Command cmd){
		StringBuilder builder = new StringBuilder(helpHeader)
			.append(ChatColor.GOLD).append("Command ").append(ChatColor.RED).append('/').append(cmd.getName()).append(ChatColor.GOLD).append(":\n")
			.append("Description: ").append(ChatColor.WHITE).append(cmd.getDescription()).append('\n').append(ChatColor.GOLD)
			.append("Usage: ").append(ChatColor.WHITE).append(cmd.getUsage()).append('\n').append(ChatColor.GOLD);
		if(cmd.getAliases() != null && !cmd.getAliases().isEmpty())
			builder.append("Aliases: ").append(ChatColor.WHITE).append(cmd.getAliases()).append('\n').append(ChatColor.GOLD);
		builder.append("Permission: ").append(ChatColor.WHITE).append(cmd.getPermission());
		sender.sendMessage(builder.toString());
	}

	public static void showPluginHelp(CommandSender sender, Plugin pl){
		PluginDescriptionFile desc = pl.getDescription();
		StringBuilder builder = new StringBuilder(helpHeader)
			.append(ChatColor.GOLD).append("Plugin ").append(pl.isEnabled() ? ChatColor.GREEN : ChatColor.RED).append(pl.getName())
			.append(ChatColor.GOLD).append(" version ").append(ChatColor.GRAY).append(desc.getVersion()).append(ChatColor.GOLD).append(":\n")
			.append("Authors: ").append(ChatColor.GRAY).append(String.join(graySep, desc.getAuthors())).append(ChatColor.GOLD);
		if(desc.getWebsite() != null && !desc.getWebsite().isEmpty())
			builder.append("\nWebsite: ").append(ChatColor.AQUA).append(desc.getWebsite()).append(ChatColor.GOLD);
		builder.append("\nDescription: ").append(ChatColor.WHITE).append(desc.getDescription()).append(ChatColor.GOLD);
		if(desc.getCommands() != null && !desc.getCommands().isEmpty())
			builder.append("\nCommands: ").append(ChatColor.GRAY).append('/')
			.append(String.join(graySep+'/', desc.getCommands().keySet())).append(ChatColor.GOLD);
		sender.sendMessage(builder.toString());
	}
}