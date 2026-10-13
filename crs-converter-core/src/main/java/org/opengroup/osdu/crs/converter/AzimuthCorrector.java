package org.opengroup.osdu.crs.converter;

import org.opengroup.osdu.crs.model.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import org.opengroup.osdu.crs.interfaces.IAzimuthCorrector;
import org.opengroup.osdu.crs.model.Impl.AuthorityCode;
import org.opengroup.osdu.crs.model.Impl.EarlyBoundCrs;
import org.opengroup.osdu.crs.model.Impl.LateBoundCrs;

import static org.opengroup.osdu.crs.model.ReferenceConverter.parseSpatialReference;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.sis.SisCrsFactory;
import org.opengroup.osdu.crs.sis.SisParameterDefs;
import org.opengroup.osdu.crs.sis.SisTransformations;
import org.opengroup.osdu.crs.sis.wkt.WktFactory;
import org.opengroup.osdu.crs.sis.wkt.WktParameters;
import org.opengroup.osdu.crs.sis.wkt.WktParametersFactory;

@Data
public class AzimuthCorrector implements IAzimuthCorrector {
    // local properties
    private ICrs sourceCRS;
    private String sourceCRSAsPersistableReference;
    private AzimuthReferenceType azimuthReference;
    private ISisCrs projCS;
    private ISisCrs geogCS;
    public List<String> errors;
    public List<String> messages;

    AzimuthCorrector() {
        messages = new ArrayList<>();
        errors = new ArrayList<>();
        clear();
    }

    private void clear() {
        sourceCRS = null;
        sourceCRSAsPersistableReference = null;
        azimuthReference = null;
        messages.clear();
        errors.clear();
    }

    private boolean validateArguments(String crs, String azimuthReferenceString, double[] xyCoordinates, double[] azimuths) {
        clear();
        if (crs == null) {
            errors.add("Invalid coordinate reference system specification");
        } else {
            validateCRS(crs, true);
            if (xyCoordinates != null && azimuths != null) {
                if (xyCoordinates.length / 2 != azimuths.length) {
                    errors.add("Inconsistent length of coordinates and azimuths arrays");
                }
            } else {
                if (xyCoordinates == null) {
                    errors.add("No coordinates provided.");
                }
                if (azimuths == null) {
                    errors.add("No azimuths provided.");
                }
            }
            azimuthReference = AzimuthReferenceType.getAzimuthReference(azimuthReferenceString);
            if (azimuthReference == null) {
                errors.add("Invalid azimuth reference.");
            }
        }
        return errors.size() == 0;
    }

    private void validateCRS(String crs, boolean mustBeProjected) {
        try {
            sourceCRS = null;
            IItem raw = parseSpatialReference(crs);
            if (raw instanceof ICrs) sourceCRS = (ICrs)raw;
            if (sourceCRS == null || !sourceCRS.isValid()) {
                errors.add("Failed to resolve coordinate reference systems.");
                return;
            } else if (sourceCRS.getType() == CRSType.LATE_BOUND) {
                projCS = ((ILateBoundCrs) sourceCRS).getProjectedCrs();
                geogCS = ((ILateBoundCrs) sourceCRS).getBaseGeographicCrs();
            } else if (sourceCRS.getType() == CRSType.EARLY_BOUND) {
                ILateBoundCrs lbCrs = ((IEarlyBoundCrs) sourceCRS).getLateBoundCrs();
                projCS = lbCrs.getProjectedCrs();
                geogCS = lbCrs.getBaseGeographicCrs();
            }
            ISisCrs test;
            if (mustBeProjected) test = projCS; // we must have a projected CRS
            else test = geogCS; //                 at least a geographic CRS is required
            if (test == null && sourceCRS.isValid()) { // we must have a non-null CRS
                errors.add("Invalid coordinate reference system specification.");
            } else {
                sourceCRSAsPersistableReference = crs;
            }
        } catch (IllegalArgumentException e) {
            sourceCRS = null;
            errors.add("Invalid coordinate reference system specification.");
        }
    }

    private boolean validateArguments(String crs, Point referencePoint, IUnit horizontalUnit) {
        clear();
        validateCRS(crs, false);
        if (referencePoint == null)
            errors.add("Invalid reference point.");
        else if (!Point.isValid(referencePoint))
            errors.add("Invalid reference point.");
        if (horizontalUnit == null || !horizontalUnit.isValid() || !horizontalUnit.isLength())
            errors.add("Invalid horizontal unit.");
        return errors.size() == 0;
    }

    @Override
    public int correctAzimuth(String crs, String azimuthReferenceString, double[] xyCoordinates, double[] azimuths) {
        int done = 0;
        if (validateArguments(crs, azimuthReferenceString, xyCoordinates, azimuths)) {
            try {
                for (int i = 0; i < azimuths.length; i++) {
                    double[] pt = new double[]{xyCoordinates[i * 2], xyCoordinates[i * 2 + 1]};
                    azimuths[i] = this.computeSingleCorrection(pt, azimuths[i], azimuthReference);
                }
                done = azimuths.length;
            } catch (Exception pe) { // unexpected
                errors.add(pe.getMessage());
            }
        }
        return done;
    }

    public ProjectionCorrectionSet createProjectionCorrectionSet(String crs, Point referencePoint, IUnit horizontalUnit) {
        ProjectionCorrectionSet result = new ProjectionCorrectionSet();
        if (validateArguments(crs, referencePoint, horizontalUnit)) {
            double convergenceAngle = 0.0;
            double scaleFactor = 1.0;
            double[] refXy = new double[]{referencePoint.getX(), referencePoint.getY()};
            try {
                ISisCrs azimuthalEquidistant;
                if (projCS != null) {
                    azimuthalEquidistant = getAzimuthalEquidistant(projCS, refXy);
                    double[] convergenceScale = computeConvergenceAngleAndScale(convergenceAngle, AzimuthReferenceType.GRID_NORTH, azimuthalEquidistant, projCS);
                    convergenceAngle = convergenceScale[0];
                    scaleFactor = convergenceScale[1];
                    ICrs localAziEqu = localCRS(sourceCRS, azimuthalEquidistant);
                    result = new ProjectionCorrectionSet(convergenceAngle, scaleFactor, azimuthalEquidistant, localAziEqu);
                } else { // if (geogCS != null) already validated
                    azimuthalEquidistant = getAzimuthalEquidistant(geogCS, refXy, horizontalUnit);
                    ICrs localAziEqu = localCRS(sourceCRS, azimuthalEquidistant);
                    result = new ProjectionCorrectionSet(convergenceAngle, scaleFactor, azimuthalEquidistant, localAziEqu);
                }
            } catch (Exception e) { // unexpected
                errors.add(e.getMessage());
            }
        }
        return result;
    }
    
    private double computeSingleCorrection(double[] xy, double azimuth, AzimuthReferenceType from)
            throws Exception {
        ISisCrs aziEqu = this.getAzimuthalEquidistant(projCS, xy);
        return computeConvergenceAngleAndScale(azimuth, from, aziEqu, projCS)[0];
    }
    
    private double[] computeConvergenceAngleAndScale(double azimuth, AzimuthReferenceType
            from, ISisCrs aziEqu, ISisCrs prjCrs)
            throws Exception {
        double[] pts = AE_PTS.clone();
        SisTransformations.projToGeog(aziEqu, 2, pts); // for the CRS definition
        SisTransformations.geogToProj(prjCrs, 2, pts); // for the CRS definition

        double dx = pts[2] - pts[0];
        double dy = pts[3] - pts[1];
        double angle, scale;
        if (dx != 0.0 || dy != 0.0) {
            angle = (0.5 * Math.PI - Math.atan2(dy, dx)) * RAD_2_DEGREES;
            if (from == AzimuthReferenceType.TRUE_NORTH) {
                // to GridNorth
                azimuth = (azimuth + angle) % 360.0;
                scale = Math.sqrt(dx * dx + dy * dy) / (AE_PTS[3] - AE_PTS[1]);
            } else {
                azimuth = (azimuth - angle) % 360.0;
                scale = (AE_PTS[3] - AE_PTS[1]) / Math.sqrt(dx * dx + dy * dy);
            }
            if (azimuth < 0.0) azimuth += 360.0;
            return new double[]{azimuth, scale};
        }
        return new double[]{Double.NaN, Double.NaN};
    }    
    
    private static final int AzimuthalEquidistantCode = SisParameterDefs.PE_PRJ_AZIMUTHAL_EQUIDISTANT;
    private static final int[] ParameterCodes = {
            SisParameterDefs.PE_PAR_FALSE_EASTING,
            SisParameterDefs.PE_PAR_FALSE_NORTHING,
            SisParameterDefs.PE_PAR_CENTRAL_MERIDIAN,
            SisParameterDefs.PE_PAR_LATITUDE_OF_ORIGIN};
    private static final double[] AE_PTS = new double[]{0.0, -25.0, 0.0, 25.0};   // test coordinates Azimuthal_Equidistant
    private static final double RAD_2_DEGREES = 180.0 / Math.PI;    
    
    private ISisCrs getAzimuthalEquidistant(ISisCrs crs, double[] xy)
            throws Exception {
        double[] ll = xy.clone();
        SisTransformations.projToGeog(crs, 1, ll); // for the CRS definition
        String name = String.format("Azimuthal Equidistant Lng=%1$.8f;Lat=%2$.8f", ll[0], ll[1]);
        WktParameters[] params = new WktParameters[16];  // weird, it needs 16 slots
        for (int i = 0; i < ParameterCodes.length; i++) {
            WktParameters par = WktParametersFactory.parameter(ParameterCodes[i]);
            double value = 0.0;
            if (i == 2) value = ll[0];
            else if (i == 3) value = ll[1];
            params[i] = new WktParameters(par.getName(), value);
        }
        WktFactory wktFactory = new WktFactory();
        String wkt = wktFactory.createAzimuthEquidistantFromProjection(crs, name, params);
        
        SisCrsFactory factory = new SisCrsFactory();
        return factory.createSisCrs(wkt, new AuthorityCode("", ""), "Azimuthal Equidistant");
    }

    ISisCrs getAzimuthalEquidistant(ISisCrs crs, double[] xy, IUnit horizontalUnit)
            throws Exception {
        double[] ll = xy.clone();
        String name = String.format("Azimuthal Equidistant Lng=%1$.8f;Lat=%2$.8f", ll[0], ll[1]);

        WktParameters[] params = new WktParameters[16];  // weird, it needs 16 slots
        for (int i = 0; i < ParameterCodes.length; i++) {
            WktParameters par = WktParametersFactory.parameter(ParameterCodes[i]);
            double value = 0.0;
            if (i == 2) value = ll[0];
            else if (i == 3) value = ll[1];
            params[i] = new WktParameters(par.getName(), value);
        }
        WktFactory wktFactory = new WktFactory();
        String wkt = wktFactory.createAzimuthEquidistantFromBase(crs, name, params, horizontalUnit);
        SisCrsFactory factory = new SisCrsFactory();
        return factory.createSisCrs(wkt, new AuthorityCode("", ""), "Azimuthal Equidistant");
    }

    private ICrs localCRS(ICrs refCRS, ISisCrs aziEqu) {
        ICrs result = null;
        LateBoundCrs lbCrs = new LateBoundCrs();
        lbCrs.setProjectedCrs(aziEqu);
        lbCrs.setType(CRSType.LATE_BOUND);
        lbCrs.setEngineVersion(refCRS.getEngineVersion());
        lbCrs.setAuthorityCode(new AuthorityCode("", "")); // set empty AuthorityCode
        lbCrs.setWellKnownText(aziEqu.toString());
        lbCrs.setName(aziEqu.getName());
        lbCrs.setBaseGeographicCrs(aziEqu.getGeogCoordSys());
        lbCrs.isValid();
        if (refCRS.getType() == CRSType.EARLY_BOUND) {
            IEarlyBoundCrs ref = (IEarlyBoundCrs) refCRS;
            IEarlyBoundCrs ebCRS = new EarlyBoundCrs();
            ebCRS.setLateBoundCrs(lbCrs);
            ebCRS.setTrf(ref.getTrf());
            ebCRS.setType(CRSType.EARLY_BOUND);
            ebCRS.setName(lbCrs.getName() + " - " + ref.getTrf().getName());
            ebCRS.setEngineVersion(ref.getEngineVersion());
            ebCRS.isValid(); // load the Esri objects
            result = ebCRS;
        }
        if (refCRS.getType() == CRSType.LATE_BOUND) {
            result = lbCrs;
        }
        return result;
    }
    
}
