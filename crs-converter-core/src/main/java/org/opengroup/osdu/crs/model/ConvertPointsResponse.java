package org.opengroup.osdu.crs.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import org.opengroup.osdu.crs.swagger.PointResponse;
import org.opengroup.osdu.crs.util.Constants;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = Constants.SWAGGER_CONVERSION_RESPONSE)
public class ConvertPointsResponse {

	@Schema(description = Constants.SWAGGER_NOF_POINTS_CONVERTED, type = "integer", types = {"integer"})
	private Integer successCount;

	@ArraySchema(schema = @Schema(implementation = PointResponse.class),
            arraySchema = @Schema(description = Constants.SWAGGER_CONVERTED_POINTS))
	private List<Point> points;

	@Schema(description = Constants.SWAGGER_CONVERT_AUDIT)
	private List<String> operationsApplied;
}
