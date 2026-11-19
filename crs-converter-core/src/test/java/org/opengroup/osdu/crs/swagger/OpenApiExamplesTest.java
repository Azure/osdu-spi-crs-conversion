package org.opengroup.osdu.crs.swagger;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Test;
import org.opengroup.osdu.crs.GeoJson.GeoJsonBase;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.api.HealthCheck;
import org.opengroup.osdu.crs.model.ReferenceConverter;
import org.opengroup.osdu.crs.util.Constants;

import static org.junit.Assert.*;

public class OpenApiExamplesTest {
    @Test
    public void crsExamplesDeserializeAsPersistableReferences() throws Exception {
        for (String reference : new String[]{Constants.SWAGGER_SOURCE_CRS_EXAMPLE,
                Constants.SWAGGER_CONVERT_TARGET_CRS_EXAMPLE, Constants.SWAGGER_TARGET_CRS_EX,
                Constants.SWAGGER_TARGET_CRS_GEO_EXAMPLE, Constants.SWAGGER_TRANSFORM_EXAMPLE}) {
            assertTrue(new JsonMapper().readTree(reference).isObject());
            org.opengroup.osdu.crs.model.v2.PersistableReference parsed =
                    org.opengroup.osdu.crs.model.v2.PersistableReference.createInstance(reference);
            assertNotNull(parsed);
            if (parsed instanceof org.opengroup.osdu.crs.model.v2.EarlyBoundCrs) {
                // Full transformation validation requires the external EPSG operation database.
                org.opengroup.osdu.crs.model.v2.EarlyBoundCrs bound =
                        (org.opengroup.osdu.crs.model.v2.EarlyBoundCrs) parsed;
                assertNotNull(bound.getSingleTransformation());
                assertNotNull(bound.getLateBoundCrs());
                assertNotNull(org.apache.sis.referencing.CRS.fromWKT(bound.getLateBoundCrs().getLateBoundCrsWkt()));
            } else if (parsed instanceof org.opengroup.osdu.crs.model.v2.LateBoundCrs) {
                assertTrue(reference, ReferenceConverter.parseSpatialReference(reference).isValid());
            }

        }
    }

    @Test
    public void unitExamplesAreValidLengthUnits() {
        for (String reference : new String[]{Constants.SWAGGER_TARGET_Z_UNIT_EXAMPLE,
                Constants.SWAGGER_TRJ_REQ_UNIT_XY_EXAMPLE, Constants.SWAGGER_TRJ_REQ_UNIT_Z_EXAMPLE,
                Constants.SWAGGER_TRJ_REQ_UNIT_MD_EXAMPLE}) {
            assertTrue(ReferenceConverter.parseUnitReference(reference).isValid());
            assertTrue(ReferenceConverter.parseUnitReference(reference).isLength());
        }
    }

    @Test
    public void featureCollectionSchemaOnlyAllowsDeserializableTypes() {
        Schema schema = ModelConverters.getInstance().read(GeoJsonFeatureCollection.class)
                .get("GeoJsonFeatureCollection");
        Schema type = (Schema) schema.getProperties().get("type");
        assertEquals(2, type.getEnum().size());
        for (Object value : type.getEnum()) {
            assertNotNull(GeoJsonBase.createInstance("{\"type\":\"" + value + "\",\"features\":[]}"));
        }
        assertEquals("FeatureCollection", type.getExample());
    }

    @Test
    public void healthResponsesDoNotAdvertiseABody() throws Exception {
        for (String method : new String[]{"livenessCheck", "readinessCheck"}) {
            io.swagger.v3.oas.annotations.responses.ApiResponses responses = HealthCheck.class
                    .getMethod(method).getAnnotation(io.swagger.v3.oas.annotations.responses.ApiResponses.class);
            assertEquals(Void.class, responses.value()[0].content()[0].schema().implementation());
            assertNull(new HealthCheck().livenessCheck().getBody());
        }
    }
}
