package org.opengroup.osdu.crs.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper=true)
public class CompoundTrf extends PersistableReference {

	@JsonProperty("authCode")
	private AuthorityCode authorityCode;

	@JsonProperty("name")
	@NotEmpty
	private String trfName;

	@JsonProperty("policy")
	@NotEmpty
	private String policy;

	@JsonProperty("cts")
	@NotEmpty
	private List<SingleTrf> transformationList;

	@JsonProperty("ver")
	private String version;


	@Override
	public String toJsonString()
	{
		return super.toJsonString();
	}


	public CompoundTrf() {
		super("CT");
	}
}
