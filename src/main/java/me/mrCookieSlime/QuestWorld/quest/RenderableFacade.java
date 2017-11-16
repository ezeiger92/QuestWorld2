package me.mrCookieSlime.QuestWorld.quest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;

import me.mrCookieSlime.QuestWorld.QuestWorldPlugin;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.IFacade;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.container.WeakValueMap;
import me.mrCookieSlime.QuestWorld.manager.PlayerManager;

public class RenderableFacade implements IFacade {
	private long lastSave;
	private HashMap<Integer, Category> categories = new HashMap<>();
	private WeakValueMap<Integer, Quest> quests = new WeakValueMap<>();
	private WeakValueMap<Integer, Mission> missions = new WeakValueMap<>();
	
	@Deprecated
	public Quest getQuest(int id) {
		return quests.getOrNull(id);
	}
	
	@Deprecated
	public Mission getMission(int id) {
		return missions.getOrNull(id);
	}
	
	@Override
	public Category createCategory(String name, int id) {
		return new Category(name, id);
	}
	
	@Override
	public Quest createQuest(String name, int id, ICategory category) {
		return new Quest(name, id, (Category)category);
	}
	
	@Override
	public Mission createMission(int id, IQuest quest) {
		return new Mission(id, (Quest)quest);
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
		
		for (File file: QuestWorldPlugin.getPath("data.questing").listFiles()) {
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
			// 99% of the time you should use .getState() and .apply()
			((Category)category).refreshParent();
			
			for (IQuest quest: category.getQuests())
				((Quest)quest).refreshParent();
		}
	}
	
	public void unload() {
		categories.clear();
	}
	
	public void deleteQuestFile(IQuest quest) {
		((Quest)quest).getFile().delete();
	}
	
	public void deleteCategoryFile(ICategory category) {
		((Category)category).getFile().delete();
	}
	
	public void save(boolean force) {
		for(Category c : categories.values()) {
			c.save(force);
		}
		lastSave = System.currentTimeMillis();
	}
	
	@Override
	public long getLastSave() {
		return lastSave;
	}
	
	@Override
	public Collection<Category> getCategories() {
		return categories.values();
	}
	
	@Override
	public Category getCategory(int id) {
		return categories.get(id);
	}

	@Override
	public void registerCategory(ICategory category) {
		categories.put(category.getID(), (Category)category);
	}

	@Override
	public void unregisterCategory(ICategory category) {
		for (IQuest quest: category.getQuests()) {
			PlayerManager.clearAllQuestData(quest);
			deleteQuestFile(quest);
		}
		categories.remove(category.getID());
		deleteCategoryFile(category);
	}
}
