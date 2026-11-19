package org.opengroup.osdu.crs.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import tools.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opengroup.osdu.crs.util.Constants;
import org.opengroup.osdu.crs.util.StrictStringDeserializer;
import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.util.List;

@Data
@Schema(description = Constants.SWAGGER_CONVERT_REQUEST_DESCR)
public class ConvertPointsRequest {
	@NotEmpty
	@Schema(format = "osdu-crs-reference", description = Constants.SWAGGER_SOURCE_CRS, type = "string",example = Constants.SWAGGER_SOURCE_CRS_EXAMPLE)
	@Parameter(required = true)
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String fromCRS;

	@NotEmpty
	@Schema(format = "osdu-crs-reference", description = Constants.SWAGGER_TARGET_CRS, type = "string",example = Constants.SWAGGER_CONVERT_TARGET_CRS_EXAMPLE)
	@Parameter(required = true)
	@JsonSetter(nulls = Nulls.FAIL)
	@JsonDeserialize(using = StrictStringDeserializer.class)
	private String toCRS;

	@Valid
	@NotEmpty
	@Schema(description = Constants.SWAGGER_LIST_OF_POINTS_TO_BE_CONVERTED,example = Constants.SWAGGER_LIST_OF_POINTS_TO_BE_CONVERTED_EXAMPLE)
	@Parameter(required = true)
	private List<@NotNull Point> points;
}
