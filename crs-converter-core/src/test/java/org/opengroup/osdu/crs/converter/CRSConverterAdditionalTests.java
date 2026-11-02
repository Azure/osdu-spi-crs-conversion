package org.opengroup.osdu.crs.converter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runners.Parameterized;
import org.opengroup.osdu.crs.model.ConvertPointsResponse;
import org.opengroup.osdu.crs.model.Point;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.crs.GeoJson.GeoJsonBase;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.model.ConvertGeoJsonResponse;
import org.opengroup.osdu.crs.model.ConvertPointsResponseV4;
import org.opengroup.osdu.crs.model.IEarlyBoundCrs;
import org.opengroup.osdu.crs.model.ReferenceConverter;
import org.opengroup.osdu.crs.util.ConstantsTests;
import static org.junit.jupiter.api.Assertions.*;


public class CRSConverterAdditionalTests {

    private static final double DELTA_L = 0.001;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        // https://community.opengroup.org/osdu/platform/system/reference/crs-conversion-service/-/issues/177
        return Arrays.asList(new Object[][]{
                //
                // Example 1: EPSG:4230 to EPSG:4326 via EPSG:1612
                {
                        "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"4230023\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4230\"},\"name\":\"GCS_European_1950\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4230]]\"},\"name\":\"ED50 * EPSG-Nor N62 2001 [4230,1612]\",\"singleCT\":{\"authCode\":{\"auth\":\"TEST\",\"code\":\"1612\"},\"name\":\"ED_1950_To_WGS_1984_23\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"ED_1950_To_WGS_1984_23\\\",GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-116.641],PARAMETER[\\\"Y_Axis_Translation\\\",-56.931],PARAMETER[\\\"Z_Axis_Translation\\\",-110.559],PARAMETER[\\\"X_Axis_Rotation\\\",0.893],PARAMETER[\\\"Y_Axis_Rotation\\\",0.921],PARAMETER[\\\"Z_Axis_Rotation\\\",-0.917],PARAMETER[\\\"Scale_Difference\\\",-3.52],OPERATIONACCURACY[1.0],AUTHORITY[\\\"EPSG\\\",1612]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
                        // "opendes:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4326:",
                        "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"GCS_WGS_1984\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4326]]\"}",
                        List.of(new Point(10.00000000, 63.00000000, 0d)),
                        List.of(new Point(9.998475714549945, 62.99967496200938, 0d))
                },
                //
                // Example 2: EPSG:4221 to EPSG:4326 via EPSG:1127
                {
                        "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"4221001\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4221\"},\"name\":\"GCS_Campo_Inchauspe\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_Campo_Inchauspe\\\",DATUM[\\\"D_Campo_Inchauspe\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4221]]\"},\"name\":\"Campo Inchauspe * DMA-Arg [4221,1127]\",\"singleCT\":{\"authCode\":{\"auth\":\"TEST\",\"code\":\"1127\"},\"name\":\"Campo_Inchauspe_To_WGS_1984\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"Campo_Inchauspe_To_WGS_1984\\\",GEOGCS[\\\"GCS_Campo_Inchauspe\\\",DATUM[\\\"D_Campo_Inchauspe\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-148.0],PARAMETER[\\\"Y_Axis_Translation\\\",136.0],PARAMETER[\\\"Z_Axis_Translation\\\",90.0],OPERATIONACCURACY[9.0],AUTHORITY[\\\"EPSG\\\",1127]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
                        // "opendes:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4326:",
                        "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"GCS_WGS_1984\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4326]]\"}",
                        List.of(new Point(-70.00000000, -30.00000000, 0d)),
                        List.of(new Point(-70.00095929, -29.99938902, 0d))
                },
                //
                // Example 3: EPSG:4267 to EPSG:4326 via EPSG:1170
                {
                        "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"4267001\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4267\"},\"name\":\"GCS_North_American_1927\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4267]]\"},\"name\":\"NAD27 * DMA-Carib [4267,1170]\",\"singleCT\":{\"authCode\":{\"auth\":\"TEST\",\"code\":\"1170\"},\"name\":\"NAD_1927_To_WGS_1984_1\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_1\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-3.0],PARAMETER[\\\"Y_Axis_Translation\\\",142.0],PARAMETER[\\\"Z_Axis_Translation\\\",183.0],OPERATIONACCURACY[16.0],AUTHORITY[\\\"EPSG\\\",1170]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
                        // "opendes:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4326:",
                        "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"GCS_WGS_1984\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4326]]\"}",
                        List.of(new Point(-65.00000000, 20.00000000, 0d)),
                        List.of(new Point(-64.99945251, 20.00057704, 0d))
                },
                //
                // Example 4: EPSG:4230 to EPSG:32632 via EPSG:1612
                {
                        "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"4230023\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4230\"},\"name\":\"GCS_European_1950\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4230]]\"},\"name\":\"ED50 * EPSG-Nor N62 2001 [4230,1612]\",\"singleCT\":{\"authCode\":{\"auth\":\"TEST\",\"code\":\"1612\"},\"name\":\"ED_1950_To_WGS_1984_23\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"ED_1950_To_WGS_1984_23\\\",GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-116.641],PARAMETER[\\\"Y_Axis_Translation\\\",-56.931],PARAMETER[\\\"Z_Axis_Translation\\\",-110.559],PARAMETER[\\\"X_Axis_Rotation\\\",0.893],PARAMETER[\\\"Y_Axis_Rotation\\\",0.921],PARAMETER[\\\"Z_Axis_Rotation\\\",-0.917],PARAMETER[\\\"Scale_Difference\\\",-3.52],OPERATIONACCURACY[1.0],AUTHORITY[\\\"EPSG\\\",1612]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
                        // "opendes:reference-data--CoordinateReferenceSystem:Projected:EPSG::32632:",
                        "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32632\"},\"name\":\"WGS_1984_UTM_Zone_32N\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"WGS_1984_UTM_Zone_32N\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",9.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",32632]]\"}",
                        List.of(new Point(10.00000000, 63.00000000, 0d)),
                        List.of(new Point(550574.404, 6985945.655, 0d))
                },
        });
    }

        @Test
        public void v4ExplicitDatumTransformationWorksInBothDirections() {
            IEarlyBoundCrs source = (IEarlyBoundCrs) ReferenceConverter.parseSpatialReference(
                    ConstantsTests.EB_AGD66_AMG56_1108[ConstantsTests.V2]);
            assertTrue(source.isValid());
            String geographic = source.getLateBoundCrs().createPersistableReference();
            String transformation = source.getTrf().createPersistableReference();
            double[] xy = {313152.777, 6346936.496};
            double[] z = {250};
            CRSConverter converter = new CRSConverter();
            ConvertPointsResponseV4 result = converter.convertPointV4(geographic,
                    ConstantsTests.LB_WGS84[ConstantsTests.V2], transformation, xy, z);
            assertEquals(1, result.getSuccessCount());
            assertEquals(151.001, xy[0], 0.001);
            assertEquals(-32.999, xy[1], 0.001);
            assertEquals(250, z[0], 0);
            assertEquals(1, converter.convertPointV4(ConstantsTests.LB_WGS84[ConstantsTests.V2],
                    geographic, transformation, xy, z).getSuccessCount());
            assertArrayEquals(new double[]{313152.777, 6346936.496}, xy, 0.01);
            assertEquals(250, z[0], 0);
        }

        @Test
        public void v4RejectsMalformedRequestsBeforeChangingCoordinates() {
            CRSConverter converter = new CRSConverter();
            String wgs = ConstantsTests.LB_WGS84[ConstantsTests.V2];
            double[] xy = {-117, 42};
            double[] z = {12};
            assertThrows(IllegalArgumentException.class, () -> converter.convertPointV4(null, wgs, null, xy, z));
            assertThrows(IllegalArgumentException.class, () -> converter.convertPointV4(wgs, "", null, xy, z));
            assertThrows(IllegalArgumentException.class, () -> converter.convertPointV4(wgs, wgs, null, null, z));
            assertThrows(IllegalArgumentException.class, () -> converter.convertPointV4(wgs, wgs, null, xy, new double[0]));
            assertThrows(IllegalArgumentException.class, () -> converter.convertPointV4(wgs, wgs, null, xy, new double[2]));
            assertThrows(IllegalArgumentException.class, () -> converter.convertPointV4("invalid", wgs, null, xy, z));
            assertThrows(IllegalArgumentException.class, () -> converter.convertPointV4(wgs, wgs, wgs, xy, z));
            assertThrows(IllegalArgumentException.class, () -> converter.convertPointV4(wgs, wgs, "invalid", xy, z));
            assertArrayEquals(new double[]{-117, 42}, xy, 0);
            assertArrayEquals(new double[]{12}, z, 0);
        }

        @Test
        public void v4GeoJsonRoundTripConvertsHorizontalAndVerticalUnits() {
            GeoJsonFeatureCollection collection = (GeoJsonFeatureCollection) GeoJsonFeatureCollection.createInstance(
                    "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\","
                    + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[-117,42,30.48]},\"properties\":{}}]}");
            CRSConverter converter = new CRSConverter();
            ConvertGeoJsonResponse projected = converter.convertGeoJsonV4(collection,
                    ConstantsTests.LB_WGS84_UTM11N[ConstantsTests.V2], ConstantsTests.UNIT_FT_v2, null);
            assertEquals(1, projected.getSuccessCount());
            assertEquals(GeoJsonBase.GeoJsonVariant.ANY_CRS_GEO_JSON, collection.getGeoJsonVariant());
            assertEquals(500000, collection.extractCoordinates().getXys()[0], 0.001);
            assertEquals(100, collection.extractCoordinates().getZ_s()[0], 1e-8);
            ConvertGeoJsonResponse restored = converter.convertGeoJsonV4(collection,
                    ConstantsTests.LB_WGS84[ConstantsTests.V2], null, null);
            assertEquals(1, restored.getSuccessCount());
            assertEquals(GeoJsonBase.GeoJsonVariant.GEO_JSON, collection.getGeoJsonVariant());
            assertArrayEquals(new double[]{-117, 42}, collection.extractCoordinates().getXys(), 1e-8);
            assertEquals(30.48, collection.extractCoordinates().getZ_s()[0], 1e-8);
            assertNull(collection.getPersistableReferenceCrs());
        }
    @ParameterizedTest
    @MethodSource("data")
    public void convertPoints(String fromCRS, String toCRS, List<Point> requestPoints, List<Point> expectedPoints) {
        CRSConverter converter = new CRSConverter();
        PointConverter pointConverter = new PointConverter();
        double[] xyCoordinates = pointConverter.mergeXYCoordinates(requestPoints);
        double[] zCoordinates = pointConverter.mergeZCoordinates(requestPoints);
        ConvertPointsResponse result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
        assertEquals(Integer.valueOf(1), result.getSuccessCount());
        List<Point> actualPoints = pointConverter.convertValuesToPoints(xyCoordinates, zCoordinates);
        for (int i = 0; i < actualPoints.size(); i++) {
            Point actualPoint = actualPoints.get(i);
            Point expectedPoint = expectedPoints.get(i);
            assertEquals(expectedPoint.getX(), actualPoint.getX(), DELTA_L);
            assertEquals(expectedPoint.getY(), actualPoint.getY(), DELTA_L);
        }
    }

}