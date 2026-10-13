package org.opengroup.osdu.crs.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AzimuthReferenceTypeTests {
	@Test
	public void TestGetAzimuthReferenceTN() {
		assertEquals(AzimuthReferenceType.TRUE_NORTH, AzimuthReferenceType.getAzimuthReference("TN"));
		assertEquals(AzimuthReferenceType.TRUE_NORTH, AzimuthReferenceType.getAzimuthReference("TrueN"));
		assertEquals(AzimuthReferenceType.TRUE_NORTH, AzimuthReferenceType.getAzimuthReference("TRUE_NORTH"));
	}

	@Test
	public void TestGetAzimuthReferenceGN() {
		assertEquals(AzimuthReferenceType.GRID_NORTH, AzimuthReferenceType.getAzimuthReference("GN"));
		assertEquals(AzimuthReferenceType.GRID_NORTH, AzimuthReferenceType.getAzimuthReference("GridN"));
		assertEquals(AzimuthReferenceType.GRID_NORTH, AzimuthReferenceType.getAzimuthReference("GridNorth"));
	}

	@Test
	public void TestGetAzimuthReferenceMag() { // not supported
		assertNull(AzimuthReferenceType.getAzimuthReference("Mag"));
		assertNull(AzimuthReferenceType.getAzimuthReference("MG"));
	}

	@Test
	public void TestGetAzimuthReferenceNull() {
		assertNull(AzimuthReferenceType.getAzimuthReference("TyN"));
		assertNull(AzimuthReferenceType.getAzimuthReference(null));
	}
}
