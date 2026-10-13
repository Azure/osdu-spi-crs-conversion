package org.opengroup.osdu.crs.sis.transform;

import org.opengis.referencing.operation.CoordinateOperation;

public interface ISisMathTransform {

    public enum TransformDerivedType {
        DERIVED_FROM_CODE,
        DERIVED_FROM_WKT,
        DERIVED_FROM_CRS
    }

    public CoordinateOperation getToWGS84Operation();

    public CoordinateOperation getFromWGS84Operation();

    public TransformDerivedType getDerivedType();

    public boolean isEqual(ISisMathTransform otherOperation);
    
    public boolean supports3DPointConversion();
}
