package net.evmodder.EvLib.bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.evmodder.EvLib.util.ReflectionUtils;

public record YetAnotherProfile(UUID id, String name, Multimap<String, Property> properties){
	public YetAnotherProfile(UUID id, String name){this(id, name, null);}

	private static final Method method_GameProfile_id = ReflectionUtils.findMethodByName(GameProfile.class, "id", "getId");
	private static final Method method_GameProfile_name = ReflectionUtils.findMethodByName(GameProfile.class, "name", "getName");
	private static final Method method_GameProfile_properties = ReflectionUtils.findMethodByName(GameProfile.class, "properties", "getProperties");
	private static final UUID getId(GameProfile profile){return (UUID)ReflectionUtils.call(method_GameProfile_id, profile);}
	private static final String getName(GameProfile profile){return (String)ReflectionUtils.call(method_GameProfile_name, profile);}
	public static final PropertyMap getProperties(GameProfile profile){return (PropertyMap)ReflectionUtils.call(method_GameProfile_properties, profile);}
	private static Multimap<String, Property> convertMapType(PropertyMap pm){
		if(pm == null) return null;
		Multimap<String, Property> properties = LinkedListMultimap.create();
		for(var e : pm.entries()) properties.put(e.getKey(), e.getValue());
		return properties;
	}
	public static YetAnotherProfile fromGameProfile(GameProfile profile){
		return new YetAnotherProfile(getId(profile), getName(profile), convertMapType(getProperties(profile)));
	}

	private static Class<?> class_CraftPlayerProfile;
	private static Field field_CraftPlayerProfile_uniqueId, field_CraftPlayerProfile_name, field_CraftPlayerProfile_properties;
	private static Field field_CraftPlayerProfile_profile;
	static{
		class_CraftPlayerProfile = ReflectionUtils.getClass("com.destroystokyo.paper.profile.CraftPlayerProfile", "{cb}.profile.CraftPlayerProfile");
		try{ // Paper 1.21.9+
			field_CraftPlayerProfile_profile = ReflectionUtils.getField(class_CraftPlayerProfile, "profile");
		}
		catch(RuntimeException e1){
			try{
				field_CraftPlayerProfile_uniqueId = ReflectionUtils.getField(class_CraftPlayerProfile, "uniqueId");
				field_CraftPlayerProfile_name = ReflectionUtils.getField(class_CraftPlayerProfile, "name");
				field_CraftPlayerProfile_properties = ReflectionUtils.getField(class_CraftPlayerProfile, "properties");
			}
			catch(RuntimeException e2){
//				e.printStackTrace();
			}
		}
	}
	private static UUID getId(Object playerProfile){
		try{return (UUID)ReflectionUtils.get(field_CraftPlayerProfile_uniqueId, playerProfile);}
		catch(RuntimeException e){e.printStackTrace(); return null;}
	}
	private static String getName(Object playerProfile){
		try{return (String)ReflectionUtils.get(field_CraftPlayerProfile_name, playerProfile);}
		catch(RuntimeException e){e.printStackTrace(); return null;}
	}
	@SuppressWarnings("unchecked")
	private static Multimap<String, Property> getProperties(Object playerProfile){
		try{return (Multimap<String, Property>)ReflectionUtils.get(field_CraftPlayerProfile_properties, playerProfile);}
		catch(RuntimeException e){e.printStackTrace(); return null;}
	}
	public static YetAnotherProfile fromPlayerProfile(Object playerProfile){
		if(field_CraftPlayerProfile_profile != null){
			return fromGameProfile((GameProfile)ReflectionUtils.get(field_CraftPlayerProfile_profile, playerProfile));
		}
		else return new YetAnotherProfile(getId(playerProfile), getName(playerProfile), getProperties(playerProfile));
	}

	private static Method method_OfflinePlayer_getPlayerProfile;
	static{
		try{
			method_OfflinePlayer_getPlayerProfile = OfflinePlayer.class.getMethod("getPlayerProfile");
		}
		catch(ReflectiveOperationException e){
//			e.printStackTrace();
		}
	}
	public static YetAnotherProfile fromPlayer(Player player){
		try{
			return fromPlayerProfile(method_OfflinePlayer_getPlayerProfile.invoke(player));
		}
		catch(ReflectiveOperationException e){
			e.printStackTrace();
			return null;
		}
	}

	private static Constructor<?> cnstr_GameProfile;
	private static Constructor<?> cnstr_PropertyMap;
	static{
		try{
			cnstr_PropertyMap = ReflectionUtils.getConstructor(PropertyMap.class, Multimap.class);
			cnstr_GameProfile = ReflectionUtils.getConstructor(GameProfile.class, UUID.class, String.class, PropertyMap.class);
		}
		catch(RuntimeException e){}
	}
	public GameProfile asGameProfile(){
		if(properties == null) return new GameProfile(id, name);
		if(cnstr_GameProfile != null){
			PropertyMap pm = (PropertyMap)ReflectionUtils.construct(cnstr_PropertyMap, properties);
			return (GameProfile)ReflectionUtils.construct(cnstr_GameProfile, id, name, pm);
		}
		GameProfile gp = new GameProfile(id, name);
		getProperties(gp).putAll(properties);
		return gp;
	}
	private static Method method_Bukkit_createPlayerProfile;
	static{
		try{method_Bukkit_createPlayerProfile = Bukkit.class.getMethod("createPlayerProfile", UUID.class, String.class);}
		catch(ReflectiveOperationException e){/*e.printStackTrace();*/}
	}
	public Object asPlayerProfile(){
		try{
			Object playerProfile = method_Bukkit_createPlayerProfile.invoke(null, id, name);
			if(properties != null) getProperties(playerProfile).putAll(properties);
			return playerProfile;
		}
		catch(ReflectiveOperationException e){
			e.printStackTrace();
		}
		return null;
	}
	private static Constructor<?> cnstr_ResolvableProfile;
	private static Method method_ResolvableProfile_fromGameProfile;
	static{
		Class<?> class_ResolvableProfile = null;
		try{
			class_ResolvableProfile = Class.forName("net.minecraft.world.item.component.ResolvableProfile");
		}
		catch(ClassNotFoundException e){e.printStackTrace();}
		if(class_ResolvableProfile != null) try{
			cnstr_ResolvableProfile = class_ResolvableProfile.getConstructor(GameProfile.class);
		}
		catch(ReflectiveOperationException e1){
			try{
				method_ResolvableProfile_fromGameProfile = ReflectionUtils.findMethod(
						class_ResolvableProfile, /*isStatic=*/true, class_ResolvableProfile, GameProfile.class);
			}
			catch(RuntimeException e2){
				Bukkit.getLogger().severe("YetAnotherProfile: Unable to convert to ResolvableProfile");
				e2.printStackTrace();
			}
		}
	}
	public Object asResolvableProfile(){
		try{
			if(cnstr_ResolvableProfile != null) return cnstr_ResolvableProfile.newInstance(asGameProfile());
			if(method_ResolvableProfile_fromGameProfile != null) return method_ResolvableProfile_fromGameProfile.invoke(null, asGameProfile());
			throw new RuntimeException("no conversion function found for YetAnotherProfile -> ResolvableProfile");
		}
		catch(ReflectiveOperationException e){e.printStackTrace();}
		return null;
	}

	private static Method[] method_ResolvableProfile_RecordAccessors;
	private static Field field_ResolvableProfile_name, field_ResolvableProfile_id, field_ResolvableProfile_properties;
	private static Field field_ResolvableProfile_partialProfile;
	static{
		try{
			final Class<?> class_ResolveableProfile = Class.forName("net.minecraft.world.item.component.ResolvableProfile");
			//if(class_ResolveableProfile.isInstance(profile)) printError = true;
			try{
				// 1.21.9+
				field_ResolvableProfile_partialProfile = class_ResolveableProfile.getDeclaredField("partialProfile");
			}
			catch(NoSuchFieldException e1){
				try{ // Pre 1.21.5
					field_ResolvableProfile_id = class_ResolveableProfile.getDeclaredField("id");
					field_ResolvableProfile_id.setAccessible(true);
					field_ResolvableProfile_name = class_ResolveableProfile.getDeclaredField("name");
					field_ResolvableProfile_name.setAccessible(true);
					field_ResolvableProfile_properties = class_ResolveableProfile.getDeclaredField("properties");
					field_ResolvableProfile_properties.setAccessible(true);
				}
				// 1.21.4+ changed ResolveableProfile class type to a Record
				catch(NoSuchFieldException e2){
					//if((boolean)Class.class.getMethod("isRecord").invoke(clazzResolveableProfile)){
					Object[] rcs = (Object[])Class.class.getMethod("getRecordComponents").invoke(class_ResolveableProfile);
					if(rcs == null){
//						Bukkit.getLogger().warning("YAP: No support ResolvableProfile data extraction method found");
					}
					else{
						Method getAccessor = rcs[0].getClass().getMethod("getAccessor");
						method_ResolvableProfile_RecordAccessors = new Method[rcs.length];
						for(int i=0; i<3; ++i) method_ResolvableProfile_RecordAccessors[i] = (Method)getAccessor.invoke(rcs[i]);
					}
				}
			}
		}
		catch(ReflectiveOperationException e){
//			e.printStackTrace();
		}
	}
	@SuppressWarnings("unchecked")
	private static YetAnotherProfile fromResolvableProfile(Object profile){
		if(profile == null) return null;

		final Optional<String> nameOptional;
		final Optional<UUID> uuidOptional;
		final PropertyMap properties;
		try{
			if(field_ResolvableProfile_partialProfile != null){
				return fromGameProfile((GameProfile)field_ResolvableProfile_partialProfile.get(profile));
			}
			else if(method_ResolvableProfile_RecordAccessors != null){
				uuidOptional = (Optional<UUID>)method_ResolvableProfile_RecordAccessors[1].invoke(profile);
				nameOptional = (Optional<String>)method_ResolvableProfile_RecordAccessors[0].invoke(profile);
				properties = (PropertyMap)method_ResolvableProfile_RecordAccessors[2].invoke(profile);
			}
			else if(field_ResolvableProfile_properties != null){
				uuidOptional = (Optional<UUID>)field_ResolvableProfile_id.get(profile);
				nameOptional = (Optional<String>)field_ResolvableProfile_name.get(profile);
				properties = (PropertyMap)field_ResolvableProfile_properties.get(profile);
			}
			else{
				Bukkit.getLogger().severe("YAP: Unable to convert from ResolvableProfile");
				return null;
			}
		}
		catch(ReflectiveOperationException e){
			Bukkit.getLogger().severe("YAP: Unable to convert from ResolvableProfile");
			e.printStackTrace();
			return null;
		}
		final String name = nameOptional.orElse("");
		final UUID uuid = uuidOptional.orElseThrow(() -> new NullPointerException("UUID missing in ResolvableProfile"));
//		GameProfile gp = new GameProfile(uuid, name);
//		if(properties != null) getProperties(gp).putAll(properties);
//		return new YetAnotherProfile(gp);
		return new YetAnotherProfile(uuid, name, convertMapType(properties));
	}

	private static Field field_SkullMeta_profile, field_Skull_profile;
	private static boolean useGameProfileForCraftSkull;
	static{
		try{
			field_SkullMeta_profile = ReflectionUtils.getField(ReflectionUtils.getClass("{cb}.inventory.CraftMetaSkull"), "profile");
			field_Skull_profile = ReflectionUtils.getField(ReflectionUtils.getClass("{cb}.block.CraftSkull"), "profile");
		}
		catch(RuntimeException e){
			e.printStackTrace();
		}
		useGameProfileForCraftSkull = field_SkullMeta_profile.getType().equals(GameProfile.class);
		Bukkit.getLogger().info("YetAnotherProfile: use GameProfile for Skull/SkullMeta: "+useGameProfileForCraftSkull);
	}
	public void set(SkullMeta skullMeta){
		try{
			field_SkullMeta_profile.set(skullMeta, useGameProfileForCraftSkull ? asGameProfile() : asResolvableProfile());
		}
		catch(ReflectiveOperationException e){
			e.printStackTrace();
		}
	}
	public void set(Skull skull){
		try{
			field_Skull_profile.set(skull, useGameProfileForCraftSkull ? asGameProfile() : asResolvableProfile());
		}
		catch(ReflectiveOperationException e){
			e.printStackTrace();
		}
	}
	public static YetAnotherProfile fromSkullMeta(SkullMeta skullMeta){
		try{
			Object profile = field_SkullMeta_profile.get(skullMeta);
			return useGameProfileForCraftSkull ? fromGameProfile((GameProfile)profile) : fromResolvableProfile(profile);
		}
		catch(ReflectiveOperationException e){
			e.printStackTrace();
			return null;
		}
	}
	public static YetAnotherProfile fromSkull(Skull skull){
		try{
			Object profile = field_Skull_profile.get(skull);
			return useGameProfileForCraftSkull ? fromGameProfile((GameProfile)profile) : fromResolvableProfile(profile);
		}
		catch(ReflectiveOperationException e){
			e.printStackTrace();
			return null;
		}
	}
}