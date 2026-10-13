package org.opengroup.osdu.crs.sis.transform;

import java.util.ArrayList;
import java.util.List;
import org.apache.sis.geometry.DirectPosition2D;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengroup.osdu.crs.sis.AreaOfUseUtils;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.sis.operation.OperationResponse;

public class SingleWGS84TransformFromCrs implements IWGS84Transform {

    private boolean negativeScale = false;
    private final CoordinateOperation toWGS84Operation;
    private final CoordinateOperation fromWGS84Operation;
    private final String xyCRSName;
    private GeographicBoundingBox transformBoundingBox;

    public SingleWGS84TransformFromCrs(ISisCrs sisCrs, ISisMathTransform operation) throws Exception {
        this.negativeScale = sisCrs.isNegativeScale();

        /**
         * Normalize the X, Y axis directions order.
         */
        xyCRSName = sisCrs.getName();
        toWGS84Operation = operation.getToWGS84Operation();
        fromWGS84Operation = operation.getFromWGS84Operation();
    }

    @Override
    public double[] transformSingleXYPointToWGS84(double x, double y, double z) throws FactoryException, MismatchedDimensionException, TransformException {

        // Temporary workaround because library does not support negative scale factors
        if (negativeScale) {
            x = -x;
            y = -y;
        }
        DirectPosition source = new DirectPosition2D(x, y);
        DirectPosition target = toWGS84Operation.getMathTransform().transform(source, null);
        double[] coordinate = target.getCoordinate();

        if (transformBoundingBox != null) {
            if (!AreaOfUseUtils.isPointInAreaOfUse(coordinate[0], coordinate[1], transformBoundingBox)) {
                throw new TransformException("Point" + coordinate[0] + "," + coordinate[1] + " is outside the area of use: " + transformBoundingBox);
            }
        }

        double[] response = new double[3];
        response[0] = coordinate[0];
        response[1] = coordinate[1];
        response[2] = z;

        return response;
    }

    @Override
    public double[] transformSingleWGS84PointToXY(double longitude, double latitude, double z) throws FactoryException, MismatchedDimensionException, TransformException {

        if (transformBoundingBox != null) {
            if (!AreaOfUseUtils.isPointInAreaOfUse(longitude, latitude, transformBoundingBox)) {
                throw new TransformException("Point" + longitude + "," + latitude + " is outside the area of use: " + transformBoundingBox);
            }
        }

        DirectPosition source = new DirectPosition2D(longitude, latitude);
        DirectPosition target = fromWGS84Operation.getMathTransform().transform(source, null);
        double[] coordinate = target.getCoordinate();

        // Temporary workaround because library does not support negative scale factors
        if (negativeScale) {
            coordinate[0] = -coordinate[0];
            coordinate[1] = -coordinate[1];

        }
        double[] response = new double[3];
        response[0] = coordinate[0];
        response[1] = coordinate[1];
        response[2] = z;

        return response;
    }

    @Override
    public OperationResponse transformWGS84PointsToXY(double[] xyValues, double[] zValues) {
        int numPoints = xyValues.length / 2;
        int successfulConversionCount = 0;
        if (transformBoundingBox != null) {
            for (int i = 0; i < numPoints; i++) {
                double currentX = xyValues[i * 2];
                double currentY = xyValues[(i * 2) + 1];
                if (!AreaOfUseUtils.isPointInAreaOfUse(currentX, currentY, transformBoundingBox)) {
                    xyValues[i * 2] = Double.NaN;
                    xyValues[(i * 2) + 1] = Double.NaN;
                    zValues[i] = Double.NaN;
                }
            }
        }
        double[] pointsToConvert = new double[xyValues.length];
        System.arraycopy(xyValues, 0, pointsToConvert, 0, xyValues.length);

        double[] convertedPoints = new double[xyValues.length];
        try {
            fromWGS84Operation.getMathTransform().transform(pointsToConvert, 0, convertedPoints, 0, numPoints);
        } catch (Exception ex) {
            return fallbackTransformWGS84PointsToXY(xyValues, zValues);
        }

        if (negativeScale) {
            for (int i = 0; i < convertedPoints.length; i++) {
                convertedPoints[i] = -convertedPoints[i];
            }
        }
        System.arraycopy(convertedPoints, 0, xyValues, 0, convertedPoints.length);
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

        List<String> operations = new ArrayList<>();
        operations.add(String.format("transformation %s to %s; %d points successfully transformed", "GCS_WGS_1984", xyCRSName, successfulConversionCount));
        return new OperationResponse(operations, successfulConversionCount);
    }

    private OperationResponse fallbackTransformWGS84PointsToXY(double[] xyValues, double[] zValues) {
        int successfulConversionCount = 0;
        for (int i = 0; i < zValues.length; i++) {
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
        operations.add(String.format("transformation %s to %s; %d points successfully transformed", "GCS_WGS_1984", xyCRSName, successfulConversionCount));
        return new OperationResponse(operations, successfulConversionCount);
    }

    @Override
    public OperationResponse transformXYPointsToWGS84(double[] xyValues, double[] zValues) {
        int numPoints = xyValues.length / 2;
        int successfulConversionCount = 0;
        double[] pointsToConvert = new double[xyValues.length];
        System.arraycopy(xyValues, 0, pointsToConvert, 0, xyValues.length);
        if (negativeScale) {
            for (int i = 0; i < pointsToConvert.length; i++) {
                pointsToConvert[i] = -pointsToConvert[i];
            }
        }
        double[] convertedPoints = new double[xyValues.length];
        try {
            toWGS84Operation.getMathTransform().transform(pointsToConvert, 0, convertedPoints, 0, numPoints);
        } catch (Exception ex) {
            return fallbackTransformXYPointsToWGS84(xyValues, zValues);
        }
        System.arraycopy(convertedPoints, 0, xyValues, 0, convertedPoints.length);
        for (int i = 0; i < numPoints; i++) {
            double currentX = xyValues[i * 2];
            double currentY = xyValues[(i * 2) + 1];
            if (Double.isNaN(currentX) || Double.isNaN(currentY)) {
                xyValues[i * 2] = Double.NaN;
                xyValues[(i * 2) + 1] = Double.NaN;
                zValues[i] = Double.NaN;
            } else if (transformBoundingBox != null && !AreaOfUseUtils.isPointInAreaOfUse(currentX, currentY, transformBoundingBox)) {
                xyValues[i * 2] = Double.NaN;
                xyValues[(i * 2) + 1] = Double.NaN;
                zValues[i] = Double.NaN;
            } else {
                successfulConversionCount++;
            }
        }

        List<String> operations = new ArrayList<>();
        operations.add(String.format("transformation %s to %s; %d points successfully transformed", xyCRSName, "GCS_WGS_1984", successfulConversionCount));
        return new OperationResponse(operations, successfulConversionCount);
    }

    private OperationResponse fallbackTransformXYPointsToWGS84(double[] xyValues, double[] zValues) {
        int successfulConversionCount = 0;
        for (int i = 0; i < zValues.length; i++) {
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
        operations.add(String.format("transformation %s to %s; %d points successfully transformed", xyCRSName, "GCS_WGS_1984", successfulConversionCount));
        return new OperationResponse(operations, successfulConversionCount);
    }

    @Override
    public boolean supports3dPointConversion() {
        return false;
    }

    @Override
    public void enable3DPointConversion(boolean enable) {
    }
    
    
}
