package net.evmodder.EvLib.types;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public abstract class TypeUtils{
	static int version = 13;
	static{
		try{version = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);}
		catch(IllegalArgumentException | ArrayIndexOutOfBoundsException e){
			System.err.println("EvLib failed to detect server version!");
		}
		if(version < 13){
			Bukkit.getLogger().severe("This version of EvLib does not support servers below 1.13!");
		}
	}

	public abstract boolean isSpawnEgg(Material mat);
	public abstract EntityType getSpawnedMob(Material spawnEggType);
	public abstract Material getSpawnEgg(EntityType eType);

	public abstract boolean isOre(Material mat);

	public abstract boolean isInfested(Material mat);

	public abstract ChatColor getRarityColor(ItemStack item, boolean checkCustomName);

	public abstract boolean isDye(Material mat);
	public abstract boolean isBed(Material mat);
	public abstract boolean isWool(Material mat);
	public abstract boolean isShulkerBox(Material mat);
	public abstract boolean isConcrete(Material mat);
	public abstract boolean isConcretePowder(Material mat);
	public abstract boolean isStainedGlass(Material mat);
	public abstract boolean isStainedGlassPane(Material mat);
	public abstract boolean isTerracotta(Material mat);
	public abstract boolean isGlazedTerracotta(Material mat);
	public abstract boolean isCarpet(Material mat);
	public abstract boolean isBanner(Material mat);
	public abstract boolean isWallBanner(Material mat);

	public abstract DyeColor getDyeColor(Material mat);

	public abstract boolean isFlowerPot(Material mat);

	public abstract boolean isSign(Material mat);

	public abstract boolean isWallSign(Material mat);

	public abstract boolean isDoublePlant(Material mat);

	public abstract boolean isRail(Material mat);

	public abstract boolean isSapling(Material mat);

	public abstract boolean isButton(Material mat);

	public abstract boolean isPressurePlate(Material mat);

	public abstract boolean isDoor(Material mat);

	public abstract boolean isPlanks(Material mat);

	public abstract boolean isSword(Material mat);
	public abstract boolean isAxe(Material mat);
	public abstract boolean isPickaxe(Material mat);
	public abstract boolean isShovel(Material mat);
	public abstract boolean isHoe(Material mat);

	protected abstract byte pickaxeNumber(Material pickType);
	public abstract boolean pickIsAtLeast(Material pickType, Material needPick);
	protected abstract byte swordNumber(Material swordType);
	public abstract boolean swordIsAtLeast(Material swordType, Material needSword);


	enum ObtainableOptions{
		SILK_SPAWNERS, SILK_INFESTED, MOB_EGGS, CMD_BLOCKS,
		BEDROCK, END_PORTAL_FRAMES, BARRIERS, STRUCTURE_BLOCKS, PETRIFIED_SLABS,
		ITEM_LORE, ITEM_NAME_COLOR, CONFLICTING_ENCHANTS, ABOVE_MAX_ENCHANTS, OVERSTACKED,
		PLAYER_HEADS, TATTERED_BOOKS
	};
	public abstract boolean isObtainable(Material mat, ObtainableOptions... opts);

	// Broken if the block relative to a given BlockFace is removed
	public abstract BlockFace getFragileFace(Material mat, BlockFace facing);
}