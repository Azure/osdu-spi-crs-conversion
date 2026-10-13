package org.opengroup.osdu.crs.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotEmpty;


@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class CompoundCrs extends PersistableReference {

	@JsonProperty("authCode")
	private AuthorityCode authorityCode;

	@JsonProperty("horzEarlyBoundCRS")
	private EarlyBoundCrs horizontalEarlyBoundCrs;

	@JsonProperty("horzLateBoundCRS")
	private LateBoundCrs horizontalLateBoundCrs;

	@JsonProperty("name")
	@NotEmpty
	private String crsName;

	@JsonProperty("ver")
	private String version;

	@JsonProperty("vertEarlyBoundCRS")
	private EarlyBoundCrs verticalEarlyBoundCrs;

	@JsonProperty("vertLateBoundCRS")
	private LateBoundCrs verticalLateBoundCrs;


	@Override
	public String toJsonString()
	{
		return super.toJsonString();
	}


	public CompoundCrs() {
		super("CC");
	}
}
