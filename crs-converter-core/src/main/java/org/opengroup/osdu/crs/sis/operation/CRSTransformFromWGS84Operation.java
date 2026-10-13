package org.opengroup.osdu.crs.sis.operation;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.opengroup.osdu.crs.sis.transform.IWGS84Transform;

public class CRSTransformFromWGS84Operation implements ICRSCoordinateOperation {

    private static final Logger LOGGER = Logger.getLogger(CRSTransformFromWGS84Operation.class.getName());
    private final IWGS84Transform operation;

    public CRSTransformFromWGS84Operation(IWGS84Transform operation) {
        this.operation = operation;
    }

    @Override
    public double[] convertSinglePoint(double x, double y, double z) throws Exception {
        try {
            return operation.transformSingleWGS84PointToXY(x, y, z);
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, "Can't convert point", ex);
            return null;
        }
    }

    @Override
    public OperationResponse convertPoints(double[] xyValues, double[] zValues) {
        return operation.transformWGS84PointsToXY(xyValues, zValues);
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