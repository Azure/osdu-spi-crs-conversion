package org.opengroup.osdu.crs.BinGrid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AbstractFeatureCollection {
	
	@JsonProperty("features")
    private List<AbstractFeature> features;
	
	@JsonProperty("type")
    private String type;

}
