package me.mrCookieSlime.QuestWorld.hooks.builtin;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.utils.SubmissionItemResolver;
import me.mrCookieSlime.QuestWorld.utils.Text;

public class KillNamedMission extends MissionType {
	public KillNamedMission() {
		super("KILL_NAMED_MOB", true, true, false, SubmissionType.ENTITY, new MaterialData(Material.GOLD_SWORD));
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return SubmissionItemResolver.mobEgg(instance.getEntity());
	}
	
	@Override
	protected String displayString(IMission instance) {
		String type = Text.niceName(instance.getEntity().toString());
		return "&7Kill " + instance.getAmount() + "x " + (instance.acceptsSpawners() ? "naturally spawned " : "") + type + " named &r" + instance.getName();
	}
}
