package org.opengroup.osdu.crs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.opengroup.osdu.crs.swagger.PointResponse;
import org.opengroup.osdu.crs.swagger.NonFiniteNumber;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.opengroup.osdu.crs.util.Constants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = Constants.SWAGGER_TRJ_STN_OUT_DESCRIPTION)

public class TrajectoryStationOut {
    @Schema(description = Constants.SWAGGER_MD, types = {"number", "string"}, oneOf = {Double.class, NonFiniteNumber.class},example = Constants.SWAGGER_MD_EXAMPLE)
    @Parameter(required = true)
    private Double md;

    @Schema(description = Constants.SWAGGER_INC, types = {"number", "string"}, oneOf = {Double.class, NonFiniteNumber.class},example = Constants.SWAGGER_INC_EXAMPLE)
    @Parameter(required = true)
    private Double inclination;

    @Schema(description = Constants.SWAGGER_AZI_TN, types = {"number", "string"}, oneOf = {Double.class, NonFiniteNumber.class},example = Constants.SWAGGER_AZI_TN_EXAMPLE)
    @Parameter(required = true)
    private Double azimuthTN;

    @Schema(description = Constants.SWAGGER_AZI_GN, types = {"number", "string"}, oneOf = {Double.class, NonFiniteNumber.class},example = Constants.SWAGGER_AZI_GN_EXAMPLE)
    @Parameter(required = true)
    private Double azimuthGN;

    @Schema(description = Constants.SWAGGER_DX_TN, types = {"number", "string"}, oneOf = {Double.class, NonFiniteNumber.class},example = Constants.SWAGGER_DX_TN_EXAMPLE)
    @Parameter(required = true)
    private Double dxTN;

    @Schema(description = Constants.SWAGGER_DY_TN, types = {"number", "string"}, oneOf = {Double.class, NonFiniteNumber.class},example = Constants.SWAGGER_DY_TN_EXAMPLE)
    @Parameter(required = true)
    private Double dyTN;

    @Schema(description = Constants.SWAGGER_DZ, types = {"number", "string"}, oneOf = {Double.class, NonFiniteNumber.class},example = Constants.SWAGGER_DZ_EXAMPLE)
    @Parameter(required = true)
    // Jackson 2 legacy bean naming exposed getDZ() as "dz"; keep it for the API contract
    @Getter(onMethod_ = @JsonProperty("dz"))
    @Setter(onMethod_ = @JsonProperty("dz"))
    private Double dZ;

    @Schema(description = Constants.SWAGGER_TRJ_POINT, implementation = PointResponse.class)
    @Parameter(required = true)
    private Point point;

    @Schema(description = Constants.SWAGGER_TRJ_WGS84_LONGITUDE, types = {"number", "string"}, oneOf = {Double.class, NonFiniteNumber.class})
    double wgs84Longitude;

    @Schema(description = Constants.SWAGGER_TRJ_WGS84_LATITUDE, types = {"number", "string"}, oneOf = {Double.class, NonFiniteNumber.class})
    double wgs84Latitude;

    @Schema(description = Constants.SWAGGER_TRJ_RSP_DLS, types = {"number", "string"}, oneOf = {Double.class, NonFiniteNumber.class})
    double dls;

    @Schema(description = Constants.SWAGGER_TRJ_RSP_ORIGINAL, type = "boolean", types = {"boolean"})
    private boolean original;
}
