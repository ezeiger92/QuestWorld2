package com.questworld.api.menu;

public class LinkedMenu extends Menu {
	private final boolean isEditor;
	private final Object link;

	public LinkedMenu(int rows, String title, Object link, boolean isEditor) {
		super(rows, title);
		this.isEditor = isEditor;
		this.link = link;
	}

	public Object getLink() {
		return link;
	}

	public boolean isLinked(Object object) {
		if(link == null) {
			return object == null;
		}

		return link.equals(object);
	}

	public boolean isEditor() {
		return isEditor;
	}
}
