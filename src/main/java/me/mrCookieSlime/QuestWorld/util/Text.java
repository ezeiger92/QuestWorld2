package me.mrCookieSlime.QuestWorld.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
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
	public static String colorize(@Nullable("Returns null") String input) {
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
	
	public static String decolor(@Nullable("Returns null") String input) {
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
	
	public static String serializeColor(@Nullable("Returns null") String input) {
		if(input == null)
			return null;
		
		return input.replace("\\", "\\\\").replace("&", "\\&").replace(colorChar, dummyChar);
	}
	
	public static String deserializeColor(@Nullable("Returns null") String input) {
		if(input == null)
			return null;
		
		input = input.replaceAll("(?i)(?<!\\\\)((?:\\\\\\\\)*)&([0-9A-FK-OR])", "$1"+colorChar+"$2");
		return input.replace("\\\\", "\\");
	}
	
	public static String stringOf(Location location) {
		return "X: " + location.getBlockX() +
				", Y: "+ location.getBlockY() +
				", Z: "+ location.getBlockZ() +
				", World: " + location.getWorld().getName();
	}
	
	public static String stringOf(Location location, int radius) {
		return stringOf(location) + ", Range: " + radius;
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
		minutes = minutes - hours * 60;
		
		return hours + "h " + minutes + "m";
	}
	
	private static final String[] progress_colors = {
		"&4", "&c", "&6", "&e", "&2", "&a"
	};
		
	private static final String progress_bar = "::::::::::::::::::::";
	
	public static String progressBar(int current, int total, @Nullable("defaults to xy%") String append) {
		if(total <= 0) {
			total = 1;
			current = 0;
		}
		current = Math.max(Math.min(current, total), 0);
		
		int length = (current * 20) / total;
		if(append == null)
			append = ((current * 100) / total) + "%";
		
		return colorize(
				progress_colors[(current * 5) / total],
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
			e.printStackTrace();
			return niceName(stack.getType().toString());
		}
	}
	
	/**
	 * This function wraps words to align at a specific length, and works with
	 * color *formatted* strings (rather than colorized strings). If you supply
	 * strings from {@link Text.colorize}, they will fail to wrap colors across
	 * multiple lines. The minimum length is 8 characters, and shorter lengths
	 * will be set to 8.
	 * 
	 * @param max_length The maximum length of string
	 * @param input An array of non-colorized strings
	 * @return An array of strings, split by length and colorized
	 */
	public static ArrayList<String> wrap(int max_length, String... input) {
		ArrayList<String> output = new ArrayList<>(input.length);
		max_length = Math.max(max_length, 8);
		String format = "";
		for(String s : input) {
			if(s == null) {
				continue;
			}
			
			int begin = 0;
			int end = -1;
			int seq_begin = 0;
			int seq_end = -1;
			String prepared_format = format;
			String committed_format = format;
			for(int i = 0, n = 0; i < s.length(); ++i) {
				char c1 = s.charAt(i);
				if(c1 == Text.dummyChar) {
					if(i + 1 != s.length()) {
						char c = s.charAt(i + 1);
						if("0123456789aAbBcCdDeEfFrR".indexOf(c) != -1)  {
							prepared_format = String.valueOf(Text.dummyChar) + c;
							n -= 2;
							if(i > seq_end)
								seq_begin = i;
							seq_end = i+1;
						}
						else if("oOlLmMnNkK".indexOf(c) != -1) {
							prepared_format += String.valueOf(Text.dummyChar) + c;
							n -= 2;
							if(i > seq_end)
								seq_begin = i;
							seq_end = i+1;
						}
					}
				}
				if(c1 == ' ' && i > 0) {
					end = i;
				}
				
				//Log.info("n: " + n + ", c: " + s.charAt(i) + ", i: " + i +  ", s: " + seq_begin + ", e: " + seq_end + ", p: " + prepared_format + ", co: " + committed_format);
				
				if(n == max_length) {
					if(end == -1) {
						if(i-2 == seq_end || i-1 == seq_end) {
							end = seq_begin + i - seq_end - 2;
						}
						else
							end = i - 1;

						//Log.info("truncate: " + s.substring(begin, end) + '-');
						output.add(Text.colorize(format + s.substring(begin, end) + '-'));
					}
					else {
						//Log.info("full: " + s.substring(begin, end));
						output.add(Text.colorize(format + s.substring(begin, end)));
					}
					//Log.info("prepared: " + prepared_format + ", committed: " + committed_format + ", format: " + format);
					begin = end;
					n = i - end;
					end = -1;
					format = committed_format;
				}
				else
					++n;
				
				if(i > seq_end) {
					committed_format = prepared_format;
				}
			}
			output.add(Text.colorize(format + s.substring(begin)));
			//Log.info("prepared: " + prepared_format + ", committed: " + committed_format + ", format: " + format);
			format = prepared_format;
		}
		
		return output;
	}
}
