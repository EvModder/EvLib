package net.evmodder.EvLib.extras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class SelectorUtils{
	enum SelectorArgumentType{
		X/*(<Int,Int>)*/, Y, Z, DISTANCE, DX, DY, DZ, WORLD,// TODO: SELECTOR 'world' DOES NOT EXIST IN VANILLA! Should file a bug...
		SCORES, TAG, TEAM, LIMIT, SORT, LEVEL, GAMEMODE/*(<adventure,creative,survival,spectator>)*/,
		NAME, X_ROTATION, Y_ROTATION, TYPE, NBT, ADVANCEMENTS, PREDICATE;
		@Override public String toString(){return name().toLowerCase();}
	}
	enum SortType{NEAREST, FURTHEST, RANDOM, ARBITRARY};

	public final static class SelectorArgument{
		final SelectorArgumentType type;
		final String value;
		public SelectorArgument(SelectorArgumentType argumentType, String value){
			this.type = argumentType;
			this.value = value;
		}
		public SelectorArgument(SelectorArgumentType argumentType, String value, boolean validate){
			if(!validateSelectorArgument(argumentType, value)) throw new IllegalArgumentException(value+" is not a valid value for "+argumentType);
			this.type = argumentType;
			this.value = value;
		}
		@Override public String toString(){return new StringBuilder(type.toString()).append("=").append(value).toString();}
	}

	public enum SelectorType{
		ALL_PLAYERS("@a"), ALL_ENTITIES("@e"), NEAREST_PLAYER("@p"), RANDOM_PLAYER("@r"), YOURSELF("@s"/* '*' also works */), UUID("");
		final String toString;
		SelectorType(String toString){this.toString = toString;}
		@Override public String toString(){return toString;}
		public static SelectorType fromString(String str){ // Case sensitive!
			for(SelectorType type : SelectorType.values()) if(type.name().equals(str) || type.toString.equals(str)) return type;
			throw new IllegalArgumentException("Unable to parse SelectorType from string: "+str);
		}
	}
	public final static class Selector{
		final SelectorType type;
		final List<SelectorArgument> arguments;//can be null
//		final UUID uuid;
		final CommandSender executer;
		final Location origin;//can be null

		private Location getOrigin(CommandSender sender){
			return
					executer instanceof Entity ? ((Entity)executer).getLocation() :
					executer instanceof BlockCommandSender ? ((BlockCommandSender)executer).getBlock().getLocation() :
					executer instanceof ProxiedCommandSender ? getOrigin(((ProxiedCommandSender)executer).getCallee()) :
//					executer instanceof ConsoleCommandSender ? xxx :
//					executer instanceof RemoteConsoleCommandSender ? xxx :
					new Location(Bukkit.getWorlds().get(0), 0, 0, 0);// No joke, this is actually what it does (checked 2020-04-22)
//					null;
		}
		public Selector(SelectorType type, CommandSender executer, SelectorArgument...arguments){
			if(type == SelectorType.UUID) throw new IllegalArgumentException("Please provide just the UUID of the entity to select");
			this.type = type;
			this.arguments = Arrays.asList(arguments);
			this.executer = executer;
			this.origin = getOrigin(executer);
//			this.uuid = null;
		}
		public Selector(UUID uuid){
			this.type = SelectorType.UUID;
//			this.uuid = uuid;
			this.executer = Bukkit.getEntity(uuid);
			this.origin = null;
			this.arguments = null;
		}

		public void addArgument(SelectorArgument argument){arguments.add(argument);}

		public Collection<Entity> resolve(){
			final ArrayList<Entity> entities = new ArrayList<>();
			Collection<? extends Player> onlinePlayers = Bukkit.getServer().getOnlinePlayers();
			switch(type){
				case ALL_ENTITIES:
					entities.addAll(origin.getWorld().getEntities());
					break;
				case ALL_PLAYERS:
					entities.addAll(onlinePlayers);
					break;
				case NEAREST_PLAYER:
					if(!origin.getWorld().getPlayers().isEmpty())
						entities.add(Collections.min(origin.getWorld().getPlayers(),
								Comparator.comparingDouble(p -> p.getLocation().distanceSquared(origin))));
					break;
				case RANDOM_PLAYER:
					entities.add(onlinePlayers.stream().skip((int)(onlinePlayers.size()*Math.random())).findFirst().get());
					break;
				case UUID:
					// Return immediately since UUID selector ignores arguments
					return Arrays.asList((Entity)executer/*Bukkit.getEntity(uuid)*/);
				case YOURSELF:
					entities.add((Entity)executer);
					break;
				default:
					throw new UnsupportedOperationException("Unknown selector '"+type+"', please update EvLib");
			}
			Double x, y, z; x = y = z = null;
			World world; world = null;
			for(SelectorArgument argument : arguments){
				switch(argument.type){
					case X: x = Double.parseDouble(argument.value); break;
					case Y: y = Double.parseDouble(argument.value); break;
					case Z: z = Double.parseDouble(argument.value); break;
					case WORLD: world = Bukkit.getWorld(argument.value); break;
					default:
				}
			}
			if(x != null) origin.setX(x);
			if(y != null) origin.setY(y);
			if(z != null) origin.setZ(z);
			if(world != null) origin.setWorld(world);

			SortType sort = SortType.ARBITRARY;
			int limit = 0;
			boolean hasNameEquals = false, hasNameNotEquals = false;
			boolean hasTypeEquals = false, hasTypeNotEquals = false;
			for(SelectorArgument argument : arguments){
				switch(argument.type){
					case LIMIT:
						limit = Integer.parseInt(argument.value);
						if(limit < 1) throw new IllegalArgumentException("Selector 'limit' argument can not be less than 1");
						break;
					case SORT:
						sort = SortType.valueOf(argument.value.toUpperCase());
						if(!argument.value.equals(argument.value.toLowerCase())) throw new IllegalArgumentException("Sort argument should be lower case");
						break;
					case DX:
						// DX, DY, DZ are exclusive (bounding box must intersect, not just touch the defined volume)
						double dx = Double.parseDouble(argument.value);
						double minX = Math.min(origin.getX(), origin.getX() + dx), maxX = Math.max(origin.getX(), origin.getX() + dx);
						entities.removeIf(e -> e.getBoundingBox().getMinX() <= minX || e.getBoundingBox().getMaxX() >= maxX);
						break;
					case DY:
						double dy = Double.parseDouble(argument.value);
						double minY = Math.min(origin.getY(), origin.getY() + dy), maxY = Math.max(origin.getY(), origin.getY() + dy);
						entities.removeIf(e -> e.getBoundingBox().getMinY() <= minY || e.getBoundingBox().getMaxY() >= maxY);
						break;
					case DZ:
						double dz = Double.parseDouble(argument.value);
						double minZ = Math.min(origin.getZ(), origin.getZ() + dz), maxZ = Math.max(origin.getZ(), origin.getZ() + dz);
						entities.removeIf(e -> e.getBoundingBox().getMinZ() <= minZ || e.getBoundingBox().getMaxZ() >= maxZ);
						break;
					case DISTANCE:
						double minDist = getRangeMin(argument.value, 0);
						double maxDist = getRangeMax(argument.value, Double.MAX_VALUE);
						double minDistSq = minDist * minDist;
						double maxDistSq = maxDist * maxDist;
						entities.removeIf(e -> {
							double distSq = e.getLocation().distanceSquared(origin);
							return distSq < minDistSq || distSq > maxDistSq;
						});
						break;
					case LEVEL:
						double minLevel = getRangeMin(argument.value, 0);
						double maxLevel = getRangeMax(argument.value, Double.MAX_VALUE);
						entities.removeIf(e -> !(e instanceof Player) || ((Player)e).getLevel() < minLevel || ((Player)e).getLevel() > maxLevel);
						break;
					case X_ROTATION:
						// -90==up, 0==forward, +90==down
						double minRotX = getRangeMin(argument.value, -90);
						double maxRotX = getRangeMax(argument.value, 90);
						entities.removeIf(e -> e.getLocation().getPitch() < minRotX || e.getLocation().getPitch() > maxRotX);
						break;
					case Y_ROTATION:
						// -180==+180==north, -90=east, 0=south, +90=west
						double minRotY = getRangeMin(argument.value, -180);
						double maxRotY = getRangeMax(argument.value, +180);
						entities.removeIf(e -> e.getLocation().getYaw() < minRotY || e.getLocation().getYaw() > maxRotY);
						break;
					case GAMEMODE:
						boolean not = argument.value.startsWith("!");
						GameMode gm = GameMode.valueOf((not ? argument.value.substring(1) : argument.value).toUpperCase());
						entities.removeIf(e -> !(e instanceof HumanEntity) || not == (((HumanEntity)e).getGameMode() == gm));
						break;
					case TEAM:
						// "team=" == teamless, "team=!" == has a team
						String teamName = (not = argument.value.startsWith("!")) ? argument.value.substring(1) : argument.value;
						Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
						// Note: TECHNICALLY vanilla doesn't currently support matching non-players for team argument.. (but this will likely be fixed)
						if(teamName.isEmpty()){
							entities.removeIf(e -> not == (e.getCustomName() != null && scoreboard.getEntryTeam(e.getCustomName()) != null));
						}
						else{
							Team team = scoreboard.getTeam(teamName);
							entities.removeIf(e -> not == (e.getCustomName() != null && team.hasEntry(e.getCustomName())));
						}
						break;
					case TAG:
						// "tag=" == doesn't have any tags, "tag=!" == has at least one tag
						String tagName = (not = argument.value.startsWith("!")) ? argument.value.substring(1) : argument.value;
						if(tagName.isEmpty()) entities.removeIf(e -> not == e.getScoreboardTags().isEmpty());
						else entities.removeIf(e -> not == e.getScoreboardTags().contains(tagName));
						break;
					case NAME:
						// Minecraft does not currently (checked=2020-04-22) support "name=" or "name=!"
						String name = (not = argument.value.startsWith("!")) ? argument.value.substring(1) : argument.value;
						// Can only have one name=x, but can have multiple name=!x (causes error if run in vanilla)
						if(not) hasNameNotEquals = true;
						else{
							if(hasNameEquals || hasNameNotEquals) throw new IllegalArgumentException("Cannot reuse argument 'name' equals");
							hasNameEquals = true;
						}
						final String rawName = name.startsWith("\"") ? TextUtils.unescapeString(name.substring(1, name.length()-1)) : name;
						entities.removeIf(e -> not == (e.getCustomName() != null && e.getCustomName().equals(rawName)));
						break;
					case TYPE:// @e[type=!chicken,type=!cow], @e[type=#skeletons] <-- selects all skeleton
						String typeName = (not = argument.value.startsWith("!")) ? argument.value.substring(1) : argument.value;
						// Can only have one type=x, but can have multiple type=!x (causes error if run in vanilla)
						if(not) hasTypeNotEquals = true;
						else{
							if(hasTypeEquals || hasTypeNotEquals) throw new IllegalArgumentException("Cannot reuse argument 'type' equals");
							hasTypeEquals = true;
						}
						if(typeName.equals("#skeletons")){// Selects all skeletons, wither skeletons, and strays
							// TODO: This doesn't currently include skeleton horses (checked=2020-04-22) in vanilla
							entities.removeIf(e -> {
								switch(e.getType()){
									case SKELETON: case WITHER_SKELETON: case STRAY: return not;
									default: return !not;
								}
							});
						}
						// TODO: add #arrows, #axolotl_always_hostiles, #axolotl_hunt_targets, #beehive_inhabitors, #freeze_hurts_extra_types,
						// #freeze_immune_entity_types, #impact_projectiles, #powder_snow_walkable_mobs, #raiders
						EntityType type = EntityType.valueOf(typeName.toUpperCase());
						entities.removeIf(e -> not == (e.getType() == type));
						break;
					case SCORES: {
						// scores={health=15..20,deaths=3..}
						String scoresStr = argument.value.substring(1, argument.value.length()-1);
						if(scoresStr.isEmpty()) break; // Yes, vanilla minecraft considers "scores={}" valid, and doesn't filter anything.
						String[] scores = scoresStr.split(",");
						Objective[] objectives = new Objective[scores.length];
						int[] scoreMinValues = new int[scores.length];
						int[] scoreMaxValues = new int[scores.length];
						scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
						for(int i=0; i<scores.length; ++i){
							int idx = scores[i].indexOf('=');
							objectives[i] = scoreboard.getObjective(scores[i].substring(0, idx));
							scores[i] = scores[i].substring(idx+1);
							double minValueDouble = getRangeMin(argument.value, Integer.MIN_VALUE); // Yes, scores can be negative
							double maxValueDouble = getRangeMax(argument.value, Integer.MAX_VALUE);
							scoreMinValues[i] = (int)minValueDouble;
							scoreMaxValues[i] = (int)maxValueDouble;
							// Minecraft does not allow non-integer scores
							if(minValueDouble != scoreMinValues[i] || maxValueDouble != scoreMaxValues[i]){
								throw new IllegalArgumentException("Scores in the 'score' argument may only be integers");
							}
						}
						Predicate<String> checkScores = eName -> {
							for(int i=0; i<scores.length; ++i){
								int score = objectives[i].getScore(eName).getScore();
								if(score < scoreMinValues[i] || score > scoreMaxValues[i]) return false;
							}
							return true;
						};
						// Note: TECHNICALLY vanilla doesn't currently support matching non-players for scores argument.. (but this will likely be fixed)
						// TODO: Once it does get fixed, make sure we handle "name==null" the same way as minecraft vanilla
						entities.removeIf(e -> e.getCustomName() == null || !checkScores.test(e.getCustomName()));
						break;
					}
					case NBT:
						// TODO:
						// Example: @e[type=item,nbt={Item:{id:"minecraft:slime_ball"}}]
					case PREDICATE:
						// TODO:
						// Example: predicate=example:test_predicate
						// BronGhast video: https://www.youtube.com/watch?v=NbZo3IPlHSA
					case ADVANCEMENTS:
						// TODO:
						// @a[advancements={story/form_obsidian=false}], @a[advancement={<namespaced ID>={<criteria>=<bool>}}]
					default:
						break;

				}
			}
			// Note: sort is applied before limit in vanilla, so we do so here as well
			if(sort != SortType.ARBITRARY){
				switch(sort){
					// For entities in other worlds, minecraft seems to sort by distance from origin (checked = 2020-04-22)
					case NEAREST:
						sortByDistanceToPointThenByDistanceToSpawn(entities, origin);
						break;
					case FURTHEST:
						sortByDistanceToPointThenByDistanceToSpawn(entities, origin);
						Collections.reverse(entities);
						break;
					case RANDOM:
						Collections.shuffle(entities);
						break;
					default:
						throw new UnsupportedOperationException("Unknown sort type '"+sort+"', please update EvLib");
				}
			}
			return limit > 0 && limit < entities.size() ? entities.subList(0, limit) : entities;
		}

		@SuppressWarnings("deprecation") // TODO: add "throws X, Y, Z" for all possible exceptions
		public static Selector fromString(final CommandSender sender, final String str){
			// Attempt to parse as UUID selector
			try{return new Selector(UUID.fromString(str));}
			catch(IllegalArgumentException ex){}
			if(Bukkit.getPlayer(str) != null) return new Selector(Bukkit.getPlayer(str).getUniqueId());

			// Attempt to parse as selector without arguments
			try{return new Selector(SelectorType.fromString(str), sender);}
			catch(IllegalArgumentException ex){}

			// Attempt to parse @<SelectorType>[<argument=value>, ...]
			if(str.length() < 4) throw new IllegalArgumentException("Unable to parse selector (incomplete): "+str);
			SelectorType type = SelectorType.fromString(str.substring(0, 2));
			ArrayList<SelectorArgument> arguments = new ArrayList<>();
			String argumentStrs = str.substring(3, str.length()-1)+",";
			int argStart = 0, argEnd = argumentStrs.indexOf(',');
			while(argEnd != -1){
				int valSep = argumentStrs.indexOf('=', argStart);
				if(valSep == -1) throw new IllegalArgumentException("Unable to parse selector (missing =): "+str);
				String argTypeStr = argumentStrs.substring(argStart, valSep);
				if(!argTypeStr.equals(argTypeStr.toLowerCase())) throw new IllegalArgumentException("Selector arg types should be lower case");
				SelectorArgumentType argType = SelectorArgumentType.valueOf(argTypeStr.toUpperCase());
				String argValue;
				if(argumentStrs.charAt(valSep+1) == '"' || argumentStrs.startsWith("!\"", valSep+1)){
					int endQuote = argumentStrs.indexOf('"', valSep+2);
					while(TextUtils.isEscaped(argumentStrs, endQuote)) endQuote = argumentStrs.indexOf('"', endQuote+1);
					argValue = TextUtils.unescapeString(argumentStrs.substring(valSep+1, endQuote+1));
					argEnd = endQuote+1; // Since ',' could have been in the quoted string
				}
				else argValue = argumentStrs.substring(valSep+1, argEnd);
				arguments.add(new SelectorArgument(argType, argValue, /*validate=*/true));

				argStart = argEnd+1;
				argEnd = argumentStrs.indexOf(',', argStart);
			}
			return new Selector(type, sender, arguments.toArray(new SelectorArgument[0]));
		}

		public static List<String> getTabComplete(final CommandSender sender, final String selectorSubstr){
			List<String> tabCompletes = new ArrayList<>();
			final boolean startsWithAt = selectorSubstr.startsWith("@");
			if(selectorSubstr.length() <= 1){
				if("@".startsWith(selectorSubstr)){
					for(SelectorType s : SelectorType.values()) if(!s.toString().isEmpty()) tabCompletes.add(s.toString());
				}
				if(startsWithAt) return tabCompletes;
			}
			if(!startsWithAt){
				final String lowerCaseSelector = selectorSubstr.toLowerCase();
				for(Player p : sender.getServer().getOnlinePlayers()){
					if(p.getName().toLowerCase().startsWith(lowerCaseSelector)) tabCompletes.add(p.getName());
				}
				return tabCompletes;
			}
			try{SelectorType.fromString(selectorSubstr.substring(0, 2));}
			catch(IllegalArgumentException e){return null;}
			if(selectorSubstr.length() == 2) return Arrays.asList(selectorSubstr+"[");
			if(selectorSubstr.endsWith("]")){
				try{Selector.fromString(sender, selectorSubstr);}
				catch(Exception e){return null;}  // TODO: list specific exceptions
				return Arrays.asList(selectorSubstr);
			}
			int lastComma = selectorSubstr.lastIndexOf(',');
			if(lastComma != -1){
				try{Selector.fromString(sender, selectorSubstr.substring(0, lastComma)+"]");}
				catch(Exception e){return null;}  // TODO: list specific exceptions
			}
			int lastArgStart = Math.max(lastComma+1, 3);
			String currentArg = selectorSubstr.substring(lastArgStart);
			int valSep = currentArg.indexOf('=');
			if(valSep == -1){
				HashSet<SelectorArgumentType> usedArgumentType = new HashSet<>();
				for(String selectorArg : selectorSubstr.substring(3).split(",")){
					if(!selectorArg.equals(currentArg)) usedArgumentType.add(SelectorArgumentType.valueOf(
							selectorArg.substring(0, selectorArg.indexOf('=')).toUpperCase()));
				}
				for(SelectorArgumentType t : SelectorArgumentType.values()){
					if(t.toString().startsWith(currentArg) && !usedArgumentType.contains(t)){
						tabCompletes.add(selectorSubstr.substring(0, lastArgStart)+t.toString()+"=");
					}
				}
				return tabCompletes;
			}
			SelectorArgumentType type;
			try{type = SelectorArgumentType.valueOf(currentArg.substring(0, valSep).toUpperCase());}
			catch(IllegalArgumentException e){return null;}
			String argValueStr = currentArg.substring(valSep+1);
			try{new SelectorArgument(type, argValueStr, /*validate=*/true);}
			catch(IllegalArgumentException e){
				if(type == SelectorArgumentType.GAMEMODE){
					String currentSelectorStr = selectorSubstr.substring(0, selectorSubstr.lastIndexOf('=')+1);
					for(GameMode gm : GameMode.values()){
						final String gmNameLower = gm.name().toLowerCase();
						final String gmNameLowerNot = "!"+gmNameLower;
						if(gmNameLower.startsWith(argValueStr)) tabCompletes.add(currentSelectorStr+gmNameLower);
						if(gmNameLowerNot.startsWith(argValueStr)) tabCompletes.add(currentSelectorStr+gmNameLowerNot);
					}
					return tabCompletes;
				}
				// TODO: else if(type == SelectorArgumentType.TYPE) {... see if possible to add more types besides GAMEMODE
				return null/*currentSelectorStr*/; // might become valid once they finish typing argValue;
			}
			return Arrays.asList(selectorSubstr+",", selectorSubstr+"]");
		}

		@Override public String toString(){
			//@e[limit=4,sort=random,x=22,y=22,z=22,dx=11,dy=11,dz=11,gamemode=!survival,gamemode=!adventure,level=2..100,
			//type=!spider,name=!bob,name=!fred,scores={test=5..10},team=!team1,team=!team2,team=!"!team3",x_rotation=4..5,
			//y_rotation=4..5,tag=has_this_tag,or_this_tag,tag=and_this_tag,tag=!doesnt_hav_this_tag,nbt="{Age:0}"]
			if(type == SelectorType.UUID) return ((Entity)executer).getUniqueId().toString();
			if(arguments.isEmpty()) return type.toString;
			return new StringBuilder(type.toString()).append('[').append(
					arguments.stream().map(arg -> arg.toString()).collect(Collectors.joining(","))
			).append(']').toString();
		}
		@Override public boolean equals(Object other){
			return other != null && other instanceof Selector &&
					((Selector)other).type.equals(type) &&
					((Selector)other).executer.equals(executer) &&
					((Selector)other).arguments == null ? arguments == null : ((Selector)other).arguments.equals(arguments) &&
					((Selector)other).origin == null ? origin == null : ((Selector)other).origin.equals(origin);
		}
	}

	// Utilities for SelectorArgument validation
	private static boolean isValidPositionNumber(String value){
		return value.matches("^(?:[~^]|(?:[~^]?-?(?:(?:\\.[0-9]+)|(?:[0-9]+(?:\\.[0-9]+)?))))$");
	}
	private static boolean isValidNumber(String value){
		return value.matches("^-?(?:(?:\\.[0-9]+)|(?:[0-9]+(?:\\.[0-9]+)?))$");
	}
	private static boolean isValidRange(String value, double min, double max){
		if(value.startsWith("..")) value = value.substring(2);
		else if(value.endsWith("..")) value = value.substring(0, value.length()-2);
		else if(value.contains("..")){
			String[] nums = value.split("\\.\\.");
			if(nums.length > 2) return false;
			// Format is XXX..XXX
			try{
				double lower = Double.parseDouble(nums[0]), upper = Double.parseDouble(nums[1]);
				return min < lower && upper < max;
			}
			catch(NumberFormatException ex){return false;}
		}
		double dValue = Double.parseDouble(value);
		return min <= dValue && dValue <= max;
	}
	public static boolean validateSelectorArgument(SelectorArgumentType type, String value){
		switch(type){
			case X: case Y: case Z:
				if(value.startsWith("..")) value = value.substring(2);
				else if(value.endsWith("..")) value = value.substring(0, value.length()-2);
				else if(value.contains("..")){
					String[] nums = value.split("\\.\\.");
					if(nums.length > 2) return false;
					// Format is ~XXX..XXX
					// Tidle notation is only available in Bedrock Edition
//					return isValidPositionNumber(nums[0]) && isValidNumber(nums[1]);
					return isValidNumber(nums[0]) && isValidNumber(nums[1]);
				}
				return isValidPositionNumber(value);
			case DX: case DY: case DZ:
				return value.matches("-?(?:(?:[0-9]+)|(?:[0-9]*\\.[0-9]+))");
			case DISTANCE:
			case LEVEL:
				return isValidRange(value, Double.MIN_VALUE, Double.MAX_VALUE);
			case X_ROTATION:
				return isValidRange(value, -90, +90);// -90==up, 0==forward, +90==down
			case Y_ROTATION:
				return isValidRange(value, -180, +180);// -180==+180==north, -90=east, 0=south, +90=west
			case LIMIT:
				return value.matches("0*[1-9][0-9]*");
			case GAMEMODE:
				final String finalValue = value;
				return Arrays.stream(GameMode.values()).anyMatch(gm -> gm.name().toLowerCase().equals(finalValue));
			case SORT:
				return Arrays.asList("nearest", "furthest", "random", "arbitrary").contains(value);
			case NAME:
//				if(value.startsWith("!")) value = value.substring(1);
			case TEAM:// team=teamName,team=!teamName,team=,team=!
			case TAG:
//				if(value.isEmpty() || value.equals("!")) return true;
//				if(value.startsWith("!")) value = value.substring(1);
//				return Bukkit.getScoreboardManager().getMainScoreboard().getTeam(value) != null;
			case SCORES:// scores={health=15..20,deaths=3..}
			case TYPE:// type=!chicken,type=!cow,type=#skeletons
			case NBT:// type=item,nbt={Item:{id:"minecraft:slime_ball"}}
			case PREDICATE:// predicate=example:test_predicate
			case ADVANCEMENTS:// advancements={story/form_obsidian=false}], @a[advancement={<namespaced ID>={<criteria>=<bool>}}
			default:
				return true;
			
		}
	}

	// Utilities for Selector.resolve()
	private static double getRangeMin(String value, double min){
		if(value.startsWith("..")) return min;
		else if(value.endsWith("..")) value = value.substring(0, value.length()-2);
		else if(value.contains("..")) value = value.substring(0, value.indexOf(".."));
		if(value.contains("..")) throw new IllegalArgumentException("Can only have one '..' separator per selector argument");
		double dValue = Double.parseDouble(value);
		if(dValue < min) throw new IllegalArgumentException("Selector argument '"+value+"' is smalller than the allowed minimum '"+min+"'");
		return dValue;
	}
	private static double getRangeMax(String value, double max){
		if(value.endsWith("..")) return max;
		else if(value.startsWith("..")) value = value.substring(2);
		else if(value.contains("..")) value = value.substring(value.indexOf("..")+2);
		if(value.contains("..")) throw new IllegalArgumentException("Can only have one '..' per selector argument");
		double dValue = Double.parseDouble(value);
		if(dValue > max) throw new IllegalArgumentException("Selector argument '"+value+"' is greater than the allowed maximum '"+max+"'");
		return dValue;
	}
	private static void sortByDistanceToPointThenByDistanceToSpawn(List<Entity> entities, Location point){
		Comparator<Double> doubleComparator = Comparator.comparingDouble(d -> d);
		Collections.sort(entities, new Comparator<Entity>(){
			@Override public int compare(Entity e1, Entity e2){
				if(e1.getWorld().getUID().equals(point.getWorld().getUID())){
					if(e2.getWorld().getUID().equals(point.getWorld().getUID())){
						return doubleComparator.compare(
								e1.getLocation().distanceSquared(point),
								e2.getLocation().distanceSquared(point));
					}
					return -1;
				}
				if(e2.getWorld().getUID().equals(point.getWorld().getUID())){
					return 1;
				}
				return doubleComparator.compare(
						e1.getLocation().distanceSquared(e1.getWorld().getSpawnLocation()),
						e2.getLocation().distanceSquared(e2.getWorld().getSpawnLocation()));
			}
		});
	}
}