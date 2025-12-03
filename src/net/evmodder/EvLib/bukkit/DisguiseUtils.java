package net.evmodder.EvLib.bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import net.evmodder.EvLib.util.ReflectionUtils;

public class DisguiseUtils{
	private DisguiseType disguise;
	private UUID player;
	private Class<?> entity;
	private Class<?> lastClassEntity;
	private Object thisObject;

	private Class<?> getEntity(String entity, UUID p) {
		Class<?> classEntity = ReflectionUtils.getClass("{nms}." + entity);

		Constructor<?> entConstructor = ReflectionUtils.getConstructor(classEntity, ReflectionUtils.getClass("{nms}.World"));

		Class<?> classCraftWorld = ReflectionUtils.getClass("{cb}.CraftWorld");
		Method methodGetHandle = ReflectionUtils.getMethod(classCraftWorld, "getHandle");

		org.bukkit.World world = Bukkit.getServer().getPlayer(p).getWorld();
		Object handle = ReflectionUtils.call(methodGetHandle, world);

		Object fin = ReflectionUtils.construct(entConstructor, handle);

		this.thisObject = fin;
		this.lastClassEntity = fin.getClass();
		return lastClassEntity;
	}

	public DisguiseUtils(DisguiseType d, UUID p) {
		disguise = d;
		player = p;
		Location location = Bukkit.getServer().getPlayer(p).getLocation();
		switch(disguise) {
		case ZOMBIE:
			entity = getEntity("EntityZombie", p);
			break;
		case WITHER_SKELETON:
			entity = getEntity("EntitySkeleton", p);
			ReflectionUtils.call(ReflectionUtils.findMethodByName(entity, "setSkeletonType"), 1);
			break;
		case SKELETON:
			entity = getEntity("EntitySkeleton", p);
			break;
		case ZOMBIEPIG:
			entity = getEntity("EntityPigZombie", p);
			break;
		case BLAZE:
			entity = getEntity("EntityBlaze", p);
			break;
		case ENDERMAN:
			entity = getEntity("EntityEnderman", p);
			break;
		case CREEPER:
			entity = getEntity("EntityCreeper", p);
			break;
		case SPIDER:
			entity = getEntity("EntitySpider", p);
			break;
		case WITCH:
			entity = getEntity("EntityWitch", p);
			break;
		case WITHER_BOSS:
			entity = getEntity("EntityWither", p);
			break;
		case GHAST:
			entity = getEntity("EntityGhast", p);
			break;
		case GIANT:
			entity = getEntity("EntityGiant", p);
			break;
		}
		if(d != null) {
			Method m = ReflectionUtils.getMethod(entity, "setPosition", double.class, double.class, double.class);
			Method mm = ReflectionUtils.getMethod(entity, "d", int.class);
			Method mmm = ReflectionUtils.getMethod(entity, "setCustomName", String.class);
			Method mmmm = ReflectionUtils.getMethod(entity, "setCustomNameVisible", boolean.class);

			ReflectionUtils.call(m, thisObject, location.getX(), location.getY(), location.getZ());
			ReflectionUtils.call(mm, thisObject, Bukkit.getServer().getPlayer(p).getEntityId());
			ReflectionUtils.call(mmm, thisObject, ChatColor.YELLOW + Bukkit.getServer().getPlayer(p).getName());
			ReflectionUtils.call(mmmm, thisObject, true);

			ReflectionUtils.set(ReflectionUtils.getField(entity, "locX"), thisObject, location.getX());
			ReflectionUtils.set(ReflectionUtils.getField(entity, "locY"), thisObject, location.getY());
			ReflectionUtils.set(ReflectionUtils.getField(entity, "locZ"), thisObject, location.getZ());
			ReflectionUtils.set(ReflectionUtils.getField(entity, "yaw"), thisObject, location.getYaw());
			ReflectionUtils.set(ReflectionUtils.getField(entity, "pitch"), thisObject, location.getPitch());
		}
	}

	public void removeDisguise() {
		this.disguise = null;

		Class<?> p29 = ReflectionUtils.getClass("{nms}.PacketPlayOutEntityDestroy");
		Class<?> p20 = ReflectionUtils.getClass("{nms}.PacketPlayOutNamedEntitySpawn");

		Class<?> classEntityPlayer = ReflectionUtils.getClass("{nms}.EntityPlayer");
		Constructor<?> pp20 = ReflectionUtils.getConstructor(p20, classEntityPlayer);
		Constructor<?> pp29 = ReflectionUtils.getConstructor(p29, int[].class);

		final int[] entityId = new int[]{Bukkit.getPlayer(player).getEntityId()};

		Object packetEntityDestroy = ReflectionUtils.construct(pp29, entityId);

		Class<?> classCraftPlayer = ReflectionUtils.getClass("{cb}.entity.CraftPlayer");
		Method methodGetHandle = ReflectionUtils.getMethod(classCraftPlayer, "getHandle");
		Object handle = ReflectionUtils.call(methodGetHandle, Bukkit.getPlayer(player));
		Object packetNamedEntitySpawn = ReflectionUtils.construct(pp20, handle);

		Field fieldPlayerConnection = ReflectionUtils.getField(classEntityPlayer, "playerConnection");
		Class<?> classPlayerConnection = ReflectionUtils.getClass("{nms}.PlayerConnection");
		Method methodSendPacket = ReflectionUtils.findMethodByName(classPlayerConnection, "sendPacket");

		for(Player player : Bukkit.getOnlinePlayers()){
			if(player != Bukkit.getPlayer(this.player)) {
				Object handle2 = ReflectionUtils.call(methodGetHandle, player);
				Object connection = ReflectionUtils.get(fieldPlayerConnection, handle2);

				ReflectionUtils.call(methodSendPacket, connection, packetEntityDestroy);
				ReflectionUtils.call(methodSendPacket, connection, packetNamedEntitySpawn);
			}
		}
	}

	public void changeDisguise(DisguiseType d) {
		removeDisguise();
		this.disguise = d;
		DisguiseUtils dis = new DisguiseUtils(d, player);
		dis.disguiseToAll();
	}

	public void disguiseToAll() {
		Class<?> p29 = ReflectionUtils.getClass("{nms}.PacketPlayOutEntityDestroy");
		Class<?> p20 = ReflectionUtils.getClass("{nms}.PacketPlayOutSpawnEntityLiving");

		Constructor<?> pp20 = ReflectionUtils.getConstructor(p20, ReflectionUtils.getClass("{nms}.EntityLiving"));
		Constructor<?> pp29 = ReflectionUtils.getConstructor(p29, int[].class);

		int[] entityId;

		entityId = new int[1];

		entityId[0] = Bukkit.getPlayer(player).getEntityId();

		Object packetEntityDestroy = ReflectionUtils.construct(pp29, entityId);
		Object packetNamedEntitySpawn = ReflectionUtils.construct(pp20, thisObject);

		Class<?> classCraftPlayer = ReflectionUtils.getClass("{cb}.entity.CraftPlayer");
		Method methodGetHandle = ReflectionUtils.getMethod(classCraftPlayer, "getHandle");
		Class<?> classEntityPlayer = ReflectionUtils.getClass("{nms}.EntityPlayer");
		Field fieldPlayerConnection = ReflectionUtils.getField(classEntityPlayer, "playerConnection");
		Class<?> classPlayerConnection = ReflectionUtils.getClass("{nms}.PlayerConnection");
		Method methodSendPacket = ReflectionUtils.findMethodByName(classPlayerConnection, "sendPacket");

		for (Player all : Bukkit.getOnlinePlayers()) {
			if(all != Bukkit.getPlayer(player)) {
				Object handle = ReflectionUtils.call(methodGetHandle, player);
				Object connection = ReflectionUtils.get(fieldPlayerConnection, handle);

				ReflectionUtils.call(methodSendPacket, connection, packetEntityDestroy);
				ReflectionUtils.call(methodSendPacket, connection, packetNamedEntitySpawn);
			}
		}
	}

	public static enum DisguiseType {
		ZOMBIE(Type.BIPED),
		WITHER_SKELETON(Type.BIPED),
		SKELETON(Type.BIPED),
		ZOMBIEPIG(Type.BIPED),
		BLAZE(Type.MOB),
		ENDERMAN(Type.MOB),
		CREEPER(Type.MOB),
		SPIDER(Type.MOB),
		WITCH(Type.MOB),
		WITHER_BOSS(Type.MOB),
		GHAST(Type.MOB),
		GIANT(Type.MOB);

		private Type type;

		DisguiseType(Type type) {
			this.type = type;
		}

		public Type getType() {
			return type;
		}

		public boolean isBiped() {
			if(type == Type.BIPED) {
				return true;
			}
			return false;
		}

		public static enum Type {
			BIPED, MOB;
		}
	}
}