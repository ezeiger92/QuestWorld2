package me.mrCookieSlime.QuestWorld.hooks.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.utils.EntityTools;
import me.mrCookieSlime.QuestWorld.utils.SubmissionItemResolver;
import me.mrCookieSlime.QuestWorld.utils.Text;

public class KillMission extends MissionType implements Listener {
	public KillMission() {
		super("KILL", true, true, new ItemStack(Material.IRON_SWORD));
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		return SubmissionItemResolver.mobEgg(instance.getEntity());
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		String type = Text.niceName(instance.getEntity().toString());
		return "&7Kill " + instance.getAmount() + "x " + (!instance.acceptsSpawners() ? "naturally spawned " : "") + type;
	}
	
	@EventHandler
	public void onKill(EntityDeathEvent e) {
		Player killer = e.getEntity().getKiller();
		if(killer == null)
			return;

		QuestWorld.getInstance().getManager(killer).forEachTaskOf(this, mission -> {
			return mission.getEntity() == e.getEntityType()
					&& (mission.acceptsSpawners() || !EntityTools.fromSpawner(e.getEntity()));
		});
	}
	
	@Override
	protected void layoutMenu(MissionChange changes) {
		super.layoutMenu(changes);
		putButton(10, MissionButton.entity(changes));
		putButton(11, MissionButton.spawnersAllowed(changes));
		putButton(17, MissionButton.amount(changes));
	}
}
