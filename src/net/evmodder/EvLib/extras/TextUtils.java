package net.evmodder.EvLib.extras;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class TextUtils{
	//static final char COLOR_SYMBOL = ChatColor.WHITE.toString().charAt(0);
	static final char RESET = ChatColor.RESET.toString().charAt(0);
	public static final char[] COLOR_CHARS = new char[]
			{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	public static final char[] FORMAT_CHARS = new char[]{'k', 'l', 'm', 'n', 'o'};
	public static boolean isColor(char ch){
		switch(ch){
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
			case 'a': case 'b': case 'c': case 'd': case 'e':
			case 'f': case 'r': return true;
			default: return false;
		}
	}
	public static boolean isFormat(char ch){
		switch(ch){
			case 'k': case 'l': case 'm': case 'n': case 'o': return true;
			default: return false;
		}
	}
	public static boolean isColorOrFormat(char ch){
		switch(ch){
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
			case 'a': case 'b': case 'c': case 'd': case 'e':
			case 'k': case 'l': case 'm': case 'n': case 'o':
			case 'f': case 'r': return true;
			default: return false;
		}
	}


	//======================================== TODO: DELETE ALL THIS CRAP ========================================//
	enum Event{CLICK,HOVER};
	public enum TextAction{
		//ClickEvent
		LINK("§b", "open_url", Event.CLICK),
		FILE("&[something]", "open_file", Event.CLICK),
		RUN_CMD("§2", "run_command", Event.CLICK),
		SUGGEST_CMD("§9", "suggest_command", Event.CLICK),
		PAGE("${PAGE}", "change_page", Event.CLICK),
		//HoverEvent
		SHOW_TEXT("§a", "show_text", Event.HOVER),
		ACHIEVEMENT("${ACHIEVEMENT}", "show_achievement", Event.HOVER),
		ITEM("${ITEM}", "show_item", Event.HOVER),
		ENTITY("${ENTITY}", "show_entity", Event.HOVER),

		//custom
		WARP(ChatColor.LIGHT_PURPLE+"@", "run_command", Event.CLICK);
		//MONEY(ChatColor.GREEN+"$", "show_text"),
		//PLUGIN(ChatColor.RED+"", "show_item"),

		Event type;
		String marker, action;
		TextAction(String s, String a, Event t){marker = s; action = a; type = t;}

		@Override public String toString(){return marker;}

		public static int countNodes(String str){
			int count = 0;
			for(TextAction n : TextAction.values()){
				count += StringUtils.countMatches(str.replace("\\"+n.marker, ""), n.marker);
			}
			return count;
		}

		public static String parseToRaw(String string, String endIndicator){
			//{"text":"xxx","extra":[{"text":"xxx","clickEvent":{"action":"xxx","value":"xxx"}}]}
			StringBuilder raw = new StringBuilder("[{\"text\":\"\"}");

			int nodes = TextAction.countNodes(string);//component count
			for(short i = 0; i < nodes; ++i){
				//get next node
				TextAction node = null;
				int nodeIndex = string.length();
				for(TextAction n : TextAction.values()){
					int x = -1;
					do{x = string.indexOf(n.marker, x+1);}
					while(x != -1 && isEscaped(string, x));
					if(x != -1 && x < nodeIndex){nodeIndex = x; node=n;}
				}

				//cut off preText
				String preText = string.substring(0, nodeIndex); string = string.substring(nodeIndex);
				preText = unescapeString(preText);

				//cut off hyperText
				int endSpecial = string.indexOf(endIndicator); if(endSpecial == -1) endSpecial = string.length();
				String hyperText = string.substring(0, endSpecial); string = string.substring(endSpecial);
				hyperText = unescapeString(hyperText);

				String actionText="";
				//detect underlying command/link/values
				int keyValueI = hyperText.indexOf("=>");
				if(keyValueI != -1){
					actionText = hyperText.substring(keyValueI+2).trim();
					hyperText = hyperText.substring(0, keyValueI);
				}
				else{
					if(node == TextAction.RUN_CMD){
						actionText = hyperText.substring(node.marker.length()).trim();
					}
					else if(node == TextAction.WARP){
						actionText = "/warp "+hyperText.substring(node.marker.length()).trim();
					}
					else actionText = hyperText.trim();
				}

				if(!preText.isEmpty()) raw.append(",{\"text\":\"").append(preText).append("\"}");
				if(!hyperText.isEmpty()){
					raw.append(",{\"text\":\"").append(hyperText).append('"');
					if(node != null) raw
						.append(",\"").append(node.type == Event.CLICK ? "clickEvent" : "hoverEvent")
						.append("\":{\"action\":\"").append(node.action).append("\",\"value\":\"")
						.append(actionText).append("\"}");
					raw.append('}');
				}
				// Old (but perfectly valid) method:
/*				raw.append(",{\"text\":\"").append(preText).append('"');
				if(!hyperText.isEmpty()){
					raw.append(",\"extra\":[{\"text\":\"").append(hyperText).append("\"");
					if(node != null) raw.append(",\"").append(node.type == Event.CLICK ? "clickEvent" : "hoverEvent")
						.append("\":{\"action\":\"").append(node.action).append("\",\"value\":\"")
						.append(actionText).append("\"}}");
					raw.append(']');
				}
				raw.append('}');*/

				// /tellraw @a ["First","Second","Third"]
				// {"text":"Click","clickEvent":{"action":"open_url","value":"http://google.com"}}
				// {"text":"xxx","extra":[{"text":"xxx","clickEvent":{"action":"xxx","value":"xxx"}}]}
			}
			string = unescapeString(string);
			if(!string.isEmpty()) raw.append(",{\"text\":\"").append(string).append("\"}");
			return raw.append(']').toString();
		}
	};

	public static void sendModifiedText(String preMsg, String hyperMsg, TextAction action, String value,
			String postMsg, Player... recipients){
		preMsg = preMsg.replace("\n", "\\n");
		hyperMsg = hyperMsg.replace("\n", "\\n");
		value = value.replace("\n", "\\n");
		postMsg = postMsg.replace("\n", "\\n");
		StringBuilder raw = new StringBuilder("[");
		if(preMsg != null && !preMsg.isEmpty()) raw.append("{\"text\":\"").append(preMsg).append("\"},");
		raw.append("{\"text\":\"").append(hyperMsg).append("\",\"clickEvent\":{\"action\":\"")
				.append(action.action).append("\",\"value\":\"").append(value).append("\"}}");
		if(postMsg != null && !postMsg.isEmpty()) raw.append(",{\"text\": \"").append(postMsg).append("\"}");
		raw.append(']');
		for(Player p : recipients){
			//CommandUtils.runCommand("minecraft:tellraw "+p.getName()+' '+raw);//TODO: remove dependency
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:tellraw "+p.getName()+' '+raw);
			//p.sendRawMessage(raw);//TODO: Doesn't work! (last checked: 1.12.1)
		}
	}

	public static void getModifiedText(String preMsg, String hyperMsg, TextAction action, String value,
			String postMsg, Player... recipients){
		preMsg = preMsg.replace("\n", "\\n");
		hyperMsg = hyperMsg.replace("\n", "\\n");
		value = value.replace("\n", "\\n");
		postMsg = postMsg.replace("\n", "\\n");
		StringBuilder raw = new StringBuilder("[");
		if(preMsg != null && !preMsg.isEmpty()) raw.append("{\"text\":\"").append(preMsg).append("\"},");
		raw.append("{\"text\":\"").append(hyperMsg).append("\",\"clickEvent\":{\"action\":\"")
				.append(action.action).append("\",\"value\":\"").append(value).append("\"}}");
		if(postMsg != null && !postMsg.isEmpty()) raw.append(",{\"text\": \"").append(postMsg).append("\"}");
		raw.append(']');
		for(Player p : recipients){
			//CommandUtils.runCommand("minecraft:tellraw "+p.getName()+' '+raw);//TODO: remove dependency
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:tellraw "+p.getName()+' '+raw);
			//p.sendRawMessage(raw);//TODO: Doesn't work! (last checked: 1.12.1)
		}
	}

	public static void sendModifiedText(String[] preMsgs, String[] hyperMsgs,
			TextAction[] actions, String[] values, String postMsg, Player... recipients){
		if(preMsgs.length != hyperMsgs.length || hyperMsgs.length != actions.length || actions.length != values.length ||
				preMsgs.length == 0) return;

		StringBuilder raw = new StringBuilder("[");
		for(int i=0; i<hyperMsgs.length; ++i){
			if(i != 0) raw.append(',');
			raw.append(" {\"text\":\"").append(preMsgs[i]).append("\"}, {\"text\":\"")
				.append(hyperMsgs[i]).append("\", \"clickEvent\": {\"action\": \"")
				.append(actions[i].action).append("\", \"value\": \"").append(values[i]).append("\"}}");
		}
		if(postMsg != null && !postMsg.isEmpty()) raw.append(", {\"text\": \"").append(postMsg).append("\"} ");
		raw.append(']');
		for(Player p : recipients){
			//CommandUtils.runCommand("minecraft:tellraw "+p.getName()+' '+raw);//TODO: remove dependency
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:tellraw "+p.getName()+' '+raw);
			//p.sendRawMessage(raw);//TODO: Doesn't work! (last checked: 1.12.1)
		}
	}
	//============================================================================================================//

	public static String generateRandomASCII(int desiredLength){//TODO: currently unused
		StringBuilder builder = new StringBuilder();
		Random rand = new Random();
		for(int i=0; i<desiredLength; ++i){
			int randC = 33 + rand.nextInt(223);
			switch(randC){
				// These are all spaces; we avoid using them just generate a new randC
				case 127: case 129: case 141: case 143: case 144: case 157: case 160: case 173: --i; continue;
				default: break;
			}
			builder.append((char)randC);
		}
		return builder.toString();
	}

	public static String translateAlternateColorCodes(char altColorChar, String textToTranslate){
		char[] msg = textToTranslate.toCharArray();
		for(int i=1; i<msg.length; ++i){
			if(msg[i-1] == altColorChar && isColorOrFormat(msg[i]) && !isEscaped(msg, i-1)){
				msg[i-1] = ChatColor.COLOR_CHAR;
				++i;
			}
		}
		return new String(msg);
	}
	public static String translateAlternateColorCodes(char altColorChar, String textToTranslate, String reset){
		char[] msg = textToTranslate.toCharArray();
		StringBuilder builder = new StringBuilder("");
		for(int i=1; i<msg.length; ++i){
			if(msg[i-1] == altColorChar && isColorOrFormat(msg[i]) && !isEscaped(msg, i-1)){
				if(msg[i] == RESET){builder.append(RESET); continue;}
				builder.append(ChatColor.COLOR_CHAR);
				++i;
			}
			builder.append(msg[i-1]);
		}
		if(msg.length != 0) builder.append(msg[msg.length-1]);
		return builder.toString();
	}

	public static String stripColorsOnly(String str){return stripColorsOnly(str, ChatColor.COLOR_CHAR);}
	public static String stripColorsOnly(String str, char altColorChar){
		StringBuilder builder = new StringBuilder("");
		boolean colorPick = false;
		for(char ch : str.toCharArray()){
			if(colorPick && !isColor(ch)){colorPick=false; builder.append(altColorChar).append(ch);}
			else if((colorPick=(ch == '§')) == false) builder.append(ch);
		}
		if(colorPick) builder.append(altColorChar);
		return builder.toString();
	}
	public static String stripFormatsOnly(String str){return stripFormatsOnly(str, ChatColor.COLOR_CHAR);}
	public static String stripFormatsOnly(String str, char altColorChar){
		StringBuilder builder = new StringBuilder("");
		boolean colorPick = false;
		for(char ch : str.toCharArray()){
			if(colorPick && !isFormat(ch)){colorPick=false; builder.append(altColorChar).append(ch);}
			else if((colorPick=(ch == '§')) == false) builder.append(ch);
		}
		if(colorPick) builder.append(altColorChar);
		return builder.toString();
	}

	public static ChatColor getCurrentColor(String str){
		char[] msg = str.toCharArray();
		for(int i=msg.length-1; i>0; --i){
			if(msg[i-1] == ChatColor.COLOR_CHAR && isColor(msg[i])) return ChatColor.getByChar(msg[i]);
		}
		return null;
	}
	//Returns NULL if no format is present at end of string
	public static ChatColor getCurrentFormat(String str){
		char[] msg = str.toCharArray();
		for(int i=msg.length-1; i>0; --i){
			if(msg[i-1] == ChatColor.COLOR_CHAR && isColorOrFormat(msg[i])){
				return isColor(msg[i]) ? null : ChatColor.getByChar(msg[i]);
			}
		}
		return null;
	}
	public static String getCurrentColorAndFormat(String str){
		char[] msg = str.toCharArray();
		String result = "";
		for(int i=msg.length-1; i>0; --i){
			if(msg[i-1] == ChatColor.COLOR_CHAR){
				if(isColor(msg[i])) return ChatColor.getByChar(msg[i]) + result;
				if(isFormat(msg[i])){result = ChatColor.getByChar(msg[i]) + result; --i;}
			}
		}
		return result;
	}

	public static boolean isEscaped(char[] str, int x){
		boolean escaped = false;
		while(x != 0 && str[--x] == '\\') escaped = !escaped;
		return escaped;
	}
	public static boolean isEscaped(String str, int x){
		boolean escaped = false;
		while(x != 0 && str.charAt(--x) == '\\') escaped = !escaped;
		return escaped;
	}

	public static String unescapeString(String str){
		StringBuilder builder = new StringBuilder("");
		boolean unescaped = true;
		for(char c : str.toCharArray()){
			if(c == '\\' && unescaped) unescaped = false;
			else{
				builder.append(c);
				unescaped = true;
			}
		}
		return builder.toString();
	}

	public static String escape(String str, String... thingsToEscape){
		str = str.replace("\\", "\\\\");//Escape escapes first!
		for(String item : thingsToEscape) str = str.replace(item, "\\"+item);
		return str;
	}

	public static LinkedList<String> toListFromString(String string){
		LinkedList<String> list = new LinkedList<String>();
		list.addAll(Arrays.asList(string.substring(1, string.lastIndexOf(']')).split(", ")));
		if(list.size() == 1 && list.get(0).isEmpty()) list.clear();
		return list;
	}

	public static String locationToStrig(Location loc){
		return locationToString(loc, null, null);}
	public static String locationToString(Location loc, ChatColor coordColor, ChatColor commaColor){
		return locationToString(loc, coordColor, commaColor, 2);
	}
	public static String locationToString(Location loc, ChatColor coordColor, ChatColor commaColor, int precision){
		String coordPrefix = coordColor == null ? "" : ""+coordColor;
		String commaPrefix = commaColor == null ? "" : ""+commaColor;
		if(precision < 1){
			return new StringBuilder("")
					.append(coordPrefix).append(loc.getBlockX()).append(commaPrefix).append(',')
					.append(coordPrefix).append(loc.getBlockY()).append(commaPrefix).append(',')
					.append(coordPrefix).append(loc.getBlockZ())
				.toString();
		}
		String formatP = "%."+precision+"f";
		return new StringBuilder("")
				.append(coordPrefix).append(String.format(formatP, loc.getX())).append(commaPrefix).append(',')
				.append(coordPrefix).append(String.format(formatP, loc.getY())).append(commaPrefix).append(',')
				.append(coordPrefix).append(String.format(formatP, loc.getZ()))
			.toString();
	}
	public static Location getLocationFromString(String s){
		String[] data = ChatColor.stripColor(s).split(",");
		World world = org.bukkit.Bukkit.getWorld(data[0]);
		if(world != null){
			try{return new Location(world,
					Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));}
			catch(NumberFormatException ex){}
		}
		return null;
	}
	public static Location getLocationFromString(World w, String s){
		String[] data = ChatColor.stripColor(s).split(",");
		try{return new Location(w,
				Double.parseDouble(data[data.length-3]),
				Double.parseDouble(data[data.length-2]),
				Double.parseDouble(data[data.length-1]));}
		catch(ArrayIndexOutOfBoundsException | NumberFormatException ex){return null;}
	}

	static long[] scale = new long[]{31536000000L, /*2628000000L,*/ 604800000L, 86400000L, 3600000L, 60000L, 1000L};
	static char[] units = new char[]{'y', /*'m',*/ 'w', 'd', 'h', 'm', 's'};
	public static String formatTime(long millisecond){
		return formatTime(millisecond, true, "", "", ", ", scale, units);
	}
	public static String formatTime(long millisecond, boolean show0s){
		return formatTime(millisecond, show0s, "", "", ", ", scale, units);
	}
	public static String formatTime(long millisecond, boolean show0s, ChatColor timeColor, ChatColor unitColor){
		return formatTime(millisecond, show0s, timeColor, unitColor, null);
	}
	public static String formatTime(long millisecond, boolean show0s, ChatColor timeColor, ChatColor unitColor, ChatColor commaColor){
		String timePrefix = timeColor == null ? "" : timeColor+"";
		String unitPrefix = unitColor == null ? "" : unitColor+"";
		String commaPrefix = commaColor == null ? ", " : commaColor+", ";
		return formatTime(millisecond, show0s, timePrefix, unitPrefix, commaPrefix, scale, units);
	}
	public static String formatTime(long millisecond, boolean show0s, String timePrefix, String unitPrefix, String sep){
		return formatTime(millisecond, show0s, timePrefix, unitPrefix, sep, scale, units);
	}
	public static String formatTime(long time, boolean show0s, String timePrefix, String unitPrefix, String sep, long[] scale, char[] units){
		int i = 0;
		while(time < scale[i]) ++i;
		StringBuilder builder = new StringBuilder("");
		for(; i < scale.length-1; ++i){
			if(show0s || time / scale[i] != 0){
				builder.append(timePrefix).append(time / scale[i]).append(unitPrefix).append(units[i]).append(sep);
			}
			time %= scale[i];
		}
		return builder.append(timePrefix).append(time / scale[scale.length-1])
					  .append(unitPrefix).append(units[units.length-1]).toString();
	}
	public static long parseTime(String formattedTime){
		formattedTime = formattedTime.toLowerCase();
		//formattedTime.matches("(?:y[1-9][0-9]*)?(?:ew[1-9][0-9]*)?(?:d[1-9][0-9]*)?(?:h[1-9][0-9]*)?(?:m[1-9][0-9]*)?(?:s[1-9][0-9]*)?");
		long time = 0;
		for(int i=0; i<units.length && !formattedTime.isEmpty(); ++i){
			int idx = formattedTime.indexOf(units[i]);
			if(idx != -1){
				time += Long.parseLong(formattedTime.substring(0, idx))*scale[i];
				formattedTime = formattedTime.substring(idx+1);
			}
		}
		return time;
	}


	public static String capitalizeAndSpacify(String str, char toSpace){
		StringBuilder builder = new StringBuilder("");
		boolean upper = true;
		for(char ch : str.toCharArray()){
			if(ch == toSpace){builder.append(' '); upper=true;}
			else if(upper){builder.append(Character.toUpperCase(ch)); upper=false;}
			else{builder.append(Character.toLowerCase(ch));}
		}
		return builder.toString();
	}

	public static String getNormalizedItemName(String material){//TODO: move to TypeUtils?
		switch(material){
			case "CREEPER_BANNER_PATTERN":
			case "FLOWER_BANNER_PATTERN":
			case "GLOBE_BANNER_PATTERN":
			case "MOJANG_BANNER_PATTERN":
			case "SKULL_BANNER_PATTERN":
				return "Banner Pattern";
			default:
//				return capitalizeWords(material.name().toLowerCase().replace("_", " "));
				return capitalizeAndSpacify(material, '_');
		}
	}
	public static String getNormalizedEntityName(String eType){//TODO: move to EntityUtils?
		//TODO: improve this algorithm / test for errors
		switch(eType){
		case "PIG_ZOMBIE":
			return "Zombie Pigman";
		case "MUSHROOM_COW":
			return "Mooshroom";
		case "TROPICAL_FISH"://TODO: 22 varieties (already implemented in TextureKeyLookup.java)
		default:
//			return capitalizeWords(eType.toLowerCase().replace("_", " "));
			return capitalizeAndSpacify(eType, '_');
		}
	}
	public static String getNormalizedName(Material material){return getNormalizedItemName(material.name());}
	public static String getNormalizedName(EntityType eType){return getNormalizedEntityName(eType.name());}


	//TODO: Move this to TabText (once TabText is cleaned up)?
	/* ----------==========---------- PIXEL WIDTH CALCULATION METHODS ----------==========---------- */
	final public static int MAX_PIXEL_WIDTH = 320, MAX_MONO_WIDTH = 80, MAX_PLAYERNAME_MONO_WIDTH=16, MAX_PLAYERNAME_PIXEL_WIDTH = 96/*6*16*/;
	// Supports ASCII + Extended codes (32-255), and currently just assumes width=6 for all others
	// Note: Returns 1 more than actual width, since all characters are separated by a pixel
	/**
	 * returns character pixel-width, NOT safe with format codes
	 * @param ch the character to check
	 * @return character width in pixels
	 */
	public static int pxLen(char ch){
		switch(ch){
			case '§':
				return -6; // Actual width is 5
			case '.': case ','://comma44
			case ':': case ';':
			case 'i': case '!': case '|': case '\'':
			case '¡': case '¦': case '´': case '¸':
			case '·':
				return 2;
			case '`':
			case 'l':
			case '‚'://comma130
			case 'ˆ':
			case '‘': case '’':
			case '•':
			case '¨':
			case 'ì': case 'í':
				return 3;
			case 'I': case 't':
			case 'Ì': case 'Í': case 'Î': case 'Ï': case 'î': case 'ï':
			case '[': case ']': case '(': case ')': case '{': case '}':
			case ' '://space!
			case '‹': case '›':
			case '˜':
			case '°': case '¹':
				return 4;
			case '"': case '*':
			case '<': case '>':
			case 'f': case 'k':
			case '„': case '“': case '”':
			case 'ª': case '²': case '³': case 'º':
				return 5;
//			case '-':
//				return 6;
			case '@': case '~': case '–':
			case '«': case '»':
			case '¶':
				return 7;
			case '…':
			case '‰':
			case '¤':
			case '©': case '®':
			case '¼': case '½': case '¾':
				return 8;
			case '—':
			case '™':
				return 9;
			case 'Œ': case 'œ': case 'Æ': case 'æ':
				return 10;
		}
		//for(int px : charList.keySet()) if(charList.get(px).indexOf(ch) >= 0) return px;
		return 6;
	}
	public static boolean isHalfPixel(char ch){
		//Note: Italicizing can make chars "half-pixel-y", but won't ever change their width.
		switch(ch){
			case '´': case '¸'://2
			case 'ˆ': case '¨'://3
			case '˜'://4
				return true;
			default:
				return false;
		}
	}

	/**
	 * returns String pixel-width, considering format codes
	 * @param str the String to check
	 * @return String width in pixels
	 */
	public static int strLen(String str, boolean mono){
		if(mono) return ChatColor.stripColor(str).length();
		int len = 0;
		boolean bold = false, colorPick = false, halfPixel = false;
		for(char ch : str.toCharArray()){
			if(colorPick){
				colorPick = false;
				switch(ch){
					case '0': case '1': case '2': case '3': case '4':
					case '5': case '6': case '7': case '8': case '9':
					case 'a': case 'b': case 'c': case 'd': case 'e':
					case 'f': case 'r': bold = false; continue;
					case 'l': bold = true; continue;
					case 'k': case 'm': case 'n': case 'o': continue;
					default: /**/continue; // Apparently, "§x" => ""
				}
			}
			if(ch == '§'){colorPick = true; continue;}
			len += pxLen(ch);
			if(bold){
				if(isHalfPixel(ch)){
					if(!halfPixel) ++len; // Round up
					halfPixel = !halfPixel;
				}
				else ++len;
			}
		}
		return len;
	}

	public static double strLenExact(String str, boolean mono){
		if(mono) return ChatColor.stripColor(str).length();
		double len = 0;
		boolean bold = false, colorPick = false;
		for(char ch : str.toCharArray()){
			if(colorPick){
				colorPick = false;
				switch(ch){
					case '0': case '1': case '2': case '3': case '4':
					case '5': case '6': case '7': case '8': case '9':
					case 'a': case 'b': case 'c': case 'd': case 'e':
					case 'f': case 'r': bold = false; continue;
					case 'l': bold = true; continue;
					case 'k': case 'm': case 'n': case 'o': continue;
					default: /**/continue; // Apparently, "§x" => ""
				}
			}
			if(ch == '§'){colorPick = true; continue;}
			len += pxLen(ch);
			if(bold) len += isHalfPixel(ch) ? .5 : 1;
		}
		return len;
	}

	public static class StrAndPxLen{
		public String str;
		public double pxLen;
		public StrAndPxLen(String s, double l){str = s; pxLen = l;}
	}
	/**
	 * returns substring, in chars or pixels, considering format codes
	 * @param str input string
	 * @param len desired string length
	 * @param mono true if length will be in chars (for console) or false if will be in pixels (for chat area)
	 * @return object array with stripped string [0] and integer length in pixels or chars depending of mono
	 */
	public static StrAndPxLen pxSubstring(String str, double maxLen, boolean mono){
		if(mono){
			int len = 0, subStrLen = 0;
			for(char ch : str.toCharArray()){
				len += (ch == '§' ? -1 : 1);
				if(len > maxLen) break;
				++subStrLen;
			}
			return new StrAndPxLen(str.substring(0, subStrLen), len);
		}
		else{
			double pxLen = 0, subStrPxLen = 0;
			int subStrLen = 0;
			boolean bold = false, colorPick = false;
			for(char ch : str.toCharArray()){
				if(colorPick){
					colorPick = false;
					subStrLen += 2;
					switch(ch){
						case '0': case '1': case '2': case '3': case '4':
						case '5': case '6': case '7': case '8': case '9':
						case 'a': case 'b': case 'c': case 'd': case 'e':
						case 'f': case 'r': bold = false; continue;
						case 'l': bold = true; continue;
						case 'k': case 'm': case 'n': case 'o': continue;
						default: /**/continue; // Apparently, "§x" => ""
					}
				}
				if(ch == '§'){colorPick = true; continue;}
				pxLen += TextUtils.pxLen(ch);
				if(bold) pxLen += isHalfPixel(ch) ? .5 : 1;
				if(pxLen > maxLen) break;
				++subStrLen;
				subStrPxLen = pxLen;
			}
			return new StrAndPxLen(str.substring(0, subStrLen), subStrPxLen);
		}
	}
}