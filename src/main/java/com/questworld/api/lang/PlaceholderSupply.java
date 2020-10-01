package com.questworld.api.lang;

public interface PlaceholderSupply<T> {
	Class<T> forClass();
	
	String getReplacement(String forKey);
}
