package org.opengroup.osdu.crs.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.opengroup.osdu.crs.model.ConvertPointsResponse;
import org.opengroup.osdu.crs.util.ConstantsTests;

public class CRSConverter3DTests {

    private static final double DELTA_L = 0.00001;

    @Test
    public void convertPointEarlyToEarlyBound() {
        ConvertPointsResponse result;
        CRSConverter converter = new CRSConverter();
        // successful example GCS_Provisional_S_American_1956 -> Trinidad_1903_Trinidad_Grid with transformation via WGS84
        double[] xyCoordinates = new double[]{
                -62.088361835310273, 9.8331328244829646,
                -59.9984596548835, 9.8331230048119114,
                -62.088352818867975, 11.51308347808844,
                -59.9984511768478, 11.513072002985011,
                -61.043406288714543, 10.673103179456877
        };
        double[] zCoordinates = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
        double[] expectedXYCoordinates = new double[]{
                16979.216004341986, -12893.186986758385,
                1156645.1234614472, -11914.127523030565,
                19237.224250591764, 910934.37444776611,
                1152665.7797102598, 912072.47864155506,
                586399.423030929, 448578.26031174022
        };
        double[] expectedZCoordinates = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
        result = converter.convertPoint(
                ConstantsTests.EB_PSAD56_1209[ConstantsTests.V1],
                ConstantsTests.EB_TRINIDAD_10085[ConstantsTests.V1],
                xyCoordinates, zCoordinates);

        assertEquals((Integer)5, result.getSuccessCount());

        for (int i = 0; i < xyCoordinates.length; i++) {
            assertEquals(expectedXYCoordinates[i], xyCoordinates[i], DELTA_L);
            assertEquals(expectedZCoordinates[i / 2], zCoordinates[i / 2], DELTA_L);
        }
    }

    @Test
    public void TestEBToEB_NoZShift_151393() {
        ConvertPointsResponse result;
        double[] xyCoordinates, zCoordinates, expectedXYCoordinates, expectedZCoordinates;
        // PSAD56 * DMA-Ven [4248,1209] [dega]
        String fromCRS = ConstantsTests.EB_PSAD56_4248_1209[ConstantsTests.V1];
        // Trinidad 1903 * EOG-Tto Trin / Trinidad Grid [30200,10085] [lkCla]
        String to_CRS1 = ConstantsTests.EB_Trinidad_30200_10085[ConstantsTests.V1];
        //
        xyCoordinates = new double[]{
                -62.088361835310273, 9.8331328244829646,
                -62.088361835310273, 9.8331328244829646,
                -62.088361835310273, 9.8331328244829646,
                -62.088361835310273, 9.8331328244829646,
                -62.088361835310273, 9.8331328244829646,

                -62.088361835310273, 9.8331328244829646,
                -62.088361835310273, 9.8331328244829646,
                -62.088361835310273, 9.8331328244829646,
                -62.088361835310273, 9.8331328244829646,
                -62.088361835310273, 9.8331328244829646
        };
        expectedXYCoordinates = new double[]{
                16979.216004341986, -12893.186986758385,
                16979.216004341986, -12893.186986758385,
                16979.216004341986, -12893.186986758385,
                16979.216004341986, -12893.186986758385,
                16979.216004341986, -12893.186986758385,

                16979.216004341986, -12893.186986758385,
                16979.216004341986, -12893.186986758385,
                16979.216004341986, -12893.186986758385,
                16979.216004341986, -12893.186986758385,
                16979.216004341986, -12893.186986758385
        };
        zCoordinates = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        expectedZCoordinates = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        CRSConverter converter = new CRSConverter();

        result = converter.convertPoint(fromCRS, to_CRS1, xyCoordinates, zCoordinates);
        assertEquals((Integer)10, result.getSuccessCount()); // all points in US
        for (int i = 0; i < xyCoordinates.length; i++) {
            assertEquals(expectedXYCoordinates[i], xyCoordinates[i], DELTA_L);
            assertEquals(expectedZCoordinates[i / 2], zCoordinates[i / 2], DELTA_L);
        }
    }

    @Test
    public void TestDuplicatePoints() {
        ConvertPointsResponse result;
        double[] xyCoordinates, zCoordinates, expectedXYCoordinates, expectedZCoordinates;

        String fromCRS = "%7B%22LB_CRS%22%3A%22%257B%2522WKT%2522%253A%2522GEOGCS%255B%255C%2522GCS_Provisional_S_American_1956%255C%2522%252CDATUM%255B%255C%2522D_Provisional_S_American_1956%255C%2522%252CSPHEROID%255B%255C%2522International_1924%255C%2522%252C6378388.0%252C297.0%255D%255D%252CPRIMEM%255B%255C%2522Greenwich%255C%2522%252C0.0%255D%252CUNIT%255B%255C%2522Degree%255C%2522%252C0.0174532925199433%255D%252CAUTHORITY%255B%255C%2522EPSG%255C%2522%252C4248%255D%255D%2522%252C%2522Type%2522%253A%2522LBCRS%2522%252C%2522EngineVersion%2522%253A%2522PE_10_3_1%2522%252C%2522AuthorityCode%2522%253A%257B%2522Authority%2522%253A%2522EPSG%2522%252C%2522Code%2522%253A%25224248%2522%257D%252C%2522Name%2522%253A%2522GCS_Provisional_S_American_1956%2522%257D%22%2C%22TRF%22%3A%22%257B%2522WKT%2522%253A%2522GEOGTRAN%255B%255C%2522PSAD_1956_To_WGS_1984_9%255C%2522%252CGEOGCS%255B%255C%2522GCS_Provisional_S_American_1956%255C%2522%252CDATUM%255B%255C%2522D_Provisional_S_American_1956%255C%2522%252CSPHEROID%255B%255C%2522International_1924%255C%2522%252C6378388.0%252C297.0%255D%255D%252CPRIMEM%255B%255C%2522Greenwich%255C%2522%252C0.0%255D%252CUNIT%255B%255C%2522Degree%255C%2522%252C0.0174532925199433%255D%255D%252CGEOGCS%255B%255C%2522GCS_WGS_1984%255C%2522%252CDATUM%255B%255C%2522D_WGS_1984%255C%2522%252CSPHEROID%255B%255C%2522WGS_1984%255C%2522%252C6378137.0%252C298.257223563%255D%255D%252CPRIMEM%255B%255C%2522Greenwich%255C%2522%252C0.0%255D%252CUNIT%255B%255C%2522Degree%255C%2522%252C0.0174532925199433%255D%255D%252CMETHOD%255B%255C%2522Geocentric_Translation%255C%2522%255D%252CPARAMETER%255B%255C%2522X_Axis_Translation%255C%2522%252C-295.0%255D%252CPARAMETER%255B%255C%2522Y_Axis_Translation%255C%2522%252C173.0%255D%252CPARAMETER%255B%255C%2522Z_Axis_Translation%255C%2522%252C-371.0%255D%252CAUTHORITY%255B%255C%2522EPSG%255C%2522%252C1209%255D%255D%2522%252C%2522Type%2522%253A%2522STRF%2522%252C%2522EngineVersion%2522%253A%2522PE_10_3_1%2522%252C%2522AuthorityCode%2522%253A%257B%2522Authority%2522%253A%2522EPSG%2522%252C%2522Code%2522%253A%25221209%2522%257D%252C%2522Name%2522%253A%2522PSAD_1956_To_WGS_1984_9%2522%257D%22%2C%22Type%22%3A%22EBCRS%22%2C%22EngineVersion%22%3A%22PE_10_3_1%22%2C%22Name%22%3A%22PSAD56+*+DMA-Ven+%5B4248%2C1209%5D%22%2C%22AuthorityCode%22%3A%7B%22Authority%22%3A%22SLB%22%2C%22Code%22%3A%224248009%22%7D%7D";
        String toCRS = "%7B%22LB_CRS%22%3A%22%257B%2522WKT%2522%253A%2522PROJCS%255B%255C%2522Trinidad_1903_Trinidad_Grid%255C%2522%252CGEOGCS%255B%255C%2522GCS_Trinidad_1903%255C%2522%252CDATUM%255B%255C%2522D_Trinidad_1903%255C%2522%252CSPHEROID%255B%255C%2522Clarke_1858%255C%2522%252C6378293.64520876%252C294.260676369%255D%255D%252CPRIMEM%255B%255C%2522Greenwich%255C%2522%252C0.0%255D%252CUNIT%255B%255C%2522Degree%255C%2522%252C0.0174532925199433%255D%255D%252CPROJECTION%255B%255C%2522Cassini%255C%2522%255D%252CPARAMETER%255B%255C%2522False_Easting%255C%2522%252C430000.0%255D%252CPARAMETER%255B%255C%2522False_Northing%255C%2522%252C325000.0%255D%252CPARAMETER%255B%255C%2522Central_Meridian%255C%2522%252C-61.3333333333333%255D%252CPARAMETER%255B%255C%2522Scale_Factor%255C%2522%252C1.0%255D%252CPARAMETER%255B%255C%2522Latitude_Of_Origin%255C%2522%252C10.4416666666667%255D%252CUNIT%255B%255C%2522Link_Clarke%255C%2522%252C0.201166195164%255D%252CAUTHORITY%255B%255C%2522EPSG%255C%2522%252C30200%255D%255D%2522%252C%2522Type%2522%253A%2522LBCRS%2522%252C%2522EngineVersion%2522%253A%2522PE_10_3_1%2522%252C%2522AuthorityCode%2522%253A%257B%2522Authority%2522%253A%2522EPSG%2522%252C%2522Code%2522%253A%252230200%2522%257D%252C%2522Name%2522%253A%2522Trinidad_1903_Trinidad_Grid%2522%257D%22%2C%22TRF%22%3A%22%257B%2522WKT%2522%253A%2522GEOGTRAN%255B%255C%2522Trinidad_1903_To_WGS_1984_2%255C%2522%252CGEOGCS%255B%255C%2522GCS_Trinidad_1903%255C%2522%252CDATUM%255B%255C%2522D_Trinidad_1903%255C%2522%252CSPHEROID%255B%255C%2522Clarke_1858%255C%2522%252C6378293.64520876%252C294.260676369%255D%255D%252CPRIMEM%255B%255C%2522Greenwich%255C%2522%252C0.0%255D%252CUNIT%255B%255C%2522Degree%255C%2522%252C0.0174532925199433%255D%255D%252CGEOGCS%255B%255C%2522GCS_WGS_1984%255C%2522%252CDATUM%255B%255C%2522D_WGS_1984%255C%2522%252CSPHEROID%255B%255C%2522WGS_1984%255C%2522%252C6378137.0%252C298.257223563%255D%255D%252CPRIMEM%255B%255C%2522Greenwich%255C%2522%252C0.0%255D%252CUNIT%255B%255C%2522Degree%255C%2522%252C0.0174532925199433%255D%255D%252CMETHOD%255B%255C%2522Geocentric_Translation%255C%2522%255D%252CPARAMETER%255B%255C%2522X_Axis_Translation%255C%2522%252C-61.0%255D%252CPARAMETER%255B%255C%2522Y_Axis_Translation%255C%2522%252C285.2%255D%252CPARAMETER%255B%255C%2522Z_Axis_Translation%255C%2522%252C471.6%255D%252CAUTHORITY%255B%255C%2522EPSG%255C%2522%252C10085%255D%255D%2522%252C%2522Type%2522%253A%2522STRF%2522%252C%2522EngineVersion%2522%253A%2522PE_10_3_1%2522%252C%2522AuthorityCode%2522%253A%257B%2522Authority%2522%253A%2522EPSG%2522%252C%2522Code%2522%253A%252210085%2522%257D%252C%2522Name%2522%253A%2522Trinidad_1903_To_WGS_1984_2%2522%257D%22%2C%22Type%22%3A%22EBCRS%22%2C%22EngineVersion%22%3A%22PE_10_3_1%22%2C%22Name%22%3A%22Trinidad+1903+*+EOG-Tto+Trin+%2F+Trinidad+Grid+%5B30200%2C10085%5D%22%2C%22AuthorityCode%22%3A%7B%22Authority%22%3A%22SLB%22%2C%22Code%22%3A%2230200002%22%7D%7D";

        xyCoordinates = new double[]{
                -62.088361835310273,
                9.8331328244829646,
                -62.088361835310273,
                9.8331328244829646,
                -62.088361835310273,
                9.8331328244829646,
                -62.088361835310273,
                9.8331328244829646,
                -62.088361835310273,
                9.8331328244829646,
                -62.088361835310273,
                9.8331328244829646,
                -62.088361835310273,
                9.8331328244829646,
                -62.088361835310273,
                9.8331328244829646,
                -59.9984596548835,
                9.8331230048119114,
                -62.088352818867975,
                11.51308347808844,
                -59.9984511768478,
                11.513072002985011,
                -61.043406288714543,
                10.673103179456877
        };

        zCoordinates = new double[] {
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0
        };

        expectedXYCoordinates = new double[]{
                16979.216004341986,
                -12893.186986758385,
                16979.216004341986,
                -12893.186986758385,
                16979.216004341986,
                -12893.186986758385,
                16979.216004341986,
                -12893.186986758385,
                16979.216004341986,
                -12893.186986758385,
                16979.216004341986,
                -12893.186986758385,
                16979.216004341986,
                -12893.186986758385,
                16979.216004341986,
                -12893.186986758385,
                1156645.1234614472,
                -11914.127523030565,
                19237.224250591764,
                910934.37444776611,
                1152665.7797102598,
                912072.47864155506,
                586399.423030929,
                448578.26031174022
        };

        expectedZCoordinates = new double[] {
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0
        };
        CRSConverter converter = new CRSConverter();

        result = converter.convertPoint(fromCRS, toCRS, xyCoordinates, zCoordinates);
        assertEquals((Integer)12, result.getSuccessCount());
        for (int i = 0; i < xyCoordinates.length; i++) {
            assertEquals(expectedXYCoordinates[i], xyCoordinates[i], DELTA_L);
            assertTrue(is_close(expectedXYCoordinates[i], xyCoordinates[i]));
            assertEquals(expectedZCoordinates[i / 2], zCoordinates[i / 2], DELTA_L);
            assertTrue(is_close(expectedZCoordinates[i / 2], zCoordinates[i / 2]));
        }

    }

    private boolean is_close(double a, double b) {
        double rel_tol = 0.000000001;
        double abs_tol = 0.0;
        if (Double.isNaN(a) && Double.isNaN(b)) {
            return true;
        }
        if (Double.isNaN(a) || Double.isNaN(b)) {
            return false;
        }
        return Math.abs(a-b) <= Math.max(rel_tol * Math.max(Math.abs(a), Math.abs(b)), abs_tol);

    }

}
