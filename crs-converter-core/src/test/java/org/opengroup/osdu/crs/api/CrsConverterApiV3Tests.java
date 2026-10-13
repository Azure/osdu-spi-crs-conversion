package org.opengroup.osdu.crs.api;

import jakarta.validation.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.crs.interfaces.ICRSConverter;
import org.opengroup.osdu.crs.interfaces.IPointConverter;
import org.opengroup.osdu.crs.interfaces.ITrajectoryConverter;
import org.opengroup.osdu.crs.model.ConvertPointsRequest;
import org.opengroup.osdu.crs.model.ConvertPointsResponse;
import org.opengroup.osdu.crs.model.ConvertTrajectoryRequest;
import org.opengroup.osdu.crs.model.ConvertTrajectoryResponse;
import org.opengroup.osdu.crs.osducoreserviceclient.storage.IStorageClient;
import org.opengroup.osdu.crs.util.RecordIdNormalizer;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrsConverterApiV3Tests {

    private static final String FROM_CRS_ID = "osdu-dev:reference-data--CoordinateReferenceSystem:Projected:EPSG::32621";
    private static final String TO_CRS_ID = "osdu-dev:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4326";
    private static final String PERSISTABLE_REFERENCE = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"}}";
    private static final String HORIZONTAL_AXIS_UNIT_ID = "osdu-dev:reference-data--UnitOfMeasure:m:";

    @Mock
    private ICRSConverter crsConverter;

    @Mock
    private ITrajectoryConverter crsTrajectoryConverter;

    @Mock
    private IPointConverter pointConverter;

    @Mock
    private IStorageClient storageClient;

    @Mock
    private JaxRsDpsLog logger;

    private CrsConverterApiV3 crsConverterApiV3;

    @Before
    public void setUp() {
        crsConverterApiV3 = new CrsConverterApiV3(crsConverter, crsTrajectoryConverter, pointConverter);
        // fields are injected by Spring in production; names match the declarations in CrsConverterApiV3
        ReflectionTestUtils.setField(crsConverterApiV3, "StorageClient", storageClient);
        ReflectionTestUtils.setField(crsConverterApiV3, "logger", logger);
    }

    @Test
    public void convertPointThrowsValidationExceptionWhenRecordHasNoPersistableReference() {
        Record record = mock(Record.class);
        when(record.getData()).thenReturn(new HashMap<>());
        when(storageClient.getRecord(anyString())).thenReturn(record);

        try {
            crsConverterApiV3.convertPoint(request());
            fail("Expected ValidationException for a record without PersistableReference");
        } catch (ValidationException e) {
            assertEquals("record does not have PersistableReference: "
                    + RecordIdNormalizer.normalizeRecordID(FROM_CRS_ID), e.getMessage());
        }
    }

    @Test
    public void convertPointThrowsValidationExceptionWhenRecordNotFound() {
        when(storageClient.getRecord(anyString())).thenReturn(null);

        try {
            crsConverterApiV3.convertPoint(request());
            fail("Expected ValidationException for a missing record");
        } catch (ValidationException e) {
            assertEquals("record not found: " + RecordIdNormalizer.normalizeRecordID(FROM_CRS_ID), e.getMessage());
        }
    }

    @Test
    public void convertPointResolvesPersistableReferenceWhenPresent() {
        Map<String, Object> data = new HashMap<>();
        data.put("PersistableReference", PERSISTABLE_REFERENCE);
        Record record = mock(Record.class);
        when(record.getData()).thenReturn(data);
        when(storageClient.getRecord(anyString())).thenReturn(record);
        when(pointConverter.mergeXYCoordinates(any())).thenReturn(new double[0]);
        when(pointConverter.mergeZCoordinates(any())).thenReturn(new double[0]);
        when(pointConverter.convertValuesToPoints(any(), any())).thenReturn(Collections.emptyList());
        when(crsConverter.convertPoint(anyString(), anyString(), any(), any())).thenReturn(new ConvertPointsResponse());

        ConvertPointsResponse response = crsConverterApiV3.convertPoint(request());

        assertNotNull(response);
        verify(crsConverter).convertPoint(eq(PERSISTABLE_REFERENCE), eq(PERSISTABLE_REFERENCE), any(), any());
    }

    @Test
    public void convertTrajectoryThrowsValidationExceptionWhenRecordNotFound() {
        when(storageClient.getRecord(anyString())).thenReturn(null);

        try {
            crsConverterApiV3.convertTrajectory(new LinkedMultiValueMap<>(), trajectoryRequest());
            fail("Expected ValidationException for a missing trajectory CRS record");
        } catch (ValidationException e) {
            assertEquals("record not found: " + RecordIdNormalizer.normalizeRecordID(FROM_CRS_ID), e.getMessage());
        }
    }

    @Test
    public void convertTrajectoryThrowsValidationExceptionWhenCoordinateSystemMissing() {
        Record record = mock(Record.class);
        when(record.getData()).thenReturn(new HashMap<>());
        when(storageClient.getRecord(anyString())).thenReturn(record);

        try {
            crsConverterApiV3.convertTrajectory(new LinkedMultiValueMap<>(), trajectoryRequest());
            fail("Expected ValidationException for a record without CoordinateSystem");
        } catch (ValidationException e) {
            assertEquals("record does not have CoordinateSystem: "
                    + RecordIdNormalizer.normalizeRecordID(FROM_CRS_ID), e.getMessage());
        }
    }

    @Test
    public void convertTrajectoryThrowsValidationExceptionWhenHorizontalAxisUnitIdMissing() {
        Record record = recordWithCoordinateSystem(new HashMap<>());
        when(storageClient.getRecord(anyString())).thenReturn(record);

        try {
            crsConverterApiV3.convertTrajectory(new LinkedMultiValueMap<>(), trajectoryRequest());
            fail("Expected ValidationException for a record without CoordinateSystem.HorizontalAxisUnitID");
        } catch (ValidationException e) {
            assertEquals("record does not have CoordinateSystem.HorizontalAxisUnitID: "
                    + RecordIdNormalizer.normalizeRecordID(FROM_CRS_ID), e.getMessage());
        }
    }

    @Test
    public void convertTrajectoryThrowsValidationExceptionWhenHorizontalAxisUnitIdIsBlank() {
        Map<String, Object> coordinateSystem = new HashMap<>();
        coordinateSystem.put("HorizontalAxisUnitID", "   ");
        Record record = recordWithCoordinateSystem(coordinateSystem);
        when(storageClient.getRecord(anyString())).thenReturn(record);

        try {
            crsConverterApiV3.convertTrajectory(new LinkedMultiValueMap<>(), trajectoryRequest());
            fail("Expected ValidationException for a blank CoordinateSystem.HorizontalAxisUnitID");
        } catch (ValidationException e) {
            assertEquals("record does not have CoordinateSystem.HorizontalAxisUnitID: "
                    + RecordIdNormalizer.normalizeRecordID(FROM_CRS_ID), e.getMessage());
        }
    }

    @Test
    public void convertTrajectoryResolvesUnitXyWhenHorizontalAxisUnitIdIsPresent() {
        Map<String, Object> coordinateSystem = new HashMap<>();
        coordinateSystem.put("HorizontalAxisUnitID", HORIZONTAL_AXIS_UNIT_ID);
        Map<String, Object> data = new HashMap<>();
        data.put("CoordinateSystem", coordinateSystem);
        data.put("PersistableReference", PERSISTABLE_REFERENCE);
        Record record = mock(Record.class);
        when(record.getData()).thenReturn(data);
        when(storageClient.getRecord(anyString())).thenReturn(record);
        ConvertTrajectoryResponse expected = new ConvertTrajectoryResponse();
        when(crsTrajectoryConverter.convertTrajectory(any(), any())).thenReturn(expected);

        ConvertTrajectoryRequest request = trajectoryRequest();
        ConvertTrajectoryResponse response =
                crsConverterApiV3.convertTrajectory(new LinkedMultiValueMap<>(), request);

        assertSame(expected, response);
        assertEquals(PERSISTABLE_REFERENCE, request.getUnitXY());
    }

    private Record recordWithCoordinateSystem(Map<String, Object> coordinateSystem) {
        Map<String, Object> data = new HashMap<>();
        data.put("CoordinateSystem", coordinateSystem);
        Record record = mock(Record.class);
        when(record.getData()).thenReturn(data);
        return record;
    }

    private ConvertPointsRequest request() {
        ConvertPointsRequest request = new ConvertPointsRequest();
        request.setFromCRS(FROM_CRS_ID);
        request.setToCRS(TO_CRS_ID);
        request.setPoints(Collections.emptyList());
        return request;
    }

    // unitXY is left unset so convertTrajectory resolves it from the Projected trajectory CRS record
    private ConvertTrajectoryRequest trajectoryRequest() {
        ConvertTrajectoryRequest request = new ConvertTrajectoryRequest();
        request.setTrajectoryCRS(FROM_CRS_ID);
        request.setUnitZ(PERSISTABLE_REFERENCE);
        return request;
    }
}
