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
@Schema(description = Constants.SWAGGER_TRJ_STN_OUT_DESCRIPTION)

public class TrajectoryStationOut {
    @Schema(description = Constants.SWAGGER_MD, type = "number", format = "double",example = Constants.SWAGGER_MD_EXAMPLE)
    @Parameter(required = true)
    private Double md;

    @Schema(description = Constants.SWAGGER_INC, type = "number", format = "double",example = Constants.SWAGGER_INC_EXAMPLE)
    @Parameter(required = true)
    private Double inclination;

    @Schema(description = Constants.SWAGGER_AZI_TN, type = "number", format = "double",example = Constants.SWAGGER_AZI_TN_EXAMPLE)
    @Parameter(required = true)
    private Double azimuthTN;

    @Schema(description = Constants.SWAGGER_AZI_GN, type = "number", format = "double",example = Constants.SWAGGER_AZI_GN_EXAMPLE)
    @Parameter(required = true)
    private Double azimuthGN;

    @Schema(description = Constants.SWAGGER_DX_TN, type = "number", format = "double",example = Constants.SWAGGER_DX_TN_EXAMPLE)
    @Parameter(required = true)
    private Double dxTN;

    @Schema(description = Constants.SWAGGER_DY_TN, type = "number", format = "double",example = Constants.SWAGGER_DY_TN_EXAMPLE)
    @Parameter(required = true)
    private Double dyTN;

    @Schema(description = Constants.SWAGGER_DZ, type = "number", format = "double",example = Constants.SWAGGER_DZ_EXAMPLE)
    @Parameter(required = true)
    private Double dZ;

    @Schema(description = Constants.SWAGGER_TRJ_POINT, type = "number", format = "double")
    @Parameter(required = true)
    private Point point;

    @Schema(description = Constants.SWAGGER_TRJ_WGS84_LONGITUDE, type = "number", format = "double")
    double wgs84Longitude;

    @Schema(description = Constants.SWAGGER_TRJ_WGS84_LATITUDE, type = "number", format = "double")
    double wgs84Latitude;

    @Schema(description = Constants.SWAGGER_TRJ_RSP_DLS, type = "number", format = "double")
    double dls;

    @Schema(description = Constants.SWAGGER_TRJ_RSP_ORIGINAL, type = "number", format = "double")
    private boolean original;
}
