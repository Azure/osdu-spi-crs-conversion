package org.opengroup.osdu.crs.BinGrid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import tools.jackson.databind.annotation.JsonDeserialize;
import org.opengroup.osdu.crs.util.StrictStringDeserializer;

import lombok.Data;

@Data
public class AbstractFeatureCollection {
	
	@Valid
	@JsonProperty("features")
    @JsonSetter(nulls = Nulls.FAIL)
    private List<@NotNull AbstractFeature> features;
	
	@JsonProperty("type")
    @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String type;

}
