package me.mrCookieSlime.QuestWorld.utils;

import java.util.Arrays;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.Colorable;

public class ItemBuilder {
	private ItemStack stack_;
	
	/**
     * Constructs an ItemBuilder.
     *
     * @param stack Base item to work with
     */
	public ItemBuilder(ItemStack stack) {
		stack_ = new ItemStack(stack);
	}
	
	/**
     * Constructs an ItemBuilder.
     *
     * @param type Material of item
     */
	public ItemBuilder(Material type) {
		stack_ = new ItemStack(type);
	}
	
	/**
     * Constructs an ItemBuilder.
     *
     * @param type Material of item
     * @param amount Amount of material
     */
	public ItemBuilder(Material type, int amount) {
		stack_ = new ItemStack(type, amount);
	}
	
	
	
	/**
     * Constructs an ItemBuilder.
     *
     * @param type Material of item
     * @param amount Amount of material
     * @param durability Stack durability
     */
	public ItemBuilder(Material type, int amount, short durability) {
		stack_ = new ItemStack(type, amount, durability);
	}
	
	public ItemBuilder(Material type, int amount, int durability) {
		this(type, amount, (short)durability);
	}
	
	/**
     * Returns a reference to our ItemStack so our builder can tweak later
     *
     * @return stack
     */
	public ItemStack get() {
		return stack_;
	}
	
	/**
     * Returns a new ItemStack based on our stack
     *
     * @return stack
     */
	public ItemStack getNew() {
		return new ItemStack(stack_);
	}
	
	/**
     * Adds an enchantment
     *
     * @param ench Enchantment type
     * @param level Level of enchantment
     * 
     * @return this, for chaining
     */
	public ItemBuilder enchant(Enchantment ench, int level) {
		stack_.addEnchantment(ench, level);
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
	public ItemBuilder forceEnchant(Enchantment ench, int level) {
		stack_.addUnsafeEnchantment(ench, level);
		return this;
	}
	
	/**
     * Removes an enchantment
     *
     * @param ench Enchantment type
     * 
     * @return this, for chaining
     */
	public ItemBuilder disenchant(Enchantment ench) {
		stack_.removeEnchantment(ench);
		return this;
	}
	
	/**
     * Sets stack amount
     *
     * @param amount Target size of stack
     * 
     * @return this, for chaining
     */
	public ItemBuilder amount(int amount) {
		stack_.setAmount(amount);
		return this;
	}
	
	/**
     * Sets stack durability
     *
     * @param durability Target durability for stack
     * 
     * @return this, for chaining
     */
	public ItemBuilder durability(short durability) {
		stack_.setDurability(durability);
		return this;
	}
	
	public ItemBuilder durability(int durability) {
		return durability((short)durability);
	}
	
	/**
     * Sets stack material
     *
     * @param type Material
     * 
     * @return this, for chaining
     */
	public ItemBuilder type(Material type) {
		stack_.setType(type);
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
	public ItemBuilder color(DyeColor color) {
		if(stack_.getData() instanceof Colorable) {
			((Colorable)stack_.getData()).setColor(color);
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
	public ItemBuilder leather(Color color) {
		if(stack_.getItemMeta() instanceof LeatherArmorMeta) {
			LeatherArmorMeta meta = (LeatherArmorMeta)stack_.getItemMeta();
			meta.setColor(color);
			stack_.setItemMeta(meta);
		}
		return this;
	}
	

	public ItemBuilder flag(ItemFlag... flags) {
		ItemMeta stackMeta = stack_.getItemMeta();
		stackMeta.addItemFlags(flags);
		stack_.setItemMeta(stackMeta);
		return this;
	}
	
	public ItemBuilder unflag(ItemFlag... flags) {
		ItemMeta stackMeta = stack_.getItemMeta();
		stackMeta.removeItemFlags(flags);
		stack_.setItemMeta(stackMeta);
		return this;
	}
	
	public ItemBuilder display(String displayName) {
		ItemMeta stackMeta = stack_.getItemMeta();
		stackMeta.setDisplayName(Text.colorize(displayName));
		stack_.setItemMeta(stackMeta);
		return this;
	}
	
	public ItemBuilder lore(String... lore) {
		ItemMeta stackMeta = stack_.getItemMeta();
		stackMeta.setLore(Arrays.asList(Text.colorizeList(lore)));
		stack_.setItemMeta(stackMeta);
		return this;
	}
}
