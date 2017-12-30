package me.mrCookieSlime.QuestWorld.quest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;

import me.mrCookieSlime.QuestWorld.QuestWorldPlugin;
import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.contract.ICategory;
import me.mrCookieSlime.QuestWorld.api.contract.IFacade;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.manager.PlayerStatus;
import me.mrCookieSlime.QuestWorld.manager.ProgressTracker;
import me.mrCookieSlime.QuestWorld.util.WeakValueMap;

public class Facade implements IFacade {
	private long lastSave;
	private HashMap<Integer, Category> categoryMap = new HashMap<>();
	private WeakValueMap<Long, Quest> questMap = new WeakValueMap<>();
	private WeakValueMap<Long, Mission> missionMap = new WeakValueMap<>();
	
	@Deprecated
	public Quest getQuest(long unique) {
		return questMap.getOrNull(unique);
	}
	
	@Deprecated
	public Mission getMission(long unique) {
		return missionMap.getOrNull(unique);
	}
	
	@Override
	public Category createCategory(String name, int id) {
		Category c = new Category(name, id, this);
		categoryMap.put(id, c);
		return c;
	}
	
	
	public Quest createQuest(String name, int id, ICategory category) {
		Quest q = new Quest(name, id, (Category)category);
		questMap.putWeak(q.getUnique(), q);
		return q;
	}
	
	public Mission createMission(int id, IQuest quest) {
		Mission m = new Mission(id, (Quest)quest);
		ProgressTracker.loadDialogue(m);
		missionMap.putWeak(m.getUnique(), m);
		return m;
	}
	
	static File fileFor(ICategory category) {
		return new File(QuestWorldPlugin.getPath("data.questing"), category.getID() + ".category");
	}
	
	static File fileFor(IQuest quest) {
		return new File(QuestWorldPlugin.getPath("data.questing"), stringOfQuest(quest) + ".quest");
	}
	
	private static int[] splitQuestString(String in) {
		int result[] = {0, 0};
		
		int len = in.length();
		int mid = in.lastIndexOf('C', len - 2);
		
		result[0] = Integer.parseInt(in.substring(0, mid - 1));
		result[1] = Integer.parseInt(in.substring(mid + 1, len));

		return result;
	}
	
	static Quest questOfString(String in) {
		if(in == null)
			return null;
		
		int[] parts = splitQuestString(in);
		ICategory c = QuestWorld.getFacade().getCategory(parts[1]);
		if (c != null)
			return (Quest)c.getQuest(parts[0]);
		
		return null;
	}
	
	static String stringOfQuest(IQuest quest) {
		if(quest == null)
			return null;
		
		return quest.getID() + "-C" + quest.getCategory().getID();
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
		
		ArrayList<Category> categories = new ArrayList<>(categoryData.size());
		
		for (ParseData cData: categoryData) {
			Category category = new Category(cData.id, cData.file, this);
			categories.add(category);
			
			ArrayList<ParseData> elements = questData.get(cData.id);
			
			if(elements != null)
				for (ParseData qData: questData.get(cData.id)) {
					Quest q = new Quest(qData.id, qData.file, category);
					category.directAddQuest(q);
					questMap.putWeak(q.getUnique(), q);
				}
			
			categoryMap.put(category.getID(), category);
		}
		
		for (Category category: categories) {
			category.refreshParent();
			
			for (Quest quest: category.getQuests())
				quest.refreshParent();
		}
	}
	
	public void unload() {
		categoryMap.clear();
		questMap.clear();
		missionMap.clear();
	}
	
	private void deleteQuestFile(IQuest quest) {
		fileFor(quest).delete();
	}
	
	private void deleteCategoryFile(ICategory category) {
		fileFor(category).delete();
	}
	
	public void save(boolean force) {
		for(Category c : categoryMap.values()) {
			c.save(force);
			for(Quest q : c.getQuests())
				for(Mission m : q.getMissions())
					ProgressTracker.saveDialogue(m);
		}
		lastSave = System.currentTimeMillis();
	}
	
	public void onReload() {
		for(Category c: categoryMap.values())
			for(Quest q : c.getQuests())
				for(Mission m : q.getMissions())
					ProgressTracker.loadDialogue(m);
	}
	
	@Override
	public long getLastSave() {
		return lastSave;
	}
	
	@Override
	public Collection<Category> getCategories() {
		return categoryMap.values();
	}
	
	@Override
	public Category getCategory(int id) {
		return categoryMap.get(id);
	}

	@Override
	public void deleteCategory(ICategory category) {
		for (IQuest quest: category.getQuests())
			deleteQuest(quest);
		
		categoryMap.remove(category.getID());
		deleteCategoryFile(category);
	}
	
	@Override
	public void deleteQuest(IQuest quest) {
		for(IMission mission : quest.getMissions())
			deleteMission(mission);
		
		PlayerStatus.clearAllQuestData(quest);
		questMap.remove(((Quest)quest).getUnique());
		deleteQuestFile(quest);
	}
	
	@Override
	public void deleteMission(IMission mission) {
		missionMap.remove(((Mission)mission).getUnique());
	}
	
	public void clearAllUserData(Category category) {
		for(Quest quest : category.getQuests())
			clearAllUserData(quest);
	}
	
	public void clearAllUserData(Quest quest) {
		PlayerStatus.clearAllQuestData(quest);
	}
}
