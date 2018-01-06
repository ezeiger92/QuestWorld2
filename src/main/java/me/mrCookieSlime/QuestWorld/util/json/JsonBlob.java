package me.mrCookieSlime.QuestWorld.util.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.mrCookieSlime.QuestWorld.util.Text;

public class JsonBlob {
	ArrayList<HashMap<String, String>> message = new ArrayList<>();
	
	public JsonBlob() {
	}
	
	public JsonBlob(String text, Prop... props) {
		add(text, props);
	}
	
	public JsonBlob add(String text, Prop... props) {
		HashMap<String, String> properties = new HashMap<>(props.length + 1);
		properties.put("\"text\"", '"'+text+'"');
		for(Prop p : props)
			p.apply(properties);
		
		message.add(properties);
		
		return this;
	}
	
	private static void appendMap(HashMap<String, String> map, StringBuilder builder) {
		builder.append('{');
		
		String prefix = "";
		for(Map.Entry<String, String> entry : map.entrySet()) {
			builder.append(prefix).append(entry.getKey()).append(':').append(entry.getValue());
			prefix = ",";
		}
		
		builder.append('}');
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder().append('[');
		
		String prefix = "";
		for(HashMap<String,String> map : message) {
			appendMap(map, builder.append(prefix));
			prefix = ",";
		}
		
		return builder.append(']').toString();
	}
	
	private static Prop ofChar(char in, Prop normal) {
		switch(in) {
		case '0': return Prop.BLACK;
		case '1': return Prop.DARK_BLUE;
		case '2': return Prop.DARK_GREEN;
		case '3': return Prop.DARK_AQUA;
		case '4': return Prop.DARK_RED;
		case '5': return Prop.DARK_PURPLE;
		case '6': return Prop.GOLD;
		case '7': return Prop.GRAY;
		case '8': return Prop.DARK_GRAY;
		case '9': return Prop.BLUE;
		case 'a': return Prop.GREEN;
		case 'b': return Prop.AQUA;
		case 'c': return Prop.RED;
		case 'd': return Prop.LIGHT_PURPLE;
		case 'e': return Prop.YELLOW;
		case 'f': return Prop.WHITE;
		case 'r': return normal;
		
		case 'o': return Prop.ITALIC;
		case 'l': return Prop.BOLD;
		case 'm': return Prop.STRIKE;
		case 'n': return Prop.UNDERLINE;
		case 'k': return Prop.MAGIC;
		
		default: return Prop.NONE;
		}
	}
	
	public JsonBlob addLegacy(String legacy, Prop... defaults) {
		message.addAll(fromLegacy(legacy, defaults).message);
		return this;
	}
	
	public static JsonBlob fromLegacy(String legacy, Prop... defaults) {
		JsonBlob result = new JsonBlob();
		
		Prop normal = Prop.FUSE(defaults);
		
		Prop color = Prop.NONE;
		Prop style = Prop.NONE;
		int start = 0, end = 0;
		
		for(int i = 0; i < legacy.length() - 1; ++i) {
			if(legacy.charAt(i) == Text.colorChar) {
				char c = Character.toLowerCase(legacy.charAt(i + 1));
				boolean valid = false;
				
				Prop nextColor = color;
				Prop nextStyle = style;
				
				if("0123456789abcdefr".indexOf(c) != -1)  {
					nextColor = ofChar(c, normal);
					nextStyle = Prop.NONE;
					valid = true;
				}
				else if("olmnk".indexOf(c) != -1) {
					nextStyle = ofChar(c, normal);
					valid = true;
				}
				
				if(valid) {
					if(start != end)
						result.add(legacy.substring(start, end), normal, color, style);
					
					color = nextColor;
					style = nextStyle;
					start = end = i + 2;
					++i;
					continue;
				}
			}
			
			++end;
		}
		
		return result.add(legacy.substring(start, legacy.length()), normal, color, style);
	}
}
