package org.opengroup.osdu.crs.BinGrid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import tools.jackson.databind.annotation.JsonDeserialize;

import org.opengroup.osdu.crs.util.StrictStringDeserializer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AbstractFeature{	

	@JsonProperty("type")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String type;
	
	@NotNull
	@Valid
	@JsonProperty("properties")
    private PropertiesBinGridCorners properties;	
	
	@NotNull
	@Valid
	@JsonProperty("geometry")
    private Geometry geometry;
	
}
