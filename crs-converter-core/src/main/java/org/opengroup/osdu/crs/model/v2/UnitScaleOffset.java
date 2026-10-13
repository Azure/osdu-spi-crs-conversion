package org.opengroup.osdu.crs.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotEmpty;


@Data
@EqualsAndHashCode(callSuper=true)
public class UnitScaleOffset extends PersistableReference {

	@JsonProperty("baseMeasurement")
	@NotEmpty
	private Measurement baseMeasurement;

	@JsonProperty("symbol")
	@NotEmpty
	private String unitSymbol;

	@JsonProperty("scaleOffset")
	@NotEmpty
	private ScaleOffset scaleOffset;


	@Override
	public String toJsonString()
	{
		return super.toJsonString();
	}


	public UnitScaleOffset() {
		super("USO");
	}
}
