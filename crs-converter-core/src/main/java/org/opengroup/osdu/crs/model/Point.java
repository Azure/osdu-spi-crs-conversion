package org.opengroup.osdu.crs.model;

import io.swagger.v3.oas.annotations.Parameter;
import org.opengroup.osdu.crs.util.Constants;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = Constants.SWAGGER_POINT_DESCR)
public class Point {
    public Point() {
        setNaN(this);
    }
    @NotNull
    @Schema(description = Constants.SWAGGER_X_COORDINATE, type = "number", format = "double",example = Constants.SWAGGER_X_COORDINATE_EXAMPLE)
    @Parameter(required = true)
    private Double x;

    @NotNull
    @Schema(description = Constants.SWAGGER_Y_COORDINATE, type = "number", format = "double",example = Constants.SWAGGER_Y_COORDINATE_EXAMPLE)
    @Parameter(required = true)
    private Double y;

    @NotNull
    @Schema(description = Constants.SWAGGER_Z_COORDINATE, type = "number", format = "double",example = Constants.SWAGGER_Z_COORDINATE_EXAMPLE)
    @Parameter(required = true)
    private Double z;

    public static boolean isValid(Point point) {
        if (point == null) return false;
        // if (point.x == null || point.y ==null || point.z == null) return false; // values cannot be null due to lombok constraints
        return !(Double.isNaN(point.x) || Double.isNaN(point.y) || Double.isNaN(point.z));
    }

    public static void setNaN(Point p) {
        p.x = Double.NaN;
        p.y = Double.NaN;
        p.z = Double.NaN;
    }
}
