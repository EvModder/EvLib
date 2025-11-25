package net.evmodder.EvLib.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tool to help with reflection targeting Minecraft server classes.
 * @author DPOH-VAR and EvModder
 * @version 1.2
 */
public class ReflectionUtils{// version = X1.0
	/**  prefix of bukkit classes */
	private static final String preClassB;
	/** prefix of minecraft classes */
	private static final String preClassM;
	/** boolean value, TRUE if server uses forge or MCPC+ */
	private static final boolean forge;
	/** vX_XX_X server version string (e.g.: v1_13_2) */
	private static String serverVersionString;
	public static String getServerVersionString(){return serverVersionString;}

	/** check server version and class names */
	static{
		forge = (Bukkit.getVersion().contains("MCPC") || Bukkit.getVersion().contains("Forge"));
		final Server server = Bukkit.getServer();
		preClassB = server.getClass().getPackage().getName();
		//Bukkit.getLogger().warning("1: "+Bukkit.getVersion());// "git-Purpur-2233 (MC: 1.20.6)"
		//Bukkit.getLogger().warning("2: "+Bukkit.getServer().getClass().getName());// "org.bukkit.craftbukkit.v1_20_R4.CraftServer"
		//Bukkit.getLogger().warning("3: "+Bukkit.getVersion());// "1.20.6-R0.1-SNAPSHOT"

		final Class<?> bukkitServerClass = server.getClass();
		String[] pas = bukkitServerClass.getName().split("\\.");
		//if(pas.length == 5) serverVersionString = pas[3];
		Object handle;
		try{
			handle = bukkitServerClass.getDeclaredMethod("getHandle").invoke(server);
		}
		catch(NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
			throw new RuntimeException(e);
		}

		Class<?> handleServerClass = handle.getClass();
		pas = handleServerClass.getName().split("\\.");
		if(pas.length == 5 && pas[3].matches("v[0-9]+(_[0-9]+)*(_R[0-9]+)?")){
			String verM = pas[3];
			preClassM = "net.minecraft.server."+verM;
			//serverVersionString = verM;
		}
		else{
			preClassM = "net.minecraft.server";
			//if(serverVersionString == null) serverVersionString = "v" + Bukkit.getBukkitVersion().replace("-SNAPSHOT", "");
		}
		serverVersionString = "v" + Bukkit.getBukkitVersion().replace("-SNAPSHOT", "");
		serverVersionString = serverVersionString.replace("R", "").replace('.', '_').replace('-', '_');
	}

	/**
	 * @return true if server has forge classes
	 */
	public static boolean isForge(){ return forge; }

	/**
	 * get RefClass object by real class.
	 * @param clazz class
	 * @return RefClass based on passed class
	 */
	public static RefClass getRefClass(Class<?> clazz){ return new RefClass(clazz); }

	/**
	 * Get class for name.
	 * Replace {nms} to net.minecraft.server.V*.
	 * Replace {cb} to org.bukkit.craftbukkit.V*.
	 * Replace {nm} to net.minecraft
	 * @param classes possible class paths
	 * @return RefClass object
	 * @throws RuntimeException if no class found
	 */
	public static RefClass getRefClass(String... classes){
		String className = "";
		for(String rawName: classes){
			try{
				className = rawName
						.replace("{cb}", preClassB)
						.replace("{nms}", preClassM)
						.replace("{nm}", "net.minecraft");
				return getRefClass(Class.forName(className));
			}
			catch(ClassNotFoundException ignored){}
		}
		throw new RuntimeException("no class found: " + className);
	}

	/**
	 * RefClass - utility to simplify work with reflections.
	 */
	public static class RefClass{
		private final Class<?> clazz;

		/**
		 * get passed class
		 * @return class
		 */
		public Class<?> getRealClass(){ return clazz; }
		private RefClass(Class<?> clazz){ this.clazz = clazz; }

		/**
		 * see {@link Class#isInstance(Object)}
		 * @param object the object to check
		 * @return true if object is an instance of this class
		 */
		public boolean isInstance(Object object){ return clazz.isInstance(object); }

		/**
		 * get existing method by name and types
		 * @param name name
		 * @param types method parameters. can be Class or RefClass
		 * @return RefMethod object
		 * @throws RuntimeException if method not found
		 */
		public RefMethod getMethod(String name, Object... types){
			Class<?>[] classes = new Class[types.length];
			int i=0; for (Object e: types){
				if(e instanceof Class) classes[i++] = (Class<?>)e;
				else if (e instanceof RefClass) classes[i++] = ((RefClass)e).getRealClass();
				else classes[i++] = e.getClass();
			}
			try{return new RefMethod(clazz.getMethod(name, classes));}
			catch(NoSuchMethodException ignored){
				try{return new RefMethod(clazz.getDeclaredMethod(name, classes));}
				catch(NoSuchMethodException | SecurityException e){throw new RuntimeException(e);}
			}
		}

		/**
		 * get existing constructor by types
		 * @param types parameters. can be Class or RefClass
		 * @return RefMethod object
		 * @throws RuntimeException if constructor not found
		 */
		public RefConstructor getConstructor(Object... types){
			Class<?>[] classes = new Class[types.length];
			int i=0;
			for(Object e: types){
				if(e instanceof Class) classes[i] = (Class<?>)e;
				else if (e instanceof RefClass) classes[i] = ((RefClass)e).getRealClass();
				else classes[i] = e.getClass();
				++i;
			}
			try{return new RefConstructor(clazz.getConstructor(classes));}
			catch(NoSuchMethodException ignored){
				try{return new RefConstructor(clazz.getDeclaredConstructor(classes));}
				catch(NoSuchMethodException | SecurityException e){throw new RuntimeException(e);}
			}
		}

		/**
		 * find method by type parameters
		 * @param types parameters. can be Class or RefClass
		 * @return RefMethod object
		 * @throws RuntimeException if method not found
		 */
		public RefMethod findMethod(boolean isStatic, Object returnType, Object... types){
			Class<?> returnTypeClass;
			if(returnType instanceof Class) returnTypeClass = (Class<?>)returnType;
			else if (returnType instanceof RefClass) returnTypeClass = ((RefClass)returnType).getRealClass();
			else returnTypeClass = returnType.getClass();

			Class<?>[] classes = new Class[types.length];
			int i=0;
			for(Object e : types){
				if(e instanceof Class) classes[i] = (Class<?>)e;
				else if (e instanceof RefClass) classes[i] = ((RefClass)e).getRealClass();
				else classes[i] = e.getClass();
				++i;
			}
			List<Method> methods = new ArrayList<>();
			Collections.addAll(methods, clazz.getMethods());
			Collections.addAll(methods, clazz.getDeclaredMethods());
			Method deprecatedM = null;
			for(Method m : methods){
				if(Modifier.isStatic(m.getModifiers()) != isStatic) continue;
				if(!m.getReturnType().equals(returnTypeClass)) continue;
				if(!Arrays.equals(classes, m.getParameterTypes())) continue;
				if(m.getAnnotation(Deprecated.class) == null) return new RefMethod(m);
				deprecatedM = m;
			}
			if(deprecatedM != null) return new RefMethod(deprecatedM);
			throw new RuntimeException("no such method");
		}

		/**
		 * find method by name
		 * @param names possible names of method
		 * @return RefMethod object
		 * @throws RuntimeException if method not found
		 */
		public RefMethod findMethodByName(String... names){
			List<Method> methods = new ArrayList<>();
			Collections.addAll(methods, clazz.getMethods());
			Collections.addAll(methods, clazz.getDeclaredMethods());
			for(Method m: methods){
				for(String name: names){
					if(m.getName().equals(name)) return new RefMethod(m);
				}
			}
			throw new RuntimeException("no such method");
		}

		/**
		 * find method by return value
		 * @param type type of returned value
		 * @return RefMethod
		 * @throws RuntimeException if method not found
		 */
		public RefMethod findMethodByReturnType(Class<?> type){
			if(type==null) type = void.class;
			List<Method> methods = new ArrayList<>();
			Collections.addAll(methods, clazz.getMethods());
			Collections.addAll(methods, clazz.getDeclaredMethods());
			for(Method m: methods){
				if(type.equals(m.getReturnType())) return new RefMethod(m);
			}
			throw new RuntimeException("no such method");
		}

		/**
		 * find method by return value
		 * @param type type of returned value
		 * @throws RuntimeException if method not found
		 * @return RefMethod
		 */
		public RefMethod findMethodByReturnType(RefClass type){ return findMethodByReturnType(type.clazz); }

		/**
		 * find constructor by number of arguments
		 * @param number number of arguments
		 * @return RefConstructor
		 * @throws RuntimeException if constructor not found
		 */
		public RefConstructor findConstructor(int number){
			List<Constructor<?>> constructors = new ArrayList<>();
			Collections.addAll(constructors, clazz.getConstructors());
			Collections.addAll(constructors, clazz.getDeclaredConstructors());
			for(Constructor<?> m: constructors){
				if(m.getParameterTypes().length == number) return new RefConstructor(m);
			}
			throw new RuntimeException("no such constructor");
		}

		/**
		 * get field by name
		 * @param name field name
		 * @return RefField
		 * @throws RuntimeException if field not found
		 */
		public RefField getField(String name){
			try{
				return new RefField(clazz.getField(name));
			}
			catch(NoSuchFieldException ignored){
				try{return new RefField(clazz.getDeclaredField(name));}
				catch(NoSuchFieldException | SecurityException e){throw new RuntimeException(e);}
			}
		}

		/**
		 * find field by type
		 * @param type field type (Class or RefClass)
		 * @return RefField
		 * @throws RuntimeException if field not found
		 */
		public RefField findField(Object type){
			if(type==null) type = void.class;
			if(type instanceof RefClass) type = ((RefClass)type).clazz;
			for(Field f: clazz.getDeclaredFields()){
				if(type.equals(f.getType())) return new RefField(f);
			}
			for(Field f: clazz.getFields()){
				if(type.equals(f.getType())) return new RefField(f);
			}
			throw new RuntimeException("no such field");
		}

		/**
		 * find field by type
		 * @param type field type (Class or RefClass)
		 * @param isStatic restrict matching on static modifier
		 * @return RefField
		 * @throws RuntimeException if field not found
		 */
		public RefField findField(Object type, boolean isStatic, boolean isPublic){
			if(type==null) type = void.class;
			if(type instanceof RefClass) type = ((RefClass)type).clazz;
			for(Field f: clazz.getDeclaredFields()){
				if(Modifier.isStatic(f.getModifiers()) != isStatic) continue;
				if(Modifier.isPublic(f.getModifiers()) != isPublic) continue;
				if(type.equals(f.getType())) return new RefField(f);
			}
			for(Field f: clazz.getFields()){
				if(Modifier.isStatic(f.getModifiers()) != isStatic) continue;
				if(Modifier.isPublic(f.getModifiers()) != isPublic) continue;
				if(type.equals(f.getType())) return new RefField(f);
			}
			throw new RuntimeException("no such field");
		}
	}

	/**
	 * Method wrapper
	 */
	public static class RefMethod{
		private final Method method;

		/**
		 * @return passed method
		 */
		public Method getRealMethod(){ return method; }
		/**
		 * @return owner class of method
		 */
		public RefClass getRefClass(){ return new RefClass(method.getDeclaringClass()); }
		/**
		 * @return class of method return type
		 */
		public RefClass getReturnRefClass(){ return new RefClass(method.getReturnType()); }
		private RefMethod(Method method){
			this.method = method;
			method.setAccessible(true);
		}
		/**
		 * apply method to object
		 * @param e object to which the method is applied
		 * @return RefExecutor with method call(...)
		 */
		public RefExecutor of(Object e){ return new RefExecutor(e); }

		/**
		 * call static method
		 * @param params sent parameters
		 * @return return value
		 */
		public Object call(Object... params){
			try{return method.invoke(null, params);}
			catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
				throw new RuntimeException(e);
			}
		}

		public class RefExecutor{
			Object e;
			public RefExecutor(Object e){ this.e = e; }

			/**
			 * apply method for selected object
			 * @param params sent parameters
			 * @return return value
			 * @throws RuntimeException if something went wrong
			 */
			public Object call(Object... params){
				try{return method.invoke(e, params);}
				catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * Constructor wrapper
	 */
	public static class RefConstructor{
		private final Constructor<?> constructor;

		/**
		 * @return passed constructor
		 */
		public Constructor<?> getRealConstructor(){ return constructor; }

		/**
		 * @return owner class of method
		 */
		public RefClass getRefClass(){ return new RefClass(constructor.getDeclaringClass()); }
		private RefConstructor (Constructor<?> constructor){
			this.constructor = constructor;
			constructor.setAccessible(true);
		}

		/**
		 * create new instance with constructor
		 * @param params parameters for constructor
		 * @return new object
		 * @throws RuntimeException if something went wrong
		 */
		public Object create(Object... params){
			try{
				try{constructor.setAccessible(true);} catch(SecurityException e){}
				return constructor.newInstance(params);
			}
			catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
				throw new RuntimeException(e);
			}
		}
	}

	/** Field wrapper */
	public static class RefField{
		private Field field;

		/**
		 * @return passed field
		 */
		public Field getRealField(){ return field; }

		/**
		 * @return owner class of field
		 */
		public RefClass getRefClass(){ return new RefClass(field.getDeclaringClass()); }

		/**
		 * @return type of field
		 */
		public RefClass getFieldRefClass(){ return new RefClass(field.getType()); }
		private RefField(Field field){
			this.field = field;
			field.setAccessible(true);
		}

		/**
		 * apply fiend for object
		 * @param e applied object
		 * @return RefExecutor with getter and setter
		 */
		public RefExecutor of(Object e){ return new RefExecutor(e); }
		public class RefExecutor{
			Object e;
			public RefExecutor(Object e){ this.e = e; }

			/**
			 * set field value for applied object
			 * @param param value
			 */
			public void set(Object param){
				try{field.set(e,param);}
				catch(IllegalArgumentException | IllegalAccessException e){
					throw new RuntimeException(e);
				}
			}

			/**
			 * get field value for applied object
			 * @return value of field
			 */
			public Object get(){
				try{return field.get(e);}
				catch(IllegalArgumentException | IllegalAccessException e){
					throw new RuntimeException(e);
				}
			}
		}
	}
}