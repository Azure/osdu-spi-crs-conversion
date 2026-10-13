package org.opengroup.osdu.crs.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CRSTypeTests {

	@Test
	public void toStringTest() {
		assertEquals("EBCRS", CRSType.EARLY_BOUND.toString());
		assertEquals("LBCRS", CRSType.LATE_BOUND.toString());
		assertEquals("STRF", CRSType.TRF.toString());
		assertEquals("CTRF", CRSType.COMPOUND_TRF.toString());
	}
}
