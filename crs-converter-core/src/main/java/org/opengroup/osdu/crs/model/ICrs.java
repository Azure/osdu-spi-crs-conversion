package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.model.Impl.AuthorityCode;
import org.opengroup.osdu.crs.sis.ISisCrs;
import org.opengroup.osdu.crs.model.CRSType;

// shared interface for coordinate reference systems
public interface ICrs extends IItem {
    boolean isProjectedCrs();
    boolean isGeographicCrs();
    boolean isVerticalCrs();

    String getEngineVersion();
    void setEngineVersion(String version);

    CRSType getType();
    void setType(CRSType type);

    String getName();
    void setName(String name);

    AuthorityCode getAuthorityCode();
    void setAuthorityCode(AuthorityCode ac);

    public ISisCrs getBaseGeographicCrs();

    public ISisCrs getProjectedCrs();
}
