package com.questworld.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Version implements Comparable<Version> {
	private final String serialVersion;
	
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
	}
	
	private static final String processVersion(String in) {
		in = in.toUpperCase(Locale.ENGLISH).replace('.', '_');

		if (in.startsWith("V"))
			in = in.substring(1);

		return in;
	}

	private static final int apiLevel(String serverKind) {
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

	@Override
	public final int compareTo(Version other) {
		String[] ourParts = toString().split("_");
		String[] theirParts = other.toString().split("_");

		int length = Math.min(ourParts.length, theirParts.length);

		for (int i = 0; i < length; ++i) {
			int ourSubver = 0;
			int theirSubver = 0;

			if (ourParts[i].startsWith("R")) {
				ourParts[i] = ourParts[i].substring(1);
				ourSubver = -1;
			}

			if (theirParts[i].startsWith("R")) {
				theirParts[i] = theirParts[i].substring(1);
				theirSubver = -1;
			}

			if (ourSubver != theirSubver) {
				return Integer.compare(theirSubver, ourSubver);
			}

			try {
				ourSubver = Integer.parseInt(ourParts[i]);
			}
			catch (NumberFormatException e) {}

			try {
				theirSubver = Integer.parseInt(theirParts[i]);
			}
			catch (NumberFormatException e) {}

			if (ourSubver != theirSubver) {
				return Integer.compare(theirSubver, ourSubver);
			}
			else if (ourSubver <= 0) {
				return Integer.compare(apiLevel(theirParts[i]), apiLevel(ourParts[i]));
			}
		}

		return Integer.compare(theirParts.length, ourParts.length);
	}

	@Override
	public String toString() {
		return serialVersion;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Version)
			return toString().equals(other.toString());

		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
