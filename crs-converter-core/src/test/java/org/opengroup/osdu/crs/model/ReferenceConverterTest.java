package org.opengroup.osdu.crs.model;

import org.junit.Ignore;
import org.opengroup.osdu.crs.util.ConstantsTests;
import org.opengroup.osdu.crs.model.v2.LateBoundCrs;
import org.opengroup.osdu.crs.model.v2.PersistableReference;
import org.junit.Test;
import static org.opengroup.osdu.crs.model.ReferenceConverter.parseSpatialReference;
import static org.opengroup.osdu.crs.model.ReferenceConverter.parseUnitReference;

import static junit.framework.TestCase.*;

public class ReferenceConverterTest {

    private static final String V1_START = "%7B";
    private static final String V1_STOP = "%7D";

    @Ignore
    @Test
    public void TestParseSpatialReferenceInvalidV2() {
        IItem result;
        result = parseSpatialReference(ConstantsTests.EB_AGD84_AMG56[ConstantsTests.V2].substring(1));
        assertNotNull(result);
        assertFalse(result.isValid());
        result = parseSpatialReference(ConstantsTests.EB_AGD84_AMG56[ConstantsTests.V2].substring(0, ConstantsTests.EB_AGD84_AMG56[ConstantsTests.V2].length() - 2));
        assertNotNull(result);
        assertFalse(result.isValid());
        result = parseSpatialReference(ConstantsTests.STRF_DGN95_WGS84[ConstantsTests.V2].substring(1));
        assertNotNull(result);
        assertFalse(result.isValid());
        result = parseSpatialReference(ConstantsTests.STRF_DGN95_WGS84[ConstantsTests.V2].substring(0, ConstantsTests.STRF_DGN95_WGS84[ConstantsTests.V2].length() - 2));
        assertNotNull(result);
        assertFalse(result.isValid());
    }

    @Ignore
    @Test
    public void TestParseLateBoundCrsReferenceV1(){
        IItem result;
        result = parseSpatialReference(ConstantsTests.LB_DGN95[ConstantsTests.V1]);
        validateGeographicCrs(result);

        result = parseSpatialReference(ConstantsTests.LB_DGN95_UTM48N[ConstantsTests.V1]);
        validateProjectedCrs(result);

        result = parseSpatialReference((ConstantsTests.LB_Vertical_5773[ConstantsTests.V1]));
        validateVerticalCrs(result);
    }

    private void validateGeographicCrs(IItem result) {
        ILateBoundCrs item;
        assertNotNull(result);
        assertTrue(result instanceof ILateBoundCrs);
        item = (ILateBoundCrs) result;
        assertTrue(item.isValid());
        assertTrue(item.isGeographicCrs());
        assertFalse(item.isProjectedCrs());
        assertFalse(item.isVerticalCrs());
        assertNotNull(item.getName());
        assertNotNull(item.getAuthorityCode());
        assertNotNull(item.getEngineVersion());
        assertEquals(CRSType.LATE_BOUND, item.getType());
    }

    private void validateProjectedCrs(IItem result) {
        ILateBoundCrs item;
        assertNotNull(result);
        assertTrue(result instanceof ILateBoundCrs);
        item = (ILateBoundCrs) result;
        assertTrue(item.isValid());
        assertFalse(item.isGeographicCrs());
        assertTrue(item.isProjectedCrs());
        assertFalse(item.isVerticalCrs());
        assertNotNull(item.getName());
        assertNotNull(item.getAuthorityCode());
        assertNotNull(item.getEngineVersion());
        assertEquals(CRSType.LATE_BOUND, item.getType());
    }

    private void validateVerticalCrs(IItem result) {
        ILateBoundCrs item;
        assertNotNull(result);
        assertTrue(result instanceof ILateBoundCrs);
        item = (ILateBoundCrs) result;
        assertTrue(item.isValid());
        assertFalse(item.isGeographicCrs());
        assertFalse(item.isProjectedCrs());
        assertTrue(item.isVerticalCrs());
        assertNotNull(item.getName());
        assertNotNull(item.getAuthorityCode());
        assertNotNull(item.getEngineVersion());
        assertEquals(CRSType.LATE_BOUND, item.getType());
    }

    @Ignore
    @Test
    public void TestParseLateBoundCrsReferenceV2(){
        IItem result;
        ILateBoundCrs item;

        result = parseSpatialReference(ConstantsTests.LB_DGN95[ConstantsTests.V2]);
        validateGeographicCrs(result);
        // coverage for null AuthorityCode
        PersistableReference pr = PersistableReference.createInstance(ConstantsTests.LB_DGN95[ConstantsTests.V2]);
        assertTrue(pr instanceof LateBoundCrs);
        LateBoundCrs v2 = (LateBoundCrs)pr;
        assertNotNull(v2);
        v2.setAuthorityCode(null);
        result = parseSpatialReference(v2.toJsonString());
        assertNotNull(result);
        assertTrue(result instanceof ILateBoundCrs);
        item = (ILateBoundCrs)result;
        assertTrue(item.isValid());

        result = parseSpatialReference(ConstantsTests.LB_DGN95_UTM48N[ConstantsTests.V2]);
        validateProjectedCrs(result);

        result = parseSpatialReference((ConstantsTests.LB_Vertical_5773[ConstantsTests.V2]));
        validateVerticalCrs(result);
    }

    @Test
    public void TestParseSingleTrfReferenceV1(){
        IItem result;
        result = parseSpatialReference(ConstantsTests.STRF_DGN95_WGS84[ConstantsTests.V1]);
        validateSingleTrf(result);
    }

    private void validateSingleTrf(IItem result) {
        ISingleTrf item;
        assertNotNull(result);
        assertTrue(result instanceof ISingleTrf);
        item = (ISingleTrf) result;
        assertTrue(item.isValid());
        assertNotNull(item.getName());
        assertNotNull(item.getAuthorityCode());
        assertNotNull(item.getEngineVersion());
        assertEquals(CRSType.TRF, item.getType());
    }

    @Test
    public void TestParseSingleTrfReferenceV2(){
        IItem result;
        result = parseSpatialReference(ConstantsTests.STRF_DGN95_WGS84[ConstantsTests.V2]);
        validateSingleTrf(result);
    }

    @Test
    public void TestParseCompoundTrfReferenceV1(){
        IItem result;
        result = parseSpatialReference(ConstantsTests.CTRF_NAD27_FALLBACK[ConstantsTests.V1]);
        validateCompoundTrf(result);
    }

    private void validateCompoundTrf(IItem result) {
        ICompoundTrf item;
        assertNotNull(result);
        assertTrue(result instanceof ICompoundTrf);
        item = (ICompoundTrf) result;
        assertTrue(item.isValid());
        assertNotNull(item.getName());
        assertNotNull(item.getAuthorityCode());
        assertNotNull(item.getEngineVersion());
        assertEquals(CRSType.COMPOUND_TRF, item.getType());
    }

    @Test
    public void TestParseCompoundTrfReferenceV2(){
        IItem result;
        result = parseSpatialReference(ConstantsTests.CTRF_NAD27_FALLBACK[ConstantsTests.V2]);
        validateCompoundTrf(result);
    }

    @Test
    public void TestParseEarlyBoundCrsReferenceV1(){
        IItem result;
        result = parseSpatialReference(ConstantsTests.EB_NAD27_UTM11_Fallback[ConstantsTests.V1]);
        validateEarlyBoundCrs(result);
    }

    private void validateEarlyBoundCrs(IItem result) {
        IEarlyBoundCrs item;
        assertNotNull(result);
        assertTrue(result instanceof IEarlyBoundCrs);
        item = (IEarlyBoundCrs) result;
        assertTrue(item.isValid());
        assertNotNull(item.getName());
        assertNotNull(item.getAuthorityCode());
        assertNotNull(item.getEngineVersion());
        assertEquals(CRSType.EARLY_BOUND, item.getType());
    }

    @Test
    public void TestParseEarlyBoundCrsReferenceV2(){
        IItem result;
        result = parseSpatialReference(ConstantsTests.EB_NAD27_UTM11_Fallback[ConstantsTests.V2]);
        validateEarlyBoundCrs(result);
    }

    @Test
    public void TestUnitV1V2(){
        IItem result;
        result = parseUnitReference(ConstantsTests.UNIT_FT_US_E);
        validateUnit(result);
        result = parseUnitReference(ConstantsTests.UNIT_FT);
        validateUnit(result);
        result = parseUnitReference(ConstantsTests.UNIT_FT_US_E_v2);
        validateUnit(result);
        result = parseUnitReference(ConstantsTests.UNIT_FT_US_v2);
        validateUnit(result);
    }

    private void validateUnit(IItem result){
        IUnit item;
        assertNotNull(result);
        assertTrue(result instanceof IUnit);
        item = (IUnit) result;
        assertTrue(item.isValid());
        assertNotNull(item.getSymbol());
    }

    @Test
    public void TestUnitV1V2Failures() {
        IItem result;
        result = parseUnitReference(ConstantsTests.EB_AGD66_AMG56[ConstantsTests.V1]); // not a unit
        assertNotNull(result);
        assertFalse(result.isValid());
        result = parseUnitReference(ConstantsTests.EB_AGD66_AMG56[ConstantsTests.V1].replace("EBCRS","Corrupted")); // not a unit
        assertNotNull(result);
        assertFalse(result.isValid());
        result = parseUnitReference(ConstantsTests.EB_AGD84_AMG56[ConstantsTests.V2]);
        assertNotNull(result);
        assertFalse(result.isValid());
        result = parseUnitReference(ConstantsTests.EB_AGD66_AMG56[ConstantsTests.V2].replace("LBC","Corrupted")); // not a unit
        assertNotNull(result);
        assertFalse(result.isValid());
        // invalid V2 json
        result = parseUnitReference(ConstantsTests.UNIT_FT_US_E_v2.substring(1));
        assertNotNull(result);
        assertFalse(result.isValid());
        result = parseUnitReference(ConstantsTests.UNIT_FT_US_E_v2.substring(0, ConstantsTests.UNIT_FT_US_E_v2.length()-2));
        assertNotNull(result);
        assertFalse(result.isValid());
        result = parseUnitReference(ConstantsTests.UNIT_FT_US_v2.substring(1));
        assertNotNull(result);
        assertFalse(result.isValid());
        result = parseUnitReference(ConstantsTests.UNIT_FT_US_v2.substring(0, ConstantsTests.UNIT_FT_US_v2.length()-2));
        assertNotNull(result);
        assertFalse(result.isValid());
        // invalid V1
        result = parseUnitReference(ConstantsTests.UNIT_FT_US_E.replace(V1_START, ""));
        assertNotNull(result);
        assertFalse(result.isValid());
        result = parseUnitReference(ConstantsTests.UNIT_FT_US_E.replace(V1_STOP, ""));
        assertNotNull(result);
        assertFalse(result.isValid());
        result = parseUnitReference(ConstantsTests.UNIT_FT_US.replace(V1_START, ""));
        assertNotNull(result);
        assertFalse(result.isValid());
        result = parseUnitReference(ConstantsTests.UNIT_FT_US.replace(V1_STOP, ""));
        assertNotNull(result);
        assertFalse(result.isValid());
    }
}
