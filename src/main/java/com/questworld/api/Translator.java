package com.questworld.api;

import com.questworld.util.Log;

public interface Translator {
	String path();

	String[] placeholders();

	String toString();
	
	default String[] wrap(String[] rawPlaceholders) {
		int length = rawPlaceholders.length;
		String[] result = new String[length];
		for (int i = 0; i < length; ++i)
			result[i] = "%" + rawPlaceholders[i] + "%";

		return result;
	}
	
	static void test(Translator... values) {
		String[] strings = {
				"[first]", "[second]", "[third]", "[fourth]", "[fifth]", "[sixth]", "[seventh]", "[eighth]"
		};
		
		for(Translator t : values) {
			Log.info(QuestWorld.translate(t, strings));
		}
	}
}
