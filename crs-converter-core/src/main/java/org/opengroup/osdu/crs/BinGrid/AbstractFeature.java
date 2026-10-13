package org.opengroup.osdu.crs.BinGrid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AbstractFeature{	

	@JsonProperty("type")
	private String type;
	
	@JsonProperty("properties")
    private PropertiesBinGridCorners properties;	
	
	@JsonProperty("geometry")
    private Geometry geometry;
	
}
