package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.model.Impl.AuthorityCode;

public interface ITrf extends IItem {

    String getEngineVersion();
    void setEngineVersion(String version);

    CRSType getType();
    void setType(CRSType type);

    String getName();
    void setName(String name);

    AuthorityCode getAuthorityCode();
    void setAuthorityCode(AuthorityCode ac);

    boolean equalInBehavior(ITrf otherTrf);
}
