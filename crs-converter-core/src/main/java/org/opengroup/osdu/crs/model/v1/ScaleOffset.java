package org.opengroup.osdu.crs.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
class ScaleOffset extends UnitParameters {
	@JsonProperty("Scale")
	protected double scale;

	@JsonProperty("Offset")
	protected double offset;

	public double scaleToSI() {
		return this.scale;
	}
}
