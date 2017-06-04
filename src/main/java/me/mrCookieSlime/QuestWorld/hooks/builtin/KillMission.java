package me.mrCookieSlime.QuestWorld.hooks.builtin;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.utils.SubmissionItemResolver;
import me.mrCookieSlime.QuestWorld.utils.Text;

public class KillMission extends MissionType {
	public KillMission() {
		super("KILL", true, true, false, SubmissionType.ENTITY, new MaterialData(Material.IRON_SWORD));
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return SubmissionItemResolver.mobEgg(instance.getEntity());
	}
	
	@Override
	protected String displayString(IMission instance) {
		String type = Text.niceName(instance.getEntity().toString());
		return "&7Kill " + instance.getAmount() + "x " + (instance.acceptsSpawners() ? "naturally spawned " : "") + type;
	}
}
