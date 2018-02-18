package me.mrCookieSlime.QuestWorld.util.json;

import java.util.Map;

import org.bukkit.entity.Player;

import me.mrCookieSlime.QuestWorld.command.ClickCommand;

public interface Prop {
	void apply(Map<String, String> properties);
	
	Prop BLACK        = new DefaultProp("\"color\"", "\"black\"");
	Prop DARK_BLUE    = new DefaultProp("\"color\"", "\"dark_blue\"");
	Prop DARK_GREEN   = new DefaultProp("\"color\"", "\"dark_green\"");
	Prop DARK_AQUA    = new DefaultProp("\"color\"", "\"dark_aqua\"");
	Prop DARK_RED     = new DefaultProp("\"color\"", "\"dark_red\"");
	Prop DARK_PURPLE  = new DefaultProp("\"color\"", "\"dark_purple\"");
	Prop GOLD         = new DefaultProp("\"color\"", "\"gold\"");
	Prop GRAY         = new DefaultProp("\"color\"", "\"gray\"");
	Prop DARK_GRAY    = new DefaultProp("\"color\"", "\"dark_gray\"");
	Prop BLUE         = new DefaultProp("\"color\"", "\"blue\"");
	Prop GREEN        = new DefaultProp("\"color\"", "\"green\"");
	Prop AQUA         = new DefaultProp("\"color\"", "\"aqua\"");
	Prop RED          = new DefaultProp("\"color\"", "\"red\"");
	Prop LIGHT_PURPLE = new DefaultProp("\"color\"", "\"light_purple\"");
	Prop YELLOW       = new DefaultProp("\"color\"", "\"yellow\"");
	Prop WHITE        = new DefaultProp("\"color\"", "\"white\"");

	Prop MAGIC     = new DefaultProp("\"obfuscated\"",    "\"true\"");
	Prop BOLD      = new DefaultProp("\"bold\"",          "\"true\"");
	Prop STRIKE    = new DefaultProp("\"strikethrough\"", "\"true\"");
	Prop UNDERLINE = new DefaultProp("\"underline\"",     "\"true\"");
	Prop ITALIC    = new DefaultProp("\"italic\"",        "\"true\"");

	Prop NONE = new NullProp();
	static Prop FUSE(Prop... props) { return new FuseProp(props); }
	
	static Prop HOVER_TEXT(String text, Prop... props) {
		return new DefaultProp("\"hoverEvent\"", pre("show_text", new JsonBlob(text, props).toString()));
	}
	
	static Prop CLICK_RUN(String command) {
		return new DefaultProp("\"clickEvent\"", pre("run_command", '"' + command + '"'));
	}
	
	static Prop CLICK_RUN(Player p, Runnable callback) {
		return CLICK_RUN("/qw-invoke " + ClickCommand.add(p.getUniqueId(), callback));
	}
	
	static Prop CLICK_SUGGEST(String command) {
		return new DefaultProp("\"clickEvent\"", pre("suggest_command", '"' + command + '"'));
	}
	
	static Prop CLICK_URL(String url) {
		return new DefaultProp("\"clickEvent\"", pre("open_url", '"' + url + '"'));
	}
	
	////
	
	static String pre(String action, String value) {
		return "{\"action\":\"" + action + "\",\"value\":" + value + "}";
	}
}
