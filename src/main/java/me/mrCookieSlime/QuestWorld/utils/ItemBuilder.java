package me.mrCookieSlime.QuestWorld.utils;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

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
		stack_ = new ItemStack(type, amount, (short)durability);
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
		stack_.setDurability((short)durability);
		return this;
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
	

	public ItemBuilder flag(ItemFlag... flags) {
		stack_.getItemMeta().addItemFlags(flags);
		return this;
	}
	
	public ItemBuilder unflag(ItemFlag... flags) {
		stack_.getItemMeta().removeItemFlags(flags);
		return this;
	}
	
	public ItemBuilder display(String displayName) {
		stack_.getItemMeta().setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
		return this;
	}
	
	public ItemBuilder lore(String... lore) {
		stack_.getItemMeta().setLore(Arrays.asList(lore));
		return this;
	}
}
