package me.mrCookieSlime.QuestWorld.util;

import java.util.List;
import java.util.Locale;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class Sounds {
	public final SoundList QUEST_CLICK;
	public final SoundList MISSION_SUBMIT;
	public final SoundList MISSION_REJECT;
	public final SoundList QUEST_REWARD;

	public final SoundList EDITOR_CLICK;
	public final SoundList DIALOG_ADD;
	public final SoundList DESTRUCTIVE_WARN;
	public final SoundList DESTRUCTIVE_CLICK;
	
	public final SoundList PARTY_CLICK;
	
	public Sounds(ConfigurationSection config) {
		
		QUEST_CLICK    = new SoundList(config.getConfigurationSection("sounds.quest.click"), 1.0f, 0.2f);
		MISSION_SUBMIT = new SoundList(config.getConfigurationSection("sounds.quest.mission-submit"), 0.3f, 0.7f);
		MISSION_REJECT = new SoundList(config.getConfigurationSection("sounds.quest.mission-reject"));
		QUEST_REWARD   = new SoundList(config.getConfigurationSection("sounds.quest.reward"));

		EDITOR_CLICK      = new SoundList(config.getConfigurationSection("sounds.editor.click"), 1.0f, 0.2f);
		DIALOG_ADD        = new SoundList(config.getConfigurationSection("sounds.editor.dialog-add"));
		DESTRUCTIVE_WARN  = new SoundList(config.getConfigurationSection("sounds.editor.destructive-action-warning"));
		DESTRUCTIVE_CLICK = new SoundList(config.getConfigurationSection("sounds.editor.destructive-action-click"), 0.5f, 0.5f);
		
		PARTY_CLICK = new SoundList(config.getConfigurationSection("sounds.party.click"), 1.0f, 0.2f);
	}

	public static class SoundList {
		private Sound sound = null;
		private float volume;
		private float pitch;
		
		public SoundList(ConfigurationSection config, float volume, float pitch) {
			String soundStr = config.getString("sound", null);
			
			if(soundStr == null) {
				List<String> sounds = config.getStringList("list");
				if(!sounds.isEmpty()) {
					soundStr = sounds.get(0);
					Log.warning("Sound path \"" + config.getCurrentPath() + "\" uses an old format, using first sound in list ("+soundStr+")");
					Log.warning("  Please change this from \"list: [sound[, sound, ...]]\" to \"sound: [sound]");
				}
				else {
					Log.severe("No sound found for path \"" + config.getCurrentPath() + "\" in sounds.yml");
					return;
				}
			}
			
			try {
				sound = Sound.valueOf(soundStr.toUpperCase(Locale.US));
			}
			catch(Exception e) {
				Log.severe("The sound \"" + soundStr + "\" is not a valid sound (path = \""+config.getCurrentPath()+"\")");
				return;
			}
			
			this.volume = (float) config.getDouble("volume", volume);
			this.pitch  = (float) config.getDouble("pitch",  pitch);
		}
		
		public SoundList(ConfigurationSection config) {
			this(config, 1.0f, 1.0f);
		}
		
		public void playTo(Player p) {
			p.playSound(p.getLocation(), sound, volume, pitch);
		}
	}
}
