package org.opengroup.osdu.crs.GeoJson;

import org.opengroup.osdu.crs.model.IUnit;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import static org.opengroup.osdu.crs.model.ReferenceConverter.parseUnitReference;

@Data
public class GeoJsonCoordinates {
    private double[] xys;
    private double[] z_s;
    private int index;

    @Setter(AccessLevel.NONE)
    private int length;

    GeoJsonCoordinates(int length) {
        this.xys = new double[2 * length];
        this.z_s = new double[length];
        this.index = 0;
        this.length = length;
    }

    private void addCoordinates(double[][][][] list_of_list_of_coordinates) {
        for (double[][][] list_of_coordinates : list_of_list_of_coordinates) {
            this.addCoordinates(list_of_coordinates);
        }
    }

    private void addCoordinates(double[][][] list_of_coordinates) {
        for (double[][] coordinates : list_of_coordinates) {
            this.addCoordinates(coordinates);
        }
    }

    private void addCoordinates(double[][] coordinates) {
        for (double[] coordinate : coordinates) {
            this.addCoordinate(coordinate);
        }
    }

    private void addCoordinate(double[] coordinate) {
        this.xys[2 * this.index] = coordinate[0];
        this.xys[2 * this.index + 1] = coordinate[1];
        if (coordinate.length > 2) {
            this.z_s[this.index] = coordinate[2];
        } else {
            this.z_s[this.index] = 0.0;
        }
        this.index += 1;
    }

    void addAnyCoordinates(Object coordinates) {
        if (coordinates != null) {
            if (coordinates instanceof double[]) {
                this.addCoordinate((double[]) coordinates);
            } else if (coordinates instanceof double[][]) {
                this.addCoordinates((double[][]) coordinates);
            } else if (coordinates instanceof double[][][]) {
                this.addCoordinates((double[][][]) coordinates);
            } else if (coordinates instanceof double[][][][]) {
                this.addCoordinates((double[][][][]) coordinates);
            }
        }
    }

    public String convertUnits(String fromUnitZ, String toUnitZ) {
        String message = "No unit conversion for Z-axis";
        IUnit from_u = parseUnitReference(fromUnitZ);
        IUnit to___u = parseUnitReference(toUnitZ);
        if (from_u.isValid() && to___u.isValid()) {
            double scale = from_u.convertToUnit(to___u);
            for (int i=0; i<this.getZ_s().length; i++) this.getZ_s()[i] *= scale;
            message = String.format("Z-axis unit conversion from %s to %s", from_u.getSymbol(), to___u.getSymbol());
        }
        return message;
    }
}
