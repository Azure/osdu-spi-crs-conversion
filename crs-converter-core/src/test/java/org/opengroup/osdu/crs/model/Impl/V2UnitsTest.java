package org.opengroup.osdu.crs.model.Impl;

import org.opengroup.osdu.crs.model.v2.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class V2UnitsTest {
    @Test
    public void TestV2ScaleOffsetUnit() {
        UnitScaleOffset u_s = new UnitScaleOffset();

        Unit unit = new Unit(u_s);
        assertNotNull(unit);
        assertFalse(unit.isValid());
        u_s.setUnitSymbol("Symbol");
        u_s.setBaseMeasurement(new Measurement());
        unit = new Unit(u_s);
        assertFalse(unit.isValid());
        Measurement m = new Measurement();
        m.setMeasurementAncestry("baseMeasurement");
        u_s.setBaseMeasurement(m);
        unit = new Unit(u_s);
        assertFalse(unit.isValid());
        m.setMeasurementAncestry("Length");
        u_s.setBaseMeasurement(m);
        unit = new Unit(u_s);
        assertFalse(unit.isValid());
        m.setMeasurementAncestry("L");
        u_s.setBaseMeasurement(m);
        unit = new Unit(u_s);
        assertFalse(unit.isValid());
        m.setMeasurementAncestry("Plane_Angle");
        u_s.setBaseMeasurement(m);
        unit = new Unit(u_s);
        assertFalse(unit.isValid());
        m.setMeasurementAncestry("A");
        u_s.setBaseMeasurement(m);
        ScaleOffset so = new ScaleOffset();
        u_s.setScaleOffset(so);
        unit = new Unit(u_s);
        assertFalse(unit.isValid());
        unit = new Unit(u_s);
        assertFalse(unit.isValid());
        so.setOffset(0.0);
        unit = new Unit(u_s);
        assertFalse(unit.isValid());
        so.setScaleFactor(0.0);
        unit = new Unit(u_s);
        assertFalse(unit.isValid());
        so.setScaleFactor(3.0);
        unit = new Unit(u_s);
        assertTrue(unit.isValid());
    }

    @Test
    public void TestV2EnergisticsUnits() {
        UnitEnergistics u_e = new UnitEnergistics();

        Unit unit = new Unit(u_e);
        assertNotNull(unit);
        assertFalse(unit.isValid());
        u_e.setAbcd(new Abcd());
        unit = new Unit(u_e);
        assertFalse(unit.isValid());
        u_e.setBaseMeasurement(new Measurement());
        unit = new Unit(u_e);
        assertFalse(unit.isValid());
        u_e.setUnitSymbol("Symbol");
        unit = new Unit(u_e);
        assertFalse(unit.isValid());
        Measurement m = new Measurement();
        m.setMeasurementAncestry("baseMeasurement");
        u_e.setBaseMeasurement(m);
        unit = new Unit(u_e);
        assertFalse(unit.isValid());
        m.setMeasurementAncestry("Length");
        u_e.setBaseMeasurement(m);
        unit = new Unit(u_e);
        assertFalse(unit.isValid());
        m.setMeasurementAncestry("L");
        u_e.setBaseMeasurement(m);
        unit = new Unit(u_e);
        assertFalse(unit.isValid());
        m.setMeasurementAncestry("Plane_Angle");
        u_e.setBaseMeasurement(m);
        unit = new Unit(u_e);
        assertFalse(unit.isValid());
        m.setMeasurementAncestry("A");
        u_e.setBaseMeasurement(m);
        unit = new Unit(u_e);
        assertFalse(unit.isValid());
        Abcd abcd = new Abcd();
        abcd.setA(0.0);
        u_e.setAbcd(abcd);
        unit = new Unit(u_e);
        assertFalse(unit.isValid());
        abcd.setB(0.0);
        unit = new Unit(u_e);
        assertFalse(unit.isValid());
        abcd.setC(0.0);
        unit = new Unit(u_e);
        assertFalse(unit.isValid());
        abcd.setD(0.0);
        unit = new Unit(u_e);
        assertFalse(unit.isValid());
        abcd.setC(1.0);
        unit = new Unit(u_e);
        assertFalse(unit.isValid());
        abcd.setB(1.0);
        unit = new Unit(u_e);
        assertTrue(unit.isValid());
    }
}
