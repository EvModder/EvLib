package net.evmodder.EvLib.bukkit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import net.evmodder.EvLib.TextUtils;
import net.evmodder.EvLib.TextUtils.StrAndPxLen;

/**<pre>
 * TabText: class to write column formatted text in minecraft chat area
 * 
 * - it splits each field and trims or fill with spaces to adjust tabs
 * - you can use some format codes, see http://minecraft.gamepedia.com/Formatting_codes
 * - DO NOT USE LOWERCASE CODES OR BOLD FORMAT BECAUSE IT CAN BREAK SPACING
 * 
 * - // example:
 * - multilineString  = "PLAYER------`RATE------`RANK------\n";
 * - multilineString += "§EJohn`10.01`1§R\n";
 * - multilineString += "Doe`-9.30`2";
 * 
 * - TabText tt = new TabText(multilineString);
 * - int numPages = tt.setPageHeight(pageHeight); // set page height and get number of pages
 * - tt.setTabs(10, 18, ...); // horizontal tabs positions
 * - tt.sortByFields(-2, 1); // sort by second column descending, then by first
 * - printedText = tt.getPage(desiredPage, (boolean) monospace); // get your formatted page, for console or chat area
 * 
 * see each method javadoc for additional details 
 *</pre>
 * @version 5
 * @author atesin#gmail, evmodder#gmail
 *
 */
public class TabText{//max chat width is 53*6 + 2 = 320
	final static int CHAT_HEIGHT = 100;//Chat history goes back 100 lines
	final static int CHAT_WIDTH = 320, MONO_WIDTH = 80, MAX_PLAYER_NAME_WIDTH = 96/*6*16*/;
	//Half-pixel-characters with widths [1, 2, 3, 4](+.5 if bold)
	final static char W1_HALF_C = '΄', W2_HALF_C = 'ʹ', W3_HALF_C = 'ˆ', W4_HALF_C = '˜';
	private int chatHeight;
	private double[] tabs;
	private int numPages;
	private String[] lines;
	private static Map<Integer, String> charList = new HashMap<>();

	// CONSTRUCTOR METHOD
	public TabText(String multilineString){
		setText(multilineString);
	}

	static int countMatches(String str, char ch){
		int count = 0;
		for(char c : str.toCharArray()) if(c == ch) ++count;
		return count;
	}

	// "tabs" = columns. Specify a width for each.
	// If flexFill=true, each column will grow evenly to fill any extra width.
	public static String parse(String str){return parse(str, false, false);}
	public static String parse(String str, boolean mono, boolean flexFill){
		int newLine = str.indexOf('\n');
		// Create a column for each '`' in the top line
		int numTabs = countMatches(newLine == -1 ? str : str.substring(0, newLine), '`');
		return parse(str, mono, flexFill, new double[Math.max(numTabs, 1)]);
	}
	public static String parse(String str, boolean mono, boolean flexFill, int[] tabs){
		double[] dTabs = new double[tabs.length];
		for(int i=0; i<tabs.length; ++i) dTabs[i] = tabs[i];
		return parse(str, mono, flexFill, dTabs);
	}
	public static String parse(String str, boolean mono, boolean flexFill, double[] tabs){
		TabText tt = new TabText(str);
		tt.setPageHeight(tt.lines.length);
		tt.tabs = tabs;
		int fixI = 0;
		for(int i=0; i<tt.lines.length; ++i){
			String[] fields = tt.lines[i].split("`");
			if(fields.length > tt.tabs.length){
				double[] newTabs = new double[fields.length];
				for(int j=0; j<tt.tabs.length; ++j) newTabs[j] = tt.tabs[j];
				tt.tabs = newTabs;
				fixI = i;
			}
			for(int j=0; j<fields.length; ++j){
				tt.tabs[j] = Math.max(tt.tabs[j], TextUtils.strLenExact(fields[j], mono));
				fields[j] = fields[j].trim();// Absorb any leading/trailing buffer provided by the caller
			}
			int missingTabs = tt.tabs.length - fields.length;
			if(missingTabs > 0) fields[fields.length-1] += repeat(missingTabs, '`');
			tt.lines[i] = String.join("`", fields);
		}
		for(int i=0; i<fixI; ++i){
			int missingTabs = tt.tabs.length - (countMatches(tt.lines[i], '`')+1);
			tt.lines[i] += repeat(missingTabs, '`');
		}
		if(flexFill){
			double sum = 0;
			for(double w : tt.tabs) sum += w;
			double leftover = ((mono ? MONO_WIDTH : CHAT_WIDTH) - sum) / tt.tabs.length;
			for(int i=0; i<tt.tabs.length; ++i) tt.tabs[i] += leftover;
		}
		return tt.getPage(0, mono, ChatColor.BLACK);
	}

	// SETTER METHODS
	/** can reuse the object to save resources by calling this method */
	public void setText(String multilineString){
		lines = multilineString.split("\n", -1);
	}
	
	/**
	 * set page height and get number of pages according to it, for later use with getPage()
	 * @param chatHeight lines you want for each page, no more than 10 recommended
	 * @return number of pages your text will have
	 */
	public int setPageHeight(int chatHeight){
		this.chatHeight = chatHeight;
		numPages = (int) Math.ceil((double)lines.length / (double)chatHeight);
		return numPages;
	}
	
	/**
	 * set horizontal positions of "`" separators, considering 6px chars and 53 chars max 
	 * @param tabs an integer list with desired tab column positions
	 */
	public void setTabs(boolean mono, double... tabs){
		double[] tabs2 = new double[tabs.length + 1];
		tabs2[0] = tabs[0];
		for(int i=1; i<tabs.length; ++i) tabs2[i] = tabs[i] - tabs[i - 1];
		tabs2[tabs.length] = (mono ? MONO_WIDTH : CHAT_WIDTH) - tabs[tabs.length - 1];
		this.tabs = tabs2;
	}

	// REGULAR METHODS
	/**
	 *  append chars with its width to be checked too, default width = 6 so you may only use for width != 6 chars
	 *  @param charsList a string with the chars to be added (careful with unicode or ansi chars, do some tests before)
	 *  @param charsWidth horizontal space in pixels each char occupies
	 */
	public static void addChars(String charsList, int charsWidth){
		if(charsWidth == 6) return;
		if(!charList.containsKey(charsWidth)) charList.put(charsWidth, "");
		charList.get(charsWidth).concat(charsList);
	}

	public static String getPxSpaces(double pxLenGoal, boolean monospace, String resumeColor){
		if(monospace) return repeat((int)pxLenGoal, ' ');
		StringBuilder builder = new StringBuilder();
		double pxLen = 0;
		while(pxLen < pxLenGoal-0.5){// If we hit (pxLenGoal-0.5) it is not possible, sadly, as there are no chars with width < 1px
			double lenLeft = pxLenGoal - pxLen;
			double needShift = lenLeft % 4;
			if(needShift == 0){builder.append(' '); pxLen += 4;}
			else if(needShift == 1){
				if(lenLeft >= 5){builder.append(ChatColor.BOLD).append(' ').append(resumeColor); pxLen += 5;}
				else{builder.append(W1_HALF_C); pxLen += 1;}
			}
			else if(needShift == 2){
				if(lenLeft >= 10){builder.append(ChatColor.BOLD).append("  ").append(resumeColor); pxLen += 10;}
				else{builder.append(W2_HALF_C); pxLen += 2;}
			}
			else if(needShift == 3){
				if(lenLeft >= 15){builder.append(ChatColor.BOLD).append("   ").append(resumeColor); pxLen += 15;}
				else{builder.append(W3_HALF_C); pxLen += 3;}
			}
			else if(needShift == 1.5){builder.append(ChatColor.BOLD).append(W1_HALF_C).append(resumeColor); pxLen += 1.5;}
			else if(needShift == 2.5){builder.append(ChatColor.BOLD).append(W2_HALF_C).append(resumeColor); pxLen += 2.5;}
			else if(needShift == 3.5){builder.append(ChatColor.BOLD).append(W3_HALF_C).append(resumeColor); pxLen += 3.5;}
			else if(needShift == 0.5){builder.append(ChatColor.BOLD).append(W4_HALF_C).append(resumeColor); pxLen += 4.5;}
		}
		return builder.toString();
	}

	/**
	 * get your formatted page, for chat area or console
	 * @param page desired page number (0 = all in one), considering preconfigured page height
	 * @param monospace true if fonts are fixed width (for server console) or false if variable with (for chat area)
	 * @return desired formatted, tabbed page
	 */
	public String getPage(int page, boolean monospace, ChatColor hideTabs){
		// get bounds if user wants pages
		int fromLine = (--page) * chatHeight;
		int toLine = (fromLine + chatHeight > lines.length) ? lines.length : fromLine + chatHeight;
		if(page < 0){
			fromLine = 0;
			toLine = lines.length;
		}

		// prepare lines iteration
		StringBuilder outputLines = new StringBuilder("");
		StrAndPxLen subStrAndSubPxLen;
		// iterate each line
		for(int linePos=fromLine; linePos<toLine; ++linePos){
			StringBuilder line = new StringBuilder("");
			String[] fields = lines[linePos].split("`");
			double lineLen = 0, stopLen = 0;

			for(int fieldPos=0; fieldPos<fields.length; ++fieldPos){
				// add spaces to fill out width of previous tab
				if(lineLen < stopLen){
					double lenLeft = stopLen - lineLen;
					if(hideTabs != null) line.append(hideTabs);
					line.append(getPxSpaces(lenLeft, monospace, ""+hideTabs));
					line.append(ChatColor.RESET);
				}
				// get field and set line properties
				subStrAndSubPxLen = TextUtils.pxSubstring(fields[fieldPos], tabs[fieldPos], monospace);
				line.append(subStrAndSubPxLen.str);
				lineLen += subStrAndSubPxLen.pxLen;
				stopLen += tabs[fieldPos];
			}
			if(outputLines.length() > 0) outputLines.append('\n');
			outputLines.append(line);
		}
		return outputLines.toString();
	}

	private static String repeat(int count, char with) {
		return new String(new char[count]).replace('\0', with);
	}

	/**
	 * sort lines by column values, string or numeric, IF SORT BY DECIMALS IT MUST HAVE SAME DECIMAL POSITIONS
	 * @param keys a list of column numbers criteria where 1 represents first columnn and so, negative numbers means descending order
	 */
	public void sortByFields(int... keys){
		// get indexes and sort orders first
		boolean[] desc = new boolean[keys.length];

		for (int i=0; i<keys.length; ++i){
			// get field and direction
			desc[i] = (keys[i] < 0);
			keys[i] = Math.abs(keys[i]) - 1;
		}

		// iterate lines
		String[] fields;
		String line;
		String field, field2;
		double num;
		boolean desc2;
		String[] lines2 = new String[lines.length];

		for (int i=0; i<lines.length; ++i){
			// iterate fields
			fields = lines[i].replaceAll("§.", "").split("`", -1);
			line = "";

			for(int j=0; j<keys.length; ++j){
				// probe if field looks like a number
				field = fields[keys[j]]; field2 = "~";

				try{
					num = Double.parseDouble(field);
					for(int k=field.length(); k<53; ++k) field2 += " ";
					field2 += field;

					// reverse order if negative number
					desc2 = (num < 0)? (desc[j] == false): desc[j];
				}
				catch(NumberFormatException e){
					field2 += field;
					for (int k = field.length(); k < 53; ++k) field2 += " ";
					desc2 = desc[j];
				}

				// reverse field char values if reverse order (like in rot13)
				if(desc2){
					field = "";
					for(char c: field2.toCharArray()) field += (char)(158 - c);
					field2 = field;
				}

				// add field to line
				line += field2;
			}

			// add line to lines
			lines2[i] = line+"`"+i;
		}

		// sort lines
		Arrays.sort(lines2);

		// rearrange and set lines
		String[] lines3 = new String[lines.length];

		for(int i=0; i<lines.length; ++i){
			fields = lines2[i].split("`", -1);
			lines3[i] = lines[Integer.parseInt(fields[1])];
		}
		lines = lines3;
	}
}