package me.mrCookieSlime.QuestWorld.util.json;

import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Prop {
	public static final Prop BLACK        = new ColorProp(ChatColor.BLACK);
	public static final Prop DARK_BLUE    = new ColorProp(ChatColor.DARK_BLUE);
	public static final Prop DARK_GREEN   = new ColorProp(ChatColor.DARK_GREEN);
	public static final Prop DARK_AQUA    = new ColorProp(ChatColor.DARK_AQUA);
	public static final Prop DARK_RED     = new ColorProp(ChatColor.DARK_RED);
	public static final Prop DARK_PURPLE  = new ColorProp(ChatColor.DARK_PURPLE);
	public static final Prop GOLD         = new ColorProp(ChatColor.GOLD);
	public static final Prop GRAY         = new ColorProp(ChatColor.GRAY);
	public static final Prop DARK_GRAY    = new ColorProp(ChatColor.DARK_GRAY);
	public static final Prop BLUE         = new ColorProp(ChatColor.BLUE);
	public static final Prop GREEN        = new ColorProp(ChatColor.GREEN);
	public static final Prop AQUA         = new ColorProp(ChatColor.AQUA);
	public static final Prop RED          = new ColorProp(ChatColor.RED);
	public static final Prop LIGHT_PURPLE = new ColorProp(ChatColor.LIGHT_PURPLE);
	public static final Prop YELLOW       = new ColorProp(ChatColor.YELLOW);
	public static final Prop WHITE        = new ColorProp(ChatColor.WHITE);

	public static final Prop MAGIC     = MAGIC(true);
	public static final Prop BOLD      = BOLD(true);
	public static final Prop STRIKE    = STRIKE(true);
	public static final Prop UNDERLINE = UNDERLINE(true);
	public static final Prop ITALIC    = ITALIC(true);
	
	public static final Prop NONE = new NullProp();
	
	public static Prop FUSE(Prop... props) { return new FuseProp(props); }
	
	public static Prop MAGIC    (boolean state) { return new BoolProp("obfuscated",    state); }
	public static Prop BOLD     (boolean state) { return new BoolProp("bold",          state); }
	public static Prop STRIKE   (boolean state) { return new BoolProp("strikethrough", state); }
	public static Prop UNDERLINE(boolean state) { return new BoolProp("underlined",    state); }
	public static Prop ITALIC   (boolean state) { return new BoolProp("italic",        state); }
	
	public static final class HOVER {
		static final String key = "\"hoverEvent\"";
		
		public static Prop TEXT(String text, Prop... props) {
			return new HoverTextProp(text, props);
		}
		
		@Deprecated
		public static Prop ITEM(ItemStack item) {
			return null;
		}
		
		@Deprecated
		public static Prop ENTITY(EntityType type, UUID uuid, String name, Prop... props) {
			return null;
		}
		
		@Deprecated
		public static Prop ACHIEVE() {
			return null;
		}
	}
	
	public static final class CLICK {
		static final String key = "\"clickEvent\"";
		public static Prop RUN(String command) {
			return new ClickProp("\"run_command\"", command);
		}
		
		public static Prop RUN(Player p, Runnable callback) {
			return new ClickRunnableProp(p, callback);
		}
		
		public static Prop SUGGEST(String command) {
			return new ClickProp("\"suggest_command\"", command);
		}
		
		public static Prop URL(String url) {
			return new ClickProp("\"open_url\"", url);
		}
		
		@Deprecated
		public static Prop PAGE() {
			return null;
		}
	}
	
	void apply(Map<String, String> properties);
}
