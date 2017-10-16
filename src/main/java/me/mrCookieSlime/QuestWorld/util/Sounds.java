package me.mrCookieSlime.QuestWorld.util;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class Sounds {
	public final SoundList QUEST_CLICK    = new SoundList("sounds.quest.click", 1.0f, 0.2f);
	public final SoundList MISSION_SUBMIT = new SoundList("sounds.quest.mission-submit", 0.3f, 0.7f);
	public final SoundList MISSION_REJECT = new SoundList("sounds.quest.mission-reject");
	public final SoundList QUEST_REWARD   = new SoundList("sounds.quest.reward");
	
	public final SoundList EDITOR_CLICK      = new SoundList("sounds.editor.click", 1.0f, 0.2f);
	public final SoundList DIALOG_ADD        = new SoundList("sounds.editor.dialog-add");
	public final SoundList DESTRUCTIVE_WARN  = new SoundList("sounds.editor.destructive-action-warning");
	public final SoundList DESTRUCTIVE_CLICK = new SoundList("sounds.editor.destructive-action-click", 0.5f, 0.5f);
	
	public final SoundList PARTY_CLICK = new SoundList("sounds.party.click", 1.0f, 0.2f);
	
	private final ConfigurationSection config;
	public Sounds(ConfigurationSection config) {
		this.config = config;
	}

	public class SoundList {
		private Sound sound;
		private float volume;
		private float pitch;
		
		public SoundList(String path, float volume, float pitch) {
			String soundStr = config.getString(path + ".sound").toUpperCase();
			
			try {
				sound = Sound.valueOf(soundStr);
			}
			catch(Exception e) {
				sound = null;
			}

			this.volume = volume;
			this.pitch = pitch;
			
			if(config.contains(path + ".volume"))
				this.volume = (float) config.getDouble(path + ".volume");

			if(config.contains(path + ".pitch"))
				this.pitch = (float) config.getDouble(path + ".pitch");
		}
		
		public SoundList(String path) {
			this(path, 1.0f, 1.0f);
		}
		
		public void playTo(Player p) {
			p.playSound(p.getLocation(), sound, volume, pitch);
		}
	}
}
