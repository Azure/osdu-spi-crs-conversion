package org.opengroup.osdu.crs.sis.transform;

import org.opengis.referencing.operation.CoordinateOperation;

public class SisMathTransformFromCrs implements ISisMathTransform {

    private final CoordinateOperation fromWGS84Operation;
    private final CoordinateOperation toWGS84Operation;

    public SisMathTransformFromCrs(CoordinateOperation toWGS84Operation, CoordinateOperation fromWGS84Operation) {
        this.toWGS84Operation = toWGS84Operation;
        this.fromWGS84Operation = fromWGS84Operation;
    }

    @Override
    public CoordinateOperation getFromWGS84Operation() {
        return fromWGS84Operation;
    }

    @Override
    public CoordinateOperation getToWGS84Operation() {
        return toWGS84Operation;
    }

    @Override
    public ISisMathTransform.TransformDerivedType getDerivedType() {
        return ISisMathTransform.TransformDerivedType.DERIVED_FROM_CRS;
    }

    @Override
    public boolean isEqual(ISisMathTransform otherOperation) {
        if (otherOperation.getDerivedType() != ISisMathTransform.TransformDerivedType.DERIVED_FROM_CRS) {
            return false;
        }
        if (!this.getToWGS84Operation().equals(otherOperation.getToWGS84Operation())) {
            return false;
        }
        if (!this.getFromWGS84Operation().equals(otherOperation.getFromWGS84Operation())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean supports3DPointConversion() {
        return false;
    }
}
