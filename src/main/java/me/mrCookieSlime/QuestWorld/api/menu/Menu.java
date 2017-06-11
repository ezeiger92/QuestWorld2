package me.mrCookieSlime.QuestWorld.api.menu;

import java.util.Arrays;

public class Menu {
	private final int ROW_WIDTH = 9;
	private final int MAX_ROWS = 6;
	
	Menu parent = null;
	Menu[] children = {};
	
	public Menu() {
	}
	
	public Menu(int rows) {
		this();
		if(rows < 0)
			throw new IndexOutOfBoundsException("Row count " + rows + " is too small, expected range [0..6]");
		else if(rows > 6)
			throw new IndexOutOfBoundsException("Row count " + rows + " is too large, expected range [0..6]");
		
		children = new Menu[rows * 9];
	}
	
	public final int getRowWidth() {
		return ROW_WIDTH;
	}
	
	public final int getMaxRows() {
		return MAX_ROWS;
	}
	
	private int bounds(int source, int upper) {
		if(source >= 0)
			return source % upper;
		
		return ((source + 1) % upper) + (upper - 1);
	}
	
	private int relativeToIndex(int x, int y) {
		return bounds(y, children.length / 9) * 9 + bounds(y, children.length / 9);
	}
	
	protected void grow(int toContain) {
		if(children.length <= toContain)
			children = Arrays.copyOf(children, ((toContain + 1) / 9) * 9);
	}
	
	protected void validateIndex(int index) {
		if(index < 0)
			throw new IndexOutOfBoundsException("Index " + index + " is too small, expected range [0..53]");
		else if(index >= 54)
			throw new IndexOutOfBoundsException("Index " + index + " is too large, expected range [0..53]");
	}
	
	protected Menu rawSwapAtIndex(int index, Menu menu) {
		Menu result = children[index];
		children[index] = menu;
		return result;
	}
	
	
	
	public Menu add(int index, Menu menu) {
		if(menu == null)
			throw new NullPointerException("Supplied menu is null! Use remove if you are trying to remove a menu!");
		
		validateIndex(index);
		grow(index);
		
		return rawSwapAtIndex(index, menu);
	}
	
	public Menu remove(int index) {
		validateIndex(index);
		return rawSwapAtIndex(index, null);
	}
	
	public Menu addRelative(int x, int y,  Menu menu) {
		if(menu == null)
			throw new NullPointerException("Supplied menu is null! Use removeRelative if you are trying to remove a menu!");

		int index = relativeToIndex(x, y);
		grow(index);
		return rawSwapAtIndex(index, menu);
	}
	
	public Menu removeRelative(int x, int y) {
		return rawSwapAtIndex(relativeToIndex(x, y), null);
	}
}
