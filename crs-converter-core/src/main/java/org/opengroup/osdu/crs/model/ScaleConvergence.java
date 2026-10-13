package org.opengroup.osdu.crs.model;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.crs.util.Constants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = Constants.SWAGGER_TRJ_SCALE_CONVERGENCE)
public class ScaleConvergence {

    @Schema(description = Constants.SWAGGER_TRJ_SCALE_FACTOR, type = "number", format = "double",example = Constants.SWAGGER_TRJ_SCALE_FACTOR)
    @Parameter(required = true)
    private double scalefactor;

    @Schema(description = Constants.SWAGGER_TRJ_CONVERGENCE, type = "number", format = "double",example = Constants.SWAGGER_TRJ_CONVERGENCE)
    @Parameter(required = true)
    private double convergence;

    @Schema(description = Constants.SWAGGER_TRJ_POINT, type = "number", format = "double",example = Constants.SWAGGER_TRJ_POINT)
    @Parameter(required = true)
    private Point point;

}
