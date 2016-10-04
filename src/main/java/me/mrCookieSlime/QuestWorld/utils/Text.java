package me.mrCookieSlime.QuestWorld.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

public class Text {
	// TODO: Probably make all of this better and then comment
	public final static char dummyChar = '&';
	public final static char colorChar = ChatColor.COLOR_CHAR;
	
	public static String colorize(String input) {
		if(input == null)
			return null;
		
		return ChatColor.translateAlternateColorCodes(dummyChar, input);
	}
	
	public static String colorize(String... inputs) {
		StringBuilder sb = new StringBuilder();
		
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
		StringBuilder sb = new StringBuilder();
		
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
		
		return input.replace(colorChar, dummyChar);
	}
	
	public static String escape(String... inputs) {
		StringBuilder sb = new StringBuilder();
		
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
	
	public static String niceName(String INPUT) {
		String Output = INPUT.replace('_', ' ').trim().toLowerCase();
		Pattern p = Pattern.compile("\\b[a-z]");
		Matcher m = p.matcher(Output);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, Output.substring(m.start(), m.end()).toUpperCase());
		}
		m.appendTail(sb);
		
		return sb.toString();
	}
}
