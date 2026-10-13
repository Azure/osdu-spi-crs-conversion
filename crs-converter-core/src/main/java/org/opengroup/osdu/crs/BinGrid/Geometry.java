package org.opengroup.osdu.crs.BinGrid;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Geometry {	
	@JsonProperty("type")
    @NotEmpty
    private String type;

	@JsonProperty("coordinates")
    @NotEmpty
    private List<Double> coordinates;   
       	
}
