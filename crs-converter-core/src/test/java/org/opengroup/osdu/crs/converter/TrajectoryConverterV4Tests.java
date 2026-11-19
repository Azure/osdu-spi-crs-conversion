package org.opengroup.osdu.crs.converter;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.crs.api.exception.BadRequestException;
import org.opengroup.osdu.crs.model.*;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryRequestV4;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryResponseV4;
import org.opengroup.osdu.crs.model.v4.MinimumDepthInterval;
import org.opengroup.osdu.crs.model.v4.TrajectoryStationInV4;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.util.Constants;

import java.util.Arrays;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

@RunWith(MockitoJUnitRunner.class)
public class TrajectoryConverterV4Tests {

    private static final String PRJ32631 = "{\"wkt\":\"PROJCS[\\\"WGS_1984_UTM_Zone_31N\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",3.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",32631]]\",\"ver\":\"PE_10_3_1\",\"name\":\"WGS_1984_UTM_Zone_31N\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32631\"},\"type\":\"LBC\"}";
    private static final String UNIT_M = "{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}";

    @Mock
    AzimuthCorrector azimuthCorrector;

    @Mock
    TrajectoryConverter trajectoryConverter;

    @Test
    public void convertTrajectoryWithSuccessfulResponseLMP() {
        TrajectoryComputationState state = new TrajectoryComputationState();
        state.setMethod(TrajectoryComputationMethod.LeesModifiedProposal);
        state.setAzimuthReference(AzimuthReferenceType.GRID_NORTH);

        lenient().when(trajectoryConverter.isRequestValidV4(Mockito.any(ConvertTrajectoryRequestV4.class), Mockito.any(TrajectoryComputationStateV4.class))).thenReturn(Boolean.TRUE.booleanValue());
        lenient().when(azimuthCorrector.createProjectionCorrectionSet(Mockito.anyString(), Mockito.any(Point.class), Mockito.any(IUnit.class))).thenReturn(new ProjectionCorrectionSet());
        lenient().when(trajectoryConverter.normalizeTrajectory(Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(TrajectoryComputationState.class))).thenReturn(new ConvertTrajectoryResponseV4());
        lenient().when(trajectoryConverter.callTrajectoryEngineService(Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(Point.class), Mockito.any(TrajectoryComputationState.class))).thenReturn(Boolean.TRUE.booleanValue());
        lenient().doNothing().when(trajectoryConverter).minimumCurvature(Mockito.any(Point.class), Mockito.anyList());
        lenient().when(trajectoryConverter.minimumCurvaturePair(Mockito.any(Point.class), Mockito.anyList(), Mockito.anyInt())).thenReturn(new Point());
        lenient().doNothing().when(trajectoryConverter).deNormalizeTrajectory(Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(TrajectoryComputationState.class));
        lenient().doNothing().when(trajectoryConverter).convertPointsLmp(Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(TrajectoryComputationState.class));
        lenient().when(trajectoryConverter.computeScaleFactorAndConvergence(Mockito.any(DpsHeaders.class), Mockito.any(ConvertTrajectoryRequestV4.class), Mockito.anyBoolean(), Mockito.any())).thenReturn(new ScaleConvergence());
        lenient().doNothing().when(trajectoryConverter).convertToWgs84(Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(TrajectoryComputationState.class));
        lenient().when(trajectoryConverter.convertTrajectoryV4(Mockito.any(DpsHeaders.class), Mockito.any(ConvertTrajectoryRequestV4.class), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(new ConvertTrajectoryResponseV4());

        assertNotNull(ConvertTrajectoryResponseV4.class);
    }


    @Test
    public void convertTrajectoryWithSuccessfulResponseGNL() {
        TrajectoryComputationState state = new TrajectoryComputationState();
        state.setMethod(TrajectoryComputationMethod.GridNorthLocal);

        lenient().when(trajectoryConverter.isRequestValidV4(Mockito.any(ConvertTrajectoryRequestV4.class), Mockito.any(TrajectoryComputationStateV4.class))).thenReturn(Boolean.TRUE.booleanValue());
        lenient().when(azimuthCorrector.createProjectionCorrectionSet(Mockito.anyString(), Mockito.any(Point.class), Mockito.any(IUnit.class))).thenReturn(new ProjectionCorrectionSet());
        lenient().when(trajectoryConverter.normalizeTrajectory(Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(TrajectoryComputationState.class))).thenReturn(new ConvertTrajectoryResponse());
        lenient().when(trajectoryConverter.callTrajectoryEngineService(Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(Point.class), Mockito.any(TrajectoryComputationState.class))).thenReturn(Boolean.TRUE.booleanValue());
        lenient().doNothing().when(trajectoryConverter).minimumCurvature(Mockito.any(Point.class), Mockito.anyList());
        lenient().when(trajectoryConverter.minimumCurvaturePair(Mockito.any(Point.class), Mockito.anyList(), Mockito.anyInt())).thenReturn(new Point());
        lenient().doNothing().when(trajectoryConverter).deNormalizeTrajectory(Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(TrajectoryComputationState.class));
        lenient().doNothing().when(trajectoryConverter).convertPoints(Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(ISisCrs.class), Mockito.any(TrajectoryComputationState.class));
        lenient().when(trajectoryConverter.computeInterpolationForMDiInput(Mockito.any(ConvertTrajectoryRequestV4.class), Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(TrajectoryComputationStateV4.class), Mockito.anyBoolean(), Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(new ConvertTrajectoryResponseV4());
        lenient().doNothing().when(trajectoryConverter).convertToWgs84V4(Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(TrajectoryComputationStateV4.class));
        lenient().when(trajectoryConverter.computeScaleFactorAndConvergence(Mockito.any(DpsHeaders.class), Mockito.any(ConvertTrajectoryRequestV4.class), Mockito.anyBoolean(), Mockito.any())).thenReturn(new ScaleConvergence());
        lenient().doNothing().when(trajectoryConverter).computeUnscaledValuesForXAndY(new ConvertTrajectoryResponseV4());
        lenient().doNothing().when(trajectoryConverter).convertToWgs84(Mockito.any(ConvertTrajectoryResponse.class), Mockito.any(TrajectoryComputationState.class));
        lenient().when(trajectoryConverter.convertTrajectoryV4(Mockito.any(DpsHeaders.class), Mockito.any(ConvertTrajectoryRequestV4.class), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(new ConvertTrajectoryResponseV4());

        assertNotNull(ConvertTrajectoryResponseV4.class);
    }

    @Test
    public void convertTrajectoryWithSuccessfulResponseAZ() {
        TrajectoryComputationState state = new TrajectoryComputationState();
        state.setMethod(TrajectoryComputationMethod.AzimuthalEquidistant);

        lenient().when(trajectoryConverter.isRequestValidV4(Mockito.any(ConvertTrajectoryRequestV4.class), Mockito.any(TrajectoryComputationStateV4.class))).thenReturn(Boolean.TRUE.booleanValue());
        lenient().when(azimuthCorrector.createProjectionCorrectionSet(Mockito.anyString(), Mockito.any(Point.class), Mockito.any(IUnit.class))).thenReturn(new ProjectionCorrectionSet());
        lenient().when(trajectoryConverter.normalizeTrajectory(Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(TrajectoryComputationState.class))).thenReturn(new ConvertTrajectoryResponseV4());
        lenient().when(trajectoryConverter.callTrajectoryEngineService(Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(Point.class), Mockito.any(TrajectoryComputationState.class))).thenReturn(Boolean.TRUE.booleanValue());
        lenient().doNothing().when(trajectoryConverter).minimumCurvature(Mockito.any(Point.class), Mockito.anyList());
        lenient().when(trajectoryConverter.minimumCurvaturePair(Mockito.any(Point.class), Mockito.anyList(), Mockito.anyInt())).thenReturn(new Point());
        lenient().doNothing().when(trajectoryConverter).deNormalizeTrajectory(Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(TrajectoryComputationState.class));
        lenient().doNothing().when(trajectoryConverter).convertPoints(Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(ISisCrs.class), Mockito.any(TrajectoryComputationState.class));
        lenient().doNothing().when(trajectoryConverter).convertToWgs84(Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(TrajectoryComputationState.class));
        lenient().when(trajectoryConverter.convertTrajectoryV4(Mockito.any(DpsHeaders.class), Mockito.any(ConvertTrajectoryRequestV4.class), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(new ConvertTrajectoryResponseV4());

        assertNotNull(ConvertTrajectoryResponseV4.class);
    }

    @Test
    public void convertTrajectoryWithStationsFailure() {
        final String errorMsg = "Stations Failure.";
        TrajectoryComputationState state = new TrajectoryComputationState();
        state.setMethod(TrajectoryComputationMethod.AzimuthalEquidistant);

        lenient().when(trajectoryConverter.isRequestValidV4(Mockito.any(ConvertTrajectoryRequestV4.class), Mockito.any(TrajectoryComputationStateV4.class))).thenReturn(Boolean.TRUE.booleanValue());
        lenient().when(azimuthCorrector.createProjectionCorrectionSet(Mockito.anyString(), Mockito.any(Point.class), Mockito.any(IUnit.class))).thenReturn(new ProjectionCorrectionSet());
        lenient().when(trajectoryConverter.normalizeTrajectory(Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(TrajectoryComputationState.class))).thenReturn(new ConvertTrajectoryResponseV4());
        lenient().when(trajectoryConverter.callTrajectoryEngineService(Mockito.any(ConvertTrajectoryResponseV4.class), Mockito.any(Point.class), Mockito.any(TrajectoryComputationState.class))).thenReturn(Boolean.FALSE.booleanValue());
        try {
            lenient().when(trajectoryConverter.convertTrajectoryV4(Mockito.any(DpsHeaders.class), Mockito.any(ConvertTrajectoryRequestV4.class), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenThrow(BadRequestException.class);
        }catch (Exception e) {
            BadRequestException apiException = (BadRequestException) e;
            assertEquals(apiException.getMessage(), errorMsg);
        }

    }

    @Test
    public void convertTrajectoryWithInvalidRequest() {
        final String errorMsg = "Invalid Request.";
        TrajectoryComputationState state = new TrajectoryComputationState();
        state.setMethod(TrajectoryComputationMethod.AzimuthalEquidistant);

        lenient().when(trajectoryConverter.isRequestValidV4(Mockito.any(ConvertTrajectoryRequestV4.class), Mockito.any(TrajectoryComputationStateV4.class))).thenReturn(Boolean.FALSE.booleanValue());

        try {
            lenient().when(trajectoryConverter.convertTrajectoryV4(Mockito.any(DpsHeaders.class), Mockito.any(ConvertTrajectoryRequestV4.class), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenThrow(BadRequestException.class);
        }catch (Exception e) {
            BadRequestException apiException = (BadRequestException) e;
            assertEquals(apiException.getMessage(), errorMsg);
        }

    }

    @Test
    public void referencePointOutsideProjectionDomainStillReturnsStations() {
        ConvertTrajectoryRequestV4 request = new ConvertTrajectoryRequestV4();
        request.setUnitXY(UNIT_M.replace("1.0", "0.3048"));
        request.setUnitZ(UNIT_M);
        request.setInputKind("Md_Incl_Azim");
        request.setMethod("LeesModifiedProposal");
        request.setTrajectoryCRS("{\"wkt\":\"PROJCS[\\\"WGS 84 / UTM zone 31N\\\",GEOGCS[\\\"WGS 84\\\",DATUM[\\\"WGS_1984\\\",SPHEROID[\\\"WGS 84\\\",6378137,298.257223563]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"latitude_of_origin\\\",0],PARAMETER[\\\"central_meridian\\\",3],PARAMETER[\\\"scale_factor\\\",0.9996],PARAMETER[\\\"false_easting\\\",500000],PARAMETER[\\\"false_northing\\\",0],UNIT[\\\"metre\\\",1]]\",\"type\":\"LBC\",\"name\":\"WGS 84 / UTM zone 31N\",\"ver\":\"PE_10_3_1\"}");
        request.setInputStations(Arrays.asList(new TrajectoryStationInV4(
                -4.906092645308414e16, -1.0923441151226573e-39, -3.204100050124853e80, Double.NaN, Double.NaN, Double.NaN)));
        for (Point reference : new Point[]{
                new Point(-1980739702920513.0, 4.979180723076012e16, 5.960066884406833e-242),
                new Point(0.0, 0.0, 0.0)}) {
            request.setReferencePoint(reference);
            ConvertTrajectoryResponseV4 response = new TrajectoryConverter()
                    .convertTrajectoryV4(new DpsHeaders(), request, true, true, false);
            assertEquals(1, response.getStations().size());
            assertTrue(Double.isNaN(response.getStations().get(0).getPoint().getX()));
            assertTrue(Double.isNaN(response.getStations().get(0).getPoint().getY()));
            assertTrue(Double.isNaN(response.getStations().get(0).getDxTN()));
            assertTrue(Double.isNaN(response.getStations().get(0).getDyTN()));
            assertTrue(Double.isNaN(response.getStations().get(0).getDZ()));
            assertNotEquals(0.0, response.getStations().get(0).getDxTN());
            assertNotNull(response.getLocalCRS());
            assertTrue(response.getOperationsApplied().stream().anyMatch(op -> op.contains("could not be projected")));
        }
    }

    @Test
    public void convertTrajectoryV4_mdi_returnsStationsIOnlyWhenInterpolateIsTrue() {
        for (boolean interpolate : new boolean[]{true, false}) {
            ConvertTrajectoryRequestV4 request = new ConvertTrajectoryRequestV4();
            request.setTrajectoryCRS(PRJ32631);
            request.setUnitXY(UNIT_M);
            request.setUnitZ(UNIT_M);
            request.setAzimuthReference("GN");
            request.setReferencePoint(new Point(400000.0, 6500000.0, 0.0));
            request.setMethod(Constants.AZIMUTHAL_EQUIDISTANT);
            request.setInputKind(Constants.MD_INCL_AZIM);
            request.setInputStations(Arrays.asList(
                    new TrajectoryStationInV4(0.0, 0.0, 0.0, null, null, null),
                    new TrajectoryStationInV4(100.0, 10.0, 45.0, null, null, null),
                    new TrajectoryStationInV4(200.0, 20.0, 45.0, null, null, null)));
            request.setMD_i(new MinimumDepthInterval(Arrays.asList(50.0, 150.0), null));
            request.setInterpolate(interpolate);

            ConvertTrajectoryResponseV4 response = new TrajectoryConverter().convertTrajectoryV4(new DpsHeaders(), request, true, true, false);

            if (interpolate) {
                assertEquals(2, response.getStations_i().size());
                assertEquals(50.0, response.getStations_i().get(0).getMd(), 1e-6);
                assertEquals(150.0, response.getStations_i().get(1).getMd(), 1e-6);
            } else {
                assertNull(response.getStations_i());
            }
        }
    }

    public static ConvertTrajectoryResponseV4 createResponse(String json) {
        ConvertTrajectoryResponseV4 result;
        try {
            JsonMapper mapper = JsonMapper.builderWithJackson2Defaults()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .build();
            JsonNode node = mapper.readTree(json);
            result = mapper.treeToValue(node, ConvertTrajectoryResponseV4.class);
        } catch (JacksonException e) {
            return null;
        }
        return result;
    }

    public static String createPersistenceReferenceUnitXY(String json) {
        String result;
        try {
            JsonMapper mapper = JsonMapper.builderWithJackson2Defaults()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .build();
            JsonNode node = mapper.readTree(json);
            result = mapper.treeToValue(node, String.class);
        } catch (JacksonException e) {
            return null;
        }
        return result;
    }

}
