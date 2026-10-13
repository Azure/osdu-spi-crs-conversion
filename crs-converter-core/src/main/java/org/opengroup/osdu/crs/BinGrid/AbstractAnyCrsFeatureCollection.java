package org.opengroup.osdu.crs.BinGrid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AbstractAnyCrsFeatureCollection {
	
	@JsonProperty("persistableReferenceCrs")
	private String persistableReferenceCrs;

	@JsonProperty("CoordinateReferenceSystemID")
	private String CoordinateReferenceSystemID;
	
	@JsonProperty("type")
	private String type;

	@JsonProperty("features")
	private List<AbstractFeature> features;
	
}
