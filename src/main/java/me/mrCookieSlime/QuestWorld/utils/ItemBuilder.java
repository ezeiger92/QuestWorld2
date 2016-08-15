package me.mrCookieSlime.QuestWorld.utils;

import java.util.Arrays;

import org.bukkit.Bukkit;
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
import org.bukkit.material.Colorable;

public class ItemBuilder {
	private ItemStack metaHolderStack;
	private ItemStack resultStack;
	
	/**
     * Constructs an ItemBuilder.
     *
     * @param stack Base item to work with
     */
	public ItemBuilder(ItemStack stack) {
		resultStack = new ItemStack(stack);
		metaHolderStack = resultStack.clone();
	}
	
	/**
     * Constructs an ItemBuilder.
     *
     * @param type Material of item
     */
	public ItemBuilder(Material type) {
		resultStack = new ItemStack(type);
		metaHolderStack = resultStack.clone();
	}
	
	/**
     * Constructs an ItemBuilder.
     *
     * @param type Material of item
     * @param amount Amount of material
     */
	public ItemBuilder(Material type, int amount) {
		resultStack = new ItemStack(type, amount);
		metaHolderStack = resultStack.clone();
	}
	
	
	
	/**
     * Constructs an ItemBuilder.
     *
     * @param type Material of item
     * @param amount Amount of material
     * @param durability Stack durability
     */
	public ItemBuilder(Material type, int amount, short durability) {
		resultStack = new ItemStack(type, amount, durability);
		metaHolderStack = resultStack.clone();
	}
	
	public ItemBuilder(Material type, int amount, int durability) {
		this(type, amount, (short)durability);
	}
	
	public ItemBuilder(SkullType type) {
		this(Material.SKULL_ITEM);
		skull(type);
	}
	
	private void build() {
		ItemMeta metaHolder = metaHolderStack.getItemMeta();
		ItemMeta metaResult = resultStack.getItemMeta();
		
		if(metaHolder instanceof LeatherArmorMeta) {
			LeatherArmorMeta lamHolder = (LeatherArmorMeta)metaHolder;
			LeatherArmorMeta lamResult = (LeatherArmorMeta)metaResult;
			lamResult.setColor(lamHolder.getColor());
		}
		
		if(metaHolder instanceof SkullMeta) {
			SkullMeta smHolder = (SkullMeta)metaHolder;
			SkullMeta smResult = (SkullMeta)metaResult;
			smResult.setOwner(smHolder.getOwner());
		}
		
		metaResult.addItemFlags(metaHolder.getItemFlags().toArray(new ItemFlag[0]));
		metaResult.setDisplayName(metaHolder.getDisplayName());
		metaResult.setLore(metaHolder.getLore());

		resultStack.setItemMeta(metaResult);
		resultStack.addEnchantments(metaHolderStack.getEnchantments());
	}
	
	/**
     * Returns a reference to our ItemStack so our builder can tweak later
     *
     * @return stack
     */
	public ItemStack get() {
		build();
		return resultStack;
	}
	
	/**
     * Returns a new ItemStack based on our stack
     *
     * @return stack
     */
	public ItemStack getNew() {
		build();
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
	public ItemBuilder enchant(Enchantment ench, int level) {
		metaHolderStack.addEnchantment(ench, level);
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
		metaHolderStack.addUnsafeEnchantment(ench, level);
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
		metaHolderStack.removeEnchantment(ench);
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
	public ItemBuilder durability(short durability) {
		resultStack.setDurability(durability);
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
		resultStack.setType(type);
		metaHolderStack.setType(type);
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
		if(resultStack.getData() instanceof Colorable) {
			Colorable c = (Colorable)resultStack.getData();
			c.setColor(color);
		}
		
		legacyMaterialData(color);
		return this;
	}
	
	public ItemBuilder skull(SkullType type) {
		if(resultStack.getType() == Material.SKULL_ITEM) {
			durability(type.ordinal());
		}
		
		return this;
	}
	
	public ItemBuilder skull(String playerName) {
		skull(SkullType.PLAYER);
		
		if(metaHolderStack.getItemMeta() instanceof SkullMeta) {
			SkullMeta smHolder = (SkullMeta)metaHolderStack.getItemMeta();
			smHolder.setOwner(playerName);
			metaHolderStack.setItemMeta(smHolder);
		}
		
		return this;
	}

	@SuppressWarnings("deprecation")
	private void legacyMaterialData(DyeColor color) {
		if(resultStack.getType() == Material.INK_SACK)
			durability(color.getDyeData());
		else
			durability(color.getData());
	}
	
	@SuppressWarnings("deprecation")
	public ItemBuilder tag(ItemTag tag) {
		Bukkit.getUnsafe().modifyItemStack(resultStack, tag.toString());
		return this;
	}
	
	public ItemBuilder mob(EntityOther mob) {
		legacyEggData(mob.getEntity());
		return tag(new ItemTag(EntityTag.from(mob)));
	}
	
	public ItemBuilder mob(EntityType mob) {
		legacyEggData(mob);
		return tag(new ItemTag(EntityTag.from(mob)));
	}

	@SuppressWarnings("deprecation")
	private void legacyEggData(EntityType entity) {
		durability(entity.getTypeId());
	}
	
	/**
     * Sets leather armor color
     *
     * @param color Color of leather armor
     * 
     * @return this, for chaining
     */
	public ItemBuilder leather(Color color) {
		if(metaHolderStack.getItemMeta() instanceof LeatherArmorMeta) {
			LeatherArmorMeta meta = (LeatherArmorMeta)metaHolderStack.getItemMeta();
			meta.setColor(color);
			metaHolderStack.setItemMeta(meta);
		}
		return this;
	}
	

	public ItemBuilder flag(ItemFlag... flags) {
		ItemMeta stackMeta = metaHolderStack.getItemMeta();
		stackMeta.addItemFlags(flags);
		metaHolderStack.setItemMeta(stackMeta);
		return this;
	}
	
	public ItemBuilder unflag(ItemFlag... flags) {
		ItemMeta stackMeta = metaHolderStack.getItemMeta();
		stackMeta.removeItemFlags(flags);
		metaHolderStack.setItemMeta(stackMeta);
		return this;
	}
	
	public ItemBuilder display(String displayName) {
		ItemMeta stackMeta = metaHolderStack.getItemMeta();
		stackMeta.setDisplayName(Text.colorize(displayName));
		metaHolderStack.setItemMeta(stackMeta);
		return this;
	}
	
	public ItemBuilder lore(String... lore) {
		ItemMeta stackMeta = metaHolderStack.getItemMeta();
		stackMeta.setLore(Arrays.asList(Text.colorizeList(lore)));
		metaHolderStack.setItemMeta(stackMeta);
		return this;
	}
}
