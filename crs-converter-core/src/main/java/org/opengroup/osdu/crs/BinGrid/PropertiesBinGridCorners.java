package org.opengroup.osdu.crs.BinGrid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import tools.jackson.databind.annotation.JsonDeserialize;

import org.opengroup.osdu.crs.util.StrictStringDeserializer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PropertiesBinGridCorners {
	
	@JsonProperty("Kind")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String kind;
	@NotEmpty
	@Size(min = 1, max = 1)
	@Valid
	@JsonProperty("PointProperties")
	@JsonSetter(contentNulls = Nulls.FAIL)
	private List<PointProperties> pointPropertiesList;
	
}
