package org.opengroup.osdu.crs.sis;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengroup.osdu.crs.model.Impl.AuthorityCode;

public interface ISisCrs {

    public CoordinateReferenceSystem getCoordinateReferenceSystem();

    public boolean isNegativeScale();

    public String getName();

    public ISisCrs getGeogCoordSys();

    public boolean isEqual(ISisCrs otherSISCrs);

    public AuthorityCode getAuthorityCode();

    public String getWkt();
}
