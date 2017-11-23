package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.Decaying;
import me.mrCookieSlime.QuestWorld.api.MissionSet;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.util.EntityTools;
import me.mrCookieSlime.QuestWorld.util.Text;

public class KillMission extends MissionType implements Listener, Decaying {
	public KillMission() {
		super("KILL", true, new ItemStack(Material.IRON_SWORD));
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		EntityType entity = instance.getEntity();
		return EntityTools.getEntityDisplay(entity).get();
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		String type = Text.niceName(instance.getEntity().toString());
		return "&7Kill " + instance.getAmount() + "x " + (!instance.getSpawnerSupport() ? "naturally spawned " : "") + type;
	}
	
	@EventHandler
	public void onKill(EntityDeathEvent e) {
		Player killer = e.getEntity().getKiller();
		if(killer == null)
			return;

		for(MissionSet.Result r : MissionSet.of(this, killer)) {
			IMission mission = r.getMission();
			if(mission.getEntity() == e.getEntityType()
				&& (mission.getSpawnerSupport() || !EntityTools.isFromSpawner(e.getEntity())))
				r.addProgress(1);
		}
	}
	
	@Override
	protected void layoutMenu(IMissionState changes) {
		super.layoutMenu(changes);
		putButton(10, MissionButton.entity(changes));
		putButton(11, MissionButton.spawnersAllowed(changes));
		putButton(17, MissionButton.amount(changes));
	}
}
