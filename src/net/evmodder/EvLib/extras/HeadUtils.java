package net.evmodder.EvLib.extras;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;

public final class HeadUtils {
	// ========================= Commented out because UNUSED =======================
	/*public static final String[] MHF_Heads = new String[]{//Standard, Mojang-Provided MHF Heads
		"MHF_Alex", "MHF_Blaze", "MHF_CaveSpider", "MHF_Chicken", "MHF_Cow", "MHF_Creeper", "MHF_Enderman", "MHF_Ghast",
		"MHF_Golem", "MHF_Herobrine", "MHF_LavaSlime", "MHF_MushroomCow", "MHF_Ocelot", "MHF_Pig", "MHF_PigZombie",
		"MHF_Sheep", "MHF_Skeleton", "MHF_Slime", "MHF_Spider", "MHF_Squid", "MHF_Steve", "MHF_Villager",
		"MHF_Witch", "MHF_Wither", "MHF_WSkeleton", "MHF_Zombie",
		
		"MHF_Cactus", "MHF_Cake", "MHF_Chest", "MHF_CoconutB", "MHF_CoconutG", "MHF_Melon", "MHF_OakLog",
		"MHF_Present1","MHF_Present2", "MHF_Pumpkin", "MHF_TNT", "MHF_TNT2",
		
		"MHF_ArrowUp", "MHF_ArrowDown", "MHF_ArrowLeft", "MHF_ArrowRight", "MHF_Exclamation", "MHF_Question",
	};
	public static final Map<String, String> MHF_Lookup = Stream.of(MHF_Heads).collect(Collectors.toMap(h -> h.toUpperCase(), h -> h));*/

	/*public static final HashMap<EntityType, String> customHeads;//People who have set their skin to an Entity's head
	static{
		customHeads = new HashMap<>();
		customHeads.put(EntityType.BAT, "ManBatPlaysMC");
		customHeads.put(EntityType.ELDER_GUARDIAN, "MHF_EGuardian");//made by player
		customHeads.put(EntityType.ENDERMITE, "MHF_Endermites");//made by player
		customHeads.put(EntityType.EVOKER, "MHF_Evoker");//made by player
		customHeads.put(EntityType.GUARDIAN, "MHF_Guardian");//made by player
		customHeads.put(EntityType.HORSE, "gavertoso");
		customHeads.put(EntityType.PARROT, "MHF_Parrot");//made by player
		customHeads.put(EntityType.POLAR_BEAR, "NiXWorld");
		customHeads.put(EntityType.RABBIT, "MHF_Rabbit");//made by player
		customHeads.put(EntityType.SHULKER, "MHF_Shulker");//made by player
		customHeads.put(EntityType.SILVERFISH, "MHF_Silverfish");//made by player
		customHeads.put(EntityType.VEX, "MHF_Vex");//made by player
		customHeads.put(EntityType.VINDICATOR, "Vindicator");
		customHeads.put(EntityType.SNOWMAN, "MHF_SnowGolem");//made by player
		customHeads.put(EntityType.WITCH, "MHF_Witch");//made by player
		customHeads.put(EntityType.WOLF, "MHF_Wolf");//made by player
		customHeads.put(EntityType.ZOMBIE_VILLAGER, "scraftbrothers11");
	}*/

	/*public static String getMHFHeadName(String eType){
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
	}*/
	public static String normalizedNameFromMHFName(String mhfName){ // Still referenced (reflectively) by EntityUtils
		mhfName = mhfName.substring(4);
		String mhfCompact = mhfName.replace("_", "").replace(" ", "").toLowerCase();
		if(mhfCompact.equals("lavaslime")) return "Magma Cube";
		else if(mhfCompact.equals("golem")) return "Iron Golem";
		else if(mhfCompact.equals("pigzombie")) return "Zombie Pigman";
		else if(mhfCompact.equals("mushroomcow")) return "Mooshroom";
		else if(mhfName.isEmpty()) return "";
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

	// 1.21.1+ changed the field's type from GameProfile to ResolveableProfile

	private static Constructor<?> resolveableProfileConstructor;
	private static Object convertToResolvedProfile(GameProfile profile){
		try{
			if(resolveableProfileConstructor == null) resolveableProfileConstructor =
					Class.forName("net.minecraft.world.item.component.ResolvableProfile").getConstructor(GameProfile.class);
			return resolveableProfileConstructor.newInstance(profile);
		}
		catch(ReflectiveOperationException e){
			Bukkit.getLogger().severe("DropHeads-HeadUtils: Unable to convert to ResolvedProfile");
		}
		return profile;
	}

	private static Method[] resolveableProfileRecordAccessors;
	private static Field fieldResolveableName, fieldResolveableId, fieldResolveableProperties;
	@SuppressWarnings("unchecked")
	private static GameProfile convertFromResolvedProfile(Object profile){
		if(profile == null) return null;
		try{
			if(resolveableProfileRecordAccessors == null && fieldResolveableProperties == null){
				final Class<?> clazzResolveableProfile = Class.forName("net.minecraft.world.item.component.ResolvableProfile");
				//if(clazzResolveableProfile.isInstance(profile)) printError = true;
				try{
					fieldResolveableName = clazzResolveableProfile.getDeclaredField("name");
					fieldResolveableName.setAccessible(true);
					fieldResolveableId = clazzResolveableProfile.getDeclaredField("id");
					fieldResolveableId.setAccessible(true);
					fieldResolveableProperties = clazzResolveableProfile.getDeclaredField("properties");
					fieldResolveableProperties.setAccessible(true);
				}
				// 1.21.4+ changed ResolveableProfile class type to a Record
				catch(NoSuchFieldException e){
					//if((boolean)Class.class.getMethod("isRecord").invoke(clazzResolveableProfile)){
					Object[] rcs = (Object[])Class.class.getMethod("getRecordComponents").invoke(clazzResolveableProfile);
					Method getAccessor = rcs[0].getClass().getMethod("getAccessor");
					resolveableProfileRecordAccessors = new Method[rcs.length];
					for(int i=0; i<3; ++i) resolveableProfileRecordAccessors[i] = (Method)getAccessor.invoke(rcs[i]);
				}
			}
			final Optional<String> nameOptional;
			final Optional<UUID> uuidOptional;
			final PropertyMap properties;
			if(resolveableProfileRecordAccessors != null){
				nameOptional = (Optional<String>)resolveableProfileRecordAccessors[0].invoke(profile);
				uuidOptional = (Optional<UUID>)resolveableProfileRecordAccessors[1].invoke(profile);
				properties = (PropertyMap)resolveableProfileRecordAccessors[2].invoke(profile);
			}
			else{
				nameOptional = (Optional<String>)fieldResolveableName.get(profile);
				uuidOptional = (Optional<UUID>)fieldResolveableId.get(profile);
				properties = (PropertyMap)fieldResolveableProperties.get(profile);
			}
			final String name = nameOptional.orElse("");
			final UUID uuid = uuidOptional.orElseThrow(() -> new NullPointerException("UUID missing in ResolvedProfile"));
			GameProfile gp = new GameProfile(uuid, name);
			gp.getProperties().putAll(properties);
			return gp;
		}
		catch(ReflectiveOperationException e){
			Bukkit.getLogger().severe("DropHeads-HeadUtils: Unable to convert from ResolvedProfile");
			/*if(printError) */e.printStackTrace();
			fieldResolveableProperties = null;
			resolveableProfileRecordAccessors = null;
		}
		return (GameProfile)profile;
	}

	private static Field fieldProfileItem, fieldProfileBlock;
	public static void setGameProfile(SkullMeta meta, GameProfile profile){
		try{
			if(fieldProfileItem == null){fieldProfileItem = meta.getClass().getDeclaredField("profile");fieldProfileItem.setAccessible(true);}
			fieldProfileItem.set(meta, fieldProfileItem.getType().equals(GameProfile.class) ? profile : convertToResolvedProfile(profile));
		}
		catch(ReflectiveOperationException e){e.printStackTrace();}
	}
	public static void setGameProfile(Skull skull, GameProfile profile){
		try{
			if(fieldProfileBlock == null){fieldProfileBlock = skull.getClass().getDeclaredField("profile");fieldProfileBlock.setAccessible(true);}
			fieldProfileBlock.set(skull, fieldProfileBlock.getType().equals(GameProfile.class) ? profile : convertToResolvedProfile(profile));
		}
		catch(ReflectiveOperationException e){e.printStackTrace();}
	}
	public static GameProfile getGameProfile(SkullMeta meta){
		try{
			if(fieldProfileItem == null){fieldProfileItem = meta.getClass().getDeclaredField("profile");fieldProfileItem.setAccessible(true);}
			return fieldProfileItem.getType().equals(GameProfile.class)
					? (GameProfile)fieldProfileItem.get(meta) : convertFromResolvedProfile(fieldProfileItem.get(meta));
		}
		catch(ReflectiveOperationException e){e.printStackTrace();}
		return null;
	}
	public static GameProfile getGameProfile(Skull skull){
		try{
			if(fieldProfileBlock == null){fieldProfileBlock = skull.getClass().getDeclaredField("profile");fieldProfileBlock.setAccessible(true);}
			return fieldProfileBlock.getType().equals(GameProfile.class)
					? (GameProfile)fieldProfileBlock.get(skull) : convertFromResolvedProfile(fieldProfileBlock.get(skull));
		}
		catch(ReflectiveOperationException e){e.printStackTrace();}
		return null;
	}

	public static ItemStack makeCustomHead(GameProfile profile, boolean setOwner){
		final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		final SkullMeta meta = (SkullMeta) head.getItemMeta();
		setGameProfile(meta, profile);
		if(setOwner && profile.getId() != null){
			final OfflinePlayer p = Bukkit.getOfflinePlayer(profile.getId());
			if(p != null){
				meta.setOwningPlayer(p);
				//if(p.getName() != null) meta.setOwner(p.getName());
			}
		}
		head.setItemMeta(meta);
		return head;
	}

	// ========================= Commented out because UNUSED =======================
	/*public static ItemStack makeSkull(String textureCode, String headName){
		ItemStack item = new ItemStack(Material.PLAYER_HEAD);
		if(textureCode == null) return item;
		SkullMeta meta = (SkullMeta) item.getItemMeta();

		GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes(textureCode.getBytes()), headName);
		profile.getProperties().put("textures", new Property("textures", textureCode));
		setGameProfile(meta, profile);

		meta.setDisplayName(headName);
		item.setItemMeta(meta);
		return item;
	}*/
	/*public static ItemStack makePlayerHead(OfflinePlayer player){
		GameProfile profile = new GameProfile(player.getUniqueId(), player.getName());
		ItemStack head = makeCustomHead(profile);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwningPlayer(player);
		head.setItemMeta(meta);
		return head;
	}*/

	// TODO: Move to TypeUtils perhaps?
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
				if(type.name().equals("PIGLIN_HEAD") || type.name().equals("PIGLIN_WALL_HEAD")) return true;
				return false;
		}
	}

	public static boolean isPlayerHead(Material type){
		return type == Material.PLAYER_HEAD || type == Material.PLAYER_WALL_HEAD;
	}

	public static EntityType getEntityFromHead(Material type){
		switch(type){
			case CREEPER_HEAD:
			case CREEPER_WALL_HEAD:
				return EntityType.CREEPER;
			case DRAGON_HEAD:
			case DRAGON_WALL_HEAD:
				return EntityType.ENDER_DRAGON;
			case PLAYER_HEAD:
			case PLAYER_WALL_HEAD:
				return EntityType.PLAYER;
			case ZOMBIE_HEAD:
			case ZOMBIE_WALL_HEAD:
				return EntityType.ZOMBIE;
			case SKELETON_SKULL:
			case SKELETON_WALL_SKULL:
				return EntityType.SKELETON;
			case WITHER_SKELETON_SKULL:
			case WITHER_SKELETON_WALL_SKULL:
				return EntityType.WITHER_SKELETON;
			default:
				if(type.name().equals("PIGLIN_HEAD") || type.name().equals("PIGLIN_WALL_HEAD")) return EntityType.valueOf("PIGLIN");
				throw new IllegalArgumentException("Unkown head type: "+type);
		}
	}

	public enum HeadType{HEAD, SKULL, TOE}
	public static HeadType getDroppedHeadType(EntityType eType){  // Replaces `isSkeletal()`, which is now in DropHeads>JunkUtils
		if(eType == null) return null;
		switch(eType){
			case SKELETON:
			case SKELETON_HORSE:
			case WITHER_SKELETON:
			case STRAY:
				return HeadType.SKULL;
			case GIANT:
				return HeadType.TOE;
			default:
				return HeadType.HEAD;
		}
	}

	public static boolean dropsHeadFromChargedCreeper(EntityType eType){// In vanilla.
		switch(eType){
			case ZOMBIE:
			case CREEPER:
			case SKELETON:
			case WITHER_SKELETON:
			//case ZOMBIE_VILLAGER: // Surprisingly not, actually.
				return true;
			default:
				return eType.name().equals("PIGLIN");
		}
	}

	public static boolean hasGrummName(Nameable e){
		return e.getCustomName() != null && (e.getCustomName().equals("Dinnerbone") || e.getCustomName().equals("Grumm"));
	}
}