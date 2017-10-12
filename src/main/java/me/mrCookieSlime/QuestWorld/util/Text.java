package me.mrCookieSlime.QuestWorld.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.annotation.Nullable;

public class Text {
	// TODO: Probably make all of this better and then comment
	public final static char dummyChar = '&';
	public final static char colorChar = ChatColor.COLOR_CHAR;
	
	/**
	 * Colors a string
	 * 
	 * @param input A string with "&1" style colors
	 * @return Colored string
	 */
	public static String colorize(String input) {
		if(input == null)
			return null;
		
		return ChatColor.translateAlternateColorCodes(dummyChar, input);
	}
	
	public static String colorize(String... inputs) {
		StringBuilder sb = new StringBuilder(inputs.length);
		
		for(String input : inputs)
			sb.append(colorize(input));
		
		return sb.toString();
	}
	
	public static String[] colorizeList(String... inputs) {
		String[] output = new String[inputs.length];
		
		for(int i = 0; i < inputs.length; ++i)
			output[i] = colorize(inputs[i]);
		
		return output;
	}
	
	public static String decolor(String input) {
		if(input == null)
			return null;
		
		return ChatColor.stripColor(input);
	}
	
	public static String decolor(String... inputs) {
		StringBuilder sb = new StringBuilder(inputs.length);
		
		for(String input : inputs)
			sb.append(decolor(input));
		
		return sb.toString();
	}
	
	public static String[] decolorList(String... inputs) {
		String[] output = new String[inputs.length];
		
		for(int i = 0; i < inputs.length; ++i)
			output[i] = decolor(inputs[i]);
		
		return output;
	}
	
	public static String escape(String input) {
		if(input == null)
			return null;
		
		int len = input.length() - 1;
		char[] inputArray = input.toCharArray();
		
		for(int i = 0; i < len; ++i) {
			char c = inputArray[i];
			inputArray[i] = (c == colorChar) ? dummyChar : c;
		}
		
		return new String(inputArray);
	}
	
	public static String escape(String... inputs) {
		StringBuilder sb = new StringBuilder(inputs.length);
		
		for(String input : inputs)
			sb.append(escape(input));
		
		return sb.toString();
	}
	
	public static String[] escapeList(String... inputs) {
		String[] output = new String[inputs.length];
		
		for(int i = 0; i < inputs.length; ++i)
			output[i] = escape(inputs[i]);
		
		return output;
	}
	
	static Pattern firstLetter = Pattern.compile("\\b\\S");
	
	public static String niceName(String input) {
		input = input.replace('_', ' ').trim().toLowerCase();

		StringBuffer sb = new StringBuffer(input.length());
		
		Matcher m = firstLetter.matcher(input);
		while (m.find())
			m.appendReplacement(sb, m.group().toUpperCase());
		
		m.appendTail(sb);
		
		return sb.toString();
	}
	
	public static String timeFromNum(long minutes) {
		long hours = minutes / 60;
		minutes = minutes - hours;
		
		return hours + "h " + minutes + "m";
	}
	
	private static final String[] progress_colors = {
		"&4", "&c", "&6", "&e", "&2", "&a"
	};
		
	private static final String progress_bar = "::::::::::::::::::::";
	
	public static String progressBar(int current, int total, @Nullable("defaults to xx%") String append) {
		int length = (current * 20) / total;
		if(append == null)
			append = ((current * 100) / total) + "%";
		
		return colorize(
				progress_colors[(current * 6) / total],
				progress_bar.substring(20 - length),
				"&7",
				progress_bar.substring(length),
				" - ",
				append
		);
	}
	
	public static String itemName(ItemStack stack) {
		if(stack.hasItemMeta() && stack.getItemMeta().hasDisplayName())
			return stack.getItemMeta().getDisplayName();
		
		try {
			return Reflect.nmsGetItemName(stack);
		}
		catch(Exception e) {
			return niceName(stack.getType().toString());
		}
	}
}
