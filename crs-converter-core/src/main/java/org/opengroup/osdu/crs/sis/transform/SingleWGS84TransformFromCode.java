package org.opengroup.osdu.crs.sis.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.internal.system.DefaultFactories;
import org.apache.sis.metadata.iso.citation.Citations;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.IdentifiedObjects;
import org.apache.sis.referencing.crs.AbstractCRS;
import org.apache.sis.referencing.cs.AxesConvention;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.operation.transform.MathTransforms;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.SingleOperation;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengroup.osdu.crs.model.ISingleTrf;
import org.opengroup.osdu.crs.sis.AreaOfUseUtils;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.sis.MathTransformUtils;
import org.opengroup.osdu.crs.sis.SisTransformations;
import org.opengroup.osdu.crs.sis.operation.OperationResponse;

public class SingleWGS84TransformFromCode implements IWGS84Transform {

    private static final Logger LOGGER = Logger.getLogger(SingleWGS84TransformFromCode.class.getName());

    private boolean longLatOrder = true;
    private CoordinateOperation datumOperation = null;
    private CoordinateReferenceSystem crs;
    private MathTransform xyToWGS84Transform = null;
    private MathTransform wgs84ToXYTransform = null;

    private boolean negativeScale = false;
    private String xyCRSName;
    private ISingleTrf transform;
    private GeographicBoundingBox transformToWGS84BoundingBox;
    private GeographicBoundingBox transformFromWGS84BoundingBox;
    private boolean checkAreaOfUse;
    private boolean use3DPointConversion = false;
    private ISisCrs sisCrs;
    private ISisCrs iSisCrs;

    public SingleWGS84TransformFromCode(ISisCrs sisCrs, ISisCrs iSisCrs, ISingleTrf transform, boolean checkAreaOfUse) throws Exception {

        this.sisCrs = sisCrs;
        this.iSisCrs = iSisCrs;
        this.transform = transform;
        this.datumOperation = transform.getTransformOperation().getFromWGS84Operation();

        this.crs = sisCrs.getCoordinateReferenceSystem();
        this.negativeScale = sisCrs.isNegativeScale();

        this.xyCRSName = sisCrs.getName();

        /**
         * Normalize the X, Y axis directions order.
         */
        this.crs = AbstractCRS.castOrCopy(crs).forConvention(AxesConvention.DISPLAY_ORIENTED);
        /*
         * Check if the lat/long are specified in the proper order
         */
        CoordinateSystemAxis firstAxis = datumOperation.getSourceCRS().getCoordinateSystem().getAxis(0);

        this.longLatOrder = firstAxis.getDirection() != AxisDirection.NORTH;
        this.checkAreaOfUse = checkAreaOfUse;
    }

    @Override
    public double[] transformSingleXYPointToWGS84(double x, double y, double z) throws FactoryException, MismatchedDimensionException, TransformException {

        // Temporary workaround because library does not support negative scale factors
        if (negativeScale) {
            x = -x;
            y = -y;
        }

        if (xyToWGS84Transform == null) {
            initializeXYtoWGS84Transform();
        }

        double[] coordinate = MathTransformUtils.transformSinglePoint(xyToWGS84Transform, x, y, z);

        if (transformToWGS84BoundingBox != null) {
            if (!AreaOfUseUtils.isPointInAreaOfUse(coordinate[0], coordinate[1], transformToWGS84BoundingBox)) {
                throw new TransformException("Point" + coordinate[0] + "," + coordinate[1] + " is outside the area of use: " + transformToWGS84BoundingBox);
            }
        }
        double[] response = new double[3];
        response[0] = coordinate[0];
        response[1] = coordinate[1];
        response[2] = coordinate[2];

        return response;
    }

    @Override
    public double[] transformSingleWGS84PointToXY(double longitude, double latitude, double z) throws FactoryException, MismatchedDimensionException, TransformException {
        if (this.wgs84ToXYTransform == null) {
            initializeWGS84ToXYTransform();
        }

        double x = longitude;
        double y = latitude;

        if (transformFromWGS84BoundingBox != null) {
            if (!AreaOfUseUtils.isPointInAreaOfUse(longitude, latitude, transformFromWGS84BoundingBox)) {
                throw new TransformException("Point" + longitude + "," + latitude + " is outside the area of use: " + transformToWGS84BoundingBox);
            }
        }

        double[] coordinate = MathTransformUtils.transformSinglePoint(wgs84ToXYTransform, x, y, z);

        // Temporary workaround because library does not support negative scale factors
        if (negativeScale) {
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
    public OperationResponse transformWGS84PointsToXY(double[] xyValues, double[] zValues) {
        int numPoints = xyValues.length / 2;
        int successfulConversionCount = 0;
        try {
            if (this.wgs84ToXYTransform == null) {
                initializeWGS84ToXYTransform();
            }

            if (transformFromWGS84BoundingBox != null) {
                for (int i = 0; i < numPoints; i++) {
                    double currentX = xyValues[i * 2];
                    double currentY = xyValues[(i * 2) + 1];
                    if (!AreaOfUseUtils.isPointInAreaOfUse(currentX, currentY, transformFromWGS84BoundingBox)) {
                        xyValues[i * 2] = Double.NaN;
                        xyValues[(i * 2) + 1] = Double.NaN;
                        zValues[i] = Double.NaN;
                    }
                }
            }

            double[] xyValuesToConvert = new double[xyValues.length];
            System.arraycopy(xyValues, 0, xyValuesToConvert, 0, xyValues.length);

            double[] convertedXYValues = new double[xyValues.length];
            double[] convertedZValues = new double[zValues.length];
            try {
                //try to convert all points at once
                MathTransformUtils.transformMultiplePoints(wgs84ToXYTransform, xyValuesToConvert, zValues, convertedXYValues, convertedZValues);
            } catch (Exception ex) {
                //will need to convert each point individually
                return fallbackTransformWGS84PointsToXY(xyValues, zValues);
            }

            if (negativeScale) {
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
        operations.add(String.format("transformation %s to %s using %s; %d points successfully transformed", "GCS_WGS_1984", xyCRSName,
                transform.getName(), successfulConversionCount));
        return new OperationResponse(operations, successfulConversionCount);

    }

    private OperationResponse fallbackTransformWGS84PointsToXY(double[] xyValues, double[] zValues) {
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
                double[] point = transformSingleWGS84PointToXY(currentX, currentY, currentZ);
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
        operations.add(String.format("transformation %s to %s using %s; %d points successfully transformed", "GCS_WGS_1984", xyCRSName,
                transform.getName(), successfulConversionCount));
        return new OperationResponse(operations, successfulConversionCount);
    }

    @Override
    public OperationResponse transformXYPointsToWGS84(double[] xyValues, double[] zValues) {
        int numPoints = xyValues.length / 2;
        int successfulConversionCount = 0;
        try {
            if (this.xyToWGS84Transform == null) {
                initializeXYtoWGS84Transform();
            }

            double[] xyValuesToConvert = new double[xyValues.length];
            if (negativeScale) {
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
                MathTransformUtils.transformMultiplePoints(xyToWGS84Transform, xyValuesToConvert, zValues, convertedXYValues, convertedZValues);
            } catch (Exception ex) {
                //will need to convert each point individually
                return fallbackTransformXYPointsToWGS84(xyValues, zValues);
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
                } else if (transformToWGS84BoundingBox != null && !AreaOfUseUtils.isPointInAreaOfUse(currentX, currentY, transformToWGS84BoundingBox)) {
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
        operations.add(String.format("transformation %s to %s using %s; %d points successfully transformed", xyCRSName, "GCS_WGS_1984",
                transform.getName(), successfulConversionCount));
        return new OperationResponse(operations, successfulConversionCount);
    }

    private OperationResponse fallbackTransformXYPointsToWGS84(double[] xyValues, double[] zValues) {
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
                double[] point = transformSingleXYPointToWGS84(currentX, currentY, currentZ);
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
        operations.add(String.format("transformation %s to %s using %s; %d points successfully transformed", xyCRSName, "GCS_WGS_1984",
                transform.getName(), successfulConversionCount));
        return new OperationResponse(operations, successfulConversionCount);
    }

    //This method will create the transformation from XY to WGS84
    private void initializeXYtoWGS84Transform() throws MismatchedDimensionException, FactoryException, NoninvertibleTransformException {
//       The if part is added becasue in osdu BoundCRS convention assumes CT is "to WGS 84". The if condition is added to work when the CT is not "to WGS 84". Check if the sourceCRS is wgs84 or not. If the sourceCRS is Wgs84 we need to implement the below steps in reverse.
        if(isTransformfromWgs84(datumOperation)){
            CoordinateOperation xyToDatumOperation = CRS.findOperation(crs, datumOperation.getTargetCRS(), null);

            if (checkAreaOfUse) {
                transformToWGS84BoundingBox = AreaOfUseUtils.getTransformBoundingBox(datumOperation);
            }
            if (use3DPointConversion) {
                try {
                    xyToWGS84Transform = create3DPointXYToWGS84Transformation(xyToDatumOperation);
                    return;
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Can't create 3d transformation", ex);
                }
            }
            /*
             * We have two operations: from my source CRS to the CRS expected
             * by the operation as inputs, then the operation itself. We want
             * the concatenation of those two steps:
             */
            MathTransform step1 = datumOperation.getMathTransform().inverse();
            MathTransform step2 = xyToDatumOperation.getMathTransform();


            xyToWGS84Transform = MathTransforms.concatenate(step1, step2);
            if (!longLatOrder) {
                Matrix swapMatrix = Matrices.createTransform(
                        new AxisDirection[]{AxisDirection.NORTH, AxisDirection.EAST},
                        new AxisDirection[]{AxisDirection.EAST, AxisDirection.NORTH});
                xyToWGS84Transform = MathTransforms.concatenate(xyToWGS84Transform, MathTransforms.linear(swapMatrix));
            }
        }else{
        CoordinateOperation xyToDatumOperation = CRS.findOperation(crs, datumOperation.getSourceCRS(), null);
        if (checkAreaOfUse) {
            transformToWGS84BoundingBox = AreaOfUseUtils.getTransformBoundingBox(datumOperation);
        }
        if (use3DPointConversion) {
            try {
                xyToWGS84Transform = create3DPointXYToWGS84Transformation(xyToDatumOperation);
                return;
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Can't create 3d transformation", ex);
            }
        }
        /*
         * We have two operations: from my source CRS to the CRS expected
         * by the operation as inputs, then the operation itself. We want
         * the concatenation of those two steps:
         */
        MathTransform step1 = xyToDatumOperation.getMathTransform();
        MathTransform step2 = datumOperation.getMathTransform();
        xyToWGS84Transform = MathTransforms.concatenate(step1, step2);
        if (!longLatOrder) {
            Matrix swapMatrix = Matrices.createTransform(
                    new AxisDirection[]{AxisDirection.NORTH, AxisDirection.EAST},
                    new AxisDirection[]{AxisDirection.EAST, AxisDirection.NORTH});

            xyToWGS84Transform = MathTransforms.concatenate(xyToWGS84Transform, MathTransforms.linear(swapMatrix));
        }
        }
    }
    public boolean isTransformfromWgs84(CoordinateOperation transform) throws FactoryException {
        CoordinateReferenceSystem sourceCrs= transform.getSourceCRS();
        CoordinateReferenceSystem wgs84 = CRS.forCode("EPSG:4326");
        Identifier identifier = IdentifiedObjects.getIdentifier(sourceCrs, Citations.EPSG);
        Identifier otherIdentifier = IdentifiedObjects.getIdentifier(wgs84, Citations.EPSG);
        if (identifier != null && otherIdentifier != null) {
            return identifier.getCode().equals(otherIdentifier.getCode());
        }else{
            ISingleTrf singleExplicitTransform = this.transform;
            ISisMathTransform sisTransform = singleExplicitTransform.getTransformOperation();
            CoordinateOperation transformCoordinateOperation = sisTransform.getFromWGS84Operation();
            CoordinateReferenceSystem transformSourceCRS = transformCoordinateOperation.getSourceCRS();
            CoordinateReferenceSystem transformTargetCRS = transformCoordinateOperation.getTargetCRS();

            boolean do_reverse = SisTransformations.checkInverseTransformationFromScore(transformSourceCRS, transformTargetCRS, sisCrs, iSisCrs);
            return do_reverse;
        }
    }

    private void initializeWGS84ToXYTransform() throws MismatchedDimensionException, FactoryException, NoninvertibleTransformException {
        CoordinateOperation datumToXYOperation = CRS.findOperation(datumOperation.getSourceCRS(), crs, null);
        if (checkAreaOfUse) {
            transformFromWGS84BoundingBox = AreaOfUseUtils.getTransformBoundingBox(datumOperation);
        }
        if (use3DPointConversion) {
            try {
                wgs84ToXYTransform = create3DPointWGS84ToXYTransformation(datumToXYOperation);
                return;
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Can't create 3d transformation", ex);
            }
        }
        /*
         * We have two operations: from my source CRS to the CRS expected
         * by the operation as inputs, then the operation itself. We want
         * the concatenation of those two steps:
         */
        MathTransform step1 = datumOperation.getMathTransform().inverse();
        MathTransform step2 = datumToXYOperation.getMathTransform();

        wgs84ToXYTransform = MathTransforms.concatenate(step1, step2);
        if (!longLatOrder) {
            Matrix swapMatrix = Matrices.createTransform(
                    new AxisDirection[]{AxisDirection.NORTH, AxisDirection.EAST},
                    new AxisDirection[]{AxisDirection.EAST, AxisDirection.NORTH});

            wgs84ToXYTransform = MathTransforms.concatenate(MathTransforms.linear(swapMatrix), wgs84ToXYTransform);
        }
    }

    private MathTransform create3DPointXYToWGS84Transformation(CoordinateOperation xyToDatumOperation) throws Exception {
        return get3DConversion(datumOperation);
    }
    
    private MathTransform create3DPointWGS84ToXYTransformation(CoordinateOperation datumToXYOperation) throws Exception {
        return get3DConversion(datumOperation).inverse();
    }

    private MathTransform get3DConversion(CoordinateOperation operation) throws Exception {
        ParameterValueGroup srcp = ((SingleOperation) operation).getParameterValues();
        String oldName = ((SingleOperation) operation).getMethod().getName().getCode();
        String newName = oldName.replace("geog2D", "geog3D");

        MathTransformFactory mtFactory = DefaultFactories.forClass(MathTransformFactory.class);
        ParameterValueGroup tgtp = mtFactory.getDefaultParameters(newName);
        Ellipsoid sourceEllipsoid = ((GeographicCRS) operation.getSourceCRS()).getDatum().getEllipsoid();
        Ellipsoid targetEllipsoid = ((GeographicCRS) operation.getTargetCRS()).getDatum().getEllipsoid();
        tgtp.parameter("src_semi_major").setValue(sourceEllipsoid.getSemiMajorAxis(), sourceEllipsoid.getAxisUnit());
        tgtp.parameter("src_semi_minor").setValue(sourceEllipsoid.getSemiMinorAxis(), sourceEllipsoid.getAxisUnit());
        tgtp.parameter("tgt_semi_major").setValue(targetEllipsoid.getSemiMajorAxis(), targetEllipsoid.getAxisUnit());
        tgtp.parameter("tgt_semi_minor").setValue(targetEllipsoid.getSemiMinorAxis(), targetEllipsoid.getAxisUnit());
        try {
            tgtp.parameter("X-axis translation").setValue(srcp.parameter("X-axis translation").doubleValue());
        } catch (ParameterNotFoundException ex) {
            //ignore
        }
        try {
            tgtp.parameter("Y-axis translation").setValue(srcp.parameter("Y-axis translation").doubleValue());
        } catch (ParameterNotFoundException ex) {
            //ignore
        }
        try {
            tgtp.parameter("Z-axis translation").setValue(srcp.parameter("Z-axis translation").doubleValue());
        } catch (ParameterNotFoundException ex) {
            //ignore
        }
        try {
            tgtp.parameter("Scale difference").setValue(srcp.parameter("Scale difference").doubleValue());
        } catch(ParameterNotFoundException ex) {
            
        }
        try {
            tgtp.parameter("X-axis rotation").setValue(srcp.parameter("X-axis rotation").doubleValue());
        } catch(ParameterNotFoundException ex) {
            
        }
        try {
            tgtp.parameter("Y-axis rotation").setValue(srcp.parameter("Y-axis rotation").doubleValue());
        } catch(ParameterNotFoundException ex) {
            
        }
        try {
            tgtp.parameter("Z-axis rotation").setValue(srcp.parameter("Z-axis rotation").doubleValue());
        } catch(ParameterNotFoundException ex) {
            
        }
        return mtFactory.createParameterizedTransform(tgtp);
    }

    @Override
    public boolean supports3dPointConversion() {
        return transform.getTransformOperation().supports3DPointConversion();
    }

    @Override
    public void enable3DPointConversion(boolean enable) {
        if (!transform.getTransformOperation().supports3DPointConversion()) {
            return;
        }
        this.use3DPointConversion = enable;
    }

}
