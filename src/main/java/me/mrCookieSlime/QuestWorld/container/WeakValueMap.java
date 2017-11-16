package me.mrCookieSlime.QuestWorld.container;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class WeakValueMap<K, V> extends HashMap<K, WeakReference<V>>{
	private static final long serialVersionUID = 3126876009114270413L;
	
	public V putWeak(K key, V value) {
		WeakReference<V> old = super.put(key, new WeakReference<>(value));
		if(old != null)
			return old.get();
		return null;
	}
	
	public V getOrNull(Object key) {
		WeakReference<V> value = super.get(key);
		if(value != null) {
			V object = value.get();
			if(object != null)
				return object;
			
			remove(key);
		}
		
		return null;
	}
}
