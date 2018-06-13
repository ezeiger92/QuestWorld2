package com.questworld.util;

import java.util.Optional;

import org.bukkit.Bukkit;

/**
 * Single purpose class for quickly accessing services registered with Bukkit.
 * 
 * @see BukkitService#find
 * 
 * @author Erik Zeiger
 */
public final class BukkitService {

	private BukkitService() {
	}

	/**
	 * Retrieves a service provider from Bukkit given the interface it implements.
	 * 
	 * @param clazz The service class that is desired
	 * @return The service provider
	 */
	public static <T> Optional<T> find(Class<T> clazz) {
		return Optional.ofNullable(Bukkit.getServer().getServicesManager().getRegistration(clazz))
				.map(service -> service.getProvider());
	}
}
