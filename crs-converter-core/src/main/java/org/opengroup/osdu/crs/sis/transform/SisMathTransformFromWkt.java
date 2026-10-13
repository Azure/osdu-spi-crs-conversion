package org.opengroup.osdu.crs.sis.transform;

import org.opengis.referencing.operation.CoordinateOperation;

public class SisMathTransformFromWkt implements ISisMathTransform{

    private final CoordinateOperation transformOperation;
    private final boolean supports3DConversion;

    public SisMathTransformFromWkt(CoordinateOperation transformOperation, boolean supports3DConversion) {
        this.transformOperation = transformOperation;
        this.supports3DConversion = supports3DConversion;
    }

    @Override
    public CoordinateOperation getFromWGS84Operation() {
        return transformOperation;
    }

    @Override
    public CoordinateOperation getToWGS84Operation() {
        return transformOperation;
    }

    @Override
    public TransformDerivedType getDerivedType() {
        return TransformDerivedType.DERIVED_FROM_WKT;
    }

    @Override
    public boolean isEqual(ISisMathTransform otherOperation) {
        if (otherOperation.getDerivedType() != TransformDerivedType.DERIVED_FROM_WKT) {
            return false;
        }
        if (!this.transformOperation.equals(otherOperation.getFromWGS84Operation())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean supports3DPointConversion() {
        return supports3DConversion;
    }


}
