package org.opengroup.osdu.crs.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.opengroup.osdu.crs.BinGrid.AbstractBinGrid;
import org.opengroup.osdu.crs.util.ConstantsTests;

public class ConvertBinGridRequestTest {

	@Test
	public void testConvertBinGridRequestTest() {
		ConvertBinGridRequest request = new ConvertBinGridRequest();
		assertNull(request.getInBinGrid());
		assertNull(request.getToCRS());

		request.setToCRS(ConstantsTests.EB_NAD83_UTM11N_1702[ConstantsTests.V2]);
		assertEquals(ConstantsTests.EB_NAD83_UTM11N_1702[ConstantsTests.V2], request.getToCRS());

		AbstractBinGrid inBinGrid = new AbstractBinGrid();
		request.setInBinGrid(inBinGrid);
		assertNotNull(request.getInBinGrid());

	}

}
