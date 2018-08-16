package com.questworld.api.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public abstract class Context {
	private final String prefix;
	private final HashMap<String, Supplier<Object>> replacements = new HashMap<>();
	
	public Context() {
		this("");
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	protected final Map<String, Supplier<Object>> getMapping() {
		return Collections.unmodifiableMap(replacements);
	}
	
	protected final void mapAll(Map<String, Supplier<Object>> mapping, boolean addPrefix) {
		if(addPrefix) {
			for(Map.Entry<String, Supplier<Object>> entry : mapping.entrySet()) {
				map(entry.getKey(), entry.getValue());
			}
		}
		else {
			replacements.putAll(mapping);
		}
	}
	
	public Context(String prefix) {
		if(prefix != null) {
			this.prefix = prefix;
		}
		else {
			this.prefix = "";
		}
	}
	
	private static final Object nullSupplier() {
		return null;
	}
	
	protected final void map(String key, Supplier<Object> supplier) {
		if(key == null || key.length() == 0) {
			key = prefix;
		}
		else {
			key = prefix + '.' + key;
		}
		
		replacements.put(key, supplier);
	}
	
	public String lookup(String key) {
		Object result = replacements.getOrDefault(key, Context::nullSupplier).get();
		
		if(result != null) {
			return result.toString();
		}
		
		return null;
	}
	
	public final String apply(String in) {
		in = in.toLowerCase(Locale.ENGLISH);
		
		int match = -1;
		
		while((match = in.indexOf('%', match)) != -1) {
			int end = in.indexOf('%', match + 1);
			
			if(end != -1) {
				String part = in.substring(match + 1, end);
				
				String sub = lookup(part);
				
				if(sub != null) {
					in = in.substring(0, match) + sub + in.substring(end + 1);
					
					match += sub.length();
				}
				else {
					match = end;
				}
			}
			else {
				break;
			}
		}
		
		return in;
	}
}
