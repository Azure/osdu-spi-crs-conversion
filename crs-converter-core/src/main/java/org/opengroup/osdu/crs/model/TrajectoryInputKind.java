package org.opengroup.osdu.crs.model;

import com.google.common.base.Strings;

public enum TrajectoryInputKind {
    MD_INCL_AZIM("MD_Incl_Azim"),
    MD_X_Y_Z("MD_X_Y_Z"),
    MD_DX_DY_DZ("MD_dX_dY_dZ"),
    X_Y_Z("X_Y_Z"),
    DX_DY_DZ("dX_dY_dZ");
    private final String kind;
    TrajectoryInputKind(final String method) {
        this.kind = method;
    }
    public static TrajectoryInputKind getTrajectoryInputKind(String hint) {
        if (!Strings.isNullOrEmpty(hint)) {
            String kind = hint.toUpperCase();
            if (kind.contains("MD") && kind.contains("INC") && kind.contains("AZ")) return MD_INCL_AZIM;
            if (kind.contains("MD")) {
                if (kind.contains("DX") && kind.contains("DY") && kind.contains("DZ")) return MD_DX_DY_DZ;
                if (kind.contains("X") && kind.contains("Y") && kind.contains("Z")) return MD_X_Y_Z;
            }
            if (kind.contains("DX") && kind.contains("DY") && kind.contains("DZ")) return DX_DY_DZ;
            if (kind.contains("X") && kind.contains("Y") && kind.contains("Z")) return X_Y_Z;
        }
        return null;
    }

    @Override
    public String toString() {
        return this.kind;
    }
}
