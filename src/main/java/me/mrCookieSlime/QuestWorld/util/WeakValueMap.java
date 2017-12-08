package me.mrCookieSlime.QuestWorld.util;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class WeakValueMap<K, V> {
	private HashMap<K, WeakReference<V>> map = new HashMap<>();
	
	public V putWeak(K key, V value) {
		WeakReference<V> old = map.put(key, new WeakReference<>(value));
		if(old != null)
			return old.get();
		return null;
	}
	
	public V getOrNull(Object key) {
		WeakReference<V> value = map.get(key);
		if(value != null) {
			V object = value.get();
			if(object != null)
				return object;
			
			remove(key);
		}
		
		return null;
	}
	
	public void remove(Object key) {
		map.remove(key);
	}
	
	public void clear() {
		map.clear();
	}
}
