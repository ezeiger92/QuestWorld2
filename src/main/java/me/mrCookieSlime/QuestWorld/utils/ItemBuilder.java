package me.mrCookieSlime.QuestWorld.utils;

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

import me.mrCookieSlime.QuestWorld.QuestWorld;
import me.mrCookieSlime.QuestWorld.api.Translation;

/**
 * This class provides a builder for ItemStacks. It is exactly what you expect,
 * nothing special.
 * 
 * @author ezeiger92
 */
public class ItemBuilder {
	/**
	 * 
	 * @author erik
	 *
	 */
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
		if(source != null)
			return source.clone();
		
		return null;
	}
	
	public static ItemBuilder edit(ItemStack stack) {
		
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
		resultStack = new ItemStack(stack);
		
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
	public ItemStack get() {
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
	public ItemBuilder enchant(Enchantment ench, int level) {
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
	public ItemBuilder forceEnchant(Enchantment ench, int level) {
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
	public ItemBuilder disenchant(Enchantment ench) {
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
	public ItemBuilder color(DyeColor color) {
		if(resultStack.getType() == Material.INK_SACK)
			durability(color.getDyeData());
		else
			durability(color.getWoolData());
		
		return this;
	}
	
	public ItemBuilder skull(SkullType type) {
		if(resultStack.getType() == Material.SKULL_ITEM)
			durability(type.ordinal());
		
		return this;
	}
	
	@SuppressWarnings("deprecation")
	public ItemBuilder skull(String playerName) {
		skull(SkullType.PLAYER);
		
		if(resultStack.getItemMeta() instanceof SkullMeta) {
			SkullMeta smHolder = (SkullMeta)resultStack.getItemMeta();
			//smHolder.setOwningPlayer(PlayerTools.getPlayer(playerName)); // in 1.12+
			smHolder.setOwner(playerName);
			resultStack.setItemMeta(smHolder);
		}
		
		return this;
	}
	
	public ItemBuilder mob(EntityType mob) {
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
	public ItemBuilder leather(Color color) {
		if(resultStack.getItemMeta() instanceof LeatherArmorMeta) {
			LeatherArmorMeta meta = (LeatherArmorMeta)resultStack.getItemMeta();
			meta.setColor(color);
			resultStack.setItemMeta(meta);
		}
		return this;
	}
	

	public ItemBuilder flag(ItemFlag... flags) {
		ItemMeta stackMeta = resultStack.getItemMeta();
		stackMeta.addItemFlags(flags);
		resultStack.setItemMeta(stackMeta);
		return this;
	}
	
	public ItemBuilder unflag(ItemFlag... flags) {
		ItemMeta stackMeta = resultStack.getItemMeta();
		stackMeta.removeItemFlags(flags);
		resultStack.setItemMeta(stackMeta);
		return this;
	}
	
	public ItemBuilder display(String displayName) {
		ItemMeta stackMeta = resultStack.getItemMeta();
		stackMeta.setDisplayName(Text.colorize(displayName));
		resultStack.setItemMeta(stackMeta);
		return this;
	}
	
	public ItemBuilder lore(String... lore) {
		return lore(Arrays.asList(Text.colorizeList(lore)));
	}
	
	public ItemBuilder lore(List<String> lore) {
		ItemMeta stackMeta = resultStack.getItemMeta();
		stackMeta.setLore(lore);
		resultStack.setItemMeta(stackMeta);
		return this;
	}
	
	public ItemBuilder selector(int index, String... options) {
		if(options == null || options.length == 0)
			return this;
		
		if(index < 0 || index >= options.length)
			index = 0;
		
		String[] result = new String[options.length + 1];
		result[0] = "";
		
		for(int i = 0; i < options.length; ++i)
			result[i + 1] = " &7" + options[i];
		
		result[index + 1] = "&2>" + options[index];
		lore(result);
		return this;
	}
}
