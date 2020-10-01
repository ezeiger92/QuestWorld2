package com.questworld.api.lang;

public abstract class BaseReplacements<T> implements PlaceholderSupply<T> {
	private final String prefix;
	
	public BaseReplacements() {
		this("");
	}
	
	public BaseReplacements(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public final String getReplacement(String forKey) {
		return getPrefixedReplacement(prefix, forKey);
	}
	
	final String getPrefixedReplacement(String prefix, String forKey) {

		if (forKey.startsWith(prefix)) {
			forKey = forKey.substring(prefix.length());
		}
		
		int split = forKey.indexOf('.');
		
		if (split < 0) {
			return getReplacement(forKey, forKey);
		}
		
		return getReplacement(forKey.substring(0, split), forKey);
	}

	abstract String getReplacement(String base, String fullKey);
}
