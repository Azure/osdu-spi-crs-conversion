package org.opengroup.osdu.crs.model.v1;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class CompoundTRF extends TRF {

	@JsonProperty("Policy")
	protected String policy;

	@JsonProperty("CartographicTransforms")
	private List<SingleTRF> transformations;
}
