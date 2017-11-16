package me.mrCookieSlime.QuestWorld.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import me.mrCookieSlime.QuestWorld.api.QuestExtension;
import me.mrCookieSlime.QuestWorld.util.Log;

public class ExtensionInstaller implements Listener {
	private List<QuestExtension> hooks = new ArrayList<>();
	private Plugin parent;

	public ExtensionInstaller(Plugin parent) {
		this.parent = parent;
		PluginManager manager = parent.getServer().getPluginManager();
		manager.registerEvents(this, parent);
	}
	
	public void add(QuestExtension hook) {
		PluginManager manager = parent.getServer().getPluginManager();
		
		String name = hookName(hook);
		
		Log.fine("Installer - Adding hook: " + name);
		
		String[] reqs = hook.getDepends();
		if(reqs != null)
			for(int i = 0; i < reqs.length; ++i) {
				Plugin p = manager.getPlugin(reqs[i]);
				if(p != null && p.isEnabled())
					hook.directEnablePlugin(p, i);
			}
		
		if(hook.isReady()) {
			Log.fine("Installer - Dependencies found: " + name);
			initialize(hook, name);
		}
		else {
			Log.fine("Installer - Listening for dependencies: " + name);
			hooks.add(hook);
		}
	}
	
	public void addAll(Collection<QuestExtension> hooks) {
		for(QuestExtension hook : hooks)
			add(hook);
	}
	
	private void initialize(QuestExtension hook, String name) {
		Log.fine("Installer - Initializing hook: " + name);
		
		try {
			hook.init(parent);
		}
		catch(Throwable e) {
			Log.warning("Error initializing hook: " + name);
			e.printStackTrace();
		}
	}
	
	private String hookName(QuestExtension hook) {
		String hookName;
		try {
			hookName = hook.getName();
		}
		catch(Throwable e) {
			hookName = hook.getClass().getSimpleName();
			Log.warning("Error getting hook name for class " + hookName);
			e.printStackTrace();
		}
		
		return hookName;
	}
	
	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		Iterator<QuestExtension> iterator = hooks.iterator();
		
		while(iterator.hasNext()) {
			QuestExtension hook = iterator.next();
			hook.enablePlugin(event.getPlugin());
			
			if(hook.isReady()) {
				String name = hookName(hook);
				Log.fine("Installer - Dependencies loaded: " + name);
				initialize(hook, name);
				iterator.remove();
			}
		}
	}
}
