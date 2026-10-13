package org.opengroup.osdu.crs.model.v1;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class EarlyBoundCRS extends CRS {

	@JsonProperty("LB_CRS")
	private LateBoundCRS lateBoundCRS;

	@JsonProperty("TRF")
	private TRF trf;

}
