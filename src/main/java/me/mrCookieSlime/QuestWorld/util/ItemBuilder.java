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
 * nothing special.
 * 
 * @author ezeiger92
 */
public class ItemBuilder implements Cloneable {
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
	
	public @Mutable ItemBuilder lore(String... lore) {
		return lore(Arrays.asList(Text.colorizeList(lore)));
	}
	
	public @Mutable ItemBuilder lore(List<String> lore) {
		ItemMeta stackMeta = resultStack.getItemMeta();
		stackMeta.setLore(lore);
		resultStack.setItemMeta(stackMeta);
		return this;
	}
	
	public @Mutable ItemBuilder wrapText(String... input) {
		ArrayList<String> output = new ArrayList<>(input.length);

		final int max_length = Math.max(QuestWorld.getPlugin().getConfig().getInt("options.text-wrap", 32), 8);
		String format = "&f&o";
		for(String s : input) {
			if(s == null) {
				output.add("");
				continue;
			}
			
			int begin = 0;
			int end = -1;
			int seq_begin = 0;
			int seq_end = -1;
			String prepared_format = format;
			String committed_format = format;
			for(int i = 0, n = 0; i < s.length(); ++i) {
				char c1 = s.charAt(i);
				if(c1 == Text.dummyChar) {
					if(i + 1 != s.length()) {
						char c = s.charAt(i + 1);
						if("0123456789aAbBcCdDeEfFrR".indexOf(c) != -1)  {
							prepared_format = String.valueOf(Text.dummyChar) + c;
							n -= 2;
							if(i > seq_end)
								seq_begin = i;
							seq_end = i+1;
						}
						else if("oOlLmMnNkK".indexOf(c) != -1) {
							prepared_format += String.valueOf(Text.dummyChar) + c;
							n -= 2;
							if(i > seq_end)
								seq_begin = i;
							seq_end = i+1;
						}
					}
				}
				if(c1 == ' ') {
					end = i;
				}
				
				//Log.info("n: " + n + ", c: " + s.charAt(i) + ", i: " + i +  ", s: " + seq_begin + ", e: " + seq_end + ", p: " + prepared_format + ", co: " + committed_format);
				
				if(n == max_length) {
					if(end == -1) {
						if(i-2 == seq_end || i-1 == seq_end) {
							end = seq_begin + i - seq_end - 2;
						}
						else
							end = i - 1;

						//Log.info("truncate: " + s.substring(begin, end) + '-');
						output.add(Text.colorize(format + s.substring(begin, end) + '-'));
					}
					else {
						//Log.info("full: " + s.substring(begin, end));
						output.add(Text.colorize(format + s.substring(begin, end)));
					}
					begin = end;
					n = i - end;
					end = -1;
					format = committed_format;
				}
				else
					++n;
				
				if(i > seq_end) {
					committed_format = prepared_format;
				}
			}
			output.add(Text.colorize(format + s.substring(begin)));
			format = prepared_format;
		}
		
		ItemMeta stackMeta = resultStack.getItemMeta();
		String name = output.get(0);
		if(name != null)
			stackMeta.setDisplayName(name);
		output.remove(0);
		stackMeta.setLore(output);
		resultStack.setItemMeta(stackMeta);
		
		return this;
	}
	
	public @Mutable ItemBuilder selector(int index, String... options) {
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
	
	@Override
	public ItemBuilder clone() {
		return new ItemBuilder(resultStack);
	}
}
