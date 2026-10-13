package org.opengroup.osdu.crs.BinGrid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PropertiesBinGridCorners {
	
	@JsonProperty("Kind")
	private String kind;
	@JsonProperty("PointProperties")
	private List<PointProperties> pointPropertiesList;
	
}
