package org.opengroup.osdu.crs.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotEmpty;


@Data
@NoArgsConstructor
public class Wgs84BoundingBox {

	@JsonProperty("latMin")
	@NotEmpty
	private Double latitudeLower;

	@JsonProperty("latMax")
	@NotEmpty
	private Double latitudeUpper;

	@JsonProperty("lonMin")
	@NotEmpty
	private Double longitudeLeft;

	@JsonProperty("lonMax")
	@NotEmpty
	private Double longitudeRight;
}
