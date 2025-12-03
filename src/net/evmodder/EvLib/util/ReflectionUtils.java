package net.evmodder.EvLib.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Tool to help with reflection targeting Minecraft server classes.
 * @author DPOH-VAR and EvModder
 * @version 2.0
 */
public class ReflectionUtils{// version = X1.0
	private static final String pkgCraftBukkit; // org.bukkit.craftbukkit.vX_XX_RX
	private static final String pkgMinecraft; // net.minecraft.server.vX_XX_RX, or just 'net.minecraft'
	private static final String serverVersionString; // v1_13_2
	public static final boolean isForge; // TRUE if server uses Force or MCPC+
//	public static String getServerVersionString(){return serverVersionString;}
	public static boolean isAtLeastVersion(String version){
		String[] partsA = version.split("_"), partsB = serverVersionString.split("_");
		for(int i=0; i<partsA.length; ++i){
			if(i == partsB.length) return true;
			try{
				int a = Integer.parseInt(partsA[i]), b = Integer.parseInt(partsB[i]);
				if(a > b) return true;
				if(a < b) return false;
			}
			catch(NumberFormatException e){
				e.printStackTrace();
				return false;
			}
		}
		return partsA.length >= partsB.length;
	}

	static{
		boolean tempIsForge = false;
		String tempPkgCraftBukkit = "org.bukkit.craftbukkit";
		String tempPkgMinecraft = "net.minecraft.server";
		String bukkitVersion = "";
		try{
			Class<?> classBukkit = Class.forName("org.bukkit.Bukkit");
			String version = (String)classBukkit.getMethod("getVersion").invoke(null);
			tempIsForge = version.contains("MCPC") || version.contains("Forge");
			final Object bukkitServer = classBukkit.getMethod("getServer").invoke(null);
			tempPkgCraftBukkit = bukkitServer.getClass().getPackage().getName();
			
			final Object nmsHandle = bukkitServer.getClass().getDeclaredMethod("getHandle").invoke(bukkitServer);
			String[] pas = nmsHandle.getClass().getName().split("\\.");
			if(pas.length == 5 && pas[3].matches("v[0-9]+(_[0-9]+)*(_R[0-9]+)?")){
				String verM = pas[3];
				tempPkgMinecraft = "net.minecraft.server."+verM;
				//serverVersionString = verM;
			}

			bukkitVersion = (String)classBukkit.getMethod("getBukkitVersion").invoke(null);

			//Bukkit.getLogger().warning("1: "+Bukkit.getVersion());// "git-Purpur-2233 (MC: 1.20.6)"
			//Bukkit.getLogger().warning("2: "+Bukkit.getServer().getClass().getName());// "org.bukkit.craftbukkit.v1_20_R4.CraftServer"
			//Bukkit.getLogger().warning("3: "+Bukkit.getVersion());// "1.20.6-R0.1-SNAPSHOT"
			
			
		}
		catch(ReflectiveOperationException e){
			//TODO: Also load version info from Fabric, Sponge, singleplayer environment, etc.
			e.printStackTrace();
		}
		isForge = tempIsForge;
		pkgCraftBukkit = tempPkgCraftBukkit;
		pkgMinecraft = tempPkgMinecraft;
		serverVersionString = "v" + bukkitVersion.replace("-SNAPSHOT", "").replace("R", "").replace('.', '_').replace('-', '_');
	}


	//====================================================================================================//
	// DIRTY - basically just calling reflection methods with exception catchers
	/**
	 * Get class by name with exception catcher.
	 * Replace {nms} to net.minecraft.server.XX
	 * Replace {cb} to org.bukkit.craftbukkit.XX
	 * Replace {nm} to net.minecraft
	 * @param classes possible class paths
	 * @return Class object
	 * @throws RuntimeException if no class found
	 */
	public static Class<?> getClass(String... classes){
		for(String name : classes){
			try{
				return Class.forName(name
						.replace("{cb}", pkgCraftBukkit)
						.replace("{nms}", pkgMinecraft)
						.replace("{nm}", "net.minecraft"));
			}
			catch(ClassNotFoundException ignored){}
		}
		throw new RuntimeException("no class found: " + String.join(",", classes));
	}

	/**
	 * Call invoke() with exception catcher.
	 * @param name name
	 * @param args invocation arguments
	 * @return object Object
	 */
	public static Object callStatic(Method method, Object... args){
		try{return method.invoke(null, args);}
		catch(IllegalAccessException | InvocationTargetException e){throw new RuntimeException(e);}
	}

	/**
	 * Call obj.invoke() with exception catcher.
	 * @param name name
	 * @param obj Object
	 * @param args invocation arguments
	 * @return object Object
	 */
	public static Object call(Method method, Object obj, Object... args){
		try{return method.invoke(obj, args);}
		catch(IllegalAccessException | InvocationTargetException e){throw new RuntimeException(e);}
	}

	/**
	 * Get value of a static field with exception catcher.
	 * @param field field
	 * @return object Object
	 * @throws RuntimeException if field not found
	 */
	public static Object getStatic(Field field){
		try{return field.get(null);}
		catch(IllegalArgumentException | IllegalAccessException e){throw new RuntimeException(e);}
	}

	/**
	 * Get value of a member field with exception catcher.
	 * @param field field
	 * @param object parent object
	 * @return object Object
	 * @throws RuntimeException if field not found
	 */
	public static Object get(Field field, Object holder){
		try{return field.get(holder);}
		catch(IllegalArgumentException | IllegalAccessException e){throw new RuntimeException(e);}
	}
	/**
	 * Set value of a member field with exception catcher.
	 * @param field field
	 * @param object parent object
	 * @param object member object value to assign
	 * @throws RuntimeException if field not found
	 */
	public static void set(Field field, Object holder, Object value){
		try{field.set(holder, value);}
		catch(IllegalArgumentException | IllegalAccessException e){throw new RuntimeException(e);}
	}
	//public static void setStatic(Field field, Object value);
	

	/**
	 * Construct an object with exception catcher.
	 * @param field field
	 * @param object parent object
	 * @return object Object
	 * @throws RuntimeException if field not found
	 */
	public static Object construct(Constructor<?> cnstr, Object... args){
		try{return cnstr.newInstance(args);}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){throw new RuntimeException(e);}
	}
	// Powerful shorthand (perhaps TOO powerful):
	/*public static Object construct(Class<?> clazz, Object... args){
		try{return getConstructor(clazz, args).newInstance(args);}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){throw new RuntimeException(e);}
	}*/

	/**
	 * Get field by name with exception catcher.
	 * @param name name
	 * @return Field object
	 * @throws RuntimeException if field not found
	 */
	public static Field getField(Class<?> clazz, String name){
		try{return clazz.getField(name);}
		catch(NoSuchFieldException | SecurityException e){throw new RuntimeException(e);}
	}

	/**
	 * Get method by name and types (or objects) with exception catcher.
	 * @param name name
	 * @param types method parameters
	 * @return Method object
	 * @throws RuntimeException if method not found
	 */
	public static Method getMethod(Class<?> clazz, String name, Object... types){
		Class<?>[] classes = new Class[types.length];
		for(int i=0; i<types.length; ++i) classes[i] = types[i] instanceof Class c ? c : types[i].getClass();
		try{return clazz.getMethod(name, classes);}
		catch(NoSuchMethodException ignored){
			try{return clazz.getDeclaredMethod(name, classes);}
			catch(NoSuchMethodException | SecurityException e){throw new RuntimeException(e);}
		}
	}

	/**
	 * Get constructor by types (or objects) with exception catcher.
	 * @param types parameters
	 * @return Method object
	 * @throws RuntimeException if constructor not found
	 */
	public static Constructor<?> getConstructor(Class<?> clazz, Object... types){
		Class<?>[] classes = new Class[types.length];
		for(int i=0; i<types.length; ++i) classes[i] = types[i] instanceof Class c ? c : types[i].getClass();
		try{
			Constructor<?> c = clazz.getConstructor(classes);
			c.setAccessible(true);
			return c;
		}
		catch(NoSuchMethodException ignored){
			try{return clazz.getDeclaredConstructor(classes);}
			catch(NoSuchMethodException | SecurityException e){throw new RuntimeException(e);}
		}
	}
	//====================================================================================================//

	/**
	 * Find method by modifiers and parameters types (or objects) with exception catcher.
	 * @param isStatic only currently supported modifier
	 * @param types parameters
	 * @return Method object
	 * @throws RuntimeException if method not found
	 */
	public static Method findMethod(Class<?> clazz, boolean isStatic, Object returnType, Object... types){
		Class<?> returnTypeClass = returnType instanceof Class c ? c : returnType.getClass();

		Class<?>[] classes = new Class[types.length];
		for(int i=0; i<types.length; ++i) classes[i] = types[i] instanceof Class c ? c : types[i].getClass();
		List<Method> methods = new ArrayList<>();
		Collections.addAll(methods, clazz.getMethods());
		Collections.addAll(methods, clazz.getDeclaredMethods());
		Method deprecatedM = null;
		for(Method m : methods){
			if(Modifier.isStatic(m.getModifiers()) != isStatic) continue;
			if(!m.getReturnType().equals(returnTypeClass)) continue;
			if(!Arrays.equals(classes, m.getParameterTypes())) continue;
			if(m.getAnnotation(Deprecated.class) == null) return m;
			if(deprecatedM == null) deprecatedM = m;
		}
		if(deprecatedM != null) return deprecatedM;
		throw new RuntimeException("no such method");
	}

	/**
	 * Find method by name with exception catcher.
	 * @param names possible names of method
	 * @return Method object
	 * @throws RuntimeException if method not found
	 */
	public static Method findMethodByName(Class<?> clazz, String... names){
		HashSet<String> nameCheck = new HashSet<>(Arrays.asList(names));
		for(Method m : clazz.getMethods()) if(nameCheck.contains(m.getName())) return m;
		for(Method m : clazz.getDeclaredMethods()) if(nameCheck.contains(m.getName())) return m;
		throw new RuntimeException("no such method");
	}

	/**
	 * Find method by return value with exception catcher.
	 * @param type type of returned value
	 * @return Method
	 * @throws RuntimeException if method not found
	 */
	public static Method findMethodByReturnType(Class<?> clazz, Class<?> type){
		if(type == null) type = void.class;
		for(Method m : clazz.getMethods()) if(type.equals(m.getReturnType())) return m;
		for(Method m : clazz.getDeclaredMethods()) if(type.equals(m.getReturnType())) return m;
		throw new RuntimeException("no such method");
	}

	/**
	 * Find constructor by number of arguments with exception catcher.
	 * @param number number of arguments
	 * @return Constructor
	 * @throws RuntimeException if constructor not found
	 */
	public static Constructor<?> findConstructor(Class<?> clazz, int number){
		for(Constructor<?> c : clazz.getConstructors()) if(c.getParameterTypes().length == number) return c;
		for(Constructor<?> c : clazz.getDeclaredConstructors()) if(c.getParameterTypes().length == number) return c;
		throw new RuntimeException("no such constructor");
	}

	/**
	 * Find field by type with exception catcher.
	 * @param type field type
	 * @return Field
	 * @throws RuntimeException if field not found
	 */
	public static Field findField(Class<?> clazz, Object type){
		if(type == null) type = void.class;
		for(Field f : clazz.getDeclaredFields()) if(type.equals(f.getType())) return f;
		for(Field f : clazz.getFields()) if(type.equals(f.getType())) return f;
		throw new RuntimeException("no such field");
	}

	/**
	 * Find field by type and modifiers with exception catcher.
	 * @param type field type
	 * @param isStatic static modifier
	 * @param isPublic public modifier
	 * @return Field
	 * @throws RuntimeException if field not found
	 */
	public static Field findField(Class<?> clazz, Object type, boolean isStatic, boolean isPublic){
		if(type == null) type = void.class;
		for(Field f : clazz.getDeclaredFields()){
			if(Modifier.isStatic(f.getModifiers()) != isStatic) continue;
			if(Modifier.isPublic(f.getModifiers()) != isPublic) continue;
			if(type.equals(f.getType())) return f;
		}
		for(Field f : clazz.getFields()){
			if(Modifier.isStatic(f.getModifiers()) != isStatic) continue;
			if(Modifier.isPublic(f.getModifiers()) != isPublic) continue;
			if(type.equals(f.getType())) return f;
		}
		throw new RuntimeException("no such field");
	}
}