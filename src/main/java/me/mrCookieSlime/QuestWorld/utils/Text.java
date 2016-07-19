package me.mrCookieSlime.QuestWorld.utils;

import org.bukkit.ChatColor;

public class Text {
	// TODO: Probably make all of this better and then comment
	public final static char dummyChar = '&';
	public final static char colorChar = ChatColor.RESET.toString().charAt(0);
	
	public static String colorize(String input) {
		return ChatColor.translateAlternateColorCodes(dummyChar, input);
	}
	
	public static String colorize(String... inputs) {
		StringBuilder sb = new StringBuilder();
		
		for(String input : inputs)
			sb.append(colorize(input));
		
		return sb.toString();
	}
	
	public static String decolor(String input) {
		return input.replace(colorChar, dummyChar);
	}
	
	public static String decolor(String... inputs) {
		StringBuilder sb = new StringBuilder();
		
		for(String input : inputs)
			sb.append(decolor(input));
		
		return sb.toString();
	}
}
