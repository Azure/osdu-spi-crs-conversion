package org.opengroup.osdu.crs.GeoJson;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import tools.jackson.databind.annotation.JsonDeserialize;
import org.opengroup.osdu.crs.util.StrictStringDeserializer;


import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class GeoJsonFeatureCollection extends GeoJsonBase {

    @JsonProperty("features")
    @NotNull
    @Parameter(required = true)
    private GeoJsonFeature[] features;

    @JsonProperty("properties")
    @Schema(types = {"object", "null"}, nullable = true)
    private Map<String, Object> properties;

    @JsonProperty("persistableReferenceCrs")
    @Schema(nullable = true, types = {"string", "null"})
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String persistableReferenceCrs;

    @JsonProperty("CoordinateReferenceSystemID")
    @Schema(nullable = true, types = {"string", "null"})
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String CoordinateReferenceSystemID;

    @JsonProperty("VerticalUnitID")
    @Schema(nullable = true, types = {"string", "null"})
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String VerticalUnitID;

    @JsonProperty("persistableReferenceUnitZ")
    @Schema(nullable = true, types = {"string", "null"})
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String persistableReferenceUnitZ;

    @JsonProperty("coordinateReferenceSystemID")
    @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = StrictStringDeserializer.class)
    public void setCoordinateReferenceSystemID(String coordinateReferenceSystemID) {
        this.CoordinateReferenceSystemID = coordinateReferenceSystemID;
    }

    @JsonProperty("verticalUnitID")
    @JsonSetter(nulls = Nulls.FAIL)
    @JsonDeserialize(using = StrictStringDeserializer.class)
    public void setVerticalUnitID(String verticalUnitID) {
        this.VerticalUnitID = verticalUnitID;
    }

    public GeoJsonFeatureCollection() {
        super("AnyCrsFeatureCollection"); // default to non-GeoJSON
    }

    @Override
    @Schema(allowableValues = {"FeatureCollection", "AnyCrsFeatureCollection"}, example = "FeatureCollection")
    public String getType() {
        return super.getType();
    }

    @Override
    public void updateBbox() {
        if (this.getFeatures() != null && this.getFeatures().length == 0) {
            this.setBbox(null);
            return;
        }
        if (this.getBbox() == null) return;  // if there was no bounding box before, don't bother
        int dimension = this.getDimension();
        double[][] coordinates = new double[this.getFeatures().length * 2][dimension];
        for (int i = 0; i < this.getFeatures().length; i++) {
            GeoJsonBase gb = this.getFeatures()[i];
            gb.updateBbox();
            System.arraycopy(gb.getBbox(), 0, coordinates[2*i], 0, gb.getBbox().length/2);
            System.arraycopy(gb.getBbox(), dimension, coordinates[2*i+1], 0, gb.getBbox().length/2);
        }
        this.setBbox(getMinMax(coordinates, dimension));
    }

    @Override
    public boolean isValid() {
        boolean ok = this.features != null;
        int d = 3;  // 3 dimensional point are default
        if (ok) {
            for (GeoJsonFeature g : this.getFeatures()) {
                ok = ok && g != null && g.isValid();
                if (ok) d = Math.min(g.getDimension(), d);
            }
        }
        if (ok) {
            this.setDimension(d);
        } else {
            this.setDimension(-1);
        }
        return ok;
    }

    @Override
    public int getLength() {
        int length = 0;
        if (this.isValid()) {
            for (GeoJsonFeature g : this.getFeatures()) {
                length += g.getLength();
            }
        }
        return length;
    }

    @Override
    void appendParts(ArrayList<GeoJsonBase> components) {
        components.add(this);
        if (this.getFeatures() != null) {
            for (GeoJsonBase g : this.getFeatures()) {
                g.appendParts(components);
            }
        }
    }

    @Override
    Object getCoordinatesArray() {
        return null;  // delegate to the parts/components of the collection
    }
}
