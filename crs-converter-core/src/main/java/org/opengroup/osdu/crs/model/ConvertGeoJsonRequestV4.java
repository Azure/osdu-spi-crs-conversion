package org.opengroup.osdu.crs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import tools.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.util.Constants;
import org.opengroup.osdu.crs.util.StrictStringDeserializer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
@Schema(description = Constants.SWAGGER_GEO_JSON_CONVERT_REQUEST_DESCR)
public class ConvertGeoJsonRequestV4 {


	@JsonProperty("featureCollection")
	@NotNull
	@Schema(description = Constants.SWAGGER_GEO_JSON_FEATURE_COLLECTION)
	@Parameter(required = true)
	private GeoJsonFeatureCollection featureCollection;

	@NotEmpty
	@Schema(format = "osdu-crs-reference", description = Constants.SWAGGER_TARGET_CRS, type = "string",example = Constants.SWAGGER_TARGET_CRS_GEO_EXAMPLE)
	@Parameter(required = true)
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String toCRS;

	@Schema(format = "osdu-unit-reference", description = Constants.SWAGGER_TARGET_Z_UNIT, type = "string",example = Constants.SWAGGER_TARGET_Z_UNIT_EXAMPLE)
	@Parameter(required = true)
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String toUnitZ;
	
	@Schema(description = Constants.SWAGGER_TRANSFORMATION, type = "string",example = Constants.SWAGGER_TRANSFORMATION_EXAMPLE)
	@Parameter(required = false)
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String transformation;
}
