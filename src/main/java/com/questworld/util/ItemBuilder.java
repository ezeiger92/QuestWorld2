package com.questworld.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;

import com.questworld.api.QuestWorld;
import com.questworld.api.Translation;
import com.questworld.api.annotation.Mutable;

/**
 * This class provides a builder for ItemStacks. It is exactly what you expect,
 * with a few special functions listed below and some static utilities.
 * 
 * @see ItemBuilder#selector
 * @see ItemBuilder#wrapLore
 * @see ItemBuilder#wrapText
 * 
 * @author ezeiger92
 */
public class ItemBuilder {
	/**
	 * A handy set of builder prototypes that were being created by hand too
	 * often.
	 * 
	 * @author Erik Zeiger
	 */
	// TODO: Here's something that will probably need changing after 1.13
	public static enum Proto {
		RED_WOOL(new ItemBuilder(Material.WOOL).color(DyeColor.RED).get()),
		LIME_WOOL(new ItemBuilder(Material.WOOL).color(DyeColor.LIME).get()),
		MAP_BACK(new ItemBuilder(Material.MAP).flagAll().display(QuestWorld.translate(Translation.button_back_general)).get()),
		;
		private ItemStack item;
		Proto(ItemStack item) {
			this.item = item;
		}
		
		/**
		 * Creates a new ItemBuilder from the prototype item.
		 * 
		 * @return A new ItemBuilder
		 */
		public ItemBuilder get() {
			return new ItemBuilder(item);
		}
		
		/**
		 * Copies the prototype item.
		 * 
		 * @return A new ItemStack
		 */
		public ItemStack getItem() {
			return item.clone();
		}
	}
	
	/**
	 * Null pointer safe tool for cloning ItemStacks
	 * 
	 * @param source ItemStack to clone
	 * @return Clone of source, or <tt>null</tt> if source is <tt>null</tt>
	 */
	public static ItemStack clone(ItemStack source) {
		return (source != null) ? source.clone() : null;
	}
	
	/**
	 * Item comparison with (optional) wildcard matching for metadata. If either
	 * stack has the lore consisting of a single asterisk ("*"), all metadata
	 * will be discarded before a comparison is made. If either stack is null,
	 * a null pointer safe comparison is made.
	 * 
	 * @param left One of the ItemStacks
	 * @param right The other ItemStack
	 * @return Whether or not the items are considered identical
	 */
	public static boolean compareItems(ItemStack left, ItemStack right) {
		if(left == null || right == null)
			return left == right;
		
		if(left.getType() != right.getType() || left.getDurability() != right.getDurability())
			return false;
		
		boolean hasMetaLeft = left.hasItemMeta();
		ItemMeta metaLeft = hasMetaLeft ? left.getItemMeta() : null;
		ItemMeta metaRight = right.hasItemMeta() ? right.getItemMeta() : null;

		if(!isWildcard(metaLeft) && !isWildcard(metaRight))
			return (hasMetaLeft == (metaRight != null)) &&
			(!hasMetaLeft || Bukkit.getItemFactory().equals(metaLeft, metaRight));

		return true;
	}
	
	private static boolean isWildcard(ItemMeta meta) {
		return meta != null && meta.hasLore() && meta.getLore().get(0).equals("*");
	}
	
	/**
	 * Constructs an ItemBuilder by consuming an exiting ItemStack. Any
	 * modifications to the builder directly affect the ItemStack.
	 * 
	 * @param stack The ItemStack to edit
	 * @return A new ItemBuilder that modifies <tt>stack</tt>
	 */
	public static @Mutable ItemBuilder edit(@Mutable("Stored and modified by other functions") ItemStack stack) {
		
		ItemBuilder res = new ItemBuilder();
		res.resultStack = stack;

		if(stack.getType() == Material.AIR)
			res.type(Material.BARRIER);
		
		return res;
	}
	
	private ItemStack resultStack;
	
	private ItemBuilder() {
	}
	
	/**
     * Constructs an ItemBuilder.
     *
     * @param stack Base item to work with
     */
	public ItemBuilder(ItemStack stack) {
		resultStack = stack.clone();
		
		if(stack.getType() == Material.AIR)
			type(Material.BARRIER);
	}
	
	/**
     * Constructs an ItemBuilder.
     *
     * @param type Material of item
     */
	public ItemBuilder(Material type) {
		resultStack = new ItemStack(type);

		if(type == Material.AIR)
			type(Material.BARRIER);
	}
	
	/**
	 * Constructs an ItemBuilder of a skull. The resulting builder will have the
	 * material <tt>SKULL_ITEM</tt> and use the desired skull type.
	 * 
	 * @param type Type of skull to create
	 */
	public ItemBuilder(SkullType type) {
		this(Material.SKULL_ITEM);
		skull(type);
	}
	
	/**
     * Returns a reference to our ItemStack so our builder can tweak later
     *
     * @return stack
     */
	public @Mutable("ItemBuilder holds a reference") ItemStack get() {
		return resultStack;
	}
	
	/**
     * Returns a new ItemStack based on our stack
     *
     * @return stack
     */
	public ItemStack getNew() {
		return resultStack.clone();
	}
	
	/**
     * Sets stack amount
     *
     * @param amount Target size of stack
     * 
     * @return this, for chaining
     */
	public @Mutable ItemBuilder amount(int amount) {
		resultStack.setAmount(amount);
		return this;
	}
	
	/**
     * Sets stack durability
     *
     * @param durability Target durability for stack
     * 
     * @return this, for chaining
     */
	public @Mutable ItemBuilder durability(short durability) {
		resultStack.setDurability(durability);
		return this;
	}
	
	/**
     * Sets stack material
     *
     * @param type Material
     * 
     * @return this, for chaining
     */
	public @Mutable ItemBuilder type(Material type) {
		resultStack.setType(type);
		return this;
	}
	
	/**
     * Sets material color
     * Use ItemBuilder.leather(org.bukkit.Color) for leather armor color
     *
     * @param color Color of material
     * 
     * @return this, for chaining
     */
	@SuppressWarnings("deprecation")
	public @Mutable ItemBuilder color(DyeColor color) {
		if(resultStack.getType() == Material.INK_SACK)
			durability(color.getDyeData());
		else
			durability(color.getWoolData());
		
		return this;
	}
	
	/**
	 * Sets skull type, given that the current material accepts skull types.
	 * 
	 * @param type Desired type of skull
	 * @return this, for chaining
	 */
	public @Mutable ItemBuilder skull(SkullType type) {
		if(resultStack.getType() == Material.SKULL_ITEM)
			durability((short) type.ordinal());
		
		return this;
	}
	
	/**
	 * Sets the skull type to a players head, given that the current material
	 * accepts skull types. <tt>playerName</tt> must not be null. If you want a
	 * plain player skull, use
	 * {@link ItemBuilder#skull(SkullType) skull(SkullType.PLAYER)}.
	 * 
	 * @param player The player whose face will be displayed on the head
	 * @return this, for chaining
	 */
	public @Mutable ItemBuilder skull(OfflinePlayer player) {
		skull(SkullType.PLAYER);
		
		if(resultStack.getItemMeta() instanceof SkullMeta) {
			SkullMeta smHolder = (SkullMeta)resultStack.getItemMeta();
			smHolder.setOwningPlayer(player);
			resultStack.setItemMeta(smHolder);
		}
		
		return this;
	}
	
	/**
	 * Sets the mob type, given the current material supports mob types.
	 * 
	 * @param entity The type of entity
	 * @return this, for chaining
	 */
	public @Mutable ItemBuilder mob(EntityType entity) {
		if(resultStack.getItemMeta() instanceof SpawnEggMeta) {
			SpawnEggMeta meta = (SpawnEggMeta) resultStack.getItemMeta();
			meta.setSpawnedType(entity);
			resultStack.setItemMeta(meta);
		}
		
		return this;
	}

	/**
	 * Sets a series of flags on the ItemStack, affecting the information it
	 * displays.
	 * 
	 * @param flags A series of ItemFlags
	 * @return this, for chaining
	 */
	public @Mutable ItemBuilder flag(ItemFlag... flags) {
		ItemMeta stackMeta = resultStack.getItemMeta();
		stackMeta.addItemFlags(flags);
		resultStack.setItemMeta(stackMeta);
		return this;
	}
	
	/**
	 * Removes a series of flags from the ItemStack, affecting the information it
	 * displays.
	 * 
	 * @param flags A series of ItemFlags
	 * @return this, for chaining
	 */
	public @Mutable ItemBuilder unflag(ItemFlag... flags) {
		ItemMeta stackMeta = resultStack.getItemMeta();
		stackMeta.removeItemFlags(flags);
		resultStack.setItemMeta(stackMeta);
		return this;
	}
	
	/**
	 * Sets all existing flags on the ItemStack.
	 * 
	 * @see ItemBuilder#flag
	 * 
	 * @return this, for chaining
	 */
	public @Mutable ItemBuilder flagAll() {
		return flag(ItemFlag.values());
	}
	
	/**
	 * Removes all flags from the ItemStack.
	 * 
	 * @see ItemBuilder#unflag
	 * 
	 * @return this, for chaining
	 */
	public @Mutable ItemBuilder unflagAll() {
		return unflag(ItemFlag.values());
	}
	
	/**
	 * Sets the display name for the ItemStack.
	 * 
	 * @param displayName The new name
	 * @return this, for chaining
	 */
	public @Mutable ItemBuilder display(String displayName) {
		ItemMeta stackMeta = resultStack.getItemMeta();
		stackMeta.setDisplayName(Text.colorize(displayName));
		resultStack.setItemMeta(stackMeta);
		return this;
	}
	
	/**
     * Sets item lore, applying colors in the process
     *
     * @param lore The text to set
     * 
     * @return this, for chaining
     */
	public @Mutable ItemBuilder lore(String... lore) {
		return directLore(Arrays.asList(Text.colorizeList(lore)));
	}
	
	/**
     * Sets item lore without colors, in case they were processed before
     *
     * @param lore The text to set
     * 
     * @return this, for chaining
     */
	public @Mutable ItemBuilder directLore(List<String> lore) {
		ItemMeta stackMeta = resultStack.getItemMeta();
		stackMeta.setLore(lore);
		resultStack.setItemMeta(stackMeta);
		return this;
	}
	
	/**
     * Creates a wrapping text field across the display name and lore
     * Null elements will be ignored
     *
     * @param text The text to set
     * 
     * @return this, for chaining
     */
	public @Mutable ItemBuilder wrapText(String... text) {
		int length = QuestWorld.getPlugin().getConfig().getInt("options.text-wrap", 32);
		text[0] = "&f&o" + text[0];
		ArrayList<String> lines = Text.wrap(length, Text.colorizeList(text));
		
		ItemMeta stackMeta = resultStack.getItemMeta();
		if(lines.size() > 0) {
			stackMeta.setDisplayName(lines.get(0));
			lines.remove(0);
		}
		stackMeta.setLore(lines);
		resultStack.setItemMeta(stackMeta);
		
		return this;
	}
	
	/**
     * Creates a wrapping text field across lore only
     * Null elements will be ignored
     *
     * @param lore The text to set
     * 
     * @return this, for chaining
     */
	public @Mutable ItemBuilder wrapLore(String... lore) {
		int length = QuestWorld.getPlugin().getConfig().getInt("options.text-wrap", 32);
		lore[0] = "&f&o" + lore[0];
		return directLore(Text.wrap(length, Text.colorizeList(lore)));
	}
	
	/**
	 * Creates a list selector within item lore. All options will be printed in order with default
	 * formatting, and the selected index will be highlighted with special formatting.
	 * 
	 * @param index The index to highlight, defaults to 0 if outside a valid range
	 * @param options Array of options to select, must have at least 1 element to function
	 * @return this, for chaining
	 */
	public @Mutable ItemBuilder selector(int index, String... options) {
		if(options.length == 0)
			return this;
		
		if(index < 0 || index >= options.length)
			index = 0;
		
		ArrayList<String> result = new ArrayList<>(options.length + 1);
		result.add("");
		
		for(int i = 0; i < options.length; ++i)
			result.add(Text.colorize(" &7" + options[i]));
		
		result.set(index + 1, Text.colorize("&2>" + options[index]));
		directLore(result);
		return this;
	}
}
