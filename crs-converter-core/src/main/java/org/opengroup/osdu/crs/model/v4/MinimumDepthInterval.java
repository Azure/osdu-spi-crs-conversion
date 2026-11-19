package org.opengroup.osdu.crs.model.v4;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import tools.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.crs.util.Constants;
import org.opengroup.osdu.crs.util.StrictDoubleDeserializer;

import java.util.List;

@Data
// Jackson 3 would otherwise bind via the all-args constructor and treat omitted properties as null
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@AllArgsConstructor
@Schema(description = Constants.SWAGGER_TRJ_MINIMUM_DEPTH_INTERVAL_DESCRIPTION)
public class MinimumDepthInterval {

    @JsonSetter(nulls = Nulls.FAIL, contentNulls = Nulls.FAIL)
    @Schema(description = Constants.SWAGGER_MD_I, example = Constants.SWAGGER_MD_I_EXAMPLE)
    private List<Double> md_i;
    @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = StrictDoubleDeserializer.class)
    @Schema(description = Constants.SWAGGER_MD_INTERVAL, example = Constants.SWAGGER_MD_INTERVAL_EXAMPLE)
    private Double md_interval;

}
