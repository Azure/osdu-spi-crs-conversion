package org.opengroup.osdu.crs.swagger;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Test;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.converter.CRSConverter;
import org.opengroup.osdu.crs.converter.TrajectoryConverter;
import org.opengroup.osdu.crs.api.exception.BadRequestException;
import org.opengroup.osdu.crs.model.*;
import org.opengroup.osdu.crs.util.Constants;
import java.util.Collections;
import static org.junit.Assert.*;

public class ContractNullabilityAndProjectionTest {
    @Test
    public void invalidSurveyValuesAreRejectedByBothValidators() throws Exception {
        String template = "{\"trajectoryCRS\":%s,\"unitXY\":%s,\"unitZ\":%s,"
                + "\"azimuthReference\":\"GN\",\"method\":\"AzimuthalEquidistant\","
                + "\"referencePoint\":{\"x\":0,\"y\":0,\"z\":0},\"inputStations\":[%s]}";
        ObjectMapper mapper = new JsonMapper();
        TrajectoryConverter converter = new TrajectoryConverter();
        for (String station : new String[]{"null", "{\"md\":null,\"inclination\":0,\"azimuth\":0}",
                "{\"md\":1e309,\"inclination\":0,\"azimuth\":0}",
                "{\"md\":0,\"inclination\":-1e309,\"azimuth\":0}",
                "{\"md\":0,\"inclination\":0,\"azimuth\":1e309}"}) {
            String json = String.format(template, mapper.writeValueAsString(Constants.WGS84),
                    mapper.writeValueAsString(Constants.SWAGGER_TRJ_REQ_UNIT_EXAMPLE),
                    mapper.writeValueAsString(Constants.SWAGGER_TRJ_REQ_UNIT_EXAMPLE), station);
            TrajectoryComputationState state = new TrajectoryComputationState();
            assertFalse(converter.isRequestValid(mapper.readValue(json, ConvertTrajectoryRequest.class), state));
            assertTrue(state.getErrors().contains("Invalid input trajectory survey stations values."));
            TrajectoryComputationStateV4 stateV4 = new TrajectoryComputationStateV4();
            assertFalse(converter.isRequestValidV4(mapper.readValue(json,
                    org.opengroup.osdu.crs.model.v4.ConvertTrajectoryRequestV4.class), stateV4));
            assertTrue(stateV4.getErrors().contains("Invalid input trajectory survey stations values."));
        }
    }

    @Test
    public void responseUnionAllowsNumbersAndMarkersAtOuterLevel() {
        Schema<?> point = ModelConverters.getInstance(true).read(PointResponse.class).get("PointResponse");
        assertTrue(point.getProperties().get("x").getTypes().contains("string"));
        assertTrue(point.getProperties().get("x").getTypes().contains("number"));
    }

    @Test(expected = tools.jackson.databind.exc.InvalidNullException.class)
    public void explicitNullInterpolateIsRejected() throws Exception {
        new JsonMapper().readValue("{\"interpolate\":null}", ConvertTrajectoryRequest.class);
    }

    @Test
    public void omittedInterpolateKeepsDefault() throws Exception {
        assertTrue(new JsonMapper().readValue("{}", ConvertTrajectoryRequest.class).isInterpolate());
    }

    @Test
    public void optionalGeoJsonMetadataAllowsNull() {
        Schema<?> collection = ModelConverters.getInstance(true).read(GeoJsonFeatureCollection.class).get("GeoJsonFeatureCollection");
        assertTrue(collection.getProperties().get("persistableReferenceUnitZ").getTypes().contains("null"));
    }

    @Test
    public void emptyAnyCrsCollectionDoesNotNeedSourceCrs() {
        GeoJsonFeatureCollection collection = new GeoJsonFeatureCollection();
        collection.setFeatures(new org.opengroup.osdu.crs.GeoJson.GeoJsonFeature[0]);
        ConvertGeoJsonResponse response = new CRSConverter().convertGeoJson(collection, Constants.WGS84, null);
        assertEquals(Integer.valueOf(0), response.getSuccessCount());
    }

    @Test(expected = BadRequestException.class)
    public void invalidGeographicReferencePointIsBadRequest() {
        ConvertTrajectoryRequest request = new ConvertTrajectoryRequest();
        request.setTrajectoryCRS(Constants.WGS84);
        request.setReferencePoint(new Point(3296099658495631.0, 8.164610318395175e269, 2.2542923300287693e256));
        request.setUnitXY(Constants.SWAGGER_TRJ_REQ_UNIT_EXAMPLE);
        request.setUnitZ(Constants.SWAGGER_TRJ_REQ_UNIT_EXAMPLE);
        request.setAzimuthReference("GN");
        request.setMethod("LeesModifiedProposal");
        request.setInputStations(Collections.singletonList(new TrajectoryStationIn(-2.403236346863823e203, 1.0, 1.0, Double.MAX_VALUE, 0.0, 0.0)));
        new TrajectoryConverter().convertTrajectory(new DpsHeaders(), request);
    }
}
