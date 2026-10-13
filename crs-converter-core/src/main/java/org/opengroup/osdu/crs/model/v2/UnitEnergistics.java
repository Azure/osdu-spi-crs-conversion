package org.opengroup.osdu.crs.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotEmpty;


@Data
@EqualsAndHashCode(callSuper=true)
public class UnitEnergistics extends PersistableReference {

	@JsonProperty("abcd")
	@NotEmpty
	private Abcd abcd;

	@JsonProperty("baseMeasurement")
	@NotEmpty
	private Measurement baseMeasurement;

	@JsonProperty("symbol")
	@NotEmpty
	private String unitSymbol;


	@Override
	public String toJsonString()
	{
		return super.toJsonString();
	}


	public UnitEnergistics() {
		super("UAD");
	}
}
