package com.questworld.api.context;

import java.util.HashMap;
import java.util.function.Supplier;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.questworld.api.QuestWorld;
import com.questworld.api.contract.DataObject;
import com.questworld.api.contract.ICategory;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IPlayerStatus;
import com.questworld.api.contract.IQuest;

public class ContextBuilder {
	private Context context = new Context();
	
	private final HashMap<String, OfflinePlayer> players = new HashMap<>();
	private final HashMap<String, DataObject> data = new HashMap<>();
	
	private boolean immutable = false;
	
	public ContextBuilder() {
	}
	
	public Context get() {
		immutable = true;
		return new Context.Immutable(context);
	}
	
	public Context getMutable() {
		return context;
	}
	
	private final void prepareContext() {
		if(immutable) {
			context = context.clone();
			immutable = false;
		}
	}
	
	private final void map(String prefix, String key, Supplier<Object> supplier) {
		if(key == null || key.length() == 0) {
			key = prefix;
		}
		else {
			key = prefix + '.' + key;
		}
		
		context.map(key, supplier);
	}
	
	private final void finalInsert(String key, OfflinePlayer player, DataObject dataObject) {
		IPlayerStatus status = QuestWorld.getPlayerStatus(player);
		
		if(dataObject instanceof IMission) {
			IMission mission = (IMission) dataObject;
			
			map(key, null, () -> status.getProgress(mission));
			map(key, "current", () -> status.getProgress(mission));
			map(key, "remaining", () -> mission.getAmount() - status.getProgress(mission));
		}
		else if(dataObject instanceof IQuest) {
			IQuest quest = (IQuest) dataObject;
			
			map(key, null, () -> status.getProgress(quest));
			map(key, "current", () -> status.getProgress(quest));
			map(key, "remaining", () -> quest.getMissions().size() - status.getProgress(quest));
		}
		else /*if(dataObject instanceof ICategory)*/ {
			ICategory category = (ICategory) dataObject;
			
			map(key, null, () -> status.getProgress(category));
			map(key, "current", () -> status.getProgress(category));
			map(key, "remaining", () -> category.getQuests().size() - status.getProgress(category));
		}
	}
	
	private final boolean insertDataProgress(String prefix, DataObject dataObject) {
		DataObject old = data.put(prefix, dataObject);
		
		if(old != null) {
			data.put(prefix, old);
			return false;
		}
		
		for(HashMap.Entry<String, OfflinePlayer> playerEntry : players.entrySet()) {
			String key = prefix + '.' + playerEntry.getKey() + "-progress";
			
			if(!context.has(key)) {
				finalInsert(key, playerEntry.getValue(), dataObject);
			}
		}
		
		return true;
	}
	
	private final boolean insertPlayerProgress(String prefix, OfflinePlayer player) {
		OfflinePlayer old = players.put(prefix, player);
		
		if(old != null) {
			players.put(prefix, old);
			return false;
		}
		
		for(HashMap.Entry<String, DataObject> dataEntry : data.entrySet()) {
			String key = dataEntry.getKey() + '.' + prefix + "-progress";
			
			if(!context.has(key)) {
				finalInsert(key, player, dataEntry.getValue());
			}
		}
		
		return true;
	}
	
	public ContextBuilder withPlayer(OfflinePlayer player) {
		return withPlayer("player", player);
	}
	
	public ContextBuilder withPlayer(String prefix, OfflinePlayer player) {
		prepareContext();
		
		if(!insertPlayerProgress(prefix, player)) {
			return this;
		}
		
		map(prefix, null, player::getName);
		map(prefix, "name", player::getName);
		map(prefix, "uuid", player::getUniqueId);
		
		if(player instanceof Player) {
			Player online = (Player) player;
			map(prefix, "health", online::getHealth);
		}
		
		return this;
	}
	
	public ContextBuilder withCategory(ICategory category) {
		return withCategory("category", category);
	}
	
	public ContextBuilder withCategory(String prefix, ICategory category) {
		prepareContext();
		
		if(!insertDataProgress(prefix, category)) {
			return this;
		}
		
		map(prefix, null, category::getName);
		map(prefix, "name", category::getName);
		map(prefix, "quests", () -> category.getQuests().size());
		
		return this;
	}
	
	public ContextBuilder withQuest(IQuest quest) {
		return withQuest("quest", quest);
	}
	
	public ContextBuilder withQuest(String prefix, IQuest quest) {
		prepareContext();
		
		if(!insertDataProgress(prefix, quest)) {
			return this;
		}
		
		map(prefix, null, quest::getName);
		map(prefix, "name", quest::getName);
		map(prefix, "missions", () -> quest.getMissions().size());
		
		return withCategory(prefix + ".category", quest.getCategory());
	}
	
	public ContextBuilder withMission(IMission mission) {
		return withMission("mission", mission);
	}
	
	public ContextBuilder withMission(String prefix, IMission mission) {
		prepareContext();
		
		if(!insertDataProgress(prefix, mission)) {
			return this;
		}
		
		map(prefix, null, mission::getText);
		map(prefix, "name", mission::getText);
		map(prefix, "amount", mission::getAmount);
		
		return withQuest(prefix + ".quest", mission.getQuest());
	}
	
	public ContextBuilder with(String prefix, Object object) {
		prepareContext();
		
		map(prefix, null, () -> object);
		
		return this;
	}
}
