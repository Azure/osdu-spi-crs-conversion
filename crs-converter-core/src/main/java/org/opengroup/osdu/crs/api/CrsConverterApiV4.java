package org.opengroup.osdu.crs.api;

import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.MultiRecordInfo;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.interfaces.ICRSConverter;
import org.opengroup.osdu.crs.interfaces.IPointConverter;
import org.opengroup.osdu.crs.interfaces.ITrajectoryConverter;
import org.opengroup.osdu.crs.model.*;
import org.opengroup.osdu.crs.model.v4.*;
import org.opengroup.osdu.crs.osducoreserviceclient.storage.IStorageClient;
import org.opengroup.osdu.crs.util.Constants;
import org.opengroup.osdu.crs.util.RecordCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opengroup.osdu.crs.util.RecordIdNormalizer;

@Tag(name = Constants.SWAGGER_TAG_CRS_CONVERSION)
@CrossOrigin
@RestController
@RequestMapping("/v4")
public class CrsConverterApiV4 {
    private static final String BOUND_PROJECTED = "BoundProjected";
    private static final String PROJECTED = "Projected";
    private static final String UTF_8 = "UTF-8";
    private static final String RECORD_NOT_FOUND = "record not found:";
    private static final String UNIT_OF_MEASURE = "UnitOfMeasure";
    private final ITrajectoryConverter crsTrajectoryConverter;
    @Autowired
    private JaxRsDpsLog logger;
    @Autowired
    private IStorageClient storageClient;
    private RecordCache recordCache;
    private final IPointConverter pointConverter;
    private final ICRSConverter crsConverter;

    public CrsConverterApiV4(@NonNull ITrajectoryConverter crsTrajectoryConverter, @NonNull IPointConverter pointConverter,@NonNull ICRSConverter crsConverter) {
        this.crsTrajectoryConverter = crsTrajectoryConverter;
        this.pointConverter = pointConverter;
        this.crsConverter = crsConverter;
        this.recordCache = new RecordCache();
    }

    // parameter str could be a persistableReference string or recordID. mustID means parameter str must be a recordID
    public String getPersistableReferenceFromID(String str, boolean mustID) {
        String temp;
        try {
            temp = URLDecoder.decode(str, UTF_8);
        } catch (Exception e) {
            return str; // try our best to return user input
        }
        // persistableReference string starts with "{..}" - also check for JSON with leading whitespace
        if ((temp.trim().startsWith("{") || temp.contains("{\"")) && mustID == false) {
            return str;
        }
        // set temp as str as we don't want to decode. for example UnitOfMeasure:ft%5BUS%5D in record id
        temp = str;
        // check the cache for recordID with its persistableReference string
        String pr = recordCache.get(temp);
        if (pr != null) {
            return pr;
        }

        temp = RecordIdNormalizer.normalizeRecordID(temp);
        Record dataRecord;
        try {
            dataRecord = storageClient.getRecord(temp);
        }
        catch(RuntimeException exception){
            if(temp.contains(UNIT_OF_MEASURE)){
                String[] parts = temp.split(":");
                Pattern pattern = Pattern.compile("[!@#\\$%^&*()_+\\-=\\[\\] {};':\"\\\\|,.<>\\/?~`]");
                Matcher matcher = pattern.matcher(parts[parts.length - 1]);
                String decodedRecord = URLDecoder.decode(temp, StandardCharsets.UTF_8);
                if (!temp.equals(decodedRecord) || matcher.find()) {
                    Collection collection = new ArrayList();
                    collection.add(temp);
                    MultiRecordInfo multiDataRecord = storageClient.getRecords(collection);
                    if (multiDataRecord == null || multiDataRecord.getRecords().size() == 0)
                        throw new ValidationException(String.join(" ", RECORD_NOT_FOUND, temp));
                    else{
                        Map<String, Object> data = multiDataRecord.getRecords().get(0).getData();
                        pr = data.get("PersistableReference").toString();
                        if (pr != null) {
                            recordCache.put(temp, pr);
                            return pr;
                        } else {
                            throw new ValidationException(String.join(" ", "record does not have PersistableReference:", temp));
                        }
                    }
                } else
                    throw new AppException(HttpStatus.BAD_REQUEST.value(), "Error getting record",
                            "Please check the input type and format and try again.");
            }else
                throw new AppException(HttpStatus.BAD_REQUEST.value(), "Error getting record",
                        "Please check the input type and format and try again.");

        }
            if (dataRecord == null)
                throw new ValidationException(String.join(" ", RECORD_NOT_FOUND, temp));
            pr = dataRecord.getData().get("PersistableReference").toString();
            if (pr != null) {
                recordCache.put(temp, pr);
                return pr;
            } else {
                throw new ValidationException(String.join(" ", "record does not have PersistableReference:", temp));
            }
    }

    public String getUnitFromTrajectoryCRS(String trajectoryCRS) {
        String temp;
        try {
            temp = URLDecoder.decode(trajectoryCRS, UTF_8);
        } catch (Exception e) {
            return trajectoryCRS; // try our best to return user input
        }
        // If it's a persistable reference JSON, we can't extract HorizontalAxisUnitID from it
        // The caller should provide unitXY explicitly when using persistable reference
        if (temp.trim().startsWith("{") || temp.contains("{\"")) {
            return null; // Signal that unit must be provided separately
        }
        temp = RecordIdNormalizer.normalizeRecordID(temp);
        Record dataRecord = storageClient.getRecord(temp);
        if (dataRecord == null)
            throw new ValidationException(String.join(" ", RECORD_NOT_FOUND, temp));
        Map<String, Object> data = dataRecord.getData();
        Map<String, Object> coordinateSystem = (Map<String, Object>) data.get("CoordinateSystem");
        return (String) coordinateSystem.get("HorizontalAxisUnitID");
    }

    public boolean checkCRSType(String trajectoryCRS) {
        boolean flag = false;
        String temp;
        try {
            temp = URLDecoder.decode(trajectoryCRS, UTF_8);
        } catch (Exception e) {
            return false; // try our best to return user input
        }
        // If it's a persistable reference JSON, check if it contains "LBC" (LateBoundCRS) type
        // which indicates a projected CRS
        if (temp.trim().startsWith("{") || temp.contains("{\"")) {
            // It's a persistable reference JSON - check for projected CRS indicators
            return temp.contains("\"type\":\"LBC\"") || temp.contains("PROJCS");
        }
        temp = RecordIdNormalizer.normalizeRecordID(temp);
        Record dataRecord = storageClient.getRecord(temp);
        if (dataRecord == null)
            throw new ValidationException(String.join(" ", RECORD_NOT_FOUND, temp));
        Map<String, Object> data = dataRecord.getData();
        if (data != null && (data.get("Kind").toString().equalsIgnoreCase(BOUND_PROJECTED) || data.get("Kind").toString().equalsIgnoreCase(PROJECTED))) {
            flag = true;
        }
        return flag;
    }

    @PostMapping(value ="/convertTrajectory", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "${CrsConverterApi.convertTrajectoryV4.summary}", description = "${CrsConverterApi.convertTrajectoryV4.description}",
            security = {@SecurityRequirement(name = "Authorization")},tags = {"Convert"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.SWAGGER_TRJ_CONVERT_SUCCESS_RESPONSE, content = { @Content(schema = @Schema(implementation = ConvertTrajectoryResponseV4.class)) }),
            @ApiResponse(responseCode = "400", description = Constants.SWAGGER_CONVERT_BAD_INPUT_BASE_PATH, content = { @Content(schema = @Schema(implementation = AppError.class)) }),
            @ApiResponse(responseCode = "500", description = Constants.SWAGGER_CONVERT_UNKNOWN_ERROR, content = { @Content(schema = @Schema(implementation = AppError.class)) }),
            @ApiResponse(responseCode = "503", description = Constants.SWAGGER_CONVERT_OVERLOAD, content = { @Content(schema = @Schema(implementation = AppError.class)) })})
    @Parameter(hidden = true)
    public ConvertTrajectoryResponseV4 convertTrajectory(@Parameter(hidden = true) @RequestHeader MultiValueMap<String, String> headers,
                                                         @NonNull @Valid @RequestBody ConvertTrajectoryRequestV4 request) {
        String message = String.format("Using trajectory: %s", "no");
        logger.info(message);
        DpsHeaders dpsHeaders = DpsHeaders.createFromEntrySet(headers.entrySet());
        String trajectorycrs = request.getTrajectoryCRS();

        // Check CRS type first to determine if it's a projected CRS
        Boolean checkCRSType = checkCRSType(request.getTrajectoryCRS());
        // Handle unitXY based on CRS type
        // For projected CRS (BoundProjected/Projected), unitXY is needed for output coordinates
        if (!Constants.DX_DY_DZ.equals(request.getInputKind()) && checkCRSType) {
            // Check if trajectoryCRS is a persistable reference (JSON) or a record ID
            String tempCrs = request.getTrajectoryCRS();
            try {
                tempCrs = URLDecoder.decode(tempCrs, UTF_8);
            } catch (Exception e) {
                // ignore
            }
            boolean isPersistableReference = tempCrs.trim().startsWith("{") || tempCrs.contains("{\"");
            
            if (!Strings.isNullOrEmpty(request.getUnitXY())) {
                // unitXY explicitly provided - use it (works for both CRS ID and persistable reference)
                request.setUnitXY(getPersistableReferenceFromID(request.getUnitXY(), false));
            } else if (isPersistableReference) {
                // Persistable reference without unitXY - throw error
                throw new ValidationException("unitXY is required when trajectoryCRS is defined by a persistable reference string.");
            } else {
                // CRS record ID without unitXY - derive from record
                String unit = getUnitFromTrajectoryCRS(request.getTrajectoryCRS());
                if (unit != null) {
                    request.setUnitXY(getPersistableReferenceFromID(unit, false));
                }
            }
        } else if (!Strings.isNullOrEmpty(request.getUnitXY())) {
            request.setUnitXY(getPersistableReferenceFromID(request.getUnitXY(), false));
        }
        
        request.setTrajectoryCRS(getPersistableReferenceFromID(request.getTrajectoryCRS(), false));
        request.setUnitZ(getPersistableReferenceFromID(request.getUnitZ(), false));
        if (!Strings.isNullOrEmpty(request.getUnitMD())) {
            request.setUnitMD(getPersistableReferenceFromID(request.getUnitMD(), false));
        }
        MinimumDepthInterval minimumDepthInterval = request.getMD_i();
        if (minimumDepthInterval != null) {
            if (minimumDepthInterval.getMd_interval() != null && minimumDepthInterval.getMd_interval() > 0 && minimumDepthInterval.getMd_i() != null && !minimumDepthInterval.getMd_i().isEmpty()) {
                throw new ValidationException("Both md_i array and md_interval values are provided in the input.");
            } else if (minimumDepthInterval.getMd_interval() != null && minimumDepthInterval.getMd_interval() > 0) {
                List<Double> mdiList = computeMinimumDepthPointsUsingInterval(request.getInputStations().get(0).getMd(),
                        request.getInputStations().get(request.getInputStations().size() - 1).getMd(), minimumDepthInterval.getMd_interval());
                mdiList.add(request.getInputStations().get(request.getInputStations().size() - 1).getMd());
                minimumDepthInterval.setMd_i(mdiList);
            } else if (minimumDepthInterval.getMd_i() != null && !minimumDepthInterval.getMd_i().isEmpty() && checkMdiListForRange(request.getInputStations().get(0).getMd(), request.getInputStations().get(request.getInputStations().size() - 1).getMd(),
                    minimumDepthInterval.getMd_i())) {
                throw new ValidationException("md_i array values provided are not in range of MD stations.");
            }
        }
        if (Constants.MD_INCL.equals(request.getInputKind())) {

            request.getInputStations().stream().forEach(station -> {
                if (station.getAzimuth() != null)
                    throw new ValidationException("Azimuth data shouldn't be provided for input kind : " + request.getInputKind());
            });

            ConvertTrajectoryRequestV4 dummyRequest = request;
            dummyRequest.getInputStations().stream().forEach(station -> {
                station.setAzimuth(0.0);
            });
            dummyRequest.setInputKind(Constants.MD_INCL_AZIM);
            dummyRequest.setMethod(Constants.AZIMUTHAL_EQUIDISTANT);

            ConvertTrajectoryResponseV4 dummyResponse = this.crsTrajectoryConverter.convertTrajectoryV4(dpsHeaders, dummyRequest, checkCRSType, true, true);

            AtomicInteger index = new AtomicInteger(0);
                dummyRequest.getInputStations().stream().forEach(data -> {
                    data.setMd(dummyResponse.getStations().get(index.getAndIncrement()).getDZ());
                    data.setAzimuth(data.getInclination());
                    data.setInclination(0.0);
                });
            ConvertTrajectoryResponseV4 response = this.crsTrajectoryConverter.convertTrajectoryV4(dpsHeaders, dummyRequest, checkCRSType, true, false);
            response.getStations().stream().forEach(station -> station.setOriginal(false));
            response.getOperationsApplied().add(0,Constants.INC_ONLY_OPERTN_APPL);
            response.getOperationsApplied().add(dummyResponse.getOperationsApplied().get(dummyResponse.getOperationsApplied().size() - 1));
            response.getOperationsApplied().add(dummyResponse.getOperationsApplied().get(dummyResponse.getOperationsApplied().size() - 2));
            return response;
        }

        if (Constants.DX_DY_DZ.equals(request.getInputKind())) {
            request.getInputStations().stream().forEach(station -> {
                if (station.getAzimuth() != null || station.getInclination()!= null || station.getMd()!= null)
                    throw new ValidationException("Azimuth / Inclination / Minimum Depth data shouldn't be provided for input kind : " + request.getInputKind());
            });
            List<TrajectoryStationInV4> calculatedAzIncMdInputStations =this.crsTrajectoryConverter.populateMdInclAziFromRequestV4ForInverseMinimumCurvature(request);

            ConvertTrajectoryRequestV4 dummyRequest = request;
            if (trajectorycrs.contains(BOUND_PROJECTED) || trajectorycrs.contains(PROJECTED)) {
                String unit = getUnitFromTrajectoryCRS(trajectorycrs);
                dummyRequest.setUnitXY(getPersistableReferenceFromID(unit, false));
            }
            dummyRequest.setInputKind(Constants.MD_INCL_AZIM);
            AtomicInteger index = new AtomicInteger(0);
            dummyRequest.getInputStations().stream().forEach(data -> {
                data.setMd(calculatedAzIncMdInputStations.get(index.get()).getMd());
                data.setAzimuth(calculatedAzIncMdInputStations.get(index.get()).getAzimuth());
                data.setInclination(calculatedAzIncMdInputStations.get(index.getAndIncrement()).getInclination());
            });
            ConvertTrajectoryResponseV4 response = this.crsTrajectoryConverter.convertTrajectoryV4(dpsHeaders, dummyRequest, checkCRSType, true, false);
            response.getOperationsApplied().add(0, "Input dX_dY_dZ .  Applying inverse minimum curvature to compute Md_Incl_Azim");
            DecimalFormat upto3decimal = new DecimalFormat("#.###");
            response.getStations().stream().forEach(station -> {
                station.setOriginal(false);
                station.setMd(Double.parseDouble(upto3decimal.format(station.getMd())));
                station.setInclination(Double.parseDouble(upto3decimal.format(station.getInclination())));
                station.setAzimuthTN(Double.parseDouble(upto3decimal.format(station.getAzimuthTN())));
                station.setAzimuthGN(Double.parseDouble(upto3decimal.format(station.getAzimuthGN())));
            });
            return response;
        }
        return this.crsTrajectoryConverter.convertTrajectoryV4(dpsHeaders, request, checkCRSType, true, false);
    }

    public boolean checkMdiListForRange(Double firstMd, Double lastMd, List<Double> mdiList) {
        boolean checkRange = false;
        for (int count = 0; count < mdiList.size(); count++) {
            if (mdiList.get(count) < firstMd || mdiList.get(count) > lastMd) {
                checkRange = true;
                break;
            }
        }
        return checkRange;
    }

    public List<Double> computeMinimumDepthPointsUsingInterval(Double firstMd, Double lastMd, Double mdInterval) {
        List<Double> mdiList = new ArrayList<>();
        while (lastMd > firstMd && lastMd > mdInterval) {
            mdiList.add(firstMd);
            firstMd += mdInterval;
        }
        return mdiList;
    }

    @PostMapping(value = "/convert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "${CrsConverterApi.convertPoint.summary}", description = "${CrsConverterApi.convertPoint.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"Convert"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.SWAGGER_CONVERT_SUCCESS_RESPONSE, content = { @Content(schema = @Schema(implementation = ConvertPointsResponse.class)) }),
            @ApiResponse(responseCode = "400", description = Constants.SWAGGER_CONVERT_BAD_INPUT_BASE_PATH,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = Constants.SWAGGER_CONVERT_UNKNOWN_ERROR,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = Constants.SWAGGER_CONVERT_OVERLOAD,  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    public ConvertPointsResponseV4 convertPointV4(@NonNull @Valid @RequestBody ConvertPointsRequestV4 request) {
        //Added new optional parameter transform for explicit transfrom
        String transform = getPersistableReferenceFromID(request.getTransformation(), false);
        String fromCrs = getPersistableReferenceFromID(request.getFromCRS(), false);
        String toCrs = getPersistableReferenceFromID(request.getToCRS(), false);

        double[] xyCoordinates = this.pointConverter.mergeXYCoordinates(request.getPoints());
        double[] zCoordinates = this.pointConverter.mergeZCoordinates(request.getPoints());
        ConvertPointsResponseV4 response = this.crsConverter.convertPointV4(fromCrs, toCrs,transform, xyCoordinates,
                zCoordinates);
        response.setPoints(this.pointConverter.convertValuesToPoints(xyCoordinates, zCoordinates));
        return response;
    }

    @PostMapping("/convertGeoJson")
    @Operation(summary = "${CrsConverterApi.geo_json_convert.summary}", description = "${CrsConverterApi.geo_json_convert.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"Convert"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = Constants.SWAGGER_CONVERT_SUCCESS_RESPONSE, content = { @Content(schema = @Schema(implementation = ConvertGeoJsonResponse.class)) }),
            @ApiResponse(responseCode = "400", description = Constants.SWAGGER_CONVERT_BAD_INPUT_BASE_PATH,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = Constants.SWAGGER_CONVERT_UNKNOWN_ERROR,  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = Constants.SWAGGER_CONVERT_OVERLOAD,  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    public ConvertGeoJsonResponse convertGeoJsonV4(@NonNull @Valid @RequestBody ConvertGeoJsonRequestV4 request) {
        GeoJsonFeatureCollection features = request.getFeatureCollection();
        String toCrs = getPersistableReferenceFromID(request.getToCRS(), false);
        String toUnitZ = getPersistableReferenceFromID(request.getToUnitZ(), false);
        String transform = getPersistableReferenceFromID(request.getTransformation(), false);
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

        ConvertGeoJsonResponse response = this.crsConverter.convertGeoJsonV4(
                features,
                toCrs,
                toUnitZ,transform);
        return response;
    }

}
