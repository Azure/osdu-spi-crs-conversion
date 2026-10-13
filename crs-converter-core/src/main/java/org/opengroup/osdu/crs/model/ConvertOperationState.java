package org.opengroup.osdu.crs.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConvertOperationState {
    public ConvertOperationState(ICrs sourceCrs, ICrs targetCrs, double[] xyCoordinates, double[] zCoordinates) {
        operations = new ArrayList<>();
        errors = new ArrayList<>();
        status = 0;
        this.sourceCrs = sourceCrs;
        this.targetCrs = targetCrs;
        this.xyCoordinates = xyCoordinates;
        this.zCoordinates = zCoordinates;
    }
    private List<String> errors;
    private List<String> operations;
    private int status;
    private double[] xyCoordinates;
    private double[] zCoordinates;
    private ICrs sourceCrs;
    private ICrs targetCrs;

    public int getPointCount(){
        return this.zCoordinates.length;
    }

    public ITrf getSourceTrf() {
        if (this.sourceCrs instanceof IEarlyBoundCrs) return ((IEarlyBoundCrs)this.sourceCrs).getTrf();
        return null;
    }
    public ITrf getTargetTrf() {
        if (this.targetCrs instanceof IEarlyBoundCrs) return ((IEarlyBoundCrs)this.targetCrs).getTrf();
        return null;
    }

    public boolean isValid(){
        return this.xyCoordinates != null && this.zCoordinates != null &&
                this.xyCoordinates.length > 0 && this.zCoordinates.length > 0 &&
                this.xyCoordinates.length == 2 * this.zCoordinates.length &&
                this.sourceCrs != null && this.targetCrs != null;
    }
}
