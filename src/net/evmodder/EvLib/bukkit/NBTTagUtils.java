package net.evmodder.EvLib.bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import net.evmodder.EvLib.util.ReflectionUtils;

/**
 * Utilities for editing NBT in Bukkit plugins.
 * @author EvModder
 * @version 1.0
 */
public final class NBTTagUtils{// version = X1.0
	//-------------------------------------------------- ReflectionUtils used by RefNBTTag: --------------------------------------------------//
	static final Class<?> classNBTTagCompound = ReflectionUtils.getClass("{nms}.NBTTagCompound", "{nm}.nbt.NBTTagCompound");
	static final Class<?> classNBTBase = ReflectionUtils.getClass("{nms}.NBTBase", "{nm}.nbt.NBTBase");

	// ItemStack
	static final Class<?> classItemStack = ReflectionUtils.getClass("{nms}.ItemStack", "{nm}.world.item.ItemStack");
	static final Class<?> classCraftItemStack = ReflectionUtils.getClass("{cb}.inventory.CraftItemStack");
	static final Method methodAsNMSCopy = ReflectionUtils.getMethod(classCraftItemStack, "asNMSCopy", ItemStack.class);
	static final Method methodAsCraftMirror = ReflectionUtils.getMethod(classCraftItemStack, "asCraftMirror", classItemStack);
//	static final Method methodGetTag = classItemStack.getMethod("getTag");
//	static final Method methodSetTag = classItemStack.getMethod("setTag", classNBTTagCompound);
//	static final Method methodGetTag = classItemStack.findMethod(/*isStatic=*/false, classNBTTagCompound);
//	static final Method methodSetTag = classItemStack.findMethod(/*isStatic=*/false, Void.TYPE, classNBTTagCompound);

//	static final Method methodGetTagItemStack, methodCopyTag, methodSetTagItemStack;
//	static final Object customDataTypeObj;

	// Entity
	static final Method methodGetHandle = ReflectionUtils.getMethod(ReflectionUtils.getClass("{cb}.entity.CraftEntity"), "getHandle");
	static final Class<?> classEntity = ReflectionUtils.getClass("{nms}.Entity", "{nm}.world.entity.Entity");
	static final Method methodGetBukkitEntity = ReflectionUtils.findMethodByName(classEntity, "getBukkitEntity");
//	static final Method methodSaveToTag = classEntity.findMethodByName("save");
//	static final Method methodLoadFromTag = classEntity.findMethodByName("load");

//	static final Method methodSetTagEntity = ReflectionUtils.findMethod(classEntity, /*isStatic=*/false, boolean.class, classNBTTagCompound);
//	static final Method methodGetTagEntity = ReflectionUtils.findMethod(classEntity, /*isStatic=*/false, Void.TYPE, classNBTTagCompound);

	// ItemStack + Entity
	static final Function</*{nm}.world.item.ItemStack*/Object, /*{nm}.nbt.NBTTagCompound*/Object> methodGetTagItemStack; 
	static final BiConsumer</*{nm}.world.item.ItemStack*/Object, /*{nm}.nbt.NBTTagCompound*/Object> methodSetTagItemStack;
	static final Function</*{nm}.world.entity.Entity*/Object,  /*{nm}.nbt.NBTTagCompound*/Object> methodGetTagEntity; 
	static final BiConsumer</*{nm}.world.entity.Entity*/Object, /*{nm}.nbt.NBTTagCompound*/Object> methodSetTagEntity;
	static{
		Function<Object, Object> methodGetTagItemStackTemp;
		Function<Object, Object> methodGetTagEntityTemp;
		BiConsumer<Object, Object> methodSetTagItemStackTemp;
		BiConsumer<Object, Object> methodSetTagEntityTemp;
		try{
			// Class of CUSTOM_DATA: DataComponentType
			Object CUSTOM_DATA = ReflectionUtils.getStatic(ReflectionUtils.getField(ReflectionUtils.getClass("{nm}.core.component.DataComponents"), "CUSTOM_DATA"));
//			Class<?> classCustomData = ReflectionUtils.getClass("{nm}.world.item.component.CustomData");
			Class<?> classDataComponentType = ReflectionUtils.getClass("{nm}.core.component.DataComponentType");
			Class<?> classDataComponentGetter = ReflectionUtils.getClass("{nm}.core.component.DataComponentGetter");
//			net.minecraft.world.item.ItemStack i; i.set(CUSTOM_DATA, /*tag*/null);
//			net.minecraft.world.entity.Entity e; e.setComponent(CUSTOM_DATA, /*tag*/null);
			Method methodGet = ReflectionUtils.getMethod(classDataComponentGetter, "get", classDataComponentType);
			methodGetTagItemStackTemp = o -> ReflectionUtils.call(methodGet, o, CUSTOM_DATA);
			methodGetTagEntityTemp = o -> ReflectionUtils.call(methodGet, o, CUSTOM_DATA);
			// Note: 2 options for ItemStack.set(tag), current method, or in CustomData:
			//public static void set(DataComponentType<CustomData> datacomponenttype, ItemStack itemstack, NBTTagCompound nbttagcompound);
			// i.e. CustomData.set(DataComponents.CUSTOM_DATA, stack, tag)
//			Method methodSet = ReflectionUtils.getMethod(classCustomData, "set", classDataComponentType, classItemStack, classNBTTagCompound);
			Method methodSetI = ReflectionUtils.findMethod(classItemStack, /*isStatic=*/false, Object.class, classDataComponentType, Object.class);
			methodSetTagItemStackTemp = (i, tag) -> ReflectionUtils.call(methodSetI, i, CUSTOM_DATA, tag);
			Method methodSetE = ReflectionUtils.findMethod(classEntity, /*isStatic=*/false, Object.class, classDataComponentType, Object.class);
			methodSetTagEntityTemp = (e, tag) -> ReflectionUtils.call(methodSetE, e, CUSTOM_DATA, tag);
//			methodCopyTagTemp = ReflectionUtils.getMethod(classCustomData, "copyTag");
		}
		catch(RuntimeException re){
			Method methodGetI = ReflectionUtils.findMethod(classItemStack, /*isStatic=*/false, classNBTTagCompound);
			methodGetTagItemStackTemp = i -> ReflectionUtils.call(methodGetI, i);
			Method methodSetI = ReflectionUtils.findMethod(classItemStack, /*isStatic=*/false, Void.TYPE, classNBTTagCompound);
			methodSetTagItemStackTemp = (i, tag) -> ReflectionUtils.call(methodSetI, i,  tag);
			Method methodGetE = ReflectionUtils.findMethod(classEntity, /*isStatic=*/false, Void.TYPE, classNBTTagCompound);
			methodGetTagEntityTemp = e -> ReflectionUtils.call(methodGetE, e);
			Method methodSetE = ReflectionUtils.findMethod(classEntity, /*isStatic=*/false, boolean.class, classNBTTagCompound);
			methodSetTagEntityTemp = (e, tag) -> ReflectionUtils.call(methodSetE, e, tag);
		}
		methodGetTagItemStack = methodGetTagItemStackTemp;
		methodSetTagItemStack = methodSetTagItemStackTemp;
		methodGetTagEntity = methodGetTagEntityTemp;
		methodSetTagEntity = methodSetTagEntityTemp;
//		methodCopyTag = methodCopyTagTemp;
	}

	// Tag-handling
//	static final Method methodTagRemove = classNBTTagCompound.getMethod("remove", String.class);
//	static final Method methodHasKey = classNBTTagCompound.getMethod("hasKey", String.class);
	static final Method methodTagRemove = ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, Void.TYPE, String.class);
	static Method methodHasKey = null, methodGetAllKeys = null;
	static{
		try{methodHasKey = ReflectionUtils.getMethod(classNBTTagCompound, "hasKey");}
		catch(RuntimeException e){
			try{methodGetAllKeys = ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, Set.class);}
			catch(RuntimeException e2){System.err.println("Unable to find getAllKeys() method");}
		}
	}
//	static final Method methodTagIsEmpty = classNBTTagCompound.getMethod("isEmpty");
	static final Method methodTagIsEmpty = ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, boolean.class);
	static final HashMap<Class<?>, Method> tagSetters = new HashMap<>();
	static final HashMap<Class<?>, Method> tagGetters = new HashMap<>();
	static{
//		tagSetters.put(realNBTBaseClass,classNBTTagCompound.getMethod("set",			String.class, classNBTBase));
//		tagSetters.put(boolean.class,	classNBTTagCompound.getMethod("setBoolean",		String.class, boolean.class));
//		tagSetters.put(byte.class,		classNBTTagCompound.getMethod("setByte",		String.class, byte.class));
//		tagSetters.put(byte[].class,	classNBTTagCompound.getMethod("setByteArray",	String.class, byte[].class));
//		tagSetters.put(double.class,	classNBTTagCompound.getMethod("setDouble",		String.class, double.class));
//		tagSetters.put(float.class,		classNBTTagCompound.getMethod("setFloat",		String.class, float.class));
//		tagSetters.put(int.class,		classNBTTagCompound.getMethod("setInt",			String.class, int.class));
//		tagSetters.put(int[].class,		classNBTTagCompound.getMethod("setIntArray",	String.class, int[].class));
//		tagSetters.put(long.class,		classNBTTagCompound.getMethod("setLong",		String.class, long.class));
//		tagSetters.put(short.class,		classNBTTagCompound.getMethod("setShort",		String.class, short.class));
//		tagSetters.put(String.class,	classNBTTagCompound.getMethod("setString",		String.class, String.class));
		tagSetters.put(classNBTBase,	ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, classNBTBase, String.class, classNBTBase));
		tagSetters.put(boolean.class,	ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, Void.TYPE, String.class, boolean.class));
		tagSetters.put(byte.class,		ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, Void.TYPE, String.class, byte.class));
		tagSetters.put(byte[].class,	ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, Void.TYPE, String.class, byte[].class));
		tagSetters.put(double.class,	ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, Void.TYPE, String.class, double.class));
		tagSetters.put(float.class,		ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, Void.TYPE, String.class, float.class));
		tagSetters.put(int.class,		ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, Void.TYPE, String.class, int.class));
		tagSetters.put(int[].class,		ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, Void.TYPE, String.class, int[].class));
		tagSetters.put(long.class,		ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, Void.TYPE, String.class, long.class));
		tagSetters.put(short.class,		ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, Void.TYPE, String.class, short.class));
		tagSetters.put(String.class,	ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, Void.TYPE, String.class, String.class));
	}
	static{
//		Class<?> classCompoundTag = ReflectionUtils.getClass("{nm}.nbt.CompoundTag", "{nm}.nbt.NBTTagCompound");
//		tagGetters.put(realNBTBaseClass,classNBTTagCompound.getMethod("get",			String.class));
//		tagGetters.put(boolean.class,	classNBTTagCompound.getMethod("getBoolean",		String.class));
//		tagGetters.put(byte.class,		classNBTTagCompound.getMethod("getByte",		String.class));
//		tagGetters.put(byte[].class,	classNBTTagCompound.getMethod("getByteArray",	String.class));
//		tagGetters.put(double.class,	classNBTTagCompound.getMethod("getDouble",		String.class));
//		tagGetters.put(float.class,		classNBTTagCompound.getMethod("getFloat",		String.class));
//		tagGetters.put(int.class,		classNBTTagCompound.getMethod("getInt",			String.class));
//		tagGetters.put(int[].class,		classNBTTagCompound.getMethod("getIntArray",	String.class));
//		tagGetters.put(long.class,		classNBTTagCompound.getMethod("getLong",		String.class));
//		tagGetters.put(short.class,		classNBTTagCompound.getMethod("getShort",		String.class));
//		tagGetters.put(String.class,	classNBTTagCompound.getMethod("getString",		String.class));
		//TODO: all the ones below appear broken! (2026-01-21) Need to figure out what signature change happened!!
		tagGetters.put(classNBTBase,	ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, classNBTBase, String.class));
		tagGetters.put(boolean.class,	ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, boolean.class, String.class));
		tagGetters.put(byte.class,		ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, byte.class, String.class));
		tagGetters.put(byte[].class,	ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, byte[].class, String.class));
		tagGetters.put(double.class,	ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, double.class, String.class));
		tagGetters.put(float.class,		ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, float.class, String.class));
		tagGetters.put(int.class,		ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, int.class, String.class));
		tagGetters.put(int[].class,		ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, int[].class, String.class));
		tagGetters.put(long.class,		ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, long.class, String.class));
		tagGetters.put(short.class,		ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, short.class, String.class));
		tagGetters.put(String.class,	ReflectionUtils.findMethod(classNBTTagCompound, /*isStatic=*/false, String.class, String.class));
	}
	static final Constructor<?> cnstr_NBTTagCompound = ReflectionUtils.findConstructor(classNBTTagCompound, 0);

	//-------------------------------------------------- ReflectionUtils used by RefNBTTagList: --------------------------------------------------//
//	static final Class<?> classNBTBase = ReflectionUtils.getRefClass("{nms}.NBTBase"); 
	static final Class<?> classNBTTagList = ReflectionUtils.getClass("{nms}.NBTTagList", "{nm}.nbt.NBTTagList");
	static final Constructor<?> cnstr_NBTTagList = ReflectionUtils.findConstructor(classNBTTagList, 0);
//	static final Class<?> realNBTBaseClass = classNBTBase.getRealClass();
	static Method methodAdd;
	static{
		try{methodAdd = ReflectionUtils.getMethod(AbstractList.class, "add", Object.class);}
		catch(RuntimeException e){methodAdd = ReflectionUtils.getMethod(classNBTTagList, "add", classNBTTagList);}// version <= 1.16
	}
	static final Method methodGet = ReflectionUtils.getMethod(classNBTTagList, "get", int.class);

	abstract static class RefNBTBase{
		public RefNBTBase(){};
		Object nmsTag;
		@Override public String toString(){return nmsTag.toString();}
	}

	// String tag
	static final Class<?> classNBTTagString = ReflectionUtils.getClass("{nms}.NBTTagString", "{nm}.nbt.NBTTagString");
	static final Constructor<?> cnstr_NBTTagString = ReflectionUtils.getConstructor(classNBTTagString, String.class);
	public static final class RefNBTTagString extends RefNBTBase{
		public RefNBTTagString(String str){nmsTag = ReflectionUtils.construct(cnstr_NBTTagString, str);}
	}

	// List tag
	public static final class RefNBTTagList extends RefNBTBase{
		public RefNBTTagList(){nmsTag = ReflectionUtils.construct(cnstr_NBTTagList);}
		//public RefNBTTagList(RefNBTTagList base){nmsTagList = base;};
		RefNBTTagList(Object nmsTagList){nmsTag = nmsTagList;}

		public void add(RefNBTBase tag){ReflectionUtils.call(methodAdd, nmsTag, tag.nmsTag);}
		public RefNBTBase get(int i){
			Object value = ReflectionUtils.call(methodGet, nmsTag, i);
			if(value == null) return null;
			if(value.getClass().equals(classNBTTagCompound)) return new RefNBTTagCompound(value);
			if(value.getClass().equals(classNBTTagList)) return new RefNBTTagList(value);
			if(value.getClass().equals(classNBTTagString)) return new RefNBTTagString(value.toString());
			return null;
		}
		//TODO: add getLength/getSize
	}

	// Compound tag
	public static final class RefNBTTagCompound extends RefNBTBase{
		Object nmsTag;
		public RefNBTTagCompound(){nmsTag = ReflectionUtils.construct(cnstr_NBTTagCompound);}
		//public RefNBTTagCompound(RefNBTTagCompound base){nmsTag = base;};
		RefNBTTagCompound(Object nmsTag){this.nmsTag = nmsTag;}
		private void addToTag(String key, Object value, Class<?> type) {ReflectionUtils.call(tagSetters.get(type), nmsTag, key, value);}
		private Object getFromTag(String key, Class<?> type) {return ReflectionUtils.call(tagGetters.get(type), nmsTag, key);}
		@Override public String toString(){return nmsTag.toString();}
	
		public void set(String key, RefNBTTagCompound value){addToTag(key, value.nmsTag, classNBTBase);}
		public void set(String key, RefNBTTagList value){addToTag(key, value.nmsTag, classNBTBase);}
		public void set(String key, RefNBTTagString value){addToTag(key, value.nmsTag, classNBTBase);}
		public void setBoolean	(String key, boolean		value){addToTag(key, value, boolean.class);}
		public void setByte		(String key, byte			value){addToTag(key, value, byte.class);}
		public void setByteArray(String key, byte[]			value){addToTag(key, value, byte[].class);}
		public void setDouble	(String key, double			value){addToTag(key, value, double.class);}
		public void setFloat	(String key, float			value){addToTag(key, value, float.class);}
		public void setInt		(String key, int			value){addToTag(key, value, int.class);}
		public void setIntArray	(String key, int[]			value){addToTag(key, value, int[].class);}
		public void setLong		(String key, long			value){addToTag(key, value, long.class);}
		public void setShort	(String key, short			value){addToTag(key, value, short.class);}
		public void setString	(String key, String			value){addToTag(key, value, String.class);}
		//
		public RefNBTBase get(String key){
			Object value = getFromTag(key, classNBTBase);
			if(value == null) return null;
			if(value.getClass().equals(classNBTTagCompound)) return new RefNBTTagCompound(value);
			if(value.getClass().equals(classNBTTagList)) return new RefNBTTagList(value);
			if(value.getClass().equals(classNBTTagString)) return new RefNBTTagString(value.toString());
			return null;
		}
		public boolean getBoolean	(String key){return (boolean)	getFromTag(key, boolean.class);}
		public byte getByte			(String key){return (byte)		getFromTag(key, byte.class);}
		public byte[] getByteArray	(String key){return (byte[])	getFromTag(key, byte[].class);}
		public double getDouble		(String key){return (double)	getFromTag(key, double.class);}
		public float getFloat		(String key){return (float)		getFromTag(key, float.class);}
		public int getInt			(String key){return (int)		getFromTag(key, int.class);}
		public int[] getIntArray	(String key){return (int[])		getFromTag(key, int[].class);}
		public long getLong			(String key){return (long)		getFromTag(key, long.class);}
		public short getShort		(String key){return (short)		getFromTag(key, short.class);}
		public String getString		(String key){return (String)	getFromTag(key, String.class);}
		//
		public void remove(String key){ReflectionUtils.call(methodTagRemove, nmsTag, key);}
		@SuppressWarnings("unchecked")
		public boolean hasKey(String key){
			if(methodHasKey == null) return ((Set<String>)ReflectionUtils.call(methodGetAllKeys, nmsTag)).contains(key);
			else return (boolean)ReflectionUtils.call(methodHasKey, nmsTag, key);
		}
	}

	// For ItemStacks ----------------------------------------------------
	public static ItemStack setTag(ItemStack item, RefNBTTagCompound tag){
		Object nmsTag = (tag == null || ReflectionUtils.call(methodTagIsEmpty, tag.nmsTag).equals(true)) ? null : tag.nmsTag;
		Object nmsItem = ReflectionUtils.callStatic(methodAsNMSCopy, item);
		methodSetTagItemStack.accept(nmsItem, nmsTag);
		return (ItemStack) ReflectionUtils.callStatic(methodAsCraftMirror, nmsItem);
	}
	public static RefNBTTagCompound getTag(ItemStack item){
		Object nmsItem = ReflectionUtils.callStatic(methodAsNMSCopy, item); // asNMSCopy() can generate a NPE in rare cases (plugin compatibility)
		Object nmsTag = methodGetTagItemStack.apply(nmsItem);
		return nmsTag == null ? new RefNBTTagCompound() : new RefNBTTagCompound(nmsTag);
	};

	// For Entities ----------------------------------------------------
	public static Entity setTag(Entity entity, RefNBTTagCompound tag){
		Object nmsTag = (tag == null || ReflectionUtils.call(methodTagIsEmpty, tag.nmsTag).equals(true)) ? null : tag.nmsTag;
		Object nmsEntity = ReflectionUtils.call(methodGetHandle, entity);
		methodSetTagEntity.accept(nmsEntity, nmsTag);
		return (Entity)ReflectionUtils.call(methodGetBukkitEntity, nmsEntity);
	}
	public static RefNBTTagCompound getTag(Entity entity){
		Object nmsEntity = ReflectionUtils.call(methodGetHandle, entity);
		Object nmsTag = methodGetTagEntity.apply(nmsEntity);
		return nmsTag == null ? new RefNBTTagCompound() : new RefNBTTagCompound(nmsTag);
	};
}