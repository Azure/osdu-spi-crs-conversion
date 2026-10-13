package org.opengroup.osdu.crs.sis;

import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.apache.sis.metadata.iso.extent.Extents;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.operation.CoordinateOperation;

public class AreaOfUseUtils {

    private final static long DECIMAL_PRECISION = 5;

    public static GeographicBoundingBox getTransformBoundingBox(CoordinateOperation operation) {
        Extent domainOfValidity = operation.getDomainOfValidity();
        if (domainOfValidity == null) {
            return null;
        }
        return Extents.getGeographicBoundingBox(domainOfValidity);
    }

    public static boolean isPointInAreaOfUse(double longitude, double latitude, GeographicBoundingBox areaOfUse) {
        if (Math.abs(longitude) > 180 || Math.abs(latitude) > 90) {
            return false;
        }
        double precision = Math.pow(10, DECIMAL_PRECISION);
        double roundedLongitude = Math.round(longitude * precision) / precision;
        double roundedLatitude = Math.round(latitude * precision) / precision;
        //check boundary

        DefaultGeographicBoundingBox pointAreayOfUse = new DefaultGeographicBoundingBox(roundedLongitude, roundedLongitude, roundedLatitude, roundedLatitude);
        GeographicBoundingBox intersection = Extents.intersection(pointAreayOfUse, areaOfUse);
        if (!pointAreayOfUse.equals(intersection)) {
            return false;
        }
        return pointAreayOfUse.equals(intersection);
    }
}
