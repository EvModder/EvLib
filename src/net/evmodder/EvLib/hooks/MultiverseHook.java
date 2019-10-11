package net.evmodder.EvLib.hooks;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import net.evmodder.EvLib.extras.ReflectionUtils;
import net.evmodder.EvLib.extras.ReflectionUtils.RefClass;
import net.evmodder.EvLib.extras.ReflectionUtils.RefMethod;

public class MultiverseHook{
	static String MAIN_WORLD = Bukkit.getWorlds().get(0).getName();//[0] is the default world
	static final RefClass mvCoreClass = ReflectionUtils.getRefClass("com.onarandombox.MultiverseCore.MultiverseCore"); 
	static final RefClass mvwmClass = ReflectionUtils.getRefClass("com.onarandombox.MultiverseCore.api.MVWorldManager");
	static final RefClass mvwClass = ReflectionUtils.getRefClass("com.onarandombox.MultiverseCore.api.MultiverseWorld");
	static final RefMethod methodGetMVWorldManager = mvCoreClass.getMethod("getMVWorldManager");
	static final RefMethod methodGetMVWorld = mvwmClass.getMethod("getMVWorld", World.class);
	static final RefMethod methodGetGameMode = mvwClass.getMethod("getGameMode");
	public static GameMode getWorldGameMode(World world){
		Plugin pl = Bukkit.getPluginManager().getPlugin("Multiverse-Core");
		if (pl != null && pl.isEnabled()){
			return (GameMode)
			methodGetGameMode.of(
				methodGetMVWorld.of(
					methodGetMVWorldManager.of(pl).call()
				).call(world)
			).call();
		}
		else if(world.getName().contains(MAIN_WORLD)) return Bukkit.getDefaultGameMode();
		else return null;
	}
}