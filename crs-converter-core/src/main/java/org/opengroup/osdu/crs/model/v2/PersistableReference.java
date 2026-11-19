package org.opengroup.osdu.crs.model.v2;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;


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
			JsonMapper mapper = JsonMapper.builderWithJackson2Defaults()
			        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
			        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			        .build();
			JsonNode node = mapper.readTree(json);
			result = mapper.treeToValue(node, PersistableReference.class);
		} catch (JacksonException e) {
			return null;
		}
		return result;
	}

	public String toJsonString() {
		String result;
		JsonMapper mapper = JsonMapper.builderWithJackson2Defaults()
		        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
		        .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
		        .changeDefaultPropertyInclusion(incl -> JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL))
		        .build();
		try {
			result = mapper.writeValueAsString(this);
		} catch (JacksonException e) {
			return null;
		}
		return result;
	}
}
