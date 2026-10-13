package org.opengroup.osdu.crs.converter;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opengroup.osdu.crs.model.*;
import org.opengroup.osdu.crs.util.ConstantsTests;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;
import static org.opengroup.osdu.crs.model.ReferenceConverter.parseSpatialReference;
import static org.opengroup.osdu.crs.model.ReferenceConverter.parseUnitReference;

@Ignore
public class AzimuthCorrectorTests {

    private static final String CMP_CRS_NOT_SUPPORTED_YET = "%7B%22H_CRS%22%3A%22%257B%2522WKT%2522%253A%2522PROJCS%255B%255C%2522WGS_1984_UTM_Zone_35N%255C%2522%252CGEOGCS%255B%255C%2522GCS_WGS_1984%255C%2522%252CDATUM%255B%255C%2522D_WGS_1984%255C%2522%252CSPHEROID%255B%255C%2522WGS_1984%255C%2522%252C6378137.0%252C298.257223563%255D%255D%252CPRIMEM%255B%255C%2522Greenwich%255C%2522%252C0.0%255D%252CUNIT%255B%255C%2522Degree%255C%2522%252C0.0174532925199433%255D%255D%252CPROJECTION%255B%255C%2522Transverse_Mercator%255C%2522%255D%252CPARAMETER%255B%255C%2522False_Easting%255C%2522%252C500000.0%255D%252CPARAMETER%255B%255C%2522False_Northing%255C%2522%252C0.0%255D%252CPARAMETER%255B%255C%2522Central_Meridian%255C%2522%252C27.0%255D%252CPARAMETER%255B%255C%2522Scale_Factor%255C%2522%252C0.9996%255D%252CPARAMETER%255B%255C%2522Latitude_Of_Origin%255C%2522%252C0.0%255D%252CUNIT%255B%255C%2522Meter%255C%2522%252C1.0%255D%252CAUTHORITY%255B%255C%2522EPSG%255C%2522%252C32635%255D%255D%2522%252C%2522Type%2522%253A%2522LBCRS%2522%252C%2522EngineVersion%2522%253A%2522PE_10_3_1%2522%252C%2522AuthorityCode%2522%253A%257B%2522Authority%2522%253A%2522EPSG%2522%252C%2522Code%2522%253A%252232635%2522%257D%252C%2522Name%2522%253A%2522WGS_1984_UTM_Zone_35N%2522%257D%22%2C%22V_CRS%22%3A%22%257B%2522WKT%2522%253A%2522VERTCS%255B%255C%2522EGM96_Geoid%255C%2522%252CVDATUM%255B%255C%2522EGM96_Geoid%255C%2522%255D%252CPARAMETER%255B%255C%2522Vertical_Shift%255C%2522%252C0.0%255D%252CPARAMETER%255B%255C%2522Direction%255C%2522%252C1.0%255D%252CUNIT%255B%255C%2522Meter%255C%2522%252C1.0%255D%252CAUTHORITY%255B%255C%2522EPSG%255C%2522%252C5773%255D%255D%2522%252C%2522Type%2522%253A%2522LBCRS%2522%252C%2522EngineVersion%2522%253A%2522PE_10_3_1%2522%252C%2522AuthorityCode%2522%253A%257B%2522Authority%2522%253A%2522EPSG%2522%252C%2522Code%2522%253A%25225773%2522%257D%252C%2522Name%2522%253A%2522EGM96_Geoid%2522%257D%22%2C%22Type%22%3A%22CCRS%22%2C%22EngineVersion%22%3A%22PE_10_3_1%22%2C%22Name%22%3A%22WGS+84+%2F+UTM+zone+35N+%2B+EGM96+height%22%2C%22AuthorityCode%22%3A%7B%22Authority%22%3A%22SLB%22%2C%22Code%22%3A%22326355773%22%7D%7D";

    @BeforeClass
    public static void setUp() throws IOException {

    }

    @Test
    public void testCreateProjectionCorrectionSetArguments() {
        Point p = new Point(1786900.22507825, 121755.996216339, 0.0);
        String crs = ConstantsTests.SPCS_27_1702[ConstantsTests.V1];
        IUnit horizontalUnit = parseUnitReference(ConstantsTests.UNIT_FT_US);
        AzimuthCorrector corrector = new AzimuthCorrector();
        ProjectionCorrectionSet pcs;
        pcs = corrector.createProjectionCorrectionSet(crs, p, horizontalUnit);
        assertTrue(pcs.isValid());
        assertEquals(0, corrector.errors.size()); // success
        // invalid CRSs
        crs = ConstantsTests.LB_WGS84[ConstantsTests.V1];
        pcs = corrector.createProjectionCorrectionSet(crs, p, horizontalUnit);
        //not sure why this should be valid, teh crs is wgs 84 but the reference point is not in lat long,
        //so a azimuth equidistant projection cannot be created with that reference point
        //assertTrue(pcs.isValid()); // allowed here
        assertEquals(0, corrector.errors.size());
        crs = ConstantsTests.SPCS_27_1702[ConstantsTests.V1].substring(0, ConstantsTests.SPCS_27_1702[ConstantsTests.V1].length() - 20);
        pcs = corrector.createProjectionCorrectionSet(crs, p, horizontalUnit);
        assertFalse(pcs.isValid());
        assertEquals(1, corrector.errors.size());
        pcs = corrector.createProjectionCorrectionSet(crs, p, horizontalUnit);
        assertFalse(pcs.isValid());
        assertEquals(1, corrector.errors.size());
        crs = ConstantsTests.SPCS_27_1702[ConstantsTests.V1].replace("GEOGCS", "CORRUPTED");
        pcs = corrector.createProjectionCorrectionSet(crs, p, horizontalUnit);
        assertFalse(pcs.isValid());
        assertEquals(1, corrector.errors.size());
        crs = ConstantsTests.VER_CS[ConstantsTests.V1];
        pcs = corrector.createProjectionCorrectionSet(crs, p, horizontalUnit);
        assertFalse(pcs.isValid());
        assertEquals(1, corrector.errors.size());
        crs = CMP_CRS_NOT_SUPPORTED_YET;
        pcs = corrector.createProjectionCorrectionSet(crs, p, horizontalUnit);
        assertFalse(pcs.isValid());
        crs = ConstantsTests.SPCS_27_1702[ConstantsTests.V1]; // ok CRS
        // point issues
        p = new Point();
        pcs = corrector.createProjectionCorrectionSet(crs, p, horizontalUnit);
        assertFalse(pcs.isValid());
        pcs = corrector.createProjectionCorrectionSet(crs, null, horizontalUnit);
        assertFalse(pcs.isValid());
        p = new Point(1786900.22507825, 121755.996216339, 0.0); // ok point
        // horizontalUnit issues
        pcs = corrector.createProjectionCorrectionSet(crs, p, null);
        assertFalse(pcs.isValid());
        pcs = corrector.createProjectionCorrectionSet(crs, p, parseUnitReference(ConstantsTests.UNIT_GRAD));
        assertFalse(pcs.isValid());
        pcs = corrector.createProjectionCorrectionSet(crs, p, new org.opengroup.osdu.crs.model.Impl.Unit());
        assertFalse(pcs.isValid());
    }

    @Test
    public void testCorrectAzimuthArguments()
    {
        double xyCoordinates[] = new double[]{1786900.22507825, 121755.996216339};
        double[] azimuths = new double[]{0};
        String crs = ConstantsTests.SPCS_27_1702[ConstantsTests.V1];
        String azimuthReference = "GN";
        AzimuthCorrector corrector = new AzimuthCorrector();
        int done;
        done = corrector.correctAzimuth(crs, azimuthReference, xyCoordinates, azimuths);
        assertEquals(1, done);
        assertEquals(0, corrector.errors.size());
        // invalid CRSs
        crs = ConstantsTests.LB_WGS84[ConstantsTests.V1];
        done = corrector.correctAzimuth(crs, azimuthReference, xyCoordinates, azimuths);
        assertEquals(0, done);
        assertEquals(1, corrector.errors.size());
        crs = ConstantsTests.SPCS_27_1702[ConstantsTests.V1].substring(0, ConstantsTests.SPCS_27_1702[ConstantsTests.V1].length() - 20);
        done = corrector.correctAzimuth(crs, azimuthReference, xyCoordinates, azimuths);
        assertEquals(0, done);
        assertEquals(1, corrector.errors.size());
        done = corrector.correctAzimuth(null, azimuthReference, xyCoordinates, azimuths);
        assertEquals(0, done);
        assertEquals(1, corrector.errors.size());
        crs = ConstantsTests.SPCS_27_1702[ConstantsTests.V1].replace("GEOGCS", "CORRUPTED");
        done = corrector.correctAzimuth(crs, azimuthReference, xyCoordinates, azimuths);
        assertEquals(0, done);
        assertEquals(1, corrector.errors.size());
        crs = ConstantsTests.VER_CS[ConstantsTests.V1];
        done = corrector.correctAzimuth(crs, azimuthReference, xyCoordinates, azimuths);
        assertEquals(0, done);
        assertEquals(1, corrector.errors.size());
        crs = ConstantsTests.SPCS_27_1702[ConstantsTests.V1]; // ok CRS
        // test arrays
        done = corrector.correctAzimuth(crs, azimuthReference, null, null);
        assertEquals(0, done);
        assertEquals(2, corrector.errors.size());
        done = corrector.correctAzimuth(crs, azimuthReference, null, azimuths);
        assertEquals(0, done);
        assertEquals(1, corrector.errors.size());
        done = corrector.correctAzimuth(crs, azimuthReference, xyCoordinates, null);
        assertEquals(0, done);
        assertEquals(1, corrector.errors.size());
        azimuths = new double[]{0, 0};
        done = corrector.correctAzimuth(crs, azimuthReference, xyCoordinates, azimuths);
        assertEquals(0, done);
        assertEquals(1, corrector.errors.size());
        azimuths = new double[]{0}; // repair
        // test azimuth reference
        azimuthReference = "invalid";
        done = corrector.correctAzimuth(crs, azimuthReference, xyCoordinates, azimuths);
        assertEquals(0, done);
        assertEquals(1, corrector.errors.size());
    }

    @Test
    public void testIogpLondonEye() {
        // Coordinates and grid convergence values from IOGP Geomatics guidance note 373-21
        double[] xyCoordinates = new double[]{699913.0, 5709734.4};
        double[] azimuths = new double[]{0};

        AzimuthCorrector corrector = new AzimuthCorrector();
        int result = corrector.correctAzimuth(ConstantsTests.EPSG25830[ConstantsTests.V1],
                "TRUE_NORTH",
                xyCoordinates, azimuths);
        assertEquals(azimuths.length, result);  // all of the points made it
        double expected = 360.0 - (2.0 + 15.0 / 60.0 + 19.0 / 3600.0);
        assertTrue(Math.abs(azimuths[0] - expected) < 0.0001);

        azimuths[0] = 0;
        result = corrector.correctAzimuth(ConstantsTests.EPSG25830[ConstantsTests.V1],
                "GridNorth",
                xyCoordinates, azimuths);
        assertEquals(azimuths.length, result);  // all of the points made it
        expected = 2.0 + 15.0 / 60.0 + 19.0 / 3600.0;
        assertTrue(Math.abs(azimuths[0] - expected) < 0.0001);
    }

    @Test
    public void testLouisianaSouth29N92W() {
        double xyCoordinates[] = new double[]{1786900.22507825, 121755.996216339};
        double[] azimuths = new double[]{0};

        AzimuthCorrector corrector = new AzimuthCorrector();
        int result = corrector.correctAzimuth(ConstantsTests.SPCS_27_1702[ConstantsTests.V1],
                "TRUE_NORTH",
                xyCoordinates, azimuths);
        assertEquals(azimuths.length, result);  // all of the points made it
        double expected = 0.333278214973604;
        assertTrue(Math.abs(azimuths[0] - expected) < 0.0001);

        // azimuths[0] = 0;
        result = corrector.correctAzimuth(ConstantsTests.SPCS_27_1702[ConstantsTests.V1],
                "GridNorth",
                xyCoordinates, azimuths);
        assertEquals(azimuths.length, result);  // all of the points made it
        assertEquals(0, azimuths[0], 0.0001);
    }

    @Test
    public void testGradBasedCRS() {
        // Coordinates and grid convergence values from IOGP Geomatics guidance note 373-21
        double[] xyCoordinates = new double[]{600000.0, 3200000, 300000, 3200000};
        double[] azimuths = new double[]{0, 0};
        double[] expected = new double[]{0, 357.394246228848};

        AzimuthCorrector corrector = new AzimuthCorrector();
        int result = corrector.correctAzimuth(ConstantsTests.EPSG27573[ConstantsTests.V1],
                "GridNorth",
                xyCoordinates, azimuths);
        assertEquals(azimuths.length, result);  // all of the points made it
        for (int i = 0; i < azimuths.length; i++) {
            assertTrue(Math.abs(azimuths[i] - expected[i]) < 0.0001);
        }
    }


    @Test
    public void testCorrectAzimuth() {
        double[] xyCoordinates = new double[]{
                212172.22206646437, 3382455.3819633052,
                500000.0, 6225000.3891823227};
        double[] azimuths = new double[]{360, 0};
        AzimuthCorrector corrector = new AzimuthCorrector();

        int result = corrector.correctAzimuth(ConstantsTests.EB_NAD83_UTM10N_1188[ConstantsTests.V1],
                "TRUE_NORTH",
                xyCoordinates, azimuths);
        assertEquals(azimuths.length, result);  // all of the points made it
    }

    @Test
    public void testAzimuthalEquidistantGeographic() {
        double[] xyCoordinates = new double[]{-92.0, 29.0};
        AzimuthCorrector corrector = new AzimuthCorrector();
        IItem crs = parseSpatialReference(ConstantsTests.NAD27[ConstantsTests.V1]);
        ILateBoundCrs gcs = (ILateBoundCrs) crs;
        assertNotNull(gcs);
        assertTrue(gcs.isValid());
        IUnit hzu = parseUnitReference(ConstantsTests.UNIT_FT_US_E);

    }

    @Test
    public void testProjectionCorrectionSet() {
        double[] xyCoordinates = new double[]{-92.0, 29.0};
        Point rp = new Point(-92.0, 29.0, 0.0);
        AzimuthCorrector corrector = new AzimuthCorrector();
        IItem crs = parseSpatialReference(ConstantsTests.NAD27[ConstantsTests.V1]);
        ILateBoundCrs gcs = (ILateBoundCrs) crs;
        assertNotNull(gcs);
        assertTrue(gcs.isValid());
        IUnit hzu = parseUnitReference(ConstantsTests.UNIT_FT_US_E);
        ProjectionCorrectionSet pcs = corrector.createProjectionCorrectionSet(ConstantsTests.NAD27[ConstantsTests.V1], rp, hzu);
        assertNotNull(pcs);
        assertTrue(pcs.isValid());
        String pr = pcs.azimuthalEquidistantPersistableReference();
        assertNotNull(pr);
        crs = parseSpatialReference(ConstantsTests.SPCS_27_1702[ConstantsTests.V1]);
        assertNotNull(crs);
        assertTrue(crs.isValid());
        rp = new Point(1786900.22507825, 121755.996216339, 0.0);
        pcs = corrector.createProjectionCorrectionSet(ConstantsTests.SPCS_27_1702[ConstantsTests.V1], rp, hzu);
        assertNotNull(pcs);
        assertTrue(pcs.isValid());
        pr = pcs.azimuthalEquidistantPersistableReference();
        assertNotNull(pr);
    }
}