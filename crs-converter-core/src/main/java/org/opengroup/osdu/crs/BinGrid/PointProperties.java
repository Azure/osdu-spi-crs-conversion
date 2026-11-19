package org.opengroup.osdu.crs.BinGrid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import tools.jackson.databind.annotation.JsonDeserialize;

import org.opengroup.osdu.crs.util.StrictIntegerDeserializer;
import org.opengroup.osdu.crs.util.StrictStringDeserializer;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PointProperties {
	
	@JsonProperty("Label")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String label;
	@NotNull
	@JsonProperty("Inline")
	@JsonDeserialize(using = StrictIntegerDeserializer.class)
	private Integer inline;
	@NotNull
	@JsonProperty("Crossline")
	@JsonDeserialize(using = StrictIntegerDeserializer.class)
	private Integer crossline;	

}
