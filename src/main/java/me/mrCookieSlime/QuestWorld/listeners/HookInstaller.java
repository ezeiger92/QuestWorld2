package me.mrCookieSlime.QuestWorld.listeners;

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
import me.mrCookieSlime.QuestWorld.utils.Log;

public class HookInstaller implements Listener {
	private List<QuestExtension> hooks = new ArrayList<>();
	private Plugin parent;

	public HookInstaller(Plugin parent) {
		this.parent = parent;
		PluginManager manager = parent.getServer().getPluginManager();
		manager.registerEvents(this, parent);
	}
	
	public void add(QuestExtension hook) {
		PluginManager manager = parent.getServer().getPluginManager();
		
		Log.fine("Installer - Adding hook: " + hook.getName());
		
		String[] reqs = hook.getDepends();
		if(reqs != null)
			for(int i = 0; i < reqs.length; ++i) {
				Plugin p = manager.getPlugin(reqs[i]);
				if(p != null && p.isEnabled())
					hook.directEnablePlugin(p, i);
			}
		
		if(hook.isReady()) {
			Log.fine("Installer - Dependencies found: " + hook.getName());
			initialize(hook);
		}
		else {
			Log.fine("Installer - Listening for dependencies: " + hook.getName());
			hooks.add(hook);
		}
	}
	
	public void addAll(Collection<QuestExtension> hooks) {
		for(QuestExtension hook : hooks)
			add(hook);
	}
	
	private void initialize(QuestExtension hook) {
		Log.fine("Installer - Initializing hook: " + hook.getName());
		hook.init(parent);
		//parent.registerHook(hook);
	}
	
	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		Iterator<QuestExtension> iterator = hooks.iterator();
		
		while(iterator.hasNext()) {
			QuestExtension hook = iterator.next();
			hook.enablePlugin(event.getPlugin());
			
			if(hook.isReady()) {
				Log.fine("Installer - Dependencies loaded: " + hook.getName());
				initialize(hook);
				iterator.remove();
			}
		}
	}
}
