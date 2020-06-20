package com.questworld.extension.builtin;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.questworld.api.Decaying;
import com.questworld.api.MissionType;
import com.questworld.api.QuestWorld;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IMissionState;
import com.questworld.api.contract.MissionEntry;
import com.questworld.api.menu.MissionButton;
import com.questworld.util.EntityTools;

public class KillMission extends MissionType implements Listener, Decaying {
	public KillMission() {
		super("KILL", true);
	}

	@Override
	public ItemStack userDisplayItem(IMission instance) {
		EntityType entity = instance.getEntity();
		return EntityTools.getEntityDisplay(entity).get();
	}

	@Override
	protected String userInstanceDescription(IMission instance) {
		String type = EntityTools.nameOf(instance.getEntity());
		return "&7Kill " + instance.getAmount() + "x " + (!instance.getSpawnerSupport() ? "naturally spawned " : "")
				+ type;
	}

	@EventHandler
	public void onKill(EntityDeathEvent e) {
		Player killer = e.getEntity().getKiller();
		if (killer == null)
			return;

		for (MissionEntry r : QuestWorld.getMissionEntries(this, killer)) {
			IMission mission = r.getMission();
			EntityType type = mission.getEntity();
			if ((type == e.getEntityType() || type == EntityTools.ANY_ENTITY)
					&& (mission.getSpawnerSupport() || !EntityTools.isFromSpawner(e.getEntity())))
				r.addProgress(1);
		}
	}

	@Override
	protected void layoutMenu(IMissionState changes) {
		putButton(10, MissionButton.entity(changes));
		putButton(11, MissionButton.spawnersAllowed(changes));
		putButton(17, MissionButton.amount(changes));
	}
}
