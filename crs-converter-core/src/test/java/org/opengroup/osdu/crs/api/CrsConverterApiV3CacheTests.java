package org.opengroup.osdu.crs.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.crs.interfaces.ICRSConverter;
import org.opengroup.osdu.crs.interfaces.IPointConverter;
import org.opengroup.osdu.crs.interfaces.ITrajectoryConverter;
import org.opengroup.osdu.crs.model.ConvertPointsRequest;
import org.opengroup.osdu.crs.model.ConvertPointsResponse;
import org.opengroup.osdu.crs.osducoreserviceclient.storage.IStorageClient;
import org.opengroup.osdu.crs.util.RecordIdNormalizer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CrsConverterApiV3CacheTests {

    // trailing-colon (latest version) form seen in production; normalization strips the trailing colon
    private static final String FROM_CRS_ID = "osdu-dev:reference-data--CoordinateReferenceSystem:Projected:EPSG::32621:";
    private static final String TO_CRS_ID = "osdu-dev:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4326:";
    private static final String PERSISTABLE_REFERENCE = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"}}";

    @Mock
    private ICRSConverter crsConverter;

    @Mock
    private ITrajectoryConverter crsTrajectoryConverter;

    @Mock
    private IPointConverter pointConverter;

    @Mock
    private IStorageClient storageClient;

    private CrsConverterApiV3 crsConverterApiV3;

    @BeforeEach
    public void setUp() {
        crsConverterApiV3 = new CrsConverterApiV3(crsConverter, crsTrajectoryConverter, pointConverter);
        // field is injected by Spring in production; name matches the declaration in CrsConverterApiV3
        ReflectionTestUtils.setField(crsConverterApiV3, "StorageClient", storageClient);
        when(pointConverter.mergeXYCoordinates(any())).thenReturn(new double[0]);
        when(pointConverter.mergeZCoordinates(any())).thenReturn(new double[0]);
        when(pointConverter.convertValuesToPoints(any(), any())).thenReturn(Collections.emptyList());
        when(crsConverter.convertPoint(anyString(), anyString(), any(), any())).thenReturn(new ConvertPointsResponse());
    }

    @Test
    public void convertPointReadsEachCrsRecordFromStorageOnlyOnceAcrossRepeatedCalls() {
        Map<String, Object> data = new HashMap<>();
        data.put("PersistableReference", PERSISTABLE_REFERENCE);
        Record record = mock(Record.class);
        when(record.getData()).thenReturn(data);
        when(storageClient.getRecord(anyString())).thenReturn(record);

        crsConverterApiV3.convertPoint(request(FROM_CRS_ID, TO_CRS_ID));
        crsConverterApiV3.convertPoint(request(FROM_CRS_ID, TO_CRS_ID));

        verify(storageClient, times(1)).getRecord(RecordIdNormalizer.normalizeRecordID(FROM_CRS_ID));
        verify(storageClient, times(1)).getRecord(RecordIdNormalizer.normalizeRecordID(TO_CRS_ID));
        verifyNoMoreInteractions(storageClient);
        verify(crsConverter, times(2)).convertPoint(eq(PERSISTABLE_REFERENCE), eq(PERSISTABLE_REFERENCE), any(), any());
    }

    @Test
    public void convertPointDoesNotCallStorageForInlinePersistableReference() {
        crsConverterApiV3.convertPoint(request(PERSISTABLE_REFERENCE, PERSISTABLE_REFERENCE));

        verify(storageClient, never()).getRecord(anyString());
        verify(crsConverter).convertPoint(eq(PERSISTABLE_REFERENCE), eq(PERSISTABLE_REFERENCE), any(), any());
    }

    private ConvertPointsRequest request(String fromCrs, String toCrs) {
        ConvertPointsRequest request = new ConvertPointsRequest();
        request.setFromCRS(fromCrs);
        request.setToCRS(toCrs);
        request.setPoints(Collections.emptyList());
        return request;
    }
}
