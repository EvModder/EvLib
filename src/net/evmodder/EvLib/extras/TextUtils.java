package net.evmodder.EvLib.extras;

import java.util.Arrays;
import java.util.LinkedList;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TextUtils{
	static final char COLOR_SYMBOL = ChatColor.WHITE.toString().charAt(0);
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
/*	private static final RefClass classIChatBaseComponent = ReflectionUtils.getRefClass("{nms}.IChatBaseComponent");
	private static final RefClass classChatSerializer = ReflectionUtils.getRefClass("{nms}.IChatBaseComponent$ChatSerializer");
	private static final RefClass classPacketPlayOutChat = ReflectionUtils.getRefClass("{nms}.PacketPlayOutChat");
	private static final RefClass classCraftPlayer = ReflectionUtils.getRefClass("{cb}.entity.CraftPlayer");
	private static final RefClass classEntityPlayer = ReflectionUtils.getRefClass("{nms}.EntityPlayer");
	private static final RefClass classPlayerConnection = ReflectionUtils.getRefClass("{nms}.PlayerConnection");
	private static final RefClass classPacket = ReflectionUtils.getRefClass("{nms}.Packet");

	private static final RefMethod methodA = classChatSerializer.getMethod("a", String.class);
	private static final RefMethod methodAddSibling = classIChatBaseComponent.getMethod("addSibling", classIChatBaseComponent);
	private static final RefMethod methodGetHandle = classCraftPlayer.getMethod("getHandle");
	private static final RefMethod methodSendPacket = classPlayerConnection.getMethod("sendPacket", classPacket);

	private static final RefField fieldPlayerConnection = classEntityPlayer.getField("playerConnection");
	private static final RefConstructor makePacketPlayOutChat = classPacketPlayOutChat.getConstructor(classIChatBaseComponent);
*/
	enum Event{CLICK,HOVER};
	public enum TextAction{
		//ClickEvent
		LINK("§b", "open_url", Event.CLICK),
		FILE("&[something]", "open_file", Event.CLICK),
		RUN_CMD("§2", "run_command", Event.CLICK),
		SUGGEST_CMD("§9", "suggest_command", Event.CLICK),
		PAGE("&[something]", "change_page", Event.CLICK),
		//HoverEvent
		SHOW_TEXT("§a", "show_text", Event.HOVER),
		ACHIEVEMENT("&[something]", "show_achievement", Event.HOVER),
		ITEM("&[something]", "show_item", Event.HOVER),
		ENTITY("&[something]", "show_entity", Event.HOVER),

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
				if(hyperText.contains("=>")){
					String[] data = hyperText.split("=>");
					hyperText = data[0];
					actionText = data[1].trim();
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

//				Eventials.getPlugin().getLogger().info("PreText: "+preText);
//				Eventials.getPlugin().getLogger().info("HyperText: "+hyperText);
//				Eventials.getPlugin().getLogger().info("ActionText: "+actionText);

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
			CommandUtils.runCommand("minecraft:tellraw "+p.getName()+' '+raw);
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
			CommandUtils.runCommand("minecraft:tellraw "+p.getName()+' '+raw);
			//p.sendRawMessage(raw);//TODO: Doesn't work! (last checked: 1.12.1)
		}
	}

	public static String translateAlternateColorCodes(char altColorChar, String textToTranslate){
		char[] msg = textToTranslate.toCharArray();
		for(int i=1; i<msg.length; ++i){
			if(msg[i-1] == altColorChar && isColorOrFormat(msg[i]) && !isEscaped(msg, i-1)){
				msg[i-1] = COLOR_SYMBOL;
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
				builder.append(COLOR_SYMBOL);
				++i;
			}
			builder.append(msg[i-1]);
		}
		if(msg.length != 0) builder.append(msg[msg.length-1]);
		return builder.toString();
	}

	public static ChatColor getCurrentColor(String str){
		char[] msg = str.toCharArray();
		for(int i=msg.length-1; i>0; --i){
			if(msg[i-1] == COLOR_SYMBOL && isColor(msg[i])) return ChatColor.getByChar(msg[i]);
		}
		return null;
	}
	//Returns NULL if no format is present at end of string
	public static ChatColor getCurrentFormat(String str){
		char[] msg = str.toCharArray();
		for(int i=msg.length-1; i>0; --i){
			if(msg[i-1] == COLOR_SYMBOL && isColorOrFormat(msg[i])){
				return isColor(msg[i]) ? null : ChatColor.getByChar(msg[i]);
			}
		}
		return null;
	}
	public static String getCurrentColorAndFormat(String str){
		char[] msg = str.toCharArray();
		String result = "";
		for(int i=msg.length-1; i>0; --i){
			if(msg[i-1] == COLOR_SYMBOL){
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

	public static String escapeTextActionCodes(String str){
		str = str.replace("\\", "\\\\");//Escape escapes first!
		for(TextAction n : TextAction.values()) str.replace(n.marker, "\\"+n.marker);
		return str;
	}

	public static LinkedList<String> toListFromString(String string){
		LinkedList<String> list = new LinkedList<String>();
		list.addAll(Arrays.asList(string.substring(1, string.lastIndexOf(']')).split(", ")));
		if(list.size() == 1 && list.get(0).isEmpty()) list.clear();
		return list;
	}

	public static String locationToString(Location loc){
		return locationToString(loc, ChatColor.GRAY, ChatColor.DARK_GRAY);}
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

	/* ----------==========---------- PIXEL WIDTH CALCULATION METHODS ----------==========---------- */
	final public static int MAX_PIXEL_WIDTH = 320, MAX_MONO_WIDTH = 80, MAX_PLAYERNAME_WIDTH = 96/*6*16*/;
	/**
	 * returns character pixel-width, NOT safe with format codes
	 * @param ch the character to check
	 * @return character width in pixels
	 */
	public static int pxLen(char ch){
		switch(ch){
			case '§':
				return -6; // Actual width is 5
			case '.': case ',':
			case ':': case ';':
			case 'i': case '!': case '|': case '\'':
				return 2;
			case '`':
			case 'l':
				return 3;
			case 'I': case 't':
			case '[': case ']': case '(': case ')': case '{': case '}':
			case ' ': // space!
				return 4;
			case '"': case '*':
			case '<': case '>':
			case 'f': case 'k':
				return 5;
			case '@': case '~':
				return 7;
		}
		//for(int px : charList.keySet()) if(charList.get(px).indexOf(ch) >= 0) return px;
		return 6;
	}

	/**
	 * returns String pixel-width, considering format codes
	 * @param str the String to check
	 * @return String width in pixels
	 */
	public static int strLen(String str, boolean mono){
		if(mono) return ChatColor.stripColor(str).length();
		int len = 0;
		boolean bold = false, colorPick = false;
		for(char ch : str.toCharArray()){
			if(colorPick){
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
			colorPick = (ch == '§');
			len += pxLen(ch);
			if(bold && pxLen(ch) > 0) ++len;
		}
		return len;
	}
}