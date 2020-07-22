package net.evmodder.EvLib.extras;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

public class TextUtils{
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

	private static boolean isHex(char ch){return  ('0' <= ch && ch <= '9') || ('a' <= ch && ch <= 'f') || ('A' <= ch && ch <= 'F');}
	public static String translateAlternateColorCodes(char altColorChar, String textToTranslate){
		StringBuilder builder = new StringBuilder("");
		char[] msg = textToTranslate.toCharArray();
		boolean colorPending = false;
		for(int i=0; i<msg.length; ++i){
			if(msg[i] == altColorChar && !isEscaped(msg, i)){
				if(colorPending) builder.append(altColorChar); // &&&ctest => &&<red>test
				colorPending = true;
			}
			else if(!colorPending) builder.append(msg[i]);
			else{
				if(isColorOrFormat(msg[i])){
					builder.append(ChatColor.COLOR_CHAR).append(msg[i]);
				}
				else if(msg[i] == '#' || msg[i] == 'x' && i+3 < msg.length){//§x§0§0§9§9§0§0
					if(msg[i+1] == altColorChar){
						if(i+6 < msg.length && isHex(msg[i+2]) && isHex(msg[i+4]) && isHex(msg[i+6])){//&#&r&r&g&g&b&b or &#&r&g&b
							if(i+12 < msg.length && isHex(msg[i+8]) && isHex(msg[i+10]) && isHex(msg[i+12])){//&#&r&r&g&g&b&b
								builder.append(ChatColor.COLOR_CHAR).append('x')
									.append(ChatColor.COLOR_CHAR).append(msg[i+2]).append(ChatColor.COLOR_CHAR).append(msg[i+4])//r
									.append(ChatColor.COLOR_CHAR).append(msg[i+6]).append(ChatColor.COLOR_CHAR).append(msg[i+8])//g
									.append(ChatColor.COLOR_CHAR).append(msg[i+10]).append(ChatColor.COLOR_CHAR).append(msg[i+12]);//b
								i += 12;
							}
							else{//&#&r&g&b
								builder.append(ChatColor.COLOR_CHAR).append('x')
									.append(ChatColor.COLOR_CHAR).append(msg[i+2]).append(ChatColor.COLOR_CHAR).append(msg[i+2])//r
									.append(ChatColor.COLOR_CHAR).append(msg[i+4]).append(ChatColor.COLOR_CHAR).append(msg[i+4])//g
									.append(ChatColor.COLOR_CHAR).append(msg[i+6]).append(ChatColor.COLOR_CHAR).append(msg[i+6]);//b
								i += 6;
							}
						}
					}
					else{
						if(isHex(msg[i+1]) && isHex(msg[i+2]) && isHex(msg[i+3])){//&#rrggbb or //&#rgb
							if(i+6 < msg.length && isHex(msg[i+4]) && isHex(msg[i+5]) && isHex(msg[i+6])){//&#rrggbb
								builder.append(ChatColor.COLOR_CHAR).append('x')
									.append(ChatColor.COLOR_CHAR).append(msg[i+1]).append(ChatColor.COLOR_CHAR).append(msg[i+2])//r
									.append(ChatColor.COLOR_CHAR).append(msg[i+3]).append(ChatColor.COLOR_CHAR).append(msg[i+4])//g
									.append(ChatColor.COLOR_CHAR).append(msg[i+5]).append(ChatColor.COLOR_CHAR).append(msg[i+6]);//b
								i += 6;
							}
							else{//&#rgb
								builder.append(ChatColor.COLOR_CHAR).append('x')
									.append(ChatColor.COLOR_CHAR).append(msg[i+1]).append(ChatColor.COLOR_CHAR).append(msg[i+1])//r
									.append(ChatColor.COLOR_CHAR).append(msg[i+2]).append(ChatColor.COLOR_CHAR).append(msg[i+2])//g
									.append(ChatColor.COLOR_CHAR).append(msg[i+3]).append(ChatColor.COLOR_CHAR).append(msg[i+3]);//b
								i += 3;
							}
						}
					}
				}
			}
		}
		if(colorPending) builder.append(altColorChar);
		return builder.toString();
	}
	public static String translateAlternateColorCodes(char altColorChar, String textToTranslate, char resetColor){//TODO: doesn't support hex colors
		char[] msg = textToTranslate.toCharArray();
		for(int i=1; i<msg.length; ++i){
			if(msg[i-1] == altColorChar && isColorOrFormat(msg[i]) && !isEscaped(msg, i-1)){
				msg[i-1] = ChatColor.COLOR_CHAR;
				if(msg[i] == ChatColor.RESET.getChar()) msg[i] = resetColor;
			}
		}
		return new String(msg);
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
			else if((colorPick=(ch == altColorChar)) == false) builder.append(ch);
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
		for(String item : thingsToEscape){
			//TODO: special handling for special characters
			if(item.equals("\n")) str = str.replace("\n", "\\n");
			else str = str.replace(item, "\\"+item);
		}
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
	public static String formatTime(long millis, boolean show0s, String timePrefix, String unitPrefix, String sep, long[] scale, char[] units){
		if(millis == 0) return new StringBuilder(timePrefix).append("0").append(unitPrefix).append(units[units.length-1]).toString();
		int i = 0;
		while(millis < scale[i]) ++i;
		StringBuilder builder = new StringBuilder("");
		for(; i < scale.length-1; ++i){
			if(show0s || millis / scale[i] != 0){
				long scaledTime = millis / scale[i];
				builder.append(timePrefix).append(scaledTime).append(unitPrefix).append(units[i]).append(sep);
			}
			millis %= scale[i];
		}
		if(show0s || millis != 0) builder
			.append(timePrefix).append(millis / scale[scale.length-1])
			.append(unitPrefix).append(units[units.length-1]).toString();
		return builder.toString();
	}
	/**
	 * Takes a string such as 30w6d11h55m33s or 1y and returns a value in milliseconds
	 *
	 * @param formattedTime an Ev-style formatted time string
	 * @return a {@code long} representing the time interval in milliseconds.
	 */
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
		boolean lower = false;
		for(char c : str.toCharArray()){
			if(c == toSpace){builder.append(' '); lower = false;}
			else if(lower){builder.append(Character.toLowerCase(c));}
			else{builder.append(Character.toUpperCase(c)); lower = true;}
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
	//https://minecraft.gamepedia.com/Resource_pack#Fonts, DefaultAsstes/assets/minecraft/textures/font/ascii.png
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