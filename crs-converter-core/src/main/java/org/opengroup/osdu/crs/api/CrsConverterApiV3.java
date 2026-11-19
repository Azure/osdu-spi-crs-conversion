package org.opengroup.osdu.crs.api;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.crs.BinGrid.AbstractBinGrid;
import org.opengroup.osdu.crs.BinGrid.AbstractFeature;
import org.opengroup.osdu.crs.BinGrid.AbstractFeatureCollection;
import org.opengroup.osdu.crs.BinGrid.AbstractSpatialLocation;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeature;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.interfaces.ICRSConverter;
import org.opengroup.osdu.crs.interfaces.IPointConverter;
import org.opengroup.osdu.crs.interfaces.ITrajectoryConverter;
import org.opengroup.osdu.crs.model.*;
import org.opengroup.osdu.crs.osducoreserviceclient.storage.IStorageClient;
import org.opengroup.osdu.crs.util.Constants;
import org.opengroup.osdu.crs.util.ProjectedAxisUnit;
import org.opengroup.osdu.crs.util.RecordCache;
import org.opengroup.osdu.crs.util.RecordIdNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/v3")
public class CrsConverterApiV3 {

	@Autowired
	private JaxRsDpsLog logger;
    
	@Autowired
    private IStorageClient StorageClient;

	private final ICRSConverter crsConverter;
	private final ITrajectoryConverter crsTrajectoryConverter;
	private final IPointConverter pointConverter;
    private RecordCache recordCache;
    private static final String FEATURE_TYPE = "Feature";
	private static final String GEOMETRY_TYPE = "Point";
	private static final String BIN_GRID_METHOD_4_CORNER = ":reference-data--BinGridDefinitionMethodType:4Corner:";

	private static final String BOUND_PROJECTED = "BoundProjected";
	private static final String PROJECTED = "Projected";
 
	public CrsConverterApiV3(@NonNull ICRSConverter crsConverter,
				  @NonNull ITrajectoryConverter crsTrajectoryConverter,
				  @NonNull IPointConverter pointConverter) {
		this.crsConverter = crsConverter;
		this.crsTrajectoryConverter = crsTrajectoryConverter;
		this.pointConverter = pointConverter;
		this.recordCache = new RecordCache();
	}
	// parameter str could be a persistableReference string or recordID. mustID means parameter str must be a recordID
	private String getPersistableReferenceFromID(String str, boolean mustID){
		if (str == null || str.isBlank()) {
			return null;
		}
		String temp;
		try {
			temp = URLDecoder.decode(str, "UTF-8"); 
		} catch (Exception e) {
			return str; // try our best to return user input
		}
		// persistableReference string starts with {....}
	    if (temp.startsWith("{") && mustID == false){
			return str;
		}
		if (!str.contains(":")) {
			throw new AppException(HttpStatus.NOT_FOUND.value(), "record not found",
					"Please check the input type and format and try again.");
		}
	
		// set temp as str as we don't want to decode. for example UnitOfMeasure:ft%5BUS%5D in record id
		// normalize before the cache lookup so get/put and the storage call all use the same key
		temp = RecordIdNormalizer.normalizeRecordID(str);
        String pr = recordCache.get(temp);
        if (pr != null){
			return pr; 
		}

		Record record =  StorageClient.getRecord(temp);
		if (record == null)
			throw new ValidationException(String.join(" ", "record not found:", temp));
		Object persistableReference = record.getData().get("PersistableReference");
		if (persistableReference == null) {
			throw new ValidationException(String.join(" ", "record does not have PersistableReference:", temp));
		}
		pr = persistableReference.toString();
		recordCache.put(temp, pr);
		return pr;
    }

	private String getUnitFromTrajectoryCRS(String trajectoryCRS)  {

		String temp;
		try {
			temp = URLDecoder.decode(trajectoryCRS, "UTF-8");
		} catch (Exception e) {
			return trajectoryCRS; // try our best to return user input
		}

		temp = RecordIdNormalizer.normalizeRecordID(temp);

		Record record =  StorageClient.getRecord(temp);
		if (record == null)
			throw new ValidationException(String.join(" ", "record not found:", temp));
		Map<String, Object> data = record.getData();
		if (data == null || (nestedCoordinateSystem(data) == null
				&& !(data.get("CoordinateSystem.HorizontalAxisUnitID") instanceof String))) {
			throw new ValidationException(String.join(" ", "record does not have CoordinateSystem:", temp));
		}
		String horizontalAxisUnitID = horizontalAxisUnitId(data);
		if (horizontalAxisUnitID == null) {
			throw new ValidationException(String.join(" ", "record does not have CoordinateSystem.HorizontalAxisUnitID:", temp));
		}
		return horizontalAxisUnitID;
	}

	private String projectedUnitReference(String trajectoryCrs) {
		String unitId = getUnitFromTrajectoryCRS(trajectoryCrs);
		try {
			String resolved = getPersistableReferenceFromID(unitId, false);
			if (resolved != null) {
				return resolved;
			}
		} catch (ValidationException ex) {
			if (ex.getMessage() == null || !ex.getMessage().contains("does not have PersistableReference")) {
				throw ex;
			}
		}
		String derived = ProjectedAxisUnit.fromPersistableReference(getPersistableReferenceFromID(trajectoryCrs, false));
		if (derived == null) {
			throw new ValidationException(String.join(" ", "record does not have PersistableReference:",
					RecordIdNormalizer.normalizeRecordID(unitId)));
		}
		return derived;
	}

	/**
	 * Storage records expose the unit either as a nested {@code CoordinateSystem} object
	 * or as the flat property {@code CoordinateSystem.HorizontalAxisUnitID}.
	 */
	static String horizontalAxisUnitId(Map<String, Object> data) {
		if (data == null) {
			return null;
		}
		Map<?, ?> coordinateSystem = nestedCoordinateSystem(data);
		if (coordinateSystem != null) {
			Object nestedUnit = coordinateSystem.get("HorizontalAxisUnitID");
			if (nestedUnit instanceof String unit && !unit.isBlank()) {
				return unit;
			}
		}
		Object flatUnit = data.get("CoordinateSystem.HorizontalAxisUnitID");
		if (flatUnit instanceof String unit && !unit.isBlank()) {
			return unit;
		}
		return null;
	}

	private static Map<?, ?> nestedCoordinateSystem(Map<String, Object> data) {
		Object coordinateSystem = data.get("CoordinateSystem");
		return coordinateSystem instanceof Map<?, ?> nested ? nested : null;
	}

	@PostMapping(value = "/convert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "${CrsConverterApi.convertPoint.summary}", description = "${CrsConverterApi.convertPoint.description}",
			security = {@SecurityRequirement(name = "Authorization")}, tags = {"crs-converter-api-v3"},
			requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = Constants.SWAGGER_CONVERT_SUCCESS_RESPONSE, content = { @Content(schema = @Schema(implementation = ConvertPointsResponse.class)) }),
			@ApiResponse(responseCode = "400", description = Constants.SWAGGER_CONVERT_BAD_INPUT_BASE_PATH,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "401", description = "Unauthorized: missing or invalid Authorization header", content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "403", description = "Forbidden: insufficient permissions or invalid partition", content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "404", description = "Not found: referenced record does not exist", content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "415", description = "Unsupported media type; Content-Type must be application/json", content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "500", description = Constants.SWAGGER_CONVERT_UNKNOWN_ERROR,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "503", description = Constants.SWAGGER_CONVERT_OVERLOAD,  content = {@Content(schema = @Schema(implementation = AppError.class ))})
	})
	public ConvertPointsResponse convertPoint(@NonNull @Valid @RequestBody ConvertPointsRequest request) {
		String fromCrs = getPersistableReferenceFromID(request.getFromCRS(), false);
		String toCrs = getPersistableReferenceFromID(request.getToCRS(), false);
		double[] xyCoordinates = this.pointConverter.mergeXYCoordinates(request.getPoints());
		double[] zCoordinates = this.pointConverter.mergeZCoordinates(request.getPoints());
		ConvertPointsResponse response = this.crsConverter.convertPoint(fromCrs, toCrs, xyCoordinates,
				zCoordinates);
		response.setPoints(this.pointConverter.convertValuesToPoints(xyCoordinates, zCoordinates));
		return response;
	}

	@PostMapping("/convertGeoJson")
	@Operation(summary = "${CrsConverterApi.geo_json_convert.summary}", description = "${CrsConverterApi.geo_json_convert.description}",
			security = {@SecurityRequirement(name = "Authorization")}, tags = {"crs-converter-api-v3"})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = Constants.SWAGGER_CONVERT_SUCCESS_RESPONSE, content = { @Content(schema = @Schema(implementation = ConvertGeoJsonResponse.class)) }),
			@ApiResponse(responseCode = "400", description = Constants.SWAGGER_CONVERT_BAD_INPUT_BASE_PATH,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "401", description = "Unauthorized: missing or invalid Authorization header", content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "403", description = "Forbidden: insufficient permissions or invalid partition", content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "404", description = "Not found: referenced record does not exist", content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "415", description = "Unsupported media type; Content-Type must be application/json", content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "500", description = Constants.SWAGGER_CONVERT_UNKNOWN_ERROR,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "503", description = Constants.SWAGGER_CONVERT_OVERLOAD,  content = {@Content(schema = @Schema(implementation = AppError.class ))})
	})
	public ConvertGeoJsonResponse convertGeoJson(@NonNull @Valid @RequestBody ConvertGeoJsonRequest request) {
		GeoJsonFeatureCollection features = request.getFeatureCollection();
		String toCrs = getPersistableReferenceFromID(request.getToCRS(), false);
		String toUnitZ = getPersistableReferenceFromID(request.getToUnitZ(), false);

		// The CRS reference as persistableReference string. If populated, the CoordinateReferenceSystemID takes precedence
		if( features.getCoordinateReferenceSystemID() != null){
			String temp = getPersistableReferenceFromID(features.getCoordinateReferenceSystemID(), true);
			if(temp != null)
				features.setPersistableReferenceCrs(temp);
			features.setCoordinateReferenceSystemID(null);
		}
		// The VerticalUnitID definition overrides any self-contained definition in persistableReferenceUnitZ.
		if( features.getVerticalUnitID() != null){
			String temp = getPersistableReferenceFromID(features.getVerticalUnitID(), true);
			if(temp != null)
				features.setPersistableReferenceUnitZ(temp);
			features.setVerticalUnitID(null);
		}

		ConvertGeoJsonResponse response = this.crsConverter.convertGeoJson(
				features,
				toCrs,
				toUnitZ);
		return response;
	}

	@PostMapping("/convertTrajectory")
	@Operation(summary = "${CrsConverterApi.convertTrajectory.summary}", description = "${CrsConverterApi.convertTrajectory.description}",
			security = {@SecurityRequirement(name = "Authorization")}, tags = {"convert-trajectory-api-v3"})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = Constants.SWAGGER_TRJ_CONVERT_SUCCESS_RESPONSE, content = { @Content(schema = @Schema(implementation = ConvertTrajectoryResponse.class)) }),
			@ApiResponse(responseCode = "400", description = Constants.SWAGGER_CONVERT_BAD_INPUT_BASE_PATH,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "401", description = "Unauthorized: missing or invalid Authorization header", content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "403", description = "Forbidden: insufficient permissions or invalid partition", content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "404", description = "Not found: referenced record does not exist", content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "415", description = "Unsupported media type; Content-Type must be application/json", content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "500", description = Constants.SWAGGER_CONVERT_UNKNOWN_ERROR,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "503", description = Constants.SWAGGER_CONVERT_OVERLOAD,  content = {@Content(schema = @Schema(implementation = AppError.class ))})
	})
	@Parameter(hidden = true)
	public ConvertTrajectoryResponse convertTrajectory(@RequestHeader MultiValueMap<String, String> headers,
													   @NonNull @Valid @RequestBody ConvertTrajectoryRequest request) {
		String message = String.format("Using trajectory: %s", "no");
		logger.info(message);
		DpsHeaders dpsHeaders = DpsHeaders.createFromEntrySet(headers.entrySet());
		if(request.getTrajectoryCRS().contains(BOUND_PROJECTED) || request.getTrajectoryCRS().contains(PROJECTED)){
			if(!Strings.isNullOrEmpty(request.getUnitXY())) {
				throw new ValidationException("unitXY should not be provided for BoundProjected and Projected CRS.");
			}
			request.setUnitXY(projectedUnitReference(request.getTrajectoryCRS()));
		}else
			request.setUnitXY(getPersistableReferenceFromID(request.getUnitXY(), false));
		request.setTrajectoryCRS(getPersistableReferenceFromID(request.getTrajectoryCRS(), false));
		request.setUnitZ(getPersistableReferenceFromID(request.getUnitZ(), false));
		ConvertTrajectoryResponse response = this.crsTrajectoryConverter.convertTrajectory(dpsHeaders, request);
		return response;
	}
	@PostMapping("/convertBinGrid")
	@Operation(summary = "${CrsConverterApi.BinGrid.summary}", description = "${CrsConverterApi.BinGrid.description}",
			security = {@SecurityRequirement(name = "Authorization")}, tags = {"crs-converter-api-v3"})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = Constants.SWAGGER_BIN_GRID_CONVERSION_RESPONSE, content = { @Content(schema = @Schema(implementation = ConvertBinGridResponse.class)) }),
			@ApiResponse(responseCode = "400", description = Constants.SWAGGER_CONVERT_BAD_INPUT_BASE_PATH,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "401", description = "Unauthorized: missing or invalid Authorization header", content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "403", description = "Forbidden: insufficient permissions or invalid partition", content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "404", description = "Not found: referenced record does not exist", content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "415", description = "Unsupported media type; Content-Type must be application/json", content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "500", description = Constants.SWAGGER_CONVERT_UNKNOWN_ERROR,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "503", description = Constants.SWAGGER_CONVERT_OVERLOAD,  content = {@Content(schema = @Schema(implementation = AppError.class ))})
	})
	@Parameter(hidden = true)
	public ConvertBinGridResponse convertBinGrid(@NonNull @Valid @RequestBody ConvertBinGridRequest request,
			HttpServletRequest httpServletRequest) {

		logger.info("Starting Bin Grid Convert API.");
		ConvertBinGridResponse convertBinGridResponse = new ConvertBinGridResponse();
		try {
			AbstractBinGrid inBinGrid = request.getInBinGrid();
			if (inBinGrid == null || inBinGrid.getABCDBinGridSpatialLocation() == null
					|| inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates() == null
					|| inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures() == null) {
				throw new ValidationException("Invalid bin grid request.");
			}
			for (AbstractFeature feature : inBinGrid.getABCDBinGridSpatialLocation()
					.getAsIngestedcoordinates().getFeatures()) {
				if (feature == null || feature.getGeometry() == null || feature.getGeometry().getCoordinates() == null
						|| feature.getGeometry().getCoordinates().contains(null)
						|| StringUtils.isEmpty(feature.getGeometry().getType())
						|| feature.getProperties() == null || feature.getProperties().getPointPropertiesList() == null
						|| feature.getProperties().getPointPropertiesList().contains(null)) {
					throw new ValidationException("Invalid bin grid request.");
				}
			}
			int size = inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().size();
			if (size != 4) {
				logger.info("Invalid size for spatial coordinates in the input request.");
				throw new ValidationException(
						"Invalid size for spatial coordinates in the input request. Expected 4 AnyCrsFeatures with geometry �AnyCrsPoint�.  Found "
								+ size + " points");
			}
			
			List<String> operationsApplied = new ArrayList<>();
			convertBinGridResponse.setOutBinGrid(inBinGrid);
			if (!StringUtils.isEmpty(request.getToCRS())) {

				GeoJsonFeatureCollection geoJsonFeatureCollection = this.crsConverter.prepareGeoJsonRequest(
						request.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures(),
						inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
								.getCoordinateReferenceSystemID());
				ConvertGeoJsonRequest convertGeoJsonRequest = new ConvertGeoJsonRequest();
				convertGeoJsonRequest.setFeatureCollection(geoJsonFeatureCollection);
				convertGeoJsonRequest.setToCRS(request.getToCRS());
				logger.info("Sending the GeoJsonRequest. With CRS value : " + convertGeoJsonRequest.getToCRS());
				ConvertGeoJsonResponse convertGeoJsonResponse = convertGeoJson(convertGeoJsonRequest);
				GeoJsonFeature[] geoJsonFeature = convertGeoJsonResponse.getFeatureCollection().getFeatures();

				request.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(0)
						.getGeometry().setCoordinates(Arrays.asList(geoJsonFeature[0].extractCoordinates().getXys()[0],
								geoJsonFeature[0].extractCoordinates().getXys()[1]));
				request.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(1)
						.getGeometry().setCoordinates(Arrays.asList(geoJsonFeature[1].extractCoordinates().getXys()[0],
								geoJsonFeature[1].extractCoordinates().getXys()[1]));
				request.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(2)
						.getGeometry().setCoordinates(Arrays.asList(geoJsonFeature[2].extractCoordinates().getXys()[0],
								geoJsonFeature[2].extractCoordinates().getXys()[1]));
				request.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getFeatures().get(3)
						.getGeometry().setCoordinates(Arrays.asList(geoJsonFeature[3].extractCoordinates().getXys()[0],
								geoJsonFeature[3].extractCoordinates().getXys()[1]));
				operationsApplied.addAll(convertGeoJsonResponse.getOperationsApplied());
				convertBinGridResponse.setAppliedOperations(operationsApplied);
				convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
						.setCoordinateReferenceSystemID(request.getToCRS());
				convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
						.setPersistableReferenceCrs(
								convertGeoJsonResponse.getFeatureCollection().getPersistableReferenceCrs());
				logger.info("CRS ID from outBinGrid. " + convertBinGridResponse.getOutBinGrid()
						.getABCDBinGridSpatialLocation().getAsIngestedcoordinates().getCoordinateReferenceSystemID());
			} else {
				String persistableRefString = getPersistableReferenceFromID(inBinGrid.getABCDBinGridSpatialLocation()
						.getAsIngestedcoordinates().getCoordinateReferenceSystemID(), true);
				logger.info("persistableRefString : " + String.valueOf(persistableRefString));
				if (persistableRefString != null)
					convertBinGridResponse.getOutBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
							.setPersistableReferenceCrs(persistableRefString);
			}
			if (StringUtils.isEmpty(inBinGrid.getBinGridDefinitionMethodTypeID()))
				inBinGrid.setBinGridDefinitionMethodTypeID(
						httpServletRequest.getHeader("data-partition-id") + BIN_GRID_METHOD_4_CORNER);
			convertBinGridResponse = this.crsConverter.squaring(request.getToCRS(), inBinGrid, convertBinGridResponse);
			convertBinGridResponse = convertedWgs84Coordinates(convertBinGridResponse, inBinGrid);
			return convertBinGridResponse;
		} catch (IllegalArgumentException illegalException) {
			logger.error("Exception from the convert call " + illegalException.getMessage());
			throw new ValidationException(illegalException.getMessage());
		}
	}
	
	public ConvertBinGridResponse convertedWgs84Coordinates(ConvertBinGridResponse convertBinGrid,
			AbstractBinGrid inBinGrid) {

		logger.info("Start of convertedWgs84Coordinates.");
		AbstractSpatialLocation location = convertBinGrid.getOutBinGrid().getABCDBinGridSpatialLocation();
		location.setWgs84Coordinates(null);
		try {
			DecimalFormat upto8Decimal = new DecimalFormat("0.00000000");
			GeoJsonFeatureCollection geoJsonFeatureCollection = this.crsConverter.prepareGeoJsonRequest(
					location.getAsIngestedcoordinates().getFeatures(),
					inBinGrid.getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
							.getCoordinateReferenceSystemID());
			ConvertGeoJsonRequest convertGeoJsonRequest = new ConvertGeoJsonRequest();
			convertGeoJsonRequest.setFeatureCollection(geoJsonFeatureCollection);
			convertGeoJsonRequest.setToCRS(Constants.WGS84);
			ConvertGeoJsonResponse convertGeoJsonResponse = convertGeoJson(convertGeoJsonRequest);
			GeoJsonFeature[] geoJsonFeature = convertGeoJsonResponse.getFeatureCollection().getFeatures();
			if (geoJsonFeature == null || geoJsonFeature.length < 4) {
				logger.info("WGS84 bin-grid coordinates were omitted: conversion did not return four corners.");
				return convertBinGrid;
			}
			List<List<Double>> corners = new ArrayList<>();
			for (int i = 0; i < 4; i++) {
				double x = geoJsonFeature[i].extractCoordinates().getXys()[0];
				double y = geoJsonFeature[i].extractCoordinates().getXys()[1];
				if (!Double.isFinite(x) || !Double.isFinite(y)) {
					logger.info("WGS84 bin-grid coordinates were omitted: conversion produced a non-finite corner.");
					return convertBinGrid;
				}
				corners.add(Arrays.asList(Double.valueOf(upto8Decimal.format(x)), Double.valueOf(upto8Decimal.format(y))));
			}
			Gson gson = new Gson();
			Type listType = new TypeToken<ArrayList<AbstractFeature>>() {
			}.getType();
			List<AbstractFeature> deepCopy = gson.fromJson(
					gson.toJson(location.getAsIngestedcoordinates().getFeatures()), listType);
			for (int i = 0; i < 4; i++) {
				deepCopy.get(i).setType(FEATURE_TYPE);
				deepCopy.get(i).getGeometry().setType(GEOMETRY_TYPE);
				deepCopy.get(i).getGeometry().setCoordinates(corners.get(i));
			}
			AbstractFeatureCollection wgs84 = new AbstractFeatureCollection();
			wgs84.setType(convertGeoJsonResponse.getFeatureCollection().getType());
			wgs84.setFeatures(deepCopy);
			location.setWgs84Coordinates(wgs84);
		} catch (ValidationException validationException) {
			location.setWgs84Coordinates(null);
			throw validationException;
		} catch (RuntimeException conversionFailure) {
			location.setWgs84Coordinates(null);
			logger.info("WGS84 bin-grid coordinates were omitted: " + conversionFailure.getMessage());
		}
		return convertBinGrid;
	}


}
