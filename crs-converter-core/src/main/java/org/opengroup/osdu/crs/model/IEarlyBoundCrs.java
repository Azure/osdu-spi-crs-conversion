package org.opengroup.osdu.crs.model;

public interface IEarlyBoundCrs extends ICrs {
    ILateBoundCrs getLateBoundCrs();
    void setLateBoundCrs(ILateBoundCrs crs);

    ITrf getTrf();
    void setTrf(ITrf trf);
}
