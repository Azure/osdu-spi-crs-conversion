package org.opengroup.osdu.crs.swagger;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.exc.InvalidNullException;
import tools.jackson.databind.exc.MismatchedInputException;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import org.junit.jupiter.api.Test;
import jakarta.validation.ValidationException;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.BinGrid.AbstractAnyCrsFeatureCollection;
import org.opengroup.osdu.crs.BinGrid.AbstractFeatureCollection;
import org.opengroup.osdu.crs.BinGrid.AbstractBinGrid;
import org.opengroup.osdu.crs.BinGrid.AbstractFeature;
import org.opengroup.osdu.crs.BinGrid.AbstractSpatialLocation;
import org.opengroup.osdu.crs.BinGrid.Geometry;
import org.opengroup.osdu.crs.model.ScaleConvergence;
import org.opengroup.osdu.crs.BinGrid.PointProperties;
import org.opengroup.osdu.crs.BinGrid.PropertiesBinGridCorners;
import org.opengroup.osdu.crs.converter.CRSConverter;
import org.opengroup.osdu.crs.model.v4.MinimumDepthInterval;
import org.opengroup.osdu.crs.converter.TrajectoryConverter;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryResponseV4;
import org.opengroup.osdu.crs.model.ConvertBinGridRequest;
import org.opengroup.osdu.crs.model.ConvertGeoJsonRequest;
import org.opengroup.osdu.crs.model.ConvertGeoJsonRequestV4;
import org.opengroup.osdu.crs.model.ConvertPointsRequest;
import org.opengroup.osdu.crs.model.ConvertPointsRequestV4;
import org.opengroup.osdu.crs.model.ConvertTrajectoryRequest;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryRequestV4;

import static org.junit.jupiter.api.Assertions.*;

class RequestScalarTypesTest {
    private final ObjectMapper mapper = new JsonMapper();

    @Test
    void boundingBoxRejectsNullItems() throws Exception {
        String prefix = "{\"type\":\"FeatureCollection\",\"features\":[],\"bbox\":";
        for (String bbox : new String[]{"[null]", "[0,null,1,2]"}) {
            assertThrows(MismatchedInputException.class,
                    () -> mapper.readValue(prefix + bbox + "}", GeoJsonFeatureCollection.class));
        }
        assertArrayEquals(new double[]{0, 1, 2, 3}, mapper.readValue(prefix + "[0,1,2,3]}",
                GeoJsonFeatureCollection.class).getBbox());
        assertNull(mapper.readValue(prefix + "null}", GeoJsonFeatureCollection.class).getBbox());
    }

    @Test
    void boundingBoxSchemaAllowsNull() {
        ArraySchema bbox = new ArraySchema().items(new NumberSchema());
        ObjectSchema collection = new ObjectSchema();
        collection.addProperty("bbox", bbox);
        OpenAPI api = new OpenAPI().components(new Components().addSchemas("GeoJsonFeatureCollection", collection));
        org.opengroup.osdu.crs.swagger.SwaggerConfiguration.allowNullBoundingBox(api);
        assertTrue(bbox.getTypes().contains("array"));
        assertTrue(bbox.getTypes().contains("null"));
        assertNotNull(bbox.getItems());
    }

    @Test
    void geoJsonVariantInternalRejectsNull() throws Exception {
        ObjectMapper springLike = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
        String prefix = "{\"type\":\"FeatureCollection\",\"features\":[],\"geoJsonVariantInternal\":";
        assertThrows(DatabindException.class,
                () -> springLike.readValue(prefix + "null}", GeoJsonFeatureCollection.class));
        assertThrows(DatabindException.class,
                () -> springLike.readValue(prefix + "1}", GeoJsonFeatureCollection.class));
        assertEquals("FeatureCollection", springLike.readValue(prefix + "\"GEO_JSON\"}",
                GeoJsonFeatureCollection.class).getType());
        assertEquals("AnyCrsFeatureCollection", springLike.readValue(prefix + "\"ANY_CRS_GEO_JSON\"}",
                GeoJsonFeatureCollection.class).getType());
        assertEquals("FeatureCollection", springLike.readValue(
                "{\"type\":\"FeatureCollection\",\"features\":[]}", GeoJsonFeatureCollection.class).getType());

        String requestPrefix = "{\"toCRS\":\"crs\",\"featureCollection\":" + prefix;
        assertThrows(DatabindException.class,
                () -> springLike.readValue(requestPrefix + "null}}", ConvertGeoJsonRequest.class));
        assertEquals("FeatureCollection", springLike.readValue(requestPrefix + "\"GEO_JSON\"}}",
                ConvertGeoJsonRequest.class).getFeatureCollection().getType());

        io.swagger.v3.oas.models.media.Schema<?> variant = (io.swagger.v3.oas.models.media.Schema<?>)
                io.swagger.v3.core.converter.ModelConverters.getInstance(true)
                        .read(GeoJsonFeatureCollection.class).get("GeoJsonFeatureCollection")
                        .getProperties().get("geoJsonVariantInternal");
        assertTrue(variant.getTypes() == null || !variant.getTypes().contains("null"));
        assertEquals(2, variant.getEnum().size());
        assertTrue(variant.getEnum().contains("GEO_JSON"));
        assertTrue(variant.getEnum().contains("ANY_CRS_GEO_JSON"));
        assertEquals(Boolean.TRUE, variant.getWriteOnly());
    }

    @Test
    void referenceStringsRejectNullAndNonStringsOnEveryVersion() throws Exception {
        ObjectMapper springLike = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
        assertRejectsNullAndNonString(springLike, ConvertPointsRequest.class, "toCRS");
        assertRejectsNullAndNonString(springLike, ConvertPointsRequest.class, "fromCRS");
        assertRejectsNullAndNonString(springLike, ConvertPointsRequestV4.class, "toCRS");
        assertRejectsNullAndNonString(springLike, ConvertPointsRequestV4.class, "transformation");
        assertRejectsNullAndNonString(springLike, ConvertGeoJsonRequest.class, "toCRS");
        assertRejectsNullAndNonString(springLike, ConvertGeoJsonRequestV4.class, "toCRS");
        assertRejectsNullAndNonString(springLike, ConvertGeoJsonRequestV4.class, "transformation");
        assertRejectsNullAndNonString(springLike, ConvertBinGridRequest.class, "toCRS");
        assertRejectsNullAndNonString(springLike, ConvertTrajectoryRequest.class, "unitZ");
        assertRejectsNullAndNonString(springLike, ConvertTrajectoryRequest.class, "trajectoryCRS");
        assertRejectsNullAndNonString(springLike, ConvertTrajectoryRequestV4.class, "unitZ");
        assertRejectsNullAndNonString(springLike, ConvertTrajectoryRequestV4.class, "unitMD");
        assertRejectsNullAndNonString(springLike, ConvertTrajectoryRequestV4.class, "unitXY");
        assertNull(springLike.readValue("{}", ConvertPointsRequestV4.class).getTransformation());
        assertNull(springLike.readValue("{}", ConvertTrajectoryRequestV4.class).getUnitMD());
        assertNull(springLike.readValue("{}", ConvertTrajectoryRequestV4.class).getUnitXY());
        assertEquals("m", springLike.readValue("{\"unitMD\":\"m\"}", ConvertTrajectoryRequestV4.class).getUnitMD());
        io.swagger.v3.oas.models.media.Schema<?> features = (io.swagger.v3.oas.models.media.Schema<?>)
                io.swagger.v3.core.converter.ModelConverters.getInstance(true)
                        .read(AbstractAnyCrsFeatureCollection.class).get("AbstractAnyCrsFeatureCollection")
                        .getProperties().get("features");
        assertEquals(Integer.valueOf(4), features.getMinItems());
        assertEquals(Integer.valueOf(4), features.getMaxItems());
        new TrajectoryConverter().computeUnscaledValuesForXAndY(new ConvertTrajectoryResponseV4());
    }

    private static void assertRejectsNullAndNonString(ObjectMapper mapper, Class<?> type, String field) {
        for (String value : new String[]{"null", "false", "1"}) {
            assertThrows(DatabindException.class,
                    () -> mapper.readValue("{\"" + field + "\":" + value + "}", type),
                    type.getSimpleName() + "." + field + "=" + value);
        }
        assertDoesNotThrow(() -> mapper.readValue("{\"" + field + "\":\"m\"}", type));
    }

    @Test
    void toUnitZRejectsNullButAllowsOmission() throws Exception {
        ObjectMapper springLike = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
        String collection = "{\"type\":\"FeatureCollection\",\"features\":[]}";
        for (Class<?> type : new Class<?>[]{ConvertGeoJsonRequest.class, ConvertGeoJsonRequestV4.class}) {
            assertThrows(DatabindException.class, () -> springLike.readValue(
                    "{\"toCRS\":\"crs\",\"toUnitZ\":null,\"featureCollection\":" + collection + "}", type));
            assertThrows(DatabindException.class, () -> springLike.readValue(
                    "{\"toCRS\":\"crs\",\"toUnitZ\":1,\"featureCollection\":" + collection + "}", type));
            Object omitted = springLike.readValue(
                    "{\"toCRS\":\"crs\",\"featureCollection\":" + collection + "}", type);
            Object provided = springLike.readValue(
                    "{\"toCRS\":\"crs\",\"toUnitZ\":\"m\",\"featureCollection\":" + collection + "}", type);
            assertNull(type.getMethod("getToUnitZ").invoke(omitted));
            assertEquals("m", type.getMethod("getToUnitZ").invoke(provided));
            io.swagger.v3.oas.models.media.Schema<?> property = (io.swagger.v3.oas.models.media.Schema<?>)
                    io.swagger.v3.core.converter.ModelConverters.getInstance(true).read(type)
                            .get(type.getSimpleName()).getProperties().get("toUnitZ");
            assertNotNull(property);
            assertTrue(property.getTypes() == null || !property.getTypes().contains("null"));
        }
    }

    @Test
    void collectionPropertiesRequireObjectOrNull() throws Exception {
        String prefix = "{\"type\":\"FeatureCollection\",\"features\":[],\"properties\":";
        for (String value : new String[]{"[null,null]", "[]", "true", "42", "\"text\""}) {
            assertThrows(MismatchedInputException.class,
                    () -> mapper.readValue(prefix + value + "}", GeoJsonFeatureCollection.class));
        }
        assertNull(mapper.readValue(prefix + "null}", GeoJsonFeatureCollection.class).getProperties());
        String json = prefix + "{\"nested\":[null,{\"value\":3}]}}";
        GeoJsonFeatureCollection collection = mapper.readValue(json, GeoJsonFeatureCollection.class);
        assertEquals(mapper.readTree(json).get("properties"), mapper.readTree(mapper.writeValueAsString(collection)).get("properties"));
        io.swagger.v3.oas.models.media.Schema<?> schema = io.swagger.v3.core.converter.ModelConverters
                .getInstance(true).read(GeoJsonFeatureCollection.class).get("GeoJsonFeatureCollection");
        assertTrue(schema.getProperties().get("properties").getTypes().contains("object"));
        assertTrue(schema.getProperties().get("properties").getTypes().contains("null"));
    }

    @Test
    void optionalDisplacementsRejectNullButKeepOmittedDefaults() throws Exception {
        for (Class<?> type : new Class<?>[]{org.opengroup.osdu.crs.model.TrajectoryStationIn.class,
                org.opengroup.osdu.crs.model.v4.TrajectoryStationInV4.class}) {
            for (String field : new String[]{"dx", "dy", "dz"}) {
                assertThrows(MismatchedInputException.class,
                        () -> mapper.readValue("{\"" + field + "\":null}", type));
                Object omitted = mapper.readValue("{}", type);
                Object provided = mapper.readValue("{\"" + field + "\":0}", type);
                java.lang.reflect.Method getter = type.getMethod("get" + Character.toUpperCase(field.charAt(0)) + field.substring(1));
                assertTrue(Double.isNaN((Double) getter.invoke(omitted)));
                assertEquals(0.0, (Double) getter.invoke(provided));
            }
        }
    }

    @Test
    void interpolateRequiresJsonBooleanInBothVersions() throws Exception {
        for (Class<?> type : new Class<?>[]{ConvertTrajectoryRequest.class, ConvertTrajectoryRequestV4.class}) {
            for (String value : new String[]{"0", "1", "0.0", "\"true\"", "\"false\"", "\"\"", "[]", "{}", "null"}) {
                assertThrows(MismatchedInputException.class,
                        () -> mapper.readValue("{\"interpolate\":" + value + "}", type), type + ": " + value);
            }
            assertNotNull(mapper.readValue("{\"interpolate\":true}", type));
            assertNotNull(mapper.readValue("{\"interpolate\":false}", type));
        }
        assertTrue(mapper.readValue("{}", ConvertTrajectoryRequest.class).isInterpolate());
        assertFalse(mapper.readValue("{\"interpolate\":false}", ConvertTrajectoryRequest.class).isInterpolate());
        assertTrue(mapper.readValue("{}", ConvertTrajectoryRequestV4.class).isInterpolate());
        assertFalse(mapper.readValue("{\"interpolate\":false}", ConvertTrajectoryRequestV4.class).isInterpolate());
    }

    @Test
    void camelCaseGeoJsonIdsRejectNull() throws Exception {
        ObjectMapper springLike = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
        String prefix = "{\"type\":\"FeatureCollection\",\"features\":[],";
        for (String field : new String[]{"verticalUnitID", "coordinateReferenceSystemID"}) {
            assertThrows(DatabindException.class,
                    () -> springLike.readValue(prefix + "\"" + field + "\":null}", GeoJsonFeatureCollection.class),
                    field);
            assertThrows(DatabindException.class,
                    () -> springLike.readValue(prefix + "\"" + field + "\":1}", GeoJsonFeatureCollection.class),
                    field);
            GeoJsonFeatureCollection collection = springLike.readValue(prefix + "\"" + field + "\":\"m\"}",
                    GeoJsonFeatureCollection.class);
            if ("verticalUnitID".equals(field)) {
                assertEquals("m", collection.getVerticalUnitID());
            } else {
                assertEquals("m", collection.getCoordinateReferenceSystemID());
            }
            io.swagger.v3.oas.models.media.Schema<?> property = (io.swagger.v3.oas.models.media.Schema<?>)
                    io.swagger.v3.core.converter.ModelConverters.getInstance(true)
                            .read(GeoJsonFeatureCollection.class).get("GeoJsonFeatureCollection")
                            .getProperties().get(field);
            assertNotNull(property);
            assertTrue(property.getTypes() == null || !property.getTypes().contains("null"));
        }
        assertNull(springLike.readValue(prefix + "\"VerticalUnitID\":null}",
                GeoJsonFeatureCollection.class).getVerticalUnitID());
    }

    @Test
    void geoJsonMetadataRequiresStringOrNull() throws Exception {
        for (String field : new String[]{"persistableReferenceUnitZ", "persistableReferenceCrs",
                "CoordinateReferenceSystemID", "VerticalUnitID"}) {
            String prefix = "{\"type\":\"FeatureCollection\",\"features\":[],\"" + field + "\":";
            for (String value : new String[]{"false", "true", "0", "1.5", "[]", "{}"}) {
                assertThrows(MismatchedInputException.class,
                        () -> mapper.readValue(prefix + value + "}", GeoJsonFeatureCollection.class), field + ": " + value);
            }
            assertNotNull(mapper.readValue(prefix + "null}", GeoJsonFeatureCollection.class));
            assertNotNull(mapper.readValue(prefix + "\"\"}", GeoJsonFeatureCollection.class));
        }
        GeoJsonFeatureCollection collection = mapper.readValue(
                "{\"type\":\"FeatureCollection\",\"features\":[],\"persistableReferenceUnitZ\":\"m\"}",
                GeoJsonFeatureCollection.class);
        assertEquals("m", collection.getPersistableReferenceUnitZ());
    }

    @Test
    void binGridRequiresCornerStructureAndRejectsEmptyFeatures() throws Exception {
        assertRequired(AbstractBinGrid.class, "ABCDBinGridSpatialLocation");
        assertFalse(io.swagger.v3.core.converter.ModelConverters.getInstance(true)
                .read(AbstractBinGrid.class).get("AbstractBinGrid").getProperties()
                .containsKey("abcdbinGridSpatialLocation"));
        assertRequired(AbstractSpatialLocation.class, "AsIngestedCoordinates");
        assertRequired(AbstractAnyCrsFeatureCollection.class, "features");
        assertRequired(AbstractFeature.class, "geometry", "properties");
        assertRequired(PropertiesBinGridCorners.class, "PointProperties");
        assertRequired(PointProperties.class, "Inline", "Crossline");
        io.swagger.v3.oas.models.media.Schema<?> coordinateSchema = (io.swagger.v3.oas.models.media.Schema<?>)
                io.swagger.v3.core.converter.ModelConverters.getInstance(true)
                        .read(Geometry.class).get("Geometry").getProperties().get("coordinates");
        assertEquals(Integer.valueOf(2), coordinateSchema.getMinItems());
        assertEquals(Integer.valueOf(2), coordinateSchema.getMaxItems());
        io.swagger.v3.oas.models.media.Schema<?> pointProperties = (io.swagger.v3.oas.models.media.Schema<?>)
                io.swagger.v3.core.converter.ModelConverters.getInstance(true)
                        .read(PropertiesBinGridCorners.class).get("PropertiesBinGridCorners")
                        .getProperties().get("PointProperties");
        assertEquals(Integer.valueOf(1), pointProperties.getMaxItems());
        ObjectMapper springLike = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
        assertThrows(DatabindException.class, () -> springLike.readValue(
                "{\"type\":\"AnyCrsPoint\",\"coordinates\":[null,0]}", Geometry.class));
        assertEquals(2, springLike.readValue(
                "{\"type\":\"AnyCrsPoint\",\"coordinates\":[0,1]}", Geometry.class).getCoordinates().size());
        assertThrows(DatabindException.class, () -> springLike.readValue(
                "{\"PointProperties\":[null]}", PropertiesBinGridCorners.class));
        assertThrows(DatabindException.class, () -> springLike.readValue(
                "{\"P6BinNodeIncrementOnJaxis\":null}", AbstractBinGrid.class));
        assertNull(springLike.readValue("{}", AbstractBinGrid.class).getP6BinNodeIncrementOnJaxis());
        assertNull(springLike.readValue("{\"abcdbinGridSpatialLocation\":{}}", AbstractBinGrid.class)
                .getABCDBinGridSpatialLocation());
        assertNotNull(springLike.readValue("{\"ABCDBinGridSpatialLocation\":{}}", AbstractBinGrid.class)
                .getABCDBinGridSpatialLocation());
        assertTrue(springLike.writeValueAsString(springLike.readValue(
                "{\"ABCDBinGridSpatialLocation\":{}}", AbstractBinGrid.class)).contains("\"ABCDBinGridSpatialLocation\""));
        assertFalse(springLike.writeValueAsString(springLike.readValue(
                "{\"ABCDBinGridSpatialLocation\":{}}", AbstractBinGrid.class)).contains("abcdbinGridSpatialLocation"));

        AbstractAnyCrsFeatureCollection coordinates = new AbstractAnyCrsFeatureCollection();
        coordinates.setFeatures(java.util.Arrays.asList(
                new AbstractFeature(), new AbstractFeature(), new AbstractFeature(), new AbstractFeature()));
        AbstractSpatialLocation location = new AbstractSpatialLocation();
        location.setAsIngestedcoordinates(coordinates);
        AbstractBinGrid grid = new AbstractBinGrid();
        grid.setABCDBinGridSpatialLocation(location);
        assertThrows(ValidationException.class,
                () -> new CRSConverter().squaring("", grid, new org.opengroup.osdu.crs.model.ConvertBinGridResponse()));
    }

    @Test
    void measuredDepthIntervalRejectsNullAndMutualPresence() throws Exception {
        ObjectMapper springLike = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
        assertThrows(DatabindException.class,
                () -> springLike.readValue("{\"md_interval\":null}", MinimumDepthInterval.class));
        assertThrows(DatabindException.class,
                () -> springLike.readValue("{\"md_interval\":false}", MinimumDepthInterval.class));
        assertThrows(DatabindException.class,
                () -> springLike.readValue("{\"md_i\":null}", MinimumDepthInterval.class));
        assertNull(springLike.readValue("{}", MinimumDepthInterval.class).getMd_interval());
        assertEquals(25.0, springLike.readValue("{\"md_interval\":25}", MinimumDepthInterval.class).getMd_interval());
        for (String name : new String[]{"MD_i", "md_i", "mD_i"}) {
            assertThrows(DatabindException.class, () -> springLike.readValue(
                    "{\"" + name + "\":{\"md_i\":[],\"md_interval\":null}}", ConvertTrajectoryRequestV4.class), name);
        }
        for (String name : new String[]{"MD_i", "md_i", "mD_i"}) {
            assertThrows(DatabindException.class, () -> springLike.readValue(
                    "{\"" + name + "\":null}", ConvertTrajectoryRequestV4.class), name + " null");
        }
        assertEquals(25.0, springLike.readValue("{\"md_i\":{\"md_interval\":25}}",
                ConvertTrajectoryRequestV4.class).getMD_i().getMd_interval());
        io.swagger.v3.oas.models.media.Schema<?> scalePoint = (io.swagger.v3.oas.models.media.Schema<?>)
                io.swagger.v3.core.converter.ModelConverters.getInstance(true)
                        .read(ScaleConvergence.class).get("ScaleConvergence").getProperties().get("point");
        assertNotNull(scalePoint.get$ref());
        assertTrue(scalePoint.get$ref().contains("PointResponse"));
        assertTrue(scalePoint.getTypes() == null || !scalePoint.getTypes().contains("number"));
        io.swagger.v3.oas.models.media.Schema<?> scaleFactor = (io.swagger.v3.oas.models.media.Schema<?>)
                io.swagger.v3.core.converter.ModelConverters.getInstance(true)
                        .read(ScaleConvergence.class).get("ScaleConvergence").getProperties().get("scalefactor");
        assertNotNull(scaleFactor.getOneOf());
        assertTrue(scaleFactor.getTypes().contains("string"));
        OpenAPI constraints = new OpenAPI().components(new Components()
                .addSchemas("ConvertTrajectoryRequest", new ObjectSchema())
                .addSchemas("ConvertTrajectoryRequestV4", new ObjectSchema()));
        SwaggerConfiguration.applyTrajectoryRequestConstraints(constraints);
        java.util.List<io.swagger.v3.oas.models.media.Schema> v2v3Rules =
                constraints.getComponents().getSchemas().get("ConvertTrajectoryRequest").getAllOf();
        assertEquals(2, v2v3Rules.size());
        assertEquals(java.util.Arrays.asList("unitXY"), v2v3Rules.get(1).getThen().getRequired());
        java.util.List<io.swagger.v3.oas.models.media.Schema> v4Rules =
                constraints.getComponents().getSchemas().get("ConvertTrajectoryRequestV4").getAllOf();
        assertEquals(4, v4Rules.size());
        assertEquals(java.util.Arrays.asList("unitXY"), v4Rules.get(3).getThen().getRequired());
        assertEquals(SwaggerConfiguration.PERSISTABLE_CRS_PATTERN,
                ((io.swagger.v3.oas.models.media.StringSchema) v4Rules.get(3).getIf().getProperties().get("trajectoryCRS")).getPattern());
        java.util.List<String> v4Required = io.swagger.v3.core.converter.ModelConverters.getInstance(true)
                .read(ConvertTrajectoryRequestV4.class).get("ConvertTrajectoryRequestV4").getRequired();
        assertTrue(v4Required == null || !v4Required.contains("unitXY"));
        io.swagger.v3.oas.models.media.Schema<?> deviationStations = ((io.swagger.v3.oas.models.media.ArraySchema)
                v4Rules.get(2).getThen().getProperties().get("inputStations")).getItems();
        assertEquals(java.util.Arrays.asList("azimuth", "inclination", "md"),
                deviationStations.getNot().getAnyOf().stream()
                        .map(item -> ((io.swagger.v3.oas.models.media.Schema<?>) item).getRequired().get(0)).toList());
        assertNull(springLike.readValue("{}", ConvertTrajectoryRequestV4.class).getMD_i());
        io.swagger.v3.oas.models.media.Schema<?> inputKind = (io.swagger.v3.oas.models.media.Schema<?>)
                io.swagger.v3.core.converter.ModelConverters.getInstance(true)
                        .read(ConvertTrajectoryRequestV4.class).get("ConvertTrajectoryRequestV4")
                        .getProperties().get("inputKind");
        assertTrue(inputKind.getEnum().contains("MD_Incl_Azim"));
        assertTrue(inputKind.getEnum().contains("dX_dY_dZ"));
        assertTrue(inputKind.getEnum().contains("MD_Incl"));
        assertFalse(inputKind.getEnum().contains("not-a-kind"));
        java.util.Map<String, ?> trajectoryProperties = io.swagger.v3.core.converter.ModelConverters.getInstance(true)
                .read(ConvertTrajectoryRequestV4.class).get("ConvertTrajectoryRequestV4").getProperties();
        assertTrue(trajectoryProperties.containsKey("MD_i"), trajectoryProperties.keySet().toString());
        assertTrue(trajectoryProperties.containsKey("md_i"), trajectoryProperties.keySet().toString());

        io.swagger.v3.oas.models.media.Schema<?> interval = (io.swagger.v3.oas.models.media.Schema<?>)
                io.swagger.v3.core.converter.ModelConverters.getInstance(true)
                        .read(MinimumDepthInterval.class).get("MinimumDepthInterval")
                        .getProperties().get("md_interval");
        assertTrue(interval.getTypes() == null || !interval.getTypes().contains("null"));

        OpenAPI api = new OpenAPI().components(new Components().addSchemas("MinimumDepthInterval", new ObjectSchema()));
        SwaggerConfiguration.forbidBothMeasuredDepthInputs(api);
        assertEquals(java.util.Arrays.asList("md_i", "md_interval"),
                api.getComponents().getSchemas().get("MinimumDepthInterval").getNot().getRequired());
    }

    @Test
    void wgs84FeatureWithoutPropertiesIsRejected() {
        String corner = "{\"geometry\":{\"coordinates\":[0.0,0.0],\"type\":\"Point\"},\"properties\":{\"PointProperties\":[{\"Crossline\":0,\"Inline\":0}]}}";
        String withProperties = "{\"toCRS\":\"\",\"inBinGrid\":{\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"features\":["
                + corner + "," + corner + "," + corner + "," + corner
                + "]},\"Wgs84Coordinates\":{\"type\":\"FeatureCollection\",\"features\":[" + corner + "]}}}}";
        String missingProperties = withProperties.replace(
                "\"Wgs84Coordinates\":{\"type\":\"FeatureCollection\",\"features\":[" + corner + "]}",
                "\"Wgs84Coordinates\":{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[0.0,0.0]}}]}");
        try (jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            jakarta.validation.Validator validator = factory.getValidator();
            assertTrue(validator.validate(mapper.readValue(missingProperties, ConvertBinGridRequest.class)).stream()
                    .anyMatch(violation -> violation.getPropertyPath().toString().contains("properties")));
            String missingGeometryType = withProperties.replace(
                    "\"geometry\":{\"coordinates\":[0.0,0.0],\"type\":\"Point\"}",
                    "\"geometry\":{\"coordinates\":[0.0,0.0]}");
            assertTrue(validator.validate(mapper.readValue(missingGeometryType, ConvertBinGridRequest.class)).stream()
                    .anyMatch(violation -> violation.getPropertyPath().toString().endsWith("type")));
            String missingIngestedGeometryType = withProperties.replaceFirst(
                    "\"geometry\":\\{\"coordinates\":\\[0\\.0,0\\.0\\],\"type\":\"Point\"\\}",
                    "\"geometry\":{\"coordinates\":[0.0,0.0]}");
            assertTrue(validator.validate(mapper.readValue(missingIngestedGeometryType, ConvertBinGridRequest.class)).stream()
                    .anyMatch(violation -> violation.getPropertyPath().toString().contains("asIngestedcoordinates")
                            && violation.getPropertyPath().toString().endsWith("type")));
            assertTrue(validator.validate(mapper.readValue(withProperties, ConvertBinGridRequest.class)).isEmpty());
            String missingPointProperties = withProperties.replace(
                    "\"Wgs84Coordinates\":{\"type\":\"FeatureCollection\",\"features\":[" + corner + "]}",
                    "\"Wgs84Coordinates\":{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"\",\"properties\":{\"Kind\":\"\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0.0,0.0]}}]}");
            assertTrue(validator.validate(mapper.readValue(missingPointProperties, ConvertBinGridRequest.class)).stream()
                    .anyMatch(violation -> violation.getPropertyPath().toString().contains("pointPropertiesList")));
            String missingInline = withProperties.replace(
                    "\"Wgs84Coordinates\":{\"type\":\"FeatureCollection\",\"features\":[" + corner + "]}",
                    "\"Wgs84Coordinates\":{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"\",\"properties\":{\"PointProperties\":[{\"Crossline\":0}]},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0.0,0.0]}}]}");
            assertTrue(validator.validate(mapper.readValue(missingInline, ConvertBinGridRequest.class)).stream()
                    .anyMatch(violation -> violation.getPropertyPath().toString().contains("inline")));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    void wgs84CollectionRejectsNullTypeAndAllowsOmission() throws Exception {
        assertThrows(InvalidNullException.class, () -> mapper.readValue("{\"type\":null,\"features\":[]}", AbstractFeatureCollection.class));
        assertThrows(MismatchedInputException.class, () -> mapper.readValue("{\"type\":0,\"features\":[]}", AbstractFeatureCollection.class));
        assertNull(mapper.readValue("{\"features\":[]}", AbstractFeatureCollection.class).getType());
        assertThrows(InvalidNullException.class, () -> mapper.readValue("{\"type\":\"\",\"features\":null}", AbstractFeatureCollection.class));
        assertNull(mapper.readValue("{\"type\":\"\"}", AbstractFeatureCollection.class).getFeatures());
        assertThrows(InvalidNullException.class, () -> mapper.readValue("{\"Wgs84Coordinates\":null}", AbstractSpatialLocation.class));
        assertNull(mapper.readValue("{}", AbstractSpatialLocation.class).getWgs84Coordinates());
        assertThrows(InvalidNullException.class, () -> mapper.readValue("{\"type\":null,\"features\":[]}", AbstractAnyCrsFeatureCollection.class));
        assertThrows(MismatchedInputException.class, () -> mapper.readValue("{\"type\":0,\"features\":[]}", AbstractAnyCrsFeatureCollection.class));
        assertNull(mapper.readValue("{\"features\":[]}", AbstractAnyCrsFeatureCollection.class).getType());
        assertEquals("", mapper.readValue("{\"type\":\"\",\"features\":[]}", AbstractAnyCrsFeatureCollection.class).getType());
    }

    @Test
    void spatialLocationAndBinGridStringsRejectNull() throws Exception {
        for (String field : new String[]{"SpatialLocationCoordinatesDate", "QuantitativeAccuracyBandID",
                "QualitativeSpatialAccuracyTypeID", "CoordinateQualityCheckPerformedBy",
                "CoordinateQualityCheckDateTime", "CoordinateQualityCheckRemarks", "AppliedOperations",
                "SpatialParameterTypeID", "SpatialGeometryTypeID"}) {
            assertThrows(InvalidNullException.class,
                    () -> mapper.readValue("{\"" + field + "\":null}", AbstractSpatialLocation.class), field);
        }
        assertNull(mapper.readValue("{}", AbstractSpatialLocation.class).getSpatialGeometryTypeID());
        for (String field : new String[]{"BinGridName", "BinGridTypeID", "SourceBinGridAppID",
                "BinGridDefinitionMethodTypeID"}) {
            assertThrows(InvalidNullException.class,
                    () -> mapper.readValue("{\"" + field + "\":null}", AbstractBinGrid.class), field);
        }
        assertThrows(MismatchedInputException.class, () -> mapper.readValue(
                "{\"P6BinNodeIncrementOnJaxis\":2.5890419884777833e-42}", AbstractBinGrid.class));
        assertEquals(1, mapper.readValue("{\"P6BinNodeIncrementOnJaxis\":1.0}", AbstractBinGrid.class)
                .getP6BinNodeIncrementOnJaxis());
    }

    @Test
    void ingestedCoordinatesRejectNullCrsStrings() throws Exception {
        for (String field : new String[]{"CoordinateReferenceSystemID", "coordinateReferenceSystemID", "persistableReferenceCrs"}) {
            assertThrows(InvalidNullException.class, () -> mapper.readValue(
                    "{\"features\":[],\"" + field + "\":null}", AbstractAnyCrsFeatureCollection.class), field);
            assertThrows(MismatchedInputException.class, () -> mapper.readValue(
                    "{\"features\":[],\"" + field + "\":0}", AbstractAnyCrsFeatureCollection.class), field);
        }
        AbstractAnyCrsFeatureCollection omitted = mapper.readValue("{\"features\":[]}", AbstractAnyCrsFeatureCollection.class);
        assertNull(omitted.getCoordinateReferenceSystemID());
        assertNull(omitted.getPersistableReferenceCrs());
        assertEquals("crs", mapper.readValue(
                "{\"features\":[],\"coordinateReferenceSystemID\":\"crs\"}", AbstractAnyCrsFeatureCollection.class)
                .getCoordinateReferenceSystemID());
        assertEquals(java.util.Set.of("CoordinateReferenceSystemID", "coordinateReferenceSystemID", "features",
                        "persistableReferenceCrs", "type"),
                io.swagger.v3.core.converter.ModelConverters.getInstance(true)
                        .read(AbstractAnyCrsFeatureCollection.class).get("AbstractAnyCrsFeatureCollection")
                        .getProperties().keySet());
    }

    @Test
    void binGridFeatureStringsRejectNonStringTokens() {
        String[] payloads = {
                "{\"type\":\"\",\"properties\":{\"PointProperties\":[{\"Inline\":0,\"Crossline\":0}]},\"geometry\":{\"type\":false,\"coordinates\":[0.0,0.0]}}",
                "{\"type\":0,\"properties\":{\"PointProperties\":[{\"Inline\":0,\"Crossline\":0}]},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0.0,0.0]}}",
                "{\"type\":\"\",\"properties\":{\"Kind\":true,\"PointProperties\":[{\"Inline\":0,\"Crossline\":0}]},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0.0,0.0]}}",
                "{\"type\":\"\",\"properties\":{\"PointProperties\":[{\"Label\":1,\"Inline\":0,\"Crossline\":0}]},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0.0,0.0]}}"
        };
        for (String payload : payloads) {
            assertThrows(MismatchedInputException.class, () -> mapper.readValue(payload, AbstractFeature.class), payload);
        }
    }

    @Test
    void pointPropertiesRejectFractionalInlineAndCrossline() throws Exception {
        assertThrows(MismatchedInputException.class, () -> mapper.readValue(
                "{\"Inline\":0,\"Crossline\":2.5890419884777833e-42}", PointProperties.class));
        assertThrows(MismatchedInputException.class, () -> mapper.readValue(
                "{\"Inline\":1.5,\"Crossline\":0}", PointProperties.class));
        PointProperties whole = mapper.readValue("{\"Inline\":1.0,\"Crossline\":0}", PointProperties.class);
        assertEquals(1, whole.getInline());
        assertEquals(0, whole.getCrossline());
    }

    @Test
    void binGridFeatureStringsRejectNullAndAllowOmission() throws Exception {
        assertThrows(InvalidNullException.class, () -> mapper.readValue(
                "{\"Label\":null,\"Inline\":0,\"Crossline\":0}", PointProperties.class));
        assertNull(mapper.readValue("{\"Inline\":0,\"Crossline\":0}", PointProperties.class).getLabel());
        assertThrows(InvalidNullException.class, () -> mapper.readValue(
                "{\"Kind\":null,\"PointProperties\":[{\"Inline\":0,\"Crossline\":0}]}", PropertiesBinGridCorners.class));
        assertNull(mapper.readValue(
                "{\"PointProperties\":[{\"Inline\":0,\"Crossline\":0}]}", PropertiesBinGridCorners.class).getKind());
        String properties = "\"properties\":{\"PointProperties\":[{\"Inline\":0,\"Crossline\":0}]},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0.0,0.0]}";
        assertThrows(InvalidNullException.class, () -> mapper.readValue(
                "{\"type\":null," + properties + "}", AbstractFeature.class));
        assertNull(mapper.readValue("{" + properties + "}", AbstractFeature.class).getType());
    }

    private static void assertRequired(Class<?> type, String... names) {
        io.swagger.v3.oas.models.media.Schema<?> schema = io.swagger.v3.core.converter.ModelConverters.getInstance(true)
                .read(type).get(type.getSimpleName());
        assertNotNull(schema.getRequired(), type.getSimpleName());
        for (String name : names) {
            assertTrue(schema.getRequired().contains(name), type.getSimpleName() + " requires " + name);
        }
    }
}
