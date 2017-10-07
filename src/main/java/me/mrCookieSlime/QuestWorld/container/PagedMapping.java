package me.mrCookieSlime.QuestWorld.container;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.menu.Menu;
import me.mrCookieSlime.QuestWorld.util.ItemBuilder;

public class PagedMapping {
	private final ArrayList<Panel> panels = new ArrayList<>(1);
	private final Panel frame = new Panel(9);
	
	private final int pageSize;

	private int currentPage = 0;
	private int activeSize;
	private Consumer<InventoryClickEvent> backButton = null;
	
	public PagedMapping(int cellsPerPanel, int minDisplay) {
		pageSize = cellsPerPanel;
		panels.add(new Panel(pageSize));
		activeSize = minDisplay;
	}
	
	public PagedMapping(int cellsPerPanel) {
		this(cellsPerPanel, cellsPerPanel);
	}
	
	public void setBackButton(Consumer<InventoryClickEvent> button) {
		backButton = button;
	}

	public void reserve(int pages) {
		ListIterator<Panel> it = panels.listIterator(panels.size());
		
		while(it.hasPrevious() && it.previous().getFill() == 0)
			--pages;
		
		while(pages > 0) {
			panels.add(new Panel(pageSize));
			--pages;
		}
	}
	
	public int getCapacity() {
		return panels.size() * pageSize;
	}
	
	private Panel findPanel(int index) {
		activeSize = Math.max(1 + (index % pageSize), activeSize);
		
		index /= pageSize;
		while(panels.size() <= index)
			panels.add(new Panel(pageSize));
		
		return panels.get(index);
	}
	
	public void addButton(int index, ItemStack item, Consumer<InventoryClickEvent> button, boolean isNavButton) {
		findPanel(index).addButton(index % pageSize, item, isNavButton ? event -> {
			QuestWorld.getInstance().getManager((OfflinePlayer) event.getWhoClicked()).putPage(currentPage);
			button.accept(event);
		} : button);
	}

	public void addFrameButton(int index, ItemStack item, Consumer<InventoryClickEvent> button, boolean isNavButton) {
		frame.addButton(index, item, isNavButton ? event -> {
			QuestWorld.getInstance().getManager((OfflinePlayer) event.getWhoClicked()).putPage(currentPage);
			button.accept(event);
		} : button);
	}
	
	public void build(Menu menu, Player p) {
		int page = QuestWorld.getInstance().getManager(p).popPage();
		if(page >= panels.size()) {
			page = 0;
		}
		
		build(menu, page);
	}

	private void build(Menu menu, int page) {
		currentPage = page;
		
		panels.get(page).build(menu, 9, activeSize);
		
		String display = QuestWorld.translate(Translation.NAV_DISPLAY, String.valueOf(page + 1), String.valueOf(panels.size()));

		String nextPre = QuestWorld.translate(page < panels.size() - 1 ? Translation.NAV_NEXT : Translation.NAV_NEXTBAD);
		String prevPre = QuestWorld.translate(page > 0 ? Translation.NAV_PREV : Translation.NAV_PREVBAD);
		String[] lore = QuestWorld.translate(Translation.NAV_LORE, nextPre, prevPre).split("\n");

		frame.addButton(1,
				new ItemBuilder(Material.PAPER).amount(page + 1).display(display).lore(lore).get(),
				event -> {
					int delta = (event.isRightClick() ? -1 : 1) * (event.isShiftClick() ? panels.size() : 1);
					int nextPage = Math.min(Math.max(0, page + delta), panels.size() - 1);
					Player p = (Player) event.getWhoClicked();

					QuestWorld.getSounds().EditorClick().playTo(p); // TODO This is not entirely right - not all PagedMappings are editor menus!
					build(menu, nextPage);
					
					// This logically isn't needed, BUT inventories have a nasty habit of displaying old items when only metadata is
					// changed. Most obvious in the entity selector with spawn eggs. Last checked MC1.12.2
					menu.openFor(p);
				}
		);
		
		if(backButton != null) 
			frame.addButton(0, ItemBuilder.Proto.MAP_BACK.getItem(), backButton);
		
		frame.build(menu);
	}
}
