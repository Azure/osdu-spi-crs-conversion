package org.opengroup.osdu.crs.model;

public interface IUnit extends IItem {
    String getSymbol();
    void setSymbol(String symbol);

    double getScale();
    void setScale(double scale);

    double getOffset();
    void setOffset(double offset);

    boolean isValid();
    boolean isLength();
    boolean isAngle();

    double scaleToSI();
    double convertToUnit(IUnit toUnit);
}
