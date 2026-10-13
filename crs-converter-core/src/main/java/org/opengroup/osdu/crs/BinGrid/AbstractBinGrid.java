package org.opengroup.osdu.crs.BinGrid;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class AbstractBinGrid {
    
	@JsonProperty("BinGridName")
	private String binGridName;

	@JsonProperty("BinGridTypeID")
	private String binGridTypeID;

	@JsonProperty("SourceBinGridID")
	private Integer sourceBinGridID;

	@JsonProperty("SourceBinGridAppID")
	private String sourceBinGridAppID;

	@JsonProperty("CoveragePercent")
	private Double coveragePercent;

	@JsonProperty("BinGridDefinitionMethodTypeID")
	private String binGridDefinitionMethodTypeID;

	@JsonProperty("P6TransformationMethod")
	private Integer p6TransformationMethod;

	@JsonProperty("P6BinGridOriginI")
	private Double p6BinGridOriginI;

	@JsonProperty("P6BinGridOriginJ")
	private Double p6BinGridOriginJ;

	@JsonProperty("P6BinGridOriginEasting")
	private Double p6BinGridOriginEasting;

	@JsonProperty("P6BinGridOriginNorthing")
	private Double p6BinGridOriginNorthing;

	@JsonProperty("P6ScaleFactorOfBinGrid")
	private Double p6ScaleFactorOfBinGrid;

	@JsonProperty("P6BinWidthOnIaxis")
	private Double p6BinWidthOnIaxis;

	@JsonProperty("P6BinWidthOnJaxis")
	private Double p6BinWidthOnJaxis;

	@JsonProperty("P6MapGridBearingOfBinGridJaxis")
	private Double p6MapGridBearingOfBinGridJaxis;

	@JsonProperty("P6BinNodeIncrementOnIaxis")
	private Integer p6BinNodeIncrementOnIaxis;

	@JsonProperty("P6BinNodeIncrementOnJaxis")
	private Integer p6BinNodeIncrementOnJaxis;
	 
	@JsonProperty("ABCDBinGridSpatialLocation")
	private AbstractSpatialLocation aBCDBinGridSpatialLocation;

}
