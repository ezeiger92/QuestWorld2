package me.mrCookieSlime.QuestWorld.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.mrCookieSlime.QuestWorld.api.menu.Menu;

public class MenuManager {
	private static MenuManager instance = new MenuManager();
	public static MenuManager get() {
		return instance;
	}
	
	public Map<UUID, Menu> playerOpenMenus = new HashMap<>();
}
