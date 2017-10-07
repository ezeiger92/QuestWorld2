package me.mrCookieSlime.QuestWorld.util;

import java.util.List;

import org.bukkit.entity.Player;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.general.audio.Soundboard;
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
		private List<String> sounds;
		private float volume;
		private float pitch;
		
		public SoundList(String path) {
			this(path, 1F, 1F);
			
		}
		
		public SoundList(String path, float volume, float pitch) {
			Config cfg = QuestWorld.getInstance().getSoundCfg();
			sounds = cfg.getStringList(path + ".list");

			this.volume = volume;
			this.pitch = pitch;
			
			if(cfg.contains(path + ".volume")) {
				this.volume = cfg.getFloat(path + ".volume");
			}

			if(cfg.contains(path + ".pitch")) {
				this.pitch = cfg.getFloat(path + ".pitch");
			}
		}
		
		public String[] get() {
			return sounds.toArray(new String[0]);
		}
		
		public void playTo(Player p) {
			if(!mute)
				p.playSound(p.getLocation(), Soundboard.getLegacySounds(get()), volume, pitch);
			mute = false;
		}
	}
	
	private static boolean mute = false;
	
	public void muteNext() {
		mute = true;
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
