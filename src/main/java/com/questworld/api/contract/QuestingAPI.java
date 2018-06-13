package com.questworld.api.contract;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.questworld.api.MissionType;
import com.questworld.api.MissionViewer;
import com.questworld.api.Translator;
import com.questworld.api.annotation.NoImpl;
import com.questworld.api.annotation.Nullable;
import com.questworld.util.Reloadable;
import com.questworld.util.Sounds;

import net.milkbowl.vault.economy.Economy;

@NoImpl
public interface QuestingAPI extends Reloadable {
	/**
	 * Gets the MissionType instance represented by this String. The owning
	 * extension must already be loaded, otherwise <tt>null</tt> will be returned.
	 * <p>
	 * If this method is called in a non-static context, the Builtin extension and
	 * all of its MissionTypes are guaranteed to exist.
	 * 
	 * @param typeName Name of the desired MissionType
	 * @return The found type, or <tt>null</tt>
	 */
	@Nullable
	<T extends MissionType> T getMissionType(String typeName);

	Map<String, MissionType> getMissionTypes();

	MissionViewer getViewer();

	Optional<Economy> getEconomy();

	IFacade getFacade();

	Sounds getSounds();

	String translate(Translator key, String... replacements);

	String translate(Player player, Translator key, String... replacements);

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
