package com.questworld.util.json;

import java.util.Map;

class FuseProp implements Prop {
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
