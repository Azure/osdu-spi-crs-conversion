package org.opengroup.osdu.crs.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.crs.BinGrid.AbstractAnyCrsFeatureCollection;
import org.opengroup.osdu.crs.BinGrid.AbstractFeature;
import org.opengroup.osdu.crs.converter.CRSConverter;
import org.opengroup.osdu.crs.converter.PointConverter;
import org.opengroup.osdu.crs.converter.TrajectoryConverter;
import org.opengroup.osdu.crs.model.ConvertBinGridRequest;
import org.opengroup.osdu.crs.model.ConvertBinGridResponse;
import org.opengroup.osdu.crs.osducoreserviceclient.storage.IStorageClient;
import org.opengroup.osdu.crs.util.ConstantsTests;
import org.opengroup.osdu.crs.util.RecordIdNormalizer;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CrsConverterApiV3BinGridBehaviorTests {
    private static final String CRS_ID = "test:reference-data--CoordinateReferenceSystem:Projected:EPSG::32611";
    private CrsConverterApiV3 api;
    private IStorageClient storage;
    private MockHttpServletRequest httpRequest;

    @BeforeEach
    void setUp() {
        storage = mock(IStorageClient.class);
        CRSConverter converter = new CRSConverter();
        ReflectionTestUtils.setField(converter, "logger", mock(JaxRsDpsLog.class));
        api = new CrsConverterApiV3(converter, new TrajectoryConverter(), new PointConverter());
        ReflectionTestUtils.setField(api, "StorageClient", storage);
        ReflectionTestUtils.setField(api, "logger", mock(JaxRsDpsLog.class));
        httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("data-partition-id", "test");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void squareGridRetainsCoordinatesAndProducesIndependentWgs84Corners(boolean convertCrs) throws Exception {
        ConvertBinGridRequest request = request();
        if (convertCrs) {
            request.setToCRS(CRS_ID);
            request.getInBinGrid().setBinGridDefinitionMethodTypeID("existing-definition");
        }
        Record record = mock(Record.class);
        when(record.getData()).thenReturn(Map.of(
                "PersistableReference", ConstantsTests.LB_WGS84_UTM11N[ConstantsTests.V1]));
        when(storage.getRecord(RecordIdNormalizer.normalizeRecordID(CRS_ID))).thenReturn(record);

        ConvertBinGridResponse response = api.convertBinGrid(request, httpRequest);

        assertSame(request.getInBinGrid(), response.getOutBinGrid());
        assertEquals(0.0, response.getMaxMisLocation().getDI(), 1e-6);
        assertEquals(0.0, response.getMaxMisLocation().getDJ(), 1e-6);
        assertEquals(500000.0, response.getOutBinGrid().getP6BinGridOriginEasting(), 1e-6);
        assertEquals(4000000.0, response.getOutBinGrid().getP6BinGridOriginNorthing(), 1e-6);
        assertEquals(convertCrs ? "existing-definition" : "test:reference-data--BinGridDefinitionMethodType:4Corner:",
                response.getOutBinGrid().getBinGridDefinitionMethodTypeID());
        AbstractAnyCrsFeatureCollection source = response.getOutBinGrid()
                .getABCDBinGridSpatialLocation().getAsIngestedcoordinates();
        assertEquals(500000.0, source.getFeatures().get(0).getGeometry().getCoordinates().get(0), 1e-5);
        AbstractFeature wgs84 = response.getOutBinGrid().getABCDBinGridSpatialLocation()
                .getWgs84Coordinates().getFeatures().get(0);
        assertEquals("Feature", wgs84.getType());
        assertEquals("Point", wgs84.getGeometry().getType());
        assertEquals(-117.0, wgs84.getGeometry().getCoordinates().get(0), 1e-6);
        assertTrue(wgs84.getGeometry().getCoordinates().get(1) > 36);
        assertNotSame(source.getFeatures().get(0), wgs84);
        assertFalse(response.getAppliedOperations().isEmpty());
        verify(storage).getRecord(RecordIdNormalizer.normalizeRecordID(CRS_ID));
    }

    private ConvertBinGridRequest request() throws Exception {
        String json = """
                {
                  "inBinGrid": {
                    "P6ScaleFactorOfBinGrid": 1,
                    "P6BinNodeIncrementOnIaxis": 1,
                    "P6BinNodeIncrementOnJaxis": 1,
                    "ABCDBinGridSpatialLocation": {
                      "AsIngestedCoordinates": {
                        "type": "AnyCrsFeatureCollection",
                        "features": [
                          {"type":"AnyCrsFeature","properties":{"PointProperties":[{"Inline":1,"Crossline":1000}]},"geometry":{"type":"AnyCrsPoint","coordinates":[500000,4000000]}},
                          {"type":"AnyCrsFeature","properties":{"PointProperties":[{"Inline":1,"Crossline":2000}]},"geometry":{"type":"AnyCrsPoint","coordinates":[500000,4000100]}},
                          {"type":"AnyCrsFeature","properties":{"PointProperties":[{"Inline":101,"Crossline":1000}]},"geometry":{"type":"AnyCrsPoint","coordinates":[500100,4000000]}},
                          {"type":"AnyCrsFeature","properties":{"PointProperties":[{"Inline":101,"Crossline":2000}]},"geometry":{"type":"AnyCrsPoint","coordinates":[500100,4000100]}}
                        ]
                      }
                    }
                  }
                }
                """;
        ConvertBinGridRequest request = new ObjectMapper().readValue(json, ConvertBinGridRequest.class);
        AbstractAnyCrsFeatureCollection coordinates = request.getInBinGrid().getABCDBinGridSpatialLocation()
                .getAsIngestedcoordinates();
        coordinates.setCoordinateReferenceSystemID(CRS_ID);
        coordinates.setFeatures(new ArrayList<>(coordinates.getFeatures()));
        return request;
    }
}
