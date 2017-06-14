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
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;

public class PagedMapping {
	private final int pageSize;
	private final List<PageList> pages = new ArrayList<>();

	private int activeSize;
	private ItemStack defaultItem = null;
	private MenuClickHandler defaultButton = null;
	
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
	
	public int getCapacity() {
		return pages.size() * pageSize;
	}
	
	private PageList findPage(int index) {
		activeSize = Math.max(1 + (index % pageSize), activeSize);
		
		index /= pageSize;
		while(pages.size() <= index)
			pages.add(new PageList(pageSize, defaultItem, defaultButton));
		
		return pages.get(index);
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
	
	public void removeItem(int index) {
		findPage(index).removeItem(index % pageSize);
	}
	
	public void removeButton(int index) {
		findPage(index).removeButton(index % pageSize);
	}
	
	public void build(ChestMenu menu, int page) {
		if(page < 0 || page >= pages.size())
			return;
		
		pages.get(page).build(menu, 9, activeSize);
		
		//ItemBuilder arrow = new ItemBuilder(Material.SKULL_ITEM);
		
		//menu.addItem(0, arrow.skull("MHF_ArrowLeft").display("&7Prev page").getNew());
		//menu.addItem(2, arrow.skull("MHF_ArrowRight").display("&7Next page").getNew());
		
		ItemBuilder navigation = new ItemBuilder(Material.PAPER).display("&7Page " + (page + 1) + "/" + pages.size());

		String prevColor = page > 0 ? "&c" : "&7&o";
		String nextColor = page < pages.size() - 1 ? "&3" : "&7&o";
		
		navigation.lore(
				nextColor + "Next page (left-click)",
				prevColor + "Prev page (right-click)",
				"&6&oHold shift to jump to the end"
				);

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
				
				//int nextPage = (page + delta + pages.size() + 1) % (pages.size() + 1);
				ChestMenu self = Maps.getInstance().menus.get(p.getUniqueId());
				build(self, nextPage);
				self.reset(true);
				//self.open(p);
				return false;
			}
		});
	}
}
