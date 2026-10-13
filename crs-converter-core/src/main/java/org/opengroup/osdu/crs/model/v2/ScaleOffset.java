package org.opengroup.osdu.crs.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.validation.constraints.NotNull;


@Data
@AllArgsConstructor
public class ScaleOffset {

	public ScaleOffset()
    {
        this.scaleFactor = Double.NaN;
        this.offset = Double.NaN;
    }

    @NotNull
    @JsonProperty("offset")
	private Double offset;

	@NotNull
	@JsonProperty("scale")
	private Double scaleFactor;
}
