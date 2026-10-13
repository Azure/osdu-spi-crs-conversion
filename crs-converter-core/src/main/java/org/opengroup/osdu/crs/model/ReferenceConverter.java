package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.model.Impl.LateBoundCrs;
import org.opengroup.osdu.crs.model.Impl.Unit;
import org.opengroup.osdu.crs.model.v1.CRS;
import org.opengroup.osdu.crs.model.v2.PersistableReference;

import static org.opengroup.osdu.crs.model.Impl.ItemFactory.createModel;

public final class ReferenceConverter {
    private ReferenceConverter() {}
    private static final String V2_START = "{";
    private static final String V1_START = "%7B";
    private static final String V2_STOP = "}";
    private static final String V1_STOP = "%7D";

    // private static final List
    public static IItem parseSpatialReference(String reference) {
        IItem result, raw = new LateBoundCrs();
        String cleaned = reference.trim();
        if (cleaned.startsWith(V1_START) && cleaned.endsWith(V1_STOP)){
            CRS instance = CRS.createInstance(cleaned);
            result = createModel(instance);
        } else if (cleaned.startsWith(V2_START) && cleaned.endsWith(V2_STOP)) {
            PersistableReference instance = PersistableReference.createInstance(cleaned);
            result = createModel(instance);
        } else {
            result = null;
        }
        if (result != null) {
            if (result instanceof ICrs) raw = result;
            else if (result instanceof ITrf) raw = result;
        }
        return raw;
    }

    public static IUnit parseUnitReference(String reference) {
        IItem raw;
        IUnit result = new Unit();
        if (reference != null) {
            String cleaned = reference.trim();
            if (cleaned.startsWith(V1_START) && cleaned.endsWith(V1_STOP)) {
                org.opengroup.osdu.crs.model.v1.Unit instance = org.opengroup.osdu.crs.model.v1.Unit.createInstance(cleaned);
                raw = createModel(instance);
                if (raw != null) result = (IUnit) raw;
            } else if (cleaned.startsWith(V2_START) && cleaned.endsWith(V2_STOP)) {
                PersistableReference instance = PersistableReference.createInstance(cleaned);
                raw = createModel(instance);
                if (raw instanceof IUnit) result = (IUnit) raw;
            }
        }
        return result;
    }
}
