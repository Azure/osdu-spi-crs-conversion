package org.opengroup.osdu.crs.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.crs.converter.PointConverter;
import org.opengroup.osdu.crs.interfaces.ICRSConverter;
import org.opengroup.osdu.crs.interfaces.ITrajectoryConverter;
import org.opengroup.osdu.crs.middleware.RestExceptionHandler;
import org.opengroup.osdu.crs.model.ConvertTrajectoryResponse;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryResponseV4;
import org.opengroup.osdu.crs.osducoreserviceclient.storage.IStorageClient;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CrsConverterApiTrajectoryHttpTests {
    private static final String CRS_ID =
            "osdu:reference-data--CoordinateReferenceSystem:Projected:EPSG::32631:";
    private static final String UNIT_ID = "osdu:reference-data--UnitOfMeasure:m:";
    private static final String PERSISTABLE = "{\"type\":\"USO\"}";

    private ITrajectoryConverter trajectory;
    private IStorageClient storage;
    private MockMvc v2;
    private MockMvc v3;
    private MockMvc v4;

    @BeforeEach
    void setUp() {
        trajectory = mock(ITrajectoryConverter.class);
        storage = mock(IStorageClient.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        RestExceptionHandler handler = new RestExceptionHandler();
        ReflectionTestUtils.setField(handler, "jaxRsDpsLog", mock(JaxRsDpsLog.class));

        CrsConverterApiV2 apiV2 = new CrsConverterApiV2(mock(ICRSConverter.class), trajectory, new PointConverter());
        ReflectionTestUtils.setField(apiV2, "logger", mock(JaxRsDpsLog.class));
        v2 = MockMvcBuilders.standaloneSetup(apiV2).setControllerAdvice(handler).setValidator(validator).build();

        CrsConverterApiV3 apiV3 = new CrsConverterApiV3(mock(ICRSConverter.class), trajectory, new PointConverter());
        ReflectionTestUtils.setField(apiV3, "logger", mock(JaxRsDpsLog.class));
        ReflectionTestUtils.setField(apiV3, "StorageClient", storage);
        v3 = MockMvcBuilders.standaloneSetup(apiV3).setControllerAdvice(handler).setValidator(validator).build();

        CrsConverterApiV4 apiV4 = new CrsConverterApiV4(trajectory, new PointConverter(), mock(ICRSConverter.class));
        ReflectionTestUtils.setField(apiV4, "logger", mock(JaxRsDpsLog.class));
        ReflectionTestUtils.setField(apiV4, "storageClient", storage);
        v4 = MockMvcBuilders.standaloneSetup(apiV4).setControllerAdvice(handler).setValidator(validator).build();
    }

    @Test
    void omittedUnitXyPassesHttpValidationOnV2() throws Exception {
        when(trajectory.convertTrajectory(any(), any())).thenReturn(new ConvertTrajectoryResponse());

        v2.perform(post("/v2/convertTrajectory").contentType(MediaType.APPLICATION_JSON).content(body(false)))
                .andExpect(status().isOk());

        verify(trajectory).convertTrajectory(any(), argThat(request -> request.getUnitXY() == null));
    }

    @Test
    void projectedRecordWithoutUnitXyResolvesTheFlatStorageUnitOnV3() throws Exception {
        Record record = projectedRecord();
        when(storage.getRecord(any())).thenReturn(record);
        when(trajectory.convertTrajectory(any(), any())).thenReturn(new ConvertTrajectoryResponse());

        v3.perform(post("/v3/convertTrajectory").contentType(MediaType.APPLICATION_JSON).content(body(false)))
                .andExpect(status().isOk());

        verify(trajectory).convertTrajectory(any(), argThat(request ->
                PERSISTABLE.equals(request.getUnitXY()) && PERSISTABLE.equals(request.getTrajectoryCRS())));
    }

    @Test
    void projectedRecordRejectsAnExplicitUnitXyOnV3() throws Exception {
        v3.perform(post("/v3/convertTrajectory").contentType(MediaType.APPLICATION_JSON).content(body(true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("unitXY should not be provided for BoundProjected and Projected CRS."));

        verifyNoInteractions(trajectory);
    }

    @Test
    void projectedRecordWithoutUnitXyResolvesTheFlatStorageUnitOnV4() throws Exception {
        Record record = projectedRecord();
        when(storage.getRecord(any())).thenReturn(record);
        when(trajectory.convertTrajectoryV4(any(), any(), eq(true), eq(true), eq(false)))
                .thenReturn(new ConvertTrajectoryResponseV4());

        v4.perform(post("/v4/convertTrajectory").contentType(MediaType.APPLICATION_JSON).content(body(false)))
                .andExpect(status().isOk());

        verify(trajectory).convertTrajectoryV4(any(), argThat(request ->
                PERSISTABLE.equals(request.getUnitXY()) && PERSISTABLE.equals(request.getTrajectoryCRS())),
                eq(true), eq(true), eq(false));
    }

    @Test
    void projectedRecordWithoutAHorizontalUnitIs400RatherThan500() throws Exception {
        Record record = mock(Record.class);
        when(record.getData()).thenReturn(Map.of("Kind", "projected", "PersistableReference", PERSISTABLE));
        when(storage.getRecord(any())).thenReturn(record);

        v4.perform(post("/v4/convertTrajectory").contentType(MediaType.APPLICATION_JSON).content(body(false)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(
                        "CoordinateSystem.HorizontalAxisUnitID")));

        verifyNoInteractions(trajectory);
    }

    private static Record projectedRecord() {
        Map<String, Object> data = new HashMap<>();
        data.put("Kind", "projected");
        data.put("CoordinateSystem.HorizontalAxisUnitID", UNIT_ID);
        data.put("PersistableReference", PERSISTABLE);
        Record record = mock(Record.class);
        when(record.getData()).thenReturn(data);
        return record;
    }

    private static String body(boolean includeUnitXy) {
        String unitXy = includeUnitXy ? "\"unitXY\":\"" + UNIT_ID + "\"," : "";
        return "{\"trajectoryCRS\":\"" + CRS_ID + "\","
                + unitXy
                + "\"azimuthReference\":\"GN\","
                + "\"unitZ\":\"" + PERSISTABLE.replace("\"", "\\\"") + "\","
                + "\"method\":\"AzimuthalEquidistant\","
                + "\"referencePoint\":{\"x\":500000,\"y\":0,\"z\":0},"
                + "\"inputStations\":[{\"md\":0,\"inclination\":0,\"azimuth\":0}]}";
    }
}
