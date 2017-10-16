package me.mrCookieSlime.QuestWorld.quest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;

public class RenderableFacade {
	public Category createCategory(String name, int id) {
		return new Category(name, id);
	}
	
	public Quest createQuest(String name, int id, ICategory category) {
		return new Quest(name, id, (Category)category);
	}
	
	/*public Mission createMission(IQuest quest, String id, MissionType type, EntityType entity, String customString,
			ItemStack item, Location location, int amount, String displayName, int timeframe,
			boolean deathReset, int customInt, boolean spawnersAllowed, String description) {
		return new Mission((Quest)quest, id, type, entity, customString, item, location, amount, displayName, timeframe,
				deathReset, customInt, spawnersAllowed, description);
	}*/
	
	public Mission createMission(IQuest quest, int id) {
		return new Mission(String.valueOf(id), (Quest)quest);
	}
	
	static int[] splitQuestString(String in) {
		int result[] = {0, 0};
		
		int len = in.length();
		int mid = in.lastIndexOf('C', len - 2);
		
		result[0] = Integer.parseInt(in.substring(0, mid - 1));
		result[1] = Integer.parseInt(in.substring(mid + 1, len));
		
		return result;
	}
	
	private class ParseData {
		public ParseData(int id, File file) {
			this.id = id;
			this.file = YamlConfiguration.loadConfiguration(file);
		}
		
		public final int id;
		public final YamlConfiguration file;
	}
	
	public void load() {
		ArrayList<ParseData> categoryData = new ArrayList<>();
		HashMap<Integer, ArrayList<ParseData>> questData = new HashMap<>();
		
		for (File file: QuestWorld.getPath("data.questing").listFiles()) {
			String fileName = file.getName();
			if (fileName.endsWith(".quest")) {
				int[] parts = splitQuestString(fileName.substring(0, fileName.length() - 6));
				
				ArrayList<ParseData> files = questData.get(parts[1]);
				if (files == null) {
					files = new ArrayList<>();
					questData.put(parts[1], files);
				}

				files.add(new ParseData(parts[0], file));
			}
			else if (fileName.endsWith(".category"))
				categoryData.add(new ParseData(Integer.parseInt(fileName.substring(0, fileName.length() - 9)), file));
		}
		
		ArrayList<ICategory> categories = new ArrayList<>(categoryData.size());
		
		for (ParseData cData: categoryData) {
			Category category = new Category(cData.id, cData.file);
			categories.add(category);
			
			for (ParseData qData: questData.get(cData.id))
				new Quest(qData.id, qData.file, category);
		}
		
		for (ICategory category: categories) {
			// Administrative process - bypass events and directly modify data
			// 99% of the time you should use .getWriter() and .apply()
			((Category)category).refreshParent();
			
			for (IQuest quest: category.getQuests())
				((Quest)quest).refreshParent();
		}
	}
	
	public void deleteQuestFile(IQuest quest) {
		((Quest)quest).getFile().delete();
	}
	
	public void deleteCategoryFile(ICategory category) {
		((Category)category).getFile().delete();
	}
}
