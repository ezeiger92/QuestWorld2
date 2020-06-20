package com.questworld.api.menu;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import com.questworld.api.config.ConfigObject;
import com.questworld.api.config.Section;

@Section(path="icons.yml")
public class Icons extends ConfigObject {
	public ItemStack destructive_action; // red wool
	public ItemStack destructive_accept; // lime wool
	public ItemStack destructive_cancel; // red wool
	
	public ItemStack action_accept; // green wool
	public ItemStack action_cancel; // red wool

	public ItemStack default_category; // writable book
	public ItemStack default_quest; // writable book
	public ItemStack default_mission_item; // stone
	public ItemStack category_locked; // barrier
	public ItemStack quest_locked; // red stained glass
	public ItemStack back; // map
	public ItemStack navigation; // paper
	
	public ItemStack check_all_tasks; // chest
	public ItemStack cooldown; // clock
	public ItemStack economy_reward; // gold ingot
	public ItemStack experience_reward; // exp bottle
	public ItemStack task_locked; // red stained glass
	public ItemStack task_has_reward; // purple stained glass
	public ItemStack task_on_cooldown; // yellow stained glass
	public ItemStack task_inactive; // light gray stained glass
	
	public ItemStack view_party_memebers; // head
	public ItemStack open_party; // head
	public ItemStack party_disabled_display; // ench book
	public ItemStack player_head; // head
	
	public ItemStack book_item; // enchanted book
	
//	public MissionTypeIcons mission_type = new MissionTypeIcons();
//	public static class MissionTypeIcons {
//		public ItemStack craft; // crafting table
//		public ItemStack detect; // observer
//		public ItemStack fish; // fishing rod
//		public ItemStack harvest; // wheat
//		public ItemStack join; // gold nugget
//		public ItemStack kill; // iron sword
//		public ItemStack kill_named; // gold sword
//		public ItemStack level; // exp bottle
//		public ItemStack location; // leather boots
//		public ItemStack mine; // iron pickaxe
//		public ItemStack play; // clock
//		public ItemStack submit; // chest
//	}
	public Map<String, ItemStack> mission_icons = new HashMap<>();
	public ItemStack unknown_mission; // barrier

	public EditorIcons editor = new EditorIcons();
	public static class EditorIcons {
		public ItemStack empty_category; // red stained glass
		public ItemStack empty_quest; // red stained glass
		public ItemStack empty_mission; // red stained glass
		public ItemStack open_category_editor; // writable book
		public ItemStack open_quest_list; // writable book
		public ItemStack open_quest_editor; // writable book
		public ItemStack open_mission_list; // writable book
		public ItemStack set_name; // name tag
		public ItemStack set_required_quest; // writable book
		public ItemStack set_permission; // name tag
		public ItemStack set_visibility; // golden carrot
		public ItemStack world_blacklist; // grass block
		public ItemStack set_item_reward; // chest
		public ItemStack set_duration; // clock
		public ItemStack set_economy_reward; // gold ingot
		public ItemStack set_experience_reward; // exp bottle
		public ItemStack set_command_reward; // command block
		public ItemStack set_party_usable; // firework rocket
		public ItemStack set_ordered; // command block
		public ItemStack set_autoclaim; // chest
		public ItemStack set_party_size; // firework rocket
		public ItemStack set_amount; // redstone
		public ItemStack set_death_reset; // player head
		public ItemStack open_dialog_editor; // paper
		public ItemStack set_spawners_allowed; // spawner
		public ItemStack set_match_type; // golden apple
		public ItemStack set_radius; // compass
		
		public ItemStack world_display; // grass block
		public Map<String, ItemStack> custom_world_display = new HashMap<>();
		
		public ItemStack any_mob; // nether star
		public ItemStack unknown_mob; // barrier
		public Map<String, ItemStack> mob_selector = new HashMap<>();
	}
	
	public Items items = new Items();
	public static class Items {
		public ItemStack crafting_table;
		public ItemStack written_book;
		public Map<String, ItemStack> mob_egg_overloads;
	}
}
