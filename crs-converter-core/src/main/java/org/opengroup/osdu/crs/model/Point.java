package org.opengroup.osdu.crs.model;

import io.swagger.v3.oas.annotations.Parameter;
import org.opengroup.osdu.crs.util.Constants;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
@Schema(description = Constants.SWAGGER_POINT_DESCR)
public class Point {
    public Point() {
        setNaN(this);
    }
    @JsonCreator
    public Point(@JsonProperty(value = "x", required = true) Double x,
                 @JsonProperty(value = "y", required = true) Double y,
                 @JsonProperty(value = "z", required = true) Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "Coordinates must be finite numbers")
    public boolean isFinite() {
        return (x == null || Double.isFinite(x)) && (y == null || Double.isFinite(y))
                && (z == null || Double.isFinite(z));
    }

    @NotNull
    @Schema(description = Constants.SWAGGER_X_COORDINATE, type = "number", types = {"number"}, format = "double",example = Constants.SWAGGER_X_COORDINATE_EXAMPLE)
    @Parameter(required = true)
    private Double x;

    @NotNull
    @Schema(description = Constants.SWAGGER_Y_COORDINATE, type = "number", types = {"number"}, format = "double",example = Constants.SWAGGER_Y_COORDINATE_EXAMPLE)
    @Parameter(required = true)
    private Double y;

    @NotNull
    @Schema(description = Constants.SWAGGER_Z_COORDINATE, type = "number", types = {"number"}, format = "double",example = Constants.SWAGGER_Z_COORDINATE_EXAMPLE)
    @Parameter(required = true)
    private Double z;

    public static boolean isValid(Point point) {
        return point != null && point.x != null && point.y != null && point.z != null
                && Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z);
    }

    public static void setNaN(Point p) {
        p.x = Double.NaN;
        p.y = Double.NaN;
        p.z = Double.NaN;
    }
}
