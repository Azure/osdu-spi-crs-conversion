package org.opengroup.osdu.crs.model.v2;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property="type", visible=true)
@JsonSubTypes({
	@JsonSubTypes.Type(value = LateBoundCrs.class, name = "LBC"),
	@JsonSubTypes.Type(value = EarlyBoundCrs.class, name = "EBC"),
	@JsonSubTypes.Type(value = CompoundCrs.class, name = "CC"),
	@JsonSubTypes.Type(value = SingleTrf.class, name = "ST"),
	@JsonSubTypes.Type(value = CompoundTrf.class, name = "CT"),
	@JsonSubTypes.Type(value = AreaOfUse.class, name = "AOU"),
	@JsonSubTypes.Type(value = Measurement.class, name = "UM"),
	@JsonSubTypes.Type(value = UnitScaleOffset.class, name = "USO"),
	@JsonSubTypes.Type(value = UnitEnergistics.class, name = "UAD"),
})
public class PersistableReference {

	@JsonProperty("type")
	@NotEmpty
	private String typeOfPersistableReference;


	public static PersistableReference createInstance(String json) {
		PersistableReference result;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JsonNode node = mapper.readTree(json);
			result = mapper.treeToValue(node, PersistableReference.class);
		} catch (IOException e) {
			return null;
		}
		return result;
	}

	public String toJsonString() {
		String result;
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try {
			result = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return null;
		}
		return result;
	}
}
