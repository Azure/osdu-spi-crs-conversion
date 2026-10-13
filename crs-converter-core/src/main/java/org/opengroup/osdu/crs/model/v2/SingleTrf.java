package org.opengroup.osdu.crs.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotEmpty;


@Data
@EqualsAndHashCode(callSuper=true)
public class SingleTrf extends PersistableReference {

	@JsonProperty("authCode")
	private AuthorityCode authorityCode;

	@JsonProperty("name")
	@NotEmpty
	private String trfName;

	@JsonProperty("ver")
	private String version;

	@JsonProperty("wkt")
	@NotEmpty
	private String wellKnownText;


	@Override
	public String toJsonString()
	{
		return super.toJsonString();
	}


	public SingleTrf() {
		super("ST");
	}
}
