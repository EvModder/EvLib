package net.evmodder.EvLib.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import com.earth2me.essentials.perm.PermissionsHandler;
import com.earth2me.essentials.User;
import net.ess3.api.IEssentials;

public class EssPermHook{
	static IEssentials eBase;
	static PermissionsHandler ePerms;

	static IEssentials getEssBase(){
		if(eBase == null) eBase = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
		return eBase;
	}
	static PermissionsHandler getPermHandler(){
		if(ePerms == null) ePerms = getEssBase().getPermissionsHandler();
		return ePerms;
	}

	public static boolean hasPermission(Player p, String permission){
		return p.hasPermission(permission) || getPermHandler().hasPermission(p, permission);
	}
	public static boolean hasPermission(Player p, Permission permission){
		return p.hasPermission(permission) || getPermHandler().hasPermission(p, permission.getName());
	}
	public static boolean isAuthorized(Player p, String permission){
		return new User(p, getEssBase()).isAuthorized(permission);
	}
	public static boolean isAuthorized(Player p, Permission permission){
		return new User(p, getEssBase()).isAuthorized(permission.getName());
	}
}