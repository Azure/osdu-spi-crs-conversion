package org.opengroup.osdu.crs.BinGrid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import tools.jackson.databind.annotation.JsonDeserialize;

import org.opengroup.osdu.crs.util.StrictIntegerDeserializer;
import org.opengroup.osdu.crs.util.StrictStringDeserializer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@JsonInclude(Include.NON_NULL)
public class AbstractBinGrid {
    
	@JsonProperty("BinGridName")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String binGridName;

	@JsonProperty("BinGridTypeID")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String binGridTypeID;

	@JsonProperty("SourceBinGridID")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictIntegerDeserializer.class)
	private Integer sourceBinGridID;

	@JsonProperty("SourceBinGridAppID")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String sourceBinGridAppID;

	@JsonProperty("CoveragePercent")
	@JsonSetter(nulls = Nulls.FAIL)
	private Double coveragePercent;

	@JsonProperty("BinGridDefinitionMethodTypeID")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String binGridDefinitionMethodTypeID;

	@JsonProperty("P6TransformationMethod")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictIntegerDeserializer.class)
	private Integer p6TransformationMethod;

	@JsonProperty("P6BinGridOriginI")
	@JsonSetter(nulls = Nulls.FAIL)
	private Double p6BinGridOriginI;

	@JsonProperty("P6BinGridOriginJ")
	@JsonSetter(nulls = Nulls.FAIL)
	private Double p6BinGridOriginJ;

	@JsonProperty("P6BinGridOriginEasting")
	@JsonSetter(nulls = Nulls.FAIL)
	private Double p6BinGridOriginEasting;

	@JsonProperty("P6BinGridOriginNorthing")
	@JsonSetter(nulls = Nulls.FAIL)
	private Double p6BinGridOriginNorthing;

	@JsonProperty("P6ScaleFactorOfBinGrid")
	@JsonSetter(nulls = Nulls.FAIL)
	private Double p6ScaleFactorOfBinGrid;

	@JsonProperty("P6BinWidthOnIaxis")
	@JsonSetter(nulls = Nulls.FAIL)
	private Double p6BinWidthOnIaxis;

	@JsonProperty("P6BinWidthOnJaxis")
	@JsonSetter(nulls = Nulls.FAIL)
	private Double p6BinWidthOnJaxis;

	@JsonProperty("P6MapGridBearingOfBinGridJaxis")
	@JsonSetter(nulls = Nulls.FAIL)
	private Double p6MapGridBearingOfBinGridJaxis;

	@JsonProperty("P6BinNodeIncrementOnIaxis")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictIntegerDeserializer.class)
	private Integer p6BinNodeIncrementOnIaxis;

	@JsonProperty("P6BinNodeIncrementOnJaxis")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictIntegerDeserializer.class)
	private Integer p6BinNodeIncrementOnJaxis;
	 
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@JsonIgnore
	private AbstractSpatialLocation aBCDBinGridSpatialLocation;

	@NotNull
	@Valid
	@JsonProperty("ABCDBinGridSpatialLocation")
	public AbstractSpatialLocation getABCDBinGridSpatialLocation() {
		return aBCDBinGridSpatialLocation;
	}

	@JsonProperty("ABCDBinGridSpatialLocation")
	public void setABCDBinGridSpatialLocation(AbstractSpatialLocation value) {
		this.aBCDBinGridSpatialLocation = value;
	}

}
