package me.mrCookieSlime.QuestWorld.api;

import java.util.Map;

import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.api.contract.QuestingAPI;
import me.mrCookieSlime.QuestWorld.api.contract.IFacade;
import me.mrCookieSlime.QuestWorld.util.Sounds;
import net.milkbowl.vault.economy.Economy;

public final class QuestWorld {
	private QuestWorld() {	
	}
	
	private static QuestingAPI api = null;
	
	public static void setAPI(QuestingAPI api) {
		if(QuestWorld.api != null)
			throw new UnsupportedOperationException("Cannot redefine API singleton");
		
		if(api == null)
			throw new NullPointerException("API cannot be null");
		
		QuestWorld.api = api;
	}
	
	@SuppressWarnings("unchecked")
	public static final <T extends MissionType> T getMissionType(String typeName) {
		return (T)api.missionTypeOf(typeName);
	}
	
	public static final Map<String, MissionType> getMissionTypes() {
		return api.missionTypes();
	}
	
	public static final MissionViewer getViewer() {
		return api.missionViewer();
	}
	
	public static final Economy getEconomy() {
		return api.economy();
	}
	
	public static final IFacade getFacade() {
		return api.facade();
	}
	
	public static final Sounds getSounds() {
		return api.sounds();
	}
	
	public static String translate(Translator key, String... replacements) {
		return api.translation(key, replacements);
	}
	
	public static Plugin getPlugin() {
		return api.plugin();
	}
	
	public static QuestingAPI getAPI() {
		return api;
	}
}
