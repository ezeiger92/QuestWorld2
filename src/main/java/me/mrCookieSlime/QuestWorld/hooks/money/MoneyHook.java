package me.mrCookieSlime.QuestWorld.hooks.money;

import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestExtension;

public class MoneyHook extends QuestExtension {

	private MissionType[] missions = null;
	
	@Override
	public String[] getDepends() {
		return new String[] { "Vault" };
	}

	@Override
	protected void initialize(Plugin parent) {
		missions = new MissionType[] {
				
		};
	}

	@Override
	public MissionType[] getMissions() {
		return missions;
	}
}
