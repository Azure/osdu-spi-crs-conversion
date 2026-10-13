package org.opengroup.osdu.crs.sis.transform;

import org.apache.sis.referencing.operation.transform.MathTransforms;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengroup.osdu.crs.model.ICompoundTrf;
import org.opengroup.osdu.crs.model.ISingleTrf;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.sis.MathTransformUtils;
import org.opengroup.osdu.crs.sis.operation.OperationResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ConcatenatedWGS84TransformFromCode class is designed to support concatenated transformations
 * in the OSDU CRS Conversion Service. Concatenated transformations involve combining two or more
 * individual transformations into a single compound transformation, enabling complex coordinate
 * reference system (CRS) conversions.
 * <p>
 * This class implements the IWGS84Transform interface and uses a list of single transformations
 * (ISingleTrf) to perform the concatenated transformation. It ensures that the compound transformation
 * is valid before applying it.
 * <p>
 * The class constructor initializes the transformations and the CRS name using the provided ISisCrs
 * and ICompoundTrf instances. If the compound transformation is invalid, it throws an
 * IllegalArgumentException.
 *
 * @see ISingleTrf
 * @see ISisCrs
 * @see ICompoundTrf
 * @see IWGS84Transform
 */
public class ConcatenatedWGS84TransformFromCode extends AbstractWGS84Transform {

    private static final Logger log = Logger.getLogger(ConcatenatedWGS84TransformFromCode.class.getName());

    private MathTransform forwardMathTransform;
    private MathTransform invertedMathTransform;
    private final String crsName;
    private final String transformName;

    public ConcatenatedWGS84TransformFromCode(ISisCrs sisCrs, ICompoundTrf compoundTransform) throws Exception {
        if (!compoundTransform.isValid()) {
            throw new IllegalArgumentException("Invalid transform " + compoundTransform.getName());
        }

        this.crsName = sisCrs.getName();
        this.transformName = compoundTransform.getName();
        ISisMathTransform sisMathTransform = compoundTransform.getTransformOperation();
        if(sisMathTransform != null) {
            forwardMathTransform = normalizeAndConcatenate(sisCrs.getCoordinateReferenceSystem(), sisMathTransform.getFromWGS84Operation());
        } else {
            MathTransform[] fromMathTransforms = compoundTransform.getTransformations().stream()
                    .map(trf -> trf.getTransformOperation().getFromWGS84Operation().getMathTransform())
                    .toArray(MathTransform[]::new);

            MathTransform tempMathTransform = fromMathTransforms[0];

            for (int i = 1; i < fromMathTransforms.length; i++) {
                tempMathTransform = MathTransforms.concatenate(tempMathTransform, fromMathTransforms[i]);
            }
            forwardMathTransform = tempMathTransform;
        }
        invertedMathTransform = forwardMathTransform.inverse();
    }

    @Override
    public double[] transformSingleXYPointToWGS84(double x, double y, double z) throws FactoryException, MismatchedDimensionException, TransformException {
        double[] xyValues = new double[] {x, y};
        double[] zValues = new double[] {z};

        transformXYPointsToWGS84(xyValues, zValues);

        return new double[] {xyValues[0], xyValues[1], zValues[0]};
    }

    @Override
    public double[] transformSingleWGS84PointToXY(double longitude, double latitude, double z) throws FactoryException, MismatchedDimensionException, TransformException {
        double[] xyValues = new double[] {longitude, latitude};
        double[] zValues = new double[] {z};

        transformWGS84PointsToXY(xyValues, zValues);

        return new double[] {xyValues[0], xyValues[1], zValues[0]};
    }

    @Override
    public OperationResponse transformWGS84PointsToXY(double[] xyValues, double[] zValues) {
        int numPoints = xyValues.length / 2;
        double[] convertedXYValues = new double[xyValues.length];
        double[] convertedZValues = new double[zValues.length];

        Arrays.fill(convertedXYValues, Double.NaN);
        Arrays.fill(convertedZValues, Double.NaN);

        try {
            MathTransformUtils.transformMultiplePoints(invertedMathTransform, xyValues, zValues, convertedXYValues, convertedZValues);
        } catch (TransformException e) {
            log.log(Level.WARNING, "Exception occurred while transforming points: ", e);
        }

        List<String> operations = new ArrayList<>();
        int totalSuccessCount = 0;

        for (int i = 0; i < numPoints; i++) {
            double currentX = xyValues[i * 2];
            double currentY = xyValues[(i * 2) + 1];
            if (Double.isNaN(currentX) || Double.isNaN(currentY)) {
                xyValues[i * 2] = Double.NaN;
                xyValues[(i * 2) + 1] = Double.NaN;
                zValues[i] = Double.NaN;
            } else {
                totalSuccessCount++;
            }
        }

        operations.add(String.format("transformation %s to %s using %s; %d points successfully transformed", crsName, "GCS_WGS_1984",
                transformName, totalSuccessCount));

        System.arraycopy(convertedXYValues, 0, xyValues, 0, xyValues.length);
        System.arraycopy(convertedZValues, 0, zValues, 0, zValues.length);

        return new OperationResponse(operations, totalSuccessCount);
    }

    @Override
    public OperationResponse transformXYPointsToWGS84(double[] xyValues, double[] zValues) {
        int numPoints = xyValues.length / 2;
        double[] convertedXYValues = new double[xyValues.length];
        double[] convertedZValues = new double[zValues.length];

        Arrays.fill(convertedXYValues, Double.NaN);
        Arrays.fill(convertedZValues, Double.NaN);

        try {
            MathTransformUtils.transformMultiplePoints(forwardMathTransform, xyValues, zValues, convertedXYValues, convertedZValues);
        } catch (TransformException e) {
            log.log(Level.WARNING, "Exception occurred while transforming points: ", e);
        }

        List<String> operations = new ArrayList<>();
        int totalSuccessCount = 0;

        for (int i = 0; i < numPoints; i++) {
            double currentX = xyValues[i * 2];
            double currentY = xyValues[(i * 2) + 1];
            if (Double.isNaN(currentX) || Double.isNaN(currentY)) {
                xyValues[i * 2] = Double.NaN;
                xyValues[(i * 2) + 1] = Double.NaN;
                zValues[i] = Double.NaN;
            } else {
                totalSuccessCount++;
            }
        }

        operations.add(String.format("transformation %s to %s using %s; %d points successfully transformed", crsName, "GCS_WGS_1984",
                transformName, totalSuccessCount));

        System.arraycopy(convertedXYValues, 0, xyValues, 0, xyValues.length);
        System.arraycopy(convertedZValues, 0, zValues, 0, zValues.length);

        return new OperationResponse(operations, totalSuccessCount);

    }

    @Override
    public boolean supports3dPointConversion() {
        //for now don't support 3d point conversion for compound transforms. OSDU trackable issue 175 covers the problem
        return false;
    }

    @Override
    public void enable3DPointConversion(boolean enable) {
        //for now don't support 3d point conversion for compound transforms. OSDU trackable issue 175 covers the problem
    }
}
