package org.opengroup.osdu.crs.BinGrid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class MaxMisLocation {
	
	// Jackson 2 legacy bean naming also exposed the accessors as "di"; keep it for the API contract
	@JsonProperty("dI")
	@Getter(onMethod_ = @JsonProperty("di"))
	@Setter(onMethod_ = @JsonProperty("di"))
	private Double dI;
	@JsonProperty("dJ")
	@Getter(onMethod_ = @JsonProperty("dj"))
	@Setter(onMethod_ = @JsonProperty("dj"))
	private Double dJ;	
	
}
