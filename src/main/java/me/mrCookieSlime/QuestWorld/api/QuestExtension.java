package me.mrCookieSlime.QuestWorld.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.mrCookieSlime.QuestWorld.utils.Log;

public abstract class QuestExtension {
	private String[] requirements;
	private int remaining;
	private Plugin[] found;
	private QuestLoader loader = null;
	
	public final <T> T getService(Class<T> clazz) {
		RegisteredServiceProvider<T> service = Bukkit.getServer().getServicesManager().getRegistration(clazz);
		if(service != null)
			return service.getProvider();
		
		return null;
	}
	
	public QuestExtension() {
		setup();
		loader = getService(QuestLoader.class);
		
		requirements = getDepends();
		if(requirements == null)
			requirements = new String[0];
		remaining = requirements.length;
		found = new Plugin[remaining];
		
		loader.attach(this);
	}
	
	public String getName() {
		return getClass().getSimpleName();
	}
	
	/**
	 * Called before anything else, use this for things not dependent on other
	 * plugins.
	 */
	public void setup() {
	}
	
	/**
	 * This must return a list of all plugins that the hook depends on. It is
	 * called after {@link #setup}, so override that method if you need to
	 * prepare dependency names.
	 * 
	 * @return A list of all names of plugin dependencies
	 */
	public abstract String[] getDepends();
	
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
	protected abstract void initialize(Plugin parent);
	
	private boolean initialized = false;
	public final void init(Plugin parent) {
		if(initialized)
			return;
		
		try {
			initialize(parent);
		}
		catch(RuntimeException e) {
			Log.warning("Failed to initialize hook " + getName());
			e.printStackTrace();
			return;
		}
		loader.enable(this);
		initialized = true;
	}
	
	/**
	 * This must return all custom MissionTypes so QuestWorld can prepare them.
	 * <p>
	 * If a MissionType implements {@link org.bukkit.event.Listener Listener},
	 * it will be automatically registered by QuestWorld after
	 * {@link #initialize} is called.
	 * 
	 * @return A list of all custom MissionTypes in this hook
	 */
	public abstract MissionType[] getMissions();
	
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
	
	/**
	 * Gets the resolved dependencies.
	 * The order matches the order of {@link #getDepends}.
	 * 
	 * @return The list of resolved dependencies
	 */
	public final Plugin[] getPlugins() {
		if(!isReady())
			return null;
		
		return found;
	}
}
