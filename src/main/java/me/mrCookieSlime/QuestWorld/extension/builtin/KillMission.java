package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionWrite;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.util.EntityTools;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.Text;

public class KillMission extends MissionType implements Listener {
	public KillMission() {
		super("KILL", true, true, new ItemStack(Material.IRON_SWORD));
	}
	
	@Override
	public ItemStack userDisplayItem(IMission instance) {
		EntityType entity = instance.getEntity();
		return  new ItemBuilder(EntityTools.getEntityDisplay(entity))
				.display("&7Entity Type: &r" + Text.niceName(entity.name())).get();
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
	protected void layoutMenu(IMissionWrite changes) {
		super.layoutMenu(changes);
		putButton(10, MissionButton.entity(changes));
		putButton(11, MissionButton.spawnersAllowed(changes));
		putButton(17, MissionButton.amount(changes));
	}
}
