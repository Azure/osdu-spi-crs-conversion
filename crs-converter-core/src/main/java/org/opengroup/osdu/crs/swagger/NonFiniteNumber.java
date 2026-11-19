package org.opengroup.osdu.crs.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

/** Jackson's existing wire values for unavailable or unbounded computed results. */
@Schema(type = "string", types = {"string"}, allowableValues = {"NaN", "Infinity", "-Infinity"})
public class NonFiniteNumber {
}
