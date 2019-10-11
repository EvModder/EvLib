package net.evmodder.EvLib;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class EvCommand implements TabExecutor{
//	protected EvPlugin plugin;
	final String commandName;
	final PluginCommand command;
	final static CommandExecutor disabledCmdExecutor = new CommandExecutor(){
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
			sender.sendMessage(ChatColor.RED+"This command is currently unavailable");
			return true;
		}
	};


//	@Override
//	abstract public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args);

	public EvCommand(JavaPlugin pl, boolean enabled){
//		plugin = p;
		commandName = getClass().getSimpleName().substring(7).toLowerCase();
		command = pl.getCommand(commandName);
		if(enabled){command.setExecutor(this); command.setTabCompleter(this);}
		else command.setExecutor(disabledCmdExecutor);
	}

	public EvCommand(JavaPlugin pl){
		this(pl, true);
	}

	public PluginCommand getCommand(){return command;}
}