package com.questworld.api.lang;

import com.questworld.api.contract.IParty;

public class PartyReplacements extends BaseReplacements<IParty> {
	private final IParty party;

	public PartyReplacements(IParty party) {
		super("party.");
		this.party = party;
	}
	
	@Override
	public Class<IParty> forClass() {
		return IParty.class;
	}

	@Override
	public String getReplacement(String base, String fullKey) {
		switch (base) {
			case "size":
				return Integer.toString(party.getSize());
				
			case "leader":
				return new PlayerReplacements(party.getLeader()).getReplacement(fullKey.substring(7));
			
		}
		return null;
	}

}
