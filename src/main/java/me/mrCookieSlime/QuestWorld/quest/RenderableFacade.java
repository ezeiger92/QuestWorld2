package me.mrCookieSlime.QuestWorld.quest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.ICategoryWrite;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.IQuestWrite;

public class RenderableFacade {
	public Category createCategory(String name, int id) {
		return new Category(name, id);
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
	
	public List<ICategory> load() {
		ArrayList<File> categories = new ArrayList<>();
		Map<Integer, ArrayList<FileConfiguration>> quests = new HashMap<>();
		
		for (File file: new File("plugins/QuestWorld/quests").listFiles()) {
			String fileName = file.getName();
			if (fileName.endsWith(".quest")) {
				int category = Integer.parseInt(fileName.replaceAll("\\d+-C(\\d+)\\.quest", "$1"));
				
				ArrayList<FileConfiguration> files = quests.get(category);
				if (files == null) {
					files = new ArrayList<>();
					quests.put(category, files);
				}

				files.add(YamlConfiguration.loadConfiguration(file));
			}
			else if (fileName.endsWith(".category"))
				categories.add(file);
		}
		
		ArrayList<ICategory> is = new ArrayList<>(categories.size());
		
		for (File file: categories) {
			int id = Integer.parseInt(file.getName().replace(".category", ""));

			new Category(id, YamlConfiguration.loadConfiguration(file), quests.get(id));
		}
		
		for (ICategory category: is) {
			// Administrative process - bypass events and directly modify data
			// 99% of the time you should use .getWriter() and .apply()
			ICategoryWrite c = (ICategoryWrite)category;
			c.refreshParent();
			
			for (IQuest quest: category.getQuests()) {
				IQuestWrite q = (IQuestWrite)quest;
				q.refreshParent();
			}
		}
		
		return is;
	}
}
