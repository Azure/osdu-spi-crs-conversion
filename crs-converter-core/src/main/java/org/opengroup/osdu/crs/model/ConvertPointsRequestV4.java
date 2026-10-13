package org.opengroup.osdu.crs.model;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.opengroup.osdu.crs.util.Constants;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Schema(description = Constants.SWAGGER_CONVERT_REQUEST_DESCR)
public class ConvertPointsRequestV4 {

	@NotEmpty
	@Schema(description = Constants.SWAGGER_SOURCE_CRS, type = "string",example = Constants.SWAGGER_SOURCE_CRS_EXAMPLE)
	@Parameter(required = true)
	private String fromCRS;

	@NotEmpty
	@Schema(description = Constants.SWAGGER_TARGET_CRS, type = "string",example = Constants.SWAGGER_CONVERT_TARGET_CRS_EXAMPLE)
	@Parameter(required = true)
	private String toCRS;

	@Valid
	@NotEmpty
	@Schema(description = Constants.SWAGGER_LIST_OF_POINTS_TO_BE_CONVERTED,example = Constants.SWAGGER_LIST_OF_POINTS_TO_BE_CONVERTED_EXAMPLE)
	@Parameter(required = true)
	private List<Point> points;

	@Schema(description = Constants.SWAGGER_TRANSFORM,
			example = Constants.SWAGGER_TRANSFORM_EXAMPLE)
	@Parameter(required = false)
	private String transformation;
}
