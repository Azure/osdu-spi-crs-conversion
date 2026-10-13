package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.BinGrid.AbstractBinGrid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ConvertBinGridRequest {
	@JsonProperty("toCRS")
	private String toCRS;
	@JsonProperty("inBinGrid")
	private AbstractBinGrid inBinGrid;	

}
