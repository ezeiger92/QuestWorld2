package me.mrCookieSlime.QuestWorld.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Single purpose class for quickly accessing services registered with Bukkit.
 * 
 * @see BukkitService#get
 * 
 * @author Erik Zeiger
 */
public final class BukkitService {
	/**
	 * Retrieves a service provider from Bukkit given the interface it
	 * implements. If no provider exists, <tt>null</tt> is returned.
	 * 
	 * @param clazz The service class that is desired
	 * @return The service provider, if found. Otherwise <tt>null</tt>
	 */
	public static <T> T get(Class<T> clazz) {
		RegisteredServiceProvider<T> service = Bukkit.getServer().getServicesManager().getRegistration(clazz);
		if(service != null)
			return service.getProvider();
		
		return null;
	}
}
