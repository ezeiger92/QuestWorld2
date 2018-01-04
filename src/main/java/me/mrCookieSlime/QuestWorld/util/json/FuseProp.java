package me.mrCookieSlime.QuestWorld.util.json;

import java.util.Map;

public class FuseProp implements Prop {
	private Prop[] props;
	public FuseProp(Prop... props) {
		this.props = props;
	}

	@Override
	public void apply(Map<String, String> properties) {
		for(Prop prop : props)
			prop.apply(properties);
	}

}
