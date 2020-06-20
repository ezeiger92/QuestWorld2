package com.questworld.util.adapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.EnumMap;

import com.questworld.util.Version;

/**
 * Provides an interface for common operations that require different code in
 * different server versions. Please note: This interface may expand at any
 * time. New methods will be marked deprecated and given a safe, dummy
 * implementation. The next minor API version will change them into abstract
 * methods.<br/>
 * <br/>
 * Originally created to handle Spigot's method of sending actionbar messages,
 * this can be used to support legacy server versions to a degree.
 * 
 * @see PartialAdapter
 * 
 * @author ezeiger92
 */
public abstract class VersionAdapter implements Comparable<VersionAdapter> {
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	protected @interface Implementing {
		Action value();
	}
	
	private final Version version;
	private final EnumMap<Action, Method> mapping;
	
	VersionAdapter() {
		this.version = null;
		this.mapping = null;
	}
	
	protected final Object invoke(Action method, Object... args) throws Exception {
		Method m = mapping.get(method);
		
		if (m != null) {
			return m.invoke(this, args);
		}
		
		throw new UnsupportedOperationException("Adaptor does not supply \"" + method + "\"");
	}
	
	public VersionAdapter(Version version) {
		this.version = version;
		
		EnumMap<Action, Method> mapping = new EnumMap<>(Action.class);
		
		for (Method method : getClass().getMethods()) {
			Implementing imp  = method.getAnnotation(Implementing.class);
			
			if (imp == null) {
				continue;
			}
			
			Action action = imp.value();
			
			if (mapping.putIfAbsent(action, method) != null) {
				throw new IllegalStateException("Multiple definitions found for " +
						action + ": " + mapping.get(action).getName() + ", " + method.getName());
			}
			
			action.validate(method);
		}
		
		this.mapping = mapping;
	}
	
	public Version getVersion() {
		return version;
	}

	@Override
	public int compareTo(VersionAdapter other) {
		return getVersion().compareTo(other.getVersion());
	}

	@Override
	public String toString() {
		return getVersion().toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof VersionAdapter)
			return getVersion().equals(((VersionAdapter)other).getVersion());

		return false;
	}

	@Override
	public int hashCode() {
		return ("va-" + toString()).hashCode();
	}
}
