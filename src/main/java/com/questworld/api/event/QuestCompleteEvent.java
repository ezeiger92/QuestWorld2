package com.questworld.api.event;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;

import com.questworld.api.contract.IQuest;

public class QuestCompleteEvent extends CancellableEvent {
	private final IQuest quest;
	private final OfflinePlayer player;

	public QuestCompleteEvent(IQuest quest, OfflinePlayer player) {
		this.quest = quest;
		this.player = player;
	}

	public IQuest getQuest() {
		return quest;
	}

	public OfflinePlayer getPlayer() {
		return player;
	}

	// Boilerplate copy/paste from CancellableEvent
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private static final HandlerList handlers = new HandlerList();
}
