package com.questworld.api.context;

public class MultiContext extends Context {
	
	public MultiContext(Context... contexts) {
		this("", contexts);
	}
	
	public MultiContext(String prefix, Context... contexts) {
		super(prefix);
		for(Context ctx : contexts) {
			addContext(ctx);
		}
	}
	
	private void addContext(Context context) {
		String prefix = context.getPrefix();
		
		if(prefix.length() == 0) {
			throw new IllegalArgumentException("Context must have a prefix!");
		}
		
		mapAll(context.getMapping(), false);
		
		/*Context old = contexts.put(prefix, context);
		
		if(old != null) {
			contexts.put(prefix, old);
			throw new IllegalArgumentException("Context prefix already in use!");
		}*/
	}
	
	public void add(Context context, Context... extra) {
		addContext(context);
		
		for(Context ctx : extra) {
			addContext(ctx);
		}
	}
	
	/*@Override
	public String lookup(String key) {
		String prefix = key.substring(0, key.indexOf('.'));
		Context context = contexts.get(prefix);
		
		if(context != null) {
			return context.lookup(key);
		}
		
		return super.lookup(key);
	}*/
}
