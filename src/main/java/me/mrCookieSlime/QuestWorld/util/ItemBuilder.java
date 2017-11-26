package me.mrCookieSlime.QuestWorld.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;

import me.mrCookieSlime.QuestWorld.api.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translation;
import me.mrCookieSlime.QuestWorld.api.annotation.Mutable;

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
public class ItemBuilder implements Cloneable {
	/**
	 * 
	 * @author erik
	 *
	 */
	// TODO: Here's something that will probably need changing after 1.13
	public static enum Proto {
		RED_WOOL(new ItemBuilder(Material.WOOL).color(DyeColor.RED).get()),
		LIME_WOOL(new ItemBuilder(Material.WOOL).color(DyeColor.LIME).get()),
		MAP_BACK(new ItemBuilder(Material.MAP).display(QuestWorld.translate(Translation.button_back_general)).get()),
		;
		private ItemStack item;
		Proto(ItemStack item) {
			this.item = item;
		}
		
		public ItemBuilder get() {
			return new ItemBuilder(item);
		}
		
		public ItemStack getItem() {
			return item.clone();
		}
	}
	
	public static ItemStack clone(ItemStack source) {
		return (source != null) ? source.clone() : null;
	}
	
	public static boolean compareItems(ItemStack left, ItemStack right) {
		if(left == null || right == null)
			return left == right;

		boolean wildcardLore = false;
		
		if(left.hasItemMeta()) {
			ItemMeta meta = left.getItemMeta();
			wildcardLore = meta.hasLore() && meta.getLore().get(0).equals("*");
		}
		
		if(!wildcardLore && right.hasItemMeta()) {
			ItemMeta meta = right.getItemMeta();
			wildcardLore = meta.hasLore() && meta.getLore().get(0).equals("*");
		}
		
		if(wildcardLore) {
			left = left.clone();
			ItemMeta meta = left.getItemMeta();
			meta.setLore(null);
			left.setItemMeta(meta);
			
			right = right.clone();
			meta = right.getItemMeta();
			meta.setLore(null);
			right.setItemMeta(meta);
		}
		
		return left.isSimilar(right);
	}
	
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
     * Constructs an ItemBuilder.
     *
     * @param type Material of item
     * @param amount Amount of material
     */
	public ItemBuilder(Material type, int amount) {
		this(type);
		amount(amount);
	}
	
	/**
     * Constructs an ItemBuilder.
     *
     * @param type Material of item
     * @param amount Amount of material
     * @param durability Stack durability
     */
	public ItemBuilder(Material type, int amount, short durability) {
		this(type, amount);
		durability(durability);
	}
	
	public ItemBuilder(Material type, int amount, int durability) {
		this(type, amount, (short)durability);
	}
	
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
     * Adds an enchantment
     *
     * @param ench Enchantment type
     * @param level Level of enchantment
     * 
     * @return this, for chaining
     */
	public @Mutable ItemBuilder enchant(Enchantment ench, int level) {
		resultStack.addEnchantment(ench, level);
		return this;
	}
	
	/**
     * Adds an unsafe enchantment
     *
     * @param ench Enchantment type
     * @param level Level of enchantment
     * 
     * @return this, for chaining
     */
	public @Mutable ItemBuilder forceEnchant(Enchantment ench, int level) {
		resultStack.addUnsafeEnchantment(ench, level);
		return this;
	}
	
	/**
     * Removes an enchantment
     *
     * @param ench Enchantment type
     * 
     * @return this, for chaining
     */
	public @Mutable ItemBuilder disenchant(Enchantment ench) {
		resultStack.removeEnchantment(ench);
		return this;
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
	
	public @Mutable ItemBuilder durability(int durability) {
		return durability((short)durability);
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
	
	public @Mutable ItemBuilder skull(SkullType type) {
		if(resultStack.getType() == Material.SKULL_ITEM)
			durability(type.ordinal());
		
		return this;
	}
	
	@SuppressWarnings("deprecation")
	public @Mutable ItemBuilder skull(String playerName) {
		skull(SkullType.PLAYER);
		
		if(resultStack.getItemMeta() instanceof SkullMeta) {
			SkullMeta smHolder = (SkullMeta)resultStack.getItemMeta();
			//smHolder.setOwningPlayer(PlayerTools.getPlayer(playerName)); // in 1.12+
			smHolder.setOwner(playerName);
			resultStack.setItemMeta(smHolder);
		}
		
		return this;
	}
	
	public @Mutable ItemBuilder mob(EntityType mob) {
		if(resultStack.getItemMeta() instanceof SpawnEggMeta) {
			SpawnEggMeta meta = (SpawnEggMeta) resultStack.getItemMeta();
			meta.setSpawnedType(mob);
			resultStack.setItemMeta(meta);
		}
		
		return this;
	}
	
	/**
     * Sets leather armor color
     *
     * @param color Color of leather armor
     * 
     * @return this, for chaining
     */
	public @Mutable ItemBuilder leather(Color color) {
		if(resultStack.getItemMeta() instanceof LeatherArmorMeta) {
			LeatherArmorMeta meta = (LeatherArmorMeta)resultStack.getItemMeta();
			meta.setColor(color);
			resultStack.setItemMeta(meta);
		}
		return this;
	}

	public @Mutable ItemBuilder flag(ItemFlag... flags) {
		ItemMeta stackMeta = resultStack.getItemMeta();
		stackMeta.addItemFlags(flags);
		resultStack.setItemMeta(stackMeta);
		return this;
	}
	
	public @Mutable ItemBuilder unflag(ItemFlag... flags) {
		ItemMeta stackMeta = resultStack.getItemMeta();
		stackMeta.removeItemFlags(flags);
		resultStack.setItemMeta(stackMeta);
		return this;
	}
	
	public @Mutable ItemBuilder flagAll() {
		return flag(
				ItemFlag.HIDE_ATTRIBUTES,
				ItemFlag.HIDE_DESTROYS,
				ItemFlag.HIDE_ENCHANTS,
				ItemFlag.HIDE_PLACED_ON,
				ItemFlag.HIDE_POTION_EFFECTS,
				ItemFlag.HIDE_UNBREAKABLE);
	}
	
	public @Mutable ItemBuilder unflagAll() {
		return unflag(
				ItemFlag.HIDE_ATTRIBUTES,
				ItemFlag.HIDE_DESTROYS,
				ItemFlag.HIDE_ENCHANTS,
				ItemFlag.HIDE_PLACED_ON,
				ItemFlag.HIDE_POTION_EFFECTS,
				ItemFlag.HIDE_UNBREAKABLE);
	}
	
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
		ArrayList<String> lines = Text.wrap(length, text);
		
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
		return directLore(Text.wrap(length, lore));
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
		
		result.add(Text.colorize("&2>" + options[index]));
		directLore(result);
		return this;
	}
	
	@Override
	public ItemBuilder clone() {
		return new ItemBuilder(resultStack);
	}
}
