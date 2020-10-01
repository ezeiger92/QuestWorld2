package com.questworld.quest;

import org.bukkit.inventory.ItemStack;

import com.questworld.api.contract.IQuest;
import com.questworld.api.event.CancellableEvent;
import com.questworld.api.event.CategoryChangeEvent;
import com.questworld.util.BitFlag;

class CategoryState extends Category {
	private long changeBits = 0;
	private Category origin;

	public CategoryState(Category copy) {
		super(copy);
		origin = copy;
	}

	@Override
	public CategoryState getState() {
		return this;
	}

	@Override
	public IQuest addQuest(String name, int id) {
		IQuest result = super.addQuest(name, id);
		changeBits |= BitFlag.getBits(Member.QUESTS);
		
		return result;
	}

	@Override
	public void removeQuest(IQuest quest) {
		super.removeQuest(quest);
		changeBits |= BitFlag.getBits(Member.QUESTS);
	}

	// Modify "setX" methods
	@Override
	public void setItem(ItemStack item) {
		super.setItem(item);
		changeBits |= BitFlag.getBits(Member.ITEM);
	}

	@Override
	public void setName(String name) {
		super.setName(name);
		changeBits |= BitFlag.getBits(Member.NAME);
	}

	@Override
	public void setParent(IQuest quest) {
		super.setParent(quest);
		changeBits |= BitFlag.getBits(Member.PARENT);
	}

	@Override
	public void setPermission(String permission) {
		super.setPermission(permission);
		changeBits |= BitFlag.getBits(Member.PERMISSION);
	}

	@Override
	public void setHidden(boolean hidden) {
		super.setHidden(hidden);
		changeBits |= BitFlag.getBits(Member.HIDDEN);
	}

	@Override
	public void toggleWorld(String world) {
		super.toggleWorld(world);
		changeBits |= BitFlag.getBits(Member.WORLD_BLACKLIST);
	}

	/**
	 * Applies all changes held by this object to the original category
	 */
	@Override
	public boolean apply() {
		if (sendEvent()) {
			copyTo(origin);
			origin.updateLastModified();
			changeBits = 0;
			return true;
		}
		return false;
	}

	@Override
	public boolean discard() {
		if (changeBits != 0) {
			copy(origin);
			changeBits = 0;
			return true;
		}
		return false;
	}

	/**
	 * Get the category these changes apply to
	 * 
	 * @return Origin category
	 */
	@Override
	public Category getSource() {
		return origin;
	}

	public boolean hasChange(Member field) {
		return (changeBits & BitFlag.getBits(field)) != 0;
	}

	private boolean sendEvent() {
		return changeBits != 0 && CancellableEvent.send(new CategoryChangeEvent(this));
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}
}
