package me.mrCookieSlime.QuestWorld.hooks.builtin;

import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;

public class KillNamedMission extends KillMission {
	public KillNamedMission() {
		setId("KILL_NAMED_MOB");
		setSelectorMaterial(new MaterialData(Material.GOLD_SWORD));
	}
	
	@Override
	protected String displayString(IMission instance) {
		return super.displayString(instance) + " named &r" + instance.getName();
	}
}
