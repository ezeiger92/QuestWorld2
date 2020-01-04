package com.questworld.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.bukkit.Bukkit;

public final class Version implements Comparable<Version> {
	private static final String CURRENT_VER;
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
	
	public static Version current() {
		return ofString(CURRENT_VER);
	}
	
	static {
		String pack = Bukkit.getServer().getClass().getPackage().getName();
		CURRENT_VER = pack.substring(pack.lastIndexOf('.') + 1);
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
	
	private static final int[] bitPosition;
	
	static {
		// release_version_patch_Rapi_type
		int[] bitPartition = {
				// 16 unused
				2, // MC release, 4
				5, // MC version, 32
				4, // MC patch, 16
				3, // API revision, 8
				2, // API type, 4
		};
		bitPosition = new int[bitPartition.length];

		bitPosition[0] = bitPartition[0];
		bitPosition[1] = bitPartition[1] + bitPosition[0];
		bitPosition[2] = bitPartition[2] + bitPosition[1];
		bitPosition[3] = bitPartition[3] + bitPosition[2];
		bitPosition[4] = bitPartition[4] + bitPosition[3];
	}
	
	private static final int makeHash(String serialVersion) {
		String[] ourParts = serialVersion.substring(1).split("_");

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
						value = Integer.parseInt(part.substring(1));
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

		return "v" + in;
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
