package me.mrCookieSlime.QuestWorld.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.menu.Menu;
import me.mrCookieSlime.QuestWorld.managers.MenuManager;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;

public class PagedMapping {
	private int currentPage = 0;
	private final int pageSize;
	private final List<PageList> pages = new ArrayList<>();

	private int activeSize;
	private ItemStack defaultItem = null;
	private Consumer<InventoryClickEvent> defaultButton = null;
	
	private Consumer<InventoryClickEvent> backButton = null;
	
	public PagedMapping(int elementsPerPage) {
		pageSize = elementsPerPage;
		activeSize = elementsPerPage;
	}
	
	public PagedMapping(int elementsPerPage, int activeSize) {
		this(elementsPerPage);
		this.activeSize = activeSize;
	}
	
	public List<PageList> getPages() {
		return pages;
	}
	
	public void setDefaultItem(ItemStack item) {
		defaultItem = ItemBuilder.clone(item);
		for(PageList page : pages)
			page.setDefaultItem(item);
	}
	
	public void setDefaultButton(Consumer<InventoryClickEvent> button) {
		defaultButton = button;
		for(PageList page : pages)
			page.setDefaultButton(button);
	}
	
	public void setBackButton(Consumer<InventoryClickEvent> button) {
		backButton = button;
	}
	
	public int getCapacity() {
		return pages.size() * pageSize;
	}
	
	public int getPageCapacity() {
		return pageSize;
	}
	
	private PageList findPage(int index) {
		activeSize = Math.max(1 + (index % pageSize), activeSize);
		
		index /= pageSize;
		while(pages.size() <= index)
			pages.add(new PageList(pageSize, defaultItem, defaultButton));
		
		return pages.get(index);
	}
	
	public void touch(int index) {
		findPage(index);
	}
	
	public void touchPage(int page) {
		findPage(page * pageSize);
	}
	
	public ItemStack getItem(int index) {
		return findPage(index).getItem(index % pageSize);
	}
	
	public Consumer<InventoryClickEvent> getButton(int index) {
		return findPage(index).getButton(index % pageSize);
	}
	
	public void addButton(int index, ItemStack item, Consumer<InventoryClickEvent> button) {
		findPage(index).addButton(index % pageSize, item, button);
	}
	
	// TODO this is all to handle party buttons NOT being part of the nav frame
	boolean[] hacked = {
			false,false,false,
			false,false,false,
			false,false,false,
	};
	public void hackNav(int index) {
		hacked[index] = true;
	}
	
	public void addNavButton(int index, ItemStack item, Consumer<InventoryClickEvent> button) {

		findPage(index).addButton(index % pageSize, item, event -> {
			QuestWorld.getInstance().getManager((OfflinePlayer) event.getWhoClicked()).putPage(currentPage);
			button.accept(event);
		});
	}
	
	public void removeItem(int index) {
		findPage(index).removeItem(index % pageSize);
	}
	
	public void removeButton(int index) {
		findPage(index).removeButton(index % pageSize);
	}
	
	public void build(Menu menu, Player p) {
		int page = QuestWorld.getInstance().getManager(p).popPage();
		build(menu, page);
	}

	private void build(Menu menu, int page) {
		if(page < 0 || page >= pages.size())
			return;
		currentPage = page;
		
		// TODO same as above, hack made for party button
		for(int i = 0; i < 9; ++i) {
			if(hacked[i]) {
				Consumer<InventoryClickEvent> old = menu.getHandler(i);
				menu.put(i, menu.getItem(i), event -> {
					QuestWorld.getInstance().getManager((Player) event.getWhoClicked()).clearPages();
					old.accept(event);
				});
			}
		}
		
		pages.get(page).build(menu, 9, activeSize);
		
		String display = QuestWorld.translate(Translation.nav_display, String.valueOf(page + 1), String.valueOf(pages.size()));

		String nextPre = QuestWorld.translate(page < pages.size() - 1 ? Translation.nav_next : Translation.nav_nextbad);
		String prevPre = QuestWorld.translate(page > 0 ? Translation.nav_prev : Translation.nav_prevbad);
		String[] lore = QuestWorld.translate(Translation.nav_lore, nextPre, prevPre).split("\n");
		
		ItemBuilder navigation = new ItemBuilder(Material.PAPER)
				.amount(page + 1)
				.display(display).lore(lore);

		menu.put(1, navigation.getNew(), event -> {
			int delta = 1;
			if(event.isRightClick())
				delta = -1;
			
			if(event.isShiftClick())
				delta *= pages.size();
			
			int nextPage = Math.max(Math.min(pages.size() - 1, page + delta), 0);
			Player p = (Player) event.getWhoClicked();
			
			Menu self = MenuManager.get().playerOpenMenus.get(p.getUniqueId());
			build(self, nextPage);
			self.openFor(p); // This isn't really needed, but it forces items to appear correct --- TODO NOW LEGACY, CHECK
		});
		
		if(backButton != null) 
			menu.put(0, ItemBuilder.Proto.MAP_BACK.getItem(), backButton);
	}
}
