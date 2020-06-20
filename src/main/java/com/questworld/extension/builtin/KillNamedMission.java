package com.questworld.extension.builtin;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.questworld.api.MissionType;
import com.questworld.api.QuestWorld;
import com.questworld.api.contract.IMission;
import com.questworld.api.contract.IMissionState;
import com.questworld.api.contract.MissionEntry;
import com.questworld.api.menu.MissionButton;
import com.questworld.util.EntityTools;
import com.questworld.util.ItemBuilder;

public class KillNamedMission extends MissionType {
	private static final int EXACT = 0;
	private static final int CONTAINS = 1;

	public KillNamedMission() {
		super("KILL_NAMED_MOB", true);
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
				+ type + " named &r" + instance.getCustomString();
	}

	@Override
	public void validate(IMissionState state) {
		if (state.getCustomString().length() == 0) {
			state.setCustomString("Jerry");
			state.apply();
		}
	}

	@EventHandler
	public void onKill(EntityDeathEvent e) {
		Player killer = e.getEntity().getKiller();
		if (killer == null)
			return;

		String name;
		if (e.getEntityType() == EntityType.PLAYER)
			name = e.getEntity().getName();
		else
			name = e.getEntity().getCustomName();

		if (name == null)
			return;

		for (MissionEntry r : QuestWorld.getMissionEntries(this, killer)) {
			IMission mission = r.getMission();
			EntityType type = mission.getEntity();
			if ((type == e.getEntityType() || type == EntityTools.ANY_ENTITY)
					&& (mission.getSpawnerSupport() || !EntityTools.isFromSpawner(e.getEntity()))
					&& (mission.getCustomInt() == EXACT && name.equals(mission.getCustomString())
							|| mission.getCustomInt() == CONTAINS && name.contains(mission.getCustomString())))
				r.addProgress(1);
		}
	}

	@Override
	protected void layoutMenu(IMissionState changes) {
		putButton(10, MissionButton.entity(changes));
		putButton(11, MissionButton.spawnersAllowed(changes));
		putButton(17, MissionButton.amount(changes));
		putButton(12, MissionButton.entityName(changes));
		putButton(16, MissionButton.simpleButton(changes,
				new ItemBuilder(QuestWorld.getIcons().editor.set_match_type)
						.display("&7Name match type")
						.selector(changes.getCustomInt(), "Exact", "Contains").get(),
				event -> {
					changes.setCustomInt(1 - changes.getCustomInt());
				}
		));
	}
}
