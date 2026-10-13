package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.model.WellKnownTextType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class WellKnownTextTypeTests {

	@Test
	public void getWellKnownTextTypeGeogcs() {

		assertEquals(WellKnownTextType.GEOGCS, WellKnownTextType.getWellKnownTextType("GeogCS..."));
	}

	@Test
	public void getWellKnownTextTypeGeogtran() {

		assertEquals(WellKnownTextType.GEOGTRAN, WellKnownTextType.getWellKnownTextType("GeogTran..."));
	}

	@Test
	public void getWellKnownTextTypeProjcs() {

		assertEquals(WellKnownTextType.PROJCS, WellKnownTextType.getWellKnownTextType("ProjCS..."));
	}

	@Test
	public void getWellKnownTextTypeVertcs() {

		assertEquals(WellKnownTextType.VERTCS, WellKnownTextType.getWellKnownTextType("VertCS..."));
	}

	@Test
	public void getWellKnownTextTypeNone() {

		assertNull(WellKnownTextType.getWellKnownTextType("anyString"));
	}

	@Test
	public void getWellKnownTextTypeNull() {

		assertNull(WellKnownTextType.getWellKnownTextType(null));
	}
}
