package com.questworld.api.context;

import java.util.HashMap;
import java.util.Locale;
import java.util.function.Supplier;

public class Context implements Cloneable {
	protected HashMap<String, Supplier<Object>> replacements;
	
	public Context() {
		this(new HashMap<>());
	}
	
	protected Context(HashMap<String, Supplier<Object>> map) {
		replacements = map;
	}
	
	private static final Object nullSupplier() {
		return null;
	}
	
	public boolean map(String key, Supplier<Object> value) {
		Supplier<Object> old = replacements.put(key, value);
		
		if(old != null) {
			replacements.put(key, old);
			return false;
		}
		
		return true;
	}
	
	public final boolean has(String key) {
		return replacements.containsKey(key);
	}
	
	public final String apply(String in) {
		in = in.toLowerCase(Locale.ENGLISH);
		
		int match = -1;
		
		while((match = in.indexOf('%', match)) != -1) {
			int end = in.indexOf('%', match + 1);
			
			if(end != -1) {
				String part = in.substring(match + 1, end);
				
				Object found = replacements.getOrDefault(part, Context::nullSupplier).get();
				
				if(found != null) {
					String sub = found.toString();
					
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
	
	@Override
	public Context clone() {
		Context context;
		try {
			context = (Context) super.clone();
		}
		catch(CloneNotSupportedException e) {
			throw new Error(e);
		}
		
		context.replacements = new HashMap<>(replacements);
		
		return context;
	}
	
	public static final class Immutable extends Context {
		public Immutable(Context source) {
			super(source.replacements);
		}
		
		@Override
		public boolean map(String key, Supplier<Object> value) {
			return false;
		}
		
		// 
		@Override
		public Immutable clone() {
			return this;
		}
	}
}
