package org.opengroup.osdu.crs.api;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HealthCheckTests {

	@Test
	public void testLiveness() {
		HealthCheck hc = new HealthCheck();
		assertNotNull(hc);
		ResponseEntity<?> r = hc.livenessCheck();
		assertNotNull(r);
		assertEquals(HttpStatus.OK, r.getStatusCode());
	}

	@Test
	public void testReadiness() {
		HealthCheck hc = new HealthCheck();
		assertNotNull(hc);
		ResponseEntity<?> r = hc.readinessCheck();
		assertNotNull(r);
		assertEquals(HttpStatus.OK, r.getStatusCode());
	}
}