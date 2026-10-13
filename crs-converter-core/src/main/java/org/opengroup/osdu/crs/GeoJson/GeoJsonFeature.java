package org.opengroup.osdu.crs.GeoJson;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class GeoJsonFeature extends GeoJsonBase {
    @JsonProperty("geometry")
    @NotNull
    @Parameter(required = true)
    private GeoJsonBase geometry;

    @JsonProperty("properties")
    private Object properties;

    public GeoJsonFeature() {
        super("AnyCrsFeature"); // default to non-GeoJSON
        this.properties = new Object();
    }

    @Override
    public void updateBbox() {
        int dimension = this.getDimension();
        GeoJsonBase gb = this.getGeometry();
        gb.updateBbox();
        this.setBbox(gb.getBbox());
    }

    @Override
    public boolean isValid() {
        boolean ok = this.geometry != null && this.geometry.isValid();
        if (ok) {
            this.setDimension(this.geometry.getDimension());
        } else {
            this.setDimension(-1);
        }
        return ok;
    }


    @Override
    public int getLength() {
        int length = 0;
        if (this.isValid()) {
            length = this.getGeometry().getLength();
        }
        return length;
    }

//    @Override
//    void replaceCoordinates(GeoJsonCoordinates coordinates) {
//        replaceCoordinateArray(coordinates, this);
//    }

    @Override
    void appendParts(ArrayList<GeoJsonBase> components) {
        components.add(this);
        if (this.getGeometry() != null) this.getGeometry().appendParts(components);
    }

    @Override
    Object getCoordinatesArray(){
        return null;  // delegate to the parts/components of the feature
    }
}
