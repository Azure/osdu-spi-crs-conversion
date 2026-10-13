package org.opengroup.osdu.crs.sis.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.crs.AbstractCRS;
import org.apache.sis.referencing.cs.AxesConvention;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.operation.transform.MathTransforms;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.SingleOperation;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengroup.osdu.crs.model.ISingleTrf;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.sis.MathTransformUtils;
import org.opengroup.osdu.crs.sis.operation.CRSTransformToWGS84Operation;
import org.opengroup.osdu.crs.sis.operation.ICRSCoordinateOperation;
import org.opengroup.osdu.crs.sis.operation.OperationResponse;

public class ExplicitTransformFromCode implements ICRSCoordinateOperation {

    private static final Logger LOGGER = Logger.getLogger(CRSTransformToWGS84Operation.class.getName());

    private CoordinateOperation datumOperation = null;
    private MathTransform explicitMathTransform = null;

    private boolean fromNegativeSacle = false;
    private boolean toNegativeSacle = false;
    private boolean longLatOrder = true;
    private String fromCrsName;
    private String toCrsName;
    private ISingleTrf transform;
    private boolean use3DPointConversion = false;

    private CoordinateReferenceSystem fromCrs;
    private CoordinateReferenceSystem toCrs;

    public ExplicitTransformFromCode(ISisCrs fromSisCrs, ISisCrs toSisCrs, ISingleTrf transform) {
        this.transform = transform;
        this.datumOperation = transform.getTransformOperation().getFromWGS84Operation();

        this.fromCrs = fromSisCrs.getCoordinateReferenceSystem();
        this.toCrs = toSisCrs.getCoordinateReferenceSystem();

        this.fromCrs = AbstractCRS.castOrCopy(fromCrs).forConvention(AxesConvention.DISPLAY_ORIENTED);
        this.toCrs = AbstractCRS.castOrCopy(toCrs).forConvention(AxesConvention.DISPLAY_ORIENTED);

        this.fromNegativeSacle = fromSisCrs.isNegativeScale();
        this.toNegativeSacle = toSisCrs.isNegativeScale();
        this.fromCrsName = fromSisCrs.getName();
        this.toCrsName = toSisCrs.getName();
        /*
         * Check if the lat/long are specified in the proper order
         */
        CoordinateSystemAxis firstAxis = datumOperation.getSourceCRS().getCoordinateSystem().getAxis(0);
        this.longLatOrder = firstAxis.getDirection() != AxisDirection.NORTH;
    }

    @Override
    public double[] convertSinglePoint(double x, double y, double z) throws Exception {
        try {
            double[] point = transformSinglePoint(x, y, z);
            return point;
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Can't convert point", ex);
            return new double[]{Double.NaN, Double.NaN, Double.NaN};
        }
    }

    private double[] transformSinglePoint(double x, double y, double z) throws FactoryException, MismatchedDimensionException, TransformException {
        // Temporary workaround because library does not support negative scale factors
        if (fromNegativeSacle) {
            x = -x;
            y = -y;
        }

        if (explicitMathTransform == null) {
            initializeExplicitTransform();
        }

        double[] coordinate = MathTransformUtils.transformSinglePoint(explicitMathTransform, x, y, z);

        // Temporary workaround because library does not support negative scale factors
        if (toNegativeSacle) {
            coordinate[0] = -coordinate[0];
            coordinate[1] = -coordinate[1];
        }

        double[] response = new double[3];
        response[0] = coordinate[0];
        response[1] = coordinate[1];
        response[2] = coordinate[2];

        return response;
    }

    @Override
    public OperationResponse convertPoints(double[] xyValues, double[] zValues) {
        int numPoints = xyValues.length / 2;
        int successfulConversionCount = 0;
        try {
            if (explicitMathTransform == null) {
                initializeExplicitTransform();
            }

            double[] xyValuesToConvert = new double[xyValues.length];
            if (fromNegativeSacle) {
                for (int i = 0; i < xyValues.length; i++) {
                    xyValuesToConvert[i] = -xyValues[i];
                }
            } else {
                System.arraycopy(xyValues, 0, xyValuesToConvert, 0, xyValues.length);
            }

            double[] convertedXYValues = new double[xyValues.length];
            double[] convertedZValues = new double[zValues.length];
            try {
                //try to convert all points at once
                MathTransformUtils.transformMultiplePoints(explicitMathTransform, xyValuesToConvert, zValues, convertedXYValues, convertedZValues);
            } catch (Exception ex) {
                //will need to convert each point individually
                return fallbackTransformPoints(xyValues, zValues);
            }

            if (toNegativeSacle) {
                for (int i = 0; i < convertedXYValues.length; i++) {
                    convertedXYValues[i] = -convertedXYValues[i];
                }
            }

            System.arraycopy(convertedXYValues, 0, xyValues, 0, convertedXYValues.length);
            System.arraycopy(convertedZValues, 0, zValues, 0, convertedZValues.length);

            for (int i = 0; i < numPoints; i++) {
                double currentX = xyValues[i * 2];
                double currentY = xyValues[(i * 2) + 1];

                if (Double.isNaN(currentX) || Double.isNaN(currentY)) {
                    xyValues[i * 2] = Double.NaN;
                    xyValues[(i * 2) + 1] = Double.NaN;
                    zValues[i] = Double.NaN;
                } else {
                    successfulConversionCount++;
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Can't transform points", ex);
        }

        List<String> operations = new ArrayList<>();
        operations.add(String.format("transformation %s to %s using %s; %d points successfully transformed", fromCrsName, toCrsName,
                transform.getName(), successfulConversionCount));
        return new OperationResponse(operations, successfulConversionCount);
    }

    private OperationResponse fallbackTransformPoints(double[] xyValues, double[] zValues) {
        int numPoints = xyValues.length / 2;
        int successfulConversionCount = 0;
        for (int i = 0; i < numPoints; i++) {
            double currentX = xyValues[i * 2];
            double currentY = xyValues[(i * 2) + 1];
            double currentZ = zValues[i];
            if (Double.isNaN(currentX) || Double.isNaN(currentY)) {
                xyValues[i * 2] = Double.NaN;
                xyValues[(i * 2) + 1] = Double.NaN;
                zValues[i] = Double.NaN;
                continue;
            }
            try {
                double[] point = transformSinglePoint(currentX, currentY, currentZ);
                xyValues[i * 2] = point[0];
                xyValues[(i * 2) + 1] = point[1];
                zValues[i] = point[2];
                successfulConversionCount++;

            } catch (Exception ex) {
                xyValues[i * 2] = Double.NaN;
                xyValues[(i * 2) + 1] = Double.NaN;
                zValues[i] = Double.NaN;
            }
        }
        List<String> operations = new ArrayList<>();
        operations.add(String.format("transformation %s to %s using %s; %d points successfully transformed", fromCrsName, toCrsName,
                transform.getName(), successfulConversionCount));
        return new OperationResponse(operations, successfulConversionCount);
    }

    private void initializeExplicitTransform() throws MismatchedDimensionException, FactoryException {
        CoordinateOperation fromDatumOperation = CRS.findOperation(fromCrs, datumOperation.getSourceCRS(), null);
        if (use3DPointConversion) {
            try {
                explicitMathTransform = create3DExplicitMathTransform();
                return;
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Can't create 3d transformation", ex);
            }
        }
        MathTransform step1 = fromDatumOperation.getMathTransform();
        MathTransform step2 = datumOperation.getMathTransform();
        explicitMathTransform = MathTransforms.concatenate(step1, step2);
        if (!longLatOrder) {
            Matrix swapMatrix = Matrices.createTransform(
                    new AxisDirection[]{AxisDirection.NORTH, AxisDirection.EAST},
                    new AxisDirection[]{AxisDirection.EAST, AxisDirection.NORTH});

            explicitMathTransform = MathTransforms.concatenate(explicitMathTransform, MathTransforms.linear(swapMatrix));
        }
    }

    private MathTransform create3DExplicitMathTransform() throws Exception {
        return MathTransformUtils.get3DMathTransform((SingleOperation) datumOperation);
    }

    @Override
    public void enable3DPointConversion(boolean enable) {
        if (!transform.getTransformOperation().supports3DPointConversion()) {
            return;
        }
        this.use3DPointConversion = enable;
    }

    @Override
    public boolean supports3DPointConversion() {
        return transform.getTransformOperation().supports3DPointConversion();
    }

}
