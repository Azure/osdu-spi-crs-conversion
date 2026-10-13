package org.opengroup.osdu.crs.model;

public interface ILateBoundCrs extends ICrs {
    String getWellKnownText();
    void setWellKnownText(String wkt);
}
