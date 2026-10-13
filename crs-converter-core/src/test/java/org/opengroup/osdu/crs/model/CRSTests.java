package org.opengroup.osdu.crs.model;

import org.junit.Ignore;
import org.opengroup.osdu.crs.model.Impl.AuthorityCode;
import org.opengroup.osdu.crs.util.Constants;
import org.opengroup.osdu.crs.util.ConstantsTests;
import junit.framework.TestCase;
import org.junit.Test;

import static org.opengroup.osdu.crs.model.ReferenceConverter.parseSpatialReference;
import static org.junit.Assert.*;

@Ignore
public class CRSTests {

	@Test
	public void testProjectedLateBoundCRS() {

		IItem crs = parseSpatialReference(ConstantsTests.LB_DGN95_UTM48N[ConstantsTests.V1]);

		assertTrue(crs instanceof org.opengroup.osdu.crs.model.ILateBoundCrs);

		ILateBoundCrs lateBoundCRS = (ILateBoundCrs) crs;
		assertNotNull(lateBoundCRS.getAuthorityCode());
		assertNotNull(lateBoundCRS.getEngineVersion());
		assertNotNull(lateBoundCRS.getName());
		assertNotNull(lateBoundCRS.getType());
		assertNotNull(lateBoundCRS.getWellKnownText());
		assertTrue(lateBoundCRS.getWellKnownText().startsWith("PROJCS"));
		assertTrue(lateBoundCRS.isValid());
		// assertNotNull(lateBoundCRS.getProjectedCrs());
		// assertNotNull(lateBoundCRS.getBaseGeographicCrs());
		assertFalse(lateBoundCRS.isGeographicCrs());
		assertTrue(lateBoundCRS.isProjectedCrs());
		assertFalse(lateBoundCRS.isVerticalCrs());
        testLateBoundPersistableReference(lateBoundCRS, true);
        crs = parseSpatialReference(ConstantsTests.LB_DGN95_UTM48N[ConstantsTests.V1].replace("SPHEROID", "Corrupted"));
        lateBoundCRS = (ILateBoundCrs) crs;
        assertFalse(lateBoundCRS.isValid());
        assertFalse(lateBoundCRS.isVerticalCrs());
        assertFalse(lateBoundCRS.isGeographicCrs());
        assertFalse(lateBoundCRS.isProjectedCrs());
        testLateBoundPersistableReference(lateBoundCRS, false);
	}

	@Test
	public void testGeographicLateBoundCRS() {

		IItem crs = parseSpatialReference(ConstantsTests.LB_DGN95[ConstantsTests.V1]);

		assertTrue(crs instanceof ILateBoundCrs);

		ILateBoundCrs lateBoundCRS = (ILateBoundCrs)crs;
		assertNotNull(lateBoundCRS.getAuthorityCode());
		assertNotNull(lateBoundCRS.getEngineVersion());
		assertNotNull(lateBoundCRS.getName());
		assertNotNull(lateBoundCRS.getType());
		assertNotNull(lateBoundCRS.getWellKnownText());
		assertTrue(lateBoundCRS.getWellKnownText().startsWith("GEOGCS"));
		assertTrue(lateBoundCRS.isValid());
		// assertNull(lateBoundCRS.getProjectedCrs());
		// assertNotNull(lateBoundCRS.getBaseGeographicCrs());
		assertTrue(lateBoundCRS.isGeographicCrs());
		assertFalse(lateBoundCRS.isProjectedCrs());
		assertFalse(lateBoundCRS.isVerticalCrs());
        testLateBoundPersistableReference(lateBoundCRS, true);
		crs = parseSpatialReference(ConstantsTests.LB_DGN95[ConstantsTests.V1].replace("SPHEROID", "Corrupted"));
        lateBoundCRS = (ILateBoundCrs) crs;
		assertFalse(lateBoundCRS.isValid());
        assertFalse(lateBoundCRS.isVerticalCrs());
        assertFalse(lateBoundCRS.isGeographicCrs());
        assertFalse(lateBoundCRS.isProjectedCrs());
        testLateBoundPersistableReference(lateBoundCRS, false);
	}

	@Test
	public void testVerticalLateBoundCRS() {
		IItem crs = parseSpatialReference(ConstantsTests.LB_Vertical_5773[ConstantsTests.V1]);
		ILateBoundCrs lateBoundCRS = (ILateBoundCrs) crs;
		assertNotNull(lateBoundCRS.getAuthorityCode());
		assertNotNull(lateBoundCRS.getEngineVersion());
		assertNotNull(lateBoundCRS.getName());
		assertNotNull(lateBoundCRS.getType());
		assertNotNull(lateBoundCRS.getWellKnownText());
		assertTrue(lateBoundCRS.getWellKnownText().startsWith("VERT"));
		assertTrue(lateBoundCRS.isValid());
		// assertNull(lateBoundCRS.getProjectedCrs());
		// assertNull(lateBoundCRS.getBaseGeographicCrs());
		// assertNotNull(lateBoundCRS.getVerticalCrs());
		assertFalse(lateBoundCRS.isGeographicCrs());
		assertFalse(lateBoundCRS.isProjectedCrs());
		assertTrue(lateBoundCRS.isVerticalCrs());
        testLateBoundPersistableReference(lateBoundCRS, true);
		crs = parseSpatialReference(ConstantsTests.LB_Vertical_5773[ConstantsTests.V1].replace("VDATUM", "Corrupted"));
		lateBoundCRS = (ILateBoundCrs) crs;
		assertFalse(lateBoundCRS.isValid());
		assertFalse(lateBoundCRS.isVerticalCrs());
		assertFalse(lateBoundCRS.isGeographicCrs());
		assertFalse(lateBoundCRS.isProjectedCrs());
        testLateBoundPersistableReference(lateBoundCRS, false);
	}

    private void testLateBoundPersistableReference(ICrs candidate, boolean expectedOk) {
        String pr1 = candidate.createPersistableReference();
        assertEquals(expectedOk, !pr1.isEmpty());
        if (expectedOk) { // try round-trip with null authority code
            IItem raw  = parseSpatialReference(pr1);
            assertTrue(raw instanceof ICrs);
            ICrs crs = (ICrs)raw;
            crs.setAuthorityCode(null);
            String pr2 = crs.createPersistableReference();
            assertFalse(pr2.isEmpty());
            raw = parseSpatialReference(pr2);
            assertTrue(raw instanceof ICrs);
            crs = (ICrs)raw;
            assertTrue(crs.isValid());
        }
    }

	@Test
	public void testSingleTransform() {
		IItem crs = parseSpatialReference(ConstantsTests.STRF_DGN95_WGS84[ConstantsTests.V1]);

		assertTrue(crs instanceof ISingleTrf);

		ISingleTrf singleTrf = (ISingleTrf) crs;
		assertNotNull(singleTrf.getAuthorityCode());
		assertNotNull(singleTrf.getEngineVersion());
		assertNotNull(singleTrf.getName());
		assertNotNull(singleTrf.getType());
		assertNotNull(singleTrf.getWellKnownText());
		assertTrue(singleTrf.getWellKnownText().startsWith("GEOGTRAN"));
		assertTrue(singleTrf.isValid());
		// assertNotNull(singleTrf.getFromGeographicCrs());
		// assertNotNull(singleTrf.getToGeographicCrs());

		// create a new persistable reference (V2 exclusively)
        String pr1 = singleTrf.createPersistableReference();
        assertFalse(pr1.isEmpty());
        crs = parseSpatialReference(pr1);
        assertTrue(crs instanceof ISingleTrf);
        ISingleTrf oTrf = (ISingleTrf) crs;
        assertEquals(singleTrf.getName(), oTrf.getName());
        assertEquals(singleTrf.getWellKnownText(), oTrf.getWellKnownText());
        assertEquals(singleTrf.getEngineVersion(), oTrf.getEngineVersion());
        assertEquals(singleTrf.getAuthorityCode().getAuthority(), oTrf.getAuthorityCode().getAuthority());
        assertEquals(singleTrf.getAuthorityCode().getCode(), oTrf.getAuthorityCode().getCode());
        // assertTrue(singleTrf.equalInBehavior(oTrf));
        // assertTrue(oTrf.equalInBehavior(singleTrf));

        singleTrf.setAuthorityCode(null);
        String pr2 = singleTrf.createPersistableReference();
        crs = parseSpatialReference(pr2);
        assertTrue(crs instanceof ISingleTrf);
        oTrf = (ISingleTrf) crs;
        assertFalse(pr2.isEmpty());
        assertEquals(singleTrf.getName(), oTrf.getName());
        assertEquals(singleTrf.getWellKnownText(), oTrf.getWellKnownText());
        assertEquals(singleTrf.getEngineVersion(), oTrf.getEngineVersion());
        assertNull(null, oTrf.getAuthorityCode());
        // assertTrue(singleTrf.equalInBehavior(oTrf));
        // assertTrue(oTrf.equalInBehavior(singleTrf));

		// prepare an invalid WKT - first start with a valid one
		String trf_pr = ConstantsTests.STRF_Egy_WGS84[ConstantsTests.V1];
		crs = parseSpatialReference(trf_pr);
		ISingleTrf trf = (ISingleTrf)crs;
		TestCase.assertTrue(trf.isValid());
		// make the WKT invalid by removing the last ']]'
		crs = parseSpatialReference(trf_pr.replace("15846%5D%5D", ""));  // destroy the WKT
		trf = (ISingleTrf) crs;
		assertFalse(trf.isValid());
		// Empty persistable model
		pr1 = trf.createPersistableReference();
		assertEquals("", pr1);

		// assertFalse(trf.equalInBehavior(oTrf));
		// assertFalse(oTrf.equalInBehavior(trf));
	}

	@Test
	public void testEarlyBoundCRS() {
		IItem crs = parseSpatialReference(ConstantsTests.EB_AGD66_AMG56_NTv2[ConstantsTests.V1]);

		assertTrue(crs instanceof IEarlyBoundCrs);

		IEarlyBoundCrs earlyBoundCRS = (IEarlyBoundCrs) crs;
		assertTrue(earlyBoundCRS.isValid());
		assertNotNull(earlyBoundCRS.getAuthorityCode());
		assertNotNull(earlyBoundCRS.getEngineVersion());
		assertNotNull(earlyBoundCRS.getName());
		assertNotNull(earlyBoundCRS.getType());
		assertNotNull(earlyBoundCRS.getLateBoundCrs().getAuthorityCode());
		assertNotNull(earlyBoundCRS.getLateBoundCrs().getEngineVersion());
		assertNotNull(earlyBoundCRS.getLateBoundCrs().getName());
		assertNotNull(earlyBoundCRS.getLateBoundCrs().getType());
		assertNotNull(earlyBoundCRS.getLateBoundCrs().getWellKnownText());
		assertNotNull(earlyBoundCRS.getLateBoundCrs().getAuthorityCode());
		assertNotNull(earlyBoundCRS.getTrf().getAuthorityCode());
		assertNotNull(earlyBoundCRS.getTrf().getEngineVersion());
		assertNotNull(earlyBoundCRS.getTrf().getName());
		assertNotNull(earlyBoundCRS.getTrf().getType());
		assertNotNull(earlyBoundCRS.getTrf().getAuthorityCode());
		assertTrue(earlyBoundCRS.getLateBoundCrs().isValid());
		assertTrue(earlyBoundCRS.getTrf().isValid());
		assertFalse(earlyBoundCRS.isGeographicCrs());
		assertTrue(earlyBoundCRS.isProjectedCrs());
		assertFalse(earlyBoundCRS.isVerticalCrs());

		String pr1 = earlyBoundCRS.createPersistableReference();
		assertFalse(pr1.isEmpty());
		crs = parseSpatialReference(pr1);
        assertTrue(crs instanceof IEarlyBoundCrs);
        IEarlyBoundCrs eb2 = (IEarlyBoundCrs) crs;
        assertTrue(eb2.isValid());

        ILateBoundCrs lb = earlyBoundCRS.getLateBoundCrs();
        earlyBoundCRS.setLateBoundCrs(null);
        assertFalse(earlyBoundCRS.isValid());
        assertTrue(earlyBoundCRS.createPersistableReference().isEmpty());
        earlyBoundCRS.setLateBoundCrs(lb);
        ITrf trf = earlyBoundCRS.getTrf();
        earlyBoundCRS.setTrf(null);
        assertFalse(earlyBoundCRS.isValid());
        assertTrue(earlyBoundCRS.createPersistableReference().isEmpty());
        earlyBoundCRS.setTrf(trf);
        trf.setAuthorityCode(null);
        assertTrue(earlyBoundCRS.isValid());
        assertFalse(earlyBoundCRS.createPersistableReference().isEmpty());
        earlyBoundCRS.setTrf(trf);
        trf.setAuthorityCode(new AuthorityCode("", ""));
        assertTrue(earlyBoundCRS.isValid());
        assertFalse(earlyBoundCRS.createPersistableReference().isEmpty());

        crs = parseSpatialReference(ConstantsTests.EB_NAD27_UTM12_Fallback[ConstantsTests.V1]);

        assertTrue(crs instanceof IEarlyBoundCrs);

        earlyBoundCRS = (IEarlyBoundCrs) crs;
        assertTrue(earlyBoundCRS.isValid());
        assertNotNull(earlyBoundCRS.getAuthorityCode());
        assertNotNull(earlyBoundCRS.getEngineVersion());
        assertNotNull(earlyBoundCRS.getName());
        assertNotNull(earlyBoundCRS.getType());
        assertNotNull(earlyBoundCRS.getLateBoundCrs().getAuthorityCode());
        assertNotNull(earlyBoundCRS.getLateBoundCrs().getEngineVersion());
        assertNotNull(earlyBoundCRS.getLateBoundCrs().getName());
        assertNotNull(earlyBoundCRS.getLateBoundCrs().getType());
        assertNotNull(earlyBoundCRS.getLateBoundCrs().getWellKnownText());
        assertNotNull(earlyBoundCRS.getLateBoundCrs().getAuthorityCode());
        assertNotNull(earlyBoundCRS.getTrf().getAuthorityCode());
        assertNotNull(earlyBoundCRS.getTrf().getEngineVersion());
        assertNotNull(earlyBoundCRS.getTrf().getName());
        assertNotNull(earlyBoundCRS.getTrf().getType());
        assertNotNull(earlyBoundCRS.getTrf().getAuthorityCode());
        assertTrue(earlyBoundCRS.getLateBoundCrs().isValid());
        assertTrue(earlyBoundCRS.getTrf().isValid());
        assertFalse(earlyBoundCRS.isGeographicCrs());
        assertTrue(earlyBoundCRS.isProjectedCrs());
        assertFalse(earlyBoundCRS.isVerticalCrs());
        pr1 = earlyBoundCRS.createPersistableReference();
        assertFalse(pr1.isEmpty());
        crs = parseSpatialReference(pr1);
        assertTrue(crs instanceof IEarlyBoundCrs);
        eb2 = (IEarlyBoundCrs) crs;
        assertTrue(eb2.isValid());

        lb = earlyBoundCRS.getLateBoundCrs();
        earlyBoundCRS.setLateBoundCrs(null);
        assertFalse(earlyBoundCRS.isValid());
        assertTrue(earlyBoundCRS.createPersistableReference().isEmpty());
        earlyBoundCRS.setLateBoundCrs(lb);
        trf = earlyBoundCRS.getTrf();
        earlyBoundCRS.setTrf(null);
        assertFalse(earlyBoundCRS.isValid());
        assertTrue(earlyBoundCRS.createPersistableReference().isEmpty());
        earlyBoundCRS.setTrf(trf);
        trf.setAuthorityCode(null);
        assertTrue(earlyBoundCRS.isValid());
        assertFalse(earlyBoundCRS.createPersistableReference().isEmpty());
        earlyBoundCRS.setTrf(trf);
        trf.setAuthorityCode(new AuthorityCode("", ""));
        assertTrue(earlyBoundCRS.isValid());
        assertFalse(earlyBoundCRS.createPersistableReference().isEmpty());

    }

	@Test
	public void testEarlyBoundWithCompoundTransform() {
		IItem crs = parseSpatialReference(ConstantsTests.EB_NAD27_UTM11_Fallback[ConstantsTests.V1]);

		assertTrue(crs instanceof IEarlyBoundCrs);

		IEarlyBoundCrs earlyBoundCRS = (IEarlyBoundCrs) crs;
		assertNotNull(earlyBoundCRS.getAuthorityCode());
		assertNotNull(earlyBoundCRS.getEngineVersion());
		assertNotNull(earlyBoundCRS.getName());
		assertNotNull(earlyBoundCRS.getType());
		assertNotNull(earlyBoundCRS.getLateBoundCrs().getAuthorityCode());
		assertNotNull(earlyBoundCRS.getLateBoundCrs().getEngineVersion());
		assertNotNull(earlyBoundCRS.getLateBoundCrs().getName());
		assertNotNull(earlyBoundCRS.getLateBoundCrs().getType());
		assertNotNull(earlyBoundCRS.getLateBoundCrs().getWellKnownText());
		assertNotNull(earlyBoundCRS.getLateBoundCrs().getAuthorityCode());
		assertNotNull(earlyBoundCRS.getTrf().getAuthorityCode());
		assertTrue(earlyBoundCRS.isValid());
		assertTrue(earlyBoundCRS.getTrf() instanceof ICompoundTrf);
		ICompoundTrf trf = (ICompoundTrf) earlyBoundCRS.getTrf();
		for (ISingleTrf t : trf.getTransformations()) {
			assertNotNull(t);
			assertNotNull(t.getWellKnownText());
			assertTrue(t.isValid());
			assertTrue(t.getWellKnownText().startsWith("GEOGTRAN"));
		}
		String trf_pr = trf.createPersistableReference();
		IItem trf_2 = parseSpatialReference(trf_pr);
		assertTrue(trf_2 instanceof ICompoundTrf);
//		assertTrue(trf.equalInBehavior(trf_2));
		assertNotNull(earlyBoundCRS.getTrf().getAuthorityCode());

		crs = parseSpatialReference(ConstantsTests.EB_G1_Invalid_Fallback[ConstantsTests.V1]);

		assertTrue(crs instanceof IEarlyBoundCrs);
		earlyBoundCRS = (IEarlyBoundCrs) crs;
		try {
			assertFalse(earlyBoundCRS.isValid());
		}
		catch (IllegalArgumentException e){
			assertEquals(e.getMessage(), Constants.ERROR_MSG_INCOHERENT_BOUND_TRFS);
		}
		crs = parseSpatialReference(ConstantsTests.EB_G1_Valid_G1G2_Fallback[ConstantsTests.V1]);

		assertTrue(crs instanceof IEarlyBoundCrs);
		earlyBoundCRS = (IEarlyBoundCrs) crs;
		assertTrue(earlyBoundCRS.isValid());
		trf_pr = trf.createPersistableReference();
		trf_2 = parseSpatialReference(trf_pr);
        assertTrue(trf_2 instanceof ICompoundTrf);
 //       assertTrue(trf.equalInBehavior(trf_2));
        assertNotNull(earlyBoundCRS.getTrf().getAuthorityCode());
	}

	@Test
    public void testCompoundTrf() {
        IItem trf = parseSpatialReference(ConstantsTests.CTRF_NAD27_FALLBACK[ConstantsTests.V1]);
        assertNotNull(trf);
        assertTrue(trf instanceof ICompoundTrf);
        ICompoundTrf compoundTrf = (ICompoundTrf) trf;
        assertTrue(compoundTrf.isValid());
        assertTrue(compoundTrf.isFallback());
        String pr1 = compoundTrf.createPersistableReference();
        compoundTrf.setAuthorityCode(null);
        String pr2 = compoundTrf.createPersistableReference();
        assertFalse(pr2.isEmpty());
        trf = parseSpatialReference(pr1);
        assertNotNull(trf);
        assertTrue(trf instanceof ICompoundTrf);
        ICompoundTrf trf1 = (ICompoundTrf)trf;
        assertTrue(trf1.isValid());
        compoundTrf.setAuthorityCode(new AuthorityCode("", ""));
        pr2 = compoundTrf.createPersistableReference();
        trf = parseSpatialReference(pr1);
        assertNotNull(trf);
        assertTrue(trf instanceof ICompoundTrf);
        trf1 = (ICompoundTrf)trf;
        assertTrue(trf1.isValid());

        trf = parseSpatialReference(pr2);
        assertNotNull(trf);
        assertTrue(trf instanceof ICompoundTrf);
        ICompoundTrf trf2 = (ICompoundTrf)trf;
        assertTrue(trf2.isValid());
        // assertTrue(trf1.equalInBehavior(trf2));
        // assertTrue(trf2.equalInBehavior(trf1));
        // assertTrue(compoundTrf.equalInBehavior(trf2));

        trf = parseSpatialReference(pr2.replace("SPHEROID", "Corrupted"));
        assertNotNull(trf);
        assertTrue(trf instanceof ICompoundTrf);
        trf2 = (ICompoundTrf)trf;
        pr2 = trf2.createPersistableReference();
        assertTrue(pr2.isEmpty());
    }

	@Test
	public void testInstanceInvalidEncodedString() {

		try {
			String invalidString = "%7B%22LB_CRS%22%3A%22%257B%2522WKT%2522%253A%2522PROJ255C%2522Greenwich%255C%2522%252C0.0%255D%252CUNIT%255B%255C%2522Degree%255C%2522%252C0.0174532925199433%255D%255D%252CPROJECTION%255B%255C%2522Transverse_Mercator%255C%2522%255D%252CPARAMETER%255B%255C%2522False_Easting%255C%2522%252C500000.0%255D%252CPARAMETER%255B%255C%2522False_Northing%255C%2522%252C10000000.0%255D%252CPARAMETER%255B%255C%2522Central_Meridian%255C%2522%252C153.0%255D%252CPARAMETER%255B%255C%2522Scale_Factor%255C%2522%252C0.9996%255D%252CPARAMETER%255B%255C%2522Latitude_Of_Origin%255C%2522%252C0.0%255D%252CUNIT%255B%255C%2522Meter%255C%2522%252C1.0%255D%252CAUTHORITY%255B%255C%2522EPSG%255C%2522%252C20256%255D%255D%2522%252C%2522Type%2522%253A%2522LBCRS%2522%252C%2522EngineVersion%2522%253A%2522PE_10_3_1%2522%252C%2522AuthorityCode%2522%253A%257B%2522Authority%2522%253A%2522EPSG%2522%252C%2522Code%2522%253A%252220256%2522%257D%252C%2522Name%2522%253A%2522AGD_1966_AMG_Zone_56%2522%257D%22%2C%22TRF%22%3A%22%257B%2522WKT%2522%253A%2522GEOGTRAN%255B%255C%2522AGD_1966_To_WGS_1984_17_NTv2%255C%2522%252CGEOGCS%255B%255C%2522GCS_Australian_1966%255C%2522%252CDATUM%255B%255C%2522D_Australian_1966%255C%2522%252CSPHEROID%255B%255C%2522Australian%255C%2522%252C6378160.0%252C298.25%255D%255D%252CPRIMEM%255B%255C%2522Greenwich%255C%2522%252C0.0%255D%252CUNIT%255B%255C%2522Degree%255C%2522%252C0.0174532925199433%255D%255D%252CGEOGCS%255B%255C%2522GCS_WGS_1984%255C%2522%252CDATUM%255B%255C%2522D_WGS_1984%255C%2522%252CSPHEROID%255B%255C%2522WGS_1984%255C%2522%252C6378137.0%252C298.257223563%255D%255D%252CPRIMEM%255B%255C%2522Greenwich%255C%2522%252C0.0%255D%252CUNIT%255B%255C%2522Degree%255C%2522%252C0.0174532925199433%255D%255D%252CMETHOD%255B%255C%2522NTv2%255C%2522%255D%252CPARAMETER%255B%255C%2522Dataset_australia%252FA66_National_13_09_01%255C%2522%252C0.0%255D%252CAUTHORITY%255B%255C%2522EPSG%255C%2522%252C15786%255D%255D%2522%252C%2522Type%2522%253A%2522STRF%2522%252C%2522EngineVersion%2522%253A%2522PE_10_3_1%2522%252C%2522AuthorityCode%2522%253A%257B%2522Authority%2522%253A%2522EPSG%2522%252C%2522Code%2522%253A%252215786%2522%257D%252C%2522Name%2522%253A%2522AGD_1966_To_WGS_1984_17_NTv2%2522%257D%22%2C%22Type%22%3A%22EBCRS%22%2C%22EngineVersion%22%3A%22PE_10_3_1%22%2C%22Name%22%3A%22AGD66+*+OGP-Aus+0.1m+%2F+AMG+zone+56+%5B20256%2C15786%5D%22%2C%22AuthorityCode%22%3A%7B%22Authority%22%3A%22SLB%22%2C%22Code%22%3A%2220256017%22%7D%7D";
            parseSpatialReference(invalidString);
		}
		catch (IllegalArgumentException e){
			assertTrue(e.getMessage().startsWith(Constants.ERROR_MSG_JSON_PARSE));
		}
	}

	@Test
    public void testAuthorityCode(){
	    AuthorityCode ac = new AuthorityCode();
	    assertNotNull(ac);
	    assertFalse(ac.isDefined());
	    ac = new AuthorityCode("", "code");
        assertFalse(ac.isDefined());
        ac = new AuthorityCode("authority", "");
        assertFalse(ac.isDefined());
        ac = new AuthorityCode("authority", "code");
        assertTrue(ac.isDefined());
    }
}
