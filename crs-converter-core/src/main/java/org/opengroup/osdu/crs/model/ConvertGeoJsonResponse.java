package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.util.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = Constants.SWAGGER_GEO_JSON_CONVERSION_RESPONSE)
public class ConvertGeoJsonResponse {

	@Schema(description = Constants.SWAGGER_GEO_JSON_SUCCESS_COUNT, type = "integer")
	private Integer successCount;

	@Schema(description = Constants.SWAGGER_GEO_JSON_COORDINATE_COUNT, type = "integer")
	private Integer totalCount;

	@Schema(description = Constants.SWAGGER_GEO_JSON_CONVERTED)
	private GeoJsonFeatureCollection featureCollection;

	@Schema(description = Constants.SWAGGER_CONVERT_AUDIT)
	private List<String> operationsApplied;
}