package net.evmodder.EvLib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class EvUtils{// version = 1.1
	public static String getNormalizedName(EntityType entity){
		//TODO: improve this algorithm / test for errors
		switch(entity){
		case PIG_ZOMBIE:
			return "Zombie Pigman";
		case MUSHROOM_COW:
			return "Mooshroom";
		default:
			boolean wordStart = true;
			char[] arr = entity.name().toCharArray();
			for(int i=0; i<arr.length; ++i){
				if(wordStart) wordStart = false;
				else if(arr[i] == '_' || arr[i] == ' '){arr[i] = ' '; wordStart = true;}
				else arr[i] = Character.toLowerCase(arr[i]);
			}
			return new String(arr);
		}
	}

	static long[] scale = new long[]{31536000000L, /*2628000000L,*/ 604800000L, 86400000L, 3600000L, 60000L, 1000L};
	static char[] units = new char[]{'y', /*'m',*/ 'w', 'd', 'h', 'm', 's'};
	public static String formatTime(long time, ChatColor timeColor, ChatColor unitColor){
		return formatTime(time, timeColor, unitColor, scale, units);
	}
	public static String formatTime(long time, ChatColor timeColor, ChatColor unitColor, long[] scale, char[] units){
		int i = 0;
		while(time < scale[i]) ++i;
		StringBuilder builder = new StringBuilder("");
		for(; i < scale.length-1; ++i){
			builder.append(timeColor).append(time / scale[i]).append(unitColor).append(units[i]).append(", ");
			time %= scale[i];
		}
		return builder.append(timeColor).append(time / scale[scale.length-1])
					  .append(unitColor).append(units[units.length-1]).toString();
	}

	public static Location getLocationFromString(String s){
		String[] data = s.split(",");
		World world = org.bukkit.Bukkit.getWorld(data[0]);
		if(world != null){
			try{return new Location(world,
					Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));}
			catch(NumberFormatException ex){}
		}
		return null;
	}
	public static Location getLocationFromString(World w, String s){
		String[] data = s.split(",");
		try{return new Location(w,
				Double.parseDouble(data[data.length-3]),
				Double.parseDouble(data[data.length-2]),
				Double.parseDouble(data[data.length-1]));}
		catch(ArrayIndexOutOfBoundsException | NumberFormatException ex){return null;}
	}

	public static Collection<Advancement> getVanillaAdvancements(Player p){
		Vector<Advancement> advs = new Vector<Advancement>();
		Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
		while(it.hasNext()){
			Advancement adv = it.next();
			if(adv.getKey().getNamespace().equals(NamespacedKey.MINECRAFT) 
					&& p.getAdvancementProgress(adv).isDone())
				advs.add(adv);
		}
		return advs;
	}
	public static Collection<Advancement> getVanillaAdvancements(Player p, Collection<String> include){
		Vector<Advancement> advs = new Vector<Advancement>();
		Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
		while(it.hasNext()){
			Advancement adv = it.next();
			int i = adv.getKey().getKey().indexOf('/');
			if(adv.getKey().getNamespace().equals(NamespacedKey.MINECRAFT) && i != -1
					&& include.contains(adv.getKey().getKey().substring(0, i))
					&& p.getAdvancementProgress(adv).isDone())
				advs.add(adv);
		}
		return advs;
	}
	public static Collection<Advancement> getVanillaAdvancements(Collection<String> include){
		Vector<Advancement> advs = new Vector<Advancement>();
		Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
		while(it.hasNext()){
			Advancement adv = it.next();
			int i = adv.getKey().getKey().indexOf('/');
			if(adv.getKey().getNamespace().equals(NamespacedKey.MINECRAFT) && i != -1
					&& include.contains(adv.getKey().getKey().substring(0, i)))
				advs.add(adv);
		}
		return advs;
	}

	public static Collection<ItemStack> getEquipmentGuaranteedToDrop(LivingEntity entity){
		ArrayList<ItemStack> itemsThatWillDrop = new ArrayList<>();
		EntityEquipment equipment = entity.getEquipment();
		if(equipment.getItemInMainHandDropChance() >= 1) itemsThatWillDrop.add(equipment.getItemInMainHand());
		if(equipment.getItemInOffHandDropChance() >= 1) itemsThatWillDrop.add(equipment.getItemInOffHand());
		if(equipment.getChestplateDropChance() >= 1) itemsThatWillDrop.add(equipment.getChestplate());
		if(equipment.getLeggingsDropChance() >= 1) itemsThatWillDrop.add(equipment.getLeggings());
		if(equipment.getHelmetDropChance() >= 1) itemsThatWillDrop.add(equipment.getHelmet());
		if(equipment.getBootsDropChance() >= 1) itemsThatWillDrop.add(equipment.getBoots());
		return itemsThatWillDrop;
	}

	public static boolean notFar(Location from, Location to){
		int x1 = from.getBlockX(), y1 = from.getBlockY(), z1 = from.getBlockZ(),
			x2 = to.getBlockX(), y2 = to.getBlockY(), z2 = to.getBlockZ();

		return (Math.abs(x1 - x2) < 20 &&
				Math.abs(y1 - y2) < 15 &&
				Math.abs(z1 - z2) < 20 &&
				from.getWorld().getName().equals(to.getWorld().getName()));
	}

	public static Location getClosestBlock(Location start, int MAX_DIST, Function<Block, Boolean> test){
		if(test.apply(start.getBlock())) return start;
		World w  = start.getWorld();
		int cX = start.getBlockX(), cY = start.getBlockY(), cZ = start.getBlockZ();
		for(int dist = 1; dist < MAX_DIST; ++dist){
			int mnX = cX-dist, mxX = cX+dist;
			int mnZ = cZ-dist, mxZ = cZ+dist;
			int mnY = Math.max(cY-dist, 0), mxY = Math.min(cY+dist, 256);
			for(int y=mnY; y<=mxY; ++y) for(int z=mnZ; z<=mxZ; ++z){
				if(test.apply(w.getBlockAt(mnX, y, z))) return new Location(w, mnX, y, z);
				if(test.apply(w.getBlockAt(mxX, y, z))) return new Location(w, mxX, y, z);
			}
			for(int x=mnX; x<=mxX; ++x) for(int z=mnZ; z<=mxZ; ++z){
				if(test.apply(w.getBlockAt(x, mnY, z))) return new Location(w, x, mnY, z);
				if(test.apply(w.getBlockAt(x, mxY, z))) return new Location(w, x, mxY, z);
			}
			for(int x=mnX; x<=mxX; ++x) for(int y=mnY; y<=mxY; ++y){
				if(test.apply(w.getBlockAt(x, y, mnZ))) return new Location(w, x, y, mnZ);
				if(test.apply(w.getBlockAt(x, y, mxZ))) return new Location(w, x, y, mxZ);
			}
		}
		return null;
	}

	public static String executePost(String post){
		URLConnection connection = null;
		try{
			connection = new URL(post).openConnection();
			connection.setUseCaches(false);
			connection.setDoOutput(true);

			// Get response
//			Scanner s = new Scanner(connection.getInputStream()).useDelimiter("\\A");
//			String response = s.hasNext() ? s.next() : null;
//			s.close();
//			return response;
			BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = rd.readLine();
			rd.close();
			return line;
		}
		catch(IOException e){
			System.out.println(e.getStackTrace());
			return null;
		}
	}

	public static int maxCapacity(Inventory inv, Material item){
		int sum = 0;
		for(ItemStack i : inv.getContents()){
			if(i == null || i.getType() == Material.AIR) sum += item.getMaxStackSize();
			else if(i.getType() == item) sum += item.getMaxStackSize() - i.getAmount();
		}
		return sum;
	}

	public static Vector<String> installedEvPlugins(){
		Vector<String> evPlugins = new Vector<String>();
		for(Plugin pl : Bukkit.getServer().getPluginManager().getPlugins()){
			try{
				@SuppressWarnings("unused")
				String ver = pl.getClass().getField("EvLib_ver").get(null).toString();
				evPlugins.add(pl.getName());
				//TODO: potentially return list of different EvLib versions being used
			}
			catch(IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e){}
		}
		return evPlugins;
	}

	static HashMap<String, Boolean> exists = new HashMap<String, Boolean>();
	public static boolean checkExists(String player){
		if(!exists.containsKey(player)){
			//Sample data (braces included): {"id":"34471e8dd0c547b9b8e1b5b9472affa4","name":"EvDoc"}
			String data = executePost("https://api.mojang.com/users/profiles/minecraft/"+player);
			exists.put(player, data != null);
		}
		return exists.get(player);
	}

	public static ArrayList<Player> getNearbyPlayers(Location loc, int range){//+
		range = range*range;
		ArrayList<Player> ppl = new ArrayList<Player>();
		for(Player p : Bukkit.getServer().getOnlinePlayers()){
			if(p.getWorld().getName().equals(loc.getWorld().getName()) && p.getLocation().distanceSquared(loc) > range)
				ppl.add(p);
		}
		return ppl;
	}

	public static String capitalizeAndSpacify(String str, char toSpace){
		StringBuilder builder = new StringBuilder("");
		boolean lower = false;
		for(char ch : str.toCharArray()){
			if(ch == toSpace){builder.append(' '); lower=false;}
			else if(lower) builder.append(Character.toLowerCase(ch));
			else{builder.append(Character.toUpperCase(ch)); lower = true;}
		}
		return builder.toString();
	}

	/*public static Gene getPandaTrait(Panda panda){
		if(panda.getMainGene() == panda.getHiddenGene()) return panda.getMainGene();
		switch(panda.getMainGene()){
			case BROWN:
			case WEAK:
				return Gene.NORMAL;
			default:
				return panda.getMainGene();
		}
	}*/
	public static String getPandaTrait(String mainGene, String hiddenGene){
		if(mainGene.equals(hiddenGene)) return mainGene;
		switch(mainGene){
			case "BROWN":
			case "WEAK":
				return "NORMAL";
			default:
				return mainGene;
		}
	}

	public static boolean isHead(Material type){
		switch(type){
			case CREEPER_HEAD:
			case CREEPER_WALL_HEAD:
			case DRAGON_HEAD:
			case DRAGON_WALL_HEAD:
			case PLAYER_HEAD:
			case PLAYER_WALL_HEAD:
			case ZOMBIE_HEAD:
			case ZOMBIE_WALL_HEAD:
			case SKELETON_SKULL:
			case SKELETON_WALL_SKULL:
			case WITHER_SKELETON_SKULL:
			case WITHER_SKELETON_WALL_SKULL:
				return true;
			default:
				return false;
		}
	}
	public static boolean isPlayerHead(Material type){
		return type == Material.PLAYER_HEAD || type == Material.PLAYER_WALL_HEAD;
	}

	public static boolean hasGrummName(Nameable e){
		return e.getCustomName() != null && !(e instanceof Player) &&
				(e.getCustomName().equals("Dinnerbone") || e.getCustomName().equals("Grumm"));
	}

	public static EntityType getEntityByName(String name){
		//TODO: improve this algorithm / test for errors
		if(name.toUpperCase().startsWith("MHF_")) name = normalizedNameFromMHFName(name);
		name = name.toUpperCase().replace(' ', '_');

		try{EntityType type = EntityType.valueOf(name.toUpperCase()); return type;}
		catch(IllegalArgumentException ex){}
		name = name.replace("_", "");
		for(EntityType t : EntityType.values()) if(t.name().replace("_", "").equals(name)) return t;
		if(name.equals("ZOMBIEPIGMAN")) return EntityType.PIG_ZOMBIE;
		else if(name.equals("MOOSHROOM")) return EntityType.MUSHROOM_COW;
		return EntityType.UNKNOWN;
	}
	public static String getMHFHeadName(String eType){
		switch(eType){
		case "MAGMA_CUBE":
			return "MHF_LavaSlime";
		case "IRON_GOLEM":
			return "MHF_Golem";
		case "MOOSHROOM":
			return "MHF_MushroomCow";
		case "WITHER_SKELETON":
			return "MHF_Wither";
		default:
			StringBuilder builder = new StringBuilder("MHF_");
			boolean lower = false;
			for(char ch : eType.toCharArray()){
				if(ch == '_') lower = false;
				else if(lower) builder.append(Character.toLowerCase(ch));
				else{builder.append(Character.toUpperCase(ch)); lower = true;}
			}
			return builder.toString();
		}
	}
	public static String getNormalizedName(String eType){
		//TODO: improve this algorithm / test for errors
		switch(eType){
		case "PIG_ZOMBIE":
			return "Zombie Pigman";
		case "MUSHROOM_COW":
			return "Mooshroom";
		case "TROPICAL_FISH"://TODO: 22 varieties, e.g. Clownfish
		default:
			return capitalizeAndSpacify(eType, '_');
		}
	}
	public static String normalizedNameFromMHFName(String mhfName){
		mhfName = mhfName.substring(4);
		String mhfCompact = mhfName.replace("_", "").replace(" ", "").toLowerCase();
		if(mhfCompact.equals("lavaslime")) return "Magma Cube";
		else if(mhfCompact.equals("golem")) return "Iron Golem";
		else if(mhfCompact.equals("pigzombie")) return "Zombie Pigman";
		else if(mhfCompact.equals("mushroomcow")) return "Mooshroom";
		else{
			char[] chars = mhfName.toCharArray();
			StringBuilder name = new StringBuilder("").append(chars[0]);
			for(int i=1; i<chars.length; ++i){
				if(Character.isUpperCase(chars[i]) && chars[i-1] != ' ') name.append(' ');
				name.append(chars[i]);
			}
			return name.toString();
		}
	}

	private static byte pickaxeNumber(Material pickType){
		switch(pickType){
			case DIAMOND_PICKAXE:
				return 4;
			case IRON_PICKAXE:
				return 3;
			case STONE_PICKAXE:
				return 2;
			case WOODEN_PICKAXE:
			case GOLDEN_PICKAXE:
				return 1;
			default:
				return 0;
		}
	}
	public static boolean pickIsAtLeast(Material pickType, Material needPick){//+
		return pickaxeNumber(pickType) >= pickaxeNumber(needPick);
	}
	private static byte swordNumber(Material swordType){
		switch(swordType){
			case DIAMOND_SWORD:
				return 4;
			case IRON_SWORD:
				return 3;
			case STONE_SWORD:
				return 2;
			case GOLDEN_SWORD:
			case WOODEN_SWORD:
				return 1;
			default:
				return 0;
		}
	}
	public static boolean swordIsAtLeast(Material swordType, Material needSword){//+
		return swordNumber(swordType) >= swordNumber(needSword);
	}

	public static List<Block> getBlockStructure(Block block0, Function<Block, Boolean> test, 
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

	final static List<BlockFace> dirs6 = Arrays.asList(BlockFace.UP, BlockFace.DOWN,
			BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);//+
	public static ArrayDeque<Container> getStorageDepot(Location loc){//+
		return getBlockStructure(loc.getBlock(), (b -> b.getState() instanceof Container), dirs6, 1000).stream()
				.map(b -> (Container)b.getState()).collect(
						Collector.of(ArrayDeque::new,
								ArrayDeque::add,
								(a, b) -> {a.addAll(b); return a;}
						//(deq, t) -> deq.addFirst(t),
						//(d1, d2) -> {d2.addAll(d1); return d2;}
						));
	}

	public static boolean checkHeight(Material blockType, double offset){
		switch(blockType){//TODO: RaysWorks world w/ every block height & TypeUtils.isStair() etc
			case ACACIA_SLAB:
				return offset == .5;
			case ACACIA_STAIRS:
				return offset == .5 || offset == 1;
			case ACACIA_FENCE:
				return offset == 1.5;
			default:
				return offset == 1;
		}
	}

	public static boolean isOnGround(Location loc){//for parkour world anti-cheat &/| checkpoints
		if(loc == null) return false;
		boolean useBelow = loc.getBlock() == null || loc.getBlock().isEmpty();
		if(useBelow){
			loc.setY(loc.getY() - 1D);
			if(loc.getBlock() == null || loc.getBlock().isEmpty()) return false;
			return checkHeight(loc.getBlock().getType(), 1D + loc.getY() - loc.getBlockY());
		}
		return checkHeight(loc.getBlock().getType(), loc.getY() - loc.getBlockY());
	}
}