package net.evmodder.EvLib.util;

public class Section {
	public final int maxX, minX;
	public final int maxY, minY;
	public final int maxZ, minZ;
	public final String world;

	public Section(String world, int maxX, int minX, int maxY, int minY, int maxZ, int minZ){
		this.world = world;
		if(maxX > minX){this.maxX = maxX; this.minX = minX;}
		else{this.maxX = minX; this.minX = maxX;}
		
		if(maxY > minY){this.maxY = maxY; this.minY = minY;}
		else{this.maxY = minY; this.minY = maxY;}
		
		if(maxZ > minZ){this.maxZ = maxZ; this.minZ = minZ;}
		else{this.maxZ = minZ; this.minZ = maxZ;}
	}

	public boolean contains(String world, int x, int y, int z){
		if(this.world.equals(world) &&
			maxX >= x && minX <= x &&
			maxY >= y && minY <= y &&
			maxZ >= z && minZ <= z) return true;
		else return false;
	}
	private static Class<?> classLocation, classVec3i;
	private static java.lang.reflect.Method methodWorld_getName;
	static{
		try{
			classLocation = Class.forName("org.bukkit.Location");
			methodWorld_getName = Class.forName("org.bukkit.World").getMethod("getName");
		}
		catch(ReflectiveOperationException e){}
		try{classVec3i = Class.forName("net.minecraft.util.math.Vec3i");} catch(ClassNotFoundException e){}
	}
	public boolean contains(Object obj){
		try{
			if(classLocation != null && classLocation.isInstance(obj)){
				String world = (String)methodWorld_getName.invoke(classLocation.getMethod("getWorld").invoke(obj));
				int x = (int)classLocation.getMethod("getBlockX").invoke(obj);
				int y = (int)classLocation.getMethod("getBlockY").invoke(obj);
				int z = (int)classLocation.getMethod("getBlockZ").invoke(obj);
				return contains(world, x, y, z);
			}
			else if(classVec3i != null && classVec3i.isInstance(obj)){
				String world = null;
				int x = (int)classVec3i.getMethod("getX").invoke(obj);
				int y = (int)classVec3i.getMethod("getY").invoke(obj);
				int z = (int)classVec3i.getMethod("getZ").invoke(obj);
				return contains(world, x, y, z);
			}
			else throw new RuntimeException("Section.contains(obj) doesn't recognize class-type: "+obj.getClass().getName());
		}
		catch(ReflectiveOperationException e){
//			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString(){
		return new StringBuilder(world)
				.append(',').append(minX).append(',').append(maxX)
				.append(',').append(minY).append(',').append(maxY)
				.append(',').append(minZ).append(',').append(maxZ).toString();
	}
	
	public static Section fromString(String str){
		String[] data = str.split(",");
		if(data.length != 7) return null;
		try{
			return new Section(data[0], Integer.parseInt(data[1]), Integer.parseInt(data[2]),
										Integer.parseInt(data[3]), Integer.parseInt(data[4]),
										Integer.parseInt(data[5]), Integer.parseInt(data[6]));
		}
		catch(NumberFormatException ex){return null;}
	}
}