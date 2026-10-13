package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.GeoJson.GeoJsonBase;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ConvertGeoJsonResponseTest {

    @Test
    public void testConvertGeoJsonResponseTest() {
        ConvertGeoJsonResponse response = new ConvertGeoJsonResponse();
        assertNull(response.getFeatureCollection());
        assertNull(response.getTotalCount());
        assertNull(response.getSuccessCount());
        assertNull(response.getOperationsApplied());

        GeoJsonFeatureCollection fc = new GeoJsonFeatureCollection();
        fc.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
        response.setFeatureCollection(fc);
        assertEquals(fc, response.getFeatureCollection());

        List<String> ops = Arrays.asList("something", "done");
        response.setOperationsApplied(ops);
        assertEquals(ops, response.getOperationsApplied());

        Integer c = 10;
        response.setSuccessCount(c);
        assertEquals(c, response.getSuccessCount());

        c = 20;
        response.setTotalCount(c);
        assertEquals(c, response.getTotalCount());
    }
}