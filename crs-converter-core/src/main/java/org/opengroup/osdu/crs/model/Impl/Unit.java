package org.opengroup.osdu.crs.model.Impl;

import org.opengroup.osdu.crs.model.IUnit;
import org.opengroup.osdu.crs.model.v2.UnitEnergistics;
import org.opengroup.osdu.crs.model.v2.UnitScaleOffset;
import org.opengroup.osdu.crs.model.v2.Measurement;
import org.opengroup.osdu.crs.model.v2.ScaleOffset;
import lombok.Data;

@Data
public class Unit implements IUnit {
    private org.opengroup.osdu.crs.model.v1.Unit implementationV1;
    private UnitEnergistics implementationV2E;
    private UnitScaleOffset implementationV2S;

    private boolean valid;
    private String symbol;
    private double scale;
    private double offset;

    private boolean length;
    private boolean angle;

    static final String LENGTH_OSDD = "Length";
    static final String LENGTH_ENERGISTICS = "L";
    static final String ANGLE_OSDD = "Plane_Angle";
    static final String ANGLE_ENERGISTICS = "A";

    public Unit() {
        initialize();
    }
    private void initialize(){
        this.implementationV1 = null;
        this.implementationV2E = null;
        this.implementationV2S = null;
        this.valid = false;
        this.length = false;
        this.angle = false;
        this.scale = Double.NaN;
        this.offset = Double.NaN;
    }

    Unit(org.opengroup.osdu.crs.model.v1.Unit parsedItem) {
        initialize();
        this.implementationV1 = parsedItem;
        this.valid = parsedItem.isValid();
        this.length = parsedItem.isLength();
        this.angle = parsedItem.isAngle();
        this.scale = parsedItem.scaleToSI();
        this.offset = parsedItem.getOffset();
        this.symbol = parsedItem.getSymbol();
    }

    Unit(UnitEnergistics parsedItem) {
        initialize();
        this.implementationV2E = parsedItem;
        this.symbol = parsedItem.getUnitSymbol();
        Measurement measurement = parsedItem.getBaseMeasurement();
        if (measurement != null) {
            this.length = measurement.getMeasurementAncestry().equals(LENGTH_OSDD) || measurement.getMeasurementAncestry().equals(LENGTH_ENERGISTICS);
            this.angle = measurement.getMeasurementAncestry().equals(ANGLE_OSDD) || measurement.getMeasurementAncestry().equals(ANGLE_ENERGISTICS);
            this.valid = this.isLength() || this.isAngle();
        }
        if (parsedItem.getAbcd() != null) {
            this.valid = this.valid && !(Double.isNaN(parsedItem.getAbcd().getA()) || Double.isNaN(parsedItem.getAbcd().getB())
                    || Double.isNaN(parsedItem.getAbcd().getC()) || Double.isNaN(parsedItem.getAbcd().getD()));
            this.valid = this.valid && parsedItem.getAbcd().getC() != 0.0 && parsedItem.getAbcd().getB() != 0.0;
            if (this.valid) {
                this.scale = parsedItem.getAbcd().getB() / parsedItem.getAbcd().getC();
                this.offset = -parsedItem.getAbcd().getA() / parsedItem.getAbcd().getB();
            }
            else {
                this.length = false;
                this.angle = false;
            }
        }
    }

    Unit(UnitScaleOffset parsedItem) {
        initialize();
        this.implementationV2S = parsedItem;
        this.symbol = parsedItem.getUnitSymbol();
        Measurement measurement = parsedItem.getBaseMeasurement();
        this.valid = measurement != null;
        if (this.valid) {
            this.length = measurement.getMeasurementAncestry().equals(LENGTH_OSDD) ||
                    measurement.getMeasurementAncestry().equals(LENGTH_ENERGISTICS);
            this.angle = measurement.getMeasurementAncestry().equals(ANGLE_OSDD) ||
                    measurement.getMeasurementAncestry().equals(ANGLE_ENERGISTICS);
            this.valid = this.isLength() || this.isAngle();
        }
        this.valid = this.valid && parsedItem.getScaleOffset() != null;
        if (this.valid) {
            this.valid = !(Double.isNaN(parsedItem.getScaleOffset().getScaleFactor()) ||
                    Double.isNaN(parsedItem.getScaleOffset().getOffset()) ||
                    parsedItem.getScaleOffset().getScaleFactor() == 0.0);
            this.scale = parsedItem.getScaleOffset().getScaleFactor();
            this.offset = parsedItem.getScaleOffset().getOffset();
        }
    }

    @Override
    public double scaleToSI() {
        return this.getScale();
    }

    @Override
    public double convertToUnit(IUnit toUnit) {
        if (toUnit != null && this.isValid() && toUnit.isValid())
            return this.scaleToSI() / toUnit.scaleToSI();
        return Double.NaN;
    }

    @Override
    public String createPersistableReference() {
        String pr = "";
        if (this.isValid()) {
            UnitScaleOffset unit = new UnitScaleOffset();
            unit.setUnitSymbol(this.getSymbol());
            unit.setScaleOffset(new ScaleOffset(this.getOffset(), this.getScale()));
            Measurement m = new Measurement();
            if (this.isLength()) m.setMeasurementAncestry(LENGTH_OSDD);
            else m.setMeasurementAncestry(ANGLE_OSDD);
            unit.setBaseMeasurement(m);
            this.setImplementationV2S(unit);
            pr = unit.toJsonString();
        }
        return pr;
    }
}
