package org.opengroup.osdu.crs.BinGrid;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import tools.jackson.databind.annotation.JsonDeserialize;

import org.opengroup.osdu.crs.util.StrictStringDeserializer;

import lombok.Data;

@Data
public class Geometry {	
	@JsonProperty("type")
    @JsonDeserialize(using = StrictStringDeserializer.class)
    @NotEmpty
    private String type;

	@JsonProperty("coordinates")
    @JsonSetter(contentNulls = Nulls.FAIL)
    @NotEmpty
    @Size(min = 2, max = 2)
    private List<Double> coordinates;   
       	
}
