package me.mrCookieSlime.QuestWorld.event;

import org.bukkit.event.HandlerList;

import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.IQuestState;

/**
 * An event fired before applying a set of changes to a quest
 */
public class QuestChangeEvent extends CancellableEvent {
	private IQuestState nextState;

	public QuestChangeEvent(IQuestState nextState) {
		this.nextState = nextState;
	}

	public IQuest getQuest() {
		return nextState.getSource();
	}

	public IQuestState getNextState() {
		return nextState;
	}
	
	public boolean hasChange(IQuestState.Member field) {
		return nextState.hasChange(field);
	}
	
	// Boilerplate copy/paste from CancellableEvent
	@Override
	public HandlerList getHandlers() { return handlers;	}
	public static HandlerList getHandlerList() { return handlers; }
	private static final HandlerList handlers = new HandlerList();
}
