package org.opengroup.osdu.crs.sis.wkt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opengroup.osdu.crs.model.IUnit;
import org.opengroup.osdu.crs.sis.ISisCrs;

public class WktFactory {

    public WktFactory() {

    }

    public String createAzimuthEquidistantFromBase(ISisCrs originalBaseCrs, String name, WktParameters[] parameters, IUnit unit) {
        String baseWkt = originalBaseCrs.getWkt();
        WktParser parser = new WktParser();
        WktSection geogcsSection = parser.parseWkt(baseWkt);
        WktSection unitSection = createUnitSection(unit);
        WktSection projectionSection = createProjcsSection(geogcsSection, name, parameters, unitSection);
        return projectionSection.toWktString();
    }

    public String createAzimuthEquidistantFromProjection(ISisCrs originalProjectionCrs, String name, WktParameters[] parameters) {
        String originalWkt = originalProjectionCrs.getWkt();
        WktParser parser = new WktParser();
        WktSection projcsSection = parser.parseWkt(originalWkt);
        modifyProjcsSection(projcsSection, name, parameters);
        return projcsSection.toWktString();
    }    
    
    private WktSection createUnitSection(IUnit unit) {
        if (unit == null) {
            return null;
        }
        String symbol = unit.getSymbol();
        double scaleToSI = unit.scaleToSI();

        List<IWktAttribute> attributes = new ArrayList<>();
        attributes.add(new WktStringAttribute("\"" + symbol + "\""));
        attributes.add(new WktDoubleAttribute(scaleToSI));

        return new WktSection("UNIT", attributes, new ArrayList<>());
    }

    private WktSection createProjcsSection(WktSection geogcsSection, String name, WktParameters[] parameters, WktSection unitSection) {
        Iterator<WktSection> iterator = geogcsSection.getSubSections().iterator();
        while (iterator.hasNext()) {
            WktSection currentSection = iterator.next();
            if (currentSection.getType().equals("AUTHORITY")) {
                iterator.remove();
            }
        }
        List<WktSection> subSections = new ArrayList<>();
        subSections.add(geogcsSection);
        subSections.add(createProjectionSection());
        for (WktParameters currentParameters : parameters) {
            if (currentParameters == null) {
                continue;
            }
            WktSection currentParameterSection = createParameterSection(currentParameters);
            subSections.add(currentParameterSection);
        }
        if (unitSection != null) {
            subSections.add(unitSection);
        }
        List<IWktAttribute> attributes = new ArrayList<>();
        attributes.add(new WktStringAttribute("\"" + name + "\""));
        return new WktSection("PROJCS", attributes, subSections);
    }

    private WktSection createProjectionSection() {
        List<IWktAttribute> attributes = new ArrayList<>();
        attributes.add(new WktStringAttribute("\"Azimuthal_Equidistant\""));
        return new WktSection("PROJECTION", attributes, new ArrayList<>());
    }

    private void modifyProjcsSection(WktSection projectionSection, String name, WktParameters[] parameters) {
        projectionSection.getAttributes().get(0).setValue("\"" + name + "\"");
        //remove old parameters and authority
        Iterator<WktSection> iterator = projectionSection.getSubSections().iterator();
        while (iterator.hasNext()) {
            WktSection currentSection = iterator.next();
            if (currentSection.getType().equals("PARAMETER")) {
                iterator.remove();
            }
            if (currentSection.getType().equals("AUTHORITY")) {
                iterator.remove();
            }
        }
        List<WktSection> subSections = projectionSection.getSubSections();
        int newParameterIndex = subSections.size();
        for (int i = 0; i < subSections.size(); i++) {
            if (subSections.get(i).getType().equals("PROJECTION")) {
                subSections.get(i).getAttributes().get(0).setValue("\"Azimuthal_Equidistant\"");
                newParameterIndex = i + 1;
                break;
            }
        }
        for (WktParameters currentParameters : parameters) {
            if (currentParameters == null) {
                continue;
            }
            WktSection currentParameterSection = createParameterSection(currentParameters);
            projectionSection.getSubSections().add(newParameterIndex, currentParameterSection);
            newParameterIndex++;
        }
    }

    private WktSection createParameterSection(WktParameters parameter) {
        List<IWktAttribute> attributes = new ArrayList<>();
        attributes.add(new WktStringAttribute("\"" + parameter.getName() + "\""));
        attributes.add(new WktDoubleAttribute(parameter.getValue()));
        return new WktSection("PARAMETER", attributes, new ArrayList<>());
    }
}
