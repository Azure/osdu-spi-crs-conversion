package org.opengroup.osdu.crs.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CRSType {

	@JsonProperty("EBCRS")
	EARLY_BOUND("EBCRS"),

	@JsonProperty("LBCRS")
	LATE_BOUND("LBCRS"),

	@JsonProperty("STRF")
	TRF("STRF"),

	@JsonProperty("CTRF")
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