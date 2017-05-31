package me.mrCookieSlime.QuestWorld.quests.pluginmissions;

import org.bukkit.Material;
import org.bukkit.SkullType;

import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class CitizenSubmitMission extends MissionType {
	public CitizenSubmitMission() {
		super("CITIZENS_SUBMIT", false, false, false, SubmissionType.CITIZENS_ITEM,
				new ItemBuilder(Material.SKULL_ITEM).skull(SkullType.PLAYER).get().getData());
	}
	@Override
	protected String formatMissionDisplay(QuestMission instance) {
		String name = "N/A";
		NPC npc = CitizensAPI.getNPCRegistry().getById(instance.getCitizenID());
		if(npc != null)
			name = npc.getName();
		
		return "&7Give " + instance.getAmount() + "x " + StringUtils.formatItemName(instance.getDisplayItem(), false) + " to " + name;
	}
}
