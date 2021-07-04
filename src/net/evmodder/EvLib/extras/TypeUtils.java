package net.evmodder.EvLib.extras;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class TypeUtils{
	static int version = 13;
	final static HashMap<Material, DyeColor> dyeTypes = new HashMap<>();
	static{
		try{version = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);}
		catch(IllegalArgumentException | ArrayIndexOutOfBoundsException e){System.err.println("EvLib failed to detect server version!");}
		if(version < 13) Bukkit.getLogger().severe("This version of EvLib does not support servers below 1.13!");

		Arrays.stream(Material.values()).filter(m -> m.name().endsWith("_DYE"))
				.forEach(m -> dyeTypes.put(m, DyeColor.valueOf(m.name().substring(0, m.name().length()-4))));
		if(version <= 13){
			dyeTypes.put(Material.INK_SAC, DyeColor.BLACK);
			dyeTypes.put(Material.LAPIS_LAZULI, DyeColor.BLUE);
			dyeTypes.put(Material.COCOA_BEANS, DyeColor.BROWN);
			dyeTypes.put(Material.BONE_MEAL, DyeColor.WHITE);
			dyeTypes.put(Material.valueOf("ROSE_RED"), DyeColor.RED);
			dyeTypes.put(Material.valueOf("DANDELION_YELLOW"), DyeColor.YELLOW);
			dyeTypes.put(Material.valueOf("CACTUS_GREEN"), DyeColor.GREEN);
		}
	}

	public static boolean isDye(Material mat){return dyeTypes.containsKey(mat);}

	public static DyeColor getDyeColor(Material mat){
		switch(mat){
			case BLACK_BED: case BLACK_WOOL: case BLACK_SHULKER_BOX: case BLACK_CONCRETE: case BLACK_CONCRETE_POWDER:
			case BLACK_STAINED_GLASS: case BLACK_STAINED_GLASS_PANE: case BLACK_TERRACOTTA: case BLACK_GLAZED_TERRACOTTA:
			case BLACK_CARPET: case BLACK_BANNER: case BLACK_WALL_BANNER:
				return DyeColor.BLACK;
			case BLUE_BED: case BLUE_WOOL: case BLUE_SHULKER_BOX: case BLUE_CONCRETE: case BLUE_CONCRETE_POWDER:
			case BLUE_STAINED_GLASS: case BLUE_STAINED_GLASS_PANE: case BLUE_TERRACOTTA: case BLUE_GLAZED_TERRACOTTA:
			case BLUE_CARPET: case BLUE_BANNER: case BLUE_WALL_BANNER:
				return DyeColor.BLUE;
			case BROWN_BED: case BROWN_WOOL: case BROWN_SHULKER_BOX: case BROWN_CONCRETE: case BROWN_CONCRETE_POWDER:
			case BROWN_STAINED_GLASS: case BROWN_STAINED_GLASS_PANE: case BROWN_TERRACOTTA: case BROWN_GLAZED_TERRACOTTA:
			case BROWN_CARPET: case BROWN_BANNER: case BROWN_WALL_BANNER:
				return DyeColor.BROWN;
			case CYAN_BED: case CYAN_WOOL: case CYAN_SHULKER_BOX: case CYAN_CONCRETE: case CYAN_CONCRETE_POWDER:
			case CYAN_STAINED_GLASS: case CYAN_STAINED_GLASS_PANE: case CYAN_TERRACOTTA: case CYAN_GLAZED_TERRACOTTA:
			case CYAN_CARPET: case CYAN_BANNER: case CYAN_WALL_BANNER:
				return DyeColor.CYAN;
			case GRAY_BED: case GRAY_WOOL: case GRAY_SHULKER_BOX: case GRAY_CONCRETE: case GRAY_CONCRETE_POWDER:
			case GRAY_STAINED_GLASS: case GRAY_STAINED_GLASS_PANE: case GRAY_TERRACOTTA: case GRAY_GLAZED_TERRACOTTA:
			case GRAY_CARPET: case GRAY_BANNER: case GRAY_WALL_BANNER:
				return DyeColor.GRAY;
			case GREEN_BED: case GREEN_WOOL: case GREEN_SHULKER_BOX: case GREEN_CONCRETE: case GREEN_CONCRETE_POWDER:
			case GREEN_STAINED_GLASS: case GREEN_STAINED_GLASS_PANE: case GREEN_TERRACOTTA: case GREEN_GLAZED_TERRACOTTA:
			case GREEN_CARPET: case GREEN_BANNER: case GREEN_WALL_BANNER:
				return DyeColor.GREEN;
			case LIGHT_BLUE_BED: case LIGHT_BLUE_WOOL: case LIGHT_BLUE_SHULKER_BOX: case LIGHT_BLUE_CONCRETE: case LIGHT_BLUE_CONCRETE_POWDER:
			case LIGHT_BLUE_STAINED_GLASS: case LIGHT_BLUE_STAINED_GLASS_PANE: case LIGHT_BLUE_TERRACOTTA: case LIGHT_BLUE_GLAZED_TERRACOTTA:
			case LIGHT_BLUE_CARPET: case LIGHT_BLUE_BANNER: case LIGHT_BLUE_WALL_BANNER:
				return DyeColor.LIGHT_BLUE;
			case LIGHT_GRAY_BED: case LIGHT_GRAY_WOOL: case LIGHT_GRAY_SHULKER_BOX: case LIGHT_GRAY_CONCRETE: case LIGHT_GRAY_CONCRETE_POWDER:
			case LIGHT_GRAY_STAINED_GLASS: case LIGHT_GRAY_STAINED_GLASS_PANE: case LIGHT_GRAY_TERRACOTTA: case LIGHT_GRAY_GLAZED_TERRACOTTA:
			case LIGHT_GRAY_CARPET: case LIGHT_GRAY_BANNER: case LIGHT_GRAY_WALL_BANNER:
				return DyeColor.LIGHT_GRAY;
			case LIME_BED: case LIME_WOOL: case LIME_SHULKER_BOX: case LIME_CONCRETE: case LIME_CONCRETE_POWDER:
			case LIME_STAINED_GLASS: case LIME_STAINED_GLASS_PANE: case LIME_TERRACOTTA: case LIME_GLAZED_TERRACOTTA:
			case LIME_CARPET: case LIME_BANNER: case LIME_WALL_BANNER:
				return DyeColor.LIME;
			case MAGENTA_BED: case MAGENTA_WOOL: case MAGENTA_SHULKER_BOX: case MAGENTA_CONCRETE: case MAGENTA_CONCRETE_POWDER:
			case MAGENTA_STAINED_GLASS: case MAGENTA_STAINED_GLASS_PANE: case MAGENTA_TERRACOTTA: case MAGENTA_GLAZED_TERRACOTTA:
			case MAGENTA_CARPET: case MAGENTA_BANNER: case MAGENTA_WALL_BANNER:
				return DyeColor.MAGENTA;
			case ORANGE_BED: case ORANGE_WOOL: case ORANGE_SHULKER_BOX: case ORANGE_CONCRETE: case ORANGE_CONCRETE_POWDER:
			case ORANGE_STAINED_GLASS: case ORANGE_STAINED_GLASS_PANE: case ORANGE_TERRACOTTA: case ORANGE_GLAZED_TERRACOTTA:
			case ORANGE_CARPET: case ORANGE_BANNER: case ORANGE_WALL_BANNER:
				return DyeColor.ORANGE;
			case PINK_BED: case PINK_WOOL: case PINK_SHULKER_BOX: case PINK_CONCRETE: case PINK_CONCRETE_POWDER:
			case PINK_STAINED_GLASS: case PINK_STAINED_GLASS_PANE: case PINK_TERRACOTTA: case PINK_GLAZED_TERRACOTTA:
			case PINK_CARPET: case PINK_BANNER: case PINK_WALL_BANNER:
				return DyeColor.PINK;
			case PURPLE_BED: case PURPLE_WOOL: case PURPLE_SHULKER_BOX: case PURPLE_CONCRETE: case PURPLE_CONCRETE_POWDER:
			case PURPLE_STAINED_GLASS: case PURPLE_STAINED_GLASS_PANE: case PURPLE_TERRACOTTA: case PURPLE_GLAZED_TERRACOTTA:
			case PURPLE_CARPET: case PURPLE_BANNER: case PURPLE_WALL_BANNER:
				return DyeColor.PURPLE;
			case RED_BED: case RED_WOOL: case RED_SHULKER_BOX: case RED_CONCRETE: case RED_CONCRETE_POWDER:
			case RED_STAINED_GLASS: case RED_STAINED_GLASS_PANE: case RED_TERRACOTTA: case RED_GLAZED_TERRACOTTA:
			case RED_CARPET: case RED_BANNER: case RED_WALL_BANNER:
				return DyeColor.RED;
			case WHITE_BED: case WHITE_WOOL: case WHITE_SHULKER_BOX: case WHITE_CONCRETE: case WHITE_CONCRETE_POWDER:
			case WHITE_STAINED_GLASS: case WHITE_STAINED_GLASS_PANE: case WHITE_TERRACOTTA: case WHITE_GLAZED_TERRACOTTA:
			case WHITE_CARPET: case WHITE_BANNER: case WHITE_WALL_BANNER:
				return DyeColor.WHITE;
			case YELLOW_BED: case YELLOW_WOOL: case YELLOW_SHULKER_BOX: case YELLOW_CONCRETE: case YELLOW_CONCRETE_POWDER:
			case YELLOW_STAINED_GLASS: case YELLOW_STAINED_GLASS_PANE: case YELLOW_TERRACOTTA: case YELLOW_GLAZED_TERRACOTTA:
			case YELLOW_CARPET: case YELLOW_BANNER: case YELLOW_WALL_BANNER:
				return DyeColor.YELLOW;
			default:
				return dyeTypes.getOrDefault(mat, null);
		}
	}

	public static boolean isOre(Material mat){
		switch(mat){
			case NETHER_QUARTZ_ORE:
			case COAL_ORE:
			case IRON_ORE:
			case GOLD_ORE:
			case REDSTONE_ORE:
			case LAPIS_ORE:
			case EMERALD_ORE:
			case DIAMOND_ORE:
				return true;
			default:
				return version >= 16 && mat.name().equals("NETHER_GOLD_ORE");
		}
	}

	public static boolean isInfested(Material mat){
		switch(mat){
			case INFESTED_CHISELED_STONE_BRICKS:
			case INFESTED_COBBLESTONE:
			case INFESTED_CRACKED_STONE_BRICKS:
			case INFESTED_MOSSY_STONE_BRICKS:
			case INFESTED_STONE:
			case INFESTED_STONE_BRICKS:
				return true;
			default:
				return false;
		}
	}

	public static ChatColor getRarityColor(ItemStack item){
		//String itemNameFormat = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? ""+ChatColor.ITALIC : "";
		if(version >= 14){
			switch(item.getType().name()){
				// EPIC:
				case "MOJANG_BANNER_PATTERN":
				case "JIGSAW":
					return ChatColor.LIGHT_PURPLE;
				// UNCOMMON:
				case "CREEPER_BANNER_PATTERN":
				case "SKULL_BANNER_PATTERN":
					return item.hasItemMeta() && item.getItemMeta().hasEnchants() ? ChatColor.AQUA : ChatColor.YELLOW;
			}
		}
		switch(item.getType()){
			// EPIC:
			case DRAGON_EGG:
			case ENCHANTED_GOLDEN_APPLE:
			case COMMAND_BLOCK: case CHAIN_COMMAND_BLOCK: case REPEATING_COMMAND_BLOCK:
			case STRUCTURE_BLOCK:
				return ChatColor.LIGHT_PURPLE;
			// RARE:
			case BEACON:
			case CONDUIT:
			case END_CRYSTAL:
			case GOLDEN_APPLE:
			case MUSIC_DISC_11: case MUSIC_DISC_13: case MUSIC_DISC_BLOCKS: case MUSIC_DISC_CAT: case MUSIC_DISC_CHIRP: case MUSIC_DISC_FAR:
			case MUSIC_DISC_MALL: case MUSIC_DISC_MELLOHI: case MUSIC_DISC_STAL: case MUSIC_DISC_WAIT: case MUSIC_DISC_WARD:
				return ChatColor.AQUA;
			// UNCOMMON:
			case EXPERIENCE_BOTTLE:
			case DRAGON_BREATH:
			case ELYTRA:
			case ENCHANTED_BOOK:
			case PLAYER_HEAD: case CREEPER_HEAD: case ZOMBIE_HEAD: case DRAGON_HEAD:
			case SKELETON_SKULL: case WITHER_SKELETON_SKULL:
			case HEART_OF_THE_SEA:
			case NETHER_STAR:
			case TOTEM_OF_UNDYING:
				return item.hasItemMeta() && item.getItemMeta().hasEnchants() ? ChatColor.AQUA : ChatColor.YELLOW;
			// COMMON:
			default:
				return item.hasItemMeta() && item.getItemMeta().hasEnchants() ? ChatColor.AQUA : ChatColor.WHITE;
		}
	}

	public static boolean isBed(Material mat){
		switch(mat){
			case BLACK_BED:
			case BLUE_BED:
			case BROWN_BED:
			case CYAN_BED:
			case GRAY_BED:
			case GREEN_BED:
			case LIGHT_BLUE_BED:
			case LIME_BED:
			case MAGENTA_BED:
			case ORANGE_BED:
			case PINK_BED:
			case PURPLE_BED:
			case RED_BED:
			case LIGHT_GRAY_BED:
			case WHITE_BED:
			case YELLOW_BED:
				return true;
			default:
				return false;
		}
	}
	public static boolean isWool(Material mat){
		switch(mat){
			case BLACK_WOOL:
			case BLUE_WOOL:
			case BROWN_WOOL:
			case CYAN_WOOL:
			case GRAY_WOOL:
			case GREEN_WOOL:
			case LIGHT_BLUE_WOOL:
			case LIME_WOOL:
			case MAGENTA_WOOL:
			case ORANGE_WOOL:
			case PINK_WOOL:
			case PURPLE_WOOL:
			case RED_WOOL:
			case LIGHT_GRAY_WOOL:
			case WHITE_WOOL:
			case YELLOW_WOOL:
				return true;
			default:
				return false;
		}
	}
	public static boolean isShulkerBox(Material mat){
		switch(mat){
			case SHULKER_BOX:
			case BLACK_SHULKER_BOX:
			case BLUE_SHULKER_BOX:
			case BROWN_SHULKER_BOX:
			case CYAN_SHULKER_BOX:
			case GRAY_SHULKER_BOX:
			case GREEN_SHULKER_BOX:
			case LIGHT_BLUE_SHULKER_BOX:
			case LIME_SHULKER_BOX:
			case MAGENTA_SHULKER_BOX:
			case ORANGE_SHULKER_BOX:
			case PINK_SHULKER_BOX:
			case PURPLE_SHULKER_BOX:
			case RED_SHULKER_BOX:
			case LIGHT_GRAY_SHULKER_BOX:
			case WHITE_SHULKER_BOX:
			case YELLOW_SHULKER_BOX:
				return true;
			default:
				return false;
		}
	}
	public static boolean isConcrete(Material mat){
		switch(mat){
			case BLACK_CONCRETE:
			case BLUE_CONCRETE:
			case BROWN_CONCRETE:
			case CYAN_CONCRETE:
			case GRAY_CONCRETE:
			case GREEN_CONCRETE:
			case LIGHT_BLUE_CONCRETE:
			case LIGHT_GRAY_CONCRETE:
			case LIME_CONCRETE:
			case MAGENTA_CONCRETE:
			case ORANGE_CONCRETE:
			case PINK_CONCRETE:
			case PURPLE_CONCRETE:
			case RED_CONCRETE:
			case WHITE_CONCRETE:
			case YELLOW_CONCRETE:
				return true;
			default:
				return false;
		}
	}
	public static boolean isConcretePowder(Material mat){
		switch(mat){
			case BLACK_CONCRETE_POWDER:
			case BLUE_CONCRETE_POWDER:
			case BROWN_CONCRETE_POWDER:
			case CYAN_CONCRETE_POWDER:
			case GRAY_CONCRETE_POWDER:
			case GREEN_CONCRETE_POWDER:
			case LIGHT_BLUE_CONCRETE_POWDER:
			case LIGHT_GRAY_CONCRETE_POWDER:
			case LIME_CONCRETE_POWDER:
			case MAGENTA_CONCRETE_POWDER:
			case ORANGE_CONCRETE_POWDER:
			case PINK_CONCRETE_POWDER:
			case PURPLE_CONCRETE_POWDER:
			case RED_CONCRETE_POWDER:
			case WHITE_CONCRETE_POWDER:
			case YELLOW_CONCRETE_POWDER:
				return true;
			default:
				return false;
		}
	}
	public static boolean isStainedGlass(Material mat){
		switch(mat){
			case BLACK_STAINED_GLASS:
			case BLUE_STAINED_GLASS:
			case BROWN_STAINED_GLASS:
			case CYAN_STAINED_GLASS:
			case GRAY_STAINED_GLASS:
			case GREEN_STAINED_GLASS:
			case LIGHT_BLUE_STAINED_GLASS:
			case LIGHT_GRAY_STAINED_GLASS:
			case LIME_STAINED_GLASS:
			case MAGENTA_STAINED_GLASS:
			case ORANGE_STAINED_GLASS:
			case PINK_STAINED_GLASS:
			case PURPLE_STAINED_GLASS:
			case RED_STAINED_GLASS:
			case WHITE_STAINED_GLASS:
			case YELLOW_STAINED_GLASS:
				return true;
			default:
				return false;
		}
	}
	public static boolean isStainedGlassPane(Material mat){
		switch(mat){
			case BLACK_STAINED_GLASS_PANE:
			case BLUE_STAINED_GLASS_PANE:
			case BROWN_STAINED_GLASS_PANE:
			case CYAN_STAINED_GLASS_PANE:
			case GRAY_STAINED_GLASS_PANE:
			case GREEN_STAINED_GLASS_PANE:
			case LIGHT_BLUE_STAINED_GLASS_PANE:
			case LIGHT_GRAY_STAINED_GLASS_PANE:
			case LIME_STAINED_GLASS_PANE:
			case MAGENTA_STAINED_GLASS_PANE:
			case ORANGE_STAINED_GLASS_PANE:
			case PINK_STAINED_GLASS_PANE:
			case PURPLE_STAINED_GLASS_PANE:
			case RED_STAINED_GLASS_PANE:
			case WHITE_STAINED_GLASS_PANE:
			case YELLOW_STAINED_GLASS_PANE:
				return true;
			default:
				return false;
		}
	}
	public static boolean isTerracotta(Material mat){
		switch(mat){
			case TERRACOTTA:
			case BLACK_TERRACOTTA:
			case BLUE_TERRACOTTA:
			case BROWN_TERRACOTTA:
			case CYAN_TERRACOTTA:
			case GRAY_TERRACOTTA:
			case GREEN_TERRACOTTA:
			case LIGHT_BLUE_TERRACOTTA:
			case LIGHT_GRAY_TERRACOTTA:
			case LIME_TERRACOTTA:
			case MAGENTA_TERRACOTTA:
			case ORANGE_TERRACOTTA:
			case PINK_TERRACOTTA:
			case PURPLE_TERRACOTTA:
			case RED_TERRACOTTA:
			case WHITE_TERRACOTTA:
			case YELLOW_TERRACOTTA:
				return true;
			default:
				return false;
		}
	}
	public static boolean isGlazedTerracotta(Material mat){
		switch(mat){
			case BLACK_GLAZED_TERRACOTTA:
			case BLUE_GLAZED_TERRACOTTA:
			case BROWN_GLAZED_TERRACOTTA:
			case CYAN_GLAZED_TERRACOTTA:
			case GRAY_GLAZED_TERRACOTTA:
			case GREEN_GLAZED_TERRACOTTA:
			case LIGHT_BLUE_GLAZED_TERRACOTTA:
			case LIGHT_GRAY_GLAZED_TERRACOTTA:
			case LIME_GLAZED_TERRACOTTA:
			case MAGENTA_GLAZED_TERRACOTTA:
			case ORANGE_GLAZED_TERRACOTTA:
			case PINK_GLAZED_TERRACOTTA:
			case PURPLE_GLAZED_TERRACOTTA:
			case RED_GLAZED_TERRACOTTA:
			case WHITE_GLAZED_TERRACOTTA:
			case YELLOW_GLAZED_TERRACOTTA:
				return true;
			default:
				return false;
		}
	}
	public static boolean isCarpet(Material mat){
		switch(mat){
			case BLACK_CARPET:
			case BLUE_CARPET:
			case BROWN_CARPET:
			case CYAN_CARPET:
			case GRAY_CARPET:
			case GREEN_CARPET:
			case LIGHT_BLUE_CARPET:
			case LIGHT_GRAY_CARPET:
			case LIME_CARPET:
			case MAGENTA_CARPET:
			case ORANGE_CARPET:
			case PINK_CARPET:
			case PURPLE_CARPET:
			case RED_CARPET:
			case WHITE_CARPET:
			case YELLOW_CARPET:
				return true;
			default:
				return false;
		}
	}
	public static boolean isBanner(Material mat){
		switch(mat){
			case BLACK_BANNER:
			case BLUE_BANNER:
			case BROWN_BANNER:
			case CYAN_BANNER:
			case GRAY_BANNER:
			case GREEN_BANNER:
			case LIGHT_BLUE_BANNER:
			case LIGHT_GRAY_BANNER:
			case LIME_BANNER:
			case MAGENTA_BANNER:
			case ORANGE_BANNER:
			case PINK_BANNER:
			case PURPLE_BANNER:
			case RED_BANNER:
			case WHITE_BANNER:
			case YELLOW_BANNER:
				return true;
			default:
				return false;
		}
	}
	public static boolean isWallBanner(Material mat){
		switch(mat){
			case BLACK_WALL_BANNER:
			case BLUE_WALL_BANNER:
			case BROWN_WALL_BANNER:
			case CYAN_WALL_BANNER:
			case GRAY_WALL_BANNER:
			case GREEN_WALL_BANNER:
			case LIGHT_BLUE_WALL_BANNER:
			case LIGHT_GRAY_WALL_BANNER:
			case LIME_WALL_BANNER:
			case MAGENTA_WALL_BANNER:
			case ORANGE_WALL_BANNER:
			case PINK_WALL_BANNER:
			case PURPLE_WALL_BANNER:
			case RED_WALL_BANNER:
			case WHITE_WALL_BANNER:
			case YELLOW_WALL_BANNER:
				return true;
			default:
				return false;
		}
	}
	public static boolean isSign(Material mat){return mat.name().endsWith("_SIGN") && !mat.name().endsWith("_WALL_SIGN");}
	public static boolean isWallSign(Material mat){return mat.name().endsWith("_WALL_SIGN");}

	public static boolean isFlowerPot(Material mat){return mat == Material.FLOWER_POT || mat.name().startsWith("POTTED_");}

	public static boolean isDoublePlant(Material mat){
		switch(mat){
			case SUNFLOWER:
			case LILAC:
			case ROSE_BUSH:
			case PEONY:
			case TALL_GRASS:
			case LARGE_FERN:
				return true;
			default:
				return false;
		}
	}

	public static boolean isRail(Material mat){
		switch(mat){
			case RAIL:
			case ACTIVATOR_RAIL:
			case DETECTOR_RAIL:
			case POWERED_RAIL:
				return true;
			default:
				return false;
		}
	}

	public static boolean isSapling(Material mat){
		switch(mat){
			case ACACIA_SAPLING:
			case BIRCH_SAPLING:
			case DARK_OAK_SAPLING: 
			case JUNGLE_SAPLING:
			case OAK_SAPLING:
			case SPRUCE_SAPLING:
				return true;
			default:
				return false;
		}
	}

	public static boolean isButton(Material mat){
		switch(mat){
			case ACACIA_BUTTON:
			case BIRCH_BUTTON:
			case DARK_OAK_BUTTON:
			case JUNGLE_BUTTON:
			case OAK_BUTTON:
			case SPRUCE_BUTTON:
			case STONE_BUTTON:
				return true;
			default:
				return version >= 16 && (mat.name().equals("WARPED_BUTTON") || mat.name().equals("CRIMSON_BUTTON"));
		}
	}

	public static boolean isPressurePlate(Material mat){
		switch(mat){
			case ACACIA_PRESSURE_PLATE:
			case BIRCH_PRESSURE_PLATE:
			case DARK_OAK_PRESSURE_PLATE:
			case JUNGLE_PRESSURE_PLATE:
			case OAK_PRESSURE_PLATE:
			case SPRUCE_PRESSURE_PLATE:
			case STONE_PRESSURE_PLATE:
			case HEAVY_WEIGHTED_PRESSURE_PLATE:
			case LIGHT_WEIGHTED_PRESSURE_PLATE:
				return true;
			default:
				return version >= 16 && (mat.name().equals("WARPED_PRESSURE_PLATE") || mat.name().equals("CRIMSON_PRESSURE_PLATE"));
		}
	}

	public static boolean isDoor(Material mat){
		switch(mat){
			case ACACIA_DOOR:
			case BIRCH_DOOR:
			case DARK_OAK_DOOR:
			case JUNGLE_DOOR:
			case OAK_DOOR:
			case SPRUCE_DOOR:
				return true;
			default:
				return version >= 16 && (mat.name().equals("WARPED_DOOR") || mat.name().equals("CRIMSON_DOOR"));
		}
	}

	public static boolean isPlanks(Material mat){
		switch(mat){
			case ACACIA_PLANKS:
			case BIRCH_PLANKS:
			case DARK_OAK_PLANKS:
			case JUNGLE_PLANKS:
			case OAK_PLANKS:
			case SPRUCE_PLANKS:
				return true;
			default:
				return version >= 16 && (mat.name().equals("WARPED_PLANKS") || mat.name().equals("CRIMSON_PLANKS"));
		}
	}

	public static boolean isSword(Material mat){
		switch(mat){
			case DIAMOND_SWORD:
			case IRON_SWORD:
			case STONE_SWORD:
			case GOLDEN_SWORD:
			case WOODEN_SWORD:
				return true;
			default:
				return version >= 16 && mat.name().equals("NETHERITE_SWORD");
		}
	}
	public static boolean isAxe(Material mat){
		switch(mat){
			case DIAMOND_AXE:
			case IRON_AXE:
			case STONE_AXE:
			case GOLDEN_AXE:
			case WOODEN_AXE:
				return true;
			default:
				return version >= 16 && mat.name().equals("NETHERITE_AXE");
		}
	}
	public static boolean isPickaxe(Material mat){
		switch(mat){
			case DIAMOND_PICKAXE:
			case IRON_PICKAXE:
			case STONE_PICKAXE:
			case GOLDEN_PICKAXE:
			case WOODEN_PICKAXE:
				return true;
			default:
				return version >= 16 && mat.name().equals("NETHERITE_PICKAXE");
		}
	}
	public static boolean isShovel(Material mat){
		switch(mat){
			case DIAMOND_SHOVEL:
			case IRON_SHOVEL:
			case STONE_SHOVEL:
			case GOLDEN_SHOVEL:
			case WOODEN_SHOVEL:
				return true;
			default:
				return version >= 16 && mat.name().equals("NETHERITE_SHOVEL");
		}
	}
	public static boolean isHoe(Material mat){
		switch(mat){
			case DIAMOND_HOE:
			case IRON_HOE:
			case STONE_HOE:
			case GOLDEN_HOE:
			case WOODEN_HOE:
				return true;
			default:
				return version >= 16 && mat.name().equals("NETHERITE_HOE");
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
				if(pickType.name().equals("NETHERITE_PICKAXE")) return 5;
				if(pickType.name().endsWith("_PICKAXE"))
					throw new IllegalArgumentException("Unknown pickaxe type: "+pickType+", please update EvLib");
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
				if(swordType.name().equals("NETHERITE_SWORD")) return 5;
				if(swordType.name().endsWith("_SWORD"))
					throw new IllegalArgumentException("Unknown sword type: "+swordType+", please update EvLib");
				return 0;
		}
	}
	public static boolean swordIsAtLeast(Material swordType, Material needSword){//+
		return swordNumber(swordType) >= swordNumber(needSword);
	}

	enum ObtainableOptions{
		SILK_SPAWNERS, SILK_INFESTED, MOB_EGGS, CMD_BLOCKS,
		BEDROCK, END_PORTAL_FRAMES, BARRIERS, STRUCTURE_BLOCKS, PETRIFIED_SLABS,
		ITEM_LORE, ITEM_NAME_COLOR, CONFLICTING_ENCHANTS, ABOVE_MAX_ENCHANTS, OVERSTACKED,
		PLAYER_HEADS, TATTERED_BOOKS
	};
	public static boolean isObtainable(Material mat, ObtainableOptions... opts){
		HashSet<ObtainableOptions> canObtain = new HashSet<>();
		for(ObtainableOptions opt : opts) canObtain.add(opt);
		// Can't determine:
		// ITEM_LORE, ITEM_NAME_COLOR, CONFLICTING_ENCHANTS, ABOVE_MAX_ENCHANTS, OVERSTACKED, TATTERED_BOOKS
		// Or: non-holdable double-slabs
		switch(mat){
//			case AIR: // Debatable
//			case CAVE_AIR:
//			case VOID_AIR:
			case END_GATEWAY:
			case END_PORTAL: // Cannot be held (case END_PORTAL_FRAME: is below)
			case FARMLAND: // Cannot be held
			case FIRE: // Cannot be held
			case FROSTED_ICE: // Cannot be held
			case KNOWLEDGE_BOOK:
//			case LARGE_FERN: // Found in 18.6% of taiga village chests.
			case REDSTONE_WALL_TORCH: // Cannot be held
			case REDSTONE_WIRE: // Cannot be held
			case TALL_SEAGRASS: // Not obtainable with shears
			case TRIPWIRE: // Cannot be held
			case WALL_TORCH: // Cannot be held
			case PISTON_HEAD: // Piston heads: cannot be held
			case MOVING_PISTON: 
			case CREEPER_WALL_HEAD: // Wall heads: Cannot be held
			case DRAGON_WALL_HEAD:
			case PLAYER_WALL_HEAD:
			case ZOMBIE_WALL_HEAD:
			case BRAIN_CORAL_WALL_FAN: // Wall fans: Cannot be held
			case BUBBLE_CORAL_WALL_FAN:
			case FIRE_CORAL_WALL_FAN:
			case HORN_CORAL_WALL_FAN:
			case TUBE_CORAL_WALL_FAN:
			case DEAD_BRAIN_CORAL_WALL_FAN:
			case DEAD_BUBBLE_CORAL_WALL_FAN:
			case DEAD_FIRE_CORAL_WALL_FAN:
			case DEAD_HORN_CORAL_WALL_FAN:
			case DEAD_TUBE_CORAL_WALL_FAN:
			case ATTACHED_MELON_STEM: // Crops: Cannot be held
			case ATTACHED_PUMPKIN_STEM:
			case MELON_STEM:
			case PUMPKIN_STEM:
			case BEETROOTS:
			case CARROTS:
			case CHORUS_PLANT: // Not obtainable with silk
				return false;
			case BARRIER:
				return canObtain.contains(ObtainableOptions.BARRIERS);
			case BEDROCK:
				return canObtain.contains(ObtainableOptions.BEDROCK);
			case END_PORTAL_FRAME:
				return canObtain.contains(ObtainableOptions.END_PORTAL_FRAMES);
			case PETRIFIED_OAK_SLAB:
				return canObtain.contains(ObtainableOptions.PETRIFIED_SLABS);
			case PLAYER_HEAD:
				return canObtain.contains(ObtainableOptions.PLAYER_HEADS);
			case SPAWNER:
				return canObtain.contains(ObtainableOptions.SILK_SPAWNERS);
			case COMMAND_BLOCK:
			case COMMAND_BLOCK_MINECART:
			case CHAIN_COMMAND_BLOCK:
			case REPEATING_COMMAND_BLOCK:
				return canObtain.contains(ObtainableOptions.CMD_BLOCKS);
			case STRUCTURE_BLOCK:
			case STRUCTURE_VOID:
				return canObtain.contains(ObtainableOptions.STRUCTURE_BLOCKS);
			default:
				if(isFlowerPot(mat) && mat != Material.FLOWER_POT) return false; // Cannot be held
				if(isWallBanner(mat) || isWallSign(mat)/* || wCoralFan || wHead */) return false; // Cannot be held
				if(isInfested(mat)) return canObtain.contains(ObtainableOptions.SILK_INFESTED);
				if(mat.name().endsWith("_SPAWN_EGG")) return canObtain.contains(ObtainableOptions.MOB_EGGS);
				if(version >= 14){
					if(mat.name().equals("BAMBOO_SAPLING")) return false; // Planted-form: Cannot be held
					if(mat.name().equals("JIGSAW")) return canObtain.contains(ObtainableOptions.STRUCTURE_BLOCKS);
				}
				if(mat.name().equals(version >= 17 ? "DIRT_PATH" : "GRASS_PATH")) return false; // Not obtainable with silk
				return true;
		}
	}
}