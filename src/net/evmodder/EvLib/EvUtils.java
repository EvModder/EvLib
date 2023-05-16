package net.evmodder.EvLib;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
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
import org.bukkit.util.Vector;

public final class EvUtils{// version = 1.2, 2=moved many function to HeadUtils,WebUtils,TextUtils
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
	// Apparently this reflection is necessary for getting wither skulls worn by a wither skeleton :/ - last checked in 1.16? (bug reported: link?)
//	final static RefClass craftLivingEntityClazz = ReflectionUtils.getRefClass("{cb}.entity.CraftLivingEntity");
//	final static RefMethod livingEntityGetHandleMethod = craftLivingEntityClazz.getMethod("getHandle");
//	final static RefClass nmsEntityLivingClazz = ReflectionUtils.getRefClass("{nms}.EntityLiving");
//	final static RefClass nmsEnumItemSlotClazz = ReflectionUtils.getRefClass("{nms}.EnumItemSlot");
//	final static Object nmsEnumItemSlotHead = nmsEnumItemSlotClazz.getMethod("valueOf", String.class).call("HEAD");
//	final static RefMethod entityLivingGetEquipmentMethod = nmsEntityLivingClazz.getMethod("getEquipment", nmsEnumItemSlotClazz);
//	final static RefConstructor craftItemStackCnstr = craftItemStackClazz.getConstructor(nmsItemStackClazz);
//	public static Collection<ItemStack> getEquipmentGuaranteedToDrop(LivingEntity entity){//TODO: move to EntityUtils
//		ArrayList<ItemStack> itemsThatWillDrop = new ArrayList<>();
//		EntityEquipment equipment = entity.getEquipment();
//		if(equipment.getItemInMainHandDropChance() >= 1f) itemsThatWillDrop.add(equipment.getItemInMainHand());
//		if(equipment.getItemInOffHandDropChance() >= 1f) itemsThatWillDrop.add(equipment.getItemInOffHand());
//		if(equipment.getChestplateDropChance() >= 1f) itemsThatWillDrop.add(equipment.getChestplate());
//		if(equipment.getLeggingsDropChance() >= 1f) itemsThatWillDrop.add(equipment.getLeggings());
//		Bukkit.getLogger().info("helmet drop chance: "+equipment.getHelmetDropChance());
//		if(equipment.getHelmetDropChance() >= 1f) itemsThatWillDrop.add(
//				(ItemStack)craftItemStackCnstr
//				.create(entityLivingGetEquipmentMethod.of(
//						livingEntityGetHandleMethod
//						.of(entity).call())
//						.call(nmsEnumItemSlotHead))
//		);
//		if(equipment.getBootsDropChance() >= 1f) itemsThatWillDrop.add(equipment.getBoots());
//		return itemsThatWillDrop;
//	}

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

	private static Random globalRand;
	public static void dropItemNaturally(Location loc, ItemStack item, Random rand){
		if(rand == null) rand = globalRand = (globalRand != null ? globalRand : new Random());
		loc.getWorld().dropItem(loc, item).setVelocity(new Vector(rand.nextDouble()/5D - 0.1D, 0.2D, rand.nextDouble()/5D - 0.1D));
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
		ArrayList<Player> ppl = new ArrayList<>();
		for(Player p : Bukkit.getServer().getOnlinePlayers()){
			double dist = allowCrossDimension ? crossDimensionalDistanceSquared(p.getLocation(), loc)
					: p.getWorld().getUID().equals(loc.getWorld().getUID()) ? p.getLocation().distanceSquared(loc) : Double.MAX_VALUE;
			if(dist < max_dist) ppl.add(p);
		}
		return ppl;
	}

	public static Location getClosestBlock(Location start, int MAX_DIST, Function<Block, Boolean> test){
		if(test.apply(start.getBlock())) return start/*.clone()*/;
		if(test.apply(start.getBlock().getRelative(BlockFace.UP))) return start.clone().add(0, 1, 0);
		if(test.apply(start.getBlock().getRelative(BlockFace.NORTH))) return start.clone().add(0, 0, -1);
		if(test.apply(start.getBlock().getRelative(BlockFace.EAST))) return start.clone().add(1, 0, 0);
		if(test.apply(start.getBlock().getRelative(BlockFace.SOUTH))) return start.clone().add(0, 0, 1);
		if(test.apply(start.getBlock().getRelative(BlockFace.WEST))) return start.clone().add(-1, 0, 0);
		World w  = start.getWorld();
		int cX = start.getBlockX(), cY = start.getBlockY(), cZ = start.getBlockZ();
		Location closestLoc = null;
		double closestDistSq = Double.MAX_VALUE;
		for(int dist = 1; dist <= MAX_DIST; ++dist){
			int mnX = cX-dist, mxX = cX+dist;
			int mnZ = cZ-dist, mxZ = cZ+dist;
			int mnY = Math.max(cY-dist, 0), mxY = Math.min(cY+dist, 256);
			Location l;
			double d;
			for(int y=mxY; y>=mnY; --y) for(int z=mnZ; z<=mxZ; ++z){
				if(test.apply(w.getBlockAt(mnX, y, z)) && (d = (l = new Location(w, mnX, y, z)).distanceSquared(start)) < closestDistSq){
					closestDistSq = d; closestLoc = l;
				}
				if(test.apply(w.getBlockAt(mxX, y, z)) && (d = (l = new Location(w, mxX, y, z)).distanceSquared(start)) < closestDistSq){
					closestDistSq = d; closestLoc = l;
				}
			}
			for(int x=mnX; x<=mxX; ++x) for(int z=mnZ; z<=mxZ; ++z){
				if(test.apply(w.getBlockAt(x, mnY, z)) && (d = (l = new Location(w, x, mnY, z)).distanceSquared(start)) < closestDistSq){
					closestDistSq = d; closestLoc = l;
				}
				if(test.apply(w.getBlockAt(x, mxY, z)) && (d = (l = new Location(w, x, mxY, z)).distanceSquared(start)) < closestDistSq){
					closestDistSq = d; closestLoc = l;
				}
			}
			for(int x=mnX; x<=mxX; ++x) for(int y=mxY; y>=mnY; --y){
				if(test.apply(w.getBlockAt(x, y, mnZ)) && (d = (l = new Location(w, x, y, mnZ)).distanceSquared(start)) < closestDistSq){
					closestDistSq = d; closestLoc = l;
				}
				if(test.apply(w.getBlockAt(x, y, mxZ)) && (d = (l = new Location(w, x, y, mxZ)).distanceSquared(start)) < closestDistSq){
					closestDistSq = d; closestLoc = l;
				}
			}
			if(closestLoc != null/* && closestDistSq <= MAX_DIST*MAX_DIST*/) return closestLoc;
		}
		return null;
	}

	public static List<Block> getConnectedBlocks(Block block0, Function<Block, Boolean> test, List<BlockFace> dirs, int MAX_SIZE){//+
		HashSet<Block> visited = new HashSet<>();
		List<Block> results = new ArrayList<>();
		ArrayDeque<Block> toProcess = new ArrayDeque<>();
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