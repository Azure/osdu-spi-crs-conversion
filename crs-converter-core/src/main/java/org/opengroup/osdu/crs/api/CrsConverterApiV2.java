package org.opengroup.osdu.crs.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.crs.interfaces.ICRSConverter;
import org.opengroup.osdu.crs.interfaces.IPointConverter;
import org.opengroup.osdu.crs.interfaces.ITrajectoryConverter;
import org.opengroup.osdu.crs.model.*;
import org.opengroup.osdu.crs.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;


@CrossOrigin
@RestController
@RequestMapping("/v2")
public class CrsConverterApiV2 {

	@Autowired
	private JaxRsDpsLog logger;

	private final ICRSConverter crsConverter;
	private final ITrajectoryConverter crsTrajectoryConverter;
	private final IPointConverter pointConverter;

	public CrsConverterApiV2(@NonNull ICRSConverter crsConverter,
				  @NonNull ITrajectoryConverter crsTrajectoryConverter,
				  @NonNull IPointConverter pointConverter) {
		this.crsConverter = crsConverter;
		this.crsTrajectoryConverter = crsTrajectoryConverter;
		this.pointConverter = pointConverter;
	}

	@Operation(summary = "${CrsConverterApi.convertPoint.summary}", description = "${CrsConverterApi.convertPoint.description}",
			security = {@SecurityRequirement(name = "Authorization")}, tags = {"crs-converter-api-v2"},
			requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)))

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = Constants.SWAGGER_CONVERT_SUCCESS_RESPONSE, content = { @Content(schema = @Schema(implementation = ConvertPointsResponse.class)) }),
			@ApiResponse(responseCode = "400", description = Constants.SWAGGER_CONVERT_BAD_INPUT_BASE_PATH,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "500", description = Constants.SWAGGER_CONVERT_UNKNOWN_ERROR,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "503", description = Constants.SWAGGER_CONVERT_OVERLOAD,  content = {@Content(schema = @Schema(implementation = AppError.class ))})
	})
	@PostMapping(value = "/convert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ConvertPointsResponse convertPoint(@NonNull @Valid @RequestBody ConvertPointsRequest request) {
		double[] xyCoordinates = this.pointConverter.mergeXYCoordinates(request.getPoints());
		double[] zCoordinates = this.pointConverter.mergeZCoordinates(request.getPoints());
		ConvertPointsResponse response = this.crsConverter.convertPoint(request.getFromCRS(), request.getToCRS(), xyCoordinates,
				zCoordinates);
		response.setPoints(this.pointConverter.convertValuesToPoints(xyCoordinates, zCoordinates));
		return response;
	}

	@Operation(summary = "${CrsConverterApi.geo_json_convert.summary}", description = "${CrsConverterApi.geo_json_convert.description}",
			security = {@SecurityRequirement(name = "Authorization")}, tags = {"crs-converter-api-v2"})
	@PostMapping("/convertGeoJson")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = Constants.SWAGGER_CONVERT_SUCCESS_RESPONSE, content = { @Content(schema = @Schema(implementation = ConvertGeoJsonResponse.class)) }),
			@ApiResponse(responseCode = "400", description = Constants.SWAGGER_CONVERT_BAD_INPUT_BASE_PATH,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "500", description = Constants.SWAGGER_CONVERT_UNKNOWN_ERROR,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "503", description = Constants.SWAGGER_CONVERT_OVERLOAD,  content = {@Content(schema = @Schema(implementation = AppError.class ))})
	})
	public ConvertGeoJsonResponse convertGeoJson(@NonNull @Valid @RequestBody ConvertGeoJsonRequest request) {
		ConvertGeoJsonResponse response = this.crsConverter.convertGeoJson(
				request.getFeatureCollection(),
				request.getToCRS(),
				request.getToUnitZ());
		return response;
	}

	@Operation(summary = "${CrsConverterApi.convertTrajectory.summary}", description = "${CrsConverterApi.convertTrajectory.description}",
			security = {@SecurityRequirement(name = "Authorization")}, tags = {"convert-trajectory-api-v2"})
	@PostMapping("/convertTrajectory")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = Constants.SWAGGER_TRJ_CONVERT_SUCCESS_RESPONSE, content = { @Content(schema = @Schema(implementation = ConvertTrajectoryResponse.class)) }),
			@ApiResponse(responseCode = "400", description = Constants.SWAGGER_CONVERT_BAD_INPUT_BASE_PATH,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "500", description = Constants.SWAGGER_CONVERT_UNKNOWN_ERROR,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "503", description = Constants.SWAGGER_CONVERT_OVERLOAD,  content = {@Content(schema = @Schema(implementation = AppError.class ))})
	})
	@Parameter(hidden = true)
	public ConvertTrajectoryResponse convertTrajectory(@RequestHeader MultiValueMap<String, String> headers,
													   @NonNull @Valid @RequestBody ConvertTrajectoryRequest request) {
		String message = String.format("Using trajectory: %s", "no");
		logger.info(message);
		DpsHeaders dpsHeaders = DpsHeaders.createFromEntrySet(headers.entrySet());
		ConvertTrajectoryResponse response = this.crsTrajectoryConverter.convertTrajectory(dpsHeaders, request);
		return response;
	}
}
