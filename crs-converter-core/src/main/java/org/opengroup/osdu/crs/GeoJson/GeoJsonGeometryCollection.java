package org.opengroup.osdu.crs.GeoJson;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(callSuper = true)
public class GeoJsonGeometryCollection extends GeoJsonBase {
    @JsonProperty("geometries")
    private GeoJsonBase[] geometries;

    public GeoJsonGeometryCollection() {
        super("AnyCrsGeometryCollection");
    } // default to non-GeoJSON

    @Override
    public void updateBbox() {
        int dimension = this.getDimension();
        double[][] coordinates = new double[this.getGeometries().length][2 * dimension];
        for (int i = 0; i < this.getGeometries().length; i++) {
            GeoJsonBase gb = this.getGeometries()[i];
            gb.updateBbox();
            System.arraycopy(gb.getBbox(), 0, coordinates[i], 0, gb.getBbox().length);
        }
        this.setBbox(getMinMax(coordinates, this.getDimension()));
    }

    @Override
    public boolean isValid() {
        boolean ok = this.geometries != null && this.geometries.length > 0;
        int d = 3;  // 3 dimensional point are default
        if (ok) {
            for (GeoJsonBase g : this.geometries) {
                ok = ok && g.isValid();
                d = Math.min(g.getDimension(), d);
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
            for (GeoJsonBase g : this.geometries) {
                length += g.getLength();
            }
        }
        return length;
    }

//    @Override
//    void replaceCoordinates(GeoJsonCoordinates coordinates) {
//        for (GeoJsonBase g : this.geometries) {
//            replaceCoordinateArray(coordinates, g);
//        }
//    }

    @Override
    void appendParts(ArrayList<GeoJsonBase> components) {
        components.add(this);
        if (this.getGeometries() != null) {
            for (GeoJsonBase g : this.geometries) {
                g.appendParts(components);
            }
        }
    }

    @Override
    Object getCoordinatesArray() {
        return null;  // delegate to the parts/components of the collection
    }
}
