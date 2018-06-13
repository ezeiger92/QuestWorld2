package com.questworld.api;

public interface Translator {
	String path();

	String[] placeholders();

	public String toString();

	default public String[] wrap(String[] rawPlaceholders) {
		int length = rawPlaceholders.length;
		String[] result = new String[length];
		for (int i = 0; i < length; ++i)
			result[i] = "%" + rawPlaceholders[i] + "%";

		return result;
	}
}
