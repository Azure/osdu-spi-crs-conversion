package org.opengroup.osdu.crs.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response-only contract: computed coordinates can carry Jackson's non-finite markers. */
@Schema(description = "Computed 3D coordinates. Unavailable values use the string NaN.")
public class PointResponse {
    @Schema(types = {"number", "string"}, oneOf = {Double.class, NonFiniteNumber.class}, requiredMode = Schema.RequiredMode.REQUIRED)
    public Double x;
    @Schema(types = {"number", "string"}, oneOf = {Double.class, NonFiniteNumber.class}, requiredMode = Schema.RequiredMode.REQUIRED)
    public Double y;
    @Schema(types = {"number", "string"}, oneOf = {Double.class, NonFiniteNumber.class}, requiredMode = Schema.RequiredMode.REQUIRED)
    public Double z;
}
