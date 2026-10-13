package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.model.Impl.Unit;
import org.opengroup.osdu.crs.util.ConstantsTests;
import org.junit.Test;

import static org.opengroup.osdu.crs.model.ReferenceConverter.parseUnitReference;
import static org.junit.Assert.*;

public class UnitsTest {


    @Test
    public void testValidLengthUnits()
    {
        IUnit m = parseUnitReference(ConstantsTests.UNIT_M_v2);
        assertNotNull(m);
        assertTrue(m.isValid());
        assertTrue(m.isLength());
        IUnit m_e = parseUnitReference(ConstantsTests.UNIT_M_v2.replace("Length", "L"));
        assertNotNull(m_e);
        assertTrue(m_e.isValid());
        assertTrue(m_e.isLength());
        assertEquals(1.0, m.convertToUnit(m_e), 1.0e-11);
        assertEquals(1.0, m_e.convertToUnit(m), 1.0e-11);
        IUnit ftUS_o = parseUnitReference(ConstantsTests.UNIT_FT_US);
        assertNotNull(ftUS_o);
        assertTrue(ftUS_o.isValid());
        assertTrue(ftUS_o.isLength());
        assertFalse(ftUS_o.isAngle());
        assertEquals(0.3048006096012192, ftUS_o.scaleToSI(), 1.0e-11);
        IUnit ftUS_e = parseUnitReference(ConstantsTests.UNIT_FT_US_E);
        assertNotNull(ftUS_e);
        assertTrue(ftUS_e.isValid());
        assertTrue(ftUS_e.isLength());
        assertFalse(ftUS_e.isAngle());
        assertEquals(0.3048006096012192, ftUS_e.scaleToSI(), 1.0e-11);
        IUnit ft = parseUnitReference(ConstantsTests.UNIT_FT);
        assertNotNull(ft);
        assertTrue(ft.isValid());
        assertTrue(ft.isLength());
        assertFalse(ft.isAngle());
    }
    @Test
    public void testValidAngleUnits()
    {
        IUnit rad = parseUnitReference(ConstantsTests.UNIT_RAD);
        assertNotNull(rad);
        assertTrue(rad.isValid());
        IUnit dega = parseUnitReference(ConstantsTests.UNIT_DEGA);
        assertNotNull(dega);
        assertFalse(dega.isLength());
        assertTrue(dega.isAngle());
        assertTrue(dega.isValid());
        assertEquals(Math.PI/180.0, dega.scaleToSI(), 1.0e-11);
        IUnit grad = parseUnitReference(ConstantsTests.UNIT_GRAD);
        assertNotNull(grad);
        assertTrue(grad.isValid());
        assertFalse(grad.isLength());
        assertTrue(grad.isAngle());
        assertEquals(Math.PI/200, grad.scaleToSI(), 1.0e-11);
    }

    @Test
    public void testInvalidUnits(){
        IUnit m = parseUnitReference(ConstantsTests.UNIT_M);
        IUnit deg = parseUnitReference(ConstantsTests.UNIT_DEGA_v2);
        assertNotNull(m);
        assertTrue(m.isValid());
        assertNotNull(deg);
        assertTrue(deg.isValid());
	    String wrongDimension = "%7B%22ScaleOffset%22%3A%7B%22Scale%22%3A1.0%2C%22Offset%22%3A0.0%7D%2C%22Symbol%22%3A%22F%22%2C%22BaseMeasurement%22%3A%22%257B%2522Ancestry%2522%253A%2522Capacitance%2522%257D%22%7D";
	    IUnit F = parseUnitReference(wrongDimension);
	    assertNotNull(F);
	    assertFalse(F.isValid());
	    assertTrue(Double.isNaN(F.scaleToSI()));
	    assertTrue(Double.isNaN(F.convertToUnit(m)));
	    assertTrue(Double.isNaN(m.convertToUnit(F)));
	    String corruptUnit = ConstantsTests.UNIT_FT_US.replace("ScaleOffset", "Corrupted");
	    F = parseUnitReference(corruptUnit);
        assertNotNull(F);
        assertFalse(F.isValid());
        assertFalse(F.isLength());
        assertFalse(F.isAngle());
        // now the same with V2 units
        wrongDimension = ConstantsTests.UNIT_DEGA_v2.replace("A", "Capacitance");
        F = parseUnitReference(wrongDimension);
        assertNotNull(F);
        assertFalse(F.isValid());
        assertTrue(Double.isNaN(F.scaleToSI()));
        assertTrue(Double.isNaN(F.convertToUnit(m)));
        assertTrue(Double.isNaN(m.convertToUnit(F)));
        corruptUnit = ConstantsTests.UNIT_DEGA_v2.replace("\"a\"", "\"Corrupted\"");
        F = parseUnitReference(corruptUnit);
        assertNotNull(F);
        assertFalse(F.isValid());
        assertFalse(F.isLength());
        assertFalse(F.isAngle());
        corruptUnit = ConstantsTests.UNIT_DEGA_v2.replace("3.14159265358979", "NaN");
        F = parseUnitReference(corruptUnit);
        assertNotNull(F);
        assertFalse(F.isValid());
        corruptUnit = ConstantsTests.UNIT_DEGA_v2.replace("180.0", "NaN");
        F = parseUnitReference(corruptUnit);
        assertNotNull(F);
        assertFalse(F.isValid());
        corruptUnit = ConstantsTests.UNIT_DEGA_v2.replace("0.0", "NaN");
        F = parseUnitReference(corruptUnit);
        assertNotNull(F);
        assertFalse(F.isValid());
        assertTrue(Double.isNaN(F.convertToUnit(deg)));
        assertTrue(Double.isNaN(deg.convertToUnit(F)));
        corruptUnit = ConstantsTests.UNIT_GRAD_v2.replace("0.015707963267949", "NaN");
        F = parseUnitReference(corruptUnit);
        assertNotNull(F);
        assertFalse(F.isValid());
        corruptUnit = ConstantsTests.UNIT_GRAD_v2.replace("0.0", "NaN");
        F = parseUnitReference(corruptUnit);
        assertNotNull(F);
        assertFalse(F.isValid());
        assertTrue(Double.isNaN(F.convertToUnit(deg)));
        assertTrue(Double.isNaN(deg.convertToUnit(F)));
        F = parseUnitReference("%7BNothing%7D");
        assertNotNull(F);
        assertFalse(F.isValid());
        F = parseUnitReference("{Nothing}");
        assertNotNull(F);
        assertFalse(F.isValid());
    }

	@Test
    public void testUnitConversion(){
        IUnit ftUS_O = parseUnitReference(ConstantsTests.UNIT_FT_US);
        IUnit ftUS_E = parseUnitReference(ConstantsTests.UNIT_FT_US);
	    IUnit m = parseUnitReference(ConstantsTests.UNIT_M);
        assertNotNull(ftUS_E);
        assertNotNull(ftUS_O);
        assertNotNull(m);
	    double scale = ftUS_O.convertToUnit(m);
	    assertEquals(0.3048006096012192, scale, 1.0e-11);
        IUnit ft = parseUnitReference(ConstantsTests.UNIT_FT);
        scale = m.convertToUnit(ft);
        assertEquals(1.0/0.3048, scale, 1.0e-11);
        scale = ftUS_E.convertToUnit(m);
        assertEquals(0.3048006096012192, scale, 1.0e-11);
        scale = ftUS_E.convertToUnit(ftUS_O);
        assertEquals(1.0, scale, 1.0e-11);
        scale = ftUS_O.convertToUnit(ftUS_E);
        assertEquals(1.0, scale, 1.0e-11);
        scale = ftUS_E.convertToUnit(ftUS_E);
        assertEquals(1.0, scale, 1.0e-11);
        IUnit grad = parseUnitReference(ConstantsTests.UNIT_GRAD);
        assertNotNull(grad);
        IUnit dega = parseUnitReference(ConstantsTests.UNIT_DEGA);
        assertNotNull(dega);
        assertEquals(200.0/180.0, dega.convertToUnit(grad), 1.0e-11);
    }

    @Test
    public void testUnitSerialization(){
        IUnit unit = parseUnitReference(ConstantsTests.UNIT_FT_US);
        String pr = unit.createPersistableReference();
        assertNotNull(pr);
        IUnit copy = parseUnitReference(pr);
        assertEquals(unit.getSymbol(), copy.getSymbol());
        assertEquals(unit.getScale(), copy.getScale(), 1.0e-11);
        assertEquals(unit.getOffset(), copy.getOffset(), 1.0e-11);
        assertEquals(unit.isLength(), copy.isLength());
        assertEquals(unit.isAngle(), copy.isAngle());

        unit = parseUnitReference(ConstantsTests.UNIT_FT_US_E_v2);
        pr = unit.createPersistableReference();
        assertNotNull(pr);
        copy = parseUnitReference(pr);
        assertEquals(unit.getSymbol(), copy.getSymbol());
        assertEquals(unit.getScale(), copy.getScale(), 1.0e-11);
        assertEquals(unit.getOffset(), copy.getOffset(), 1.0e-11);
        assertEquals(unit.isLength(), copy.isLength());
        assertEquals(unit.isAngle(), copy.isAngle());

        unit = parseUnitReference(ConstantsTests.UNIT_DEGA);
        pr = unit.createPersistableReference();
        assertNotNull(pr);
        copy = parseUnitReference(pr);
        assertEquals(unit.getSymbol(), copy.getSymbol());
        assertEquals(unit.getScale(), copy.getScale(), 1.0e-11);
        assertEquals(unit.getOffset(), copy.getOffset(), 1.0e-11);
        assertEquals(unit.isLength(), copy.isLength());
        assertEquals(unit.isAngle(), copy.isAngle());

        unit = parseUnitReference(ConstantsTests.UNIT_DEGA_v2);
        pr = unit.createPersistableReference();
        assertNotNull(pr);
        copy = parseUnitReference(pr);
        assertEquals(unit.getSymbol(), copy.getSymbol());
        assertEquals(unit.getScale(), copy.getScale(), 1.0e-11);
        assertEquals(unit.getOffset(), copy.getOffset(), 1.0e-11);
        assertEquals(unit.isLength(), copy.isLength());
        assertEquals(unit.isAngle(), copy.isAngle());
    }

    @Test
    public void testUnitSerializationError(){
        IUnit empty = new Unit();
        assertFalse(empty.isValid());
        String pr = empty.createPersistableReference();
        assertNotNull(pr);
        IUnit copy = parseUnitReference(pr);
        assertNotNull(copy);
        assertFalse(copy.isValid());
        assertEquals(copy, empty);
    }
}
