package org.opengroup.osdu.crs.model.Impl;

import org.opengroup.osdu.crs.model.IItem;
import org.opengroup.osdu.crs.model.v2.UnitEnergistics;
import org.opengroup.osdu.crs.model.v2.UnitScaleOffset;

public class ItemFactory {
    private ItemFactory(){}
    public static IItem createModel(Object parsedRaw) {
        IItem result = null;
        if (parsedRaw instanceof org.opengroup.osdu.crs.model.v1.EarlyBoundCRS) {
            result = new EarlyBoundCrs((org.opengroup.osdu.crs.model.v1.EarlyBoundCRS) parsedRaw);
        }
        else if (parsedRaw instanceof org.opengroup.osdu.crs.model.v2.EarlyBoundCrs) {
            result = new EarlyBoundCrs((org.opengroup.osdu.crs.model.v2.EarlyBoundCrs) parsedRaw);
        }
        else if (parsedRaw instanceof org.opengroup.osdu.crs.model.v1.LateBoundCRS) {
            result = new LateBoundCrs((org.opengroup.osdu.crs.model.v1.LateBoundCRS) parsedRaw);
        }
        else if (parsedRaw instanceof org.opengroup.osdu.crs.model.v2.LateBoundCrs) {
            result = new LateBoundCrs((org.opengroup.osdu.crs.model.v2.LateBoundCrs) parsedRaw);
        }
        else if (parsedRaw instanceof org.opengroup.osdu.crs.model.v1.SingleTRF) {
            result = new SingleTrf((org.opengroup.osdu.crs.model.v1.SingleTRF) parsedRaw);
        }
        else if (parsedRaw instanceof org.opengroup.osdu.crs.model.v2.SingleTrf) {
            result = new SingleTrf((org.opengroup.osdu.crs.model.v2.SingleTrf) parsedRaw);
        }
        else if (parsedRaw instanceof org.opengroup.osdu.crs.model.v1.CompoundTRF) {
            result = new CompoundTrf((org.opengroup.osdu.crs.model.v1.CompoundTRF) parsedRaw);
        }
        else if (parsedRaw instanceof org.opengroup.osdu.crs.model.v2.CompoundTrf) {
            result = new CompoundTrf((org.opengroup.osdu.crs.model.v2.CompoundTrf) parsedRaw);
        }
        else if (parsedRaw instanceof org.opengroup.osdu.crs.model.v1.Unit) {
            result = new Unit((org.opengroup.osdu.crs.model.v1.Unit) parsedRaw);
        }
        else if (parsedRaw instanceof UnitScaleOffset) {
            result = new Unit((UnitScaleOffset) parsedRaw);
        }
        else if (parsedRaw instanceof UnitEnergistics) {
            result = new Unit((UnitEnergistics) parsedRaw);
        }
        return result;
    }
}
