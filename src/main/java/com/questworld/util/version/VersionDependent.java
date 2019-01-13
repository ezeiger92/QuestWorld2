package com.questworld.util.version;

import com.questworld.api.annotation.Control;
import com.questworld.util.Reflect;
import com.questworld.util.Version;

public abstract class VersionDependent<T> {
	private final Version version;

	public static <T> T pick(T beforeVersion, Version version, T object) {
		if(Reflect.getVersion().compareTo(version) > 0) {
			return beforeVersion;
		}
		else {
			return object;
		}
	}
	
	public static <T> T pick(Version version, T object, T afterVersion) {
		if(Reflect.getVersion().compareTo(version) < 0) {
			return afterVersion;
		}
		else {
			return object;
		}
	}
	
	public VersionDependent(Version version) {
		this.version = version;
	}
	
	@Control
	protected T getBefore() {
		return null;
	}
	
	@Control
	protected T getAfter() {
		return null;
	}
	
	protected abstract T getObject();
	
	public final T get() {
		Version server = Reflect.getVersion();
		
		int i = server.compareTo(version);
		
		T result = null;
		
		if(i > 0) {
			result = getAfter();
		}
		else if(i < 0) {
			result = getBefore();
		}
		
		if(result == null) {
			result = getObject();
		}
		
		return result;
	}
}
