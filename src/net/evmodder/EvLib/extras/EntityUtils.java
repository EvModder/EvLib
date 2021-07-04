package net.evmodder.EvLib.extras;

import java.util.HashMap;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.TropicalFish.Pattern;

public class EntityUtils{
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
		eggToEntity.put(Material.MOOSHROOM_SPAWN_EGG, EntityType.MUSHROOM_COW);
		try{eggToEntity.put(Material.valueOf("ZOMBIE_PIGMAN_SPAWN_EGG"), EntityType.valueOf("PIG_ZOMBIE"));} catch(IllegalArgumentException e){}
		for(Entry<Material, EntityType> e : eggToEntity.entrySet()) entityToEgg.put(e.getValue(), e.getKey());
	}

	public static boolean isSpawnEgg(@Nonnull Material mat){return eggToEntity.keySet().contains(mat);}
	public static EntityType getSpawnedMob(@Nonnull Material spawnEggType){return eggToEntity.get(spawnEggType);}
	public static Material getSpawnEgg(@Nonnull EntityType eType){return entityToEgg.get(eType);}

	public static class CCP{
		public DyeColor bodyColor, patternColor;
		public Pattern pattern;
		public CCP(DyeColor color, DyeColor pColor, Pattern p){bodyColor = color; patternColor = pColor; pattern = p;}
		@Override public boolean equals(Object o){
			if(o == this) return true;
			if(o == null || o.getClass() != getClass()) return false;
			CCP ccp = (CCP)o;
			return ccp.bodyColor == bodyColor && ccp.patternColor == patternColor && ccp.pattern == pattern;
		}
		@Override public int hashCode(){
			return bodyColor.hashCode() + 16*(patternColor.hashCode() + 16*pattern.hashCode());
		}
	}
	final static HashMap<CCP, Integer> commonTropicalFishIds;// Map from Name -> translation key id for the 22 common tropical fish
	final static HashMap<CCP, String> commonTropicalFishNames;//Names for the 22 common tropical fish
	final static HashMap<String, CCP> commonTropicalFishNamesReverse;
	final static HashMap<CCP, String> cachedTropicalFishNames;//Cached names for the 2700 other varieties (15*15*12)
	@Deprecated static final HashMap<DyeColor, String> fishColorNames;//Names used by color
	static{
		commonTropicalFishIds = new HashMap<CCP, Integer>();
		commonTropicalFishIds.put(new CCP(DyeColor.ORANGE, DyeColor.GRAY, Pattern.STRIPEY), 0);
		commonTropicalFishIds.put(new CCP(DyeColor.GRAY, DyeColor.GRAY, Pattern.FLOPPER), 1);
		commonTropicalFishIds.put(new CCP(DyeColor.GRAY, DyeColor.BLUE, Pattern.FLOPPER), 2);
		commonTropicalFishIds.put(new CCP(DyeColor.WHITE, DyeColor.GRAY, Pattern.BRINELY), 3);
		commonTropicalFishIds.put(new CCP(DyeColor.BLUE, DyeColor.GRAY, Pattern.SUNSTREAK), 4);
		commonTropicalFishIds.put(new CCP(DyeColor.ORANGE, DyeColor.WHITE, Pattern.KOB), 5);
		commonTropicalFishIds.put(new CCP(DyeColor.PINK, DyeColor.LIGHT_BLUE, Pattern.SPOTTY), 6);
		commonTropicalFishIds.put(new CCP(DyeColor.PURPLE, DyeColor.YELLOW, Pattern.BLOCKFISH), 7);
		commonTropicalFishIds.put(new CCP(DyeColor.WHITE, DyeColor.RED, Pattern.CLAYFISH), 8);
		commonTropicalFishIds.put(new CCP(DyeColor.WHITE, DyeColor.YELLOW, Pattern.SPOTTY), 9);
		commonTropicalFishIds.put(new CCP(DyeColor.WHITE, DyeColor.GRAY, Pattern.GLITTER), 10);
		commonTropicalFishIds.put(new CCP(DyeColor.WHITE, DyeColor.ORANGE, Pattern.CLAYFISH), 11);
		commonTropicalFishIds.put(new CCP(DyeColor.CYAN, DyeColor.PINK, Pattern.DASHER), 12);
		commonTropicalFishIds.put(new CCP(DyeColor.LIME, DyeColor.LIGHT_BLUE, Pattern.BRINELY), 13);
		commonTropicalFishIds.put(new CCP(DyeColor.RED, DyeColor.WHITE, Pattern.BETTY), 14);
		commonTropicalFishIds.put(new CCP(DyeColor.GRAY, DyeColor.RED, Pattern.SNOOPER), 15);
		commonTropicalFishIds.put(new CCP(DyeColor.RED, DyeColor.WHITE, Pattern.BLOCKFISH), 16);
		commonTropicalFishIds.put(new CCP(DyeColor.WHITE, DyeColor.YELLOW, Pattern.FLOPPER), 17);
		commonTropicalFishIds.put(new CCP(DyeColor.RED, DyeColor.WHITE, Pattern.KOB), 18);
		commonTropicalFishIds.put(new CCP(DyeColor.GRAY, DyeColor.WHITE, Pattern.SUNSTREAK), 19);
		commonTropicalFishIds.put(new CCP(DyeColor.CYAN, DyeColor.YELLOW, Pattern.DASHER), 20);
		commonTropicalFishIds.put(new CCP(DyeColor.YELLOW, DyeColor.YELLOW, Pattern.FLOPPER), 21);
		commonTropicalFishNames = new HashMap<CCP, String>();
		commonTropicalFishNames.put(new CCP(DyeColor.ORANGE, DyeColor.GRAY, Pattern.STRIPEY), "Anemone");
		commonTropicalFishNames.put(new CCP(DyeColor.GRAY, DyeColor.GRAY, Pattern.FLOPPER), "Black Tang");
		commonTropicalFishNames.put(new CCP(DyeColor.GRAY, DyeColor.BLUE, Pattern.FLOPPER), "Blue Tang");
		commonTropicalFishNames.put(new CCP(DyeColor.WHITE, DyeColor.GRAY, Pattern.BRINELY), "Butterflyfish");
		commonTropicalFishNames.put(new CCP(DyeColor.BLUE, DyeColor.GRAY, Pattern.SUNSTREAK), "Cichlid");
		commonTropicalFishNames.put(new CCP(DyeColor.ORANGE, DyeColor.WHITE, Pattern.KOB), "Clownfish");
		commonTropicalFishNames.put(new CCP(DyeColor.PINK, DyeColor.LIGHT_BLUE, Pattern.SPOTTY), "Cotton Candy Betta");
		commonTropicalFishNames.put(new CCP(DyeColor.PURPLE, DyeColor.YELLOW, Pattern.BLOCKFISH), "Dottyback");
		commonTropicalFishNames.put(new CCP(DyeColor.WHITE, DyeColor.RED, Pattern.CLAYFISH), /*US=*/"Emperor Red Snapper" /*UK="Red Emperor"*/);
		commonTropicalFishNames.put(new CCP(DyeColor.WHITE, DyeColor.YELLOW, Pattern.SPOTTY), "Goatfish");
		commonTropicalFishNames.put(new CCP(DyeColor.WHITE, DyeColor.GRAY, Pattern.GLITTER), "Moorish Idol");
		commonTropicalFishNames.put(new CCP(DyeColor.WHITE, DyeColor.ORANGE, Pattern.CLAYFISH), "Ornate Butterflyfish");
		commonTropicalFishNames.put(new CCP(DyeColor.CYAN, DyeColor.PINK, Pattern.DASHER), "Parrotfish");
		commonTropicalFishNames.put(new CCP(DyeColor.LIME, DyeColor.LIGHT_BLUE, Pattern.BRINELY), "Queen Angelfish");
		commonTropicalFishNames.put(new CCP(DyeColor.RED, DyeColor.WHITE, Pattern.BETTY), "Red Cichlid");
		commonTropicalFishNames.put(new CCP(DyeColor.GRAY, DyeColor.RED, Pattern.SNOOPER), "Red Lipped Blenny");
		commonTropicalFishNames.put(new CCP(DyeColor.RED, DyeColor.WHITE, Pattern.BLOCKFISH), "Red Snapper");
		commonTropicalFishNames.put(new CCP(DyeColor.WHITE, DyeColor.YELLOW, Pattern.FLOPPER), "Threadfin");
		commonTropicalFishNames.put(new CCP(DyeColor.RED, DyeColor.WHITE, Pattern.KOB), "Tomato Clownfish");
		commonTropicalFishNames.put(new CCP(DyeColor.GRAY, DyeColor.WHITE, Pattern.SUNSTREAK), "Triggerfish");
		commonTropicalFishNames.put(new CCP(DyeColor.CYAN, DyeColor.YELLOW, Pattern.DASHER), "Yellowtail Parrotfish");
		commonTropicalFishNames.put(new CCP(DyeColor.YELLOW, DyeColor.YELLOW, Pattern.FLOPPER), "Yellow Tang");
		commonTropicalFishNamesReverse = new HashMap<String, CCP>();
		commonTropicalFishNames.entrySet().stream().forEach(
				e -> commonTropicalFishNamesReverse.put(e.getValue().toUpperCase().replace(' ', '_'), e.getKey()));
		fishColorNames = new HashMap<DyeColor, String>();
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
		cachedTropicalFishNames = new HashMap<CCP, String>();
	}
	@Deprecated public static String getTropicalFishEnglishName(@Nonnull CCP ccp){
		String name = commonTropicalFishNames.get(ccp);
		if(name == null) name = cachedTropicalFishNames.get(ccp);
		if(name == null){
			StringBuilder builder = new StringBuilder(fishColorNames.get(ccp.bodyColor));
			if(ccp.bodyColor != ccp.patternColor) builder.append('-').append(fishColorNames.get(ccp.patternColor));
			builder.append(' ').append(TextUtils.capitalizeAndSpacify(ccp.pattern.name(), '_'));
			name = builder.toString();
			cachedTropicalFishNames.put(ccp, name); // Cache result. Size can reach up to 2700 varieties (15*15*12)
		}
		return name;
	}
	public static CCP getCCP(@Nonnull TropicalFish fish){
		System.out.println("CCP: "+fish.getBodyColor()+","+ fish.getPatternColor()+","+ fish.getPattern());
		return new CCP(fish.getBodyColor(), fish.getPatternColor(), fish.getPattern());
	}
	public static CCP getCCP(@Nonnull String commonTropicalFishName){
		return commonTropicalFishNamesReverse.get(commonTropicalFishName.toUpperCase().replace(' ', '_'));
	}
	public static Integer getCommonTropicalFishId(@Nonnull CCP ccp){return commonTropicalFishIds.get(ccp);}

	public static String getPandaTrait(@Nonnull String mainGene, @Nonnull String hiddenGene){
		if(mainGene.equals(hiddenGene)) return mainGene;
		switch(mainGene){
			case "BROWN":
			case "WEAK":
				return "NORMAL";
			default:
				return mainGene;
		}
	}

	public static EntityType getEntityByName(@Nonnull String name){
		//TODO: improve this function / test for errors
		//TODO: uncomment and access HeadUtils using reflection:
		//if(name.toUpperCase().startsWith("MHF_")) name = HeadUtils.normalizedNameFromMHFName(name);
		name = name.toUpperCase().replace(' ', '_');
		switch(name.replace("_", "")){
			case "MOOSHROOM": return EntityType.MUSHROOM_COW;
			case "SNOWGOLEM": return EntityType.SNOWMAN;
			case "ZOMBIEPIGMAN": return EntityType.valueOf("PIG_ZOMBIE");
			case "LEASHKNOT": return EntityType.LEASH_HITCH;
			case "TNTMINECART": return EntityType.MINECART_TNT;
			case "CHESTMINECART": return EntityType.MINECART_CHEST;
			case "HOPPERMINECART": return EntityType.MINECART_HOPPER;
			case "FURNACEMINECART": return EntityType.MINECART_FURNACE;
			case "COMMANDBLOCKMINECART": return EntityType.MINECART_COMMAND;
			case "SPAWNERMINECART": return EntityType.MINECART_MOB_SPAWNER;
			default: 
				try{return EntityType.valueOf(name);}
				catch(IllegalArgumentException ex){}
				name = name.replace("_", "");
				for(EntityType t : EntityType.values()) if(t.name().replace("_", "").equals(name)) return t;
				return EntityType.UNKNOWN;
		}
	}

	public static String getNormalizedEntityName(@Nonnull String name){
		//TODO: improve this function / test for errors
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

	public static boolean isSkeletal(@Nonnull EntityType eType){
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
}