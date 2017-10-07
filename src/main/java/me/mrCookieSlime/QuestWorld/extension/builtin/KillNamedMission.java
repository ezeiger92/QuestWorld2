package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.util.EntityTools;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;
import me.mrCookieSlime.QuestWorld.util.Text;

public class KillNamedMission extends KillMission {
	private static enum MatchType {
		EXACT,
		CONTAINS,
		;
		public static MatchType at(int index) {
			return values()[index];
		}
		
		public MatchType scroll(int amount) {
			int len = values().length;
			int pos = (ordinal() + amount) % len;
			if(pos < 0)
				pos += len;
			
			return at(pos);
		}
		
		public static String[] stringValues() {
			String[] res = new String[values().length];
			for(MatchType m : values())
				res[m.ordinal()] = m.toString();
			return res;
		}
		
		@Override
		public String toString() {
			return Text.niceName(this.name());
		}
	}
	
	private boolean search(MatchType check, String search, String pile) {
		switch(check) {
		case EXACT:    return pile.equals(search);
		case CONTAINS: return pile.contains(search);
		default:       return false;
		}
	}
	
	public KillNamedMission() {
		setName("KILL_NAMED_MOB");
		setSelectorItem(new ItemStack(Material.GOLD_SWORD));
	}
	
	@Override
	protected String userInstanceDescription(IMission instance) {
		return super.userInstanceDescription(instance) + " named &r" + instance.getCustomString();
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
					&& (mission.acceptsSpawners() || !EntityTools.fromSpawner(e.getEntity()))
					&& search(MatchType.at(mission.getCustomInt()), mission.getCustomString(), name);
		});
	}
	
	@Override
	protected void layoutMenu(MissionChange changes) {
		super.layoutMenu(changes);
		putButton(12, MissionButton.entityName(changes));
		putButton(16, MissionButton.simpleButton(
				changes,
				new ItemBuilder(Material.BEDROCK).display("&7Match Type")	
				.selector(changes.getSource().getCustomInt(), MatchType.stringValues()).get(),
				event -> {
					int delta = 1;
					if(event.isRightClick())
						delta = -1;
					
					changes.setCustomInt(MatchType.at(changes.getCustomInt()).scroll(delta).ordinal());
					if(changes.apply()) {
						
					}
				}
		));
	}
}
