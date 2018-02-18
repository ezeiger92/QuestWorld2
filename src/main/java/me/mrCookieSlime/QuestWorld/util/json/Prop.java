package me.mrCookieSlime.QuestWorld.util.json;

import java.util.Map;

import org.bukkit.entity.Player;

import me.mrCookieSlime.QuestWorld.command.ClickCommand;

public interface Prop {
	void apply(Map<String, String> properties);
	
	public static final Prop BLACK        = new DefaultProp("\"color\"", "\"black\"");
	public static final Prop DARK_BLUE    = new DefaultProp("\"color\"", "\"dark_blue\"");
	public static final Prop DARK_GREEN   = new DefaultProp("\"color\"", "\"dark_green\"");
	public static final Prop DARK_AQUA    = new DefaultProp("\"color\"", "\"dark_aqua\"");
	public static final Prop DARK_RED     = new DefaultProp("\"color\"", "\"dark_red\"");
	public static final Prop DARK_PURPLE  = new DefaultProp("\"color\"", "\"dark_purple\"");
	public static final Prop GOLD         = new DefaultProp("\"color\"", "\"gold\"");
	public static final Prop GRAY         = new DefaultProp("\"color\"", "\"gray\"");
	public static final Prop DARK_GRAY    = new DefaultProp("\"color\"", "\"dark_gray\"");
	public static final Prop BLUE         = new DefaultProp("\"color\"", "\"blue\"");
	public static final Prop GREEN        = new DefaultProp("\"color\"", "\"green\"");
	public static final Prop AQUA         = new DefaultProp("\"color\"", "\"aqua\"");
	public static final Prop RED          = new DefaultProp("\"color\"", "\"red\"");
	public static final Prop LIGHT_PURPLE = new DefaultProp("\"color\"", "\"light_purple\"");
	public static final Prop YELLOW       = new DefaultProp("\"color\"", "\"yellow\"");
	public static final Prop WHITE        = new DefaultProp("\"color\"", "\"white\"");

	public static final Prop MAGIC     = new DefaultProp("\"obfuscated\"",    "\"true\"");
	public static final Prop BOLD      = new DefaultProp("\"bold\"",          "\"true\"");
	public static final Prop STRIKE    = new DefaultProp("\"strikethrough\"", "\"true\"");
	public static final Prop UNDERLINE = new DefaultProp("\"underline\"",     "\"true\"");
	public static final Prop ITALIC    = new DefaultProp("\"italic\"",        "\"true\"");

	public static final Prop NONE = new NullProp();
	public static Prop FUSE(Prop... props) { return new FuseProp(props); }
	
	public static Prop HOVER_TEXT(String text, Prop... props) {
		return new DefaultProp("\"hoverEvent\"", pre("show_text", new JsonBlob(text, props).toString()));
	}
	
	public static Prop CLICK_RUN(String command) {
		return new DefaultProp("\"clickEvent\"", pre("run_command", '"' + command + '"'));
	}
	
	public static Prop CLICK_RUN(Player p, Runnable callback) {
		return CLICK_RUN("/qw-invoke " + ClickCommand.add(p.getUniqueId(), callback));
	}
	
	public static Prop CLICK_SUGGEST(String command) {
		return new DefaultProp("\"clickEvent\"", pre("suggest_command", '"' + command + '"'));
	}
	
	public static Prop CLICK_URL(String url) {
		return new DefaultProp("\"clickEvent\"", pre("open_url", '"' + url + '"'));
	}
	
	////
	
	static String pre(String action, String value) {
		return "{\"action\":\"" + action + "\",\"value\":" + value + "}";
	}
}
