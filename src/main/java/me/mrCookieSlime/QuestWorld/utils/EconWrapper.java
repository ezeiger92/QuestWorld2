package me.mrCookieSlime.QuestWorld.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;

public class EconWrapper {
	private Economy econ = null;
	
	public EconWrapper() {
		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
	    if (economyProvider != null)
	    	econ = economyProvider.getProvider();
	}

	public Economy get() {
		return econ;
	}
}
