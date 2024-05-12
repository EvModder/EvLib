package net.evmodder.EvLib.extras;

import java.util.HashMap;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.TropicalFish.Pattern;

public final class EntityUtils{
	static int version = 13;
	final static HashMap<Material, EntityType> eggToEntity = new HashMap<>();
	final static HashMap<EntityType, Material> entityToEgg = new HashMap<>();
	static{
		try{version = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);}
		catch(IllegalArgumentException | ArrayIndexOutOfBoundsException e){System.err.println("EvLib failed to detect server version!");}
		if(version < 13) Bukkit.getLogger().severe("This version of EvLib does not support servers below 1.13!");

		for(EntityType eType : EntityType.values()){
			Material eggType = Material.getMaterial(eType.name()+"_SPAWN_EGG");
			if(eggType != null) eggToEntity.put(eggType, eType);
		}
		try{eggToEntity.put(Material.MOOSHROOM_SPAWN_EGG, EntityType.valueOf("MUSHROOM_COW"));} catch(IllegalArgumentException e){}// pre 1.20.5
		try{eggToEntity.put(Material.valueOf("ZOMBIE_PIGMAN_SPAWN_EGG"), EntityType.valueOf("PIG_ZOMBIE"));} catch(IllegalArgumentException e){}
		for(Entry<Material, EntityType> e : eggToEntity.entrySet()) entityToEgg.put(e.getValue(), e.getKey());
	}

	public static boolean isSpawnEgg(Material mat){return eggToEntity.keySet().contains(mat);}
	public static EntityType getSpawnedMob(Material spawnEggType){return eggToEntity.get(spawnEggType);}
	public static Material getSpawnEgg(EntityType eType){return entityToEgg.get(eType);}

	public static class PCC{
		public Pattern pattern;
		public DyeColor bodyColor, patternColor;
		public PCC(Pattern p, DyeColor color, DyeColor pColor){pattern = p; bodyColor = color; patternColor = pColor;}
		@Override public boolean equals(Object o){
			if(o == this) return true;
			if(o == null || o.getClass() != getClass()) return false;
			PCC pcc = (PCC)o;
			return pcc.pattern == pattern && pcc.bodyColor == bodyColor && pcc.patternColor == patternColor;
		}
		@Override public int hashCode(){
			return intFromPCC(pattern, bodyColor, patternColor);
		}
	}

	// Map from intPCC -> "BODY_C|PATTERN_C|PATTERN"
	private static final HashMap<Integer, String> cachedTropicalFishNames;
	// Index: translate-id -> English names for the 22 common tropical fish
	private static final String[] commonTropicalFishNames = new String[]{
			"Anemone", "Black Tang", "Blue Tang", "Butterflyfish", "Cichlid", "Clownfish", "Cotton Candy Betta", "Dottyback",
			/*US=*/"Emperor Red Snapper" /*UK="Red Emperor"*/, "Goatfish", "Moorish Idol", "Ornate Butterflyfish", "Parrotfish",
			"Queen Angelfish", "Red Cichlid", "Red Lipped Blenny", "Red Snapper", "Threadfin", "Tomato Clownfish", "Triggerfish",
			"Yellowtail Parrotfish", "Yellow Tang"
	};
	// Map from ENGLISH_NAME -> translate-id
	private static final HashMap<String, Integer> commonTropicalFishNamesReverse;
	// Map from intPCC -> translate-id for the 22 common tropical fish
	static final HashMap<Integer, Integer> commonTropicalFishIds;
	static{
		commonTropicalFishIds = new HashMap<>();
		commonTropicalFishIds.put(intFromPCC(Pattern.STRIPEY, DyeColor.ORANGE, DyeColor.GRAY), 0);
		commonTropicalFishIds.put(intFromPCC(Pattern.FLOPPER, DyeColor.GRAY, DyeColor.GRAY), 1);
		commonTropicalFishIds.put(intFromPCC(Pattern.FLOPPER, DyeColor.GRAY, DyeColor.BLUE), 2);
		commonTropicalFishIds.put(intFromPCC(Pattern.BRINELY, DyeColor.WHITE, DyeColor.GRAY), 3);
		commonTropicalFishIds.put(intFromPCC(Pattern.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY), 4);
		commonTropicalFishIds.put(intFromPCC(Pattern.KOB, DyeColor.ORANGE, DyeColor.WHITE), 5);
		commonTropicalFishIds.put(intFromPCC(Pattern.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE), 6);
		commonTropicalFishIds.put(intFromPCC(Pattern.BLOCKFISH, DyeColor.PURPLE, DyeColor.YELLOW), 7);
		commonTropicalFishIds.put(intFromPCC(Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.RED), 8);
		commonTropicalFishIds.put(intFromPCC(Pattern.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW), 9);
		commonTropicalFishIds.put(intFromPCC(Pattern.GLITTER, DyeColor.WHITE, DyeColor.GRAY), 10);
		commonTropicalFishIds.put(intFromPCC(Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE), 11);
		commonTropicalFishIds.put(intFromPCC(Pattern.DASHER, DyeColor.CYAN, DyeColor.PINK), 12);
		commonTropicalFishIds.put(intFromPCC(Pattern.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE), 13);
		commonTropicalFishIds.put(intFromPCC(Pattern.BETTY, DyeColor.RED, DyeColor.WHITE), 14);
		commonTropicalFishIds.put(intFromPCC(Pattern.SNOOPER, DyeColor.GRAY, DyeColor.RED), 15);
		commonTropicalFishIds.put(intFromPCC(Pattern.BLOCKFISH, DyeColor.RED, DyeColor.WHITE), 16);
		commonTropicalFishIds.put(intFromPCC(Pattern.FLOPPER, DyeColor.WHITE, DyeColor.YELLOW), 17);
		commonTropicalFishIds.put(intFromPCC(Pattern.KOB, DyeColor.RED, DyeColor.WHITE), 18);
		commonTropicalFishIds.put(intFromPCC(Pattern.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE), 19);
		commonTropicalFishIds.put(intFromPCC(Pattern.DASHER, DyeColor.CYAN, DyeColor.YELLOW), 20);
		commonTropicalFishIds.put(intFromPCC(Pattern.FLOPPER, DyeColor.YELLOW, DyeColor.YELLOW), 21);

		commonTropicalFishNamesReverse = new HashMap<>();
		for(int i=0; i<commonTropicalFishNames.length; ++i) commonTropicalFishNamesReverse.put(
				commonTropicalFishNames[i].toUpperCase().replace(' ', '_'), i);
	}
	private static final HashMap<DyeColor, String> fishColorNames;//Names used by color
	static{
		fishColorNames = new HashMap<>();
		fishColorNames.put(DyeColor.BLACK, "Black");
		fishColorNames.put(DyeColor.BLUE, "Blue");
		fishColorNames.put(DyeColor.BROWN, "Brown");
		fishColorNames.put(DyeColor.CYAN, "Teal");
		fishColorNames.put(DyeColor.GRAY, "Gray");
		fishColorNames.put(DyeColor.GREEN, "Green");
		fishColorNames.put(DyeColor.LIGHT_BLUE, "Sky");
		fishColorNames.put(DyeColor.LIGHT_GRAY, "Silver");
		fishColorNames.put(DyeColor.LIME, "Lime");
		fishColorNames.put(DyeColor.MAGENTA, "Magenta");
		fishColorNames.put(DyeColor.ORANGE, "Orange");
		fishColorNames.put(DyeColor.PINK, "Rose");
		fishColorNames.put(DyeColor.PURPLE, "Plum");
		fishColorNames.put(DyeColor.RED, "Red");
		fishColorNames.put(DyeColor.WHITE, "White");
		fishColorNames.put(DyeColor.YELLOW, "Yellow");
		cachedTropicalFishNames = new HashMap<>();
	}
	@Deprecated public static String getTropicalFishEnglishName(int pccInt){
		Integer id = commonTropicalFishIds.get(pccInt);
		if(id != null) return commonTropicalFishNames[id];
		String name = cachedTropicalFishNames.get(pccInt);
		if(name != null) return name;

		PCC pcc = PCCFromInt(pccInt);
		StringBuilder builder = new StringBuilder(fishColorNames.get(pcc.bodyColor));
		if(pcc.bodyColor != pcc.patternColor) builder.append('-').append(fishColorNames.get(pcc.patternColor));
		builder.append(' ').append(TextUtils.capitalizeAndSpacify(pcc.pattern.name(), '_'));
		name = builder.toString();
		cachedTropicalFishNames.put(pccInt, name); // Cache size can reach up to 2700 varieties (15*15*12)
		return name;
	}
	public static int getPCCInt(TropicalFish fish){
		return intFromPCC(fish.getPattern(), fish.getBodyColor(), fish.getPatternColor());
	}
	public static int getTropicalFishId(String commonTropicalFishName){
		return commonTropicalFishNamesReverse.get(commonTropicalFishName.toUpperCase().replace(' ', '_'));
	}
	public static Integer getCommonTropicalFishId(int pccInt){return commonTropicalFishIds.get(pccInt);}

	public static int intFromPCC(Pattern p, DyeColor c1, DyeColor c2){
		final int p1 = p.ordinal()/6, p2 = p.ordinal()%6;
		final int p3 = c1.ordinal(), p4 = c2.ordinal();
		return (p4 << 24) + (p3 << 16) + (p2 << 8) + (p1 << 0);
	}
	public static PCC PCCFromInt(int pccInt){
		int p4 = (byte)(pccInt >>> 24);
		int p3 = (byte)(pccInt >>> 16);
		int p2 = (byte)(pccInt >>> 8);
		int p1 = (byte)pccInt;
		return new PCC(Pattern.values()[p1*6+p2], DyeColor.values()[p3], DyeColor.values()[p4]);
	}

	//TODO: Keep up-to-date with Minecraft updates, and test for regressions
	public static EntityType getEntityByName(String name){
		if(name.toUpperCase().startsWith("MHF_")){
			try{
				name = (String)Class.forName("net.evmodder.EvLib.extras.HeadUtils")
						.getMethod("normalizedNameFromMHFName", String.class).invoke(/*static method, so=*/null, name);
			}
			catch(ReflectiveOperationException e){}
		}
		name = name.toUpperCase().replace(' ', '_');
		try{return EntityType.valueOf(name);}
		catch(IllegalArgumentException ex){
			name = name.replace("_", "");
			switch(name){
				case "ZOMBIEPIGMAN": return EntityType.valueOf("PIG_ZOMBIE");

				// Obsolete in 1.20.5+
				case "MOOSHROOM": return EntityType.valueOf("MUSHROOM_COW");
				case "MUSHROOMCOW": return EntityType.valueOf("MOOSHROOM");
				case "SNOWGOLEM": return EntityType.valueOf("SNOWMAN");
				case "SNOWMAN": return EntityType.valueOf("SNOW_GOLEM");
				case "LEASHKNOT": return EntityType.valueOf("LEASH_HITCH");
				case "LEASHHITCH": return EntityType.valueOf("LEASH_KNOT");
				case "TNTMINECART": return EntityType.valueOf("MINECART_TNT");
				case "MINECARTTNT": return EntityType.valueOf("TNT_MINECART");
				case "CHESTMINECART": return EntityType.valueOf("MINECART_CHEST");
				case "MINECARTCHEST": return EntityType.valueOf("CHEST_MINECART");
				case "HOPPERMINECART": return EntityType.valueOf("MINECART_HOPPER");
				case "MINECARTHOPPER": return EntityType.valueOf("HOPPER_MINECART");
				case "FURNACEMINECART": return EntityType.valueOf("MINECART_FURNACE");
				case "MINECARTFURNACE": return EntityType.valueOf("FURNACE_MINECART");
				case "COMMANDBLOCKMINECART": return EntityType.valueOf("MINECART_COMMAND");
				case "MINECART_COMMAND": return EntityType.valueOf("COMMAND_BLOCK_MINECART");
				case "SPAWNERMINECART": return EntityType.valueOf("MINECART_MOB_SPAWNER");
				case "MINECARTMOBSPAWNER": return EntityType.valueOf("SPAWNER_MINECART");
			}
			for(EntityType t : EntityType.values()) if(t.name().replace("_", "").equals(name)) return t;
			return EntityType.UNKNOWN;
		}
	}

	//TODO: Keep up-to-date with Minecraft updates, and test for regressions (obsolete in 1.20.5+)
	public static String getNormalizedEntityName(String name){
		switch(name.toUpperCase()){
			case "MUSHROOM_COW": return "mooshroom";
			case "SNOWMAN": return "snow_golem";
			case "PIG_ZOMBIE": return "zombie_pigman";
			case "LEASH_HITCH": return "leash_knot";
			case "MINECART_TNT": return "tnt_minecart";
			case "MINECART_CHEST": return "chest_minecart";
			case "MINECART_HOPPER": return "hopper_minecart";
			case "MINECART_FURNACE": return "furnace_minecart";
			case "MINECART_COMMAND": return "command_block_minecart";
			case "MINECART_MOB_SPAWNER": return "spawner_minecart";
			default: return name.toLowerCase();
		}
	}

	public static boolean isSkeletal(EntityType eType){
		switch(eType){
			case SKELETON:
			case SKELETON_HORSE:
			case WITHER_SKELETON:
			case STRAY:
				return true;
			default:
				return false;
		}
	}

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
}