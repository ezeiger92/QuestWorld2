package me.mrCookieSlime.QuestWorld.hooks.builtin;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.utils.EntityTools;

public class KillNamedMission extends KillMission {
	public KillNamedMission() {
		setName("KILL_NAMED_MOB");
		setSelectorMaterial(new MaterialData(Material.GOLD_SWORD));
	}
	
	@Override
	protected String displayString(IMission instance) {
		return super.displayString(instance) + " named &r" + instance.getName();
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
		
		QuestWorld.getInstance().getManager(killer).forEachTaskOf(this, mission -> {
			return mission.getEntity() == e.getEntityType()
					&& name.equals(mission.getCustomName())
					&& (mission.acceptsSpawners() || !EntityTools.fromSpawner(e.getEntity()));
		});
	}
	
	@Override
	protected void layoutMenu(MissionChange changes) {
		super.layoutMenu(changes);
		putButton(12, MissionButton.entityName(changes));
	}
}
