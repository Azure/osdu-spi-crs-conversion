package org.opengroup.osdu.crs.sis.wkt;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WktSection implements IWktElement {

    private String type;
    private List<IWktAttribute> attributes;
    private List<WktSection> subSections;

    public WktSection(String type, List<IWktAttribute> attributes, List<WktSection> subSections) {
        this.type = type;
        this.attributes = attributes;
        this.subSections = subSections;
    }

    public String getType() {
        return type;
    }

    public void replace(WktSection newSection) {
        this.type = newSection.getType();
        this.attributes = new ArrayList<>();
        List<IWktAttribute> newAttributes = newSection.getAttributes();
        if (newAttributes != null && !newAttributes.isEmpty()) {
            this.attributes.addAll(newAttributes);
        }
        List<WktSection> newSubSections = newSection.getSubSections();
        if (newSubSections != null && !newSubSections.isEmpty()) {
            this.subSections.addAll(newSubSections);
        }
    }

    public void add(int index, WktSection newSection) {
        this.subSections.add(index, newSection);
    }

    public void remove(int index) {
        this.subSections.remove(index);
    }

    public List<IWktAttribute> getAttributes() {
        return attributes;
    }


    public List<WktSection> getSubSections() {
        return subSections;
    }

    public WktSection getSubsection(String type) {
        if (subSections == null) {
            return null;
        }
        for (WktSection currentSection : subSections) {
            if (currentSection.getType().equals(type)) {
                return currentSection;
            }
            WktSection childSection = currentSection.getSubsection(type);
            if (childSection != null) {
                return childSection;
            }
        }
        return null;
    }

    @Override
    public String toWktString() {
        StringBuilder builder = new StringBuilder();
        builder.append(type);
        builder.append("[");
        int counter = 0;
        for (IWktAttribute currentAttribute : attributes) {
            if (counter > 0) {
                builder.append(",");
            }
            builder.append(currentAttribute.toWktString());
            counter++;
        }
        for (WktSection currentSection : subSections) {
            if (counter > 0) {
                builder.append(",");
            }
            builder.append(currentSection.toWktString());
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.attributes);
        hash = 97 * hash + Objects.hashCode(this.subSections);
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
        final WktSection other = (WktSection) obj;
        if (!Objects.equals(this.attributes, other.attributes)) {
            return false;
        }
        if (!Objects.equals(this.subSections, other.subSections)) {
            return false;
        }
        return true;
    }
}
