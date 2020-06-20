package com.questworld.util.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.questworld.util.Log;
import com.questworld.util.Reflect;
import com.questworld.util.Version;

/**
 * 
 * 
 * @see VersionAdapter
 * 
 * @author ezeiger92
 */
public class MultiAdapter extends TypedAdapter {
	private final ArrayList<VersionAdapter> adapters = new ArrayList<>();
	private final HashMap<Action, VersionAdapter> cachedAdapters = new HashMap<>();
	
	public MultiAdapter() {
		super();
	}

	public void addAdapter(VersionAdapter child) {
		if (child == null)
			throw new NullPointerException("Appended adapters must not be null");

		Log.info("Adding adapter for version: " + child.toString());

		int order = compareTo(child) >= 0 ? 1 : -1;
		
		int index;
		for (index = 0; index < adapters.size(); ++index) {
			VersionAdapter adapter = adapters.get(index);
			
			if (child.compareTo(adapter) * order > 0) {
				break;
			}
			
			if (order == 1 && compareTo(adapter) < 0) {
				break;
			}
		}

		adapters.add(index, child);
		
		cachedAdapters.clear();
	}

	@Override
	public void makePlayerHead(ItemStack result, OfflinePlayer player) {
		dispatch(Action.MAKE_PLAYER_HEAD, result, player);
	}

	@Override
	public void makeSpawnEgg(ItemStack result, EntityType mob) {
		dispatch(Action.MAKE_SPAWN_EGG, result, mob);
	}

	@Override
	public void sendActionbar(Player player, String message) {
		dispatch(Action.SEND_ACTIONBAR, player, message);
	}

	@Override
	public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		dispatch(Action.SEND_TITLE, player, title, subtitle, fadeIn, stay, fadeOut);
	}
	
	private Object dispatch(Action method, Object... args) {
		VersionAdapter cached = cachedAdapters.get(method);
		
		if(cached != null) {
			try {
				return cached.invoke(method, args);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		HashMap<String, Throwable> exceptionMap = new HashMap<>();
		
		for (VersionAdapter adapter : adapters) {
			try {
				Object result = adapter.invoke(method, args);
				cachedAdapters.put(method, adapter);
				
				Log.info("Caching " + adapter.getClass().getSimpleName() + " (" + adapter.toString()
						+ ") for " + method);
				
				return result;
			}
			catch(Throwable t) {
				exceptionMap.put(adapter.getVersion().toString(), t);
			}
		}
		
		Log.warning("Dumping failed adapter output:");
		
		for(Map.Entry<String, Throwable> entry : exceptionMap.entrySet()) {
			Log.warning("  Version: " + entry.getKey());
			entry.getValue().printStackTrace();
			Log.warning("---------------------------");
		}
		
		throw new IllegalStateException("No version adapter found that supports " + method);
	}
	
	@Override
	public Version getVersion() {
		return Reflect.specificVersion();
	}

	@Override
	public String toString() {
		return "(Running " + getVersion() + ") " + adapters;
	}
}
