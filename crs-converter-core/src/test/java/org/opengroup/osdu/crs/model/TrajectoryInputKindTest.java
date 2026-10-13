package org.opengroup.osdu.crs.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class TrajectoryInputKindTest {
    @Test
    public void TestValidTrajectoryInputKinds(){
        assertEquals(TrajectoryInputKind.MD_INCL_AZIM, TrajectoryInputKind.getTrajectoryInputKind("MD__INCL__AZIM"));
        assertEquals(TrajectoryInputKind.MD_INCL_AZIM, TrajectoryInputKind.getTrajectoryInputKind("mdazimincl"));
        assertEquals(TrajectoryInputKind.MD_INCL_AZIM, TrajectoryInputKind.getTrajectoryInputKind("mdinclazim"));

        assertEquals(TrajectoryInputKind.MD_DX_DY_DZ, TrajectoryInputKind.getTrajectoryInputKind("md dx dy dz"));
        assertEquals(TrajectoryInputKind.MD_DX_DY_DZ, TrajectoryInputKind.getTrajectoryInputKind("MD_DX_DY_DZ"));
        assertEquals(TrajectoryInputKind.MD_DX_DY_DZ, TrajectoryInputKind.getTrajectoryInputKind("mddxdydz"));

        assertEquals(TrajectoryInputKind.MD_X_Y_Z, TrajectoryInputKind.getTrajectoryInputKind("md x y z"));
        assertEquals(TrajectoryInputKind.MD_X_Y_Z, TrajectoryInputKind.getTrajectoryInputKind("MD_X_Y_Z"));
        assertEquals(TrajectoryInputKind.MD_X_Y_Z, TrajectoryInputKind.getTrajectoryInputKind("mdxyz"));

        assertEquals(TrajectoryInputKind.DX_DY_DZ, TrajectoryInputKind.getTrajectoryInputKind("dx dy dz"));
        assertEquals(TrajectoryInputKind.DX_DY_DZ, TrajectoryInputKind.getTrajectoryInputKind("DX_DY_DZ"));
        assertEquals(TrajectoryInputKind.DX_DY_DZ, TrajectoryInputKind.getTrajectoryInputKind("dxdydz"));

        assertEquals(TrajectoryInputKind.X_Y_Z, TrajectoryInputKind.getTrajectoryInputKind("x y z"));
        assertEquals(TrajectoryInputKind.X_Y_Z, TrajectoryInputKind.getTrajectoryInputKind("X_Y_Z"));
        assertEquals(TrajectoryInputKind.X_Y_Z, TrajectoryInputKind.getTrajectoryInputKind("xyz"));

        assertNotNull(TrajectoryInputKind.MD_INCL_AZIM.toString());
    }

    @Test
    public void TestInvalidTrajectoryInputKinds(){
        assertNull(TrajectoryInputKind.getTrajectoryInputKind(""));
        assertNull(TrajectoryInputKind.getTrajectoryInputKind(null));

        assertNull(TrajectoryInputKind.getTrajectoryInputKind("M__INCL__AZIM"));
        assertNull(TrajectoryInputKind.getTrajectoryInputKind("MD____AZIM"));
        assertNull(TrajectoryInputKind.getTrajectoryInputKind("MD__INCL__"));
        assertNull(TrajectoryInputKind.getTrajectoryInputKind("INAZ"));

        assertNull(TrajectoryInputKind.getTrajectoryInputKind("MDXZ"));
        assertNull(TrajectoryInputKind.getTrajectoryInputKind("MDYZ"));
        assertNull(TrajectoryInputKind.getTrajectoryInputKind("MDXY"));

        assertNull(TrajectoryInputKind.getTrajectoryInputKind("XY"));
        assertNull(TrajectoryInputKind.getTrajectoryInputKind("XZ"));
        assertNull(TrajectoryInputKind.getTrajectoryInputKind("YZ"));

        assertNull(TrajectoryInputKind.getTrajectoryInputKind("DXDY"));
        assertNull(TrajectoryInputKind.getTrajectoryInputKind("DXDZ"));
        assertNull(TrajectoryInputKind.getTrajectoryInputKind("DYDZ"));

        assertNull(TrajectoryInputKind.getTrajectoryInputKind("MDDXDZ"));
        assertNull(TrajectoryInputKind.getTrajectoryInputKind("MDDYDZ"));
        assertNull(TrajectoryInputKind.getTrajectoryInputKind("MDDXDY"));
    }
}
