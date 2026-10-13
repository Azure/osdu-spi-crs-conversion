package org.opengroup.osdu.crs.model;

import java.util.List;

import org.opengroup.osdu.crs.BinGrid.AbstractBinGrid;
import org.opengroup.osdu.crs.BinGrid.MaxMisLocation;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ConvertBinGridResponse {

	@JsonProperty("maxMisLocation")
	private MaxMisLocation maxMisLocation;
	@JsonProperty("outBinGrid")
	private AbstractBinGrid outBinGrid;
	@JsonProperty("AppliedOperations")
	private List<String> appliedOperations;

}
