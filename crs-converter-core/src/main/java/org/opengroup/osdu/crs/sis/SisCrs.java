package org.opengroup.osdu.crs.sis;

import org.apache.sis.metadata.iso.citation.Citations;
import org.apache.sis.referencing.IdentifiedObjects;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengroup.osdu.crs.model.Impl.AuthorityCode;

public class SisCrs implements ISisCrs {

    private final CoordinateReferenceSystem coordianteReferenceSystem;
    private final boolean negativeScale;
    private final String wkt;
    private final AuthorityCode authorityCode;
    private final String name;
    private final boolean isProjection;
    private ISisCrs baseCrs;

    public SisCrs(CoordinateReferenceSystem coordianteReferenceSystem, boolean negativeScale, String wkt, AuthorityCode code, String name) {
        this.coordianteReferenceSystem = coordianteReferenceSystem;
        this.negativeScale = negativeScale;
        this.wkt = wkt;
        this.authorityCode = code;
        this.name = name;
        this.isProjection = coordianteReferenceSystem instanceof ProjectedCRS;
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return coordianteReferenceSystem;
    }

    @Override
    public boolean isNegativeScale() {
        return negativeScale;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ISisCrs getGeogCoordSys() throws IllegalArgumentException {
        if (!isProjection) {
            return null;
        }
        if (baseCrs == null) {
            try {
                GeographicCRS baseCRS = ((ProjectedCRS) coordianteReferenceSystem).getBaseCRS();
                String baseAuthority = "";
                String baseCodeString = "";
                String baseWKT = baseCRS.toWKT();
                Identifier identifier = IdentifiedObjects.getIdentifier(baseCRS, Citations.EPSG);

                if (identifier != null && identifier.getCode() != null) {
                    baseCodeString = identifier.getCode();
                    baseAuthority = "EPSG";
                }
                AuthorityCode baseAuthorityCode = new AuthorityCode(baseAuthority, baseCodeString);
                String baseName = CrsNameUtils.findBaseCrsName(wkt, baseWKT, "");
                SisCrsFactory sisFactory = new SisCrsFactory();
                baseCrs = sisFactory.createSisCrs(baseWKT, baseAuthorityCode, baseName);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Can't find base crs from projected crs", ex);
            }
        }

        //get the base crs
        return baseCrs;
    }

    @Override
    public boolean isEqual(ISisCrs otherSISCrs) {
        Identifier identifier = IdentifiedObjects.getIdentifier(coordianteReferenceSystem, Citations.EPSG);
        Identifier otherIdentifier = IdentifiedObjects.getIdentifier(otherSISCrs.getCoordinateReferenceSystem(), Citations.EPSG);
        if (identifier != null && otherIdentifier != null) {
            return identifier.getCode().equals(otherIdentifier.getCode());
        }

        return coordianteReferenceSystem.equals(otherSISCrs.getCoordinateReferenceSystem());
    }

    @Override
    public String toString() {
        return getWkt();
    }

    @Override
    public String getWkt() {
        if (wkt != null) {
            return wkt;
        }
        return coordianteReferenceSystem.toWKT();
    }

    @Override
    public AuthorityCode getAuthorityCode() {
        return authorityCode;
    }
}
