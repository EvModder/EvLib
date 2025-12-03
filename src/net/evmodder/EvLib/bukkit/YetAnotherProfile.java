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
import net.evmodder.EvLib.bukkit.ReflectionUtils.RefMethod;

public record YetAnotherProfile(UUID id, String name, Multimap<String, Property> properties){
	public YetAnotherProfile(UUID id, String name){this(id, name, LinkedListMultimap.create());}

	private static final RefMethod method_GameProfile_id = ReflectionUtils.getRefClass(GameProfile.class).findMethodByName("id", "getId");
	private static final RefMethod method_GameProfile_name = ReflectionUtils.getRefClass(GameProfile.class).findMethodByName("name", "getName");
	private static final RefMethod method_GameProfile_properties = ReflectionUtils.getRefClass(GameProfile.class).findMethodByName("properties", "getProperties");
	private static final UUID getId(GameProfile profile){return (UUID)method_GameProfile_id.of(profile).call();}
	private static final String getName(GameProfile profile){return (String)method_GameProfile_name.of(profile).call();}
	public static final PropertyMap getProperties(GameProfile profile){return (PropertyMap)method_GameProfile_properties.of(profile).call();}
	private static Multimap<String, Property> convertMapType(PropertyMap pm){
		Multimap<String, Property> properties = LinkedListMultimap.create();
		for(var e : pm.entries()) properties.put(e.getKey(), e.getValue());
		return properties;
	}
	public static YetAnotherProfile fromGameProfile(GameProfile profile){
		return new YetAnotherProfile(getId(profile), getName(profile), convertMapType(getProperties(profile)));
	}

	private static Class<?> class_CraftPlayerProfile;
	private static Field field_CraftPlayerProfile_uniqueId, field_CraftPlayerProfile_name, field_CraftPlayerProfile_properties;
	static{
		try{
			class_CraftPlayerProfile = ReflectionUtils.getRefClass("{cb}.profile.CraftPlayerProfile").getRealClass();
			field_CraftPlayerProfile_uniqueId = class_CraftPlayerProfile.getField("uniqueId");
			field_CraftPlayerProfile_name = class_CraftPlayerProfile.getField("name");
			field_CraftPlayerProfile_properties = class_CraftPlayerProfile.getField("properties");
		}
		catch(RuntimeException | ReflectiveOperationException e){
//			e.printStackTrace();
		}
	}
	private static UUID getId(Object playerProfile){
		try{return (UUID)field_CraftPlayerProfile_uniqueId.get(playerProfile);}
		catch(ReflectiveOperationException e){e.printStackTrace(); return null;}
	}
	private static String getName(Object playerProfile){
		try{return (String)field_CraftPlayerProfile_name.get(playerProfile);}
		catch(ReflectiveOperationException e){e.printStackTrace(); return null;}
	}
	@SuppressWarnings("unchecked")
	private static Multimap<String, Property> getProperties(Object playerProfile){
		try{return (Multimap<String, Property>)field_CraftPlayerProfile_properties.get(playerProfile);}
		catch(ReflectiveOperationException e){e.printStackTrace(); return null;}
	}
	public static YetAnotherProfile fromPlayerProfile(Object playerProfile){
		return new YetAnotherProfile(getId(playerProfile), getName(playerProfile), getProperties(playerProfile));
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

	public GameProfile asGameProfile(){
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
			Multimap<String, Property> properties = getProperties(playerProfile);
			properties.putAll(this.properties);
			return playerProfile;
		}
		catch(ReflectiveOperationException e){
			e.printStackTrace();
		}
		return null;
	}
	private static Constructor<?> cnstr_ResolvableProfile;
	private static Method method_asResolvableProfile;
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
				method_asResolvableProfile = ReflectionUtils.getRefClass(class_ResolvableProfile)
						.findMethod(/*isStatic=*/true, class_ResolvableProfile, GameProfile.class).getRealMethod();
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
			if(method_asResolvableProfile != null) method_asResolvableProfile.invoke(null, asGameProfile());
		}
		catch(ReflectiveOperationException e){e.printStackTrace();}
		return null;
	}

	private static Method[] method_ResolvableProfile_RecordAccessors;
	private static Field field_ResolvableProfile_name, field_ResolvableProfile_id, field_ResolvableProfile_properties;
	static{
		try{
			final Class<?> class_ResolveableProfile = Class.forName("net.minecraft.world.item.component.ResolvableProfile");
			//if(class_ResolveableProfile.isInstance(profile)) printError = true;
			try{
				field_ResolvableProfile_id = class_ResolveableProfile.getDeclaredField("id");
				field_ResolvableProfile_id.setAccessible(true);
				field_ResolvableProfile_name = class_ResolveableProfile.getDeclaredField("name");
				field_ResolvableProfile_name.setAccessible(true);
				field_ResolvableProfile_properties = class_ResolveableProfile.getDeclaredField("properties");
				field_ResolvableProfile_properties.setAccessible(true);
			}
			// 1.21.4+ changed ResolveableProfile class type to a Record
			catch(NoSuchFieldException e){
				//if((boolean)Class.class.getMethod("isRecord").invoke(clazzResolveableProfile)){
				Object[] rcs = (Object[])Class.class.getMethod("getRecordComponents").invoke(class_ResolveableProfile);
				Method getAccessor = rcs[0].getClass().getMethod("getAccessor");
				method_ResolvableProfile_RecordAccessors = new Method[rcs.length];
				for(int i=0; i<3; ++i) method_ResolvableProfile_RecordAccessors[i] = (Method)getAccessor.invoke(rcs[i]);
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
			if(method_ResolvableProfile_RecordAccessors != null){
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
				Bukkit.getLogger().severe("DropHeads-YAP: Unable to convert from ResolvableProfile");
				return null;
			}
		}
		catch(ReflectiveOperationException e){
			Bukkit.getLogger().severe("DropHeads-YAP: Unable to convert from ResolvableProfile");
			e.printStackTrace();
			return null;
		}
		final String name = nameOptional.orElse("");
		final UUID uuid = uuidOptional.orElseThrow(() -> new NullPointerException("UUID missing in ResolvableProfile"));
//		GameProfile gp = new GameProfile(uuid, name);
//		getProperties(gp).putAll(properties);
//		return new YetAnotherProfile(gp);
		return new YetAnotherProfile(uuid, name, convertMapType(properties));
	}

	private static Field field_SkullMeta_profile, field_Skull_profile;
	private static boolean useGameProfileForMetas;
	static{
		try{
			field_SkullMeta_profile = SkullMeta.class.getDeclaredField("profile");
			field_Skull_profile = Skull.class.getDeclaredField("profile");
		}
		catch(ReflectiveOperationException e){
			e.printStackTrace();
		}
		field_SkullMeta_profile.setAccessible(true);
		field_Skull_profile.setAccessible(true);
		useGameProfileForMetas = field_SkullMeta_profile.getType().equals(GameProfile.class);
	}
	public void set(SkullMeta skullMeta){
		try{
			field_SkullMeta_profile.set(skullMeta, useGameProfileForMetas ? asGameProfile() : asResolvableProfile());
		}
		catch(ReflectiveOperationException e){
			e.printStackTrace();
		}
	}
	public void set(Skull skull){
		try{
			field_Skull_profile.set(skull, useGameProfileForMetas ? asGameProfile() : asResolvableProfile());
		}
		catch(ReflectiveOperationException e){
			e.printStackTrace();
		}
	}
	public static YetAnotherProfile fromSkullMeta(SkullMeta skullMeta){
		try{
			Object profile = field_SkullMeta_profile.get(skullMeta);
			return useGameProfileForMetas ? fromGameProfile((GameProfile)profile) : fromResolvableProfile(profile);
		}
		catch(ReflectiveOperationException e){
			e.printStackTrace();
			return null;
		}
	}
	public static YetAnotherProfile fromSkull(Skull skull){
		try{
			Object profile = field_Skull_profile.get(skull);
			return useGameProfileForMetas ? fromGameProfile((GameProfile)profile) : fromResolvableProfile(profile);
		}
		catch(ReflectiveOperationException e){
			e.printStackTrace();
			return null;
		}
	}
}