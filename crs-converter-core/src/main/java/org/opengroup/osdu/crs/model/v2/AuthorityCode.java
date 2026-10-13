package org.opengroup.osdu.crs.model.v2;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "defined" })
public class AuthorityCode {

	@JsonProperty("auth")
	@NotEmpty
	private String authority;

	@JsonProperty("code")
	@NotEmpty
	private String code;

	public boolean isDefined() {
	    return (!this.getAuthority().isEmpty() &&
                !this.getCode().isEmpty());
    }
}
