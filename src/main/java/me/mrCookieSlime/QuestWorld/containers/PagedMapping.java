package me.mrCookieSlime.QuestWorld.containers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Maps;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;
import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;

public class PagedMapping {
	private int currentPage = 0;
	private final int pageSize;
	private final List<PageList> pages = new ArrayList<>();

	private int activeSize;
	private ItemStack defaultItem = null;
	private MenuClickHandler defaultButton = null;
	
	private MenuClickHandler backButton = null;
	
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
	
	public void setDefaultButton(MenuClickHandler button) {
		defaultButton = button;
		for(PageList page : pages)
			page.setDefaultButton(button);
	}
	
	public void setBackButton(MenuClickHandler button) {
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
	
	public MenuClickHandler getButton(int index) {
		return findPage(index).getButton(index % pageSize);
	}
	
	public void addItem(int index, ItemStack item) {
		findPage(index).addItem(index % pageSize, item);
	}
	
	public void addButton(int index, MenuClickHandler button) {
		findPage(index).addButton(index % pageSize, button);
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
	
	public void addNavButton(int index, MenuClickHandler button) {

		findPage(index).addButton(index % pageSize, new MenuClickHandler() {

			@Override
			public boolean onClick(Player arg0, int arg1, ItemStack arg2, ClickAction arg3) {
				QuestWorld.getInstance().getManager(arg0).putPage(currentPage);
				return button.onClick(arg0, arg1, arg2, arg3);
			}
			
		});
	}
	
	public void removeItem(int index) {
		findPage(index).removeItem(index % pageSize);
	}
	
	public void removeButton(int index) {
		findPage(index).removeButton(index % pageSize);
	}
	
	public void build(ChestMenu menu, Player p) {
		int page = QuestWorld.getInstance().getManager(p).popPage();
		build(menu, page);
	}

	private void build(ChestMenu menu, int page) {
		if(page < 0 || page >= pages.size())
			return;
		currentPage = page;
		
		// TODO same as above, hack made for party button
		for(int i = 0; i < 9; ++i) {
			if(hacked[i]) {
				MenuClickHandler old = menu.getMenuClickHandler(i);
				menu.addMenuClickHandler(i, new MenuClickHandler() {

					@Override
					public boolean onClick(Player arg0, int arg1, ItemStack arg2, ClickAction arg3) {
						QuestWorld.getInstance().getManager(arg0).clearPages();
						return old.onClick(arg0, arg1, arg2, arg3);
					}
					
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

		menu.addItem(1, navigation.getNew());
		menu.addMenuClickHandler(1, new MenuClickHandler() {
			
			@Override
			public boolean onClick(Player p, int slot, ItemStack item, ClickAction action) {
				int delta = 1;
				if(action.isRightClicked())
					delta = -1;
				
				if(action.isShiftClicked())
					delta *= pages.size();
				
				int nextPage = Math.max(Math.min(pages.size() - 1, page + delta), 0);
				
				ChestMenu self = Maps.getInstance().menus.get(p.getUniqueId());
				build(self, nextPage);
				self.reset(true);
				self.open(p); // This isn't really needed, but it forces items to appear correct
				return false;
			}
		});
		
		if(backButton != null) 
			menu.addItem(0, ItemBuilder.Proto.MAP_BACK.getItem(), backButton);
	}
}
