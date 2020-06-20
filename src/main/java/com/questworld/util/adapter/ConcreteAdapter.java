package com.questworld.util.adapter;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.questworld.api.annotation.Mutable;

public class ConcreteAdapter extends TypedAdapter {
	private final VersionAdapter sourceAdapter;
	
	public ConcreteAdapter(VersionAdapter sourceAdapter) {
		super();
		this.sourceAdapter = sourceAdapter;
	}

	@Override
	public void makePlayerHead(@Mutable ItemStack result, OfflinePlayer player) {
		dispatch(Action.MAKE_PLAYER_HEAD, result, player);
	}
	
	@Override
	public void makeSpawnEgg(@Mutable ItemStack result, EntityType mob) {
		dispatch(Action.MAKE_SPAWN_EGG, result, mob);
	}

	@Override
	public void sendActionbar(Player player, String message) {
		dispatch(Action.SEND_ACTIONBAR, player, message);
	}

	@Override
	public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		dispatch(Action.SEND_TITLE, title, subtitle, fadeIn, stay, fadeOut);
	}
	
	private void dispatch(Action action, Object... args) {
		try {
			sourceAdapter.invoke(action, args);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public int compareTo(VersionAdapter other) {
		return sourceAdapter.compareTo(other);
	}

	@Override
	public String toString() {
		return sourceAdapter.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof VersionAdapter)
			return getVersion().equals(((VersionAdapter)other).getVersion());

		return false;
	}
}
