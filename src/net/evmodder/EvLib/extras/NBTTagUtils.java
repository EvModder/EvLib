package net.evmodder.EvLib.extras;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.Set;
import org.bukkit.inventory.ItemStack;
import net.evmodder.EvLib.extras.ReflectionUtils.RefClass;
import net.evmodder.EvLib.extras.ReflectionUtils.RefConstructor;
import net.evmodder.EvLib.extras.ReflectionUtils.RefMethod;

public final class NBTTagUtils{// version = X1.0
	//-------------------------------------------------- ReflectionUtils used by RefNBTTag: --------------------------------------------------//
	static final RefClass classItemStack = ReflectionUtils.getRefClass("{nms}.ItemStack", "{nm}.world.item.ItemStack");
	static final RefClass classNBTTagCompound = ReflectionUtils.getRefClass("{nms}.NBTTagCompound", "{nm}.nbt.NBTTagCompound");
	static final RefClass classNBTBase = ReflectionUtils.getRefClass("{nms}.NBTBase", "{nm}.nbt.NBTBase"); 
	static final RefClass classCraftItemStack = ReflectionUtils.getRefClass("{cb}.inventory.CraftItemStack");
	static final RefMethod methodAsNMSCopy = classCraftItemStack.getMethod("asNMSCopy", ItemStack.class);
	static final RefMethod methodAsCraftMirror = classCraftItemStack.getMethod("asCraftMirror", classItemStack);
//	static final RefMethod methodGetTag = classItemStack.getMethod("getTag");
//	static final RefMethod methodSetTag = classItemStack.getMethod("setTag", classNBTTagCompound);
	static final RefMethod methodGetTag = classItemStack.findMethod(/*isStatic=*/false, classNBTTagCompound);
	static final RefMethod methodSetTag = classItemStack.findMethod(/*isStatic=*/false, Void.TYPE, classNBTTagCompound);
//	static final RefMethod methodTagRemove = classNBTTagCompound.getMethod("remove", String.class);
//	static final RefMethod methodHasKey = classNBTTagCompound.getMethod("hasKey", String.class);
	static final RefMethod methodTagRemove = classNBTTagCompound.findMethod(/*isStatic=*/false, Void.TYPE, String.class);
	static RefMethod methodHasKey = null, methodGetAllKeys = null;
	static {
		try{methodHasKey = classNBTTagCompound.getMethod("hasKey");}
		catch(RuntimeException e){
			try{methodGetAllKeys = classNBTTagCompound.findMethod(/*isStatic=*/false, Set.class);}
			catch(RuntimeException e2){System.err.println("Unable to find getAllKeys() method");}
		}
	}
//	static final RefMethod methodTagIsEmpty = classNBTTagCompound.getMethod("isEmpty");
	static final RefMethod methodTagIsEmpty = classNBTTagCompound.findMethod(/*isStatic=*/false, boolean.class);
	static final HashMap<Class<?>, RefMethod> tagSetters = new HashMap<>();
	static final HashMap<Class<?>, RefMethod> tagGetters = new HashMap<>();
	static final Class<?> realNBTTagCompoundClass = classNBTTagCompound.getRealClass();
	static final Class<?> realNBTBaseClass = classNBTBase.getRealClass();
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
		tagSetters.put(realNBTBaseClass,classNBTTagCompound.findMethod(/*isStatic=*/false, classNBTBase, String.class, classNBTBase));
		tagSetters.put(boolean.class,	classNBTTagCompound.findMethod(/*isStatic=*/false, Void.TYPE, String.class, boolean.class));
		tagSetters.put(byte.class,		classNBTTagCompound.findMethod(/*isStatic=*/false, Void.TYPE, String.class, byte.class));
		tagSetters.put(byte[].class,	classNBTTagCompound.findMethod(/*isStatic=*/false, Void.TYPE, String.class, byte[].class));
		tagSetters.put(double.class,	classNBTTagCompound.findMethod(/*isStatic=*/false, Void.TYPE, String.class, double.class));
		tagSetters.put(float.class,		classNBTTagCompound.findMethod(/*isStatic=*/false, Void.TYPE, String.class, float.class));
		tagSetters.put(int.class,		classNBTTagCompound.findMethod(/*isStatic=*/false, Void.TYPE, String.class, int.class));
		tagSetters.put(int[].class,		classNBTTagCompound.findMethod(/*isStatic=*/false, Void.TYPE, String.class, int[].class));
		tagSetters.put(long.class,		classNBTTagCompound.findMethod(/*isStatic=*/false, Void.TYPE, String.class, long.class));
		tagSetters.put(short.class,		classNBTTagCompound.findMethod(/*isStatic=*/false, Void.TYPE, String.class, short.class));
		tagSetters.put(String.class,	classNBTTagCompound.findMethod(/*isStatic=*/false, Void.TYPE, String.class, String.class));
	}
	static{
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
		tagGetters.put(realNBTBaseClass,classNBTTagCompound.findMethod(/*isStatic=*/false, classNBTBase, String.class));
		tagGetters.put(boolean.class,	classNBTTagCompound.findMethod(/*isStatic=*/false, boolean.class, String.class));
		tagGetters.put(byte.class,		classNBTTagCompound.findMethod(/*isStatic=*/false, byte.class, String.class));
		tagGetters.put(byte[].class,	classNBTTagCompound.findMethod(/*isStatic=*/false, byte[].class, String.class));
		tagGetters.put(double.class,	classNBTTagCompound.findMethod(/*isStatic=*/false, double.class, String.class));
		tagGetters.put(float.class,		classNBTTagCompound.findMethod(/*isStatic=*/false, float.class, String.class));
		tagGetters.put(int.class,		classNBTTagCompound.findMethod(/*isStatic=*/false, int.class, String.class));
		tagGetters.put(int[].class,		classNBTTagCompound.findMethod(/*isStatic=*/false, int[].class, String.class));
		tagGetters.put(long.class,		classNBTTagCompound.findMethod(/*isStatic=*/false, long.class, String.class));
		tagGetters.put(short.class,		classNBTTagCompound.findMethod(/*isStatic=*/false, short.class, String.class));
		tagGetters.put(String.class,	classNBTTagCompound.findMethod(/*isStatic=*/false, String.class, String.class));
	}
	static final RefConstructor cnstrNBTTagCompound = classNBTTagCompound.findConstructor(0);

	//-------------------------------------------------- ReflectionUtils used by RefNBTTagList: --------------------------------------------------//
//	static final RefClass classNBTBase = ReflectionUtils.getRefClass("{nms}.NBTBase"); 
	static final RefClass classNBTTagList = ReflectionUtils.getRefClass("{nms}.NBTTagList", "{nm}.nbt.NBTTagList");
	static final RefConstructor cnstrNBTTagList = classNBTTagList.findConstructor(0);
//	static final Class<?> realNBTBaseClass = classNBTBase.getRealClass();
	static final Class<?> realNBTTagListClass = classNBTTagList.getRealClass();
	static final RefMethod methodAdd;
	static{
		if(ReflectionUtils.getServerVersionString().compareTo("v1_13") < 0){ // if version <= 1.16
			methodAdd = classNBTTagList.getMethod("add", realNBTBaseClass);
		}
		else{
			methodAdd = ReflectionUtils.getRefClass(AbstractList.class).getMethod("add", Object.class);
		}
	}
	static final RefMethod methodGet = classNBTTagList.getMethod("get", int.class);

	abstract static class RefNBTBase{
		public RefNBTBase(){};
		Object nmsTag;
		@Override public String toString(){return nmsTag.toString();}
	}

	// String tag
	static final RefClass classNBTTagString = ReflectionUtils.getRefClass("{nms}.NBTTagString", "{nm}.nbt.NBTTagString");
	static final RefConstructor cnstrNBTTagString = classNBTTagString.getConstructor(String.class);
	static final Class<?> realNBTTagStringClass = classNBTTagString.getRealClass();
	public static final class RefNBTTagString extends RefNBTBase{
		public RefNBTTagString(String str){nmsTag = cnstrNBTTagString.create(str);}
	}

	// List tag
	public static final class RefNBTTagList extends RefNBTBase{
		public RefNBTTagList(){nmsTag = cnstrNBTTagList.create();}
		//public RefNBTTagList(RefNBTTagList base){nmsTagList = base;};
		RefNBTTagList(Object nmsTagList){nmsTag = nmsTagList;}

		public void add(RefNBTBase tag){methodAdd.of(nmsTag).call(tag.nmsTag);}
		public RefNBTBase get(int i){
			Object value = methodGet.of(nmsTag).call(i);
			if(value == null) return null;
			if(value.getClass().equals(realNBTTagCompoundClass)) return new RefNBTTagCompound(value);
			if(value.getClass().equals(realNBTTagListClass)) return new RefNBTTagList(value);
			if(value.getClass().equals(realNBTTagStringClass)) return new RefNBTTagString(value.toString());
			return null;
		}
		//TODO: add getLength/getSize
	}

	// Compound tag
	public static final class RefNBTTagCompound extends RefNBTBase{
		Object nmsTag;
		public RefNBTTagCompound(){nmsTag = cnstrNBTTagCompound.create();}
		//public RefNBTTagCompound(RefNBTTagCompound base){nmsTag = base;};
		RefNBTTagCompound(Object nmsTag){this.nmsTag = nmsTag;}
		private void addToTag(String key, Object value, Class<?> type) {tagSetters.get(type).of(nmsTag).call(key, value);}
		private Object getFromTag(String key, Class<?> type) {return tagGetters.get(type).of(nmsTag).call(key);}
		@Override public String toString(){return nmsTag.toString();}
	
		public void set(String key, RefNBTTagCompound value){addToTag(key, value.nmsTag, realNBTBaseClass);}
		public void set(String key, RefNBTTagList value){addToTag(key, value.nmsTag, realNBTBaseClass);}
		public void set(String key, RefNBTTagString value){addToTag(key, value.nmsTag, realNBTBaseClass);}
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
			Object value = getFromTag(key, realNBTBaseClass);
			if(value == null) return null;
			if(value.getClass().equals(realNBTTagCompoundClass)) return new RefNBTTagCompound(value);
			if(value.getClass().equals(realNBTTagListClass)) return new RefNBTTagList(value);
			if(value.getClass().equals(realNBTTagStringClass)) return new RefNBTTagString(value.toString());
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
		public void remove(String key){methodTagRemove.of(nmsTag).call(key);}
		@SuppressWarnings("unchecked")
		public boolean hasKey(String key){
			if(methodHasKey == null) return ((Set<String>)methodGetAllKeys.of(nmsTag).call()).contains(key);
			else return (boolean)methodHasKey.of(nmsTag).call(key);
		}
	}

	// For ItemStacks ----------------------------------------------------
	public static ItemStack setTag(ItemStack item, RefNBTTagCompound tag){
		Object nmsTag = (tag == null || methodTagIsEmpty.of(tag.nmsTag).call().equals(true)) ? null : tag.nmsTag;
		Object nmsItem = methodAsNMSCopy.of(null).call(item);
		methodSetTag.of(nmsItem).call(nmsTag);
		item = (ItemStack) methodAsCraftMirror.of(null).call(nmsItem);
		return item;
	}
	public static RefNBTTagCompound getTag(ItemStack item){
		Object nmsItem = methodAsNMSCopy.of(null).call(item);
		Object nmsTag = methodGetTag.of(nmsItem).call();
		return nmsTag == null ? new RefNBTTagCompound() : new RefNBTTagCompound(nmsTag);
	};
}