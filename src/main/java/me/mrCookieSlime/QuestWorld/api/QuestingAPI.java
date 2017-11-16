package me.mrCookieSlime.QuestWorld.api;

import java.util.Map;

import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.api.contract.IFacade;
import me.mrCookieSlime.QuestWorld.util.Sounds;
import net.milkbowl.vault.economy.Economy;

public abstract class QuestingAPI {

	@SuppressWarnings("unchecked")
	public static final <T extends MissionType> T getMissionType(String typeName) {
		return (T)instance.missionTypeOf(typeName);
	}
	
	public static final Map<String, MissionType> getMissionTypes() {
		return instance.missionTypes();
	}
	
	public static final MissionViewer getViewer() {
		return instance.missionViewer();
	}
	
	public static final Economy getEconomy() {
		return instance.economy();
	}
	
	public static final IFacade getFacade() {
		return instance.facade();
	}
	
	public static final Sounds getSounds() {
		return instance.sounds();
	}
	
	public static String translate(Translator key, String... replacements) {
		return instance.translation(key, replacements);
	}
	
	public static Plugin getPlugin() {
		return instance.plugin();
	}
	
	// INTERNAL
	private static QuestingAPI instance = null;
	
	protected QuestingAPI() {
		instance = this;
	}
	
	protected abstract MissionType missionTypeOf(String typeName);
	protected abstract Map<String, MissionType> missionTypes();
	protected abstract MissionViewer missionViewer();
	protected abstract Economy economy();
	protected abstract IFacade facade();
	protected abstract Sounds sounds();
	protected abstract String translation(Translator key, String... replacements);
	protected abstract Plugin plugin();
}
