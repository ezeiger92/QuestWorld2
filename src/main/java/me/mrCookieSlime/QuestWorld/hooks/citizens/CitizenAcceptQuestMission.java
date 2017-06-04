package me.mrCookieSlime.QuestWorld.hooks.citizens;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;
import net.citizensnpcs.api.npc.NPC;

public class CitizenAcceptQuestMission extends MissionType {
	public CitizenAcceptQuestMission() {
		super("ACCEPT_QUEST_FROM_NPC", false, false, false, SubmissionType.CITIZENS_INTERACT,
				new ItemBuilder(Material.SKULL_ITEM).skull(SkullType.PLAYER).get().getData());
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return getSelectorItem().toItemStack(1);
	}
	
	@Override
	protected String displayString(IMission instance) {
		String name = "N/A";
		NPC npc = CitizensHook.npcFrom(instance);
		if(npc != null)
			name = npc.getName();
		
		return "&7Accept this Quest by talking to " + name;
	}
}
