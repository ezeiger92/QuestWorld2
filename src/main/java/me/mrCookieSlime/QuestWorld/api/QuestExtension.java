package me.mrCookieSlime.QuestWorld.api;

import org.bukkit.plugin.Plugin;

import me.mrCookieSlime.QuestWorld.api.annotation.Control;
import me.mrCookieSlime.QuestWorld.api.contract.QuestingAPI;

public abstract class QuestExtension {
	private boolean initialized = false;
	private QuestingAPI api = QuestWorld.getAPI();
	private int remaining;
	private String[] requirements;
	private Plugin[] found;
	private MissionType[] types = {};
	
	//TODO: Doc fix
	/**
	 * Performs setup for this extension. In particular, this calls user methods
	 * {@link QuestExtension#setup setup} and
	 * {@link QuestExtension#getDepends getDepends}, in that order.
	 */
	public QuestExtension(String... requirements) {
		this.requirements = requirements.clone();
		remaining = requirements.length;
		found = new Plugin[remaining];
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
	
	//TODO Doc fix
	/**
	 * This must return a list of all plugins that the hook depends on. It is
	 * called after {@link #setup}, so override that method if you need to
	 * prepare dependency names.
	 * 
	 * @return A list of all names of plugin dependencies
	 */
	public final String[] getDepends() {
		return requirements;
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
	
	public void setMissionTypes(MissionType... types) {
		this.types = types.clone();
	}
	
	// TODO fix doc
	/**
	 * This must return all custom MissionTypes so QuestWorld can prepare them.
	 * <p>
	 * If a MissionType implements {@link org.bukkit.event.Listener Listener},
	 * it will be automatically registered by QuestWorld after
	 * {@link #initialize} is called.
	 * 
	 * @return A list of all custom MissionTypes in this hook
	 */
	public final MissionType[] getMissionTypes() {
		return types;
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
			return null;
		
		return found;
	}
}
