package org.opengroup.osdu.crs.sis.transform;

import java.util.ArrayList;
import java.util.List;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengroup.osdu.crs.model.ICompoundTrf;
import org.opengroup.osdu.crs.model.ISingleTrf;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.sis.operation.OperationResponse;

public class CompoundFallbackWGS84TransformWithCode implements IWGS84Transform {

    private final List<IWGS84Transform> transforms;
    private final List<ISingleTrf> transformDescriptions;
    private final String xyCRSName;

    public CompoundFallbackWGS84TransformWithCode(ISisCrs sisCrs, ICompoundTrf compoundTransform) throws Exception {
        if (!compoundTransform.isValid()) {
            throw new IllegalArgumentException("Invalid transform " + compoundTransform.getName());
        }
        this.transformDescriptions = compoundTransform.getTransformations();
        this.transforms = new ArrayList<>();
        for (int i = 0; i < transformDescriptions.size(); i++) {
            ISingleTrf currentDescription = transformDescriptions.get(i);
            this.transforms.add(new SingleWGS84TransformFromCode(sisCrs, null, currentDescription, true));
        }

        xyCRSName = sisCrs.getName();
    }

    @Override
    public double[] transformSingleXYPointToWGS84(double x, double y, double z) throws FactoryException, MismatchedDimensionException, TransformException {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return new double[]{x, y, z};
        }
        for (IWGS84Transform currentTransform : transforms) {
            try {
                double[] point = currentTransform.transformSingleXYPointToWGS84(x, y, z);
                if (Double.isNaN(point[0]) || Double.isNaN(point[1])) {
                    continue;
                }
                return point;
            } catch (Exception ex) {
            }
        }
        return new double[]{Double.NaN, Double.NaN, Double.NaN};
    }

    @Override
    public double[] transformSingleWGS84PointToXY(double longitude, double latitude, double z) throws FactoryException, MismatchedDimensionException, TransformException {
        if (Double.isNaN(longitude) || Double.isNaN(latitude)) {
            return new double[]{longitude, latitude, z};
        }
        for (IWGS84Transform currentTransform : transforms) {
            try {
                double[] point = currentTransform.transformSingleXYPointToWGS84(longitude, latitude, z);
                if (Double.isNaN(point[0]) || Double.isNaN(point[1])) {
                    continue;
                }
                return point;
            } catch (Exception ex) {
            }
        }
        return new double[]{Double.NaN, Double.NaN, Double.NaN};
    }

    @Override
    public OperationResponse transformWGS84PointsToXY(double[] xyValues, double[] zValues) {
        int numPoints = xyValues.length / 2;
        double[] convertedXYValues = new double[xyValues.length];
        double[] convertedZValues = new double[zValues.length];
        int[] successfulConversionCount = new int[transforms.size()];
        boolean[] transformAttempted = new boolean[transforms.size()];
        for (int i = 0; i < numPoints; i++) {
            double currentX = xyValues[i * 2];
            double currentY = xyValues[(i * 2) + 1];
            double currentZ = zValues[i];
            if (Double.isNaN(currentX) || Double.isNaN(currentY)) {
                convertedXYValues[i * 2] = Double.NaN;
                convertedXYValues[(i * 2) + 1] = Double.NaN;
                convertedZValues[i] = Double.NaN;
                continue;
            }
            for (int transformIndex = 0; transformIndex < transforms.size(); transformIndex++) {
                try {
                    transformAttempted[transformIndex] = true;
                    double[] point = transforms.get(transformIndex).transformSingleWGS84PointToXY(currentX, currentY, currentZ);
                    convertedXYValues[i * 2] = point[0];
                    convertedXYValues[(i * 2) + 1] = point[1];
                    convertedZValues[i] = point[2];
                    successfulConversionCount[transformIndex]++;
                    break;
                } catch (Exception ex) {
                    convertedXYValues[i * 2] = Double.NaN;
                    convertedXYValues[(i * 2) + 1] = Double.NaN;
                    convertedZValues[i] = Double.NaN;
                }
            }
        }
        List<String> operations = new ArrayList<>();
        int totalSuccessCount = 0;
        for (int i = 0; i < transforms.size(); i++) {
            if (transformAttempted[i]) {
                int currentTransformSuccessCount = successfulConversionCount[i];
                totalSuccessCount = totalSuccessCount + currentTransformSuccessCount;
                ISingleTrf currentDescription = transformDescriptions.get(i);
                operations.add(String.format("transformation %s to %s using %s; %d points successfully transformed", "GCS_WGS_1984", xyCRSName,
                        currentDescription.getName(), currentTransformSuccessCount));
            }
        }

        for (int i = 0; i < xyValues.length; i++) {
            xyValues[i] = convertedXYValues[i];
        }
        for (int i = 0; i < zValues.length; i++) {
            zValues[i] = convertedZValues[i];
        }
        return new OperationResponse(operations, totalSuccessCount);
    }

    @Override
    public OperationResponse transformXYPointsToWGS84(double[] xyValues, double[] zValues) {
        int numPoints = xyValues.length / 2;
        double[] convertedXYValues = new double[xyValues.length];
        double[] convertedZValues = new double[zValues.length];
        int[] successfulConversionCount = new int[transforms.size()];
        boolean[] transformAttempted = new boolean[transforms.size()];
        for (int i = 0; i < numPoints; i++) {
            double currentX = xyValues[i * 2];
            double currentY = xyValues[(i * 2) + 1];
            double currentZ = zValues[i];
            if (Double.isNaN(currentX) || Double.isNaN(currentY)) {
                convertedXYValues[i * 2] = Double.NaN;
                convertedXYValues[(i * 2) + 1] = Double.NaN;
                convertedZValues[i] = Double.NaN;
                continue;
            }
            for (int transformIndex = 0; transformIndex < transforms.size(); transformIndex++) {
                try {
                    transformAttempted[transformIndex] = true;
                    double[] point = transforms.get(transformIndex).transformSingleXYPointToWGS84(currentX, currentY, currentZ);
                    convertedXYValues[i * 2] = point[0];
                    convertedXYValues[(i * 2) + 1] = point[1];
                    convertedZValues[i] = zValues[i];
                    successfulConversionCount[transformIndex]++;
                    break;
                } catch (Exception ex) {
                    convertedXYValues[i * 2] = Double.NaN;
                    convertedXYValues[(i * 2) + 1] = Double.NaN;
                    convertedZValues[i] = Double.NaN;
                }
            }
        }
        List<String> operations = new ArrayList<>();
        int totalSuccessCount = 0;
        for (int i = 0; i < transforms.size(); i++) {
            if (transformAttempted[i]) {
                int currentTransformSuccessCount = successfulConversionCount[i];
                totalSuccessCount = totalSuccessCount + currentTransformSuccessCount;
                ISingleTrf currentDescription = transformDescriptions.get(i);
                operations.add(String.format("transformation %s to %s using %s; %d points successfully transformed", xyCRSName, "GCS_WGS_1984",
                        currentDescription.getName(), currentTransformSuccessCount));
            }
        }

        for (int i = 0; i < xyValues.length; i++) {
            xyValues[i] = convertedXYValues[i];
        }
        for (int i = 0; i < zValues.length; i++) {
            zValues[i] = convertedZValues[i];
        }
        return new OperationResponse(operations, totalSuccessCount);
    }

    @Override
    public boolean supports3dPointConversion() {
        //for now don't support 3d point conversion for compund transforms
        return false;
    }

    @Override
    public void enable3DPointConversion(boolean enable) {
        //for now don't support 3d point conversion for compund transforms
    }

}
