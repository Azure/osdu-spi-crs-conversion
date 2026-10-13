package org.opengroup.osdu.crs.sis.operation;

public interface ICRSCoordinateOperation {

    public double[] convertSinglePoint(double x, double y, double z) throws Exception;
    
    public OperationResponse convertPoints(double[] xyValues, double[] zValues);
    
    public boolean supports3DPointConversion();
    
    public void enable3DPointConversion(boolean enable);
}
