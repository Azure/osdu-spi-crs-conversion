package org.opengroup.osdu.crs.model;


public enum AzimuthReferenceType {
	GRID_NORTH,
	TRUE_NORTH;

	public static AzimuthReferenceType getAzimuthReference(String hint) {
		if (hint != null) {
			String az = hint.toLowerCase();
			if (az.contains("mag") || az.contains("mg")) return null; // not supported
			if (az.contains("tn") || az.contains("true")) return TRUE_NORTH;
			if (az.contains("gn") || az.contains("grid")) return GRID_NORTH;
		}
		return null;
	}
}
