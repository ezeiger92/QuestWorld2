package me.mrCookieSlime.QuestWorld.api.contract;

import me.mrCookieSlime.QuestWorld.api.annotation.NoImpl;

@NoImpl
public interface IStateful {
	default boolean apply() {
		return true;
	}
	
	default boolean discard() {
		return false;
	}
	
	default IStateful getState() {
		return this;
	}
}
