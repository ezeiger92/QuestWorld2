package me.mrCookieSlime.QuestWorld.extension.builtin;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.contract.IMission;
import me.mrCookieSlime.QuestWorld.api.contract.IMissionState;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.manager.PlayerManager;
import me.mrCookieSlime.QuestWorld.util.EntityTools;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;

public class KillNamedMission extends KillMission {
	private static final int EXACT = 0;
	private static final int CONTAINS = 1;
	/*private static enum MatchType {
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
	}*/
	
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
		
		PlayerManager.of(killer).forEachTaskOf(this, mission -> {
			return mission.getEntity() == e.getEntityType()
					&& (mission.acceptsSpawners() || !EntityTools.isFromSpawner(e.getEntity()))
					&& ((mission.getCustomInt() == EXACT && mission.getCustomString().equals(name))
					|| (mission.getCustomInt() == CONTAINS && mission.getCustomString().contains(name)));
		});
	}
	
	@Override
	protected void layoutMenu(IMissionState changes) {
		super.layoutMenu(changes);
		putButton(12, MissionButton.entityName(changes));
		putButton(16, MissionButton.simpleButton(
				changes,
				new ItemBuilder(Material.BEDROCK).display("&7Match Type")	
				.selector(changes.getSource().getCustomInt(), "Exact", "Contains").get(),
				event -> {
					changes.setCustomInt(1 - changes.getCustomInt());
					if(changes.apply()) {
						
					}
				}
		));
	}
}
