package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.BinGrid.AbstractBinGrid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import tools.jackson.databind.annotation.JsonDeserialize;
import org.opengroup.osdu.crs.util.StrictStringDeserializer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConvertBinGridRequest {
	@JsonProperty("toCRS")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String toCRS;
	@NotNull
	@Valid
	@JsonProperty("inBinGrid")
	private AbstractBinGrid inBinGrid;	

}
