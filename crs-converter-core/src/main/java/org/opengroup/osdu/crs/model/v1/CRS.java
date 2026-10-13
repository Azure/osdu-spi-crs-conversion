package org.opengroup.osdu.crs.model.v1;

import java.net.URLDecoder;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.opengroup.osdu.crs.util.Constants;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "Type", visible = true)
@JsonSubTypes({
        @Type(value = EarlyBoundCRS.class, name = "EBCRS"),
        @Type(value = LateBoundCRS.class, name = "LBCRS"),
        @Type(value = SingleTRF.class, name = "STRF"),
        @Type(value = CompoundTRF.class, name = "CTRF"),
})
@JsonIgnoreProperties({ "valid", "type" })
public abstract class CRS {

    @JsonProperty("Type")
    protected CRSType type;

    @JsonProperty("Name")
    protected String name;

    @JsonProperty("EngineVersion")
    protected String engineVersion;

    @JsonProperty("AuthorityCode")
    protected AuthorityCode authorityCode;

    /**
     * This method is to create an instance of CRS based on the Json constructed from a application/x-www-form-urlencoded String.
     * @param crsText can be either fromCRS or toCRS in request
     * @return an instance of CRS based on the type of CRS
     * @throws IllegalArgumentException when parsing fails
     */
    public static CRS createInstance(String crsText)
            throws IllegalArgumentException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

            //decode the crsText from application/x-www-form-urlencoded format and read into a tree model
            String decoded = URLDecoder.decode(crsText, "UTF-8");
            JsonNode node = mapper.readTree(decoded);

            //construct the Json structure from the long ugly string
            if (node.has("LB_CRS")) {
                JsonNode lbCRS = node.findPath("LB_CRS");
                ((ObjectNode) node).replace("LB_CRS", mapper.readTree(URLDecoder.decode(lbCRS.asText(), "UTF-8")));
            }

            if (node.has("TRF")) {
                JsonNode trf = node.findPath("TRF");
                ((ObjectNode) node).replace("TRF", mapper.readTree(URLDecoder.decode(trf.asText(), "UTF-8")));
                trf = node.findPath("TRF");
                if (trf.has("CartographicTransforms")) {
                    ArrayNode transforms = (ArrayNode) node.findPath("CartographicTransforms");
                    for (int i = 0; i < transforms.size(); i++) {
                        JsonNode item = transforms.get(i);
                        item = mapper.readTree(URLDecoder.decode(item.asText(), "UTF-8"));
                        transforms.set(i, item);
                    }
                }
            }
            else if (node.has("CartographicTransforms")) { // handle CTRF as top-level
                ArrayNode transforms = (ArrayNode) node.findPath("CartographicTransforms");
                for (int i = 0; i < transforms.size(); i++) {
                    JsonNode item = transforms.get(i);
                    item = mapper.readTree(URLDecoder.decode(item.asText(), "UTF-8"));
                    transforms.set(i, item);
                }
            }

            return mapper.treeToValue(node, CRS.class);

        } catch (Exception e) {
            String message = Constants.ERROR_MSG_JSON_PARSE + e.getMessage();
            throw new IllegalArgumentException(message);
        }
    }
}
