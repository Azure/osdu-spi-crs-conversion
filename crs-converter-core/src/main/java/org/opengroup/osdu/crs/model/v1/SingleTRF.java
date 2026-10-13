package org.opengroup.osdu.crs.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class SingleTRF extends TRF {

	@JsonProperty("WKT")
	protected String wellKnownText;

}
