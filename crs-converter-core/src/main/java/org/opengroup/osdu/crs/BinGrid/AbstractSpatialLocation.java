package org.opengroup.osdu.crs.BinGrid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class AbstractSpatialLocation {

	@JsonProperty("SpatialLocationCoordinatesDate")
	private String spatialLocationCoordinatesDate;

	@JsonProperty("QuantitativeAccuracyBandID")
	private String quantitativeAccuracyBandID;

	@JsonProperty("QualitativeSpatialAccuracyTypeID")
	private String qualitativeSpatialAccuracyTypeID;

	@JsonProperty("CoordinateQualityCheckPerformedBy")
	private String coordinateQualityCheckPerformedBy;

	@JsonProperty("CoordinateQualityCheckDateTime")
	private String coordinateQualityCheckDateTime;

	@JsonProperty("CoordinateQualityCheckRemarks")
	private List<String> coordinateQualityCheckRemarks;

	@JsonProperty("AppliedOperations")
	private List<String> appliedOperations;

	@JsonProperty("SpatialParameterTypeID")
	private String spatialParameterTypeID;

	@JsonProperty("SpatialGeometryTypeID")
	private String spatialGeometryTypeID;	
	
	@JsonProperty("AsIngestedCoordinates")
	private AbstractAnyCrsFeatureCollection asIngestedcoordinates;

	@JsonProperty("Wgs84Coordinates")
	private AbstractFeatureCollection wgs84Coordinates;

}
