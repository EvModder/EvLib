package net.evmodder.EvLib.types;

import java.util.HashSet;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

class TypeUtils_1_14 extends TypeUtils_1_13{

	@Override public ChatColor getRarityColor(ItemStack item, boolean checkCustomName){
		if(checkCustomName && item.hasItemMeta() && item.getItemMeta().hasDisplayName()){
			String displayName = item.getItemMeta().getDisplayName();
			ChatColor color = null;
			for(int i=0; i+1 < displayName.length() && displayName.charAt(i) == ChatColor.COLOR_CHAR; i+=2)
				color = ChatColor.getByChar(displayName.charAt(i+1));//TODO: currently matches formats as well
			if(color != null) return color;
		}
		switch(item.getType()){
			// EPIC:
			case DRAGON_EGG:
			case ENCHANTED_GOLDEN_APPLE:
			case MOJANG_BANNER_PATTERN:
			case COMMAND_BLOCK: case CHAIN_COMMAND_BLOCK: case REPEATING_COMMAND_BLOCK:
			case JIGSAW: case STRUCTURE_BLOCK:
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
			case CREEPER_BANNER_PATTERN:
			case SKULL_BANNER_PATTERN:
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

	@Override public boolean isDye(Material mat){
		switch(mat){
			case BLACK_DYE:
			case BLUE_DYE:
			case BROWN_DYE:
			case CYAN_DYE:
			case GRAY_DYE:
			case GREEN_DYE:
			case LIGHT_BLUE_DYE:
			case LIME_DYE:
			case MAGENTA_DYE:
			case ORANGE_DYE:
			case PINK_DYE:
			case PURPLE_DYE:
			case RED_DYE:
			case LIGHT_GRAY_DYE:
			case WHITE_DYE:
			case YELLOW_DYE:
				return true;
			default:
				return false;
		}
	}

	@Override public DyeColor getDyeColor(Material mat){
		switch(mat){
			case BLACK_DYE: case BLACK_BED:
			case BLACK_WOOL: case BLACK_SHULKER_BOX: case BLACK_CONCRETE: case BLACK_CONCRETE_POWDER:
			case BLACK_STAINED_GLASS: case BLACK_STAINED_GLASS_PANE: case BLACK_TERRACOTTA: case BLACK_GLAZED_TERRACOTTA:
			case BLACK_CARPET: case BLACK_BANNER: case BLACK_WALL_BANNER:
				return DyeColor.BLACK;
			case BLUE_DYE: case BLUE_BED:
			case BLUE_WOOL: case BLUE_SHULKER_BOX: case BLUE_CONCRETE: case BLUE_CONCRETE_POWDER:
			case BLUE_STAINED_GLASS: case BLUE_STAINED_GLASS_PANE: case BLUE_TERRACOTTA: case BLUE_GLAZED_TERRACOTTA:
			case BLUE_CARPET: case BLUE_BANNER: case BLUE_WALL_BANNER:
				return DyeColor.BLUE;
			case BROWN_DYE: case BROWN_BED:
			case BROWN_WOOL: case BROWN_SHULKER_BOX: case BROWN_CONCRETE: case BROWN_CONCRETE_POWDER:
			case BROWN_STAINED_GLASS: case BROWN_STAINED_GLASS_PANE: case BROWN_TERRACOTTA: case BROWN_GLAZED_TERRACOTTA:
			case BROWN_CARPET: case BROWN_BANNER: case BROWN_WALL_BANNER:
				return DyeColor.BROWN;
			case CYAN_DYE: case CYAN_BED:
			case CYAN_WOOL: case CYAN_SHULKER_BOX: case CYAN_CONCRETE: case CYAN_CONCRETE_POWDER:
			case CYAN_STAINED_GLASS: case CYAN_STAINED_GLASS_PANE: case CYAN_TERRACOTTA: case CYAN_GLAZED_TERRACOTTA:
			case CYAN_CARPET: case CYAN_BANNER: case CYAN_WALL_BANNER:
				return DyeColor.CYAN;
			case GRAY_DYE: case GRAY_BED:
			case GRAY_WOOL: case GRAY_SHULKER_BOX: case GRAY_CONCRETE: case GRAY_CONCRETE_POWDER:
			case GRAY_STAINED_GLASS: case GRAY_STAINED_GLASS_PANE: case GRAY_TERRACOTTA: case GRAY_GLAZED_TERRACOTTA:
			case GRAY_CARPET: case GRAY_BANNER: case GRAY_WALL_BANNER:
				return DyeColor.GRAY;
			case GREEN_DYE: case GREEN_BED:
			case GREEN_WOOL: case GREEN_SHULKER_BOX: case GREEN_CONCRETE: case GREEN_CONCRETE_POWDER:
			case GREEN_STAINED_GLASS: case GREEN_STAINED_GLASS_PANE: case GREEN_TERRACOTTA: case GREEN_GLAZED_TERRACOTTA:
			case GREEN_CARPET: case GREEN_BANNER: case GREEN_WALL_BANNER:
				return DyeColor.GREEN;
			case LIGHT_BLUE_DYE: case LIGHT_BLUE_BED:
			case LIGHT_BLUE_WOOL: case LIGHT_BLUE_SHULKER_BOX: case LIGHT_BLUE_CONCRETE: case LIGHT_BLUE_CONCRETE_POWDER:
			case LIGHT_BLUE_STAINED_GLASS: case LIGHT_BLUE_STAINED_GLASS_PANE: case LIGHT_BLUE_TERRACOTTA: case LIGHT_BLUE_GLAZED_TERRACOTTA:
			case LIGHT_BLUE_CARPET: case LIGHT_BLUE_BANNER: case LIGHT_BLUE_WALL_BANNER:
				return DyeColor.LIGHT_BLUE;
			case LIGHT_GRAY_DYE: case LIGHT_GRAY_BED:
			case LIGHT_GRAY_WOOL: case LIGHT_GRAY_SHULKER_BOX: case LIGHT_GRAY_CONCRETE: case LIGHT_GRAY_CONCRETE_POWDER:
			case LIGHT_GRAY_STAINED_GLASS: case LIGHT_GRAY_STAINED_GLASS_PANE: case LIGHT_GRAY_TERRACOTTA: case LIGHT_GRAY_GLAZED_TERRACOTTA:
			case LIGHT_GRAY_CARPET: case LIGHT_GRAY_BANNER: case LIGHT_GRAY_WALL_BANNER:
				return DyeColor.LIGHT_GRAY;
			case LIME_DYE: case LIME_BED:
			case LIME_WOOL: case LIME_SHULKER_BOX: case LIME_CONCRETE: case LIME_CONCRETE_POWDER:
			case LIME_STAINED_GLASS: case LIME_STAINED_GLASS_PANE: case LIME_TERRACOTTA: case LIME_GLAZED_TERRACOTTA:
			case LIME_CARPET: case LIME_BANNER: case LIME_WALL_BANNER:
				return DyeColor.LIME;
			case MAGENTA_DYE: case MAGENTA_BED:
			case MAGENTA_WOOL: case MAGENTA_SHULKER_BOX: case MAGENTA_CONCRETE: case MAGENTA_CONCRETE_POWDER:
			case MAGENTA_STAINED_GLASS: case MAGENTA_STAINED_GLASS_PANE: case MAGENTA_TERRACOTTA: case MAGENTA_GLAZED_TERRACOTTA:
			case MAGENTA_CARPET: case MAGENTA_BANNER: case MAGENTA_WALL_BANNER:
				return DyeColor.MAGENTA;
			case ORANGE_DYE: case ORANGE_BED:
			case ORANGE_WOOL: case ORANGE_SHULKER_BOX: case ORANGE_CONCRETE: case ORANGE_CONCRETE_POWDER:
			case ORANGE_STAINED_GLASS: case ORANGE_STAINED_GLASS_PANE: case ORANGE_TERRACOTTA: case ORANGE_GLAZED_TERRACOTTA:
			case ORANGE_CARPET: case ORANGE_BANNER: case ORANGE_WALL_BANNER:
				return DyeColor.ORANGE;
			case PINK_DYE: case PINK_BED:
			case PINK_WOOL: case PINK_SHULKER_BOX: case PINK_CONCRETE: case PINK_CONCRETE_POWDER:
			case PINK_STAINED_GLASS: case PINK_STAINED_GLASS_PANE: case PINK_TERRACOTTA: case PINK_GLAZED_TERRACOTTA:
			case PINK_CARPET: case PINK_BANNER: case PINK_WALL_BANNER:
				return DyeColor.PINK;
			case PURPLE_DYE: case PURPLE_BED:
			case PURPLE_WOOL: case PURPLE_SHULKER_BOX: case PURPLE_CONCRETE: case PURPLE_CONCRETE_POWDER:
			case PURPLE_STAINED_GLASS: case PURPLE_STAINED_GLASS_PANE: case PURPLE_TERRACOTTA: case PURPLE_GLAZED_TERRACOTTA:
			case PURPLE_CARPET: case PURPLE_BANNER: case PURPLE_WALL_BANNER:
				return DyeColor.PURPLE;
			case RED_DYE: case RED_BED:
			case RED_WOOL: case RED_SHULKER_BOX: case RED_CONCRETE: case RED_CONCRETE_POWDER:
			case RED_STAINED_GLASS: case RED_STAINED_GLASS_PANE: case RED_TERRACOTTA: case RED_GLAZED_TERRACOTTA:
			case RED_CARPET: case RED_BANNER: case RED_WALL_BANNER:
				return DyeColor.RED;
			case WHITE_DYE: case WHITE_BED:
			case WHITE_WOOL: case WHITE_SHULKER_BOX: case WHITE_CONCRETE: case WHITE_CONCRETE_POWDER:
			case WHITE_STAINED_GLASS: case WHITE_STAINED_GLASS_PANE: case WHITE_TERRACOTTA: case WHITE_GLAZED_TERRACOTTA:
			case WHITE_CARPET: case WHITE_BANNER: case WHITE_WALL_BANNER:
				return DyeColor.WHITE;
			case YELLOW_DYE: case YELLOW_BED:
			case YELLOW_WOOL: case YELLOW_SHULKER_BOX: case YELLOW_CONCRETE: case YELLOW_CONCRETE_POWDER:
			case YELLOW_STAINED_GLASS: case YELLOW_STAINED_GLASS_PANE: case YELLOW_TERRACOTTA: case YELLOW_GLAZED_TERRACOTTA:
			case YELLOW_CARPET: case YELLOW_BANNER: case YELLOW_WALL_BANNER:
				return DyeColor.YELLOW;
			default:
				return null;
		}
	}
	/*public Color convertColor(DyeColor dyeColor){
		switch(dyeColor){
			case BLACK:
				return Color.BLACK;
			case BLUE:
				return Color.BLUE;
			case BROWN:
				return Color.
			case CYAN:
				return Color.TEAL;
			case GRAY:
				return Color.GRAY;
			case GREEN:
				return Color.GREEN;
			case LIGHT_BLUE:
				return Color.
			case LIGHT_GRAY:
				return Color.SILVER;
			case LIME:
				return Color.LIME;
			case MAGENTA:
				return Color.FUCHSIA
			case ORANGE:
				return Color.ORANGE;
			case PINK:
				return Color.
			case PURPLE:
				return Color.PURPLE;
			case RED:
				return Color.RED;
			case WHITE:
				return Color.WHITE;
			case YELLOW:
				return Color.YELLOW;
			//BROWN, LIGHT_BLUE, MAGENTA, PINK =?> FUCHSIA, MAROON, NAVY, OLIVE
		}
	}*/

	@Override public boolean isFlowerPot(Material mat){
		switch(mat){
			case FLOWER_POT:
			case POTTED_ACACIA_SAPLING:
			case POTTED_ALLIUM:
			case POTTED_AZURE_BLUET:
			case POTTED_BAMBOO:
			case POTTED_BIRCH_SAPLING:
			case POTTED_BLUE_ORCHID:
			case POTTED_BROWN_MUSHROOM:
			case POTTED_CACTUS:
			case POTTED_DANDELION:
			case POTTED_DARK_OAK_SAPLING:
			case POTTED_DEAD_BUSH:
			case POTTED_FERN:
			case POTTED_JUNGLE_SAPLING:
			case POTTED_OAK_SAPLING:
			case POTTED_ORANGE_TULIP:
			case POTTED_OXEYE_DAISY:
			case POTTED_PINK_TULIP:
			case POTTED_POPPY:
			case POTTED_RED_MUSHROOM:
			case POTTED_RED_TULIP:
			case POTTED_SPRUCE_SAPLING:
			case POTTED_WHITE_TULIP:
				return true;
			default:
				return false;
		}
	}

	@Override public boolean isSign(Material mat){
		switch(mat){
			case ACACIA_SIGN:
			case BIRCH_SIGN:
			case DARK_OAK_SIGN:
			case JUNGLE_SIGN:
			case OAK_SIGN:
			case SPRUCE_SIGN:
				return true;
			default:
				return false;
		}
	}

	@Override public boolean isWallSign(Material mat){
		switch(mat){
			case ACACIA_WALL_SIGN:
			case BIRCH_WALL_SIGN:
			case DARK_OAK_WALL_SIGN:
			case JUNGLE_WALL_SIGN:
			case OAK_WALL_SIGN:
			case SPRUCE_WALL_SIGN:
				return true;
			default:
				return false;
		}
	}

	@Override public boolean isObtainable(Material mat, ObtainableOptions... opts){
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
			case GRASS_PATH: // Not obtainable with silk
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
			case BAMBOO_SAPLING:
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
			case JIGSAW:
				return canObtain.contains(ObtainableOptions.STRUCTURE_BLOCKS);
			default:
				if(isFlowerPot(mat) && mat != Material.FLOWER_POT) return false; // Cannot be held
				if(isWallBanner(mat) || isWallSign(mat)/* || wCoralFan || wHead */) return false; // Cannot be held
				if(isInfested(mat)) return canObtain.contains(ObtainableOptions.SILK_INFESTED);
				if(isSpawnEgg(mat)) return canObtain.contains(ObtainableOptions.MOB_EGGS);
				return true;
		}
	}

	@Override public BlockFace getFragileFace(Material mat, BlockFace facing){
		switch(mat){
//			case WATER:
//			case STATIONARY_WATER:
//			case LAVA:
//			case STATIONARY_LAVA:
			case GRASS:
			case DEAD_BUSH:
			case DANDELION:
			case POPPY:
			case BROWN_MUSHROOM:
			case RED_MUSHROOM:
			case FIRE:
			case REDSTONE_WIRE:
			case WHEAT:
			case CARROTS:
			case POTATOES:
			case BEETROOTS:
			case MELON_STEM:
			case PUMPKIN_STEM:
			case REDSTONE_TORCH:
			case TORCH:
			case SNOW:
			case CACTUS:
			case SUGAR_CANE:
			case CAKE:
			case REPEATER:
			case COMPARATOR:
			case LILY_PAD:
			case NETHER_WART:
			case CARROT:
			case POTATO:
			case CHORUS_PLANT:
			case CHORUS_FLOWER:
			case BAMBOO:
			case SWEET_BERRY_BUSH:
			case SCAFFOLDING:
			case CORNFLOWER:
			case LILY_OF_THE_VALLEY:
			case WITHER_ROSE:
				return BlockFace.DOWN;
			//case VINE:
				//TODO: BlockFace.UP, but only if nothing behind this block! :o
			//case BELL:
			//case LANTERN:
				//TODO: BlockFace.UP if hanging, otherwise BlockFace.DOWN
			case LADDER:
			case REDSTONE_WALL_TORCH:
			case WALL_TORCH:
			case LEVER:
				return facing.getOppositeFace();
			default:
				if(isCarpet(mat) || isBanner(mat) || isPressurePlate(mat) || isDoor(mat)
				|| isDoublePlant(mat) || isSapling(mat) || isFlowerPot(mat) || isSign(mat)) return BlockFace.DOWN;
				if(isButton(mat) || isWallBanner(mat) || isWallSign(mat)) return facing.getOppositeFace();
				return null;
		}
	}
}