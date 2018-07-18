package com.questworld.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

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

	void addAdapter(VersionAdapter child) {
		if (child == null)
			throw new NullPointerException("Appended adapters must not be null");

		Log.info("Adding adapter for version: " + child.toString());

		adapters.add(child);
		Collections.sort(adapters);
		invalidateIndices();
	}

	private void invalidateIndices() {
		makeSpawnEggIndex = -1;
		makePlayerHeadIndex = -1;
		shapelessRecipeIndex = -1;
		sendActionbarIndex = -1;
	}

	@Override
	public String forVersion() {
		throw new UnsupportedOperationException("Version cannot be determined for multi-version adapters");
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

	private int lastCacheIndex = -1;

	private Object dispatch(Object... params) {
		int i = 0;
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();

		for (; i < trace.length; ++i)
			if (trace[i].getMethodName().equals("dispatch"))
				break;

		String methodName = trace[i + 1].getMethodName();

		for (Method m : VersionAdapter.class.getMethods()) {
			if (m.getName().equals(methodName)) {
				for (i = 0; i < adapters.size(); ++i) {
					try {
						VersionAdapter adapter = adapters.get(i);
						Object result = m.invoke(adapter, params);
						lastCacheIndex = i;

						Log.info("Caching " + adapter.getClass().getSimpleName() + " (" + adapter.toString()
								+ ") for method " + methodName);

						return result;
					}
					catch (Throwable t) {
					}
				}
			}
		}

		throw new IllegalStateException("No version adapter found that supports \"" + methodName + "\"");
	}

	@Override
	public String toString() {
		return adapters.toString();
	}
}
