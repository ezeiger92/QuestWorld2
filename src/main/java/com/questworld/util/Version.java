package com.questworld.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Version implements Comparable<Version> {
	private final String serialVersion;
	private final int hash;
	
	private static final Map<String, Version> cache = new HashMap<>();
	
	public static Version ofString(String version) {
		String serialVersion = processVersion(version);
		
		Version result = cache.get(serialVersion);
		
		if(result == null) {
			cache.put(serialVersion, result = new Version(serialVersion));
		}
		
		return result;
	}
	
	private Version(String serialVersion) {
		this.serialVersion = serialVersion;
		this.hash = makeHash(serialVersion);
	}

	@Override
	public final int compareTo(Version other) {
		return Integer.compareUnsigned(other.hashCode(), hashCode());
	}
	
	public boolean lessThan(Version other) {
		return compareTo(other) < 0;
	}
	
	public boolean greaterThan(Version other) {
		return compareTo(other) > 0;
	}

	@Override
	public String toString() {
		return serialVersion;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Version)
			return other.hashCode() == hashCode();

		return false;
	}

	@Override
	public int hashCode() {
		return hash;
	}
	
	// release_version_patch_Rapi_type
	private static final int[] bitPartition = {
			// 16 unused
			2, // MC release, 4
			5, // MC version, 32
			4, // MC patch, 16
			3, // API revision, 8
			2, // API type, 4
	};
	
	private static final int[] bitPosition;
	
	static {
		int[] positions = new int[bitPartition.length];
		
		int cumulative = 0;
		
		for(int i = positions.length - 1; i >= 0; --i) {
			cumulative += bitPartition[i];
			positions[i] = cumulative;
		}
		
		bitPosition = positions;
	}
	
	private static final int makeHash(String serialVersion) {
		String[] ourParts = serialVersion.split("_");

		int length = ourParts.length;
		
		int hash = 0;
		
		for (int i = 0; i < length; ++i) {
			String part = ourParts[i];
			
			int value;
			try {
				value = Integer.parseInt(part);
			}
			catch (NumberFormatException e) {
				if(part.startsWith("R")) {
					try {
						value = (int)(Double.parseDouble(part.substring(1)) * 10);
					}
					catch(NumberFormatException e2) {
						value = 0;
					}
				}
				else {
					value = apiVariant(part);
				}
			}
			
			hash |= (value << bitPosition[i]);
		}
		
		return hash;
	}
	
	private static final String processVersion(String in) {
		in = in.toUpperCase(Locale.ENGLISH).replace('.', '_');

		if (in.startsWith("V"))
			in = in.substring(1);

		return in;
	}

	private static final int apiVariant(String serverKind) {
		switch (serverKind) {
			case "TACO":
			case "TACOSPIGOT":
				return 3;

			case "PAPER":
			case "PAPERSPIGOT":
				return 2;

			case "SPIGOT":
				return 1;

			default:
				return 0;
		}
	}
}
