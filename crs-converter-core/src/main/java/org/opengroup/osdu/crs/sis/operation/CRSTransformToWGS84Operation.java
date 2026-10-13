package org.opengroup.osdu.crs.sis.operation;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.opengroup.osdu.crs.sis.transform.IWGS84Transform;

public class CRSTransformToWGS84Operation implements ICRSCoordinateOperation {

    private static final Logger LOGGER = Logger.getLogger(CRSTransformToWGS84Operation.class.getName());
    private final IWGS84Transform operation;

    public CRSTransformToWGS84Operation(IWGS84Transform operation) {
        this.operation = operation;
    }

    @Override
    public double[] convertSinglePoint(double x, double y, double z) {
        try {
            double[] point = operation.transformSingleXYPointToWGS84(x, y, z);
            return point;
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Can't convert point", ex);
            return new double[]{Double.NaN, Double.NaN};
        }
    }

    @Override
    public OperationResponse convertPoints(double[] xyValues, double[] zValues) {
        return operation.transformXYPointsToWGS84(xyValues, zValues);
    }
    
    @Override
    public boolean supports3DPointConversion() {
        return operation.supports3dPointConversion();
    }

    @Override
    public void enable3DPointConversion(boolean enable) {
        operation.enable3DPointConversion(enable);
    }
}