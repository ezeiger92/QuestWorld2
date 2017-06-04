package me.mrCookieSlime.QuestWorld.hooks.citizens;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;
import net.citizensnpcs.api.npc.NPC;

public class CitizenSubmitMission extends MissionType {
	public CitizenSubmitMission() {
		super("CITIZENS_SUBMIT", false, false, false, SubmissionType.CITIZENS_ITEM,
				new ItemBuilder(Material.SKULL_ITEM).skull(SkullType.PLAYER).get().getData());
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return new ItemBuilder(Material.SKULL_ITEM).skull(SkullType.PLAYER).get();
	}
	
	@Override
	protected String displayString(IMission instance) {
		String name = "N/A";
		NPC npc = CitizensHook.npcFrom(instance);
		if(npc != null)
			name = npc.getName();
		
		return "&7Give " + instance.getAmount() + "x " + StringUtils.formatItemName(instance.getDisplayItem(), false) + " to " + name;
	}
}
