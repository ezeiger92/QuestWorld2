package me.mrCookieSlime.QuestWorld.extensions.citizens;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.CSCoreLibPlugin.PlayerRunnable;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage.HoverAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.CustomBookOverlay;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.MenuHelper;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.MenuHelper.ChatHandler;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.menu.MenuData;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.managers.PlayerManager;
import me.mrCookieSlime.QuestWorld.quests.QuestBook;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;
import me.mrCookieSlime.QuestWorld.utils.PlayerTools;
import me.mrCookieSlime.QuestWorld.utils.Text;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class CitizenAcceptQuestMission extends CitizenInteractMission {
	public CitizenAcceptQuestMission() {
		setName("ACCEPT_QUEST_FROM_NPC");
		this.setSelectorItem(new ItemStack(Material.BOOK_AND_QUILL));
	}

	@Override
	protected String userInstanceDescription(IMission instance) {
		String name = "N/A";
		NPC npc = Citizens.npcFrom(instance);
		if(npc != null)
			name = npc.getName();
		
		return "&7Accept this Quest by talking to " + name;
	}
	
	@Override
	@EventHandler
	public void onInteract(NPCRightClickEvent e) {
		Player p = e.getClicker();
		PlayerManager manager = QuestWorld.getInstance().getManager(p);
		
		QuestWorld.getInstance().getManager(e.getClicker()).forEachTaskOf(this, mission -> {
			if(mission.getCustomInt() != e.getNPC().getId())
				return false;
			
			TellRawMessage lore = new TellRawMessage();
			lore.addText(e.getNPC().getName() + ":\n\n");
			lore.addText(mission.getDescription());
			lore.color(ChatColor.DARK_AQUA);
			lore.addText("\n\n    ");
			lore.addText(Text.colorize("&7( &a&l\u2714 &7)"));
			lore.addHoverEvent(HoverAction.SHOW_TEXT, Text.colorize("&7Click to accept this Quest"));
			lore.addClickEvent(new PlayerRunnable(3) {
				
				@Override
				public void run(Player p) {
					manager.addProgress(mission, mission.getAmount());
				}
			});
			lore.addText("      ");
			lore.addText(Text.colorize("&7( &4&l\u2718 &7)"));
			lore.addHoverEvent(HoverAction.SHOW_TEXT, Text.colorize("&7Click to do this Quest later"));
			lore.addClickEvent(new PlayerRunnable(3) {
				
				@Override
				public void run(Player p) {
				}
			});
			new CustomBookOverlay("Quest", "TheBusyBiscuit", lore).open(p);
			
			// TODO manually handled, should not be
			return false;
		});
	}
	
	@Override
	protected void layoutMenu(MissionChange changes) {
		super.layoutMenu(changes);
		
		List<String> lore = new ArrayList<String>();
		lore.add("");

		// Could be done with .split("?<=\\G.{32}"), but why regex when we don't need to?
		for(int i = 0, len = changes.getDescription().length(); i < len; i += 32) {
			int end = Math.min(i + 32, len);
			lore.add(changes.getDescription().substring(i, end));
		}
		
		lore.add("");
		lore.add("&e> Edit the Quest's Description");
		lore.add("&7(Color Codes are not supported)");
		
		putButton(11, new MenuData(
				new ItemBuilder(Material.NAME_TAG).display("&rQuest Description").lore(lore.toArray(new String[lore.size()])).get(),
				MissionButton.simpleHandler(changes, event -> {
					Player p = (Player)event.getWhoClicked();
					PlayerTools.sendTranslation(p, true, Translation.mission_desc);
					MenuHelper.awaitChatInput(p, new ChatHandler() {
						
						@Override
						public boolean onChat(Player p, String message) {
							changes.getSource().setDescription(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message)));
							QuestBook.openQuestMissionEditor(p, changes.getSource());
							return false;
						}
					});
					p.closeInventory();
				})
		));
	}
}
