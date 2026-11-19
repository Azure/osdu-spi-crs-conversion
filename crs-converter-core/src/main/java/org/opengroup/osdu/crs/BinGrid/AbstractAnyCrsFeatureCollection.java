package org.opengroup.osdu.crs.BinGrid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import tools.jackson.databind.annotation.JsonDeserialize;

import org.opengroup.osdu.crs.util.StrictStringDeserializer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AbstractAnyCrsFeatureCollection {
	
	@JsonProperty("persistableReferenceCrs")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String persistableReferenceCrs;

	@JsonProperty("CoordinateReferenceSystemID")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String CoordinateReferenceSystemID;

	@JsonProperty("coordinateReferenceSystemID")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	public void setCoordinateReferenceSystemID(String coordinateReferenceSystemID) {
		this.CoordinateReferenceSystemID = coordinateReferenceSystemID;
	}
	
	@JsonProperty("type")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String type;

	@NotNull
	@Valid
	@JsonProperty("features")
	@Size(min = 4, max = 4)
	private List<@NotNull AbstractFeature> features;
	
}
