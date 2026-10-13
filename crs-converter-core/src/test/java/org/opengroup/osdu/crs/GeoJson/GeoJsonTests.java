package org.opengroup.osdu.crs.GeoJson;

import org.opengroup.osdu.crs.util.ConstantsTests;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class GeoJsonTests {
    private static final String P01 = "{\"coordinates\": [-92.11569999999999,29.8823,153.4779442519685],\"type\": \"Point\"}";
    private static final String P02 = "{\"coordinates\": [1315694.366039069,458966.7531300551,1223.77],\"type\": \"AnyCrsPoint\"}";
    private static final String MP01 = "{\"coordinates\": [[-92.11569999999999,29.8823,153.4779442519685],[-92.11569999999999,29.88231111,153.4779442519685]],\"type\": \"MultiPoint\"}";
    private static final String MP02 = "{\"coordinates\": [[1315694.366039069,458966.7531300551,1223.77],[1315694.366039069,458966.75313,1223.77]],\"type\": \"AnyCrsMultiPoint\"}";
    private static final String LS1 = "{\"coordinates\": [[-106.7221932468166, 58.62877104866187, 0], [-106.7221932468166, 58.62877104866187, -1000], [-106.7221932468166, 58.628841413497696, -1099.589273524356], [-106.7221932468166, 58.62905077538467, -1196.726328616693], [-106.7221932468166, 58.62939397912509, -1289.0193286012347], [-106.7221932468166, 58.629862573883045, -1374.1957135154555], [-106.7221932468166, 58.63044502127438, -1450.158158078553], [-106.7221932468166, 58.63112697948231, -1515.0362148004838], [-106.7221932468166, 58.63189165640354, -1567.2323706068285], [-106.7221932468166, 58.632720223128295, -1605.4613829125256], [-106.7221932468166, 58.63359227757328, -1628.7819265597907], [-106.7221932468166, 58.634486346850565, -1636.6197723675814], [-106.7221932468166, 58.64346390592738, -1636.6197723675814], [-106.7221256527866, 58.64436073844057, -1636.6197723675814], [-106.7219232805571, 58.645252041310655, -1636.6197723675814], [-106.72158736772859, 58.64613231873107, -1636.6197723675814], [-106.72111997572594, 58.64699614246926, -1636.6197723675814], [-106.7205239773253, 58.647838185358914, -1636.6197723675814], [-106.71980303916068, 58.64865325417819, -1636.6197723675814], [-106.71896159931258, 58.649436321710716, -1636.6197723675814], [-106.71800484011199, 58.6501825577904, -1636.6197723675814], [-106.71693865632362, 58.650887359137506, -1636.6197723675814], [-106.71576961890075, 58.65154637780062, -1636.6197723675814], [-106.71450493453315, 58.652155548027686, -1636.6197723675814], [-106.71315240123573, 58.65271111139882, -1636.6197723675814], [-106.71172036025125, 58.653209640064325, -1636.6197723675814], [-106.7102176445643, 58.65364805794299, -1636.6197723675814], [-106.70865352434504, 58.65402365974847, -1636.6197723675814], [-106.70703764966196, 58.65433412772482, -1636.6197723675814], [-106.70537999081972, 58.65457754598662, -1636.6197723675814], [-106.70369077669426, 58.65475241237421, -1636.6197723675814], [-106.70198043145001, 58.65485764774955, -1636.6197723675814], [-106.70025951003497, 58.65489260267489, -1636.6197723675814], [-106.68303279650185, 58.65488851565181, -1636.6197723675814]], \"type\": \"LineString\"}";
    private static final String LS2 = "{\"coordinates\": [[-106.7221932468166, 58.62877104866187, 0], [-106.7221932468166, 58.62877104866187, -1000], [-106.7221932468166, 58.628841413497696, -1099.589273524356], [-106.7221932468166, 58.62905077538467, -1196.726328616693], [-106.7221932468166, 58.62939397912509, -1289.0193286012347], [-106.7221932468166, 58.629862573883045, -1374.1957135154555], [-106.7221932468166, 58.63044502127438, -1450.158158078553], [-106.7221932468166, 58.63112697948231, -1515.0362148004838], [-106.7221932468166, 58.63189165640354, -1567.2323706068285], [-106.7221932468166, 58.632720223128295, -1605.4613829125256], [-106.7221932468166, 58.63359227757328, -1628.7819265597907], [-106.7221932468166, 58.634486346850565, -1636.6197723675814], [-106.7221932468166, 58.64346390592738, -1636.6197723675814], [-106.7221256527866, 58.64436073844057, -1636.6197723675814], [-106.7219232805571, 58.645252041310655, -1636.6197723675814], [-106.72158736772859, 58.64613231873107, -1636.6197723675814], [-106.72111997572594, 58.64699614246926, -1636.6197723675814], [-106.7205239773253, 58.647838185358914, -1636.6197723675814], [-106.71980303916068, 58.64865325417819, -1636.6197723675814], [-106.71896159931258, 58.649436321710716, -1636.6197723675814], [-106.71800484011199, 58.6501825577904, -1636.6197723675814], [-106.71693865632362, 58.650887359137506, -1636.6197723675814], [-106.71576961890075, 58.65154637780062, -1636.6197723675814], [-106.71450493453315, 58.652155548027686, -1636.6197723675814], [-106.71315240123573, 58.65271111139882, -1636.6197723675814], [-106.71172036025125, 58.653209640064325, -1636.6197723675814], [-106.7102176445643, 58.65364805794299, -1636.6197723675814], [-106.70865352434504, 58.65402365974847, -1636.6197723675814], [-106.70703764966196, 58.65433412772482, -1636.6197723675814], [-106.70537999081972, 58.65457754598662, -1636.6197723675814], [-106.70369077669426, 58.65475241237421, -1636.6197723675814], [-106.70198043145001, 58.65485764774955, -1636.6197723675814], [-106.70025951003497, 58.65489260267489, -1636.6197723675814], [-106.68303279650185, 58.65488851565181, -1636.6197723675814]], \"type\": \"AnyCrsLineString\"}";
    private static final String MLS1 = "{\"coordinates\": [[[-106.7221932468166, 58.62877104866187, 0], [-106.7221932468166, 58.62877104866187, -1000], [-106.7221932468166, 58.628841413497696, -1099.589273524356], [-106.7221932468166, 58.62905077538467, -1196.726328616693]], [[-106.7221932468166, 58.62939397912509, -1289.0193286012347], [-106.7221932468166, 58.629862573883045, -1374.1957135154555], [-106.7221932468166, 58.63044502127438, -1450.158158078553], [-106.7221932468166, 58.63112697948231, -1515.0362148004838], [-106.7221932468166, 58.63189165640354, -1567.2323706068285], [-106.7221932468166, 58.632720223128295, -1605.4613829125256], [-106.7221932468166, 58.63359227757328, -1628.7819265597907], [-106.7221932468166, 58.634486346850565, -1636.6197723675814], [-106.7221932468166, 58.64346390592738, -1636.6197723675814], [-106.7221256527866, 58.64436073844057, -1636.6197723675814], [-106.7219232805571, 58.645252041310655, -1636.6197723675814], [-106.72158736772859, 58.64613231873107, -1636.6197723675814], [-106.72111997572594, 58.64699614246926, -1636.6197723675814], [-106.7205239773253, 58.647838185358914, -1636.6197723675814], [-106.71980303916068, 58.64865325417819, -1636.6197723675814], [-106.71896159931258, 58.649436321710716, -1636.6197723675814], [-106.71800484011199, 58.6501825577904, -1636.6197723675814], [-106.71693865632362, 58.650887359137506, -1636.6197723675814], [-106.71576961890075, 58.65154637780062, -1636.6197723675814], [-106.71450493453315, 58.652155548027686, -1636.6197723675814], [-106.71315240123573, 58.65271111139882, -1636.6197723675814], [-106.71172036025125, 58.653209640064325, -1636.6197723675814], [-106.7102176445643, 58.65364805794299, -1636.6197723675814], [-106.70865352434504, 58.65402365974847, -1636.6197723675814], [-106.70703764966196, 58.65433412772482, -1636.6197723675814], [-106.70537999081972, 58.65457754598662, -1636.6197723675814], [-106.70369077669426, 58.65475241237421, -1636.6197723675814], [-106.70198043145001, 58.65485764774955, -1636.6197723675814], [-106.70025951003497, 58.65489260267489, -1636.6197723675814], [-106.68303279650185, 58.65488851565181, -1636.6197723675814]]], \"type\": \"MultiLineString\"}";
    private static final String MLS2 = "{\"coordinates\": [[[-106.7221932468166, 58.62877104866187, 0], [-106.7221932468166, 58.62877104866187, -1000], [-106.7221932468166, 58.628841413497696, -1099.589273524356], [-106.7221932468166, 58.62905077538467, -1196.726328616693]], [[-106.7221932468166, 58.62939397912509, -1289.0193286012347], [-106.7221932468166, 58.629862573883045, -1374.1957135154555], [-106.7221932468166, 58.63044502127438, -1450.158158078553], [-106.7221932468166, 58.63112697948231, -1515.0362148004838], [-106.7221932468166, 58.63189165640354, -1567.2323706068285], [-106.7221932468166, 58.632720223128295, -1605.4613829125256], [-106.7221932468166, 58.63359227757328, -1628.7819265597907], [-106.7221932468166, 58.634486346850565, -1636.6197723675814], [-106.7221932468166, 58.64346390592738, -1636.6197723675814], [-106.7221256527866, 58.64436073844057, -1636.6197723675814], [-106.7219232805571, 58.645252041310655, -1636.6197723675814], [-106.72158736772859, 58.64613231873107, -1636.6197723675814], [-106.72111997572594, 58.64699614246926, -1636.6197723675814], [-106.7205239773253, 58.647838185358914, -1636.6197723675814], [-106.71980303916068, 58.64865325417819, -1636.6197723675814], [-106.71896159931258, 58.649436321710716, -1636.6197723675814], [-106.71800484011199, 58.6501825577904, -1636.6197723675814], [-106.71693865632362, 58.650887359137506, -1636.6197723675814], [-106.71576961890075, 58.65154637780062, -1636.6197723675814], [-106.71450493453315, 58.652155548027686, -1636.6197723675814], [-106.71315240123573, 58.65271111139882, -1636.6197723675814], [-106.71172036025125, 58.653209640064325, -1636.6197723675814], [-106.7102176445643, 58.65364805794299, -1636.6197723675814], [-106.70865352434504, 58.65402365974847, -1636.6197723675814], [-106.70703764966196, 58.65433412772482, -1636.6197723675814], [-106.70537999081972, 58.65457754598662, -1636.6197723675814], [-106.70369077669426, 58.65475241237421, -1636.6197723675814], [-106.70198043145001, 58.65485764774955, -1636.6197723675814], [-106.70025951003497, 58.65489260267489, -1636.6197723675814], [-106.68303279650185, 58.65488851565181, -1636.6197723675814]]], \"type\": \"AnyCrsMultiLineString\"}";
    private static final String PL1 = "{\"coordinates\": [[[1.8850016458739598, 61.112087323815295, 0], [1.8792776843136592, 61.27365343694217, 0], [2.4014387140398092, 61.27696068730492, 0], [2.404496350786769, 61.11537266343621, 0], [1.8850016458739598, 61.112087323815295, 0]]], \"type\": \"Polygon\"}";
    private static final String PL2 = "{\"coordinates\": [[[1.8850016458739598, 61.112087323815295, 0], [1.8792776843136592, 61.27365343694217, 0], [2.4014387140398092, 61.27696068730492, 0], [2.404496350786769, 61.11537266343621, 0], [1.8850016458739598, 61.112087323815295, 0]]], \"type\": \"AnyCrsPolygon\"}";
    private static final String MPL1 = "{\"coordinates\": [[[[1.8850016458739598, 61.112087323815295, 0], [1.8792776843136592, 61.27365343694217, 0], [2.4014387140398092, 61.27696068730492, 0], [2.404496350786769, 61.11537266343621, 0], [1.8850016458739598, 61.112087323815295, 0]]]], \"type\": \"MultiPolygon\"}";
    private static final String MPL2 = "{\"coordinates\": [[[[1.8850016458739598, 61.112087323815295, 0], [1.8792776843136592, 61.27365343694217, 0], [2.4014387140398092, 61.27696068730492, 0], [2.404496350786769, 61.11537266343621, 0], [1.8850016458739598, 61.112087323815295, 0]]]], \"type\": \"AnyCrsMultiPolygon\"}";
    private static final String GC1 = "{\"geometries\":[{\"coordinates\":[3.0,55.0],\"type\":\"Point\"},{\"coordinates\":[[[2.0,54.0],[4.0,54.0],[4.0,56.0],[2.0,56.0],[2.0,54.0]]],\"type\":\"Polygon\"}],\"type\":\"GeometryCollection\"}\n";
    private static final String GC2 = "{\"geometries\":[{\"coordinates\":[3.0,55.0],\"type\":\"AnyCrsPoint\"},{\"coordinates\":[[[2.0,54.0],[4.0,54.0],[4.0,56.0],[2.0,56.0],[2.0,54.0]]],\"type\":\"AnyCrsPolygon\"}],\"type\":\"AnyCrsGeometryCollection\"}\n";
    private static final String F1 = "{\"geometry\":{\"coordinates\":[[[2.0,54.0],[4.0,54.0],[4.0,56.0],[2.0,56.0],[2.0,54.0]]],\"type\":\"Polygon\"},\"properties\":\"Name\",\"type\":\"Feature\"}";
    private static final String F2 = "{\"geometry\":{\"coordinates\":[[[2.0,54.0],[4.0,54.0],[4.0,56.0],[2.0,56.0],[2.0,54.0]]],\"type\":\"Polygon\"},\"properties\":\"Name\",\"type\":\"AnyCrsFeature\"}";
    private static final String FC1 = "{\"features\":[{\"geometry\":{\"coordinates\":[[[2.0,54.0],[4.0,54.0],[4.0,56.0],[2.0,56.0],[2.0,54.0]]],\"type\":\"Polygon\"},\"properties\":\"Name\",\"type\":\"Feature\"},{\"geometry\":{\"geometries\":[{\"coordinates\":[3.0,55.0],\"type\":\"Point\"},{\"coordinates\":[[[2.0,54.0],[4.0,54.0],[4.0,56.0],[2.0,56.0],[2.0,54.0]]],\"type\":\"Polygon\"}],\"type\":\"GeometryCollection\"},\"properties\":\"Name:GeomColl\",\"type\":\"Feature\"}],\"type\":\"FeatureCollection\"}\n";
    private static final String FC2 = "{\"features\":[{\"geometry\":{\"coordinates\":[[[2.0,54.0],[4.0,54.0],[4.0,56.0],[2.0,56.0],[2.0,54.0]]],\"type\":\"AnyCrsPolygon\"},\"properties\":\"Name\",\"type\":\"AnyCrsFeature\"},{\"geometry\":{\"geometries\":[{\"coordinates\":[3.0,55.0],\"type\":\"AnyCrsPoint\"},{\"coordinates\":[[[2.0,54.0],[4.0,54.0],[4.0,56.0],[2.0,56.0],[2.0,54.0]]],\"type\":\"AnyCrsPolygon\"}],\"type\":\"AnyCrsGeometryCollection\"},\"properties\":\"Name:GeomColl\",\"type\":\"AnyCrsFeature\"}],\"type\":\"AnyCrsFeatureCollection\",\"persistableReferenceCrs\":\"NoValue\"}\n";
    private static final String PR = "{\"wkt\":\"PROJCS[\\\"WGS_1984_UTM_Zone_13N\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",-105.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",32613]]\",\"ver\":\"PE_10_3_1\",\"name\":\"WGS_1984_UTM_Zone_13N\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32613\"},\"type\":\"LBC\"}";

/*
    @Test
    public void testMakeGeoJsonGeometryCollection() {
        double[][][] line = new double[][][]{{{167438.06, 7391101.72, 59, 0, 635.8, 2597},
                {167425.99, 7391104.87, 59, 1, 636.05, 2598},
                {167413.91, 7391108.03, 59, 2, 636.3, 2599},
                {167401.84, 7391111.18, 59, 3, 636.55, 2600},
                {167389.76, 7391114.33, 59, 4, 636.8, 2601},
                {167377.69, 7391117.49, 59, 5, 637.05, 2602},
                {167365.61, 7391120.64, 59, 6, 637.3, 2603},
                {167353.54, 7391123.79, 59, 7, 637.55, 2604},
                {167341.46, 7391126.95, 59, 8, 637.8, 2605},
                {167329.39, 7391130.1, 59, 9, 638.05, 2606},
                {167317.31, 7391133.25, 59, 10, 638.3, 2607},
                {167305.24, 7391136.4, 59, 11, 638.55, 2608},
                {167293.16, 7391139.56, 59, 12, 638.8, 2609},
                {167281.09, 7391142.71, 59, 13, 639.05, 2610},
                {167269.02, 7391145.86, 59, 14, 639.3, 2611},
                {167256.94, 7391149.02, 59, 15, 639.55, 2612},
                {167244.87, 7391152.17, 59, 16, 639.8, 2613},
                {167232.79, 7391155.32, 59, 17, 640.05, 2614},
                {167220.72, 7391158.48, 59, 18, 640.3, 2615},
                {167208.64, 7391161.63, 59, 19, 640.55, 2616},
                {167196.57, 7391164.78, 59, 20, 640.8, 2617},
                {167184.49, 7391167.94, 59, 21, 641.05, 2618},
                {167172.42, 7391171.09, 59, 22, 641.3, 2619},
                {167160.34, 7391174.24, 59, 23, 641.55, 2620}},{
                {166991.3, 7391218.39, 59, 37, 645.05, 2634},
                {166979.22, 7391221.54, 59, 38, 645.3, 2635},
                {166967.15, 7391224.69, 59, 39, 645.55, 2636},
                {166955.07, 7391227.85, 59, 40, 645.8, 2637},
                {166943, 7391231, 59, 41, 646.05, 2638},
                {166930.97, 7391234.39, 59, 42, 646.3, 2639},
                {166918.93, 7391237.77, 59, 43, 646.55, 2640},
                {166906.89, 7391241.16, 59, 44, 646.8, 2641},
                {166894.86, 7391244.54, 59, 45, 647.05, 2642},
                {166882.83, 7391247.93, 59, 46, 647.3, 2643},
                {166870.79, 7391251.31, 59, 47, 647.55, 2644},
                {166858.76, 7391254.7, 59, 48, 647.8, 2645},
                {166846.72, 7391258.08, 59, 49, 648.05, 2646},
                {166834.69, 7391261.47, 59, 50, 648.3, 2647},
                {166822.65, 7391264.85, 59, 51, 648.55, 2648},
                {166810.62, 7391268.24, 59, 52, 648.8, 2649},
                {166798.58, 7391271.62, 59, 53, 649.05, 2650},
                {166786.55, 7391275.01, 59, 54, 649.3, 2651},
                {166774.51, 7391278.39, 59, 55, 649.55, 2652},
                {166762.48, 7391281.78, 59, 56, 649.8, 2653},
                {166750.44, 7391285.16, 59, 57, 650.05, 2654},
                {166738.41, 7391288.55, 59, 58, 650.3, 2655},
                {166726.37, 7391291.93, 59, 59, 650.55, 2656},
                {166714.34, 7391295.32, 59, 60, 650.8, 2657}}};

        int t=0;
        String s;
        ArrayList<GeoJsonFeature> features = new ArrayList<>();

        for (double[][] part : line) {
            double[][] segment = new double[part.length][3];
            double[] cdp = new double[part.length];
            double[] shot = new double[part.length];
            double[] idx = new double[part.length];
            int p=0;
            for (double[] record : part) {
                for (int i=0; i<3; i++) segment[p][i]=record[i];
                cdp[p] = record[5];
                idx[p] = t; t++;
                shot[p] = record[4];
                p++;
            }
            GeoJsonFeature gf = new GeoJsonFeature();
            GeoJsonLineString gls = new GeoJsonLineString();
            gls.setCoordinates(segment);
            //Seismic2DProperties props = new Seismic2DProperties(idx, cdp, shot);
            //gf.setProperties(props);
            gf.setGeometry(gls);
            features.add(gf);
        }
        GeoJsonFeatureCollection gj_fc = new GeoJsonFeatureCollection();
        gj_fc.setFeatures(features.toArray(new GeoJsonFeature[0]));
        gj_fc.setPersistableReference(ConstantsTests.EB_ED5023032023);
        s = gj_fc.toJsonString();

        GeoJsonPoint g_pt = getGeoJsonPoint(0, 0, 0);
        s = g_pt.toJsonString();

        GeoJsonMultiPoint g_mpt = getGeoJsonMultiPoint(0, 0, 0);
        s = g_mpt.toJsonString();

        GeoJsonLineString g_ls = getGeoJsonLineString(0, 0, 0);
        s = g_ls.toJsonString();

        GeoJsonMultiLineString g_mls = getGeoJsonMultiLineString(0, 0, 0);
        s = g_mls.toJsonString();

        GeoJsonPolygon g_p = getGeoJsonPolygon(0, 0, 0);
        s = g_p.toJsonString();

        GeoJsonMultiPolygon g_mp = getGeoJsonMultiPolygon(0, 0, 0);
        s = g_mp.toJsonString();

        GeoJsonGeometryCollection g_gc = getGeoJsonGeometryCollection(0, 0, 0);

        s = g_gc.toJsonString();

        ArrayList<GeoJsonFeature> fs = new ArrayList<>();
        GeoJsonFeatureCollection fc = new GeoJsonFeatureCollection();
        fc.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);

        GeoJsonFeature f = getGeoJsonFeature(g_pt);
        fs.add(getGeoJsonFeature(g_pt));
        fs.add(getGeoJsonFeature(g_mpt));
        fs.add(getGeoJsonFeature(g_mls));
        fs.add(getGeoJsonFeature(g_ls));
        fs.add(getGeoJsonFeature(g_p));
        fs.add(getGeoJsonFeature(g_mp));
        fs.add(getGeoJsonFeature(g_gc));

        fc.setFeatures(fs.toArray(new GeoJsonFeature[0]));
        // System.out.println(s);
        fc.setFeatures(new GeoJsonFeature[]{getGeoJsonFeature(g_pt)});
    }
*/

    private GeoJsonFeature getGeoJsonFeature(GeoJsonBase gb) {
        GeoJsonFeature f = new GeoJsonFeature();
        f.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
        f.setGeometry(gb);
        return f;
    }

    private GeoJsonGeometryCollection getGeoJsonGeometryCollection(double dx, double dy, double dz) {
        GeoJsonGeometryCollection g_gc = new GeoJsonGeometryCollection();
        g_gc.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
        ArrayList<GeoJsonBase> gs = new ArrayList<>();
        gs.add(getGeoJsonPoint(dx, dx - 5, dz - 100));
        gs.add(getGeoJsonMultiPoint(dx, dy - 5, dx - 100));
        gs.add(getGeoJsonMultiPolygon(dx, dy - 5, dx - 100));
        gs.add(getGeoJsonPolygon(dx, dy - 5, dx - 100));
        gs.add(getGeoJsonLineString(dx, dy - 5, dx - 100));
        gs.add(getGeoJsonMultiLineString(dx, dy - 5, dx - 100));
        g_gc.setGeometries(gs.toArray(new GeoJsonBase[0]));
        return g_gc;
    }

    private GeoJsonMultiPolygon getGeoJsonMultiPolygon(double dx, double dy, double dz) {
        GeoJsonMultiPolygon g_mp = new GeoJsonMultiPolygon();
        g_mp.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
        g_mp.setCoordinates(new double[][][][]{{{{4, 58, 0}, {4, 61, 0}, {7, 60, 0}, {7, 57, 0}, {4, 58, 0}}, {{4.1, 58.1, 0}, {6.9, 57.1, 0}, {6.9, 59.9, 0}, {4.1, 60.9, 0}, {4.1, 58.1, 0}}}, {{{4, 58, 0}, {4, 61, 0}, {7, 60, 0}, {7, 57, 0}, {4, 58, 0}}, {{4.1, 58.1, 0}, {6.9, 57.1, 0}, {6.9, 59.9, 0}, {4.1, 60.9, 0}, {4.1, 58.1, 0}}}});
        int r = 1;
        for (double[][][] poly : g_mp.getCoordinates()) {
            for (double[][] pl : poly) {
                for (double[] pt : pl) {
                    pt[0] = pt[0] + r * 5 + dx;
                    pt[1] = pt[1] + dy;
                    pt[2] = pt[2] + dz;
                }
            }
            r = r * -1;
        }
        return g_mp;
    }

    private GeoJsonPolygon getGeoJsonPolygon(double dx, double dy, double dz) {
        GeoJsonPolygon g_p = new GeoJsonPolygon();
        g_p.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
        g_p.setCoordinates(new double[][][]
                {{{4.0 + dx, 58.0 + dy, 0 + dz}, {4.0 + dx, 61.0 + dy, 0 + dz}, {7.0 + dx, 60.0 + dy, 0 + dz}, {7.0 + dx, 57.0 + dy, 0 + dz}, {4.0 + dx, 58.0 + dy, 0 + dz}},
                        {{4.1 + dx, 58.1 + dy, 0 + dz}, {6.9 + dx, 57.1 + dy, 0 + dz}, {6.9 + dx, 59.9 + dy, 0 + dz}, {4.1 + dx, 60.9 + dy, 0 + dz}, {4.1 + dx, 58.1 + dy, 0 + dz}}});
        return g_p;
    }

    private GeoJsonMultiLineString getGeoJsonMultiLineString(double dx, double dy, double dz) {
        GeoJsonMultiLineString g_mls = new GeoJsonMultiLineString();
        g_mls.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
        g_mls.setCoordinates(new double[][][]{{{6.0 + dx, 60.1 + dy, 4 + dz}, {6.1 + dx, 60.1 + dy, 4 + dz}, {6.2 + dx, 60.1 + dy, 4 + dz}}, {{6.0 + dx, 60.2 + dy, 4 + dz}, {6.1 + dx, 60.2 + dy, 4 + dz}, {6.2 + dx, 60.2 + dy, 4 + dz}}});
        return g_mls;
    }

    private GeoJsonLineString getGeoJsonLineString(double dx, double dy, double dz) {
        GeoJsonLineString g_ls = new GeoJsonLineString();
        g_ls.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
        g_ls.setCoordinates(new double[][]{{6.0 + dx, 60 + dy, 4 + dz}, {6.1 + dx, 60 + dy, 4 + dz}, {6.2 + dx, 60 + dy, 4 + dz}});
        return g_ls;
    }

    private GeoJsonMultiPoint getGeoJsonMultiPoint(double dx, double dy, double dz) {
        GeoJsonMultiPoint g_mpt = new GeoJsonMultiPoint();
        g_mpt.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
        g_mpt.setCoordinates(new double[][]{{6.0 + dx, 59 + dy, 4 + dz}, {6.1 + dx, 59 + dy, 4 + dz}, {6.2 + dx, 59 + dy, 4 + dz}});
        return g_mpt;
    }

    private GeoJsonPoint getGeoJsonPoint(double dx, double dy, double dz) {
        GeoJsonPoint g_pt = new GeoJsonPoint();
        g_pt.setGeoJsonVariant(GeoJsonBase.GeoJsonVariant.GEO_JSON);
        g_pt.setCoordinates(new double[]{5 + dx, 59 + dy, 2 + dz});
        return g_pt;
    }

    @Test
    public void testGeoJsonFeatureCollection() {
        GeoJsonBase gb;
        gb = GeoJsonBase.createInstance(FC1);
        assertNotNull(gb);
        assertEquals("FeatureCollection", gb.getType());
        assertTrue(gb instanceof GeoJsonFeatureCollection);
        GeoJsonFeatureCollection gj = (GeoJsonFeatureCollection) gb;
        assertNotNull(gj);
        assertTrue(gj.getFeatures()[0].getGeometry() instanceof GeoJsonPolygon);
        assertEquals(2, gj.getDimension());
        assertTrue(gj.isValid());
        assertEquals(11, gj.getLength());
        gj.setFeatures(null);
        assertFalse(gj.isValid());
        assertEquals(0, gj.getLength());
        gb = GeoJsonBase.createInstance(FC2);
        assertNotNull(gb);
        assertEquals("AnyCrsFeatureCollection", gb.getType());
        assertTrue(gb instanceof GeoJsonFeatureCollection);
        gj = (GeoJsonFeatureCollection) gb;
        assertTrue(gj.getFeatures()[1].getGeometry() instanceof GeoJsonGeometryCollection);
        assertEquals(2, gj.getDimension());
        assertTrue(gj.isValid());
        assertNull(gj.getBbox());
        gj.setPersistableReferenceCrs(PR);
        gj.setPersistableReferenceUnitZ(ConstantsTests.UNIT_FT);
        gj.setProperties("Whatever");  // empty objects don't compare well
        String s = gb.toJsonString();
        GeoJsonBase other = GeoJsonBase.createInstance(s);
        assertEquals(gj, other);
        GeoJsonFeatureCollection fc = (GeoJsonFeatureCollection) other;
        assertNotNull(fc.getPersistableReferenceCrs());
        assertEquals(PR, ((GeoJsonFeatureCollection) other).getPersistableReferenceCrs());
        assertEquals(ConstantsTests.UNIT_FT, ((GeoJsonFeatureCollection) other).getPersistableReferenceUnitZ());

        ((GeoJsonPolygon) gj.getFeatures()[0].getGeometry()).setCoordinates(new double[][][]{{{2, 54}, {4, 54}, {2, 54}}});
        assertEquals(-1, gj.getDimension());
        assertFalse(gj.isValid());
        String corrupt = FC2.replace("AnyCrsFeatureCollection", "Corrupt");
        gb = GeoJsonBase.createInstance(corrupt);
        assertNull(gb);
    }

    @Test
    public void testGeoJsonFeature() {
        GeoJsonBase gb;
        gb = GeoJsonBase.createInstance(F1);
        assertNotNull(gb);
        assertEquals("Feature", gb.getType());
        assertTrue(gb instanceof GeoJsonFeature);
        GeoJsonFeature gj = (GeoJsonFeature) gb;
        assertNotNull(gj);
        assertTrue(gj.getGeometry() instanceof GeoJsonPolygon);
        assertEquals(2, gj.getDimension());
        assertTrue(gj.isValid());
        assertEquals(5, gj.getLength());
        gj.setGeometry(null);
        assertFalse(gj.isValid());
        assertEquals(0, gj.getLength());
        gb = GeoJsonBase.createInstance(F2);
        assertNotNull(gb);
        assertEquals("AnyCrsFeature", gb.getType());
        assertTrue(gb instanceof GeoJsonFeature);
        gj = (GeoJsonFeature) gb;
        assertTrue(gj.getGeometry() instanceof GeoJsonPolygon);
        assertEquals(2, gj.getDimension());
        assertTrue(gj.isValid());
        assertNull(gj.getBbox());

        String s = gb.toJsonString();
        GeoJsonBase other = GeoJsonBase.createInstance(s);
        assertEquals(gj, other);

        ((GeoJsonPolygon) gj.getGeometry()).setCoordinates(new double[][][]{{{2, 54}, {4, 54}, {2, 54}}});
        assertEquals(-1, gj.getDimension());
        assertFalse(gj.isValid());
        String corrupt = F2.replace("AnyCrsFeature", "Corrupt");
        gb = GeoJsonBase.createInstance(corrupt);
        assertNull(gb);
    }

    @Test
    public void testGeoJsonGeometryCollection() {
        GeoJsonBase gb;
        gb = GeoJsonBase.createInstance(GC1);
        assertNotNull(gb);
        assertEquals("GeometryCollection", gb.getType());
        assertTrue(gb instanceof GeoJsonGeometryCollection);
        GeoJsonGeometryCollection gj = (GeoJsonGeometryCollection) gb;
        assertNotNull(gj);
        assertTrue(gj.getGeometries()[0] instanceof GeoJsonPoint);
        assertTrue(gj.getGeometries()[1] instanceof GeoJsonPolygon);
        assertEquals(2, gj.getGeometries().length);
        assertEquals(2, gj.getDimension());
        assertEquals(6, gj.getLength());
        assertTrue(gj.isValid());
        gj.setGeometries(null);
        assertFalse(gj.isValid());
        assertEquals(0, gj.getLength());
        gb = GeoJsonBase.createInstance(GC2);
        assertNotNull(gb);
        assertEquals("AnyCrsGeometryCollection", gb.getType());
        assertTrue(gb instanceof GeoJsonGeometryCollection);
        gj = (GeoJsonGeometryCollection) gb;
        assertTrue(gj.getGeometries()[0] instanceof GeoJsonPoint);
        assertTrue(gj.getGeometries()[1] instanceof GeoJsonPolygon);
        assertEquals(2, gj.getGeometries().length);
        assertEquals(2, gj.getDimension());
        assertTrue(gj.isValid());
        assertNull(gj.getBbox());

        String s = gb.toJsonString();
        GeoJsonBase other = GeoJsonBase.createInstance(s);
        assertEquals(gj, other);

        ((GeoJsonPoint) gj.getGeometries()[0]).setCoordinates(new double[]{1.0});
        assertEquals(-1, gj.getDimension());
        assertFalse(gj.isValid());
        String corrupt = GC2.replace("AnyCrsGeometryCollection", "Corrupt");
        gb = GeoJsonBase.createInstance(corrupt);
        assertNull(gb);
    }

    @Test
    public void testGeoJsonMultiPolygon() {
        GeoJsonBase gb;
        gb = GeoJsonBase.createInstance(MPL1);
        assertNotNull(gb);
        assertEquals("MultiPolygon", gb.getType());
        assertTrue(gb instanceof GeoJsonMultiPolygon);
        GeoJsonMultiPolygon gj = (GeoJsonMultiPolygon) gb;
        assertNotNull(gj);
        assertEquals(61.27696068730492, gj.getCoordinates()[0][0][2][1], 1e-10);
        assertEquals(5, gj.getCoordinates()[0][0].length);
        assertEquals(3, gj.getDimension());
        assertEquals(5, gj.getLength());
        assertTrue(gj.isValid());
        gj.setCoordinates(null);
        assertFalse(gj.isValid());
        assertEquals(0, gj.getLength());
        gb = GeoJsonBase.createInstance(MPL2);
        assertNotNull(gb);
        assertEquals("AnyCrsMultiPolygon", gb.getType());
        assertTrue(gb instanceof GeoJsonMultiPolygon);
        gj = (GeoJsonMultiPolygon) gb;
        assertEquals(61.27696068730492, gj.getCoordinates()[0][0][2][1], 1e-10);
        assertEquals(5, gj.getCoordinates()[0][0].length);
        assertTrue(gj.isValid());
        assertNull(gj.getBbox());

        String s = gb.toJsonString();
        GeoJsonBase other = GeoJsonBase.createInstance(s);
        assertEquals(gj, other);

        gj.getCoordinates()[0][0][1] = new double[]{1.0};
        assertEquals(-1, gj.getDimension());
        assertFalse(gj.isValid());
        String corrupt = MPL2.replace("AnyCrsMultiPolygon", "Corrupt");
        gb = GeoJsonBase.createInstance(corrupt);
        assertNull(gb);
    }

    @Test
    public void testGeoJsonPolygon() {
        GeoJsonBase gb;
        gb = GeoJsonBase.createInstance(PL1);
        assertNotNull(gb);
        assertEquals("Polygon", gb.getType());
        assertTrue(gb instanceof GeoJsonPolygon);
        GeoJsonPolygon gj = (GeoJsonPolygon) gb;
        assertNotNull(gj);
        assertEquals(61.27696068730492, gj.getCoordinates()[0][2][1], 1e-10);
        assertEquals(5, gj.getCoordinates()[0].length);
        assertEquals(3, gj.getDimension());
        assertEquals(5, gj.getLength());
        assertTrue(gj.isValid());
        gj.setCoordinates(null);
        assertFalse(gj.isValid());
        assertEquals(0, gj.getLength());
        gb = GeoJsonBase.createInstance(PL2);
        assertNotNull(gb);
        assertEquals("AnyCrsPolygon", gb.getType());
        assertTrue(gb instanceof GeoJsonPolygon);
        gj = (GeoJsonPolygon) gb;
        assertEquals(61.27696068730492, gj.getCoordinates()[0][2][1], 1e-10);
        assertEquals(5, gj.getCoordinates()[0].length);
        assertTrue(gj.isValid());
        assertNull(gj.getBbox());

        String s = gb.toJsonString();
        GeoJsonBase other = GeoJsonBase.createInstance(s);
        assertEquals(gj, other);

        gj.getCoordinates()[0][1] = new double[]{1.0};
        assertEquals(-1, gj.getDimension());
        assertFalse(gj.isValid());
        String corrupt = PL2.replace("AnyCrsPolygon", "Corrupt");
        gb = GeoJsonBase.createInstance(corrupt);
        assertNull(gb);
    }

    @Test
    public void testGeoJsonMultiLineString() {
        GeoJsonBase gb;
        gb = GeoJsonBase.createInstance(MLS1);
        assertNotNull(gb);
        assertEquals("MultiLineString", gb.getType());
        assertTrue(gb instanceof GeoJsonMultiLineString);
        GeoJsonMultiLineString gj = (GeoJsonMultiLineString) gb;
        assertNotNull(gj);
        assertEquals(58.62905077538467, gj.getCoordinates()[0][3][1], 1e-10);
        assertTrue(gj.isValid());
        assertEquals(34, gj.getLength());
        gj.setCoordinates(null);
        assertFalse(gj.isValid());
        gb = GeoJsonBase.createInstance(MLS2);
        assertNotNull(gb);
        assertEquals("AnyCrsMultiLineString", gb.getType());
        assertTrue(gb instanceof GeoJsonMultiLineString);
        gj = (GeoJsonMultiLineString) gb;
        assertEquals(58.62905077538467, gj.getCoordinates()[0][3][1], 1e-10);
        assertEquals(2, gj.getCoordinates().length);
        assertTrue(gj.isValid());
        assertNull(gj.getBbox());

        String s = gb.toJsonString();
        GeoJsonBase other = GeoJsonBase.createInstance(s);
        assertEquals(gj, other);

        gj.getCoordinates()[0][1] = new double[]{1.0};
        assertEquals(-1, gj.getDimension());
        assertFalse(gj.isValid());
        assertEquals(0, gj.getLength());
        String corrupt = MLS2.replace("AnyCrsMultiLineString", "Corrupt");
        gb = GeoJsonBase.createInstance(corrupt);
        assertNull(gb);
    }

    @Test
    public void testGeoJsonLineString() {
        GeoJsonBase gb;
        gb = GeoJsonBase.createInstance(LS1);
        assertNotNull(gb);
        assertEquals("LineString", gb.getType());
        assertTrue(gb instanceof GeoJsonLineString);
        GeoJsonLineString gj = (GeoJsonLineString) gb;
        assertNotNull(gj);
        assertEquals(58.62905077538467, gj.getCoordinates()[3][1], 1e-10);
        assertTrue(gj.isValid());
        gj.setCoordinates(null);
        assertFalse(gj.isValid());
        gb = GeoJsonBase.createInstance(LS2);
        assertNotNull(gb);
        assertEquals("AnyCrsLineString", gb.getType());
        assertTrue(gb instanceof GeoJsonLineString);
        gj = (GeoJsonLineString) gb;
        assertEquals(58.62905077538467, gj.getCoordinates()[3][1], 1e-10);
        assertEquals(34, gj.getCoordinates().length);
        assertEquals(34, gj.getLength());
        assertNull(gj.getBbox());

        String s = gb.toJsonString();
        GeoJsonBase other = GeoJsonBase.createInstance(s);
        assertEquals(gj, other);

        gj.getCoordinates()[1] = new double[]{1.0};
        assertEquals(-1, gj.getDimension());
        assertFalse(gj.isValid());
        assertEquals(0, gj.getLength());
        String corrupt = LS2.replace("AnyCrsLineString", "Corrupt");
        gb = GeoJsonBase.createInstance(corrupt);
        assertNull(gb);
    }

    @Test
    public void testGeoJsonMultiPoint() {
        GeoJsonBase gb;
        gb = GeoJsonBase.createInstance(MP01);
        assertNotNull(gb);
        assertEquals("MultiPoint", gb.getType());
        assertTrue(gb instanceof GeoJsonMultiPoint);
        GeoJsonMultiPoint gj = (GeoJsonMultiPoint) gb;
        assertEquals(29.8823, gj.getCoordinates()[0][1], 1e-10);
        assertEquals(2, gj.getCoordinates().length);
        assertEquals(3, gj.getDimension());
        assertEquals(2, gj.getLength());
        assertTrue(gj.isValid());
        gj.setCoordinates(null);
        assertFalse(gj.isValid());
        assertNull(gj.getBbox());
        gb = GeoJsonBase.createInstance(MP02);
        assertNotNull(gb);
        assertEquals("AnyCrsMultiPoint", gb.getType());
        assertTrue(gb instanceof GeoJsonMultiPoint);
        gj = (GeoJsonMultiPoint) gb;
        assertEquals(458966.7531300551, gj.getCoordinates()[0][1], 1e-10);
        assertEquals(2, gj.getCoordinates().length);
        assertEquals(2, gj.getLength());
        assertNull(gj.getBbox());

        String s = gb.toJsonString();
        GeoJsonBase other = GeoJsonBase.createInstance(s);
        assertEquals(gj, other);

        gj.getCoordinates()[1] = new double[]{1.0};
        assertEquals(-1, gj.getDimension());
        assertFalse(gj.isValid());
        assertEquals(0, gj.getLength());
        String corrupt = MP02.replace("AnyCrsMultiPoint", "Corrupt");
        gb = GeoJsonBase.createInstance(corrupt);
        assertNull(gb);
    }

    @Test
    public void testGeoJsonPoint() {
        GeoJsonBase gb;
        gb = GeoJsonBase.createInstance(P01);
        assertNotNull(gb);
        assertEquals("Point", gb.getType());
        assertTrue(gb instanceof GeoJsonPoint);
        GeoJsonPoint gj = (GeoJsonPoint) gb;
        assertEquals(29.8823, gj.getCoordinates()[1], 1e-10);
        assertEquals(3, gj.getCoordinates().length);
        assertEquals(3, gj.getDimension());
        assertEquals(1, gj.getLength());
        assertTrue(gj.isValid());
        assertNull(gj.getBbox());

        String s = gb.toJsonString();
        GeoJsonBase other = GeoJsonBase.createInstance(s);
        assertEquals(gj, other);

        gj.setCoordinates(null);
        assertFalse(gj.isValid());
        assertEquals(0, gj.getLength());
        gb = GeoJsonBase.createInstance(P02);
        assertNotNull(gb);
        assertEquals("AnyCrsPoint", gb.getType());
        assertTrue(gb instanceof GeoJsonPoint);
        gj = (GeoJsonPoint) gb;
        assertEquals(458966.7531300551, gj.getCoordinates()[1], 1e-10);
        assertEquals(3, gj.getCoordinates().length);
        assertNull(gj.getBbox());
        String corrupt = P02.replace("AnyCrsPoint", "Corrupt");
        gb = GeoJsonBase.createInstance(corrupt);
        assertNull(gb);
    }
}

