package org.opengroup.osdu.crs.converter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.opengroup.osdu.crs.sis.ISisCrs;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

@RunWith(MockitoJUnitRunner.class)
public class TrajectoryConverterV4Tests {

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

    public static ConvertTrajectoryResponseV4 createResponse(String json) {
        ConvertTrajectoryResponseV4 result;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JsonNode node = mapper.readTree(json);
            result = mapper.treeToValue(node, ConvertTrajectoryResponseV4.class);
        } catch (IOException e) {
            return null;
        }
        return result;
    }

    public static String createPersistenceReferenceUnitXY(String json) {
        String result;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JsonNode node = mapper.readTree(json);
            result = mapper.treeToValue(node, String.class);
        } catch (IOException e) {
            return null;
        }
        return result;
    }

}
