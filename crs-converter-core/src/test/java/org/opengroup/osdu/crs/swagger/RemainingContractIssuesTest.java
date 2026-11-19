package org.opengroup.osdu.crs.swagger;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.Test;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.converter.CRSConverter;
import org.opengroup.osdu.crs.model.ConvertPointsRequest;
import org.opengroup.osdu.crs.model.ConvertTrajectoryRequest;
import org.opengroup.osdu.crs.model.v4.ConvertTrajectoryRequestV4;

import java.util.Collections;

import static org.junit.Assert.*;

public class RemainingContractIssuesTest {
    @Test(expected = IllegalArgumentException.class)
    public void nullGeoJsonFeatureIsBadInput() {
        GeoJsonFeatureCollection collection = new GeoJsonFeatureCollection();
        collection.setFeatures(new org.opengroup.osdu.crs.GeoJson.GeoJsonFeature[]{null});
        assertFalse(collection.isValid());
        new CRSConverter().convertGeoJson(collection, "0", "");
    }

    @Test
    public void nullStationsAreRejectedByRequestValidation() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            ConvertTrajectoryRequest request = new ConvertTrajectoryRequest();
            request.setInputStations(Collections.singletonList(null));
            assertFalse(factory.getValidator().validateProperty(request, "inputStations").isEmpty());
            ConvertTrajectoryRequestV4 requestV4 = new ConvertTrajectoryRequestV4();
            requestV4.setInputStations(Collections.singletonList(null));
            assertFalse(factory.getValidator().validateProperty(requestV4, "inputStations").isEmpty());
        }
    }

    @Test
    public void referenceSchemasUseSemanticFormatsWithoutGenerationRegexes() {
        Schema<?> request = ModelConverters.getInstance().read(ConvertPointsRequest.class).get("ConvertPointsRequest");
        for (String name : new String[]{"fromCRS", "toCRS"}) {
            Schema<?> property = request.getProperties().get(name);
            assertEquals("osdu-crs-reference", property.getFormat());
            assertNull(property.getPattern());
        }
    }
}
