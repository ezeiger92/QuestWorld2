package me.mrCookieSlime.QuestWorld.quest;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import me.mrCookieSlime.QuestWorld.api.contract.ICategoryWrite;
import me.mrCookieSlime.QuestWorld.api.contract.IQuest;
import me.mrCookieSlime.QuestWorld.event.CancellableEvent;
import me.mrCookieSlime.QuestWorld.event.CategoryChangeEvent;
import me.mrCookieSlime.QuestWorld.util.BitFlag;

public class CategoryChange extends Category implements ICategoryWrite {
	private long changeBits;
	private Category origin;
	public CategoryChange(Category copy) {
		super(copy);
		changeBits = 0;
		origin = copy;
	}
	
	public boolean hasChange(Member field) {
		return (changeBits & BitFlag.getBits(field)) != 0;
	}
	
	/**
	 * Get a list representing the members changed in this Category
	 * 
	 * @return Member enum list
	 */
	public List<Member> getChanges() {
		return BitFlag.getFlags(Member.values(), changeBits);
	}
	
	/**
	 * Get the category these changes apply to
	 * 
	 * @return Origin category
	 */
	public Category getSource() {
		return origin;
	}
	
	/**
	 * Applies all changes held by this object to the original category
	 */
	@Override
	public boolean apply() {
		if(sendEvent()) {
			copyTo(origin);
			changeBits = 0;
			return true;
		}
		return false;
	}

	@Override
	public boolean discard() {
		if(changeBits != 0) {
			copy(origin);
			changeBits = 0;
			return true;
		}
		return false;
	}
	
	/**
	 * Fires a CategoryChangeEvent
	 * 
	 * @see CategoryChangeEvent
	 * @return false if the event is cancelled, otherwise true
	 */
	public boolean sendEvent() {
		return CancellableEvent.send(new CategoryChangeEvent(this));
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
}
