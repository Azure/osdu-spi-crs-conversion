package org.opengroup.osdu.crs.model;

public enum CRSType {

	EARLY_BOUND("EBCRS"),

	LATE_BOUND("LBCRS"),

	TRF("STRF"),

	COMPOUND_TRF("CTRF");

	private final String type;

	CRSType(final String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return this.type;
	}
}