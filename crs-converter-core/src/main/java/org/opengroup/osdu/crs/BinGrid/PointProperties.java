package org.opengroup.osdu.crs.BinGrid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PointProperties {
	
	@JsonProperty("Label")
	private String label;
	@JsonProperty("Inline")
	private Integer inline;
	@JsonProperty("Crossline")
	private Integer crossline;	

}
