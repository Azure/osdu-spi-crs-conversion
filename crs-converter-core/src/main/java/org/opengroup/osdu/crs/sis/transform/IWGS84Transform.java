package org.opengroup.osdu.crs.sis.transform;

import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengroup.osdu.crs.sis.operation.OperationResponse;

public interface IWGS84Transform {

    public double[] transformSingleXYPointToWGS84(double x, double y, double z) throws FactoryException, MismatchedDimensionException, TransformException;

    public double[] transformSingleWGS84PointToXY(double longitude, double latitude, double z) throws FactoryException, MismatchedDimensionException, TransformException;
    
    public OperationResponse transformXYPointsToWGS84(double[] xyValues, double[] zValues);

    public OperationResponse transformWGS84PointsToXY(double[] xyValues, double[] zValues);
    
    public boolean supports3dPointConversion();
    
    public void enable3DPointConversion(boolean enable);
    
}
