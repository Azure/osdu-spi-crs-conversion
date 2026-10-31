package org.opengroup.osdu.crs.sis.operation;

import org.junit.Test;
import org.opengroup.osdu.crs.model.ICrs;
import org.opengroup.osdu.crs.model.IEarlyBoundCrs;
import org.opengroup.osdu.crs.model.ISingleTrf;
import org.opengroup.osdu.crs.model.ReferenceConverter;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.util.ConstantsTests;

import static org.junit.Assert.*;

public class ExplicitTransformBehaviorTests {

    private ICRSCoordinateOperation operation(boolean inverse, boolean threeD) {
        IEarlyBoundCrs agd = (IEarlyBoundCrs) ReferenceConverter.parseSpatialReference(
                ConstantsTests.EB_AGD66_AMG56_1108[ConstantsTests.V2]);
        ICrs wgs = (ICrs) ReferenceConverter.parseSpatialReference(ConstantsTests.LB_WGS84[ConstantsTests.V2]);
        assertTrue(agd.isValid());
        assertTrue(wgs.isValid());
        ISisCrs source = inverse ? wgs.getBaseGeographicCrs() : agd.getBaseGeographicCrs();
        ISisCrs target = inverse ? agd.getBaseGeographicCrs() : wgs.getBaseGeographicCrs();
        ISingleTrf transform = (ISingleTrf) agd.getTrf();
        ICRSCoordinateOperation result = inverse
                ? new ExplicitInverseTransformFromCode(source, target, transform)
                : new ExplicitTransformFromCode(source, target, transform);
        assertTrue(result.supports3DPointConversion());
        result.enable3DPointConversion(threeD);
        return result;
    }

    @Test
    public void explicitBatchesTransformValidPointsAndPropagateMissingCoordinates() {
        double[] xy = {151, -33, 152, -34, Double.NaN, 0};
        double[] z = {100, 200, 300};
        assertEquals(2, operation(false, false).convertPoints(xy, z).getSuccessCount());
        assertTrue(Double.isNaN(xy[4]) && Double.isNaN(xy[5]) && Double.isNaN(z[2]));
        assertEquals(2, operation(true, false).convertPoints(xy, z).getSuccessCount());
        assertArrayEquals(new double[]{151, -33, 152, -34},
                new double[]{xy[0], xy[1], xy[2], xy[3]}, 1e-6);
        assertEquals(100, z[0], 0);
        assertEquals(200, z[1], 0);
    }

    @Test
    public void geocentricThreeDimensionalSingleAndBatchConversionsAgree() throws Exception {
        double[] original = {151, -33, 1234};
        double[] wgs = operation(false, true).convertSinglePoint(original[0], original[1], original[2]);
        // Independent geocentric translation: AGD66 ellipsoid, (-133, -48, 148) metres, WGS84 ellipsoid.
        assertArrayEquals(new double[]{151.001138950832, -32.9984192452431, 1254.2618897371}, wgs, 1e-6);
        assertTrue(Math.abs(wgs[2] - original[2]) > 1);
        double[] xy = {151, -33, 152, -34};
        double[] z = {1234, 321};
        assertEquals(2, operation(false, true).convertPoints(xy, z).getSuccessCount());
        assertArrayEquals(wgs, new double[]{xy[0], xy[1], z[0]}, 1e-8);
        assertEquals(151.001, xy[0], 0.001);
        assertEquals(-32.999, xy[1], 0.001);
        ICRSCoordinateOperation inverse = operation(true, true);
        double[] singleInverse = inverse.convertSinglePoint(xy[0], xy[1], z[0]);
        assertEquals(2, operation(true, true).convertPoints(xy, z).getSuccessCount());
        assertArrayEquals(singleInverse, new double[]{xy[0], xy[1], z[0]}, 1e-8);
    }
}
