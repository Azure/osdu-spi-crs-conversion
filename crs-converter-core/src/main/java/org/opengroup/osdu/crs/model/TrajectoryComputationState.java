package org.opengroup.osdu.crs.model;

import lombok.Data;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.crs.sis.ISisCrs;

import java.util.ArrayList;
import java.util.List;

@Data
public class TrajectoryComputationState {
    public TrajectoryComputationState() {
        operations = new ArrayList<>();
        errors = new ArrayList<>();
        interpolate = true;
    }
    private ICrs sourceCRS;
    private String sourceCRSAsPersistableReference;
    private IUnit horizontalUnit;
    private IUnit horizontalCRSUnit;
    private IUnit verticalUnit;
    private AzimuthReferenceType azimuthReference;
    private TrajectoryComputationMethod method;
    private TrajectoryInputKind inputKind;
    private Point referencePoint;
    private boolean interpolate;
    private List<String> errors;
    private List<String> operations;
    private ISisCrs geogCS;
    private ISisCrs proCS;
    private DpsHeaders dpsHeaders;    
}
