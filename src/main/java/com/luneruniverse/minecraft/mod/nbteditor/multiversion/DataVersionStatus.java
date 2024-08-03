package com.luneruniverse.minecraft.mod.nbteditor.multiversion;

import java.util.Optional;

public enum DataVersionStatus {
	UNKNOWN,
	OUTDATED,
	CURRENT,
	TOO_UPDATED;
	
	public static DataVersionStatus of(Optional<Integer> dataVersion) {
		return dataVersion.map(DataVersionStatus::of).orElse(UNKNOWN);
	}
	public static DataVersionStatus of(int dataVersion) {
		int currentDataVersion = Version.getDataVersion();
		if (dataVersion < currentDataVersion)
			return OUTDATED;
		if (dataVersion == currentDataVersion)
			return CURRENT;
		return TOO_UPDATED;
	}
	
	public boolean canBeUpdated(boolean hasDefaultDataVersion) {
		return this == OUTDATED || (this == UNKNOWN && hasDefaultDataVersion);
	}
	public boolean canBeUsed(boolean hasDefaultDataVersion) {
		return this == CURRENT || canBeUpdated(hasDefaultDataVersion);
	}
}
