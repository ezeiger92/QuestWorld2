package com.questworld.api.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.questworld.util.Log;

public class ConfigObject {
	
	private static Walker configWalker = new Walker();
	private String saveLocation = null;
	
	public ConfigObject() {
		Section section = getClass().getAnnotation(Section.class);
		
		if(section != null) {
			saveLocation = section.path();
			
			if(!saveLocation.endsWith(".yml")) {
				saveLocation += ".yml";
			}
		}
		else {
			saveLocation = "config.yml";
		}
	}
	
	/**
	 * Returns the Object found at <tt>path</tt> by walking through this
	 * instance via reflection
	 * 
	 * @param path
	 *   the object path, separated by <tt>.</tt> or <tt>/</tt>
	 * 
	 * @return
	 *   the object found at <tt>path</tt>
	 * 
	 * @throws ClassCastException
	 *   if the type <tt>T</tt> is not a subclass of the resulting object's class.
	 * 
	 * @throws NoSuchFieldException
	 *   if <tt>path</tt> did not lead to an accessible field.
	 * 
	 * @throws NullPointerException
	 *   if <tt>path</tt> is null or an object along <tt>path</tt> is null.
	 * 
	 * @throws SecurityException
	 *   same conditions as {@link java.lang.Class#getField(String) Class.getField}
	 * 
	 * @author Erik Zeiger
	 */
	public <T> T resolve(String path) throws NoSuchFieldException {
		Object iter = this;
		try {
			for(String segment : path.split("[./]"))
				iter = iter.getClass().getField(segment).get(iter);
		}
		catch(IllegalArgumentException | IllegalAccessException e) {
			// These shouldn't be possible
			// IllegalAccess: field was private - getField only returns public fields
			// IllegalArgument: object/field type mismatch - We use the object's class to get the field
		}
		@SuppressWarnings("unchecked")
		T result = (T) iter;
		return result;
	}
	
	private File getFile(Plugin parent) {
		File target = new File(parent.getDataFolder(), saveLocation);
		if(!target.exists()) {
			try {
				parent.saveResource(saveLocation, false);
			}
			catch(IllegalArgumentException e) {
				// No file in jar. This is fine.
				try {
					target.getParentFile().mkdirs();
					target.createNewFile();
				} catch (IOException e1) {
					// :(
					Log.getLogger().log(Level.SEVERE, "Failed to create file: " + target.getAbsolutePath(), e1);
				}
			}
		}
		
		return target;
	}
	
	private void saveConfigToFile(YamlConfiguration config, File target) {
		try {
			config.save(target);
		}
		catch(IOException e) {
			Log.getLogger().log(Level.SEVERE, "Failed to save file: " + target.getAbsolutePath(), e);
		}
	}
	
	public void init(Plugin parent) {
		File target = getFile(parent);
		
		YamlConfiguration config = YamlConfiguration.loadConfiguration(target);
		final InputStream defConfigStream = parent.getResource(saveLocation);
        if (defConfigStream == null) {
        	readFrom(config);
            return;
        }

        config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));

		if(mergeWith(config)) {
			saveConfigToFile(config, target);
		}
	}
	
	public void save(Plugin parent) {
		File target = getFile(parent);
		
		YamlConfiguration config = new YamlConfiguration();
		writeTo(config);
		saveConfigToFile(config, target);
	}
	
	/**
	 * Walks through <tt>root</tt>, reading all values into this instance.
	 * 
	 * @param root
	 *   a <tt>ConfigurationSection</tt> containing keys/values that match
	 *   variables in this instance by name and type. If a supplied name does
	 *   not match an instance name, it is ignored. If no supplied name matches
	 *   an instance name, the instance default is used If the types of the
	 *   instance and supplied values do not match, a ClassCastException is
	 *   thrown.
	 *   
	 * @throws ClassCastException
	 *   if a supplied value does not match the type expected by this instance
	 *   value
	 */
	public void readFrom(ConfigurationSection root) {
		configWalker.walk(root, this, new TrivialVisitor(ConfigAction.LOAD));
	}
	
	/**
	 * 
	 * @param root
	 * @return
	 */
	public boolean mergeWith(ConfigurationSection root) {
		UpdatingVisitor visitor = new UpdatingVisitor();
		configWalker.walk(root, this, visitor);
		
		return visitor.UpdatesConfig();
	}
	
	/**
	 * 
	 * @param root
	 */
	public void writeTo(ConfigurationSection root) {
		configWalker.walk(root, this, new TrivialVisitor(ConfigAction.SAVE));
	}
	
	/**
	 * Sends <tt>visitor</tt> across all public fields of this
	 * <tt>ConfigObject</tt> instance. <tt>root</tt>, <tt>visitor</tt>, this
	 * instance, or any combination of these may be modified by this call,
	 * defending on the implementation of <tt>visitor</tt>.
	 * 
	 * @param root
	 *   the read and/or write for data synchronization with this instance
	 * 
	 * @param visitor
	 *   the <tt>ConfigVisitor</tt> to send each branch and leaf to, which
	 *   modifies traversal behavior
	 */
	public void walk(ConfigurationSection root, ConfigVisitor visitor) {
		configWalker.walk(root, this, visitor);
	}
	
	public Map<String, Object> serialize() {
		return configWalker.serialize(this);
	}
	
	public static <T extends ConfigObject> T deserialize(Class<T> clazz, Map<String, Object> serialData) {
		return configWalker.deserialize(clazz, serialData);
	}
	
	/**
	 * Returns a string representation of the <tt>String: Object</tt> values in
	 * this <tt>ConfigObject</tt> instance.
	 */
	@Override
	public String toString() {
		String res = serialize().toString();
		StringBuilder sb = new StringBuilder();
		
		List<Character> bak = new ArrayList<>();

		for(int i = 0; i < res.length(); ++i) {
			char c = res.charAt(i);
			switch(c) {
			case '[': case '{':
				char c2 = res.charAt(i + 1);
				if(c2 == ']' || c2 == '}') {
					sb.append(c).append(c2);
					++i;
					break;
				}
				bak.add(c);
				sb.append('\n');
				for(int j = 1; j < bak.size(); ++j)
					sb.append("  ");
				if(c == '[')
					sb.append("- ");
				break;
				
			case ']': case '}':
				if(bak.size() > 0)
					bak.remove(bak.size() - 1);
				break;
				
			case ',':
				++i;
				sb.append('\n');
				for(int j = 1; j < bak.size(); ++j)
					sb.append("  ");
				if(bak.size() > 0 && bak.get(bak.size() - 1) == '[')
					sb.append("- ");
				break;
				
			case '\n':
				sb.append('\n');
				for(int j = 0; j < bak.size(); ++j)
					sb.append("  ");
				break;
				
			case '=':
				sb.append(": ");
				break;
				
			default:
				sb.append(c);
				break;
			}
		}
		
		return sb.toString();
	}
}
