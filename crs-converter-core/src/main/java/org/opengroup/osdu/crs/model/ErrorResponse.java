package org.opengroup.osdu.crs.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response resulting of a failed CRS conversion operation")
public class ErrorResponse {

	@Schema(description = "Error message", type = "string")
	private String error;
}
