package org.opengroup.osdu.crs.swagger;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Test;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.converter.CRSConverter;
import org.opengroup.osdu.crs.converter.TrajectoryConverter;
import org.opengroup.osdu.crs.model.*;
import org.opengroup.osdu.crs.util.Config;
import org.opengroup.osdu.crs.util.Constants;

import java.util.Collections;
import static org.junit.Assert.*;

public class ConversionContractRegressionTest {
    @Test
    public void openApi31ResponsePropertiesAreNumeric() {
        Schema<?> point = ModelConverters.getInstance(true).read(Point.class).get("Point");
        for (String name : new String[]{"x", "y", "z"}) {
            assertTrue(point.getProperties().get(name).getTypes().contains("number"));
        }
        Schema<?> response = ModelConverters.getInstance(true).read(ConvertPointsResponse.class).get("ConvertPointsResponse");
        assertTrue(response.getProperties().get("successCount").getTypes().contains("integer"));
    }

    @Test
    public void emptyFeatureCollectionConvertsSuccessfully() {
        GeoJsonFeatureCollection collection = new GeoJsonFeatureCollection();
        collection.setType("FeatureCollection");
        collection.setFeatures(new org.opengroup.osdu.crs.GeoJson.GeoJsonFeature[0]);
        collection.setBbox(new double[0]);
        ConvertGeoJsonResponse response = new CRSConverter().convertGeoJson(collection, Constants.WGS84,
                Constants.SWAGGER_TRJ_REQ_UNIT_EXAMPLE);
        assertEquals(Integer.valueOf(0), response.getSuccessCount());
        assertEquals(Integer.valueOf(0), response.getTotalCount());
        assertNull(response.getFeatureCollection().getBbox());
    }

    @Test
    public void interpolatedSingleStationTrajectoryConverts() throws Exception {
        Config.setDpsTrajectoryFeature(false);
        ObjectNode crs = (ObjectNode) new JsonMapper().readTree(Constants.WGS84);
        crs.remove("authCode");
        ConvertTrajectoryRequest request = new ConvertTrajectoryRequest();
        request.setTrajectoryCRS(crs.toString());
        request.setUnitXY(Constants.SWAGGER_TRJ_REQ_UNIT_EXAMPLE);
        request.setUnitZ(Constants.SWAGGER_TRJ_REQ_UNIT_EXAMPLE);
        request.setAzimuthReference("TRUE_NORTH");
        request.setMethod("AzimuthalEquidistant");
        request.setReferencePoint(new Point(-61.04340628871454, 10.673103179456877, 0.0));
        request.setInputStations(Collections.singletonList(new TrajectoryStationIn(2563.56, 15.0, 355.0, 55.9, -145.3, 1965.6)));
        ConvertTrajectoryResponse response = new TrajectoryConverter().convertTrajectory(new DpsHeaders(), request);
        assertEquals(1, response.getStations().size());
        assertTrue(Double.isFinite(response.getStations().get(0).getPoint().getX()));
    }

    @Test
    public void nullPointsAreRejectedByValidation() {
        try (jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            ConvertPointsRequest request = new ConvertPointsRequest();
            request.setPoints(Collections.singletonList(null));
            assertFalse(factory.getValidator().validateProperty(request, "points").isEmpty());
            ConvertPointsRequestV4 requestV4 = new ConvertPointsRequestV4();
            requestV4.setPoints(Collections.singletonList(null));
            assertFalse(factory.getValidator().validateProperty(requestV4, "points").isEmpty());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyCoordinatesStillRequireValidCrs() {
        new CRSConverter().convertPoint("{}", "{}", new double[0], new double[0]);
    }

    @Test
    public void verticalStationAtEquatorHasFiniteCoordinates() {
        Config.setDpsTrajectoryFeature(false);
        ConvertTrajectoryRequest request = new ConvertTrajectoryRequest();
        request.setTrajectoryCRS(Constants.WGS84);
        request.setUnitXY(Constants.SWAGGER_TRJ_REQ_UNIT_EXAMPLE);
        request.setUnitZ(Constants.SWAGGER_TRJ_REQ_UNIT_EXAMPLE);
        request.setAzimuthReference("TRUE_NORTH");
        request.setMethod("AzimuthalEquidistant");
        request.setReferencePoint(new Point(0.0, 0.0, 0.0));
        request.setInputStations(Collections.singletonList(new TrajectoryStationIn(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)));
        ConvertTrajectoryResponse response = new TrajectoryConverter().convertTrajectory(new DpsHeaders(), request);
        assertEquals(0.0, response.getStations().get(0).getPoint().getX(), 1e-9);
        assertEquals(0.0, response.getStations().get(0).getPoint().getY(), 1e-9);
    }

    @Test(expected = tools.jackson.databind.exc.MismatchedInputException.class)
    public void missingPointCoordinateIsRejectedDuringDeserialization() throws Exception {
        new JsonMapper().readValue("{\"y\":0.3048,\"z\":0.0}", Point.class);
    }

    @Test
    public void nonFiniteAndNullCoordinatesAreRejected() {
        try (jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            assertFalse(factory.getValidator().validate(new Point(Double.NaN, 0.0, 0.0)).isEmpty());
            ConvertTrajectoryRequest request = new ConvertTrajectoryRequest();
            request.setReferencePoint(new Point(0.0, 0.0, null));
            assertTrue(factory.getValidator().validate(request).stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("referencePoint.z")));
            assertFalse(Point.isValid(request.getReferencePoint()));
        }
    }

    @Test
    public void responseSchemasDescribePointsBooleansAndNonFiniteMarkers() {
        java.util.Map<String, Schema> schemas = ModelConverters.getInstance(true).readAll(TrajectoryStationOut.class);
        Schema<?> station = schemas.get("TrajectoryStationOut");
        assertTrue(station.getProperties().get("original").getTypes().contains("boolean"));
        assertEquals("#/components/schemas/PointResponse", station.getProperties().get("point").get$ref());
        assertNotNull(station.getProperties().get("wgs84Latitude").getOneOf());
        io.swagger.v3.oas.models.media.Schema<?> bareDepth = new io.swagger.v3.oas.models.media.Schema<>();
        bareDepth.addType("number");
        bareDepth.setFormat("double");
        OpenAPI verticalDepth = new OpenAPI().components(new Components().addSchemas("TrajectoryStationOut",
                new io.swagger.v3.oas.models.media.Schema<>().addProperty("dz", bareDepth)));
        SwaggerConfiguration.allowNonFiniteVerticalDepth(verticalDepth);
        assertNotNull(bareDepth.getOneOf());
        assertTrue(bareDepth.getOneOf().stream()
                .anyMatch(branch -> branch.getEnum() != null && branch.getEnum().contains("NaN")));
        Schema<?> point = schemas.get("PointResponse");
        assertNotNull(point.getProperties().get("x").getOneOf());
        Schema<?> coordinate = point.getProperties().get("x");
        assertTrue(coordinate.getOneOf().stream()
                .anyMatch(branch -> branch.getEnum() != null && branch.getEnum().contains("NaN")));
        Schema<?> response = ModelConverters.getInstance(true).read(ConvertPointsResponse.class).get("ConvertPointsResponse");
        assertEquals("#/components/schemas/PointResponse", response.getProperties().get("points").getItems().get$ref());
        OpenAPI resolvedAsRequestPoint = new OpenAPI().components(new Components()
                .addSchemas("ConvertPointsResponse", new Schema<>().addProperty("points",
                        new ArraySchema().items(new Schema<>().$ref("#/components/schemas/Point"))))
                .addSchemas("ConvertPointsResponseV4", new Schema<>().addProperty("points",
                        new ArraySchema().items(new Schema<>().$ref("#/components/schemas/Point")))));
        SwaggerConfiguration.useComputedPointSchema(resolvedAsRequestPoint);
        assertEquals("#/components/schemas/PointResponse", ((Schema<?>) resolvedAsRequestPoint.getComponents().getSchemas()
                .get("ConvertPointsResponse").getProperties().get("points")).getItems().get$ref());
        assertEquals("#/components/schemas/PointResponse", ((Schema<?>) resolvedAsRequestPoint.getComponents().getSchemas()
                .get("ConvertPointsResponseV4").getProperties().get("points")).getItems().get$ref());
        assertNotNull(resolvedAsRequestPoint.getComponents().getSchemas().get("PointResponse"));
        Schema<?> input = ModelConverters.getInstance(true).read(Point.class).get("Point");
        assertNull(input.getProperties().get("x").getOneOf());
    }
}
