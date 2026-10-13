package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.sis.transform.ISisMathTransform;

public interface ISingleTrf extends ITrf {

    String getWellKnownText();

    void setWellKnownText(String wkt);
    
    public ISisMathTransform getTransformOperation();
}
