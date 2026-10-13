package org.opengroup.osdu.crs.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opengroup.osdu.crs.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotEmpty;

import jakarta.validation.Valid;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = Constants.SWAGGER_TRJ_RSP_DESCRIPTION)
public class ConvertTrajectoryResponse {
    @NotEmpty
    @Schema(description = Constants.SWAGGER_TRJ_REQ_CRS, type = "string",example = Constants.SWAGGER_TARGET_CRS_EXAMPLE)
    @Parameter(required = true)
    private String trajectoryCRS;

    @NotEmpty
    @Schema(description = Constants.SWAGGER_TRJ_RSP_UNIT_XY, type = "string",example = Constants.SWAGGER_TRJ_REQ_UNIT_EXAMPLE)
    private String unitXY;

    @NotEmpty
    @Schema(description = Constants.SWAGGER_TRJ_RSP_UNIT_Z, type = "string",example = Constants.SWAGGER_TRJ_REQ_UNIT_EXAMPLE)
    @Parameter(required = true)
    private String unitZ;

    @NotEmpty
    @Schema(description = Constants.SWAGGER_TRJ_RSP_UNIT_DLS, type = "string",example = Constants.SWAGGER_TRJ_RSP_UNIT_DLS_EXAMPLE)
    @Parameter(required = true)
    private String unitDls;

    @Valid
    @NotEmpty
    @Schema(description = Constants.SWAGGER_TRJ_RSP_LIST_OF_STATIONS)
    @Parameter(required = true)
    private List<TrajectoryStationOut> stations;

    @Valid
    @NotEmpty
    @Schema(description = Constants.SWAGGER_TRJ_RSP_LOCAL_CRS, type = "string",example = Constants.SWAGGER_TARGET_CRS_EXAMPLE)
    @Parameter(required = true)
    private String localCRS;

    @NotEmpty
    @Schema(description = Constants.SWAGGER_TRJ_RSP_METHOD, type = "string",example = Constants.SWAGGER_TRJ_REQ_METHOD_EXAMPLE)
    @Parameter(required = true)
    private String method;

    @JsonProperty("inputKind")
    @Schema(description = Constants.SWAGGER_TRJ_REQ_INPUT_KIND, type = "string",example = Constants.SWAGGER_TRJ_REQ_INPUT_KIND_EXAMPLE)

    private String inputKind;

    @Schema(description = Constants.SWAGGER_CONVERT_AUDIT)
    private List<String> operationsApplied;
}
