package com.questworld.api.menu;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.questworld.api.MissionType;
import com.questworld.api.QuestWorld;
import com.questworld.util.EntityTools;
import com.questworld.util.ItemBuilder;

public class IconSelector {
	private static final List<FieldWrapper<ItemStack>> iconResolver;
	private static final List<FieldWrapper<Map<String, ItemStack>>> mappedIconResolver;
	
	@SuppressWarnings("unchecked")
	private static <T> T wrappedGet(Field field, Object obj) {
		try {
			return (T)field.get(obj);
		}
		catch(Exception e) {
			return null;
		}
	}
	
	private static class FieldWrapper<T> {
		private final Supplier<Object> source;
		private final Field field;
		private final String name;
		
		public FieldWrapper(Supplier<Object> source, Field field, String name) {
			this.source = source;
			this.field = field;
			this.name = name;
		}
		
		public T get() {
			return wrappedGet(field, source.get());
		}
		
		public void set(T value) {
			try {
				field.set(source.get(), value);
			}
			catch(Exception e) {
			}
		}
		
		public String getName() {
			return name;
		}
	}
	
	private static class ClassInfo {
		public String path;
		public Class<?> clazz;
		public Supplier<Object> source;
		
		public ClassInfo(String path, Class<?> clazz, Supplier<Object> source) {
			this.path = path;
			this.clazz = clazz;
			this.source = source;
		}
	}
	
	private static boolean enclosedInIcons(Class<?> clazz) {
		while(clazz != null) {
			if(clazz == Icons.class)
				return true;
			
			clazz = clazz.getEnclosingClass();
		}
		return false;
	}
	
	static {
		iconResolver = new ArrayList<>();
		mappedIconResolver = new ArrayList<>();
		
		ArrayDeque<ClassInfo> classes = new ArrayDeque<>();
		
		classes.push(new ClassInfo("", Icons.class, QuestWorld::getIcons));
		
		while(!classes.isEmpty()) {
			ClassInfo classInfo = classes.pop();
			String prefix = classInfo.path.isEmpty() ? "" : classInfo.path + ".";
			
			for(Field f : classInfo.clazz.getFields()) {
				if(ItemStack.class.isAssignableFrom(f.getType())) {
					iconResolver.add(new FieldWrapper<>(classInfo.source, f, prefix + f.getName()));
				}
				else if(Map.class.isAssignableFrom(f.getType())) {
					mappedIconResolver.add(new FieldWrapper<>(classInfo.source, f, prefix + f.getName()));
				}
				else if(enclosedInIcons(f.getType())) {
					Supplier<Object> s = classInfo.source;
					classes.push(new ClassInfo(prefix + f.getName(), f.getType(), () -> wrappedGet(f, s.get())));
				}
			}
		}
	}

	public static void openMain(Player p) {
		Menu menu = new Menu(1, "Icon Selector");
		PagedMapping view = new PagedMapping(45, 9);
		
		view.addFrameButton(3, new ItemBuilder(QuestWorld.getIcons().action_accept)
				.wrapText("&2Save changes").get(), event -> {
			QuestWorld.getAPI().onSave();
		}, false);

		int index = 0;
		
		for(FieldWrapper<Map<String, ItemStack>> ctrl : mappedIconResolver) {
			view.addButton(index++,
					new ItemBuilder(QuestWorld.getIcons().editor.open_quest_editor)
							.wrapText("&f" + ctrl.getName() + " mapping")
							.get(),
					event -> {
						openSpecific((Player)event.getWhoClicked(), ctrl);
					}, true);
		}
		
		for(FieldWrapper<ItemStack> ctrl : iconResolver) {
			ItemStack stack = ctrl.get();
			String status = stack == null ? " [missing item]" : "";
					
			view.addButton(index++,
					new ItemBuilder(stack)
							.wrapText("&f" + ctrl.getName() + status)
							.get(),
					event -> {
						PlayerInventory inv = event.getWhoClicked().getInventory();
						ItemStack held = ItemBuilder.clone(inv.getItem(inv.getHeldItemSlot()));
				
						if(!ItemBuilder.isAir(held)) {
							ctrl.set(held);
						}
						
						openMain((Player)event.getWhoClicked());
					}, true);
		}
		
		view.build(menu, p);
		menu.openFor(p);
	}
	
	private static class Pair<T, U> {
		private final T first;
		private final U second;
		
		public Pair(T first, U second) {
			this.first = first;
			this.second = second;
		}
		
		public T first() {
			return first;
		}
		
		public U second() {
			return second;
		}
	}
	
	private static ArrayList<Pair<String, ItemStack>> existingMissionIcons() {
		ArrayList<Pair<String, ItemStack>> icons = new ArrayList<>();
		
		ArrayList<MissionType> types = new ArrayList<>(QuestWorld.getMissionTypes().values());
		types.sort((m1, m2) -> m1.toString().compareToIgnoreCase(m2.toString()));
		
		for (MissionType type : types) {
			icons.add(new Pair<>(type.toString(), type.getSelectorItem().clone()));
		}
		
		return icons;
	}
	
	private static ArrayList<Pair<String, ItemStack>> existingWorldIcons() {
		ArrayList<Pair<String, ItemStack>> icons = new ArrayList<>();
		Icons.EditorIcons config = QuestWorld.getIcons().editor;
		
		for (World world : Bukkit.getWorlds()) {
			icons.add(new Pair<>(world.getName(),
					ItemBuilder.sanitizeClone(config.custom_world_display.getOrDefault(world.getName(), config.world_display))));
		}
		
		return icons;
	}
	
	private static ArrayList<Pair<String, ItemStack>> existingMobIcons() {
		ArrayList<Pair<String, ItemStack>> icons = new ArrayList<>();
		
		for (EntityType entity : EntityTools.aliveEntityTypes()) {
			icons.add(new Pair<>(EntityTools.serialNameOf(entity), EntityTools.getEntityDisplay(entity).getNew()));
		}
		
		return icons;
	}
	
	public static void openSpecific(Player p, FieldWrapper<Map<String, ItemStack>> custom) {
		Menu menu = new Menu(1, custom.getName() + " Mapping");
		PagedMapping view = new PagedMapping(45, 9);
		
		view.setBackButton("Back", event -> openMain((Player)event.getWhoClicked()));
		
		view.addFrameButton(3, new ItemBuilder(QuestWorld.getIcons().action_accept)
				.wrapText("&2Save changes").get(), event -> {
			QuestWorld.getAPI().onSave();
		}, false);

		int index = 0;
		
		ArrayList<Pair<String, ItemStack>> existingIcons;
		
		switch(custom.getName()) {
		case "mission_icons":
			existingIcons = existingMissionIcons();
			break;
			
		case "editor.custom_world_display":
			existingIcons = existingWorldIcons();
			break;
			
		case "editor.mob_selector":
			existingIcons = existingMobIcons();
			break;
			
		default:
			existingIcons = new ArrayList<>();
			break;
		}
		
		for(Pair<String, ItemStack> pair : existingIcons) {
			String extra = custom.get().containsKey(pair.first()) ? "" : " [default]";
			String clear = extra.isEmpty() ? "  Right click to clear" : "";
			
			view.addButton(index++,
					new ItemBuilder(pair.second())
							.wrapText("&f" + pair.first() + extra, "&c" + clear)
							.get(),
					event -> {
						if (event.isRightClick()) {
							custom.get().remove(pair.first());
						}
						else {
							PlayerInventory inv = event.getWhoClicked().getInventory();
							ItemStack held = inv.getItem(inv.getHeldItemSlot());
					
							if(!ItemBuilder.isAir(held)) {
								custom.get().put(pair.first(), held.clone());
							}
						}
						
						openSpecific((Player)event.getWhoClicked(), custom);
					}, true);
		}
		
		/*view.addButton(index++,
				new ItemBuilder(QuestWorld.getIcons().action_accept)
						.wrapText("-- Create New --")
						.get(),
				event -> {
					PlayerTools.promptInput((Player)event.getWhoClicked(), new SinglePrompt("Hold item in hand and enter a string (or \"cancel\" to cancel)",
							(ctx, input) -> {
								if(!"cancel".equals(input)) {
									PlayerInventory inv = event.getWhoClicked().getInventory();
									ItemStack held = inv.getItem(inv.getHeldItemSlot());
									
									if(ItemBuilder.isAir(held)) {
										SinglePrompt.setNextDisplay(ctx, "Must be holding an item in your hand!");
									}
									custom.get().put(input, held);
								}
								openSpecific((Player)event.getWhoClicked(), custom);
								return true;
					}));
				}, true);*/
		
		view.build(menu, p);
		menu.openFor(p);
	}
}
