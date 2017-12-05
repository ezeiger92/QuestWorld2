package me.mrCookieSlime.QuestWorld.api;

import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.api.contract.QuestingAPI;
import me.mrCookieSlime.QuestWorld.api.contract.IFacade;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IPlayerStatus;
import me.mrCookieSlime.QuestWorld.api.contract.MissionEntry;
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
	
	public static final <T extends MissionType> T getMissionType(String typeName) {
		return api.getMissionType(typeName);
	}
	
	public static final Map<String, MissionType> getMissionTypes() {
		return api.getMissionTypes();
	}
	
	public static final MissionViewer getViewer() {
		return api.getViewer();
	}
	
	public static final Economy getEconomy() {
		return api.getEconomy();
	}
	
	public static final IFacade getFacade() {
		return api.getFacade();
	}
	
	public static final Sounds getSounds() {
		return api.getSounds();
	}
	
	public static String translate(Translator key, String... replacements) {
		return api.translate(key, replacements);
	}
	
	public static Iterable<MissionEntry> getMissionEntries(MissionType type, OfflinePlayer player) {
		return api.getMissionEntries(type, player);
	}
	
	public static MissionEntry getMissionEntry(IMission mission, OfflinePlayer player) {
		return api.getMissionEntry(mission, player);
	}
	
	public static Plugin getPlugin() {
		return api.getPlugin();
	}
	
	public static IPlayerStatus getPlayerStatus(OfflinePlayer player) {
		return api.getPlayerStatus(player);
	}
	
	public static QuestingAPI getAPI() {
		return api;
	}
}
