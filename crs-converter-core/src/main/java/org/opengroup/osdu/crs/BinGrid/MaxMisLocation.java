package org.opengroup.osdu.crs.BinGrid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MaxMisLocation {
	
	@JsonProperty("dI")	
	private Double dI;
	@JsonProperty("dJ")	
	private Double dJ;	
	
}
