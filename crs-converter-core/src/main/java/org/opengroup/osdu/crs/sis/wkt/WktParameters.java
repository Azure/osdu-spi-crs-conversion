package org.opengroup.osdu.crs.sis.wkt;

public class WktParameters {

    private final String name;
    private final double value;

    public WktParameters(String name) {
        this.name = name;
        this.value = 0;
    }

    public WktParameters(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public String getWKTValue() {
        return "PARAMETER[\"" + name + "\"," + String.valueOf(value) + "]";
    }
}
