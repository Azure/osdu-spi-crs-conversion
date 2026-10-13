package org.opengroup.osdu.crs.GeoJson;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class GeoJsonCoordinatesTests {

    static GeoJsonFeatureCollection makeAFeatureCollection(){
        GeoJsonFeatureCollection fc = new GeoJsonFeatureCollection();
        fc.setType("FeatureCollection");
        return fc;
    }

    private double[] makePoint(int idx, int dim) {
        double[] pt = new double[dim];
        for (int i = 0; i < dim; i++) {
            pt[i] = (i + 1) * idx;
        }
        return pt;
    }

    private double[][] makePoints(int idx, int length, int dim) {
        double[][] pts = new double[length][dim];
        for (int l = 0; l < length; l++) {
            for (int i = 0; i < dim; i++) {
                pts[l][i] = (i + 1) * idx;
            }
            idx += 1;
        }
        return pts;
    }

    private double[][][] makeListOfPoints(int idx, int listLength, int length, int dim) {
        double[][][] pts = new double[listLength][length][dim];
        for (int k = 0; k < listLength; k++) {
            for (int j = 0; j < length; j++) {
                for (int i = 0; i < dim; i++) {
                    pts[k][j][i] = (i + 1) * idx;
                }
                idx += 1;
            }
        }
        return pts;
    }

    private double[][][][] makeListOfPoints(int idx, int listOfLists, int listLength, int length, int dim) {
        double[][][][] pts = new double[listOfLists][listLength][length][dim];
        for (int n = 0; n < listOfLists; n++) {
            for (int k = 0; k < listLength; k++) {
                for (int j = 0; j < length; j++) {
                    for (int i = 0; i < dim; i++) {
                        pts[n][k][j][i] = (i + 1) * idx;
                    }
                    idx += 1;
                }
            }
        }
        return pts;
    }

    private int comparePoint(double[] pt, GeoJsonCoordinates gc, int c, double fac) {
        for (int i = 0; i < 2; i++) {
            assertEquals(pt[i] * fac, gc.getXys()[c], 1.0e-10);
            c++;
        }
        if (pt.length > 2) assertEquals(pt[2] * fac, gc.getZ_s()[(c - 1) / 2], 1.0e-10);
        else assertEquals(0.0, gc.getZ_s()[(c - 1) / 2], 1.0e-10);
        return c;
    }

    private int comparePoints(double[][] pts, GeoJsonCoordinates gc, int c, double fac) {
        for (double[] pt : pts) {
            for (int i = 0; i < 2; i++) {
                assertEquals(pt[i] * fac, gc.getXys()[c], 1.0e-10);
                c++;
            }
            if (pt.length > 2) assertEquals(pt[2] * fac, gc.getZ_s()[(c - 1) / 2], 1.0e-10);
            else assertEquals(0.0, gc.getZ_s()[(c - 1) / 2], 1.0e-10);
        }
        return c;
    }

    private int comparePoints(double[][][] list, GeoJsonCoordinates gc, int c, double fac) {
        for (double[][] pts : list) {
            for (double[] pt : pts) {
                for (int i = 0; i < 2; i++) {
                    assertEquals(pt[i] * fac, gc.getXys()[c], 1.0e-10);
                    c++;
                }
                if (pt.length > 2) assertEquals(pt[2] * fac, gc.getZ_s()[(c - 1) / 2], 1.0e-10);
                else assertEquals(0.0, gc.getZ_s()[(c - 1) / 2], 1.0e-10);
            }
        }
        return c;
    }

    private int comparePoints(double[][][][] listOfList, GeoJsonCoordinates gc, int c, double fac) {
        for (double[][][] list : listOfList) {
            for (double[][] pts : list) {
                for (double[] pt : pts) {
                    for (int i = 0; i < 2; i++) {
                        assertEquals(pt[i] * fac, gc.getXys()[c], 1.0e-10);
                        c++;
                    }
                    if (pt.length > 2) assertEquals(pt[2] * fac, gc.getZ_s()[(c - 1) / 2], 1.0e-10);
                    else assertEquals(0.0, gc.getZ_s()[(c - 1) / 2], 1.0e-10);
                }
            }
        }
        return c;
    }

    private int comparePoints(ArrayList<GeoJsonBase> geom, GeoJsonCoordinates gc, int c, double fac) {
        for (GeoJsonBase g : geom)
            if (g instanceof GeoJsonPoint) {
                c = comparePoint(((GeoJsonPoint) g).getCoordinates(), gc, c, fac);
            } else if (g instanceof GeoJsonMultiPoint) {
                c = comparePoints(((GeoJsonMultiPoint) g).getCoordinates(), gc, c, fac);
            } else if (g instanceof GeoJsonLineString) {
                c = comparePoints(((GeoJsonLineString) g).getCoordinates(), gc, c, fac);
            } else if (g instanceof GeoJsonMultiLineString) {
                c = comparePoints(((GeoJsonMultiLineString) g).getCoordinates(), gc, c, fac);
            } else if (g instanceof GeoJsonPolygon) {
                c = comparePoints(((GeoJsonPolygon) g).getCoordinates(), gc, c, fac);
            } else if (g instanceof GeoJsonMultiPolygon) {
                c = comparePoints(((GeoJsonMultiPolygon) g).getCoordinates(), gc, c, fac);
            } else if (g instanceof GeoJsonGeometryCollection) {
                GeoJsonGeometryCollection gcc = ((GeoJsonGeometryCollection) g);
                ArrayList<GeoJsonBase> gs = new ArrayList<>(Arrays.asList(gcc.getGeometries()));
                c = comparePoints(gs, gc, c, fac);
            } else if (g instanceof GeoJsonFeature) {
                ArrayList<GeoJsonBase> gs = new ArrayList<>();
                GeoJsonFeature f = (GeoJsonFeature) g;
                gs.add(f.getGeometry());
                c = comparePoints(gs, gc, c, fac);
            }
        return c;
    }

    private boolean makeIt(String what, String type) {
        return what.equals("GeoJsonGeometryCollection") || what.equals(type);
    }

    private ArrayList<GeoJsonBase> testGetGeoJsonBaseGeometries(int dim, int idx, String what) {
        GeoJsonPoint gj1;
        double[] pt1;
        GeoJsonMultiPoint gj2;
        double[][] pt2;
        GeoJsonLineString gj3;
        GeoJsonMultiLineString gj4;
        double[][][] pt3;
        GeoJsonPolygon gj5;
        GeoJsonMultiPolygon gj6;
        double[][][][] pt4;
        ArrayList<GeoJsonBase> geo = new ArrayList<>();

        if (makeIt(what, "GeoJsonPoint")) {
            gj1 = new GeoJsonPoint();
            pt1 = makePoint(1, dim);
            gj1.setCoordinates(pt1);
            geo.add(gj1);
            idx += gj1.getLength();
        }

        if (makeIt(what, "GeoJsonMultiPoint")) {
            gj2 = new GeoJsonMultiPoint();
            pt2 = makePoints(idx, 5, dim);
            gj2.setCoordinates(pt2);
            geo.add(gj2);
            idx += gj2.getLength();
        }

        if (makeIt(what, "GeoJsonLineString")) {
            gj3 = new GeoJsonLineString();
            pt2 = makePoints(idx, 4, dim);
            gj3.setCoordinates(pt2);
            geo.add(gj3);
            idx += gj3.getLength();
        }

        if (makeIt(what, "GeoJsonMultiLineString")) {
            gj4 = new GeoJsonMultiLineString();
            pt3 = makeListOfPoints(idx, 2, 5, dim);
            gj4.setCoordinates(pt3);
            geo.add(gj4);
            idx += gj4.getLength();
        }

        if (makeIt(what, "GeoJsonPolygon")) {
            gj5 = new GeoJsonPolygon();
            pt3 = makeListOfPoints(idx, 2, 5, dim);
            gj5.setCoordinates(pt3);
            geo.add(gj5);
            idx += gj5.getLength();
        }

        if (makeIt(what, "GeoJsonMultiPolygon")) {
            gj6 = new GeoJsonMultiPolygon();
            pt4 = makeListOfPoints(idx, 4, 3, 5, dim);
            gj6.setCoordinates(pt4);
            geo.add(gj6);
            idx += gj6.getLength();
        }
        return geo;
    }

    private void dummyConvertArray(double[] cs, double factor) {
        for (int i = 0; i < cs.length; i++) cs[i] = cs[i] * factor;
    }

    private void dummyConverter(GeoJsonCoordinates gc, double factor) {
        dummyConvertArray(gc.getXys(), factor);
        dummyConvertArray(gc.getZ_s(), factor);
        gc.setIndex(0);
    }

    @Test
    public void testGeoJsonPoint() {
        GeoJsonPoint gj = new GeoJsonPoint();
        double[] pt = makePoint(1, 2);
        gj.setCoordinates(pt);
        GeoJsonCoordinates gc = gj.extractCoordinates();
        comparePoint(pt, gc, 0, 1.0);
        pt = makePoint(1, 3);
        gj.setCoordinates(pt);
        gc = gj.extractCoordinates();
        comparePoint(pt, gc, 0, 1.0);

        pt = makePoint(1, 3);
        dummyConverter(gc, 2.0);
        gj.replaceCoordinates(gc);
        gc = gj.extractCoordinates();
        assertEquals(gj.getLength(), gc.getLength());
        comparePoint(pt, gc, 0, 2.0);

        assertEquals(GeoJsonBase.GeoJsonVariant.ANY_CRS_GEO_JSON, gj.getGeoJsonVariant());
        gj.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
        assertEquals(GeoJsonBase.GeoJsonVariant.GEO_JSON, gj.getGeoJsonVariant());
        gj.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.ANY_CRS_GEO_JSON);
        assertEquals(GeoJsonBase.GeoJsonVariant.ANY_CRS_GEO_JSON, gj.getGeoJsonVariant());
    }

    @Test
    public void testGeoJsonMultiPoint() {
        GeoJsonMultiPoint gj = new GeoJsonMultiPoint();
        double[][] pt = makePoints(1, 10, 2);
        gj.setCoordinates(pt);
        GeoJsonCoordinates gc = gj.extractCoordinates();
        comparePoints(pt, gc, 0, 1.0);
        pt = makePoints(1, 11, 3);
        gj.setCoordinates(pt);
        gc = gj.extractCoordinates();
        comparePoints(pt, gc, 0, 1.0);

        pt = makePoints(1, 11, 3); //create a copy
        dummyConverter(gc, 2.0);
        gj.replaceCoordinates(gc);
        gc = gj.extractCoordinates();
        comparePoints(pt, gc, 0, 2.0);

        assertEquals(GeoJsonBase.GeoJsonVariant.ANY_CRS_GEO_JSON, gj.getGeoJsonVariant());
        gj.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
        assertEquals(GeoJsonBase.GeoJsonVariant.GEO_JSON, gj.getGeoJsonVariant());
    }

    @Test
    public void testGeoJsonLineString() {
        GeoJsonLineString gj = new GeoJsonLineString();
        double[][] pt = makePoints(1, 10, 2);
        gj.setCoordinates(pt);
        GeoJsonCoordinates gc = gj.extractCoordinates();
        comparePoints(pt, gc, 0, 1.0);
        pt = makePoints(1, 11, 3);
        gj.setCoordinates(pt);
        gc = gj.extractCoordinates();
        comparePoints(pt, gc, 0, 1.0);

        pt = makePoints(1, 11, 3);
        dummyConverter(gc, 2.0);
        gj.replaceCoordinates(gc);
        gc = gj.extractCoordinates();
        comparePoints(pt, gc, 0, 2.0);

        assertEquals(GeoJsonBase.GeoJsonVariant.ANY_CRS_GEO_JSON, gj.getGeoJsonVariant());
        gj.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
        assertEquals(GeoJsonBase.GeoJsonVariant.GEO_JSON, gj.getGeoJsonVariant());
    }

    @Test
    public void testGeoJsonMultiLineString() {
        GeoJsonMultiLineString gj = new GeoJsonMultiLineString();
        double[][][] pt = makeListOfPoints(1, 3, 10, 2);
        gj.setCoordinates(pt);
        GeoJsonCoordinates gc = gj.extractCoordinates();
        comparePoints(pt, gc, 0, 1.0);
        pt = makeListOfPoints(1, 2, 11, 3);
        gj.setCoordinates(pt);
        gc = gj.extractCoordinates();
        comparePoints(pt, gc, 0, 1.0);

        pt = makeListOfPoints(1, 2, 11, 3); // create copy
        dummyConverter(gc, 2.0);
        gj.replaceCoordinates(gc);
        gc = gj.extractCoordinates();
        comparePoints(pt, gc, 0, 2.0);

        assertEquals(GeoJsonBase.GeoJsonVariant.ANY_CRS_GEO_JSON, gj.getGeoJsonVariant());
        gj.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
        assertEquals(GeoJsonBase.GeoJsonVariant.GEO_JSON, gj.getGeoJsonVariant());
    }

    @Test
    public void testGeoJsonPolygon() {
        GeoJsonPolygon gj = new GeoJsonPolygon();
        double[][][] pt = makeListOfPoints(1, 3, 10, 2);
        gj.setCoordinates(pt.clone());
        GeoJsonCoordinates gc = gj.extractCoordinates();
        comparePoints(pt, gc, 0, 1.0);
        pt = makeListOfPoints(1, 2, 11, 3);
        gj.setCoordinates(pt.clone());
        gc = gj.extractCoordinates();
        comparePoints(pt, gc, 0, 1.0);

        pt = makeListOfPoints(1, 2, 11, 3); // create copy
        dummyConverter(gc, 2.0);
        gj.replaceCoordinates(gc);
        gc = gj.extractCoordinates();
        comparePoints(pt, gc, 0, 2.0);

        assertEquals(GeoJsonBase.GeoJsonVariant.ANY_CRS_GEO_JSON, gj.getGeoJsonVariant());
        gj.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
        assertEquals(GeoJsonBase.GeoJsonVariant.GEO_JSON, gj.getGeoJsonVariant());
    }

    @Test
    public void testGeoJsonMultiPolygon() {
        GeoJsonMultiPolygon gj = new GeoJsonMultiPolygon();
        double[][][][] pt = makeListOfPoints(1, 4, 3, 5, 2);
        gj.setCoordinates(pt.clone());
        GeoJsonCoordinates gc = gj.extractCoordinates();
        comparePoints(pt, gc, 0, 1.0);
        pt = makeListOfPoints(1, 3, 2, 4, 3);
        gj.setCoordinates(pt.clone());
        gc = gj.extractCoordinates();
        comparePoints(pt, gc, 0, 1.0);

        pt = makeListOfPoints(1, 3, 2, 4, 3); // create copy
        dummyConverter(gc, 2.0);
        gj.replaceCoordinates(gc);
        gc = gj.extractCoordinates();
        comparePoints(pt, gc, 0, 2.0);

        assertEquals(GeoJsonBase.GeoJsonVariant.ANY_CRS_GEO_JSON, gj.getGeoJsonVariant());
        gj.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
        assertEquals(GeoJsonBase.GeoJsonVariant.GEO_JSON, gj.getGeoJsonVariant());
    }


    @Test
    public void testGeoJsonGeometryCollection() {
        GeoJsonGeometryCollection gj;

        for (int dim = 2; dim < 4; dim++) {
            int idx = 1;
            gj = new GeoJsonGeometryCollection();
            ArrayList<GeoJsonBase> geo = testGetGeoJsonBaseGeometries(dim, idx, "GeoJsonGeometryCollection");

            gj.setGeometries(geo.toArray(new GeoJsonBase[0]).clone());

            GeoJsonCoordinates gc = gj.extractCoordinates();
            comparePoints(geo, gc, 0, 1.0);

            geo = testGetGeoJsonBaseGeometries(dim, idx, "GeoJsonGeometryCollection"); // copy
            dummyConverter(gc, 2.0);
            gj.replaceCoordinates(gc);
            gc = gj.extractCoordinates();
            comparePoints(geo, gc, 0, 2.0);

            assertEquals(GeoJsonBase.GeoJsonVariant.ANY_CRS_GEO_JSON, gj.getGeoJsonVariant());
            gj.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
            assertEquals(GeoJsonBase.GeoJsonVariant.GEO_JSON, gj.getGeoJsonVariant());
        }
    }


    @Test
    public void testGeoJsonFeature() {
        GeoJsonGeometryCollection gc;
        GeoJsonFeature gj;
        for (int dim = 2; dim < 4; dim++) {
            int idx = 1;
            gj = new GeoJsonFeature();
            gc = new GeoJsonGeometryCollection();
            ArrayList<GeoJsonBase> geos = testGetGeoJsonBaseGeometries(dim, idx, "GeoJsonGeometryCollection");

            gc.setGeometries(geos.toArray(new GeoJsonBase[0]).clone());
            gj.setGeometry(gc);
            GeoJsonCoordinates coordinates = gj.extractCoordinates();
            int c = 0;
            comparePoints(geos, coordinates, c, 1.0);

            geos = testGetGeoJsonBaseGeometries(dim, idx, "GeoJsonGeometryCollection");
            dummyConverter(coordinates, 2.0);
            gj.replaceCoordinates(coordinates);
            coordinates = gj.extractCoordinates();
            comparePoints(geos, coordinates, 0, 2.0);

            assertEquals(GeoJsonBase.GeoJsonVariant.ANY_CRS_GEO_JSON, gj.getGeoJsonVariant());
            gj.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
            assertEquals(GeoJsonBase.GeoJsonVariant.GEO_JSON, gj.getGeoJsonVariant());
        }
    }

    @Test
    public void testGeoJsonFeatureCollection() {
        GeoJsonGeometryCollection gc;
        GeoJsonFeatureCollection gj;
        String[] types = {"GeoJsonMultiPolygon", "GeoJsonPolygon", "GeoJsonMultiLineString", "GeoJsonLineString", "GeoJsonPoint", "GeoJsonMultiPoint", "GeoJsonGeometryCollection"};
        for (int dim = 2; dim < 4; dim++) {
            int idx = 1;
            ArrayList<GeoJsonBase> allGeoJson = new ArrayList<>();
            ArrayList<GeoJsonBase> allGeoJsonBackup = new ArrayList<>();
            gj = new GeoJsonFeatureCollection();
            ArrayList<GeoJsonFeature> features = new ArrayList<>();
            for (String what : types) {
                ArrayList<GeoJsonBase> gbs = testGetGeoJsonBaseGeometries(dim, idx, what);
                allGeoJsonBackup.addAll(gbs); // copy to compare with later
                gbs = testGetGeoJsonBaseGeometries(dim, idx, what);
                allGeoJson.addAll(gbs);
                GeoJsonFeature f = new GeoJsonFeature();
                if (what.equals("GeoJsonGeometryCollection")) {
                    gc = new GeoJsonGeometryCollection();
                    gc.setGeometries(gbs.toArray(new GeoJsonBase[0]));
                    f.setGeometry(gc);
                } else {
                    f.setGeometry(gbs.get(0));
                }
                features.add(f);
            }
            gj.setFeatures(features.toArray(new GeoJsonFeature[0]));
            GeoJsonCoordinates coordinates = gj.extractCoordinates();
            comparePoints(allGeoJson, coordinates, 0, 1.0);

            dummyConverter(coordinates, 2.0);
            gj.replaceCoordinates(coordinates);
            coordinates = gj.extractCoordinates();
            comparePoints(allGeoJsonBackup, coordinates, 0, 2.0);

            assertEquals(GeoJsonBase.GeoJsonVariant.ANY_CRS_GEO_JSON, gj.getGeoJsonVariant());
            gj.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
            assertEquals(GeoJsonBase.GeoJsonVariant.GEO_JSON, gj.getGeoJsonVariant());
        }
    }
}
