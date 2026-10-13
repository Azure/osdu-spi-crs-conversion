package org.opengroup.osdu.crs.GeoJson;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(callSuper = true)
public class GeoJsonLineString extends GeoJsonBase {
    @JsonProperty("coordinates")
    private double[][] coordinates;

    public GeoJsonLineString() {
        super("AnyCrsLineString");
    } // default to non-GeoJSON

    @Override
    public void updateBbox() {
        int length = this.getDimension();
        double[] bbox = new double[length * 2];
        for (int i = 0; i < length; i++) {
            bbox[i] = coordinates[0][i];
            bbox[i + length] = coordinates[0][i];
        }
        for (int j = 1; j < this.coordinates.length; j++) {
            for (int i = 0; i < length; i++) {
                bbox[i] = Double.min(bbox[i], coordinates[j][i]);
                bbox[i + length] = Double.max(bbox[i + length], coordinates[j][i]);
            }
        }
        this.setBbox(bbox);
    }

    @Override
    public boolean isValid() {
        boolean ok = this.coordinates != null && this.coordinates.length >= 1;
        if (ok) {
            int d = 3;
            for (double[] pt : this.getCoordinates()) {
                d = Math.min(pt.length, d);
                ok = ok && pt.length >= 2;
            }
            if (ok) {
                this.setDimension(d);
            } else {
                this.setDimension(-1);
            }
        }
        return ok;
    }

    @Override
    public int getLength() {
        return this.isValid() ? this.getCoordinates().length : 0;
    }

//    @Override
//    void replaceCoordinates(GeoJsonCoordinates coordinates) {
//        double[][] pts = this.getCoordinates();
//        replaceCoordinateArray(coordinates, pts);
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
