package org.opengroup.osdu.crs.sis.transform;

import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.crs.AbstractCRS;
import org.apache.sis.referencing.cs.AxesConvention;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengroup.osdu.crs.model.ICompoundTrf;
import org.opengroup.osdu.crs.model.ICrs;
import org.opengroup.osdu.crs.model.ReferenceConverter;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.sis.SisCrs;
import org.opengroup.osdu.crs.util.ConstantsTests;

import static org.junit.Assert.*;

public class SisTransformBehaviorTests {

    private SingleWGS84TransformFromCrs utmTransform(boolean negative) throws Exception {
        ICrs parsed = (ICrs) ReferenceConverter.parseSpatialReference(
                ConstantsTests.LB_WGS84_UTM11N[ConstantsTests.V2]);
        assertTrue(parsed.isValid());
        ISisCrs original = parsed.getProjectedCrs();
        ISisCrs crs = new SisCrs(original.getCoordinateReferenceSystem(), negative,
                original.getWkt(), original.getAuthorityCode(), original.getName());
        CoordinateReferenceSystem xy = AbstractCRS.castOrCopy(crs.getCoordinateReferenceSystem())
                .forConvention(AxesConvention.DISPLAY_ORIENTED);
        CoordinateReferenceSystem wgs = AbstractCRS.castOrCopy(CRS.forCode("EPSG:4326"))
                .forConvention(AxesConvention.DISPLAY_ORIENTED);
        SisMathTransformFromCrs operation = new SisMathTransformFromCrs(
                CRS.findOperation(xy, wgs, null), CRS.findOperation(wgs, xy, null));
        assertTrue(operation.isEqual(new SisMathTransformFromCrs(
                operation.getToWGS84Operation(), operation.getFromWGS84Operation())));
        assertFalse(operation.supports3DPointConversion());
        return new SingleWGS84TransformFromCrs(crs, operation);
    }

    @Test
    public void negativeScaleAppliesToBothAxesInBothDirections() throws Exception {
        SingleWGS84TransformFromCrs transform = utmTransform(true);
        assertArrayEquals(new double[]{-117, 42, -50},
                transform.transformSingleXYPointToWGS84(-500000, -4649776.224819, -50), 1e-7);
        assertArrayEquals(new double[]{-500000, -4649776.224819, -50},
                transform.transformSingleWGS84PointToXY(-117, 42, -50), 0.001);
        double[] xy = {-500000, -4649776.224819, Double.NaN, 0};
        double[] z = {23, 24};
        assertEquals(1, transform.transformXYPointsToWGS84(xy, z).getSuccessCount());
        assertArrayEquals(new double[]{-117, 42}, new double[]{xy[0], xy[1]}, 1e-7);
        assertTrue(Double.isNaN(xy[2]) && Double.isNaN(xy[3]) && Double.isNaN(z[1]));
        assertEquals(1, transform.transformWGS84PointsToXY(xy, z).getSuccessCount());
        assertArrayEquals(new double[]{-500000, -4649776.224819},
                new double[]{xy[0], xy[1]}, 0.001);
        assertEquals(23, z[0], 0);
    }

    private CompoundFallbackWGS84TransformWithCode fallback() throws Exception {
        ICrs crs = (ICrs) ReferenceConverter.parseSpatialReference(
                ConstantsTests.EB_NAD27_UTM11_Fallback[ConstantsTests.V2]);
        ICompoundTrf description = (ICompoundTrf) ReferenceConverter.parseSpatialReference(
                ConstantsTests.CTRF_NAD27_FALLBACK[ConstantsTests.V2]);
        return new CompoundFallbackWGS84TransformWithCode(crs.getBaseGeographicCrs(), description);
    }

    @Test
    public void fallbackDatumTransformUsesGridInsideNorthAmericaAndRejectsOutside() throws Exception {
        CompoundFallbackWGS84TransformWithCode transform = fallback();
        double[] xy = {-117, 35, 0, 0, Double.NaN, 35};
        double[] z = {75, 20, 10};
        assertEquals(1, transform.transformXYPointsToWGS84(xy, z).getSuccessCount());
        assertEquals(-117.0009, xy[0], 0.0002);
        assertEquals(35, xy[1], 0.0002);
        assertEquals(75, z[0], 0);
        for (int i = 2; i < xy.length; i++) {
            assertTrue(Double.isNaN(xy[i]));
        }
        assertTrue(Double.isNaN(z[1]) && Double.isNaN(z[2]));
        assertArrayEquals(new double[]{Double.NaN, 1, 2},
                transform.transformSingleXYPointToWGS84(Double.NaN, 1, 2), 0);
        assertArrayEquals(new double[]{Double.NaN, 1, 2},
                transform.transformSingleWGS84PointToXY(Double.NaN, 1, 2), 0);
        assertArrayEquals(new double[]{Double.NaN, Double.NaN, Double.NaN},
                transform.transformSingleXYPointToWGS84(0, 0, 2), 0);
        assertArrayEquals(new double[]{Double.NaN, Double.NaN, Double.NaN},
                transform.transformSingleWGS84PointToXY(0, 0, 2), 0);
        assertFalse(transform.supports3dPointConversion());
        transform.enable3DPointConversion(true);
    }

    @Test
    public void fallbackInverseBatchPreservesHeightAndMarksFailedPoints() throws Exception {
        CompoundFallbackWGS84TransformWithCode transform = fallback();
        double[] xy = {-117.0009, 35, 0, 0, Double.NaN, 35};
        double[] z = {75, 20, 10};
        assertEquals(1, transform.transformWGS84PointsToXY(xy, z).getSuccessCount());
        assertEquals(-117, xy[0], 0.0002);
        assertEquals(35, xy[1], 0.0002);
        assertEquals(75, z[0], 0);
        assertTrue(Double.isNaN(xy[2]) && Double.isNaN(xy[3]) && Double.isNaN(z[1]));
        assertTrue(Double.isNaN(xy[4]) && Double.isNaN(xy[5]) && Double.isNaN(z[2]));
    }
}
