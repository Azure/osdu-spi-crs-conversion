package org.opengroup.osdu.crs.model.v4;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import tools.jackson.databind.annotation.JsonDeserialize;
import org.opengroup.osdu.crs.util.StrictBooleanDeserializer;
import org.opengroup.osdu.crs.util.StrictStringDeserializer;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.opengroup.osdu.crs.model.Point;
import org.opengroup.osdu.crs.model.TrajectoryInputKind;
import org.opengroup.osdu.crs.util.Constants;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = Constants.SWAGGER_TRJ_REQ_DESCRIPTION)
public class ConvertTrajectoryRequestV4 {

    // Jackson 3 would otherwise bind via the all-args constructor and skip these defaults
    @JsonCreator
    public ConvertTrajectoryRequestV4(){
        this.interpolate = true;
        this.inputKind = TrajectoryInputKind.MD_INCL_AZIM.toString();
    }

    @JsonProperty("trajectoryCRS")
    @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = StrictStringDeserializer.class)
    @NotEmpty
    @Schema(format = "osdu-crs-reference", description = Constants.SWAGGER_TRJ_REQ_CRS, type = "string",example = Constants.SWAGGER_TARGET_CRS_EX)
    @Parameter(required = true)
    private String trajectoryCRS;

    @JsonProperty("azimuthReference")
    @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = StrictStringDeserializer.class)
    @Schema(allowableValues = {"TRUE_NORTH", "TN", "GRID_NORTH", "GN"}, description = Constants.SWAGGER_TRJ_REQ_AZIMUTH_REF, type = "string",example = Constants.SWAGGER_TRJ_REQ_AZIMUTH_REF_EXAMPLE)
    private String azimuthReference;

    @JsonProperty("unitXY")
    @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = StrictStringDeserializer.class)
    @Schema(format = "osdu-unit-reference",
            description = "Unit of measure for horizontal displacements (dx, dy). Derived from a projected CRS record id when omitted. Required when trajectoryCRS is a persistable reference JSON string.",
            type = "string", example = Constants.SWAGGER_TRJ_REQ_UNIT_XY_EXAMPLE)
    private String unitXY;

    @JsonProperty("unitZ")
    @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = StrictStringDeserializer.class)
    @NotEmpty
    @Schema(format = "osdu-unit-reference", description = Constants.SWAGGER_TRJ_REQ_UNIT_Z, type = "string",example = Constants.SWAGGER_TRJ_REQ_UNIT_Z_EXAMPLE)
    @Parameter(required = true)
    private String unitZ;

    @JsonProperty("referencePoint")
    @Valid
    @NotNull
    @Schema(description = Constants.SWAGGER_TRF_REQ_REF_POINT)
    @Parameter(required = true)
    private Point referencePoint;

    @JsonProperty("inputStations")
    @Valid
    @NotEmpty
    @Schema(description = Constants.SWAGGER_TRJ_REQ_LIST_OF_INPUT_STATIONS)
    @Parameter(required = true)
    private List<@NotNull TrajectoryStationInV4> inputStations;

    @JsonProperty("method")
    @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = StrictStringDeserializer.class)
    @NotEmpty
    @Schema(allowableValues = {"AzimuthalEquidistant", "LMP", "LeesModifiedProposal", "GNL"}, description = Constants.SWAGGER_TRJ_REQ_METHOD, type = "string",example = Constants.SWAGGER_TRJ_REQ_METHOD_EXAMPLE)
    @Parameter(required = true)
    private String method;

    @JsonProperty("inputKind")
    @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = StrictStringDeserializer.class)
    @Schema(allowableValues = {"MD_Incl_Azim", "MD_Inclination_Azimuth", "Md_Incl_Azim", "dX_dY_dZ", "MD_Incl"},
            description = Constants.SWAGGER_TRJ_REQ_INPUT_KIND, type = "string", example = Constants.SWAGGER_TRJ_REQ_INPUT_KIND_EXAMPLE)
    public String inputKind;

    @JsonProperty("interpolate")
    @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = StrictBooleanDeserializer.class)
    @Schema(description = Constants.SWAGGER_TRJ_REQ_INTERPOLATE, type = "boolean", types = {"boolean"},example = Constants.SWAGGER_TRJ_REQ_INTERPOLATE_EX)
    public boolean interpolate;

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@JsonProperty("MD_i")
	@JsonAlias("mD_i")
	@JsonSetter(nulls = Nulls.FAIL)
	@Schema(description = Constants.SWAGGER_TRJ_MD_I)
	private MinimumDepthInterval MD_i;

	@JsonProperty("MD_i")
	@JsonAlias("mD_i")
	public MinimumDepthInterval getMD_i() {
		return MD_i;
	}

	@JsonProperty("MD_i")
	@JsonAlias("mD_i")
	@JsonSetter(nulls = Nulls.FAIL)
	public void setMD_i(MinimumDepthInterval value) {
		this.MD_i = value;
	}

	@JsonProperty("md_i")
	@JsonSetter(nulls = Nulls.FAIL)
	public void setMd_i(MinimumDepthInterval value) {
		this.MD_i = value;
	}

	@JsonProperty("md_i")
	public MinimumDepthInterval getMd_i() {
		return MD_i;
	}

    @JsonProperty("unitMD")
    @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = StrictStringDeserializer.class)
    @Schema(format = "osdu-unit-reference", description = Constants.SWAGGER_TRJ_UNIT_MD, type = "string", example = Constants.SWAGGER_TRJ_REQ_UNIT_MD_EXAMPLE)
    private String unitMD;

    @Override
    public String toString() {
        String result = null;
        JsonMapper mapper = JsonMapper.builderWithJackson2Defaults()
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .changeDefaultPropertyInclusion(incl -> JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL))
                .build();
        try {
            result = mapper.writeValueAsString(this);
        } catch (JacksonException e) {
            return result;
        }
        return result;
    }

    public static ConvertTrajectoryRequestV4 createInstance(String json) {
        ConvertTrajectoryRequestV4 result = null;
        try {
            JsonMapper mapper = JsonMapper.builderWithJackson2Defaults()
                    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                    .build();
            JsonNode node = mapper.readTree(json);
            result = mapper.treeToValue(node, ConvertTrajectoryRequestV4.class);

        } catch (JacksonException e) {
        	return result;
        }
        return result;
    }
}
