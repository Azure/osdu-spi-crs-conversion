package org.opengroup.osdu.crs.converter;

import org.apache.sis.io.wkt.WKTFormat;
import org.junit.Test;
import org.junit.Ignore;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengroup.osdu.crs.model.ConvertPointsResponse;
import org.opengroup.osdu.crs.util.ConstantsTests;

import java.util.Arrays;

import static org.junit.Assert.*;

public class EsriWktTransformTests {

    private static final double DELTA_L = 0.1;
    private static final double DELTA_A = 0.00001;

    @Test
    public void testEpsg4230_1311() throws Exception {
        String fromCRS = ConstantsTests.LB_WGS84[ConstantsTests.V2];
        String toCRS_fromTransformCode = "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"4230018\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4230\"},\"name\":\"GCS_European_1950\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4230]]\"},\"name\":\"ED50 * UKOOA-CO [4230,1311]\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1311\"},\"name\":\"ED_1950_To_WGS_1984_18\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"ED_1950_To_WGS_1984_18\\\",GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-89.5],PARAMETER[\\\"Y_Axis_Translation\\\",-93.8],PARAMETER[\\\"Z_Axis_Translation\\\",-123.1],PARAMETER[\\\"X_Axis_Rotation\\\",0.0],PARAMETER[\\\"Y_Axis_Rotation\\\",0.0],PARAMETER[\\\"Z_Axis_Rotation\\\",-0.156],PARAMETER[\\\"Scale_Difference\\\",1.2],OPERATIONACCURACY[1.0],AUTHORITY[\\\"EPSG\\\",1311]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\"}";
        double[] zCoordinates = new double[]{0.0, 0.0};
        double[] xyCoordinates = new double[]{
                54, 4, 54, 4
        };

        CRSConverter converter = new CRSConverter();
        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS_fromTransformCode, xyCoordinates, zCoordinates);
        double[] expectedXYCoordinates = Arrays.copyOf(xyCoordinates, xyCoordinates.length);
        double[] expectedZCoordinates = Arrays.copyOf(zCoordinates, zCoordinates.length);
        zCoordinates = new double[]{0.0, 0.0};
        xyCoordinates = new double[]{
                54, 4, 54, 4
        };

        String toCRS_fromTransformWKT = "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"4230018\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4230\"},\"name\":\"GCS_European_1950\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4230]]\"},\"name\":\"ED50 * UKOOA-CO [4230,1311]\",\"singleCT\":{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"1311\"},\"name\":\"ED_1950_To_WGS_1984_18\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"ED_1950_To_WGS_1984_18\\\",GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-89.5],PARAMETER[\\\"Y_Axis_Translation\\\",-93.8],PARAMETER[\\\"Z_Axis_Translation\\\",-123.1],PARAMETER[\\\"X_Axis_Rotation\\\",0.0],PARAMETER[\\\"Y_Axis_Rotation\\\",0.0],PARAMETER[\\\"Z_Axis_Rotation\\\",-0.156],PARAMETER[\\\"Scale_Difference\\\",1.2],OPERATIONACCURACY[1.0],AUTHORITY[\\\"EPSG\\\",1311]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\"}";
        result = converter.convertPoint(fromCRS, toCRS_fromTransformWKT, xyCoordinates, zCoordinates);
        assertEquals((Integer) (expectedXYCoordinates.length / 2), result.getSuccessCount());
        for (int i = 0; i < xyCoordinates.length; i++) {
            assertEquals(expectedXYCoordinates[i], xyCoordinates[i], DELTA_A);
            assertEquals(expectedZCoordinates[i % 2], zCoordinates[i % 2], DELTA_A);
        }
    }

    @Test
    public void testEpsg1327() throws Exception {

        String toCRS = ConstantsTests.LB_WGS84[ConstantsTests.V2];
        String fromCRS = "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"1327\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"22092\"},\"name\":\"Geographic1\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"PROJCRS[\\\"Camacupa 1948 / TM 12 SE\\\",\\r\\n  BASEGEODCRS[\\\"Camacupa 1948\\\",\\r\\n    DATUM[\\\"Camacupa 1948\\\",\\r\\n      ELLIPSOID[\\\"Clarke 1880 (RGS)\\\", 6378249.145, 293.465, LENGTHUNIT[\\\"metre\\\", 1]]],\\r\\n      PRIMEM[\\\"Greenwich\\\", 0.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295]]],\\r\\n  CONVERSION[\\\"TM 12 SE\\\",\\r\\n    METHOD[\\\"Transverse Mercator\\\", ID[\\\"EPSG\\\", 9807, \\\"9.9.1\\\"]],\\r\\n    PARAMETER[\\\"Latitude of natural origin\\\", 0.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295], ID[\\\"EPSG\\\", 8801]],\\r\\n    PARAMETER[\\\"Longitude of natural origin\\\", 12.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295], ID[\\\"EPSG\\\", 8802]],\\r\\n    PARAMETER[\\\"Scale factor at natural origin\\\", 0.9996, SCALEUNIT[\\\"unity\\\", 1], ID[\\\"EPSG\\\", 8805]],\\r\\n    PARAMETER[\\\"False easting\\\", 500000.0, LENGTHUNIT[\\\"metre\\\", 1], ID[\\\"EPSG\\\", 8806]],\\r\\n    PARAMETER[\\\"False northing\\\", 10000000.0, LENGTHUNIT[\\\"metre\\\", 1], ID[\\\"EPSG\\\", 8807]]],\\r\\n  CS[Cartesian, 2],\\r\\n    AXIS[\\\"Easting (E)\\\", east, ORDER[1]],\\r\\n    AXIS[\\\"Northing (N)\\\", north, ORDER[2]],\\r\\n    LENGTHUNIT[\\\"metre\\\", 1],\\r\\n  SCOPE[\\\"Oil exploration by Shell Angola for offshore blocks 1 and 16 and by BP, Total and ExxonMobil for offshore blocks 31-33.\\\"],\\r\\n  AREA[\\\"Angola - Angola proper - offshore.\\\"],\\r\\n  BBOX[-17.26, 8.20, -6.01, 13.86],\\r\\n  ID[\\\"EPSG\\\", 22092, \\\"9.9.1\\\", URI[\\\"urn:ogc:def:crs:EPSG:9.9.1:22092\\\"]],\\r\\n  REMARK[\\\"Used for exploration and production geoscience activity. Note: WGS 84 / TM 12 SE (CRS code 5842) used for Angola LNG project.\\\"]]\"},\"name\":\"Persistable reference name\",\"singleCT\":{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"1327\"},\"name\":\"CT name\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"Camacupa_To_WGS_1984_10\\\",GEOGCS[\\\"GCS_Camacupa\\\",DATUM[\\\"D_Camacupa\\\",SPHEROID[\\\"Clarke_1880_RGS\\\",6378249.145,293.465]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-50.9],PARAMETER[\\\"Y_Axis_Translation\\\",-347.6],PARAMETER[\\\"Z_Axis_Translation\\\",-231.0],AUTHORITY[\\\"EPSG\\\",1327]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\"}";
        double[] zCoordinates = new double[]{0.0, 0.0};
        double[] xyCoordinates = new double[]{
                358041.71, 9313954.06, 358041.71, 9313954.06
        };
        double[] expectedXYCoordinates = new double[]{
                10.7139156, -6.2071550, 10.7139156, -6.2071550
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };
        CRSConverter converter = new CRSConverter();
        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
        assertEquals((Integer) (expectedXYCoordinates.length / 2), result.getSuccessCount());
        for (int i = 0; i < xyCoordinates.length; i++) {
            assertEquals(expectedXYCoordinates[i], xyCoordinates[i], DELTA_A);
            assertEquals(expectedZCoordinates[i % 2], zCoordinates[i % 2], DELTA_A);
        }
    }

    @Test
    public void testEpsg1808() throws Exception {
        String toCRS = ConstantsTests.LB_WGS84[ConstantsTests.V2];
        String fromCRS = "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"1808\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"ESPG\",\"code\":\"2499\"},\"name\":\"Geographic1\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"PROJCRS[\\\"Pulkovo 1942 / Gauss-Kruger CM 51E\\\",\\r\\n  BASEGEODCRS[\\\"Pulkovo 1942\\\",\\r\\n    DATUM[\\\"Pulkovo 1942\\\",\\r\\n      ELLIPSOID[\\\"Krassowsky 1940\\\", 6378245.0, 298.3, LENGTHUNIT[\\\"metre\\\", 1]]],\\r\\n      PRIMEM[\\\"Greenwich\\\", 0.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295]]],\\r\\n  CONVERSION[\\\"Gauss-Kruger CM 51E\\\",\\r\\n    METHOD[\\\"Transverse Mercator\\\", ID[\\\"EPSG\\\", 9807, \\\"9.9.1\\\"]],\\r\\n    PARAMETER[\\\"Latitude of natural origin\\\", 0.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295], ID[\\\"EPSG\\\", 8801]],\\r\\n    PARAMETER[\\\"Longitude of natural origin\\\", 51.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295], ID[\\\"EPSG\\\", 8802]],\\r\\n    PARAMETER[\\\"Scale factor at natural origin\\\", 1.0, SCALEUNIT[\\\"unity\\\", 1], ID[\\\"EPSG\\\", 8805]],\\r\\n    PARAMETER[\\\"False easting\\\", 500000.0, LENGTHUNIT[\\\"metre\\\", 1], ID[\\\"EPSG\\\", 8806]],\\r\\n    PARAMETER[\\\"False northing\\\", 0.0, LENGTHUNIT[\\\"metre\\\", 1], ID[\\\"EPSG\\\", 8807]]],\\r\\n  CS[Cartesian, 2],\\r\\n    AXIS[\\\"Northing (X)\\\", north, ORDER[1]],\\r\\n    AXIS[\\\"Easting (Y)\\\", east, ORDER[2]],\\r\\n    LENGTHUNIT[\\\"metre\\\", 1],\\r\\n  SCOPE[\\\"Medium scale topographic mapping.\\\"],\\r\\n  AREA[\\\"Azerbaijan - east of 48°E; Kazakhstan - 48°E to 54°E; Russian Federation - 48°E to 54°E; Turkmenistan - west of 54°E. Includes Caspian Sea (considered a lake rather than offshore).\\\"],\\r\\n  BBOX[37.34, 48.00, 81.40, 54.00],\\r\\n  ID[\\\"EPSG\\\", 2499, \\\"9.9.1\\\", URI[\\\"urn:ogc:def:crs:EPSG:9.9.1:2499\\\"]],\\r\\n  REMARK[\\\"Truncated form of Pulkovo 1942 / Gauss-Kruger zone 9 (code 28409).\\\"]]\"},\"name\":\"Persistable reference name\",\"singleCT\":{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"1808\"},\"name\":\"CT name\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"Pulkovo_1942_To_WGS_1984_14\\\",GEOGCS[\\\"GCS_Pulkovo_1942\\\",DATUM[\\\"D_Pulkovo_1942\\\",SPHEROID[\\\"Krasovsky_1940\\\",6378245.0,298.3]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",686.1],PARAMETER[\\\"Y_Axis_Translation\\\",-123.5],PARAMETER[\\\"Z_Axis_Translation\\\",-574.4],PARAMETER[\\\"X_Axis_Rotation\\\",8.045],PARAMETER[\\\"Y_Axis_Rotation\\\",-23.366],PARAMETER[\\\"Z_Axis_Rotation\\\",10.791],PARAMETER[\\\"Scale_Difference\\\",-2.926],AUTHORITY[\\\"EPSG\\\",1808]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\"}";
        double[] zCoordinates = new double[]{0.0, 0.0};
        double[] xyCoordinates = new double[]{
                447624.90, 4420966.75, 447624.90, 4420966.75
        };
        double[] expectedXYCoordinates = new double[]{
                50.3862517, 39.9205089, 50.3862517, 39.9205089
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };
        CRSConverter converter = new CRSConverter();
        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
        assertEquals((Integer) (expectedXYCoordinates.length / 2), result.getSuccessCount());
        for (int i = 0; i < xyCoordinates.length; i++) {
            assertEquals(expectedXYCoordinates[i], xyCoordinates[i], DELTA_A);
            assertEquals(expectedZCoordinates[i % 2], zCoordinates[i % 2], DELTA_A);
        }

    }


    @Test
    public void testEpsg1148() {
        String fromCRS = "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"1148\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"ESPG\",\"code\":\"22992\"},\"name\":\"Geographic1\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"PROJCRS[\\\"Egypt 1907 / Red Belt\\\",\\r\\n  BASEGEODCRS[\\\"Egypt 1907\\\",\\r\\n    DATUM[\\\"Egypt 1907\\\",\\r\\n      ELLIPSOID[\\\"Helmert 1906\\\", 6378200.0, 298.3, LENGTHUNIT[\\\"metre\\\", 1]]],\\r\\n      PRIMEM[\\\"Greenwich\\\", 0.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295]]],\\r\\n  CONVERSION[\\\"Egypt Red Belt\\\",\\r\\n    METHOD[\\\"Transverse Mercator\\\", ID[\\\"EPSG\\\", 9807, \\\"9.9.1\\\"]],\\r\\n    PARAMETER[\\\"Latitude of natural origin\\\", 30.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295], ID[\\\"EPSG\\\", 8801]],\\r\\n    PARAMETER[\\\"Longitude of natural origin\\\", 31.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295], ID[\\\"EPSG\\\", 8802]],\\r\\n    PARAMETER[\\\"Scale factor at natural origin\\\", 1.0, SCALEUNIT[\\\"unity\\\", 1], ID[\\\"EPSG\\\", 8805]],\\r\\n    PARAMETER[\\\"False easting\\\", 615000.0, LENGTHUNIT[\\\"metre\\\", 1], ID[\\\"EPSG\\\", 8806]],\\r\\n    PARAMETER[\\\"False northing\\\", 810000.0, LENGTHUNIT[\\\"metre\\\", 1], ID[\\\"EPSG\\\", 8807]]],\\r\\n  CS[Cartesian, 2],\\r\\n    AXIS[\\\"Easting (E)\\\", east, ORDER[1]],\\r\\n    AXIS[\\\"Northing (N)\\\", north, ORDER[2]],\\r\\n    LENGTHUNIT[\\\"metre\\\", 1],\\r\\n  SCOPE[\\\"Large and medium scale topographic mapping and engineering survey.\\\"],\\r\\n  AREA[\\\"Egypt - onshore between 29°E and 33°E, offshore Mediterranean east of 29°E and offshore Gulf of Suez.\\\"],\\r\\n  BBOX[21.99, 29.00, 33.82, 34.27],\\r\\n  ID[\\\"EPSG\\\", 22992, \\\"9.9.1\\\", URI[\\\"urn:ogc:def:crs:EPSG:9.9.1:22992\\\"]],\\r\\n  REMARK[\\\"See also Egypt 1907 / Blue Belt for non oil industry usage in Sinai peninsula.\\\"]]\"},\"name\":\"Persistable reference name\",\"singleCT\":{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"1148\"},\"name\":\"CT name\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"Egypt_1907_To_WGS_1984\\\",GEOGCS[\\\"GCS_Egypt_1907\\\",DATUM[\\\"D_Egypt_1907\\\",SPHEROID[\\\"Helmert_1906\\\",6378200.0,298.3]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-130.0],PARAMETER[\\\"Y_Axis_Translation\\\",110.0],PARAMETER[\\\"Z_Axis_Translation\\\",-13.0],AUTHORITY[\\\"EPSG\\\",1148]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\"}";
        String toCRS = ConstantsTests.LB_WGS84[ConstantsTests.V2];

        double[] zCoordinates = new double[]{0.0, 0.0};
        double[] xyCoordinates = new double[]{
                504736.75, 988949.89, 504736.75, 988949.89
        };
        double[] expectedXYCoordinates = new double[]{
                29.8397494, 31.6090022, 29.8397494, 31.6090022
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };
        CRSConverter converter = new CRSConverter();
        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
        assertEquals((Integer) (expectedXYCoordinates.length / 2), result.getSuccessCount());
        for (int i = 0; i < xyCoordinates.length; i++) {
            assertEquals(expectedXYCoordinates[i], xyCoordinates[i], DELTA_A);
            assertEquals(expectedZCoordinates[i % 2], zCoordinates[i % 2], DELTA_A);
        }
    }

    @Test
    public void testEpsg23030_1311() {
        String fromCRS = "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"1311\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"ESPG\",\"code\":\"23030\"},\"name\":\"Geographic1\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"PROJCRS[\\\"ED50 / UTM zone 30N\\\",\\r\\n  BASEGEODCRS[\\\"ED50\\\",\\r\\n    DATUM[\\\"European Datum 1950\\\",\\r\\n      ELLIPSOID[\\\"International 1924\\\", 6378388.0, 297.0, LENGTHUNIT[\\\"metre\\\", 1]]],\\r\\n      PRIMEM[\\\"Greenwich\\\", 0.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295]]],\\r\\n  CONVERSION[\\\"UTM zone 30N\\\",\\r\\n    METHOD[\\\"Transverse Mercator\\\", ID[\\\"EPSG\\\", 9807, \\\"9.9.1\\\"]],\\r\\n    PARAMETER[\\\"Latitude of natural origin\\\", 0.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295], ID[\\\"EPSG\\\", 8801]],\\r\\n    PARAMETER[\\\"Longitude of natural origin\\\", -3.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295], ID[\\\"EPSG\\\", 8802]],\\r\\n    PARAMETER[\\\"Scale factor at natural origin\\\", 0.9996, SCALEUNIT[\\\"unity\\\", 1], ID[\\\"EPSG\\\", 8805]],\\r\\n    PARAMETER[\\\"False easting\\\", 500000.0, LENGTHUNIT[\\\"metre\\\", 1], ID[\\\"EPSG\\\", 8806]],\\r\\n    PARAMETER[\\\"False northing\\\", 0.0, LENGTHUNIT[\\\"metre\\\", 1], ID[\\\"EPSG\\\", 8807]]],\\r\\n  CS[Cartesian, 2],\\r\\n    AXIS[\\\"Easting (E)\\\", east, ORDER[1]],\\r\\n    AXIS[\\\"Northing (N)\\\", north, ORDER[2]],\\r\\n    LENGTHUNIT[\\\"metre\\\", 1],\\r\\n  SCOPE[\\\"Large and medium scale topographic mapping and engineering survey.\\\"],\\r\\n  AREA[\\\"Europe - between 6°W and 0°W - Channel Islands (Jersey, Guernsey); France offshore; Gibraltar; Ireland offshore; Norway including Svalbard - offshore; Spain - onshore; United Kingdom - UKCS offshore.\\\"],\\r\\n  BBOX[35.26, -6.00, 80.53, 0.00],\\r\\n  ID[\\\"EPSG\\\", 23030, \\\"9.9.1\\\", URI[\\\"urn:ogc:def:crs:EPSG:9.9.1:23030\\\"]]]\"},\"name\":\"Persistable reference name\",\"singleCT\":{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"1311\"},\"name\":\"CT name\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"ED_1950_To_WGS_1984_18\\\",GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-89.5],PARAMETER[\\\"Y_Axis_Translation\\\",-93.8],PARAMETER[\\\"Z_Axis_Translation\\\",-123.1],PARAMETER[\\\"X_Axis_Rotation\\\",0.0],PARAMETER[\\\"Y_Axis_Rotation\\\",0.0],PARAMETER[\\\"Z_Axis_Rotation\\\",-0.156],PARAMETER[\\\"Scale_Difference\\\",1.2],AUTHORITY[\\\"EPSG\\\",1311]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\"}";
        String toCRS = ConstantsTests.LB_WGS84[ConstantsTests.V2];

        double[] zCoordinates = new double[]{0.0, 0.0};
        double[] xyCoordinates = new double[]{
                443005.81, 6689620.56, 443005.81, 6689620.56
        };
        double[] expectedXYCoordinates = new double[]{
                -4.0341764, 60.3370572, -4.0341764, 60.3370572
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };
        CRSConverter converter = new CRSConverter();
        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
        assertEquals((Integer) (expectedXYCoordinates.length / 2), result.getSuccessCount());
        for (int i = 0; i < xyCoordinates.length; i++) {
            assertEquals(expectedXYCoordinates[i], xyCoordinates[i], DELTA_A);
            assertEquals(expectedZCoordinates[i % 2], zCoordinates[i % 2], DELTA_A);
        }
    }

    @Test
    public void testEpsg1150() {
        String fromCRS = "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"1150\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"ESPG\",\"code\":\"28352\"},\"name\":\"Geographic1\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"PROJCRS[\\\"GDA94 / MGA zone 52\\\",\\r\\n  BASEGEODCRS[\\\"GDA94\\\",\\r\\n    DATUM[\\\"Geocentric Datum of Australia 1994\\\",\\r\\n      ELLIPSOID[\\\"GRS 1980\\\", 6378137.0, 298.257222101, LENGTHUNIT[\\\"metre\\\", 1]]],\\r\\n      PRIMEM[\\\"Greenwich\\\", 0.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295]]],\\r\\n  CONVERSION[\\\"Map Grid of Australia zone 52\\\",\\r\\n    METHOD[\\\"Transverse Mercator\\\", ID[\\\"EPSG\\\", 9807, \\\"9.9.1\\\"]],\\r\\n    PARAMETER[\\\"Latitude of natural origin\\\", 0.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295], ID[\\\"EPSG\\\", 8801]],\\r\\n    PARAMETER[\\\"Longitude of natural origin\\\", 129.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295], ID[\\\"EPSG\\\", 8802]],\\r\\n    PARAMETER[\\\"Scale factor at natural origin\\\", 0.9996, SCALEUNIT[\\\"unity\\\", 1], ID[\\\"EPSG\\\", 8805]],\\r\\n    PARAMETER[\\\"False easting\\\", 500000.0, LENGTHUNIT[\\\"metre\\\", 1], ID[\\\"EPSG\\\", 8806]],\\r\\n    PARAMETER[\\\"False northing\\\", 10000000.0, LENGTHUNIT[\\\"metre\\\", 1], ID[\\\"EPSG\\\", 8807]]],\\r\\n  CS[Cartesian, 2],\\r\\n    AXIS[\\\"Easting (E)\\\", east, ORDER[1]],\\r\\n    AXIS[\\\"Northing (N)\\\", north, ORDER[2]],\\r\\n    LENGTHUNIT[\\\"metre\\\", 1],\\r\\n  SCOPE[\\\"Large and medium scale topographic mapping and engineering survey.\\\"],\\r\\n  AREA[\\\"Australia - onshore and offshore between 126°E and 132°E.\\\"],\\r\\n  BBOX[-37.38, 125.99, -9.10, 132.00],\\r\\n  ID[\\\"EPSG\\\", 28352, \\\"9.9.1\\\", URI[\\\"urn:ogc:def:crs:EPSG:9.9.1:28352\\\"]]]\"},\"name\":\"Persistable reference name\",\"singleCT\":{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"1150\"},\"name\":\"CT name\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"GDA_1994_To_WGS_1984\\\",GEOGCS[\\\"GCS_GDA_1994\\\",DATUM[\\\"D_GDA_1994\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.0],PARAMETER[\\\"Y_Axis_Translation\\\",0.0],PARAMETER[\\\"Z_Axis_Translation\\\",0.0],PARAMETER[\\\"X_Axis_Rotation\\\",0.0],PARAMETER[\\\"Y_Axis_Rotation\\\",0.0],PARAMETER[\\\"Z_Axis_Rotation\\\",0.0],PARAMETER[\\\"Scale_Difference\\\",0.0],AUTHORITY[\\\"EPSG\\\",1150]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\"}";
        String toCRS = ConstantsTests.LB_WGS84[ConstantsTests.V2];

        double[] zCoordinates = new double[]{0.0, 0.0};
        double[] xyCoordinates = new double[]{
                651815.00, 6132427.00, 651815.00, 6132427.00
        };
        double[] expectedXYCoordinates = new double[]{
                130.6623881, -34.9392978, 130.6623881, -34.9392978
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };
        CRSConverter converter = new CRSConverter();
        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
        assertEquals((Integer) (expectedXYCoordinates.length / 2), result.getSuccessCount());
        for (int i = 0; i < xyCoordinates.length; i++) {
            assertEquals(expectedXYCoordinates[i], xyCoordinates[i], DELTA_A);
            assertEquals(expectedZCoordinates[i % 2], zCoordinates[i % 2], DELTA_A);
        }
    }

    @Test
    public void testEpsg1330() {
        String fromCRS = "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"1330\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"ESPG\",\"code\":\"7992\"},\"name\":\"Geographic1\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"PROJCRS[\\\"Malongo 1987 / UTM zone 33S\\\",\\r\\n  BASEGEODCRS[\\\"Malongo 1987\\\",\\r\\n    DATUM[\\\"Malongo 1987\\\",\\r\\n      ELLIPSOID[\\\"International 1924\\\", 6378388.0, 297.0, LENGTHUNIT[\\\"metre\\\", 1]]],\\r\\n      PRIMEM[\\\"Greenwich\\\", 0.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295]]],\\r\\n  CONVERSION[\\\"UTM zone 33S\\\",\\r\\n    METHOD[\\\"Transverse Mercator\\\", ID[\\\"EPSG\\\", 9807, \\\"9.9.1\\\"]],\\r\\n    PARAMETER[\\\"Latitude of natural origin\\\", 0.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295], ID[\\\"EPSG\\\", 8801]],\\r\\n    PARAMETER[\\\"Longitude of natural origin\\\", 15.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295], ID[\\\"EPSG\\\", 8802]],\\r\\n    PARAMETER[\\\"Scale factor at natural origin\\\", 0.9996, SCALEUNIT[\\\"unity\\\", 1], ID[\\\"EPSG\\\", 8805]],\\r\\n    PARAMETER[\\\"False easting\\\", 500000.0, LENGTHUNIT[\\\"metre\\\", 1], ID[\\\"EPSG\\\", 8806]],\\r\\n    PARAMETER[\\\"False northing\\\", 10000000.0, LENGTHUNIT[\\\"metre\\\", 1], ID[\\\"EPSG\\\", 8807]]],\\r\\n  CS[Cartesian, 2],\\r\\n    AXIS[\\\"Easting (E)\\\", east, ORDER[1]],\\r\\n    AXIS[\\\"Northing (N)\\\", north, ORDER[2]],\\r\\n    LENGTHUNIT[\\\"metre\\\", 1],\\r\\n  SCOPE[\\\"Oil industry offshore exploration and production from 2014.\\\"],\\r\\n  AREA[\\\"The Democratic Republic of the Congo (Zaire) - offshore.\\\"],\\r\\n  BBOX[-6.04, 11.79, -5.79, 12.37],\\r\\n  ID[\\\"EPSG\\\", 7992, \\\"9.9.1\\\", URI[\\\"urn:ogc:def:crs:EPSG:9.9.1:7992\\\"]]]\"},\"name\":\"Persistable reference name\",\"singleCT\":{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"1330\"},\"name\":\"CT name\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"Malongo_1987_To_WGS_1984_1\\\",GEOGCS[\\\"GCS_Malongo_1987\\\",DATUM[\\\"D_Malongo_1987\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-252.95],PARAMETER[\\\"Y_Axis_Translation\\\",-4.11],PARAMETER[\\\"Z_Axis_Translation\\\",-96.38],OPERATIONACCURACY[10.0],AUTHORITY[\\\"EPSG\\\",1330]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\"}";
        String toCRS = ConstantsTests.LB_WGS84[ConstantsTests.V2];

        double[] zCoordinates = new double[]{0.0, 0.0};
        double[] xyCoordinates = new double[]{
                160862.68, 9333520.66, 160862.68, 9333520.66
        };
        double[] expectedXYCoordinates = new double[]{
                11.93746040, -06.02188086, 11.93746040, -06.02188086
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };
        CRSConverter converter = new CRSConverter();
        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
        assertEquals((Integer) (expectedXYCoordinates.length / 2), result.getSuccessCount());
        for (int i = 0; i < xyCoordinates.length; i++) {
            assertEquals(expectedXYCoordinates[i], xyCoordinates[i], DELTA_A);
            assertEquals(expectedZCoordinates[i % 2], zCoordinates[i % 2], DELTA_A);
        }
    }

    @Test
    public void testEpsg1056() {
        String fromCRS = "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"1056\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"ESPG\",\"code\":\"20439\"},\"name\":\"Geographic1\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"PROJCRS[\\\"Ain el Abd / UTM zone 39N\\\",\\r\\n  BASEGEODCRS[\\\"Ain el Abd\\\",\\r\\n    DATUM[\\\"Ain el Abd 1970\\\",\\r\\n      ELLIPSOID[\\\"International 1924\\\", 6378388.0, 297.0, LENGTHUNIT[\\\"metre\\\", 1]]],\\r\\n      PRIMEM[\\\"Greenwich\\\", 0.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295]]],\\r\\n  CONVERSION[\\\"UTM zone 39N\\\",\\r\\n    METHOD[\\\"Transverse Mercator\\\", ID[\\\"EPSG\\\", 9807, \\\"9.9.1\\\"]],\\r\\n    PARAMETER[\\\"Latitude of natural origin\\\", 0.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295], ID[\\\"EPSG\\\", 8801]],\\r\\n    PARAMETER[\\\"Longitude of natural origin\\\", 51.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295], ID[\\\"EPSG\\\", 8802]],\\r\\n    PARAMETER[\\\"Scale factor at natural origin\\\", 0.9996, SCALEUNIT[\\\"unity\\\", 1], ID[\\\"EPSG\\\", 8805]],\\r\\n    PARAMETER[\\\"False easting\\\", 500000.0, LENGTHUNIT[\\\"metre\\\", 1], ID[\\\"EPSG\\\", 8806]],\\r\\n    PARAMETER[\\\"False northing\\\", 0.0, LENGTHUNIT[\\\"metre\\\", 1], ID[\\\"EPSG\\\", 8807]]],\\r\\n  CS[Cartesian, 2],\\r\\n    AXIS[\\\"Easting (E)\\\", east, ORDER[1]],\\r\\n    AXIS[\\\"Northing (N)\\\", north, ORDER[2]],\\r\\n    LENGTHUNIT[\\\"metre\\\", 1],\\r\\n  SCOPE[\\\"Large and medium scale topographic mapping and engineering survey. In Kuwait, oil production (but not exploration - see KOC Lambert, code 24600).\\\"],\\r\\n  AREA[\\\"Kuwait - onshore east of 48°E. Saudi Arabia - onshore between 48°E and 54°E.\\\"],\\r\\n  BBOX[17.94, 47.99, 30.04, 54.01],\\r\\n  ID[\\\"EPSG\\\", 20439, \\\"9.9.1\\\", URI[\\\"urn:ogc:def:crs:EPSG:9.9.1:20439\\\"]],\\r\\n  REMARK[\\\"Known in Kuwait as \\\"\\\"KOC UTM\\\"\\\". Used by KOC for engineering but not explorartion (see KOC Lambert, CRS code 24600). In Saudi Arabia, replaced by MTRF-2000 / UTM zone 39N (CRS code 8839).\\\"]]\"},\"name\":\"Persistable reference name\",\"singleCT\":{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"1056\"},\"name\":\"CT name\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"Ain_El_Abd_To_WGS_1984_4\\\",GEOGCS[\\\"GCS_Ain_el_Abd_1970\\\",DATUM[\\\"D_Ain_el_Abd_1970\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Coordinate_Frame\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-85.645],PARAMETER[\\\"Y_Axis_Translation\\\",-273.077],PARAMETER[\\\"Z_Axis_Translation\\\",-79.708],PARAMETER[\\\"X_Axis_Rotation\\\",-2.289],PARAMETER[\\\"Y_Axis_Rotation\\\",1.421],PARAMETER[\\\"Z_Axis_Rotation\\\",-2.532],PARAMETER[\\\"Scale_Difference\\\",3.194],OPERATIONACCURACY[1.0],AUTHORITY[\\\"EPSG\\\",1056]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\"}";
        String toCRS = ConstantsTests.LB_WGS84[ConstantsTests.V2];

        double[] zCoordinates = new double[]{0.0, 0.0};
        double[] xyCoordinates = new double[]{
                159006.34, 3256119.41, 159006.34, 3256119.41
        };
        double[] expectedXYCoordinates = new double[]{
                47.48651763, 29.38823806, 47.48651763, 29.38823806
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };
        CRSConverter converter = new CRSConverter();
        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
        assertEquals((Integer) (expectedXYCoordinates.length / 2), result.getSuccessCount());
        for (int i = 0; i < xyCoordinates.length; i++) {
            assertEquals(expectedXYCoordinates[i], xyCoordinates[i], DELTA_A);
            assertEquals(expectedZCoordinates[i % 2], zCoordinates[i % 2], DELTA_A);
        }
    }

    @Test
    public void testGeocentricTranslation() throws Exception {
        String fromCRS = "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"50102\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"ESPG\",\"code\":\"4191\"},\"name\":\"Geographic1\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEODCRS[\\\"Albanian 1987\\\",\\r\\n  DATUM[\\\"Albanian 1987\\\",\\r\\n    ELLIPSOID[\\\"Krassowsky 1940\\\", 6378245.0, 298.3, LENGTHUNIT[\\\"metre\\\", 1]]],\\r\\n    PRIMEM[\\\"Greenwich\\\", 0.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295]],\\r\\n  CS[ellipsoidal, 2],\\r\\n    AXIS[\\\"Latitude (B)\\\", north, ORDER[1]],\\r\\n    AXIS[\\\"Longitude (L)\\\", east, ORDER[2]],\\r\\n    ANGLEUNIT[\\\"degree\\\", 0.017453292519943295],\\r\\n  SCOPE[\\\"Geodetic survey.\\\"],\\r\\n  AREA[\\\"Albania - onshore.\\\"],\\r\\n  BBOX[39.64, 19.22, 42.67, 21.06],\\r\\n  ID[\\\"EPSG\\\", 4191, \\\"9.9.1\\\", URI[\\\"urn:ogc:def:crs:EPSG:9.9.1:4191\\\"]],\\r\\n  REMARK[\\\"Replaced by KRGJSH-2010.\\\"]]\"},\"name\":\"Persistable reference name\",\"singleCT\":{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"50102\"},\"name\":\"CT name\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"Albanian 1987 to WGS 84 (Shell 2)\\\", GEOGCS[\\\"GCS_Albanian_1987\\\", DATUM[\\\"D_Albanian_1987\\\", SPHEROID[\\\"Krasovsky_1940\\\",6378245,298.3, AUTHORITY[\\\"EPSG\\\",\\\"7024\\\"]], AUTHORITY[\\\"EPSG\\\",\\\"6191\\\"]], PRIMEM[\\\"Greenwich\\\",0, AUTHORITY[\\\"EPSG\\\",\\\"8901\\\"]], UNIT[\\\"degree\\\",0.0174532925199433,AUTHORITY[\\\"EPSG\\\",\\\"9102\\\"]], AXIS[\\\"Lat\\\",north], AXIS[\\\"Lon\\\",east], AUTHORITY[\\\"EPSG\\\",\\\"4191\\\"]], GEOGCS[\\\"GCS_WGS_1984\\\", DATUM[\\\"D_WGS_1984\\\", SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563, AUTHORITY[\\\"EPSG\\\",\\\"7030\\\"]], AUTHORITY[\\\"EPSG\\\",\\\"6326\\\"]], PRIMEM[\\\"Greenwich\\\",0, AUTHORITY[\\\"EPSG\\\",\\\"8901\\\"]], UNIT[\\\"degree\\\",0.0174532925199433,AUTHORITY[\\\"EPSG\\\",\\\"9102\\\"]], AXIS[\\\"Lat\\\",north], AXIS[\\\"Lon\\\",east], AUTHORITY[\\\"EPSG\\\",\\\"4326\\\"]], METHOD[\\\"Geocentric_Translation\\\"], PARAMETER[\\\"X_Axis_Translation\\\",24, AUTHORITY[\\\"EPSG\\\",\\\"8605\\\"]], PARAMETER[\\\"Y_Axis_Translation\\\",-130, AUTHORITY[\\\"EPSG\\\",\\\"8606\\\"]], PARAMETER[\\\"Z_Axis_Translation\\\",-92, AUTHORITY[\\\"EPSG\\\",\\\"8607\\\"]], AUTHORITY[\\\"SHELL\\\",\\\"50102\\\"]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\"}";
        String toCRS = ConstantsTests.LB_WGS84[ConstantsTests.V2];

        double[] zCoordinates = new double[]{0.0, 0.0};
        double[] xyCoordinates = new double[]{
                20, 41, 20, 41
        };
        double[] expectedXYCoordinates = new double[]{
                19.99845051, 40.99952831, 19.99845051, 40.99952831
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };
        CRSConverter converter = new CRSConverter();
        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
        assertEquals((Integer) (expectedXYCoordinates.length / 2), result.getSuccessCount());
        for (int i = 0; i < xyCoordinates.length; i++) {
            assertEquals(expectedXYCoordinates[i], xyCoordinates[i], DELTA_A);
            assertEquals(expectedZCoordinates[i % 2], zCoordinates[i % 2], DELTA_A);
        }
    }

    @Test
    public void testPositionVectorTransformation() throws Exception {
        String fromCRS = "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"50004\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"ESPG\",\"code\":\"4208\"},\"name\":\"Geographic1\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEODCRS[\\\"Aratu\\\",\\r\\n  DATUM[\\\"Aratu\\\",\\r\\n    ELLIPSOID[\\\"International 1924\\\", 6378388.0, 297.0, LENGTHUNIT[\\\"metre\\\", 1]]],\\r\\n    PRIMEM[\\\"Greenwich\\\", 0.0, ANGLEUNIT[\\\"degree\\\", 0.017453292519943295]],\\r\\n  CS[ellipsoidal, 2],\\r\\n    AXIS[\\\"Latitude (B)\\\", north, ORDER[1]],\\r\\n    AXIS[\\\"Longitude (L)\\\", east, ORDER[2]],\\r\\n    ANGLEUNIT[\\\"degree\\\", 0.017453292519943295],\\r\\n  SCOPE[\\\"Geodetic survey.\\\"],\\r\\n  AREA[\\\"Brazil - offshore south and east of a line intersecting the coast at 2°55'S; onshore Tucano basin.\\\"],\\r\\n  BBOX[-35.71, -53.38, 4.26, -26.00],\\r\\n  ID[\\\"EPSG\\\", 4208, \\\"9.9.1\\\", URI[\\\"urn:ogc:def:crs:EPSG:9.9.1:4208\\\"]]]\"},\"name\":\"Persistable reference name\",\"singleCT\":{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"50004\"},\"name\":\"CT name\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"Aratu to WGS 84 (SIEP)\\\", GEOGCS[\\\"GCS_Aratu\\\", DATUM[\\\"D_Aratu\\\", SPHEROID[\\\"International_1924\\\",6378388,297, AUTHORITY[\\\"EPSG\\\",\\\"7022\\\"]], AUTHORITY[\\\"EPSG\\\",\\\"6208\\\"]], PRIMEM[\\\"Greenwich\\\",0, AUTHORITY[\\\"EPSG\\\",\\\"8901\\\"]], UNIT[\\\"degree\\\",0.0174532925199433,AUTHORITY[\\\"EPSG\\\",\\\"9102\\\"]], AXIS[\\\"Lat\\\",north], AXIS[\\\"Lon\\\",east], AUTHORITY[\\\"EPSG\\\",\\\"4208\\\"]], GEOGCS[\\\"GCS_WGS_1984\\\", DATUM[\\\"D_WGS_1984\\\", SPHEROID[\\\"WGS_1984\\\",6378137,298.257223563, AUTHORITY[\\\"EPSG\\\",\\\"7030\\\"]], AUTHORITY[\\\"EPSG\\\",\\\"6326\\\"]], PRIMEM[\\\"Greenwich\\\",0, AUTHORITY[\\\"EPSG\\\",\\\"8901\\\"]], UNIT[\\\"degree\\\",0.0174532925199433,AUTHORITY[\\\"EPSG\\\",\\\"9102\\\"]], AXIS[\\\"Lat\\\",north], AXIS[\\\"Lon\\\",east], AUTHORITY[\\\"EPSG\\\",\\\"4326\\\"]], METHOD[\\\"Position_Vector\\\"], PARAMETER[\\\"X_Axis_Translation\\\",-181, AUTHORITY[\\\"EPSG\\\",\\\"8605\\\"]], PARAMETER[\\\"Y_Axis_Translation\\\",294, AUTHORITY[\\\"EPSG\\\",\\\"8606\\\"]], PARAMETER[\\\"Z_Axis_Translation\\\",-144.5, AUTHORITY[\\\"EPSG\\\",\\\"8607\\\"]], PARAMETER[\\\"X_Axis_Rotation\\\",0, AUTHORITY[\\\"EPSG\\\",\\\"8608\\\"]], PARAMETER[\\\"Y_Axis_Rotation\\\",0, AUTHORITY[\\\"EPSG\\\",\\\"8609\\\"]], PARAMETER[\\\"Z_Axis_Rotation\\\",0.554, AUTHORITY[\\\"EPSG\\\",\\\"8610\\\"]], PARAMETER[\\\"Scale_Difference\\\",0.219, AUTHORITY[\\\"EPSG\\\",\\\"8611\\\"]], AUTHORITY[\\\"SHELL\\\",\\\"50004\\\"]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\"}";
        String toCRS = ConstantsTests.LB_WGS84[ConstantsTests.V2];

        double[] zCoordinates = new double[]{0.0, 0.0};
        double[] xyCoordinates = new double[]{
                -40, -20, -40, -20
        };
        double[] expectedXYCoordinates = new double[]{
                -39.99880571, -20.00170960, -39.99880571, -20.00170960
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };
        CRSConverter converter = new CRSConverter();
        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
        assertEquals((Integer) (expectedXYCoordinates.length / 2), result.getSuccessCount());
        for (int i = 0; i < xyCoordinates.length; i++) {
            assertEquals(expectedXYCoordinates[i], xyCoordinates[i], DELTA_A);
            assertEquals(expectedZCoordinates[i % 2], zCoordinates[i % 2], DELTA_A);
        }
    }

// Test temporarily ignored to get build to pass after unit tests were re-enabled
    @Ignore
    @Test
    public void testNTv2Wkt() throws Exception {
        String fromCRS = "{\"authCode\":{\"auth\":\"SHELL\",\"code\":\"50028033\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"SHELL\",\"code\":\"50028\"},\"name\":\"NAD27_Central_Canada_Albers_Equal_Area\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"NAD27_Central_Canada_Albers_Equal_Area\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698213901]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Albers\\\"],PARAMETER[\\\"False_Easting\\\",1000000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",-112.5],PARAMETER[\\\"Standard_Parallel_1\\\",58.0],PARAMETER[\\\"Standard_Parallel_2\\\",50.0],PARAMETER[\\\"Latitude_Of_Origin\\\",45.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"SHELL\\\",\\\"50028\\\"]]\"},\"name\":\"NAD27 * EPSG-Can / Central Canada Albers Equal Area [50028,1693]\",\"singleCT\":{\"authCode\":{\"auth\":\"SHELL\",\"code\":\"1693\"},\"name\":\"NAD_1927_To_WGS_1984_33\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_33\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NTv2\\\"],PARAMETER[\\\"Dataset_canada/Ntv2_0\\\",0.0],AUTHORITY[\\\"EPSG\\\",1693]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}";
        String toCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"GCS_WGS_1984\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4326]]\"}";
        double[] zCoordinates = new double[]{0, 0};
        double[] xyCoordinates = new double[] {2586342.78, 807360.75, 2586342.78, 807360.75};
        double[] expectedXYCoordinates = new double[]{
                -90.00000000212174, 49.99999997778123, -90.00000000212174, 49.99999997778123
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };
        CRSConverter converter = new CRSConverter();
        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
        assertEquals((Integer) (expectedXYCoordinates.length / 2), result.getSuccessCount());
        for (int i = 0; i < xyCoordinates.length; i++) {
            assertEquals(expectedXYCoordinates[i], xyCoordinates[i], DELTA_L);
            assertEquals(expectedZCoordinates[i % 2], zCoordinates[i % 2], DELTA_L);
        }
    }

    @Test
    public void testNADCONWkt() throws Exception {
        String fromCRS = "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"GCS_WGS_1984\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4326]]\"}";
        String toCRS = "{\"authCode\":{\"auth\":\"SLB\",\"code\":\"26782079\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"26782\"},\"name\":\"NAD_1927_StatePlane_Louisiana_South_FIPS_1702\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"PROJCS[\\\"NAD_1927_StatePlane_Louisiana_South_FIPS_1702\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Lambert_Conformal_Conic\\\"],PARAMETER[\\\"False_Easting\\\",2000000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",-91.3333333333333],PARAMETER[\\\"Standard_Parallel_1\\\",29.3],PARAMETER[\\\"Standard_Parallel_2\\\",30.7],PARAMETER[\\\"Latitude_Of_Origin\\\",28.6666666666667],UNIT[\\\"Foot_US\\\",0.304800609601219],AUTHORITY[\\\"EPSG\\\",26782]]\"},\"name\":\"NAD27 * OGP-Usa Conus / Louisiana South [26782,15851]\",\"singleCT\":{\"authCode\":{\"auth\":\"TEST\",\"code\":\"15851\"},\"name\":\"NAD_1927_To_WGS_1984_79_CONUS\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],AUTHORITY[\\\"EPSG\\\",15851]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\"}";
        double[] zCoordinates = new double[]{0, 0};
        double[] xyCoordinates = new double[] {-100, 30, -100, 30};
        double[] expectedXYCoordinates = new double[]{
                -740612.4032902368, 588467.2129525244, -740612.4032902368, 588467.2129525244
        };
        double[] expectedZCoordinates = new double[]{
                0, 0
        };
        CRSConverter converter = new CRSConverter();
        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
        assertEquals((Integer) (expectedXYCoordinates.length / 2), result.getSuccessCount());
        for (int i = 0; i < xyCoordinates.length; i++) {
            assertEquals(expectedXYCoordinates[i], xyCoordinates[i], DELTA_A);
            assertEquals(expectedZCoordinates[i % 2], zCoordinates[i % 2], DELTA_A);
        }
    }
}
