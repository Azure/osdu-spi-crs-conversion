package org.opengroup.osdu.crs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.crs.sis.ISisCrs;

/*
 * Container class for Azimuthal-Equidistant Projection CRS
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectionCorrectionSet {
    private double convergenceAngleInDeg;
    private double projectionScaleFactor;
    private ISisCrs peAzimuthalEquidistantCRS;
    private ICrs azimuthalEquidistantCRS;

    public String azimuthalEquidistantPersistableReference() {
        if (azimuthalEquidistantCRS.getType() == CRSType.EARLY_BOUND)
            return ((IEarlyBoundCrs) azimuthalEquidistantCRS).getLateBoundCrs().createPersistableReference();
        else if (azimuthalEquidistantCRS.getType() == CRSType.LATE_BOUND)
            return azimuthalEquidistantCRS.createPersistableReference();
        else
            return null;
    }

    public boolean isValid() {
        return peAzimuthalEquidistantCRS != null && azimuthalEquidistantCRS.isValid() &&
                (azimuthalEquidistantCRS.getType() == CRSType.LATE_BOUND ||
                        azimuthalEquidistantCRS.getType() == CRSType.EARLY_BOUND);
    }
}
