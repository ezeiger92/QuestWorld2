package me.mrCookieSlime.QuestWorld.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class BukkitService {
	public static final <T> T get(Class<T> clazz) {
		RegisteredServiceProvider<T> service = Bukkit.getServer().getServicesManager().getRegistration(clazz);
		if(service != null)
			return service.getProvider();
		
		return null;
	}
}
