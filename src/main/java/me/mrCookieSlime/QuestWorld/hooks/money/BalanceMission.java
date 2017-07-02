package me.mrCookieSlime.QuestWorld.hooks.money;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Manual;
import me.mrCookieSlime.QuestWorld.api.MissionChange;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.Ticking;
import me.mrCookieSlime.QuestWorld.api.interfaces.IMission;
import me.mrCookieSlime.QuestWorld.api.menu.MenuData;
import me.mrCookieSlime.QuestWorld.api.menu.MissionButton;
import me.mrCookieSlime.QuestWorld.managers.PlayerManager;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;
import me.mrCookieSlime.QuestWorld.utils.Text;
import net.milkbowl.vault.economy.Economy;

public class BalanceMission extends MissionType implements Manual, Ticking {

	private enum CheckType {
		AT_LEAST,
		AT_MOST,
		EXACTLY,
		;
		private static String[] v = null;
		public static String[] stringValues() {
			if(v == null) {
				int len = values().length;
				v = new String[len];
				for(int i = 0; i < len; ++i) {
					v[i] = values()[i].toString();
				}
			}
			
			return v;
		}
		
		public static String stringAt(int ind) {
			if(ind < 0 || ind >= values().length)
				ind = 0;
			
			return Text.niceName(values()[ind].toString());
		}
	}
	
	public BalanceMission() {
		super("HAVE_MONEY", false, false, new MaterialData(Material.GOLD_INGOT));
	}
	
	@Override
	protected String displayString(IMission instance) {
		String currency = "dollars,1:dollar";
		if(instance.getName() != null && instance.getName().length() != 0)
			currency = instance.getName();
		
		currency = MoneyHook.formatCurrency(currency, instance.getAmount());
		String checkType = CheckType.stringAt(instance.getCustomInt());
		
		return "&7Have " + checkType.toLowerCase() + " " + instance.getAmount() + " " + currency;
	}

	@Override
	public ItemStack displayItem(IMission instance) {
		return getSelectorItem().toItemStack(1);
	}
	
	@Override
	protected void layoutMenu(MissionChange changes) {
		int len = CheckType.values().length;
		if(changes.getCustomInt() < 0 || changes.getCustomInt() >= len)
			changes.setCustomInt(0);
		
		putButton(17, MissionButton.amount(changes, 50));
		putButton(11, new MenuData(
				new ItemBuilder(Material.PAPER).display("Check Type:").selector(changes.getCustomInt(), CheckType.stringValues()).get(),
				MissionButton.simpleHandler(changes, event -> {
					int newV = changes.getCustomInt() + (event.isRightClick() ? -1 : 1);
					changes.setCustomInt((newV + len) % len);
				})
		));
		putButton(12, new MenuData(
				new ItemBuilder(Material.NAME_TAG).display("Currency Display").lore(
						" &7" + changes.getName(),
						" &e currency[,1:singular[,2:custom[, ..]]]",
						"",
						"&cLeft Click to set currency format",
						"&cRight Click to remove format").get(),
				MissionButton.simpleHandler(changes, event -> {
					if(event.isRightClick())
						changes.setEntityName(null);
				})
		));
	}

	@Override
	public boolean onTick(PlayerManager manager, IMission mission) {
		Player p = Bukkit.getPlayer(manager.getUUID());
		Economy e = QuestWorld.getInstance().getEconomy();
		if(!e.hasAccount(p))
			return false;
		
		
		return e.getBalance(p) >= mission.getAmount();
	}

	@Override
	public int onManual(PlayerManager manager, IMission mission) {
		if(onTick(manager, mission))
			return mission.getAmount();
		
		return FAIL;
	}
}
