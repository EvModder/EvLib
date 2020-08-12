package net.evmodder.EvLib;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EvUtils{// version = 1.2, 2=moved many function to HeadUtils,WebUtils,TextUtils
	public static Collection<ItemStack> getEquipmentGuaranteedToDrop(LivingEntity entity){//TODO: move to EntityUtils
		ArrayList<ItemStack> itemsThatWillDrop = new ArrayList<>();
		EntityEquipment equipment = entity.getEquipment();
		if(equipment.getItemInMainHandDropChance() >= 1f) itemsThatWillDrop.add(equipment.getItemInMainHand());
		if(equipment.getItemInOffHandDropChance() >= 1f) itemsThatWillDrop.add(equipment.getItemInOffHand());
		if(equipment.getChestplateDropChance() >= 1f) itemsThatWillDrop.add(equipment.getChestplate());
		if(equipment.getLeggingsDropChance() >= 1f) itemsThatWillDrop.add(equipment.getLeggings());
		if(equipment.getHelmetDropChance() >= 1f) itemsThatWillDrop.add(equipment.getHelmet());
		if(equipment.getBootsDropChance() >= 1f) itemsThatWillDrop.add(equipment.getBoots());
		return itemsThatWillDrop;
	}

	public static Collection<Advancement> getVanillaAdvancements(Player p){
		return StreamSupport
				.stream(Spliterators.spliteratorUnknownSize(p.getServer().advancementIterator(), Spliterator.IMMUTABLE),
						/*parallel=*/true)
				.filter(adv -> adv.getKey().getNamespace().equals(NamespacedKey.MINECRAFT) && p.getAdvancementProgress(adv).isDone())
				.collect(Collectors.toList());
	}
	public static Collection<Advancement> getVanillaAdvancements(Collection<String> include){
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(Bukkit.getServer().advancementIterator(), Spliterator.IMMUTABLE),
				/*parallel=*/true).filter(adv -> {
					int i = adv.getKey().getKey().indexOf('/');
					return i != -1 && adv.getKey().getNamespace().equals(NamespacedKey.MINECRAFT)
							&& include.contains(adv.getKey().getKey().substring(0, i));
				}).collect(Collectors.toList());
	}
	public static Collection<Advancement> getVanillaAdvancements(Player p, Collection<String> include){
		return getVanillaAdvancements(p).stream().filter(adv -> {
			int i = adv.getKey().getKey().indexOf('/');
			return i != -1 && include.contains(adv.getKey().getKey().substring(0, i));
		}).collect(Collectors.toList());
	}

	public static int maxCapacity(Inventory inv, Material item){
		int sum = 0;
		for(ItemStack i : inv.getContents()){
			if(i == null || i.getType() == Material.AIR) sum += item.getMaxStackSize();
			else if(i.getType() == item) sum += item.getMaxStackSize() - i.getAmount();
		}
		return sum;
	}

	public static String getRegionFolder(World world){
		switch(world.getEnvironment()){
			case NORMAL:
				return "./"+world.getName()+"/region/";
			case NETHER:
				return "./"+world.getName()+"/DIM-1/region/";
			case THE_END:
				return "./"+world.getName()+"/DIM1/region/";
			default:
				return null;
		}
	}

	public static double crossDimensionalDistanceSquared(Location a, Location b){
		if(a == null || b == null) return Double.MAX_VALUE;
		if(a.getWorld().getUID().equals(b.getWorld().getUID())) return a.distanceSquared(b);
		if(a.getWorld().getEnvironment() == Environment.THE_END || b.getWorld().getEnvironment() == Environment.THE_END) return Double.MAX_VALUE;
		if(!a.getWorld().getName().startsWith(b.getWorld().getName()) && !b.getWorld().getName().startsWith(a.getWorld().getName())) return Double.MAX_VALUE;
		// By this point, we have overworld & nether (for the same world)
		if(a.getWorld().getEnvironment() == Environment.NETHER) return new Location(b.getWorld(), a.getX()*8, a.getY(), a.getZ()*8).distanceSquared(b);
		else return new Location(a.getWorld(), b.getX()*8, b.getY(), b.getZ()*8).distanceSquared(a);
	}

	public static ArrayList<Player> getNearbyPlayers(Location loc, int max_dist, boolean allowCrossDimension){//+
		max_dist = max_dist * max_dist;
		ArrayList<Player> ppl = new ArrayList<Player>();
		for(Player p : Bukkit.getServer().getOnlinePlayers()){
			double dist = allowCrossDimension ? crossDimensionalDistanceSquared(p.getLocation(), loc)
					: p.getWorld().getUID().equals(loc.getWorld().getUID()) ? p.getLocation().distanceSquared(loc) : Double.MAX_VALUE;
			if(dist < max_dist) ppl.add(p);
		}
		return ppl;
	}

	public static Location getClosestBlock(Location start, int MAX_DIST, Function<Block, Boolean> test){
		if(test.apply(start.getBlock())) return start;
		World w  = start.getWorld();
		int cX = start.getBlockX(), cY = start.getBlockY(), cZ = start.getBlockZ();
		for(int dist = 1; dist <= MAX_DIST; ++dist){
			int mnX = cX-dist, mxX = cX+dist;
			int mnZ = cZ-dist, mxZ = cZ+dist;
			int mnY = Math.max(cY-dist, 0), mxY = Math.min(cY+dist, 256);
			for(int y=mxY; y>=mnY; --y) for(int z=mnZ; z<=mxZ; ++z){
				if(test.apply(w.getBlockAt(mnX, y, z))) return new Location(w, mnX, y, z);
				if(test.apply(w.getBlockAt(mxX, y, z))) return new Location(w, mxX, y, z);
			}
			for(int x=mnX; x<=mxX; ++x) for(int z=mnZ; z<=mxZ; ++z){
				if(test.apply(w.getBlockAt(x, mnY, z))) return new Location(w, x, mnY, z);
				if(test.apply(w.getBlockAt(x, mxY, z))) return new Location(w, x, mxY, z);
			}
			for(int x=mnX; x<=mxX; ++x) for(int y=mxY; y>=mnY; --y){
				if(test.apply(w.getBlockAt(x, y, mnZ))) return new Location(w, x, y, mnZ);
				if(test.apply(w.getBlockAt(x, y, mxZ))) return new Location(w, x, y, mxZ);
			}
		}
		return null;
	}

	public static List<Block> getConnectedBlocks(Block block0, Function<Block, Boolean> test, 
			List<BlockFace> dirs, int MAX_SIZE){//+
		HashSet<Block> visited = new HashSet<Block>();
		List<Block> results = new ArrayList<Block>();
		ArrayDeque<Block> toProcess = new ArrayDeque<Block>();
		toProcess.addLast(block0);
		while(results.size() < MAX_SIZE && !toProcess.isEmpty()){
			Block b = toProcess.pollFirst();
			if(b != null && test.apply(b) && !visited.contains(b)){
				results.add(b);
				visited.add(b);
				for(BlockFace dir : dirs) toProcess.addLast(b.getRelative(dir));
			}
		}
		return results;
	}
}