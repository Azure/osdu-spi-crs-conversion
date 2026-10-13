package org.opengroup.osdu.crs.GeoJson;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(callSuper = true)
public class GeoJsonMultiPolygon extends GeoJsonBase {
    @JsonProperty("coordinates")
    private double[][][][] coordinates;

    public GeoJsonMultiPolygon() {
        super("AnyCrsMultiPolygon");
    } // default to non-GeoJSON

    @Override
    public void updateBbox() {
        this.setBbox(getMinMax(this.getCoordinates(), this.getDimension()));
    }

    @Override
    public boolean isValid() {
        boolean ok = this.coordinates != null;
        int d = 3;  // 3 dimensional point are default
        if (ok) {
            for (double[][][] pgs : this.coordinates) {
                for (double[][] p : pgs) {
                    ok = p.length >= 4;
                    for (double[] pt : p) {
                        d = Math.min(pt.length, d);
                        ok = ok && pt.length >= 2;
                    }
                    if (!ok) break;
                }
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
        if (isValid()) {
            for (double[][][] multi_poly : this.getCoordinates()) {
                for (double[][] poly : multi_poly)
                    length += poly.length;
            }
        }
        return length;
    }

//    @Override
//    void replaceCoordinates(GeoJsonCoordinates coordinates) {
//        double[][][][] pts = this.getCoordinates();
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
