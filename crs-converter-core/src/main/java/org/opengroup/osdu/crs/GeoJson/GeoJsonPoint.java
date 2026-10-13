package org.opengroup.osdu.crs.GeoJson;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(callSuper = true)

public class GeoJsonPoint extends GeoJsonBase {
    @JsonProperty("coordinates")
    private double[] coordinates;

    public GeoJsonPoint() {
        super("AnyCrsPoint");
    } // default to non-GeoJSON

    @Override
    public void updateBbox() {
        this.setBbox(getMinMax(this.getCoordinates(), this.getDimension()));
    }

    @Override
    public boolean isValid() {
        boolean ok = this.coordinates != null && this.coordinates.length >= 2;
        if (ok) {
            this.setDimension(this.coordinates.length);
        }
        return ok;
    }

    @Override
    public int getLength() {
        return this.isValid() ? 1 : 0;
    }

//    @Override
//    void replaceCoordinates(GeoJsonCoordinates coordinates) {
//        double[] pt = this.getCoordinates();
//        replaceCoordinateArray(coordinates, pt);
//    }

    @Override
    void appendParts(ArrayList<GeoJsonBase> components) {
        components.add(this);
    }

    @Override
    Object getCoordinatesArray(){
        return this.getCoordinates();
    }
}
