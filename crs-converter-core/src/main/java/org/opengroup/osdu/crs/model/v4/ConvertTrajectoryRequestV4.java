package org.opengroup.osdu.crs.model.v4;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.opengroup.osdu.crs.model.Point;
import org.opengroup.osdu.crs.model.TrajectoryInputKind;
import org.opengroup.osdu.crs.util.Constants;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = Constants.SWAGGER_TRJ_REQ_DESCRIPTION)
public class ConvertTrajectoryRequestV4 {

    public ConvertTrajectoryRequestV4(){
        this.interpolate = true;
        this.inputKind = TrajectoryInputKind.MD_INCL_AZIM.toString();
    }

    @JsonProperty("trajectoryCRS")
    @NotEmpty
    @Schema(description = Constants.SWAGGER_TRJ_REQ_CRS, type = "string",example = Constants.SWAGGER_TARGET_CRS_EX)
    @Parameter(required = true)
    private String trajectoryCRS;

    @JsonProperty("azimuthReference")
    @Schema(description = Constants.SWAGGER_TRJ_REQ_AZIMUTH_REF, type = "string",example = Constants.SWAGGER_TRJ_REQ_AZIMUTH_REF_EXAMPLE)
    private String azimuthReference;

    @JsonProperty("unitXY")
    @Schema(description = Constants.SWAGGER_TRJ_REQ_UNIT_XY, type = "string",example = Constants.SWAGGER_TRJ_REQ_UNIT_XY_EXAMPLE)
    private String unitXY;

    @JsonProperty("unitZ")
    @NotEmpty
    @Schema(description = Constants.SWAGGER_TRJ_REQ_UNIT_Z, type = "string",example = Constants.SWAGGER_TRJ_REQ_UNIT_Z_EXAMPLE)
    @Parameter(required = true)
    private String unitZ;

    @JsonProperty("referencePoint")
    @Schema(description = Constants.SWAGGER_TRF_REQ_REF_POINT)
    @Parameter(required = true)
    private Point referencePoint;

    @JsonProperty("inputStations")
    @Valid
    @NotEmpty
    @Schema(description = Constants.SWAGGER_TRJ_REQ_LIST_OF_INPUT_STATIONS)
    @Parameter(required = true)
    private List<TrajectoryStationInV4> inputStations;

    @JsonProperty("method")
    @NotEmpty
    @Schema(description = Constants.SWAGGER_TRJ_REQ_METHOD, type = "string",example = Constants.SWAGGER_TRJ_REQ_METHOD_EXAMPLE)
    @Parameter(required = true)
    private String method;

    @JsonProperty("inputKind")
    @Schema(description = Constants.SWAGGER_TRJ_REQ_INPUT_KIND, type = "string",example = Constants.SWAGGER_TRJ_REQ_INPUT_KIND_EXAMPLE)
    public String inputKind;

    @JsonProperty("interpolate")
    @Schema(description = Constants.SWAGGER_TRJ_REQ_INTERPOLATE, type = "boolean",example = Constants.SWAGGER_TRJ_REQ_INTERPOLATE_EX)
    public boolean interpolate;

	@JsonProperty("MD_i")
	@Schema(description = Constants.SWAGGER_TRJ_MD_I)
	private MinimumDepthInterval MD_i;

    @JsonProperty("unitMD")
    @Schema(description = Constants.SWAGGER_TRJ_UNIT_MD, type = "string", example = Constants.SWAGGER_TRJ_REQ_UNIT_MD_EXAMPLE)
    private String unitMD;

    @Override
    public String toString() {
        String result = null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            result = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return result;
        }
        return result;
    }

    public static ConvertTrajectoryRequestV4 createInstance(String json) {
        ConvertTrajectoryRequestV4 result = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            JsonNode node = mapper.readTree(json);
            result = mapper.treeToValue(node, ConvertTrajectoryRequestV4.class);

        } catch (IOException e) {
        	return result;
        }
        return result;
    }
}
