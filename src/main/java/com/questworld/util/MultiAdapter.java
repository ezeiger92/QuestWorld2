package com.questworld.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

/**
 * 
 * 
 * @see VersionAdapter
 * 
 * @author ezeiger92
 */
class MultiAdapter extends VersionAdapter {
	private ArrayList<VersionAdapter> adapters = new ArrayList<>();
	
	private static final HashMap<String, Method> methodCache;
	
	static {
		methodCache = new HashMap<>();
		
		for (Method m : VersionAdapter.class.getMethods()) {
			if(Modifier.isAbstract(m.getModifiers())) {
				methodCache.put(m.getName(), m);
			}
		}
	}
	
	public MultiAdapter() {
		super(Reflect.specificVersion());
	}

	void addAdapter(VersionAdapter child) {
		if (child == null)
			throw new NullPointerException("Appended adapters must not be null");

		Log.info("Adding adapter for version: " + child.toString());

		adapters.add(child);
		Collections.sort(adapters);

		int ourVersion = -1;
		int size = adapters.size();
		
		for(int i = 0; i < size; ++i) {
			if(this.compareTo(adapters.get(i)) != 1) {
				ourVersion = i;
				break;
			}
		}

		// Try old, older, oldest, new, newer, newest
		// Best chance to find a good match
		if(ourVersion > -1) {
			ArrayList<VersionAdapter> front = new ArrayList<>(adapters.subList(0, ourVersion));
			ArrayList<VersionAdapter> back = new ArrayList<>(adapters.subList(ourVersion, size));
			
			Collections.reverse(front);
			
			adapters.clear();
			adapters.addAll(back);
			adapters.addAll(front);
		}
		
		invalidateIndices();
	}

	private void invalidateIndices() {
		makeSpawnEggIndex = -1;
		makePlayerHeadIndex = -1;
		shapelessRecipeIndex = -1;
		sendActionbarIndex = -1;
		setItemDamage = -1;
	}

	private int makeSpawnEggIndex = -1;

	@Override
	public void makeSpawnEgg(ItemStack result, EntityType mob) {
		if (makeSpawnEggIndex >= 0)
			adapters.get(makeSpawnEggIndex).makeSpawnEgg(result, mob);

		else {
			dispatch(result, mob);
			makeSpawnEggIndex = lastCacheIndex;
		}
	}

	private int makePlayerHeadIndex = -1;

	@Override
	public void makePlayerHead(ItemStack result, OfflinePlayer player) {
		if (makePlayerHeadIndex >= 0)
			adapters.get(makePlayerHeadIndex).makePlayerHead(result, player);

		else {
			dispatch(result, player);
			makePlayerHeadIndex = lastCacheIndex;
		}
	}

	private int shapelessRecipeIndex = -1;

	@Override
	public ShapelessRecipe shapelessRecipe(String recipeName, ItemStack output) {
		if (shapelessRecipeIndex >= 0)
			return adapters.get(shapelessRecipeIndex).shapelessRecipe(recipeName, output);

		else {
			ShapelessRecipe result = (ShapelessRecipe) dispatch(recipeName, output);
			shapelessRecipeIndex = lastCacheIndex;

			return result;
		}
	}

	private int sendActionbarIndex = -1;

	@Override
	public void sendActionbar(Player player, String message) {
		if (sendActionbarIndex >= 0)
			adapters.get(sendActionbarIndex).sendActionbar(player, message);

		else {
			dispatch(player, message);
			sendActionbarIndex = lastCacheIndex;
		}
	}

	private int sendTitleIndex = -1;

	@Override
	public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		if (sendTitleIndex >= 0)
			adapters.get(sendTitleIndex).sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);

		else {
			dispatch(player, title, subtitle, fadeIn, stay, fadeOut);
			sendTitleIndex = lastCacheIndex;
		}
	}

	private int setItemDamage = -1;

	@Override
	public void setItemDamage(ItemStack result, int damage) {
		if (setItemDamage >= 0)
			adapters.get(setItemDamage).setItemDamage(result, damage);

		else {
			dispatch(result, damage);
			setItemDamage = lastCacheIndex;
		}
	}

	private int lastCacheIndex = -1;

	private Object dispatch(Object... params) {
		int i = 0;
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();

		for (; i < trace.length; ++i)
			if (trace[i].getMethodName().equals("dispatch"))
				break;

		String methodName = trace[i + 1].getMethodName();
		
		Method m = methodCache.get(methodName);
		
		if(m != null) {
			HashMap<String, Throwable> exceptionMap = new HashMap<>();
			
			for (i = 0; i < adapters.size(); ++i) {
				VersionAdapter adapter = adapters.get(i);
				
				try {
					Object result = m.invoke(adapter, params);
					lastCacheIndex = i;

					Log.info("Caching " + adapter.getClass().getSimpleName() + " (" + adapter.toString()
							+ ") for method " + methodName);

					return result;
				}
				catch (Throwable t) {
					exceptionMap.put(adapter.getVersion().toString(), t);
				}
			}
			
			Log.warning("Dumping failed adapter output:");
			
			for(Map.Entry<String, Throwable> entry : exceptionMap.entrySet()) {
				Log.warning("  Version: " + entry.getKey());
				entry.getValue().printStackTrace();
				Log.warning("---------------------------");
			}
		}

		throw new IllegalStateException("No version adapter found that supports \"" + methodName + "\"");
	}

	@Override
	public String toString() {
		return "(Running " + getVersion() + ") " + adapters;
	}
}
