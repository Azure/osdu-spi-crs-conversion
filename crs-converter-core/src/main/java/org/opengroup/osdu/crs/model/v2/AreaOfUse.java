package org.opengroup.osdu.crs.model.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@EqualsAndHashCode(callSuper=true)
public class AreaOfUse extends PersistableReference {

	@JsonProperty("authCode")
	private AuthorityCode authorityCode;

	@JsonProperty("boundBox")
	@NotEmpty
	private Wgs84BoundingBox boundingBox;

	@JsonProperty("name")
	private String name;

	@Override
	public String toJsonString()
	{
		return super.toJsonString();
	}

	public AreaOfUse() {
		super("AOU");
	}
}
