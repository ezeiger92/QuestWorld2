package me.mrCookieSlime.QuestWorld.Tests;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.ChatColor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import me.mrCookieSlime.QuestWorld.util.json.JsonBlob;

@RunWith(Parameterized.class)
public class JsonFromLegacy {
	private String legacy;
	private String output;
	
	public JsonFromLegacy(String legacy, String output) {
		this.legacy = legacy;
		this.output = output;
	}
	
	public static Object[] colorTest(String tag, String expected) {
		String S = String.valueOf(ChatColor.COLOR_CHAR);
		Object[] result = {S+tag+" color_test ", "[{\"text\":\" color_test \",\"color\":\""+expected+"\"}]"};
		
		return result;
	}
	
	public static Object[] formatTest(String tag, String expected) {
		String S = String.valueOf(ChatColor.COLOR_CHAR);
		Object[] result = {S+tag+" format_test ", "[{\"text\":\" format_test \",\""+expected+"\":\"true\"}]"};
		
		return result;
	}
	
	public static Object[] textTest(String text_in, String text_out) {
		Object[] result = {text_in, "[{\"text\":\""+text_out+"\"}]"};
		
		return result;
	}
	
	@Parameters
	public static Collection<Object[]> data() {
		String S = String.valueOf(ChatColor.COLOR_CHAR);
		return Arrays.asList(new Object[][]{
			// Sanity
			{"characters", "[{\"text\":\"characters\"}]"},
			
			// Colors
			colorTest("0", "black"),
			colorTest("1", "dark_blue"),
			colorTest("2", "dark_green"),
			colorTest("3", "dark_aqua"),
			colorTest("4", "dark_red"),
			colorTest("5", "dark_purple"),
			colorTest("6", "gold"),
			colorTest("7", "gray"),
			colorTest("8", "dark_gray"),
			colorTest("9", "blue"),
			colorTest("a", "green"),
			colorTest("b", "aqua"),
			colorTest("c", "red"),
			colorTest("d", "light_purple"),
			colorTest("e", "yellow"),
			colorTest("f", "white"),
			
			// Format
			formatTest("k", "obfuscated"),
			formatTest("l", "bold"),
			formatTest("m", "strikethrough"),
			formatTest("n", "underline"),
			formatTest("o", "italic"),
			
			// Noise
			{S+"0"+S+"1"+S+"2 noise ", "[{\"text\":\" noise \",\"color\":\"dark_green\"}]"},
			
			// Escaping
			textTest("\"quote\"", "\\\"quote\\\""),
			textTest("\\backslash\\", "\\\\backslash\\\\"),
		});
	}
	
	@Test
	public void toBlob() {
		assertEquals(output, JsonBlob.fromLegacy(legacy).toString());
	}
}
