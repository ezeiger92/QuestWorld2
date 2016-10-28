package me.mrCookieSlime.QuestWorld;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.mrCookieSlime.CSCoreLibPlugin.PluginUtils;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Localization;
import me.mrCookieSlime.CSCoreLibPlugin.general.Particles.MC_1_8.ParticleEffect;
import me.mrCookieSlime.CSCoreLibSetup.CSCoreLibLoader;
import me.mrCookieSlime.QuestWorld.commands.EditorCommand;
import me.mrCookieSlime.QuestWorld.commands.QuestsCommand;
import me.mrCookieSlime.QuestWorld.hooks.ASkyBlockListener;
import me.mrCookieSlime.QuestWorld.hooks.ChatReactionListener;
import me.mrCookieSlime.QuestWorld.hooks.CitizensListener;
import me.mrCookieSlime.QuestWorld.hooks.VoteListener;
import me.mrCookieSlime.QuestWorld.listeners.EditorListener;
import me.mrCookieSlime.QuestWorld.listeners.Input;
import me.mrCookieSlime.QuestWorld.listeners.InputType;
import me.mrCookieSlime.QuestWorld.listeners.PlayerListener;
import me.mrCookieSlime.QuestWorld.listeners.TaskListener;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.MissionType;
import me.mrCookieSlime.QuestWorld.quests.MissionType.SubmissionType;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.quests.QuestManager;
import me.mrCookieSlime.QuestWorld.quests.QuestMission;
import me.mrCookieSlime.QuestWorld.quests.QuestStatus;
import me.mrCookieSlime.QuestWorld.quests.missions.*;
import me.mrCookieSlime.QuestWorld.utils.ItemBuilder;
import me.mrCookieSlime.QuestWorld.utils.Sounds;
import me.mrCookieSlime.QuestWorld.utils.Text;
import net.citizensnpcs.api.npc.NPC;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class QuestWorld extends JavaPlugin implements Listener {
	
	private static QuestWorld instance;
	
	public ItemStack guide;
	
	private Map<String, MissionType> types = new HashMap<String, MissionType>();
	
	Config cfg, book, sounds;
	List<Category> categories;
	Map<Integer, Category> categoryIDs;
	
	Set<QuestManager> managers;
	Map<UUID, QuestManager> profiles;
	Map<UUID, Input> inputs;
	
	Localization local;
	Economy economy;
	
	Sounds eventSounds;
	
	boolean citizens;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		CSCoreLibLoader loader = new CSCoreLibLoader(this);
		if (loader.load()) {
			guide = new ItemBuilder(Material.ENCHANTED_BOOK)
				.display("&eQuest Book &7(Right Click)")
				.lore("", "&rYour basic Guide for Quests", "&rIn case you lose it, simply place a", "&rWorkbench into your Crafting Grid")
				.get();
			
			if (!new File("data-storage/Quest World").exists()) new File("data-storage/Quest World").mkdirs();
			if (!new File("plugins/QuestWorld/quests").exists()) new File("plugins/QuestWorld/quests").mkdirs();
			if (!new File("plugins/QuestWorld/dialogues").exists()) new File("plugins/QuestWorld/dialogues").mkdirs();
			if (!new File("plugins/QuestWorld/presets").exists()) new File("plugins/QuestWorld/presets").mkdirs();
			
			instance = this;
			
			registerMissionType(new CraftMission());
			registerMissionType(new SubmitMission());
			registerMissionType(new DetectMission());
			registerMissionType(new KillMission());
			registerMissionType(new KillNamedMission());
			registerMissionType(new FishMission());
			registerMissionType(new LocationMission());
			registerMissionType(new JoinMission());
			registerMissionType(new PlayMission());
			registerMissionType(new MineMission());
			registerMissionType(new LevelMission());
			
			if (getServer().getPluginManager().isPluginEnabled("Votifier")) {
				registerMissionType(new MissionType("VOTIFIER_VOTE", true, false, false, SubmissionType.INTEGER, "Vote %s times", new MaterialData(Material.DIAMOND)));
				new VoteListener(this);
			}
			
			if (getServer().getPluginManager().isPluginEnabled("ChatReaction")) {
				registerMissionType(new MissionType("CHATREACTION_WIN", true, false, false, SubmissionType.INTEGER, "Win %s Game(s) of ChatReaction", new MaterialData(Material.DIAMOND)));
				new ChatReactionListener(this);
			}
			
			if (getServer().getPluginManager().isPluginEnabled("ASkyBlock")) {
				registerMissionType(new MissionType("ASKYBLOCK_REACH_ISLAND_LEVEL", false, false, false, SubmissionType.INTEGER, "Reach Island Level %s", new MaterialData(Material.GRASS)));
				new ASkyBlockListener(this);
			}
			
			citizens = getServer().getPluginManager().isPluginEnabled("Citizens");
			
			if (citizens) {
				registerMissionType(new MissionType("CITIZENS_INTERACT", false, false, false, SubmissionType.CITIZENS_INTERACT, "Talk to %s", new MaterialData(Material.SKULL_ITEM, (byte) 3)));
				registerMissionType(new MissionType("CITIZENS_SUBMIT", false, false, false, SubmissionType.CITIZENS_ITEM, "Give %s&7 to %s", new MaterialData(Material.SKULL_ITEM, (byte) 3)));
				registerMissionType(new MissionType("KILL_NPC", true, true, false, SubmissionType.CITIZENS_KILL, "Kill %s", new MaterialData(Material.SKULL_ITEM, (byte) 3)));
				registerMissionType(new MissionType("ACCEPT_QUEST_FROM_NPC", false, false, false, SubmissionType.CITIZENS_INTERACT, "Accept this Quest by talking to %s", new MaterialData(Material.SKULL_ITEM, (byte) 3)));
				new CitizensListener(this);
			}
			
			categories = new ArrayList<Category>();
			categoryIDs = new HashMap<Integer, Category>();
			managers = new HashSet<QuestManager>();
			profiles = new HashMap<UUID, QuestManager>();
			inputs = new HashMap<UUID, Input>();
			
			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				
				@SuppressWarnings("unused")
				@Override
				public void run() {
					System.out.println("[Quest World 2] Retrieving Quest Configuration...");
					load();
					int categories = 0, quests = 0;
					for (Category category: getCategories()) {
						categories++;
						for (Quest quest: category.getQuests()) {
							quests++;
						}
					}

					QuestManager.updateTickingTasks();
					System.out.println("[Quest World 2] Successfully loaded " + categories + " Categories");
					System.out.println("[Quest World 2] Successfully loaded " + quests + " Quests");
				}
			}, 0L);
			
			loadConfigs();
			
			getCommand("quests").setExecutor(new QuestsCommand());
			getCommand("questeditor").setExecutor(new EditorCommand());

			new EditorListener(this);
			new PlayerListener(this);
			new TaskListener(this);
			
			if (getServer().getPluginManager().isPluginEnabled("Vault")) setupEconomy();
			
			ShapelessRecipe recipe = new ShapelessRecipe(guide);
			recipe.addIngredient(Material.WORKBENCH);
			getServer().addRecipe(recipe);
			
			getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				
				@Override
				public void run() {
					for (Player p: Bukkit.getOnlinePlayers()) {
						getManager(p).update(true);
					}
				}
			}, 0L, cfg.getInt("options.quest-check-delay"));
			
			getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				
				@Override
				public void run() {
					for (Player p: Bukkit.getOnlinePlayers()) {
						getManager(p).save();
					}
				}
			}, 0L, 5 * 60L * 20L);
			
			if (citizens) {
				getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
					
					@Override
					public void run() {
						for (QuestMission task: QuestManager.getCitizenTasks()) {
							NPC npc = task.getCitizen();
							if (npc != null && npc.getEntity() != null) {
								List<Player> players = new ArrayList<Player>();
								for (Entity n: npc.getEntity().getNearbyEntities(20D, 8D, 20D)) {
									if (n instanceof Player) {
										QuestManager manager = getManager((Player) n);
										if (manager.getStatus(task.getQuest()).equals(QuestStatus.AVAILABLE) && manager.hasUnlockedTask(task) && !manager.hasCompletedTask(task)) {
											players.add((Player) n);
										}
									}
								}
								if (!players.isEmpty()) {
									try {
										ParticleEffect.VILLAGER_HAPPY.display(npc.getEntity().getLocation().add(0, 1, 0), 0.5F, 0.7F, 0.5F, 0, 20, players);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				}, 0L, 12L);
			}
			
			eventSounds = new Sounds();
		}
	}
	
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
	    if (economyProvider != null) economy = (Economy)economyProvider.getProvider();

	    return economy != null;
	}
	
	public void loadConfigs() {
		PluginUtils utils = new PluginUtils(this);
		utils.setupConfig();
		cfg = utils.getConfig();
		utils.setupMetrics();
		utils.setupUpdater(77071, getFile());
		utils.setupLocalization();
		local = utils.getLocalization();
		
		local.setPrefix("&4Quest World &7> ");
		local.setDefault("editor.create-category", "&7Please type in the Name of your new Category (Color codes are supported)");
		local.setDefault("editor.create-quest", "&7Please type in the Name of your new Quest (Color codes are supported)");
		local.setDefault("editor.new-category", "&aSuccessfully created a new Category called &7\"&r%name%&7\"");
		local.setDefault("editor.deleted-category", "&cSuccessfully deleted Category");
		local.setDefault("editor.deleted-quest", "&cSuccessfully deleted Quest");
		local.setDefault("editor.rename-category", "&7Please type in the Name for your Category (Color codes are supported)");
		local.setDefault("editor.renamed-category", "&aSuccessfully renamed Category");
		local.setDefault("editor.rename-quest", "&7Please type in the Name for your Quest (Color codes are supported)");
		local.setDefault("editor.renamed-quest", "&aSuccessfully renamed Quest");
		local.setDefault("party.full", "&4You cannot have more than &c4 &4Party Members!");
		local.setDefault("party.invite", "&7Please type in the Name of the Player you want to invite");
		local.setDefault("party.invited", "&7Successfully invited &e%name% &7to your Party");
		local.setDefault("party.invitation", "&e%name% &7has invited you to join their Party");
		local.setDefault("party.not-online", "&cPlayer &4%name% &cis not online");
		local.setDefault("party.join", "&e%name% has accepted your Invitation and joined your Party");
		local.setDefault("party.joined", "&7You are now a member of &e%name%'s &7Party!");
		local.setDefault("party.kicked", "&e%name% &7has been kicked from the Party");
		local.setDefault("party.already", "&4%name% &cis already a Member of a Party");
		local.setDefault("editor.rename-kill-mission", "&7Please type in the Name of the Mob/Player you want to be killed (Color Code supported)");
		local.setDefault("editor.renamed-kill-type", "&aSuccessfully specified a Name for your Mob/Player");
		local.setDefault("editor.link-citizen", "&7Please right click the NPC you want to link with this Quest");
		local.setDefault("editor.link-citizen-finished", "&aSuccessfully linked this NPC");
		
		local.setDefault("notifications.task-completed", "&e&l! &7You have completed a Task for the Quest &b<Quest>", "&e&l! &7Check your Quest Book for more Info");
		local.setDefault("notifications.task-failed-timeframe", "&c&l! &7You failed to complete a Task for the Quest &b<Quest> &7within the given Timeframe.");
		local.setDefault("notifications.task-timeframe-started", "&a&l! &7You have &b<Timeframe> &7of time to &b<Objective>");
		
		local.setDefault("editor.rename-location", "&7Please type in a Name for your Location", "&7Example: Awesomeville");
		local.setDefault("editor.renamed-location", "&aSuccessfully given this Location a Name");
		local.setDefault("editor.permission-quest", "&7Please type in a Permission Node for this Quest. Type \"none\" for no Permission Node");
		local.setDefault("editor.permission-category", "&7Please type in a Permission Node for this Category. Type \"none\" for no Permission Node");
		local.setDefault("editor.permission-set-quest", "&aSuccessfully set a Permission Node for this Quest");
		local.setDefault("editor.permission-set-category", "&aSuccessfully set a Permission Node for this Category");
		local.setDefault("editor.add-dialogue", "&7Please type in the Message you want to add to the Dialogue! You can do this multiple times, simply type &eexit() &7when you are done! You can also add Commands! Just type in your command &e(ex. /say hello) &7and it will be executed within the Dialogue, use &e<player> &7for the Player's Username");
		local.setDefault("editor.set-dialogue", "&7Successfully set a Dialogue!", "&7If you want to change something you can edit the Dialogue at any time at", "&e<path>");
		local.setDefault("editor.edit-mission-name", "&aSuccessfully edited the Mission's Display Name");
		local.setDefault("editor.await-mission-name", "&7Please type in a Custom Name for this Mission.");
		local.setDefault("editor.misssion-description", "&7Please type in a Description for your Quest");
		local.save();
		
		book = new Config("plugins/QuestWorld/questbook_local.yml");
		book.setDefaultValue("gui.title", "&e&lQuest Book");
		book.setDefaultValue("gui.party", "&eParty Menu");
		book.setDefaultValue("button.open", "&7> Click to open");
		book.setDefaultValue("button.back.party", "&7> Click to go back to the Party Menu");
		book.setDefaultValue("button.back.quests", "&7> Click to go back to the Quest Menu");
		book.setDefaultValue("button.back.general", "&c< Back");
		book.setDefaultValue("quests.locked", "&4&lLOCKED");
		book.setDefaultValue("quests.locked-in-world", "&cThis Questline is not available in your World");
		book.setDefaultValue("quests.tasks_completed", " Tasks completed");
		book.setDefaultValue("quests.state.cooldown", "&e&lON COOLDOWN");
		book.setDefaultValue("quests.state.completed", "&2&lCOMPLETED");
		book.setDefaultValue("quests.state.reward_claimable", "&5&lUNCLAIMED REWARD");
		book.setDefaultValue("quests.state.reward_claim", "&5&lCLAIM REWARD");
		book.setDefaultValue("quests.display.cooldown", "&7Cooldown");
		book.setDefaultValue("quests.display.monetary", "&7Monetary Reward");
		book.setDefaultValue("quests.display.exp", "&7XP Reward");
		book.setDefaultValue("quests.display.rewards", "&rRewards");
		book.setDefaultValue("category.desc.total", " Quests in total");
		book.setDefaultValue("category.desc.completed", " completed Quests");
		book.setDefaultValue("category.desc.available", " Quests available for completion");
		book.setDefaultValue("category.desc.cooldown", " Quests are on Cooldown");
		book.setDefaultValue("category.desc.claimable_reward", " Quests with unclaimed Reward");
		book.setDefaultValue("task.locked", "&4&lLOCKED");
		book.save();
		
		sounds = new Config("plugins/QuestWorld/sounds.yml");
		sounds.setDefaultValue("sounds.quest.click.list", Arrays.asList("ENTITY_PLAYER_LEVELUP", "LEVEL_UP"));
		sounds.setDefaultValue("sounds.quest.click.pitch", 0.2F);
		sounds.setDefaultValue("sounds.quest.mission-submit.list", Arrays.asList("ENTITY_EXPERIENCE_ORB_PICKUP"));
		sounds.setDefaultValue("sounds.quest.mission-submit.pitch", 0.7F);
		sounds.setDefaultValue("sounds.quest.mission-submit.pitch", 0.3F);
		sounds.setDefaultValue("sounds.quest.mission-reject.list", Arrays.asList("BLOCK_NOTE_SNARE", "NOTE_SNARE"));
		sounds.setDefaultValue("sounds.quest.reward.list", Arrays.asList("ENTITY_ITEM_PICKUP", "ITEM_PICKUP"));
		sounds.setDefaultValue("sounds.editor.click.list", Arrays.asList("UI_BUTTON_CLICK", "CLICK"));
		sounds.setDefaultValue("sounds.editor.click.pitch", 0.2F);
		sounds.setDefaultValue("sounds.editor.dialog-add.list", Arrays.asList("ENTITY_PLAYER_LEVELUP", "LEVEL_UP"));
		sounds.setDefaultValue("sounds.editor.destructive-action-warning.list", Arrays.asList("NOTE_PLING", "BLOCK_NOTE_HARP"));
		sounds.setDefaultValue("sounds.editor.destructive-action-click.list", Arrays.asList("ENTITY_BAT_DEATH", "BAT_DEATH"));
		sounds.setDefaultValue("sounds.editor.destructive-action-click.pitch", 0.5F);
		sounds.setDefaultValue("sounds.editor.destructive-action-click.volume", 0.5F);
		sounds.setDefaultValue("sounds.party.click.list", Arrays.asList("BLOCK_NOTE_PLING", "NOTE_PLING"));
		sounds.setDefaultValue("sounds.party.click.pitch", 0.2F);
		sounds.save();
	}
	
	public void load() {
		Set<File> categories = new HashSet<File>();
		Map<Integer, List<File>> quests = new HashMap<Integer, List<File>>();
		for (File file: new File("plugins/QuestWorld/quests").listFiles()) {
			if (file.getName().endsWith(".quest")) {
				Config cfg = new Config(file);
				int category = cfg.getInt("category");
				List<File> files = new ArrayList<File>();
				if (quests.containsKey(category)) files = quests.get(category);
				files.add(file);
				quests.put(category, files);
			}
			else if (file.getName().endsWith(".category")) categories.add(file);
		}
		
		for (File file: categories) {
			int id = Integer.parseInt(file.getName().replace(".category", ""));
			List<File> files = new ArrayList<File>();
			if (quests.containsKey(id)) files = quests.get(id);
			new Category(file, files);
		}
		
		for (Category category: this.categories) {
			category.updateParent(new Config("plugins/QuestWorld/quests/" + category.getID() + ".category"));
			for (Quest quest: category.getQuests()) {
				quest.updateParent(new Config("plugins/QuestWorld/quests/" + quest.getID() + "-C" + category.getID() + ".quest"));
			}
		}
	}
	
	@Override
	public void onDisable() {
		unload();
		
		instance = null;
		QuestManager.ticking_tasks = null;
	}
	
	public void unload() {
		Iterator<Category> categories = this.categories.iterator();
		while(categories.hasNext()) {
			categories.next().save();
			categories.remove();
		}
		
		Iterator<QuestManager> managers = this.managers.iterator();
		while(managers.hasNext()) {
			managers.next().save();
			managers.remove();
		}
	}

	public static QuestWorld getInstance() {
		return instance;
	}
	
	public List<Category> getCategories() {
		return categories;
	}
	
	public Category getCategory(int id) {
		return categoryIDs.get(id);
	}
	
	public void registerCategory(Category category) {
		categories.add(category);
		categoryIDs.put(category.getID(), category);
	}
	
	public void unregisterCategory(Category category) {
		for (Quest quest: category.getQuests()) {
			QuestManager.clearAllQuestData(quest);
			new File("plugins/QuestWorld/quests/" + quest.getID() + "-C" + category.getID() + ".quest").delete();
		}
		categories.remove(category);
		categoryIDs.remove(category.getID());
		new File("plugins/QuestWorld/quests/" + category.getID() + ".category").delete();
	}
	
	public void registerManager(QuestManager manager) {
		this.managers.add(manager);
		this.profiles.put(manager.getUUID(), manager);
	}
	
	public void unregisterManager(QuestManager manager) {
		this.managers.remove(manager);
		this.profiles.remove(manager.getUUID());
	}
	
	public Set<QuestManager> getManagers() {
		return managers;
	}
	
	public QuestManager getManager(OfflinePlayer p) {
		return profiles.containsKey(p.getUniqueId()) ? profiles.get(p.getUniqueId()): new QuestManager(p);
	}
	
	public QuestManager getManager(String uuid) {
		return profiles.containsKey(UUID.fromString(uuid)) ? profiles.get(UUID.fromString(uuid)): new QuestManager(UUID.fromString(uuid));
	}
	
	public boolean isManagerLoaded(String uuid) {
		return profiles.containsKey(UUID.fromString(uuid));
	}
	
	public void storeInput(UUID uuid, Input input) {
		this.inputs.put(uuid, input);
	}
	
	public void registerMissionType(MissionType type) {
		types.put(type.getID(), type);
	}

	public Input getInput(UUID uuid) {
		if (inputs.containsKey(uuid)) return inputs.get(uuid);
		else return new Input(InputType.NONE, null);
	}
	
	public void removeInput(UUID uuid) {
		this.inputs.remove(uuid);
	}

	public Localization getLocalization() {
		return local;
	}
	
	public boolean isItemSimiliar(ItemStack item, ItemStack SFitem) {
		if(item == null || SFitem == null)
			return item == SFitem;
		
		return item.isSimilar(SFitem);
	}

	public Config getCfg() {
		return cfg;
	}
	
	public Config getSoundCfg() {
		return sounds;
	}
	
	public Economy getEconomy() {
		return economy;
	}

	public Map<String, MissionType> getMissionTypes() {
		return types;
	}
	
	public String getBookLocal(String input) {
		return Text.colorize(book.getString(input));
	}
	
	public static Sounds getSounds() {
		return instance.eventSounds;
	}
}
