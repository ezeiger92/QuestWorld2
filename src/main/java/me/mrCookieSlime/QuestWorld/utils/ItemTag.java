package me.mrCookieSlime.QuestWorld.utils;

public class ItemTag {
	private boolean first = true;
	private String tagBegin = "{";
	private String tagString = "";
	private String tagEnd = "}";
	
	public ItemTag(ItemTag... tags) {
		for(ItemTag tag : tags) {
			append(tag.toString());
		}
	}
	
	@Override
	public String toString() {
		return tagBegin + tagString + tagEnd;
	}
	
	public ItemTag(String tag) {
		tagBegin = tag + ":" + tagBegin;
	}
	
	public final ItemTag append(String tag) {
		if(!first)
			tag = "," + tag;
		
		tagString += tag;
		first = false;
		
		return this;
	}
	
	public final ItemTag append(ItemTag tag) {
		append(tag.tagString);
		
		return this;
	}
}
