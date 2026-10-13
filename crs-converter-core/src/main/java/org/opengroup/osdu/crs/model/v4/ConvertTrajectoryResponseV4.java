package org.opengroup.osdu.crs.model.v4;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.crs.model.ConvertTrajectoryResponse;
import org.opengroup.osdu.crs.model.ScaleConvergence;
import org.opengroup.osdu.crs.model.TrajectoryStationOut;
import org.opengroup.osdu.crs.util.Constants;

import jakarta.validation.Valid;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = Constants.SWAGGER_TRJ_RSP_DESCRIPTION)
@JsonPropertyOrder({"unitMD"})
public class ConvertTrajectoryResponseV4 extends ConvertTrajectoryResponse {

    @Valid
    @Schema(description = Constants.SWAGGER_TRJ_RSP_LIST_OF_STATIONS_I)
    @Parameter(required = true)
    private List<TrajectoryStationOut> stations_i;

    @Valid
    @Schema(description = Constants.SWAGGER_TRJ_SCALE_CONVERGENCE)
    @Parameter(required = true)
    private List<ScaleConvergence> scaleConvergenceList;

    @Valid
    @Schema(description = Constants.SWAGGER_TRJ_UNIT_MD)
    private String unitMD;
}
