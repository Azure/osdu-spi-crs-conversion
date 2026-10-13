package org.opengroup.osdu.crs.model;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opengroup.osdu.crs.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@Schema(description = Constants.SWAGGER_TRJ_STN_IN_DESCRIPTION)
public class TrajectoryStationIn {
	public TrajectoryStationIn(){
		// set the optional values to NaN
		dx = Double.NaN;
		dy = Double.NaN;
		dz = Double.NaN;
	}

	@NotNull
	@Schema(description = Constants.SWAGGER_MD, type = "number", format = "double",example = Constants.SWAGGER_MD_EXAMPLE)
	@Parameter(required = true)
	private Double md;

	@NotNull
	@Schema(description = Constants.SWAGGER_INC, type = "number", format = "double",example = Constants.SWAGGER_INC_EXAMPLE)
	@Parameter(required = true)
	private Double inclination;

	@NotNull
	@Schema(description = Constants.SWAGGER_AZI, type = "number", format = "double",example = Constants.SWAGGER_AZI_EXAMPLE)
	@Parameter(required = true)
	private Double azimuth;

	@Schema(description = Constants.SWAGGER_DX, type = "number", format = "double",example = Constants.SWAGGER_DX_EXAMPLE)
	private Double dx;

	@Schema(description = Constants.SWAGGER_DY, type = "number", format = "double",example = Constants.SWAGGER_DY_EXAMPLE)
	private Double dy;

	@Schema(description = Constants.SWAGGER_DZ, type = "number", format = "double",example = Constants.SWAGGER_DZ_EXAMPLE)
	private Double dz;
}
