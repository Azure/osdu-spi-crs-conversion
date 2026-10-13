package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.GeoJson.GeoJsonBase;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.util.ConstantsTests;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConvertGeoJsonRequestTest {

    @Test
    public void testConvertGeoJsonRequestTest() {
        ConvertGeoJsonRequest request = new ConvertGeoJsonRequest();
        assertNull(request.getFeatureCollection());
        assertNull(request.getToCRS());

        GeoJsonFeatureCollection fc = new GeoJsonFeatureCollection();
        fc.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
        request.setFeatureCollection(fc);
        assertEquals(fc, request.getFeatureCollection());

        request.setToCRS(ConstantsTests.EB_NAD83_UTM11N_1702[ConstantsTests.V2]);
        assertEquals(ConstantsTests.EB_NAD83_UTM11N_1702[ConstantsTests.V2], request.getToCRS());
    }
}