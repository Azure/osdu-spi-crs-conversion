package org.opengroup.osdu.crs.model.v4;

import org.junit.Test;
import org.opengroup.osdu.crs.model.TrajectoryInputKind;

import java.util.List;

import static org.junit.Assert.*;

public class ConvertTrajectoryRequestV4Test {

    @Test
    public void createInstanceKeepsDefaultsForOmittedFields() {
        ConvertTrajectoryRequestV4 decoded = ConvertTrajectoryRequestV4.createInstance(
                "{\"trajectoryCRS\":\"crs\",\"unitZ\":\"ft\",\"method\":\"AzimuthalEquidistant\"}");

        assertNotNull(decoded);
        assertTrue(decoded.isInterpolate());
        assertEquals(TrajectoryInputKind.MD_INCL_AZIM.toString(), decoded.getInputKind());
    }

    @Test
    public void toStringRoundTripsMdInterval() {
        ConvertTrajectoryRequestV4 request = new ConvertTrajectoryRequestV4();
        request.setTrajectoryCRS("crs");
        request.setUnitZ("ft");
        request.setMethod("AzimuthalEquidistant");
        request.setInterpolate(false);
        request.setMD_i(new MinimumDepthInterval(List.of(50.0, 150.0), null));

        String json = request.toString();
        assertNotNull(json);
        assertTrue(json.contains("\"md_i\":{\"md_i\":[50.0,150.0]}"));

        ConvertTrajectoryRequestV4 decoded = ConvertTrajectoryRequestV4.createInstance(json);
        assertNotNull(decoded);
        assertFalse(decoded.isInterpolate());
        assertEquals(List.of(50.0, 150.0), decoded.getMD_i().getMd_i());
    }

    @Test
    public void createInstanceReturnsNullForInvalidJson() {
        assertNull(ConvertTrajectoryRequestV4.createInstance("{\"trajectoryCRS\":"));
    }
}
