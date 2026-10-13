package org.opengroup.osdu.crs.model.v4;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.crs.util.Constants;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = Constants.SWAGGER_TRJ_MINIMUM_DEPTH_INTERVAL_DESCRIPTION)
public class MinimumDepthInterval {

    @Schema(description = Constants.SWAGGER_MD_I, example = Constants.SWAGGER_MD_I_EXAMPLE)
    private List<Double> md_i;
    @Schema(description = Constants.SWAGGER_MD_INTERVAL, example = Constants.SWAGGER_MD_INTERVAL_EXAMPLE)
    private Double md_interval;

}
