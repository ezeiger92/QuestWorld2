package com.questworld.util;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.questworld.api.QuestWorld;
import com.questworld.api.Translation;
import com.questworld.api.annotation.Nullable;

public final class Text {
	private Text() {
	}

	// TODO: Probably make all of this better and then comment
	public final static char dummyChar = '&';
	public final static char colorChar = ChatColor.COLOR_CHAR;
	
	private static final String greenCheck = colorize("&2&l\u2714");
	private static final String redX = colorize("&4&l\u2718");

	/**
	 * Colors a string
	 * 
	 * @param input A string with "&1" style colors
	 * @return Colored string
	 */
	public static String colorize(@Nullable("Returns null") String input) {
		if (input == null)
			return null;

		return ChatColor.translateAlternateColorCodes(dummyChar, input);
	}

	public static String escapeColor(String input) {
		return input.replace(colorChar, dummyChar);
	}

	public static String colorize(String... inputs) {
		StringBuilder sb = new StringBuilder(inputs.length);

		for (String input : inputs)
			sb.append(colorize(input));

		return sb.toString();
	}

	public static String[] colorizeList(String... inputs) {
		String[] output = new String[inputs.length];

		for (int i = 0; i < inputs.length; ++i)
			output[i] = colorize(inputs[i]);

		return output;
	}

	public static String decolor(@Nullable("Returns null") String input) {
		if (input == null)
			return null;

		return ChatColor.stripColor(input);
	}

	public static String decolor(String... inputs) {
		StringBuilder sb = new StringBuilder(inputs.length);

		for (String input : inputs)
			sb.append(decolor(input));

		return sb.toString();
	}

	public static String[] decolorList(String... inputs) {
		String[] output = new String[inputs.length];

		for (int i = 0; i < inputs.length; ++i)
			output[i] = decolor(inputs[i]);

		return output;
	}
	
	/**
	 * Transforms a boolean into a nice text badge.
	 * 
	 * @param state A boolean value
	 * @return <font color="green"><b>&#x2714;</b></font> if true;
	 * <font color="red"><b>&#x2718;</b></font> if false
	 */
	public static String booleanBadge(boolean state) {
		return state ? greenCheck : redX;
	}

	public static String serializeNewline(@Nullable("Returns null") String input) {
		if (input == null)
			return null;

		return input.replace("\\", "\\\\").replace("\n", "\\n");
	}

	public static String deserializeNewline(@Nullable("Returns null") String input) {
		if (input == null)
			return null;

		return input.replaceAll("(?i)(?<!\\\\)((?:\\\\\\\\)*)\\\\n", "$1\n").replace("\\\\", "\\");
	}

	public static String stringOf(Location location) {
		if (location.getWorld() != null)
			return QuestWorld.translate(Translation.WORLD_FMT, String.valueOf(location.getBlockX()),
					String.valueOf(location.getBlockY()), String.valueOf(location.getBlockZ()),
					location.getWorld().getName());
		
		return QuestWorld.translate(Translation.UNKNOWN_WORLD);
	}

	public static String stringOf(Location location, int radius) {
		if (location.getWorld() != null)
			return QuestWorld.translate(Translation.RANGE_FMT, String.valueOf(location.getBlockX()),
					String.valueOf(location.getBlockY()), String.valueOf(location.getBlockZ()),
					location.getWorld().getName(), String.valueOf(radius));
		
		return QuestWorld.translate(Translation.UNKNOWN_WORLD);
	}

	public static UUID toUniqueId(String uuidString) {
		if (uuidString != null)
			try {
				return UUID.fromString(uuidString);
			}
			catch (IllegalArgumentException e) {
			}

		return null;
	}

	static Pattern firstLetter = Pattern.compile("\\b\\S");

	public static String niceName(String input) {
		input = input.replace('_', ' ').trim().toLowerCase(Locale.getDefault());

		StringBuffer sb = new StringBuffer(input.length());

		Matcher m = firstLetter.matcher(input);
		while (m.find())
			m.appendReplacement(sb, m.group().toUpperCase(Locale.getDefault()));

		m.appendTail(sb);

		return sb.toString();
	}

	public static String timeFromNum(long minutes) {
		long hours = minutes / 60;
		minutes = minutes - hours * 60;
		
		return QuestWorld.translate(Translation.TIME_FMT, String.valueOf(hours), String.valueOf(minutes));
	}

	private static final String[] progress_colors = { "&4", "&c", "&6", "&e", "&2", "&a" };

	private static final String progress_bar = "::::::::::::::::::::";

	public static String progressBar(int current, int total, @Nullable("defaults to xy%") String append) {
		if (total <= 0) {
			total = 1;
			current = 0;
		}
		current = Math.max(Math.min(current, total), 0);

		int length = (current * 20) / total;
		if (append == null)
			append = ((current * 100) / total) + "%";

		return colorize(progress_colors[(current * 5) / total], progress_bar.substring(20 - length), "&7",
				progress_bar.substring(length), " - ", append);
	}

	public static String itemName(ItemStack stack) {
		if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName())
			return stack.getItemMeta().getDisplayName();

		try {
			return Reflect.nmsGetItemName(stack);
		}
		catch (Exception e) {
			e.printStackTrace();
			return niceName(stack.getType().toString());
		}
	}

	/**
	 * This function wraps words to align at a specific length, and works with
	 * colorized strings. The minimum length is 8 characters, and shorter lengths
	 * will be set to 8.
	 * 
	 * @param max_length The maximum length of string
	 * @param input An array of colorized strings
	 * @return An array of strings, split by length
	 */
	public static ArrayList<String> wrap(int max_length, String... input) {
		ArrayList<String> output = new ArrayList<>(input.length);
		max_length = Math.max(max_length, 8);
		String format = "";
		for (String s1 : input) {
			if (s1 == null) {
				continue;
			}

			for (String s : s1.split("\n")) {
				int begin = 0;
				int end = -1;
				int seq_begin = 0;
				int seq_end = -1;
				String prepared_format = format;
				String committed_format = format;

				for (int i = 0, n = 0; i < s.length(); ++i) {
					char c1 = s.charAt(i);
					if (c1 == colorChar)
						if (i + 1 != s.length()) {
							char c = Character.toLowerCase(s.charAt(i + 1));
							if ("0123456789abcdefr".indexOf(c) != -1) {
								prepared_format = String.valueOf(colorChar) + c;
								n -= 2;
								if (i > seq_end)
									seq_begin = i;
								seq_end = i + 1;
							}
							else if ("olmnk".indexOf(c) != -1) {
								prepared_format += String.valueOf(colorChar) + c;
								n -= 2;
								if (i > seq_end)
									seq_begin = i;
								seq_end = i + 1;
							}
						}

					if (c1 == ' ' && i > 0)
						end = i;

					if (n == max_length) {
						if (end == -1) {
							if (i - 2 == seq_end || i - 1 == seq_end)
								end = seq_begin + i - seq_end - 2;
							else
								end = i - 1;

							output.add(format + s.substring(begin, end) + '-');
						}
						else
							output.add(format + s.substring(begin, end));

						begin = end;
						n = i - end;
						end = -1;
						format = committed_format;
					}
					else
						++n;

					if (i >= seq_end)
						committed_format = prepared_format;
				}
				output.add(format + s.substring(begin));
				format = prepared_format;
			}
		}

		return output;
	}
}
