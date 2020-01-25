package com.questworld.util;

import java.util.regex.Pattern;

/*
 * Is this a knock-off 'NamspacedKey' without
 * the constructor deprecation? Maybe...
 */
public class UniqueKey {
	private final String namespace;
	private final String key;
	
	public UniqueKey(String namespace, String key) {
		this.namespace = sanitize(namespace);
		this.key = sanitize(key);
	}
	
	private static final Pattern VALID = Pattern.compile("[0-9a-z._-]+", Pattern.CASE_INSENSITIVE);
	private static String sanitize(String input) {
		if(VALID.matcher(input).matches()) {
			return input;
		}
		
		throw new IllegalArgumentException("Input contains invalid characters");
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public String getKey() {
		return key;
	}
	
	private static final int insensitiveStringHash(String input) {
		int hash = 17;
        char val[] = input.toCharArray();

        for (int i = 0; i < val.length; i++) {
        	hash = 31 * hash + (val[i] & 0b11011111);
        }
        return hash;
	}
	
	@Override
	public String toString() {
		return namespace + ":" + key;
	}
	
	@Override
	public int hashCode() {
		int hash = 17;
		hash = hash * 31 + insensitiveStringHash(namespace);
		hash = hash * 31 + insensitiveStringHash(key);
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof UniqueKey) {
			UniqueKey other = (UniqueKey) obj;
			return other.key.equalsIgnoreCase(key) && other.namespace.equalsIgnoreCase(namespace);
		}
		
		return false;
	}
}
