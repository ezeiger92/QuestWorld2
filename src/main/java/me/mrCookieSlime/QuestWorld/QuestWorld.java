package me.mrCookieSlime.QuestWorld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import me.mrCookieSlime.CSCoreLibPlugin.PluginUtils;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibSetup.CSCoreLibLoader;
import me.mrCookieSlime.QuestWorld.api.QuestLoader;
import me.mrCookieSlime.QuestWorld.api.MissionType;
import me.mrCookieSlime.QuestWorld.api.QuestExtension;
import me.mrCookieSlime.QuestWorld.api.Translator;
import me.mrCookieSlime.QuestWorld.commands.EditorCommand;
import me.mrCookieSlime.QuestWorld.commands.QuestsCommand;
import me.mrCookieSlime.QuestWorld.extensions.askyblock.ASkyBlock;
import me.mrCookieSlime.QuestWorld.extensions.builtin.Builtin;
import me.mrCookieSlime.QuestWorld.extensions.chatreaction.ChatReaction;
import me.mrCookieSlime.QuestWorld.extensions.citizens.Citizens;
import me.mrCookieSlime.QuestWorld.extensions.money.Money;
import me.mrCookieSlime.QuestWorld.extensions.votifier.Votifier;
import me.mrCookieSlime.QuestWorld.listeners.EditorListener;
import me.mrCookieSlime.QuestWorld.listeners.HookInstaller;
import me.mrCookieSlime.QuestWorld.listeners.Input;
import me.mrCookieSlime.QuestWorld.listeners.InputType;
import me.mrCookieSlime.QuestWorld.listeners.PlayerListener;
import me.mrCookieSlime.QuestWorld.listeners.SelfListener;
import me.mrCookieSlime.QuestWorld.managers.MissionViewer;
import me.mrCookieSlime.QuestWorld.managers.PlayerManager;
import me.mrCookieSlime.QuestWorld.quests.Category;
import me.mrCookieSlime.QuestWorld.quests.Mission;
import me.mrCookieSlime.QuestWorld.quests.Quest;
import me.mrCookieSlime.QuestWorld.utils.DummyEconomy;
import me.mrCookieSlime.QuestWorld.utils.Lang;
import me.mrCookieSlime.QuestWorld.utils.Log;
import me.mrCookieSlime.QuestWorld.utils.Sounds;
import me.mrCookieSlime.QuestWorld.utils.Text;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class QuestWorld extends JavaPlugin implements Listener, QuestLoader {
	private static QuestWorld instance = null;
	private long lastSave;

	Config cfg, book, sounds;
	private MissionViewer missionViewer = new MissionViewer();
	private Map<String, MissionType> types      = new HashMap<>();
	private Map<Integer, Category>   categories = new HashMap<>();
	private Map<UUID, PlayerManager>  profiles   = new HashMap<>();
	private Map<UUID, Input>         inputs     = new HashMap<>();
	
	Economy economy = null;
	Sounds eventSounds;
	
	private Lang language;
	private ExtensionLoader extLoader = null;
	private HookInstaller hookInstaller = null;
	
	public static String translate(Translator key, String... replacements) {
		return getInstance().language.translate(key, replacements);
	}
	
	public QuestWorld() {
		instance = this;
		Log.setLogger(getLogger());
		
		extLoader = new ExtensionLoader(this.getClassLoader(), new File(this.getDataFolder(), "extensions"));
		language = new Lang("en_us", getDataFolder(), getClassLoader());
		language.save();
		getServer().getServicesManager().register(QuestLoader.class, this, this, ServicePriority.Normal);
	}
	
	private List<QuestExtension> preEnableHooks = new ArrayList<>();
	public void attach(QuestExtension hook) {
		if(hookInstaller == null)
			preEnableHooks.add(hook);
		else
			hookInstaller.add(hook);
	}
	
	public Set<Mission> getMissionsOf(MissionType type) {
		return missionViewer.getMissionsOf(type);
	}
	
	public Set<Mission> getTickingMissions() {
		return missionViewer.getTickingMissions();
	}
	
	@Override
	public void onLoad() {
		extLoader.loadLocal();
	}
	
	@Override
	public void onEnable() {
		// Initialize all we can before we need CSCoreLib
		hookInstaller = new HookInstaller(this);
		hookInstaller.addAll(preEnableHooks);
		
		if (getServer().getPluginManager().isPluginEnabled("Vault"))
			setupEconomy();
		
		new Builtin();
		new Citizens();
		new ChatReaction();
		new Votifier();
		new ASkyBlock();
		
		// TODO
		//new Money(); - Incomplete
		
		// Attempt to load Core to continue
		CSCoreLibLoader loader = new CSCoreLibLoader(this);
		if(!loader.load())
			return;
		
		if (!new File("data-storage/Quest World").exists()) new File("data-storage/Quest World").mkdirs();
		if (!new File("plugins/QuestWorld/quests").exists()) new File("plugins/QuestWorld/quests").mkdirs();
		if (!new File("plugins/QuestWorld/dialogues").exists()) new File("plugins/QuestWorld/dialogues").mkdirs();
		if (!new File("plugins/QuestWorld/presets").exists()) new File("plugins/QuestWorld/presets").mkdirs();
			
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			
			@Override
			public void run() {
				Log.fine("Retrieving Quest Configuration...");
				load();
				int categories = getCategories().size(), quests = 0;
				for (Category category: getCategories())
					quests += category.getQuests().size();

				Log.fine("Successfully loaded " + categories + " Categories");
				Log.fine("Successfully loaded " + quests + " Quests");
			}
		}, 0L);
		
		loadConfigs();
		
		// Needs sound config loaded
		eventSounds = new Sounds();
		
		getCommand("quests").setExecutor(new QuestsCommand());
		PluginCommand editorCommand = getCommand("questeditor");
		editorCommand.setExecutor(new EditorCommand());
		editorCommand.setAliases(Arrays.asList("qe"));

		new EditorListener(this);
		new PlayerListener(this);
		Bukkit.getPluginManager().registerEvents(missionViewer, this);
		Bukkit.getPluginManager().registerEvents(new SelfListener(), this);
		
		ShapelessRecipe recipe = new ShapelessRecipe(GuideBook.get());
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
		
		int autosave = cfg.getInt("options.autosave-interval");
		if(autosave > 0) {
			autosave = autosave * 20 * 60; // minutes to ticks
			this.getServer().getScheduler().runTaskTimer(this, new Runnable() {
	
				@Override
				public void run() {
					save();
				}
				
			}, autosave, autosave);
		}
	}
	
	private boolean setupEconomy() {
		if(getServer().getPluginManager().getPlugin("Vault") != null) {
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
		    if (economyProvider != null)
		    	economy = economyProvider.getProvider();
		}
		
		if(economy == null) {
			Log.severe("No economy was found! Falling back to dummy (no-op) economy, no money will be transfered!");
			economy = new DummyEconomy();
		}
		
	    return true;
	}
	
	public void loadConfigs() {
		PluginUtils utils = new PluginUtils(this);
		utils.setupConfig();
		cfg = utils.getConfig();
		utils.setupMetrics();
		utils.setupUpdater(77071, getFile());
		
		//TODO make logger info (and other) levels actually work
		//Log.setLevel(cfg.getString("options.log-level"));
		
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
		sounds.setDefaultValue("sounds.quest.mission-submit.volume", 0.3F);
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
		
		for (Category category: this.categories.values()) {
			category.updateParent(new Config("plugins/QuestWorld/quests/" + category.getID() + ".category"));
			for (Quest quest: category.getQuests()) {
				quest.updateParent(new Config("plugins/QuestWorld/quests/" + quest.getID() + "-C" + category.getID() + ".quest"));
			}
		}
		
		lastSave = System.currentTimeMillis();
	}
	
	public void reloadQWConfig() {
		loadConfigs();
		language.reload();
	}
	
	public void reloadQuests() {
		// ... I feel like such a noob to have forgotten Map.clear()
		categories.clear();
		profiles.clear();
		
		load();
	}
	
	@Override
	public void onDisable() {
		unload();
		
		Log.setLogger(null);
		instance = null;
		
		getServer().getServicesManager().unregisterAll(this);
		getServer().getScheduler().cancelTasks(this);
	}
	
	public long getLastSaved() {
		return lastSave;
	}

	// Why wasn't this a thing?!
	public void save() { save(false); }
	public void save(boolean force) {
		Iterator<Map.Entry<Integer,Category>> categories = this.categories.entrySet().iterator();
		while(categories.hasNext())
			categories.next().getValue().save(force);
		
		Iterator<Map.Entry<UUID, PlayerManager>> managers = this.profiles.entrySet().iterator();
		while(managers.hasNext())
			managers.next().getValue().save();
		
		lastSave = System.currentTimeMillis();
	}
	
	public void unload() {
		Iterator<Map.Entry<Integer,Category>> categories = this.categories.entrySet().iterator();
		while(categories.hasNext()) {
			// Force save for now, change this when 100% sure we've updated all quest/category lastModified times
			categories.next().getValue().save(true);
		}
		this.categories.clear();
		
		Iterator<Map.Entry<UUID, PlayerManager>> managers = this.profiles.entrySet().iterator();
		while(managers.hasNext()) {
			managers.next().getValue().save();
		}
		profiles.clear();
	}
	
	public boolean importPreset(String fileName) {
		File file = new File("plugins/QuestWorld/presets/" + fileName);
		byte[] buffer = new byte[1024];
		if (!file.exists())
			return false;
		
		QuestWorld.getInstance().unload();
		try {
			ZipInputStream input = new ZipInputStream(new FileInputStream(file));
			ZipEntry entry = input.getNextEntry();
			
			for (File f: new File("plugins/QuestWorld/quests").listFiles()) {
				f.delete();
			}
			
			while (entry != null) {
				FileOutputStream output = new FileOutputStream(new File("plugins/QuestWorld/quests/" + entry.getName()));
				
				int length;
				while ((length = input.read(buffer)) > 0) {
					output.write(buffer, 0, length);
				}
				
				output.close();
				entry = input.getNextEntry();
			}
			
			input.closeEntry();
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		QuestWorld.getInstance().load();
		return true;
	}
	
	public boolean exportPreset(String fileName) {
		File file = new File("plugins/QuestWorld/presets/" + fileName);
		byte[] buffer = new byte[1024];
		
		if (file.exists()) file.delete();
		
		save(); // Why unload and load in a try/catch block when you can just use a save function?
		
		try {
			file.createNewFile();
			
			ZipOutputStream output = new ZipOutputStream(new FileOutputStream(file));
			for (File f: new File("plugins/QuestWorld/quests").listFiles()) {
				ZipEntry entry = new ZipEntry(f.getName());
				output.putNextEntry(entry);
				FileInputStream input = new FileInputStream(f);
				
				int length;
				while ((length = input.read(buffer)) > 0) {
					output.write(buffer, 0, length);
				}
				
				input.close();
				output.closeEntry();
			}
			output.close();
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static QuestWorld getInstance() {
		return instance;
	}
	
	public Collection<Category> getCategories() {
		return categories.values();
	}
	
	public Category getCategory(int id) {
		return categories.get(id);
	}
	
	public void registerCategory(Category category) {
		categories.put(category.getID(), category);
	}
	
	public void unregisterCategory(Category category) {
		for (Quest quest: category.getQuests()) {
			PlayerManager.clearAllQuestData(quest);
			new File("plugins/QuestWorld/quests/" + quest.getID() + "-C" + category.getID() + ".quest").delete();
		}
		categories.remove(category.getID());
		new File("plugins/QuestWorld/quests/" + category.getID() + ".category").delete();
	}
	
	public void registerManager(PlayerManager manager) {
		this.profiles.put(manager.getUUID(), manager);
	}
	
	public void unregisterManager(PlayerManager manager) {
		this.profiles.remove(manager.getUUID());
	}
	
	public PlayerManager getManager(OfflinePlayer p) {
		return profiles.containsKey(p.getUniqueId()) ? profiles.get(p.getUniqueId()): new PlayerManager(p);
	}
	
	public PlayerManager getManager(String uuid) {
		return profiles.containsKey(UUID.fromString(uuid)) ? profiles.get(UUID.fromString(uuid)): new PlayerManager(UUID.fromString(uuid));
	}
	
	public boolean isManagerLoaded(String uuid) {
		return profiles.containsKey(UUID.fromString(uuid));
	}
	
	public void storeInput(UUID uuid, Input input) {
		this.inputs.put(uuid, input);
	}
	
	public void enable(QuestExtension hook) {
		for(MissionType type : hook.getMissions())
			registerMissionType(type);
	}
	
	private void registerMissionType(MissionType type) {
		Log.fine("Registrar - Storing mission: " + type.getName());
		types.put(type.getName(), type);
		
		if(type instanceof Listener) {
			Log.fine("Registrar - Registering events: " + type.getName());
			getServer().getPluginManager().registerEvents((Listener)type, this);
		}
	}

	public Input getInput(UUID uuid) {
		if (inputs.containsKey(uuid)) return inputs.get(uuid);
		else return new Input(InputType.NONE, null);
	}
	
	public void removeInput(UUID uuid) {
		this.inputs.remove(uuid);
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
