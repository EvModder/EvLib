/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.evmodder.EvLib.extras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import net.evmodder.EvLib.EvUtils;

public class TellrawUtils{
	public enum ClickEvent{// Descriptions below are from https://minecraft.gamepedia.com/Raw_JSON_text_format
		OPEN_URL,// opens value as a URL in the player's default web browser
		OPEN_FILE,// opens the value file on the user's computer
		//NOTE: "open_file" is  used in messages generated by the game (e.g. on taking a  screenshot) and cannot be used in commands or signs.
		RUN_COMMAND,// has value entered in chat as though the player typed it themselves.
					// This can be used to run commands, provided the player has the  required permissions
		CHANGE_PAGE,// can be used only in written books
		SUGGEST_COMMAND,// similar to "run_command" but it cannot be used in a written book, the text appears only in the player's chat
						// input and it is not automatically entered. Unlike insertion, this replaces the existing contents of the chat input
		COPY_TO_CLIPBOARD;// copy the value to the clipboard

		@Override public String toString(){return name().toLowerCase();}
	}
	public enum HoverEvent{
		SHOW_TEXT,// shows raw JSON text
		SHOW_ITEM,// shows the tooltip of an item that can have NBT tags
		SHOW_ENTITY;// shows an entity's name, possibly its type, and its UUID
//		SHOW_ACHIEVEMENT;// shows advancement or statistic
//		//tellraw @a {"text":"test","hoverEvent":{"action":"show_achievement","value":"minecraft:adventure/arbalistic"}}//CURRENT DOESNT WORK

		@Override public String toString(){return name().toLowerCase();}
	}

	public enum Keybind{
		ATTACK("key.attack"), USE("key.use"),
		FORWARD("key.forward"), BACK("key.back"), LEFT("key.left"), RIGHT("key.right"), JUMP("key.jump"), SNEAK("key.sneak"), SPRINT("key.sprint"),
		OPEN_INVENTORY("key.inventory"), PICK_ITEM("key.pickItem"), DROP("key.drop"), SWAP_HANDS("key.swapHands"), OPEN_ADVANCEMENTS("key.advancements"),
		HOTBAR_1("key.hotbar.1"), HOTBAR_2("key.hotbar.2"), HOTBAR_3("key.hotbar.3"), HOTBAR_4("key.hotbar.4"), HOTBAR_5("key.hotbar.5"),
		HOTBAR_6("key.hotbar.6"), HOTBAR_7("key.hotbar.7"), HOTBAR_8("key.hotbar.8"), HOTBAR_9("key.hotbar.9"),
		CHAT("key.chat"), PLAYERLIST("key.playerlist"), COMMAND("key.command"),
		SCREENSHOT("key.screenshot"), FULLSCREEN("key.fullscreen"),
		TOGGLE_PERSPECTIVE("key.togglePerspective"), SMOOTH_CAMERA("key.smoothCamera"), SPECTATOR_OUTLINES("key.spectatorOutlines"),
		SAVE_TOOLBAR("key.saveToolbarActivator"), LOAD_TOOLBAR("key.loadToolbarActivator"),
		OPTIFINE_ZOOM("of.key.zoom");

		final String toString;
		Keybind(String toString){this.toString = toString;}
		@Override public String toString(){return toString;}
	};

	// TellrawBlob stuff
	public static abstract class Component{
		abstract public String toPlainText();
	};
//	public static abstract class TextComponent extends Component{};
	public static abstract class ComputedTextComponent extends Component{
		abstract public String toStringKV();
	};
	public final static class RawTextComponent extends ComputedTextComponent{
		String text;
		public RawTextComponent(@Nonnull String text){this.text = text;}
		//tellraw @a "test"
		//tellraw @a {"text":"test"}

		@Override public String toPlainText(){return text;}
		@Override public String toString(){return new StringBuilder().append('"').append(TextUtils.escape(text, "\"")).append('"').toString();}
		@Override public String toStringKV(){return new StringBuilder().append("\"text\":\"").append(TextUtils.escape(text, "\"")).append('"').toString();}
	}
	public final static class SelectorComponent extends Component{
		final Selector selector;
		public SelectorComponent(@Nonnull Selector selector){this.selector = selector;}
		public SelectorComponent(@Nonnull UUID uuid){this.selector = new Selector(uuid);}
		public SelectorComponent(@Nonnull SelectorType type, @Nonnull SelectorArgument...arguments){this.selector = new Selector(type, arguments);}

		@Override public String toPlainText(){
			Collection<Entity> entities = selector.resolve();
			if(entities == null || entities.isEmpty()) return "";
			Collection<String> names = entities.stream().filter(e -> e != null).map(e -> getNormalizedName(e)).collect(Collectors.toList());
			return String.join(ChatColor.GRAY+", "+ChatColor.RESET, names);
		}
		@Override public String toString(){
			return new StringBuilder().append("{\"selector\":\"").append(TextUtils.escape(selector.toString(), "\"")).append("\"}").toString();
		}
	}
	public final static class ScoreComponent extends ComputedTextComponent{
		final Selector selector;
		final Objective objective;
		String value; //Optional; overwrites output of score selector
		public ScoreComponent(@Nonnull Selector selector, @Nonnull Objective objective){this.selector = selector; this.objective = objective;}
		//tellraw @a {"score":{"name":"@p","objective":"levels","value":"3333"}}

		@Override public String toPlainText(){
			Collection<Entity> entities = selector.resolve();
			if(entities == null || entities.isEmpty()) return "";
			if(entities.size() > 1) return "ERROR: more than 1 entity matched with score selector!";
			return ""+objective.getScore(entities.iterator().next().getName()).getScore();
		}
		@Override public String toStringKV(){
			StringBuilder builder = new StringBuilder().append("\"score\":{\"name\":\"")
					.append(selector.toString()).append("\",\"objective\":\"").append(objective.getName()).append('"');
			if(value != null) builder.append(",\"value\":\"").append(TextUtils.escape(value, "\"")).append('"');
			return builder.append("}").toString();
		}
		@Override public String toString(){return new StringBuilder().append('{').append(toStringKV()).append('}').toString();}
	}
	public final static class KeybindComponent extends ComputedTextComponent{
		final Keybind keybind;
		public KeybindComponent(Keybind keybind){this.keybind = keybind;}
		//tellraw @a {"keybind":"of.key.zoom"}
		@Override public String toPlainText(){return "TODO: KEY SETTING NAME HERE";}//TODO: resolve
		@Override public String toStringKV(){return new StringBuilder().append("\"keybind\":\"").append(keybind).append('"').toString();}
		@Override public String toString(){return new StringBuilder().append('{').append(toStringKV()).append('}').toString();}
	}
	public final static class TextClickAction{
		final ClickEvent event;
		final String value;
		public TextClickAction(@Nonnull ClickEvent event, @Nonnull String value){this.event = event; this.value = value;}
		@Override public boolean equals(Object other){
			return other != null && other instanceof TextClickAction
					&& ((TextClickAction)other).event.equals(event) && ((TextClickAction)other).value.equals(value);
		}
	}
	public final static class TextHoverAction{
		final HoverEvent event;
		final String value;
		public TextHoverAction(@Nonnull HoverEvent event, @Nonnull String value){this.event = event; this.value = value;}
		@Override public boolean equals(Object other){
			return other != null && other instanceof TextHoverAction
					&& ((TextHoverAction)other).event.equals(event) && ((TextHoverAction)other).value.equals(value);
		}
	}
	public final static class ActionComponent extends Component{
		final ComputedTextComponent component;
		final TextClickAction clickAction;
		final TextHoverAction hoverAction;
		public ActionComponent(@Nonnull ComputedTextComponent component, @Nonnull TextClickAction clickAction, @Nonnull TextHoverAction hoverAction){
			this.component = component;
			this.clickAction = clickAction;
			this.hoverAction = hoverAction;
		}
		public ActionComponent(@Nonnull ComputedTextComponent component, @Nonnull TextClickAction clickAction){
			this(component, clickAction, null);
		}
		public ActionComponent(@Nonnull ComputedTextComponent component, @Nonnull TextHoverAction hoverAction){
			this(component, null, hoverAction);
		}
		public ActionComponent(@Nonnull String text, @Nonnull TextClickAction clickAction, @Nonnull TextHoverAction hoverAction){
			this(new RawTextComponent(text), clickAction, hoverAction);
		}
		public ActionComponent(@Nonnull String text, @Nonnull TextClickAction clickAction){
			this(new RawTextComponent(text), clickAction, null);
		}
		public ActionComponent(@Nonnull String text, @Nonnull TextHoverAction hoverAction){
			this(new RawTextComponent(text), null, hoverAction);
		}
		public ActionComponent(@Nonnull String text, @Nonnull ClickEvent clickEvent, @Nonnull String actionValue){
			this(new RawTextComponent(text), new TextClickAction(clickEvent, actionValue), null);
		}
		public ActionComponent(@Nonnull String text, @Nonnull HoverEvent hoverEvent, @Nonnull String actionValue){
			this(new RawTextComponent(text), null, new TextHoverAction(hoverEvent, actionValue));
		}

		boolean sameActionsAs(ActionComponent other){
			return clickAction == null ? other.clickAction == null : clickAction.equals(other.clickAction)
				&& hoverAction == null ? other.hoverAction == null : hoverAction.equals(other.hoverAction);
		}

		//minecraft:tellraw @a {"text":"[iron_hoe]","hoverEvent":{"action":"show_item","value":"{id:\"minecraft:iron_hoe\",Count:1b,tag:{Damage:2}}"}}
		@Override public String toPlainText(){return component.toPlainText();}
		@Override public String toString(){
			StringBuilder builder = new StringBuilder().append('{').append(component.toStringKV());
			if(clickAction != null) builder.append(",\"clickEvent\":{\"action\":\"").append(clickAction.event)
									.append("\",\"value\":\"").append(TextUtils.escape(clickAction.value, "\"")).append("\"}");
			if(hoverAction != null) builder.append(",\"hoverEvent\":{\"action\":\"").append(hoverAction.event)
									.append("\",\"value\":\"").append(TextUtils.escape(hoverAction.value, "\"")).append("\"}");
			return builder.append('}').toString();
		}
	}

	public final static class TellrawBlob {
		Component last = null;
		List<Component> components;
		public TellrawBlob(Component...components){
			this.components = new ArrayList<>();
			for(Component comp : components) addComponent(comp);
		}

		//TODO: instead make this an attribute of abstract Component class?
		private boolean hasModifiableText(Component comp){
			return comp instanceof RawTextComponent || (comp instanceof ActionComponent && ((ActionComponent)comp).component instanceof RawTextComponent);
		}
		private String getModifiableText(Component comp){
			return comp instanceof RawTextComponent ? ((RawTextComponent)comp).text :
					comp instanceof ActionComponent && ((ActionComponent)comp).component instanceof RawTextComponent
					? ((RawTextComponent)((ActionComponent)comp).component).text : null;
		}
		private void setModifiableText(Component comp, String text){
			if(comp instanceof RawTextComponent) ((RawTextComponent)comp).text += text;
			else if(comp instanceof ActionComponent && ((ActionComponent)comp).component instanceof RawTextComponent)
				((RawTextComponent)((ActionComponent)comp).component).text = text;
			//else throw error?
		}
		private boolean canSafelyMergeText(Component comp1, Component comp2){
			return (comp1 instanceof RawTextComponent && comp2 instanceof RawTextComponent) ||
				(comp1 instanceof ActionComponent && comp2 instanceof ActionComponent && ((ActionComponent)comp1).sameActionsAs((ActionComponent)comp2));
		}
		public boolean addComponent(@Nonnull Component component){
			String compText = getModifiableText(component);
			if(compText != null){
				if(compText.isEmpty()) return false;
				boolean isEmpty = ChatColor.stripColor(compText).isEmpty();
				if(last != null && hasModifiableText(last) && (isEmpty || canSafelyMergeText(component, last))){
					setModifiableText(last, getModifiableText(last) + compText);
					return true;
				}
				else if(isEmpty) return components.add(last = new RawTextComponent(compText));
			}
			return components.add(last = component);
		}
		public void addComponent(@Nonnull String text){addComponent(new RawTextComponent(text));}
//		public void addComponent(@Nonnull Keybind keybind){addComponent(new KeybindComponent(keybind));}
//		public void addComponent(@Nonnull Selector selector){addComponent(new SelectorComponent(selector));}
//		public void addComponent(@Nonnull String txt, @Nonnull ClickEvent evt, @Nonnull String val){addComponent(new ActionComponent(txt, evt, val));}
//		public void addComponent(@Nonnull String txt, @Nonnull HoverEvent evt, @Nonnull String val){addComponent(new ActionComponent(txt, evt, val));}

		public boolean replaceRawTextWithComponent(@Nonnull String textToReplace, @Nonnull Component replacement){
			if(textToReplace.isEmpty()) return false;

			for(int i=0; i<components.size(); ++i){
				Component comp = components.get(i);
				if(comp instanceof RawTextComponent == false) continue;
				RawTextComponent txComp = (RawTextComponent) comp;
				if(txComp.text.contains(textToReplace) == false) continue;
				int matchIdx = txComp.text.indexOf(textToReplace);
				String textBefore = txComp.text.substring(0, matchIdx);
				String textAfter = txComp.text.substring(matchIdx+textToReplace.length());
				boolean replacementHasText = hasModifiableText(replacement);
				boolean canBeEmptyBefore = (replacementHasText ? ChatColor.stripColor(textBefore) : textBefore).isEmpty();
				boolean canBeEmptyAfter = (replacementHasText ? ChatColor.stripColor(textAfter) : textAfter).isEmpty();
				if(replacementHasText){
					String replacementText = getModifiableText(replacement);
					if(canBeEmptyBefore) replacementText = textBefore + replacementText;
					if(canBeEmptyAfter) replacementText += textAfter;
					setModifiableText(replacement, replacementText);
				}
				if(canBeEmptyBefore && canBeEmptyAfter){
					components.set(i, replacement);
				}
				else if(canBeEmptyBefore){
					components.add(i, replacement);
					txComp.text = textAfter;
				}
				else if(canBeEmptyAfter){
					txComp.text = textBefore;
					components.add(i+1, replacement);
				}
				else{
					txComp.text = textBefore;
					components.add(i+1, new RawTextComponent(textAfter));
					components.add(i+1, replacement);
				}
				// Necessary to prevent accidentally creating a global selector
				if(i == 0 && replacement instanceof SelectorComponent && canBeEmptyBefore) components.add(0, new RawTextComponent(""));
//				replaceRawTextWithComponent(textToReplace, replacement); // Call recursively to replace all occurances (DANGEROUS)
				return true;
			}
			return false;
		}

		public String getPlainText(){
			StringBuilder builder = new StringBuilder();
			for(Component comp : components){
				if(hasModifiableText(comp)) builder.append(TextUtils.unescapeString(getModifiableText(comp)));
				else if(comp instanceof SelectorComponent){
					Collection<Entity> entities = ((SelectorComponent)comp).selector.resolve();
					if(entities == null || entities.isEmpty()) continue;
					Collection<String> names = entities.stream().filter(e -> e != null).map(e -> getNormalizedName(e)).collect(Collectors.toList());
					builder.append(String.join(ChatColor.GRAY+", "+ChatColor.RESET, names));
				}
				else{/* what goes here? */}
			}
			return builder.toString();
		}
		@Override public String toString(){
			String lastText = getModifiableText(last);
			while(lastText != null && ChatColor.stripColor(lastText).isEmpty()){
				components.remove(components.size()-1);
				last = components.isEmpty() ? null : components.get(components.size()-1);
				if(last != null) lastText = getModifiableText(last);
			}
			switch(components.size()){
				case 0: return "\"\"";
				case 1: return components.get(0).toString();
				default: return new StringBuilder().append('[').append(
							components.stream().map(cmp -> cmp.toString()).collect(Collectors.joining(","))
						).append(']').toString();
			}
		}
	}



	// Selector stuff. TODO: should probably relocate all of this
	private static String getNormalizedName(Entity entity){
		if(entity instanceof Player) return ((Player)entity).getDisplayName();
		return entity.getName() != null ? entity.getName() : EvUtils.getNormalizedName(entity.getType());
	}

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
		return isValidNumber(value);
	}
	public static boolean validateSelectorArgument(SelectorArgumentType type, String value){
		switch(type){
			case X: case Y: case Z: case DX: case DY: case DZ:
				if(value.startsWith("..")) value = value.substring(2);
				else if(value.endsWith("..")) value = value.substring(0, value.length()-2);
				else if(value.contains("..")){
					String[] nums = value.split("\\.\\.");
					if(nums.length > 2) return false;
					// Format is ~XXX..XXX
					return isValidPositionNumber(nums[0]) && isValidNumber(nums[1]);
				}
				return isValidPositionNumber(value);
			case DISTANCE:
			case LEVEL:
				return isValidRange(value, Double.MIN_VALUE, Double.MAX_VALUE);
			case X_ROTATION:
				return isValidRange(value, -90, +90);// -90==up, 0==forward, +90==down
			case Y_ROTATION:
				return isValidRange(value, -180, +180);// -180==+180==north, -90=east, 0=south, +90=west
			case LIMIT:
				return value.matches("[0-9]+");
			case GAMEMODE:
				return Arrays.asList("adventure", "creative", "survival", "spectator").contains(value);
//				try{GameMode.valueOf(value); return true;} catch(IllegalArgumentException e){return false;}
			case SORT:
				return Arrays.asList("nearest", "furthest", "random", "arbitrary").contains(value);
			case NAME:
//				if(value.startsWith("!")) value = value.substring(1);
			case TEAM:
			case TAG:
//				if(value.isEmpty() || value.equals("!")) return true;
//				if(value.startsWith("!")) value = value.substring(1);
//				return Bukkit.getScoreboardManager().getMainScoreboard().getTeam(value) != null;
			case SCORES://scores={test=5..10}
			case TYPE://@e[type=!chicken,type=!cow], @e[type=#skeletons] <-- selects all skeleton types
			case NBT://@e[type=item,nbt={Item:{id:"minecraft:slime_ball"}}]
			case PREDICATE://predicate=example:test_predicate
			case ADVANCEMENTS://@a[advancements={story/form_obsidian=false}], @a[advancement={<namespaced ID>={<criteria>=<bool>}}]
			default:
				return true;
			
		}
	}
	enum SelectorType{
		YOURSELF("@s"/* '*' also works */), NEAREST_PLAYER("@p"), RANDOM_PLAYER("@r"), ALL_PLAYERS("@a"), ALL_ENTITIES("@e"), UUID("");
		String toString;
		SelectorType(String toString){this.toString = toString;}
		@Override public String toString(){return toString;}
	}
	enum SelectorArgumentType{
		X/*(<Int,Int>)*/, Y, Z, DISTANCE, DX, DY, DZ,
		SCORES, TAG, TEAM, LIMIT, SORT, LEVEL, GAMEMODE/*(<adventure,creative,survival,spectator>)*/,
		NAME, X_ROTATION, Y_ROTATION, TYPE, NBT, ADVANCEMENTS, PREDICATE;
		@Override public String toString(){return name().toLowerCase();}
	}
	public static class SelectorArgument{
		String/*SelectorArgumentType*/ argument;
		String value;
		public SelectorArgument(SelectorArgumentType argument_type, String value){
			if(validateSelectorArgument(argument_type, value) == false)
				throw new IllegalArgumentException(value+" is not a valid value for "+argument_type);
			this.argument = argument_type.toString();
			this.value = value;
		}
		@Deprecated public SelectorArgument(String argument, String value){
			try{
				SelectorArgumentType argument_type = SelectorArgumentType.valueOf(argument);
				if(validateSelectorArgument(argument_type, value) == false)
					throw new IllegalArgumentException(value+" is not a valid value for "+argument_type);
			}
			catch(IllegalArgumentException ex){Bukkit.getLogger().warning("Unrecognized selector argument: "+argument);}
			this.argument = argument;
			this.value = value;
		}
		@Override public String toString(){return new StringBuilder(argument.toString()).append("=").append(value).toString();}
	}
	public static class Selector{
		final SelectorType type;
		final List<SelectorArgument> arguments;
		UUID uuid;
		Entity executer;
		Location origin;
		public Selector(UUID uuid){
			type = SelectorType.UUID;
			this.uuid = uuid;
			arguments = null;
		}
		public Selector(SelectorType type, SelectorArgument...arguments){
			if(type == SelectorType.UUID) throw new IllegalArgumentException("Please provide just the UUID of the entity to select");
			this.type = type;
			this.arguments = Arrays.asList(arguments);
		}

		public void addArgument(SelectorArgument argument){arguments.add(argument);}

		public Collection<Entity> resolve(){
			Collection<Entity> entities;
			@SuppressWarnings("unchecked")
			Collection<Entity> onlinePlayers = (Collection<Entity>)Bukkit.getServer().getOnlinePlayers();
			switch(type){
				case ALL_ENTITIES:
					entities = origin.getWorld().getEntities();
					break;
				case ALL_PLAYERS:
					entities = onlinePlayers;
					break;
				case NEAREST_PLAYER:
					entities = Arrays.asList(Collections.min(origin.getWorld().getPlayers(),
								Comparator.comparingDouble(p -> p.getLocation().distanceSquared(origin))));
					break;
				case RANDOM_PLAYER:
					entities = Arrays.asList(onlinePlayers.stream().skip((int)(onlinePlayers.size()*Math.random())).findFirst().get());
				case UUID:
					entities = Arrays.asList(Bukkit.getEntity(uuid));
					break;
				case YOURSELF:
					entities = Arrays.asList(executer);
					break;
				default:
					entities = null;//TODO: throw error
					break;
			}
			//TODO: filter entities based on selector arguments!
			return entities;
		}

		@Override public String toString(){
			//@e[limit=4,sort=random,x=22,y=22,z=22,dx=11,dy=11,dz=11,gamemode=!survival,gamemode=!adventure,level=2..100,
			//type=!spider,name=!bob,name=!fred,scores={test=5..10},team=!team1,team=!team2,team=!"!team3",x_rotation=4..5,
			//y_rotation=4..5,tag=has_this_tag,or_this_tag,tag=and_this_tag,tag=!doesnt_hav_this_tag,nbt="{Age:0}"]
			if(type == SelectorType.UUID) return uuid.toString();
			if(arguments.isEmpty()) return type.toString;
			return new StringBuilder(type.toString()).append('[').append(
					arguments.stream().map(arg -> arg.toString()).collect(Collectors.joining(","))
			).append(']').toString();
		}
	}
}
