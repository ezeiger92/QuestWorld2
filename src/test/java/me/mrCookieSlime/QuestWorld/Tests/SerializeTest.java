package me.mrCookieSlime.QuestWorld.Tests;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.ChatColor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import me.mrCookieSlime.QuestWorld.util.Text;

@RunWith(Parameterized.class)
public class SerializeTest {
	private String serialized;
	private String deserialized;
	
	public SerializeTest(String serialized, String deserialized) {
		this.serialized = serialized;
		this.deserialized = deserialized;
	}
	
	@Parameters
	public static Collection<Object[]> data() {
		String S = String.valueOf(ChatColor.COLOR_CHAR);
		return Arrays.asList(new Object[][]{
			{"Normal", "Normal"},
			{"Colored&c", "Colored" + S + "c"},
			{"Newline\\n", "Newline\n"},
			//{"Escaped\\&c", "Escaped&c"}, // No longer supported
			{"DoubleColored\\\\&c", "DoubleColored\\" + S + "c"},
			//{"TripleEscaped\\\\\\&c", "TripleEscaped\\&c"}, // No longer supported
		});
	}
	
	@Test
	public void deserialize() {
		assertEquals(deserialized, Text.deserializeNewline(Text.colorize(serialized)));
	}
	
	@Test
	public void serialize() {
		assertEquals(serialized, Text.serializeNewline(Text.escapeColor(deserialized)));
	}
}
