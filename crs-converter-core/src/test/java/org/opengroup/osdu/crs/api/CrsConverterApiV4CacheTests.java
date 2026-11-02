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
import jakarta.validation.ValidationException;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.model.ConvertGeoJsonRequestV4;
import org.opengroup.osdu.crs.model.ConvertGeoJsonResponse;
import org.opengroup.osdu.crs.model.ConvertPointsRequestV4;
import org.opengroup.osdu.crs.model.ConvertPointsResponseV4;
import org.opengroup.osdu.crs.model.Point;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void convertPointV4ForwardsTransformationAndPopulatesConvertedPoints() {
        ConvertPointsRequestV4 request = new ConvertPointsRequestV4();
        request.setFromCRS(PERSISTABLE_REFERENCE);
        request.setToCRS(PERSISTABLE_REFERENCE);
        request.setTransformation("{\"type\":\"ST\"}");
        request.setPoints(Collections.singletonList(new Point(1.0, 2.0, 3.0)));
        double[] xy = {1.0, 2.0};
        double[] z = {3.0};
        when(pointConverter.mergeXYCoordinates(request.getPoints())).thenReturn(xy);
        when(pointConverter.mergeZCoordinates(request.getPoints())).thenReturn(z);
        when(pointConverter.convertValuesToPoints(xy, z)).thenReturn(request.getPoints());
        ConvertPointsResponseV4 expected = new ConvertPointsResponseV4();
        when(crsConverter.convertPointV4(PERSISTABLE_REFERENCE, PERSISTABLE_REFERENCE,
                request.getTransformation(), xy, z)).thenReturn(expected);

        assertSame(expected, crsConverterApiV4.convertPointV4(request));
        assertEquals(request.getPoints(), expected.getPoints());
        verify(crsConverter).convertPointV4(PERSISTABLE_REFERENCE, PERSISTABLE_REFERENCE,
                request.getTransformation(), xy, z);
        verifyNoMoreInteractions(storageClient);
    }

    @Test
    void convertGeoJsonV4ResolvesSourceIdsBeforeConversion() {
        GeoJsonFeatureCollection features = new GeoJsonFeatureCollection();
        features.setCoordinateReferenceSystemID(CRS_ID);
        features.setVerticalUnitID(UOM_ID);
        features.setPersistableReferenceCrs("superseded-crs");
        features.setPersistableReferenceUnitZ("superseded-unit");
        Record crsRecord = recordWithPersistableReference();
        Record unitRecord = recordWithPersistableReference();
        when(storageClient.getRecord(RecordIdNormalizer.normalizeRecordID(CRS_ID))).thenReturn(crsRecord);
        when(storageClient.getRecord(RecordIdNormalizer.normalizeRecordID(UOM_ID))).thenReturn(unitRecord);
        ConvertGeoJsonRequestV4 request = new ConvertGeoJsonRequestV4();
        request.setFeatureCollection(features);
        request.setToCRS(PERSISTABLE_REFERENCE);
        request.setToUnitZ(PERSISTABLE_REFERENCE);
        request.setTransformation(PERSISTABLE_REFERENCE);
        ConvertGeoJsonResponse expected = new ConvertGeoJsonResponse();
        when(crsConverter.convertGeoJsonV4(features, PERSISTABLE_REFERENCE, PERSISTABLE_REFERENCE,
                PERSISTABLE_REFERENCE)).thenReturn(expected);

        assertSame(expected, crsConverterApiV4.convertGeoJsonV4(request));
        assertNull(features.getCoordinateReferenceSystemID());
        assertNull(features.getVerticalUnitID());
        assertEquals(PERSISTABLE_REFERENCE, features.getPersistableReferenceCrs());
        assertEquals(PERSISTABLE_REFERENCE, features.getPersistableReferenceUnitZ());
        verify(crsConverter).convertGeoJsonV4(features, PERSISTABLE_REFERENCE, PERSISTABLE_REFERENCE,
                PERSISTABLE_REFERENCE);
    }

    @Test
    void lookupReportsMissingRecordAndFailedUnitFallback() {
        when(storageClient.getRecord(RecordIdNormalizer.normalizeRecordID(CRS_ID))).thenReturn(null);
        ValidationException missing = assertThrows(ValidationException.class,
                () -> crsConverterApiV4.getPersistableReferenceFromID(CRS_ID, true));
        assertTrue(missing.getMessage().contains("record not found:"));

        when(storageClient.getRecord(RecordIdNormalizer.normalizeRecordID(UOM_ID)))
                .thenThrow(new RuntimeException("lookup failed"));
        MultiRecordInfo empty = new MultiRecordInfo();
        empty.setRecords(Collections.emptyList());
        when(storageClient.getRecords(anyCollection())).thenReturn(empty);
        assertThrows(ValidationException.class,
                () -> crsConverterApiV4.getPersistableReferenceFromID(UOM_ID, true));
    }

    @Test
    void projectedCrsTypeAndHorizontalUnitAreReadFromStorage() {
        Record record = mock(Record.class);
        when(record.getData()).thenReturn(Map.of(
                "Kind", "Projected", "CoordinateSystem", Map.of("HorizontalAxisUnitID", UOM_ID)));
        when(storageClient.getRecord(RecordIdNormalizer.normalizeRecordID(CRS_ID))).thenReturn(record);

        assertTrue(crsConverterApiV4.checkCRSType(CRS_ID));
        assertEquals(UOM_ID, crsConverterApiV4.getUnitFromTrajectoryCRS(CRS_ID));
        assertNull(crsConverterApiV4.getUnitFromTrajectoryCRS(PERSISTABLE_REFERENCE));
        assertFalse(crsConverterApiV4.checkCRSType(PERSISTABLE_REFERENCE));
        assertFalse(crsConverterApiV4.checkCRSType("%invalid"));
    }

    private Record recordWithPersistableReference() {
        Map<String, Object> data = new HashMap<>();
        data.put("PersistableReference", PERSISTABLE_REFERENCE);
        Record record = mock(Record.class);
        when(record.getData()).thenReturn(data);
        return record;
    }
}
