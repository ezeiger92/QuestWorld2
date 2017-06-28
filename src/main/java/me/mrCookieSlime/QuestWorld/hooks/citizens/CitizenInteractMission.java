package me.mrCookieSlime.QuestWorld.hooks.citizens;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.menu.MenuData;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class CitizenInteractMission extends MissionType implements Listener {
	public CitizenInteractMission() {
		super("CITIZENS_INTERACT", false, false, new MaterialData(Material.ITEM_FRAME));
	}
	
	@Override
	public ItemStack displayItem(IMission instance) {
		return getSelectorItem().toItemStack(1);
	}
	
	@Override
	protected String displayString(IMission instance) {
		String name = "N/A";
		NPC npc = CitizensHook.npcFrom(instance);
		if(npc != null)
			name = npc.getName();
		
		return "&7Talk to " + name;
	}
	
	@EventHandler
	public void onInteract(NPCRightClickEvent e) {
		QuestWorld.getInstance().getManager(e.getClicker()).forEachTaskOf(this, mission -> {
			return mission.getCustomInt() == e.getNPC().getId();
		});
	}
	
	@Override
	protected void layoutMenu(MissionChange changes) {
		super.layoutMenu(changes);
		NPC npc = CitizensHook.npcFrom(changes);
		putButton(10, new MenuData(
				new ItemBuilder(Material.NAME_TAG).display("&dCitizen &f#" + changes.getCustomInt()).lore(
						"&7Name: &r" + (npc != null ? npc.getName(): "&4N/A"),
						"",
						"&e> Click to change the selected NPC").get(),
				MissionButton.simpleHandler(changes, event -> {
					Player p = (Player)event.getWhoClicked();
					PlayerTools.sendTranslation(p, true, CitizenTranslation.citizen_l);
					CitizensHook.link.put(p.getUniqueId(), changes.getSource());
					p.closeInventory();
				})
		));
	}
}
