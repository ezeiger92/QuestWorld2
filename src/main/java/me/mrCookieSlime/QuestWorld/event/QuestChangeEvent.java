package me.mrCookieSlime.QuestWorld.event;

import org.bukkit.event.HandlerList;

import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.api.contract.IQuestWrite;
import me.mrCookieSlime.QuestWorld.quest.QuestChange;

/**
 * An event fired before applying a set of changes to a quest
 */
public class QuestChangeEvent extends CancellableEvent {
	private QuestChange nextState;

	public QuestChangeEvent(QuestChange nextState) {
		this.nextState = nextState;
	}

	public IQuest getQuest() {
		return nextState.getSource();
	}

	public IQuestWrite getNextState() {
		return nextState;
	}
	
	public boolean hasChange(IQuestWrite.Member field) {
		return nextState.hasChange(field);
	}
	
	// Boilerplate copy/paste from CancellableEvent
	@Override
	public HandlerList getHandlers() { return handlers;	}
	public static HandlerList getHandlerList() { return handlers; }
	private static final HandlerList handlers = new HandlerList();
}
