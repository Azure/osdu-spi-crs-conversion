package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.util.ConstantsTests;

import static org.junit.Assert.*;
import org.junit.Test;

public class ConvertOperationStateTest {
    private ICrs getLbCrs(){
        return (ICrs) ReferenceConverter.parseSpatialReference(ConstantsTests.LB_Trinidad_30200[ConstantsTests.V2]);
    }
    private ICrs getEbCrs(){
        return (ICrs) ReferenceConverter.parseSpatialReference(ConstantsTests.EB_Trinidad_30200_10085[ConstantsTests.V2]);
    }

    @Test
    public void testConstructor(){
        double[] xy = {-100, 28};
        double[] zs = {0.0};

        ConvertOperationState state = new ConvertOperationState(getLbCrs(), getEbCrs(), xy, zs);
        assertNotNull(state);
        assertNull(state.getSourceTrf());
        assertNotNull(state.getTargetTrf());
        assertTrue(state.isValid());
        // assertNotNull(state.getSourceGeographic());
        // assertNotNull(state.getTargetGeographic());
        // assertNotNull(state.getSourceProjected());
        // assertNotNull(state.getTargetProjected());
    }

    @Test
    public void testNullCrs(){
        double[] xy = {-100, 28};
        double[] zs = {0.0};
        double[] empty = {};

        ConvertOperationState state = new ConvertOperationState(null, null, xy, zs);
        assertNotNull(state);
        assertFalse(state.isValid());
        assertNull(state.getSourceCrs());
        assertNull(state.getSourceTrf());
        // assertNull(state.getSourceGeographic());
        // assertNull(state.getSourceProjected());
        // assertNull(state.getTargetCrs());
        // assertNull(state.getTargetTrf());
        // assertNull(state.getTargetGeographic());
        // assertNull(state.getTargetProjected());
        state = new ConvertOperationState(getEbCrs(), null, xy, zs);
        assertFalse(state.isValid());
        state = new ConvertOperationState(null, getEbCrs(), xy, zs);
        assertFalse(state.isValid());
        state = new ConvertOperationState(getEbCrs(), getEbCrs(), null, zs);
        assertFalse(state.isValid());
        state = new ConvertOperationState(getEbCrs(), getEbCrs(), xy, null);
        assertFalse(state.isValid());
        state = new ConvertOperationState(getEbCrs(), getEbCrs(), xy, xy);
        assertFalse(state.isValid());
        state = new ConvertOperationState(getEbCrs(), getEbCrs(), empty, xy);
        assertFalse(state.isValid());
        state = new ConvertOperationState(getEbCrs(), getEbCrs(), xy, empty);
        assertFalse(state.isValid());
        state = new ConvertOperationState(getEbCrs(), getEbCrs(), empty, empty);
        assertFalse(state.isValid());
    }
}
