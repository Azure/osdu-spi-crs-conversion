package org.opengroup.osdu.crs.sis;

import org.apache.sis.internal.system.DefaultFactories;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.SingleOperation;
import org.opengis.referencing.operation.TransformException;

public class MathTransformUtils {

    public static double[] transformSinglePoint(MathTransform transform, double x, double y, double z)
            throws TransformException {
        if (transform.getSourceDimensions() == 3 && transform.getTargetDimensions() == 3) {
            return transformSingle3DPoint(transform, x, y, z);
        } else {
            return transformSingle2DPoint(transform, x, y, z);
        }
    }

    public static void transformMultiplePoints(MathTransform transform, double[] xyValues, double[] zValues,
            double[] convertedXYValues, double[] convertedZValues) throws TransformException {
        if (transform.getSourceDimensions() == 3 && transform.getTargetDimensions() == 3) {
            transform3DPoints(transform, xyValues, zValues, convertedXYValues, convertedZValues);
        } else {
            transform2DPoints(transform, xyValues, zValues, convertedXYValues, convertedZValues);
        }
    }

    private static void transform3DPoints(MathTransform transform, double[] xyValues, double[] zValues,
            double[] convertedXYValues, double[] convertedZValues) throws TransformException {
        int numberOfPoints = zValues.length;
        double[] pointsToConvert = new double[numberOfPoints * 3];
        double[] convertedPoints = new double[numberOfPoints * 3];
        for (int i = 0; i < numberOfPoints; i++) {
            pointsToConvert[i * 3] = xyValues[i * 2];
            pointsToConvert[(i * 3) + 1] = xyValues[(i * 2) + 1];
            pointsToConvert[(i * 3) + 2] = zValues[i];
        }
        transform.transform(pointsToConvert, 0, convertedPoints, 0, numberOfPoints);
        for (int i = 0; i < numberOfPoints; i++) {
            convertedXYValues[i * 2] = convertedPoints[i * 3];
            convertedXYValues[(i * 2) + 1] = convertedPoints[(i * 3) + 1];
            convertedZValues[i] = convertedPoints[(i * 3) + 2];
        }
    }

    private static void transform2DPoints(MathTransform transform, double[] xyValues, double[] zValues,
            double[] convertedXYValues, double[] convertedZValues) throws TransformException {
        int numberOfPoints = zValues.length;
        transform.transform(xyValues, 0, convertedXYValues, 0, numberOfPoints);
        System.arraycopy(zValues, 0, convertedZValues, 0, convertedZValues.length);
    }

    private static double[] transformSingle2DPoint(MathTransform transform, double x, double y, double z) throws TransformException {
        double[] pointToConvert = new double[]{x, y};
        double[] convertedPoint = new double[2];
        transform.transform(pointToConvert, 0, convertedPoint, 0, 1);
        return new double[]{convertedPoint[0], convertedPoint[1], z};        
    }

    private static double[] transformSingle3DPoint(MathTransform transform, double x, double y, double z) throws TransformException {
        double[] pointToConvert = new double[]{x, y, z};
        double[] convertedPoint = new double[3];
        transform.transform(pointToConvert, 0, convertedPoint, 0, 1);
        return convertedPoint;
    }

    //imported for explict transform
    public static MathTransform get3DMathTransform(SingleOperation operation) throws Exception {
        ParameterValueGroup srcp = operation.getParameterValues();
        String oldName = operation.getMethod().getName().getCode();
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
        } catch (ParameterNotFoundException ex) {

        }
        try {
            tgtp.parameter("X-axis rotation").setValue(srcp.parameter("X-axis rotation").doubleValue());
        } catch (ParameterNotFoundException ex) {

        }
        try {
            tgtp.parameter("Y-axis rotation").setValue(srcp.parameter("Y-axis rotation").doubleValue());
        } catch (ParameterNotFoundException ex) {

        }
        try {
            tgtp.parameter("Z-axis rotation").setValue(srcp.parameter("Z-axis rotation").doubleValue());
        } catch (ParameterNotFoundException ex) {

        }
        return mtFactory.createParameterizedTransform(tgtp);
    }


}