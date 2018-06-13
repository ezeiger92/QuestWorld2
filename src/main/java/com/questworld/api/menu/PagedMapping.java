package com.questworld.api.menu;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Stack;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;

import com.questworld.api.QuestWorld;
import com.questworld.api.Translation;
import com.questworld.util.ItemBuilder;

public class PagedMapping {
	private final ArrayList<Panel> panels = new ArrayList<>(1);
	private final Panel frame = new Panel(9);

	private final int pageSize;

	private int currentPage = 0;
	private int activeSize;
	private String backLabel = "";
	private Consumer<InventoryClickEvent> backButton = null;

	@SuppressWarnings("unchecked")
	private static Stack<Integer> stackFor(Metadatable player) {
		Stack<Integer> result;
		try {
			result = (Stack<Integer>) player.getMetadata("questworld.pages").get(0).value();
		}
		catch (IndexOutOfBoundsException e) {
			result = new Stack<Integer>();
			player.setMetadata("questworld.pages", new FixedMetadataValue(QuestWorld.getPlugin(), result));
		}
		return result;
	}

	public static void putPage(Metadatable player, int page) {
		stackFor(player).push(page);
	}

	public static int popPage(Metadatable player) {
		Stack<Integer> stack = stackFor(player);
		if (stack.isEmpty())
			return 0;

		return stack.pop();
	}

	public static void clearPages(Metadatable player) {
		stackFor(player).clear();
	}

	public PagedMapping(int cellsPerPanel, int minDisplay) {
		pageSize = cellsPerPanel;
		panels.add(new Panel(pageSize));
		activeSize = minDisplay;
	}

	public PagedMapping(int cellsPerPanel) {
		this(cellsPerPanel, cellsPerPanel);
	}

	public void setBackButton(String label, Consumer<InventoryClickEvent> button) {
		backLabel = label != null ? label : "";
		backButton = button;
	}

	public void reserve(int pages) {
		ListIterator<Panel> it = panels.listIterator(panels.size());

		while (it.hasPrevious() && it.previous().getFill() == 0)
			--pages;

		while (pages > 0) {
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
		while (panels.size() <= index)
			panels.add(new Panel(pageSize));

		return panels.get(index);
	}

	public boolean hasButton(int index) {
		if (index / pageSize >= panels.size() || index < 0)
			return false;

		return findPanel(index).getItem(index % pageSize) != null;
	}

	public void addButton(int index, ItemStack item, Consumer<InventoryClickEvent> button, boolean isNavButton) {
		findPanel(index).addButton(index % pageSize, item, isNavButton ? event -> {
			putPage(event.getWhoClicked(), currentPage);
			button.accept(event);
		} : button);
	}

	public void addFrameButton(int index, ItemStack item, Consumer<InventoryClickEvent> button, boolean isNavButton) {
		frame.addButton(index, item, isNavButton ? event -> {
			putPage(event.getWhoClicked(), currentPage);
			button.accept(event);
		} : button);
	}

	public void build(Menu menu, Player p) {
		int page = popPage(p);
		if (page >= panels.size()) {
			page = 0;
		}

		build(menu, page);
	}

	private void build(Menu menu, int page) {
		currentPage = page;

		panels.get(page).build(menu, 9, activeSize);

		String[] lines = QuestWorld
				.translate(Translation.NAV_ITEM, String.valueOf(page + 1), String.valueOf(panels.size()),
						QuestWorld.translate(page < panels.size() - 1 ? Translation.NAV_NEXT : Translation.NAV_NEXTBAD),
						QuestWorld.translate(page > 0 ? Translation.NAV_PREV : Translation.NAV_PREVBAD))
				.split("\n");

		frame.addButton(1, new ItemBuilder(Material.PAPER).amount(page + 1).wrapText(lines).get(), event -> {
			int delta = (event.isRightClick() ? -1 : 1) * (event.isShiftClick() ? panels.size() : 1);
			int nextPage = Math.min(Math.max(0, page + delta), panels.size() - 1);
			Player p = (Player) event.getWhoClicked();

			QuestWorld.getSounds().EDITOR_CLICK.playTo(p); // TODO This is not entirely right - not all PagedMappings
															// are editor menus!
			build(menu, nextPage);

			// This logically isn't needed, BUT inventories have a nasty habit of displaying
			// old items when only metadata is
			// changed. Most obvious in the entity selector with spawn eggs. Last checked
			// MC1.12.2
			menu.openFor(p);
		});

		if (backButton != null)
			frame.addButton(0, ItemBuilder.Proto.MAP_BACK.get().wrapLore(backLabel).get(), backButton);

		frame.build(menu);
	}
}
