package me.mrCookieSlime.QuestWorld.hooks.money;

import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestExtension;
import me.mrCookieSlime.QuestWorld.utils.Log;

public class MoneyHook extends QuestExtension {
	
	public static String formatCurrency(String format, int amount) {
		String[] segments = format.split(",");
		String backup = "$";
		
		for(String s : segments) {
			String[] parts = s.split(":", 2);
			if(parts.length == 1)
				backup = parts[0];
			else {
				try{
					int i = Integer.valueOf(parts[0]);
					if(i == amount)
						return parts[1];
				}
				catch(NumberFormatException e) {
					Log.warning("Unknown currency format: \"" + format + "\", expected \"default[,#:override[, ..]]");
				}
			}
		}
		
		return backup;
	}

	private MissionType[] missions = null;
	
	@Override
	public String[] getDepends() {
		return new String[] { "Vault" };
	}

	@Override
	protected void initialize(Plugin parent) {
		if(QuestWorld.getInstance().getEconomy() == null)
			throw new NullPointerException("Economy is required for this extension!");
			
		missions = new MissionType[] {
			new BalanceMission(),
			new PayMission(),
		};
	}

	@Override
	public MissionType[] getMissions() {
		return missions;
	}
}
