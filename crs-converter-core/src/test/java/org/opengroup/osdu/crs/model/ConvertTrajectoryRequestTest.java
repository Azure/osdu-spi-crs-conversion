package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.util.ConstantsTests;
import org.opengroup.osdu.crs.model.ConvertTrajectoryRequest;
import org.opengroup.osdu.crs.model.TrajectoryStationIn;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ConvertTrajectoryRequestTest {

    private ConvertTrajectoryRequest createValidRequest(int v){
        ConvertTrajectoryRequest request = new ConvertTrajectoryRequest();
        request.setAzimuthReference("TN");
        request.setMethod("AzimuthalEquidistant");
        List<Point> pts = new ArrayList<>();
        pts.add(new Point(-92.0, 29.0, 0.0));
        request.setReferencePoint(pts.get(0));
        request.setTrajectoryCRS(ConstantsTests.SPCS_27_1702[v]);
        request.setUnitXY(ConstantsTests.UNIT_FT_US);
        request.setUnitZ(ConstantsTests.UNIT_FT);
        request.setInputKind(TrajectoryInputKind.MD_INCL_AZIM.toString());
        List<TrajectoryStationIn> tsi = new ArrayList<>();
        tsi.add(new TrajectoryStationIn(0.0, 0.0, 0.0, Double.NaN, Double.NaN, Double.NaN));
        tsi.add(new TrajectoryStationIn(1000.0, 0.0, 0.0, Double.NaN, Double.NaN, Double.NaN));
        tsi.add(new TrajectoryStationIn(2000.0, 90.0, 0.0, Double.NaN, Double.NaN, Double.NaN));
        tsi.add(new TrajectoryStationIn(3000.0, 90.0, 0.0, Double.NaN, Double.NaN, Double.NaN));
        tsi.add(new TrajectoryStationIn(5000.0, 90.0, 90.0, Double.NaN, Double.NaN, Double.NaN));
        tsi.add(new TrajectoryStationIn(6000.0, 90.0, 90.0, Double.NaN, Double.NaN, Double.NaN));
        request.setInputStations(tsi);
        return request;
    }
    @Test
    public void convertTrajectoryRequestToAndFromJson(){
        ConvertTrajectoryRequest request=createValidRequest(ConstantsTests.V1);
        assertNotNull(request);
        assertEquals(6, request.getInputStations().size());
        String json = request.toString();
        assertNotNull(json);
        ConvertTrajectoryRequest decoded = ConvertTrajectoryRequest.createInstance(json);
        assertNotNull(decoded);
        assertEquals(request.getInputStations().size(), decoded.getInputStations().size());
        assertEquals(request.getAzimuthReference(), decoded.getAzimuthReference());
        assertEquals(request.getMethod(), decoded.getMethod());
        assertEquals(request.getReferencePoint(), decoded.getReferencePoint());
        assertEquals(request.getTrajectoryCRS(), decoded.getTrajectoryCRS());
        assertEquals(request.getInputKind(), TrajectoryInputKind.MD_INCL_AZIM.toString());
        assertEquals(request.getUnitXY(), decoded.getUnitXY());
        assertEquals(request.getUnitZ(), decoded.getUnitZ());
        for (int i=0; i<request.getInputStations().size(); i++){
            assertEquals(request.getInputStations().get(i), decoded.getInputStations().get(i));
        }
    }
    @Test
    public void convertTrajectoryRequestToAndFromJsonFailures() {
        ConvertTrajectoryRequest request=createValidRequest(ConstantsTests.V1);
        assertNotNull(request);
        assertEquals(6, request.getInputStations().size());
        String json = request.toString();
        assertNotNull(json);
        String corrupt = json.replaceFirst("inputStations", "corrupted");
        ConvertTrajectoryRequest decoded = ConvertTrajectoryRequest.createInstance(corrupt);
        assertNull(decoded);
    }
}
