package me.mrCookieSlime.QuestWorld.api;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.api.annotation.Control;
import me.mrCookieSlime.QuestWorld.api.contract.QuestingAPI;
import me.mrCookieSlime.QuestWorld.util.Reloadable;
import me.mrCookieSlime.QuestWorld.util.ResourceLoader;

// Only abstract because nobody should make a raw instance of this.
public abstract class QuestExtension implements Reloadable {
	private boolean initialized = false;
	private QuestingAPI api = QuestWorld.getAPI();
	private int remaining;
	private String[] requirements;
	private Plugin[] found;
	private MissionType[] types = {};
	private ResourceLoader loader;
	
	/**
	 * Performs setup for this extension. Do not assume plugin dependencies have
	 * been loaded at this point.
	 */
	public QuestExtension(String... requirements) {
		this.requirements = requirements.clone();
		remaining = requirements.length;
		found = new Plugin[remaining];
		loader = new ResourceLoader(getClass().getClassLoader(), api.getPlugin().getDataFolder());
	}
	
	/**
	 * Supplies access to the QuestWorld API. The static form
	 * <tt>QuestWorld.<i>method</i></tt> may also be used, depending on your
	 * preference.
	 * 
	 * @see QuestingAPI
	 * @see QuestWorld
	 * 
	 * @return The API
	 */
	public final QuestingAPI getAPI() {
		return api;
	}
	
	/**
	 * Supplies the extension name, primarily used for printing status info.
	 * <p> The default value is the simple name of the class.
	 * 
	 * @return The extension name
	 */
	@Control
	public String getName() {
		return getClass().getSimpleName();
	}
	
	/**
	 * This must return a list of all plugins that the hook depends on.
	 * 
	 * @return A list of all names of plugin dependencies
	 */
	public final String[] getDepends() {
		return requirements.clone();
	}
	
	public final FileConfiguration getConfiguration(String path) {
		return loader.loadConfigNoexpect(path, true);
	}
	
	public final ResourceLoader getResourceLoader() {
		return loader;
	}
	
	@Control
	@Override
	public void onReload() {
	}
	
	
	@Control
	@Override
	public void onSave() {
	}
	
	@Control
	@Override
	public void onDiscard() {
	}
	
	/**
	 * When all dependencies are found, initialize is called. A handle to
	 * QuestWorld is given so that any custom events/listeners can be
	 * registered. All dependencies can be retrieved with {@link #getPlugins},
	 * in the same order as {@link #getDepends}, after they have been found.
	 * <p>
	 * IMPORTANT: Do NOT try to register listeners from custom MissionTypes!
	 * This is handled by QuestWorld later! See {@link #getMissions} for more.
	 * 
	 * @param parent The handle to QuestWorld
	 */
	@Control
	protected void initialize(Plugin parent) {
	}
	
	/**
	 * Initializes the extension after all dependencies have been located. This
	 * is an internal function and should not be called directly.
	 * 
	 * @param parent The plugin loading extensions (QuestWorld)
	 * 
	 * @throws Throwable Any (likely unchecked) exception raised by
	 * {@link QuestExtension#initialize initialize} will be passed up the stack
	 */
	public final void init(Plugin parent) throws Throwable {
		if(initialized || !isReady())
			return;
		
		// Never trust user code, this may throw anything
		initialize(parent);

		initialized = true;
	}
	
	/**
	 * Set the mission types supplied by this extension.
	 * <p>
	 * If a MissionType implements {@link org.bukkit.event.Listener Listener},
	 * it will be automatically registered by QuestWorld after
	 * {@link #initialize} is called.
	 * 
	 * @return A list of all custom MissionTypes in this hook
	 * @throws IllegalStateException if called after the extension is initialized.
	 */
	public final void setMissionTypes(MissionType... types) {
		if(initialized)
			throw new IllegalStateException("Cannot change mission types after initialization");
		
		this.types = types.clone();
	}
	
	/**
	 * Returns stored mission types set by {@link #setMissionTypes}.
	 * 
	 * @return A list of all custom MissionTypes in this hook
	 */
	public final MissionType[] getMissionTypes() {
		return types.clone();
	}
	
	/**
	 * Only use this if you know what you are doing! Assigns a plugin to a
	 * specific index in the resolved dependencies.
	 * 
	 * @param plugin The plugin matching a dependency
	 * @param index The index of the match
	 */
	public final boolean directEnablePlugin(Plugin plugin, int index) {
		if(!plugin.getName().equals(requirements[index]) || found[index] != null)
			return false;
		
		found[index] = plugin;
		--remaining;
		return true;
	}
	
	/**
	 * Only use this if you know what you are doing!
	 * Tries to add a plugin to the resolved dependencies.
	 * If the plugin name matches a dependency, the plugin is added to resolved dependencies
	 * 
	 * @param plugin The plugin we are attempting to match
	 */
	public final boolean enablePlugin(Plugin plugin) {
		for(int i = 0; i < requirements.length; ++i)
			if(directEnablePlugin(plugin, i))
				return true;
		
		return false;
	}
	
	/**
	 * Checks if all dependencies have been loaded.
	 * 
	 * @return true if all dependencies have been loaded, false otherwise
	 */
	public final boolean isReady() {
		return remaining <= 0;
	}
	
	public final boolean isInitialized() {
		return initialized;
	}
	
	/**
	 * Gets the resolved dependencies.
	 * The order matches the order of {@link #getDepends}.
	 * 
	 * @return The list of resolved dependencies
	 */
	public final Plugin[] getPlugins() {
		if(!isReady())
			throw new IllegalStateException("Attempted to get plugin references before they were resolved");
		
		return found.clone();
	}
}
