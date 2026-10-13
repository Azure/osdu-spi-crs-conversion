package org.opengroup.osdu.crs.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.URLDecoder;

@JsonIgnoreProperties({ "valid", "angle", "length", "offset" })
@Data
@EqualsAndHashCode(callSuper = false)
public class Unit extends UnitParameters {
	public static final String LENGTH_OSDD = "%7B%22Ancestry%22%3A%22Length%22%7D";
	public static final String LENGTH_ENERGISTICS = "%7B%22Ancestry%22%3A%22L%22%7D";
	public static final String ANGLE_OSDD = "%7B%22Ancestry%22%3A%22Plane_Angle%22%7D";
	public static final String ANGLE_ENERGISTICS = "%7B%22Ancestry%22%3A%22A%22%7D";
	@JsonProperty("ScaleOffset")
	protected ScaleOffset scaleOffset;

	@JsonProperty("ABCD")
	protected Abcd abcd;

	public double offset;

	@JsonProperty("Symbol")
	protected String symbol;

	@JsonProperty("BaseMeasurement")
	protected String baseMeasurement;

	public static Unit createInstance(String unitReference) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

			String decoded = URLDecoder.decode(unitReference, "UTF-8");
			JsonNode node = mapper.readTree(decoded);
			return mapper.treeToValue(node, Unit.class);
		} catch (Exception e) {
		    return new Unit(); // return an empty, invalid unit
		}
	}

    public boolean isLength() {
        if (this.getBaseMeasurement() != null)
            return this.baseMeasurement.equals(LENGTH_OSDD) ||
                    this.baseMeasurement.equals(LENGTH_ENERGISTICS);
        return false;
    }

    public boolean isAngle() {
        if (this.getBaseMeasurement() != null)
            return this.baseMeasurement.equals(ANGLE_OSDD) ||
                    this.baseMeasurement.equals(ANGLE_ENERGISTICS);
        return false;
    }

	public boolean isValid() {
		boolean valid = this.abcd != null || this.scaleOffset != null;
		return valid && (this.isLength() || this.isAngle());
	}

	public double scaleToSI() {
		if (isValid()) {
			if (this.abcd != null) return this.abcd.scaleToSI();
			else if (this.scaleOffset != null) return this.scaleOffset.scaleToSI();
		}
		return Double.NaN;
	}

	public double getOffset(){
        if (isValid()) {
            if (this.abcd != null) return -this.abcd.getA()/this.getAbcd().getB();
            else if (this.scaleOffset != null) return this.scaleOffset.getOffset();
        }
        return Double.NaN;
    }
}

