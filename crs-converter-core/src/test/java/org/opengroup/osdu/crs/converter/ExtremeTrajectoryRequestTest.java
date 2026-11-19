package org.opengroup.osdu.crs.converter;

import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.crs.model.*;
import org.opengroup.osdu.crs.util.Constants;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtremeTrajectoryRequestTest {
    @Test
    void extremeProjectedSurveyKeepsOriginalStations() {
        String wkt = "PROJCS[\"WGS 84 / UTM zone 31N\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\","
                + "SPHEROID[\"WGS 84\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],"
                + "UNIT[\"degree\",0.0174532925199433]],PROJECTION[\"Transverse_Mercator\"],"
                + "PARAMETER[\"latitude_of_origin\",0],PARAMETER[\"central_meridian\",3],"
                + "PARAMETER[\"scale_factor\",0.9996],PARAMETER[\"false_easting\",500000],"
                + "PARAMETER[\"false_northing\",0],UNIT[\"metre\",1]]";
        ConvertTrajectoryRequest request = new ConvertTrajectoryRequest();
        request.setTrajectoryCRS("{\"type\":\"LBC\",\"wkt\":" + new com.google.gson.Gson().toJson(wkt) + "}");
        request.setUnitXY(Constants.SWAGGER_TRJ_REQ_UNIT_EXAMPLE.replace("1.0", "0.3048"));
        request.setUnitZ(request.getUnitXY());
        request.setReferencePoint(new Point(2.225073858507e-311, Double.MAX_VALUE, -4.898170837159768e16));
        request.setMethod("LMP");
        request.setAzimuthReference("GN");
        request.setInputStations(Arrays.asList(
                new TrajectoryStationIn(-6.917870711263233e16, 2.796561391674482e16, Double.MAX_VALUE, 0., 0., 0.),
                new TrajectoryStationIn(-6.604796954792767e217, -4.952333936320569e16, 4.2347823248653786e241, 0., 0., 0.),
                new TrajectoryStationIn(-Double.MAX_VALUE, 1.0652326572118423e37, 9.123830812614506e249, 0., 0., 0.),
                new TrajectoryStationIn(2.225073858507203e-309, -8.76228761311287e58, 6.869511246898476e16, 0., 0., 0.),
                new TrajectoryStationIn(6553173295703320., -8.737533201195969e73, -1.4058656942869321e138, 0., 0., 0.)));
        ConvertTrajectoryResponse response = new TrajectoryConverter().convertTrajectory(new DpsHeaders(), request);
        assertNotNull(response);
        assertEquals(request.getInputStations().size(), response.getStations().size());
        assertEquals(Double.MAX_VALUE, request.getReferencePoint().getY());
        assertTrue(Double.isNaN(response.getStations().get(0).getPoint().getX()));
        assertTrue(Double.isNaN(response.getStations().get(0).getDxTN()));
        assertTrue(Double.isNaN(response.getStations().get(response.getStations().size() - 1).getDyTN()));
        assertTrue(Double.isNaN(response.getStations().get(response.getStations().size() - 1).getDZ()));
    }
}
