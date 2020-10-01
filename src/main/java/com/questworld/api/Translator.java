package com.questworld.api;

import com.questworld.api.lang.PlaceholderSupply;

public interface Translator {
	String path();

	@Deprecated
	String[] placeholders();
	
	Class<? extends PlaceholderSupply<?>>[] sources();

	String toString();
	
	default String[] wrap(String first, String[] rawPlaceholders) {
		int length = rawPlaceholders.length;
		String[] result = new String[length + 1];
		result[0] = "%" + first + "%";
		for (int i = 0; i < length; ++i)
			result[i + 1] = "%" + rawPlaceholders[i] + "%";

		return result;
	}
}
