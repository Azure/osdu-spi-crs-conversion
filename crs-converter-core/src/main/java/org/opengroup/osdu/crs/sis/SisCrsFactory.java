package org.opengroup.osdu.crs.sis;

import org.apache.sis.referencing.CRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;
import org.opengis.util.NoSuchIdentifierException;
import org.opengroup.osdu.crs.model.Impl.AuthorityCode;

public class SisCrsFactory {

    private final CRSAuthorityFactory epsgFactory;

    public SisCrsFactory() throws FactoryException {
        epsgFactory = CRS.getAuthorityFactory("EPSG");
    }

    public ISisCrs createSisCrs(String wkt, AuthorityCode authorityCode, String crsName) throws Exception {
        if (AuthorityCodeUtils.isEpsgCode(authorityCode)) {
            try {
                //try to create crs from authority code first
                CoordinateReferenceSystem crs = epsgFactory.createCoordinateReferenceSystem(authorityCode.getCode());
                //make sure wkt provided is valid
                if (isInvalidWkt(wkt, crsName, authorityCode)) {
                    throw new SisCrsFactoryException("Invalid wkt");
                }
                return new SisCrs(crs, false, wkt, authorityCode, crsName);
            } catch (SisCrsFactoryException ex) {
                throw ex;
            } catch (Exception ex) {
                //if crs cannot be created from authority code, try to create it from wkt
                return createSisCrsFromWkt(wkt, crsName, authorityCode);
            }
        }
        //if crs cannot be created from authority code, try to create it from wkt
        return createSisCrsFromWkt(wkt, crsName, authorityCode);
    }

    //make sure that wkt provided is valid
    private boolean isInvalidWkt(String wkt, String crsName, AuthorityCode authorityCode) {
        //handle the case where wkt is not specified
        if (wkt == null || wkt.isEmpty()) {
            return false;
        }
        try {
            createSisCrsFromWkt(wkt, crsName, authorityCode);
            return false;
        } catch (NoSuchIdentifierException ex) {
            return false;
        } catch (Exception ex) {
            return true;
        }
    }

    private ISisCrs createSisCrsFromWkt(String wkt, String crsName, AuthorityCode authorityCode) throws Exception {
        if (wkt == null) {
            throw new Exception("Can't create CoordinateReferenceSystem: Missing wkt");
        }
        if (wkt.contains("PROJECTION[\"Azimuthal_Equidistant\"]")) {
            wkt = wkt.replace("PROJECTION[\"Azimuthal_Equidistant\"]", "PROJECTION[\"Modified Azimuthal_Equidistant\"]");
        }
        boolean negativeScale = false;
        // Temporary workaround because library does not support negative scale factors
        if (wkt.contains("PARAMETER[\"Scale_Factor\",-1.0]")) {
            negativeScale = true;
            wkt = wkt.replace("PARAMETER[\"Scale_Factor\",-1.0]", "PARAMETER[\"Scale_Factor\",1.0]");
        }
        CoordinateReferenceSystem crs = CRS.fromWKT(wkt);
        return new SisCrs(crs, negativeScale, wkt, authorityCode, crsName);
    }

}
