package org.opengroup.osdu.crs.model;

import com.google.common.base.Strings;

public enum WellKnownTextType {

	GEOGCS, GEOGTRAN, PROJCS, VERTCS;

	public static WellKnownTextType getWellKnownTextType(String wellKnowText) {

		if (!Strings.isNullOrEmpty(wellKnowText)) {
			if (wellKnowText.toUpperCase().startsWith("GEOGCS")) {
				return WellKnownTextType.GEOGCS;
			}
			if (wellKnowText.toUpperCase().startsWith("GEOGTRAN")) {
				return WellKnownTextType.GEOGTRAN;
			}
			if (wellKnowText.toUpperCase().startsWith("PROJCS")) {
				return WellKnownTextType.PROJCS;
			}
			if (wellKnowText.toUpperCase().startsWith("VERTCS")) {
				return WellKnownTextType.VERTCS;
			}
		}

		return null;
	}
}
