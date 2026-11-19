package org.opengroup.osdu.crs.BinGrid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import tools.jackson.databind.annotation.JsonDeserialize;

import org.opengroup.osdu.crs.util.StrictStringDeserializer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class AbstractSpatialLocation {

	@JsonProperty("SpatialLocationCoordinatesDate")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String spatialLocationCoordinatesDate;

	@JsonProperty("QuantitativeAccuracyBandID")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String quantitativeAccuracyBandID;

	@JsonProperty("QualitativeSpatialAccuracyTypeID")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String qualitativeSpatialAccuracyTypeID;

	@JsonProperty("CoordinateQualityCheckPerformedBy")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String coordinateQualityCheckPerformedBy;

	@JsonProperty("CoordinateQualityCheckDateTime")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String coordinateQualityCheckDateTime;

	@JsonProperty("CoordinateQualityCheckRemarks")
	@JsonSetter(nulls = Nulls.FAIL, contentNulls = Nulls.FAIL)
	@JsonDeserialize(contentUsing = StrictStringDeserializer.class)
	private List<String> coordinateQualityCheckRemarks;

	@JsonProperty("AppliedOperations")
	@JsonSetter(nulls = Nulls.FAIL, contentNulls = Nulls.FAIL)
	@JsonDeserialize(contentUsing = StrictStringDeserializer.class)
	private List<String> appliedOperations;

	@JsonProperty("SpatialParameterTypeID")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String spatialParameterTypeID;

	@JsonProperty("SpatialGeometryTypeID")
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String spatialGeometryTypeID;	
	
	@NotNull
	@Valid
	@JsonProperty("AsIngestedCoordinates")
	private AbstractAnyCrsFeatureCollection asIngestedcoordinates;

	@Valid
	@JsonProperty("Wgs84Coordinates")
	@JsonSetter(nulls = Nulls.FAIL)
	private AbstractFeatureCollection wgs84Coordinates;

}
