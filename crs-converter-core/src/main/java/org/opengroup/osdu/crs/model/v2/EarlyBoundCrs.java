package org.opengroup.osdu.crs.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotEmpty;


@Data
@EqualsAndHashCode(callSuper=true)
public class EarlyBoundCrs extends PersistableReference {

	@JsonProperty("authCode")
	private AuthorityCode authorityCode;

	@JsonProperty("compoundCT")
	private CompoundTrf compoundTransformation;

	@JsonProperty("lateBoundCRS")
	@NotEmpty
	private LateBoundCrs lateBoundCrs;

	@JsonProperty("name")
	@NotEmpty
	private String crsName;

	@JsonProperty("singleCT")
	private SingleTrf singleTransformation;

	@JsonProperty("ver")
	private String version;


	@Override
	public String toJsonString()
	{
		return super.toJsonString();
	}


	public EarlyBoundCrs() {
		super("EBC");
	}
}
