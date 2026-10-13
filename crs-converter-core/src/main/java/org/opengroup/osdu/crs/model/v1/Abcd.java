package org.opengroup.osdu.crs.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Abcd extends UnitParameters {
	@JsonProperty("A")
	protected double a;

	@JsonProperty("B")
	protected double b;

	@JsonProperty("C")
	protected double c;

	@JsonProperty("D")
	protected double d;

	public double scaleToSI() {
		return this.b / this.c;
	}
}
