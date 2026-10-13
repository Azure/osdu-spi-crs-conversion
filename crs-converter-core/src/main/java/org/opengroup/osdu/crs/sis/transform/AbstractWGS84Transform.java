package org.opengroup.osdu.crs.sis.transform;

import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.crs.AbstractCRS;
import org.apache.sis.referencing.cs.AxesConvention;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.operation.transform.MathTransforms;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Matrix;
import org.opengis.util.FactoryException;

/**
 * Abstract class that serves as the parent for both ConcatenatedWGS84TransformFromCode and SingleWGS84TransformFromCode.
 * It implements the common methods for both concatenated and single transforms.
 */
abstract class AbstractWGS84Transform implements IWGS84Transform {

    /**
     * This method:
     * 1. Normalizes the X and Y axis directions order of the input CRS.
     * <p>
     * 2. Checks if the latitude/longitude of the transformation are specified in the proper order.
     * <p>
     * 3. Concatenates two operations: from the source CRS to the CRS expected
     *    by the operation as inputs, then the operation itself.
     *
     * @param crs The coordinate reference system to normalize.
     * @param datumOperation The coordinate operation to concatenate.
     * @return The concatenated math transform.
     * @throws FactoryException If an error occurs during the creation of the transform.
     */
    protected static MathTransform normalizeAndConcatenate(CoordinateReferenceSystem crs, CoordinateOperation datumOperation) throws FactoryException {

        crs = AbstractCRS.castOrCopy(crs).forConvention(AxesConvention.DISPLAY_ORIENTED);

        CoordinateSystemAxis firstAxis = datumOperation.getSourceCRS().getCoordinateSystem().getAxis(0);
        boolean longLatOrder = firstAxis.getDirection() != AxisDirection.NORTH;

        CoordinateOperation xyToDatumOperation = CRS.findOperation(crs, datumOperation.getSourceCRS(), null);

        MathTransform step1 = xyToDatumOperation.getMathTransform();
        MathTransform step2 = datumOperation.getMathTransform();
        MathTransform concatMathTransform = MathTransforms.concatenate(step1, step2);
        if (!longLatOrder) {
            Matrix swapMatrix = Matrices.createTransform(
                    new AxisDirection[]{AxisDirection.NORTH, AxisDirection.EAST},
                    new AxisDirection[]{AxisDirection.EAST, AxisDirection.NORTH});

            concatMathTransform = MathTransforms.concatenate(concatMathTransform, MathTransforms.linear(swapMatrix));
        }

        return concatMathTransform;
    }
}