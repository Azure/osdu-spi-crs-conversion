package org.opengroup.osdu.crs.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.opengroup.osdu.crs.BinGrid.MaxMisLocation;

public class ConvertBinGridResponseTest {

	@Test
	public void testConvertBinGridResponseTest() {
		ConvertBinGridResponse response = new ConvertBinGridResponse();
		assertNull(response.getAppliedOperations());
		assertNull(response.getMaxMisLocation());
		assertNull(response.getOutBinGrid());

		MaxMisLocation maxMisLocation = new MaxMisLocation();
		maxMisLocation.setDI(0.0);
		maxMisLocation.setDJ(0.0);
		response.setMaxMisLocation(maxMisLocation);
		assertEquals(maxMisLocation.getDI(), response.getMaxMisLocation().getDI());
		assertEquals(maxMisLocation.getDJ(), response.getMaxMisLocation().getDJ());

		List<String> ops = Arrays.asList("something", "done");
		response.setAppliedOperations(ops);
		assertEquals(ops, response.getAppliedOperations());

	}

}
