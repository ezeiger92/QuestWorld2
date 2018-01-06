package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.api.contract.MissionEntry;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.util.EntityTools;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;

public class KillNamedMission extends KillMission {
	private static final int EXACT = 0;
	private static final int CONTAINS = 1;
	
	public KillNamedMission() {
		setName("KILL_NAMED_MOB");
		setSelectorItem(new ItemStack(Material.GOLD_SWORD));
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		return super.userInstanceDescription(instance) + " named &r" + instance.getCustomString();
	}
	
	@Override
	public void validate(IMissionState state) {
		if(state.getCustomString().length() == 0)
			state.setCustomString("Jerry");
		
		state.apply();
	}
	
	@Override
	@EventHandler
	public void onKill(EntityDeathEvent e) {
		Player killer = e.getEntity().getKiller();
		if(killer == null)
			return;

		String name;
		if(e.getEntityType() == EntityType.PLAYER)
			name = e.getEntity().getName();
		else
			name = e.getEntity().getCustomName();
		
		if(name == null)
			return;
		
		for(MissionEntry r : QuestWorld.getMissionEntries(this, killer)) {
			IMission mission = r.getMission();
			EntityType type = mission.getEntity();
			if((type == e.getEntityType() || type == EntityType.COMPLEX_PART)
				&& (mission.getSpawnerSupport() || !EntityTools.isFromSpawner(e.getEntity()))
				&& (mission.getCustomInt() == EXACT && name.equals(mission.getCustomString())
					|| mission.getCustomInt() == CONTAINS && name.contains(mission.getCustomString())))
				r.addProgress(1);
		}
	}
	
	@Override
	protected void layoutMenu(IMissionState changes) {
		super.layoutMenu(changes);
		putButton(12, MissionButton.entityName(changes));
		putButton(16, MissionButton.simpleButton(
				changes,
				new ItemBuilder(Material.GOLDEN_APPLE).display("&7Name match type")	
				.selector(changes.getCustomInt(), "Exact", "Contains").get(),
				event -> {
					changes.setCustomInt(1 - changes.getCustomInt());
				}
		));
	}
}
