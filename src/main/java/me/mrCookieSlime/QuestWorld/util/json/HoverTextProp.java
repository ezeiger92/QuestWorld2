package me.mrCookieSlime.QuestWorld.util.json;

import java.util.Map;

class HoverTextProp extends JsonBlob implements Prop {
	
	public HoverTextProp(String text, Prop... props) {
		super(text, props);
	}
	
	@Override
	public void apply(Map<String, String> properties) {
		properties.put(Prop.HOVER.key, "{\"action\":\"show_text\",\"value\":" + toString() + "}");
	}

}
