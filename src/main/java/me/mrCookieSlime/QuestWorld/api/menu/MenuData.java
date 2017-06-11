package me.mrCookieSlime.QuestWorld.api.menu;

import java.util.HashMap;
import java.util.Map;

public class MenuData {
	private Map<Integer, MenuAction> actions = new HashMap<>();
	
	public MenuAction getAction(int index) {
		return actions.get(index);
	}
	
	public MenuAction putAction(int index, MenuAction action) {
		return actions.put(index, action);
	}
}
