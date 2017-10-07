package me.mrCookieSlime.QuestWorld.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;

public class EconWrapper {
	private Economy econ = null;
	
	public static EconWrapper wrap() {
		EconWrapper econ = new EconWrapper();
		if(econ.get() == null)
			return null;
		
		return econ;
	}
	
	private EconWrapper() {
		if(Bukkit.getPluginManager().getPlugin("Vault") != null)
			makeEcon();
	}
	
	private void makeEcon() {
		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
	    if (economyProvider != null)
	    	econ = economyProvider.getProvider();
	}

	public Economy get() {
		return econ;
	}
}
