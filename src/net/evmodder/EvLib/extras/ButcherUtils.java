package net.evmodder.EvLib.extras;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

public class ButcherUtils{
	public enum KillFlag{ANIMALS, TILE, NAMED, NEARBY, EQUIPPED, UNIQUE};
	private static final Set<String> uniqueEtypes = Set.of(
			// Pre-1.20.5 | 1.20.5+
			"DROPPED_ITEM", "ITEM",
			"FISHING_HOOK", "FISHING_BOBBER",
			"LEASH_HITCH", "LEASH_KNOT",
			"MINECART_CHEST", "CHEST_MINECART",
			"MINECART_COMMAND", "COMMAND_BLOCK_MINECART",
			"MINECART_FURNACE", "FURNACE_MINECART",
			"MINECART_HOPPER", "HOPPER_MINECART",
			"MINECART_MOB_SPAWNER", "SPAWNER_MINECART",
			"MINECART_TNT", "TNT_MINECART",
			"SNOWMAN", "SNOW_GOLEM",
			"SPLASH_POTION", "POTION"
	);
	static boolean isUnique(EntityType type){
		switch(type){
			case AREA_EFFECT_CLOUD:
			case ARMOR_STAND:
			case CAT:
			case DONKEY:
//			case DROPPED_ITEM:
			case ELDER_GUARDIAN:
			case ENDER_DRAGON:
			case EVOKER:
			case FALLING_BLOCK:
//			case FISHING_HOOK:
			case FOX:
			case GIANT:
			case HORSE:
			case ILLUSIONER:
			case IRON_GOLEM:
			case ITEM_FRAME:
//			case LEASH_HITCH:
			case LLAMA:
			case MINECART:
//			case MINECART_CHEST:
//			case MINECART_COMMAND:
//			case MINECART_FURNACE:
//			case MINECART_HOPPER:
//			case MINECART_MOB_SPAWNER:
//			case MINECART_TNT:
			case MULE:
			case PANDA:
			case PARROT:
			case PLAYER:
			case SHULKER:
			case SKELETON_HORSE:
//			case SNOWMAN:
//			case SPLASH_POTION:
			case TRADER_LLAMA:
			case TRIDENT:
			case VILLAGER:
			case WANDERING_TRADER:
			case WOLF:
				return true;
			default:
				return uniqueEtypes.contains(type.name());
		}
	}

	public static int clearEntitiesByWorld(final World world, final Map<KillFlag, Boolean> options){
		int killCount = 0;
		if(world == null){
			for(World w : Bukkit.getServer().getWorlds()) killCount += clearEntitiesByWorld(w, options);
			return killCount;
		}
		boolean tile = options.getOrDefault(KillFlag.TILE, false);
		boolean animals = options.getOrDefault(KillFlag.ANIMALS, false);
		boolean nearby = options.getOrDefault(KillFlag.NEARBY, true);//default=kill nearby
		boolean unique = options.getOrDefault(KillFlag.UNIQUE, false);
		boolean named = options.getOrDefault(KillFlag.NAMED, false);
		boolean geared = options.getOrDefault(KillFlag.EQUIPPED, false);
		Collection<? extends Entity> entitiesToScan =
				tile ? world.getEntities()
				: animals ? world.getLivingEntities()
				: world.getEntitiesByClass(Monster.class);
		for(Entity entity : entitiesToScan){
			if(!nearby){
				final long NEAR_DIST = 20*20;
				boolean near = false;
				for(Player p : Bukkit.getServer().getOnlinePlayers()){
					if(p.getWorld().getUID().equals(entity.getWorld().getUID()) && p.getLocation().distanceSquared(entity.getLocation()) < NEAR_DIST){
						near = true;
						break;
					}
				}
				if(near) continue;
			}
			if(!geared){
				if(entity instanceof LivingEntity){
					LivingEntity le = (LivingEntity) entity;
					// 2.1 is a special number that signifies that the item is of foreign origin (picked up)
					if(le.getEquipment().getItemInMainHandDropChance() >= 1 ||
						le.getEquipment().getChestplateDropChance() >= 1 ||
						le.getEquipment().getLeggingsDropChance() >= 1 ||
						le.getEquipment().getHelmetDropChance() >= 1 ||
						le.getEquipment().getBootsDropChance() >= 1) continue;
				}
			}
			if(!unique){
				if(entity instanceof LivingEntity){
					LivingEntity le = (LivingEntity) entity;
					if(le.isLeashed() || !le.getRemoveWhenFarAway()) continue;
				}
				else if(isUnique(entity.getType())) continue;
			}
			if(!named){
				if(entity instanceof LivingEntity){
					LivingEntity le = (LivingEntity) entity;
					if(le.getCustomName() != null) continue;
				}
			}
			if(entity instanceof LivingEntity && (animals || entity instanceof Monster)){
				entity.remove();
				++killCount;
			}
			else if(tile && entity instanceof LivingEntity == false){
				entity.remove();
				++killCount;
			}
		}
		return killCount;
	}
}