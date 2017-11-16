package me.mrCookieSlime.QuestWorld.api.contract;

import java.util.Map;

import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.MissionViewer;
import me.mrCookieSlime.QuestWorld.api.Translator;
import me.mrCookieSlime.QuestWorld.util.Sounds;
import net.milkbowl.vault.economy.Economy;

public interface QuestingAPI {
	<T extends MissionType> T getMissionType(String typeName);
	Map<String, MissionType> getMissionTypes();
	MissionViewer getViewer();
	Economy getEconomy();
	IFacade getFacade();
	Sounds getSounds();
	String translate(Translator key, String... replacements);
	Plugin getPlugin();
}
