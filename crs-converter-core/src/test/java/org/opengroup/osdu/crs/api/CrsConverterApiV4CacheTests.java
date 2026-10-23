package org.opengroup.osdu.crs.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.storage.MultiRecordInfo;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.crs.interfaces.ICRSConverter;
import org.opengroup.osdu.crs.interfaces.IPointConverter;
import org.opengroup.osdu.crs.interfaces.ITrajectoryConverter;
import org.opengroup.osdu.crs.osducoreserviceclient.storage.IStorageClient;
import org.opengroup.osdu.crs.util.RecordIdNormalizer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CrsConverterApiV4CacheTests {

    // trailing-colon (latest version) form seen in production; normalization strips the trailing colon
    private static final String CRS_ID = "osdu-dev:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4326:";
    private static final String UOM_ID = "osdu-dev:reference-data--UnitOfMeasure:ft%5BUS%5D:";
    private static final String PERSISTABLE_REFERENCE = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"}}";

    @Mock
    private ITrajectoryConverter trajectoryConverter;

    @Mock
    private IPointConverter pointConverter;

    @Mock
    private ICRSConverter crsConverter;

    @Mock
    private IStorageClient storageClient;

    private CrsConverterApiV4 crsConverterApiV4;

    @BeforeEach
    public void setUp() {
        crsConverterApiV4 = new CrsConverterApiV4(trajectoryConverter, pointConverter, crsConverter);
        ReflectionTestUtils.setField(crsConverterApiV4, "storageClient", storageClient);
    }

    @Test
    public void getPersistableReferenceFromIDReadsStorageOnlyOnceForRepeatedRecordId() {
        Record record = recordWithPersistableReference();
        when(storageClient.getRecord(anyString())).thenReturn(record);

        String first = crsConverterApiV4.getPersistableReferenceFromID(CRS_ID, false);
        String second = crsConverterApiV4.getPersistableReferenceFromID(CRS_ID, false);

        assertEquals(PERSISTABLE_REFERENCE, first);
        assertEquals(PERSISTABLE_REFERENCE, second);
        verify(storageClient, times(1)).getRecord(RecordIdNormalizer.normalizeRecordID(CRS_ID));
        verifyNoMoreInteractions(storageClient);
    }

    @Test
    public void getPersistableReferenceFromIDCachesUnitOfMeasureFallbackResult() {
        when(storageClient.getRecord(anyString())).thenThrow(new RuntimeException("storage lookup failed"));
        Record record = recordWithPersistableReference();
        MultiRecordInfo multiRecordInfo = new MultiRecordInfo();
        multiRecordInfo.setRecords(Collections.singletonList(record));
        when(storageClient.getRecords(anyCollection())).thenReturn(multiRecordInfo);

        String first = crsConverterApiV4.getPersistableReferenceFromID(UOM_ID, true);
        String second = crsConverterApiV4.getPersistableReferenceFromID(UOM_ID, true);

        assertEquals(PERSISTABLE_REFERENCE, first);
        assertEquals(PERSISTABLE_REFERENCE, second);
        verify(storageClient, times(1)).getRecord(RecordIdNormalizer.normalizeRecordID(UOM_ID));
        verify(storageClient, times(1)).getRecords(anyCollection());
        verifyNoMoreInteractions(storageClient);
    }

    @Test
    public void getPersistableReferenceFromIDDoesNotCallStorageForInlinePersistableReference() {
        String result = crsConverterApiV4.getPersistableReferenceFromID(PERSISTABLE_REFERENCE, false);

        assertEquals(PERSISTABLE_REFERENCE, result);
        verify(storageClient, never()).getRecord(anyString());
        verify(storageClient, never()).getRecords(anyCollection());
    }

    private Record recordWithPersistableReference() {
        Map<String, Object> data = new HashMap<>();
        data.put("PersistableReference", PERSISTABLE_REFERENCE);
        Record record = mock(Record.class);
        when(record.getData()).thenReturn(data);
        return record;
    }
}
