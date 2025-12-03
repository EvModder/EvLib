package net.evmodder.EvLib.hooks;

import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import net.evmodder.EvLib.util.ReflectionUtils;

public class MultiverseHook{
	static String MAIN_WORLD = Bukkit.getWorlds().get(0).getName();//[0] is the default world
	static final Class<?> mvCoreClass = ReflectionUtils.getClass("com.onarandombox.MultiverseCore.MultiverseCore"); 
	static final Class<?> mvwmClass = ReflectionUtils.getClass("com.onarandombox.MultiverseCore.api.MVWorldManager");
	static final Class<?> mvwClass = ReflectionUtils.getClass("com.onarandombox.MultiverseCore.api.MultiverseWorld");
	static final Method methodGetMVWorldManager = ReflectionUtils.getMethod(mvCoreClass, "getMVWorldManager");
	static final Method methodGetMVWorld = ReflectionUtils.getMethod(mvwmClass, "getMVWorld", World.class);
	static final Method methodGetGameMode = ReflectionUtils.getMethod(mvwClass, "getGameMode");
	public static GameMode getWorldGameMode(World world){
		Plugin pl = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
		if (pl != null && pl.isEnabled()){
			Object manager = ReflectionUtils.call(methodGetMVWorldManager, pl);
			Object mvWorld = ReflectionUtils.call(methodGetMVWorld, manager, world);
			return (GameMode)ReflectionUtils.call(methodGetGameMode, mvWorld);
		}
		else if(world.getName().contains(MAIN_WORLD)) return Bukkit.getDefaultGameMode();
		else return null;
	}
}