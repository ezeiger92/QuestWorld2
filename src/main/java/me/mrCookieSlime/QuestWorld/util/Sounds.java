package me.mrCookieSlime.QuestWorld.util;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.mrCookieSlime.QuestWorld.QuestWorld;

public class Sounds {
	private final SoundList questClick = new SoundList("sounds.quest.click", 1F, 0.2F);
	private final SoundList missionSubmit = new SoundList("sounds.quest.mission-submit", 0.3F, 0.7F);
	private final SoundList missionReject = new SoundList("sounds.quest.mission-reject");
	private final SoundList questReward = new SoundList("sounds.quest.reward");
	
	private final SoundList editorClick = new SoundList("sounds.editor.click", 1F, 0.2F);
	private final SoundList dialogAdd = new SoundList("sounds.editor.dialog-add");
	private final SoundList destructiveWarning = new SoundList("sounds.editor.destructive-action-warning");
	private final SoundList destructiveClick = new SoundList("sounds.editor.destructive-action-click", 0.5F, 0.5F);
	
	private final SoundList partyClick = new SoundList("sounds.party.click", 1F, 0.2F);

	public static class SoundList {
		private Sound sound;
		private float volume;
		private float pitch;
		
		public SoundList(String path, float volume, float pitch) {
			ConfigurationSection cfg = QuestWorld.getInstance().getSoundCfg();
			String soundStr = cfg.getString(path + ".sound").toUpperCase();
			
			try {
				sound = Sound.valueOf(soundStr);
			}
			catch(Exception e) {
				sound = null;
			}

			this.volume = volume;
			this.pitch = pitch;
			
			if(cfg.contains(path + ".volume")) {
				this.volume = (float) cfg.getDouble(path + ".volume");
			}

			if(cfg.contains(path + ".pitch")) {
				this.pitch = (float) cfg.getDouble(path + ".pitch");
			}
		}
		
		public SoundList(String path) {
			this(path, 1F, 1F);
		}
		
		public void playTo(Player p) {
			p.playSound(p.getLocation(), sound, volume, pitch);
		}
	}
	
	public SoundList QuestClick() {
		return questClick;
	}
	
	public SoundList MissionSubmit() {
		return missionSubmit;
	}
	
	public SoundList MissionReject() {
		return missionReject;
	}
	
	public SoundList QuestReward() {
		return questReward;
	}
	
	public SoundList EditorClick() {
		return editorClick;
	}
	
	public SoundList DialogAdd() {
		return dialogAdd;
	}
	
	public SoundList DestructiveWarning() {
		return destructiveWarning;
	}
	
	public SoundList DestructiveClick() {
		return destructiveClick;
	}
	
	public SoundList PartyClick() {
		return partyClick;
	}
}
