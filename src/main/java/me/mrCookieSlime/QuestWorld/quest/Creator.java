package me.mrCookieSlime.QuestWorld.quest;

import java.io.File;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;

public class Creator {
	public Category createCategory(File file, List<File> quests) {
		return new Category(file, quests);
	}
	
	public Category createCategory(String name, int id) {
		return new Category(name, id);
	}
	
	@Deprecated
	public Quest createQuest(ICategory category, File file) {
		return new Quest((Category)category, file);
	}
	
	public Quest createQuest(String name, String input) {
		return new Quest(name, input);
	}
	
	public Mission createMission(IQuest quest, String id, MissionType type, EntityType entity, String customString,
			ItemStack item, Location location, int amount, String displayName, int timeframe,
			boolean deathReset, int customInt, boolean spawnersAllowed, String description) {
		return new Mission((Quest)quest, id, type, entity, customString, item, location, amount, displayName, timeframe,
				deathReset, customInt, spawnersAllowed, description);
	}
}
