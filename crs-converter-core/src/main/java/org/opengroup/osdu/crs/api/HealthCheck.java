package org.opengroup.osdu.crs.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/_ah","v2/_ah","v3/_ah"})
@SecurityRequirements
public class HealthCheck {
	@Operation(summary = "${healthCheckApi.livenessCheck.summary}",
			description = "${healthCheckApi.livenessCheck.description}", tags = { "health-check-api" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "CRS Conversion service is alive", content = @Content)
	})
	@GetMapping("/liveness_check")
	public ResponseEntity<Void> livenessCheck() {
		return ResponseEntity.ok().build();
	}


	@Operation(summary = "${healthCheckApi.readinessCheck.summary}",
			description = "${healthCheckApi.readinessCheck.description}", tags = { "health-check-api" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "CRS Conversion service is ready", content = @Content)
	})
	@GetMapping("/readiness_check")
	public ResponseEntity<Void> readinessCheck() {
		// Commented logging - lets us monitor VM state while running
		// Logger log = Logger.getLogger("Readiness");
		// String message = String.format("Ready|%d|%d" , System.nanoTime(), Runtime.getRuntime().freeMemory());
		// log.info(message);
		return ResponseEntity.ok().build();
	}
}
