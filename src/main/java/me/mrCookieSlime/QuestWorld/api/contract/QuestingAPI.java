package me.mrCookieSlime.QuestWorld.api.contract;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.MissionViewer;
import me.mrCookieSlime.QuestWorld.api.Translator;
import me.mrCookieSlime.QuestWorld.api.annotation.NoImpl;
import me.mrCookieSlime.QuestWorld.api.annotation.Nullable;
import me.mrCookieSlime.QuestWorld.util.Reloadable;
import me.mrCookieSlime.QuestWorld.util.Sounds;
import net.milkbowl.vault.economy.Economy;

@NoImpl
public interface QuestingAPI extends Reloadable {
	/**
	 * Gets the MissionType instance represented by this String. The owning
	 * extension must already be loaded, otherwise <tt>null</tt> will be
	 * returned.
	 * <p> If this method is called in a non-static context, the Builtin
	 * extension and all of its MissionTypes are guaranteed to exist.
	 * 
	 * @param typeName Name of the desired MissionType
	 * @return The found type, or <tt>null</tt>
	 */
	@Nullable <T extends MissionType> T getMissionType(String typeName);
	
	
	Map<String, MissionType> getMissionTypes();
	MissionViewer getViewer();
	Optional<Economy> getEconomy();
	IFacade getFacade();
	Sounds getSounds();
	String translate(Translator key, String... replacements);
	Iterable<MissionEntry> getMissionEntries(MissionType type, OfflinePlayer player);
	MissionEntry getMissionEntry(IMission mission, OfflinePlayer player);
	Plugin getPlugin();
	
	IPlayerStatus getPlayerStatus(OfflinePlayer player);
	IPlayerStatus getPlayerStatus(UUID uuid);
	
	IParty getParty(OfflinePlayer player);
	IParty getParty(UUID uuid);
	
	IParty createParty(OfflinePlayer player);
	IParty createParty(UUID uuid);

	void disbandParty(IParty party);
}
