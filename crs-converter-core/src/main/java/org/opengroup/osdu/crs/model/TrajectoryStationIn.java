package org.opengroup.osdu.crs.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opengroup.osdu.crs.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@Schema(description = Constants.SWAGGER_TRJ_STN_IN_DESCRIPTION)
public class TrajectoryStationIn {
	// Jackson 3 would otherwise bind via the all-args constructor and skip these defaults
	@JsonCreator
	public TrajectoryStationIn(){
		// set the optional values to NaN
		dx = Double.NaN;
		dy = Double.NaN;
		dz = Double.NaN;
	}

	@NotNull
	@Schema(description = Constants.SWAGGER_MD, type = "number", types = {"number"}, format = "double",example = Constants.SWAGGER_MD_EXAMPLE)
	@Parameter(required = true)
	private Double md;

	@NotNull
	@Schema(description = Constants.SWAGGER_INC, type = "number", types = {"number"}, format = "double",example = Constants.SWAGGER_INC_EXAMPLE)
	@Parameter(required = true)
	private Double inclination;

	@NotNull
	@Schema(description = Constants.SWAGGER_AZI, type = "number", types = {"number"}, format = "double",example = Constants.SWAGGER_AZI_EXAMPLE)
	@Parameter(required = true)
	private Double azimuth;

	@Schema(description = Constants.SWAGGER_DX, type = "number", types = {"number"}, format = "double",example = Constants.SWAGGER_DX_EXAMPLE)
	@JsonSetter(nulls = Nulls.FAIL)
	private Double dx;

	@Schema(description = Constants.SWAGGER_DY, type = "number", types = {"number"}, format = "double",example = Constants.SWAGGER_DY_EXAMPLE)
	@JsonSetter(nulls = Nulls.FAIL)
	private Double dy;

	@Schema(description = Constants.SWAGGER_DZ, type = "number", types = {"number"}, format = "double",example = Constants.SWAGGER_DZ_EXAMPLE)
	@JsonSetter(nulls = Nulls.FAIL)
	private Double dz;
}
