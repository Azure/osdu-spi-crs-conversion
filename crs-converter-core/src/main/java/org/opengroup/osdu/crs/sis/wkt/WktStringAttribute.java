package org.opengroup.osdu.crs.sis.wkt;

import java.util.Objects;

public class WktStringAttribute implements IWktAttribute<String> {

    private String value;

    public WktStringAttribute(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String newValue) {
        this.value = newValue;
    }

    @Override
    public String toWktString() {
        return value;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WktStringAttribute other = (WktStringAttribute) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }
}

