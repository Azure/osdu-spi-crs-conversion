package org.opengroup.osdu.crs.GeoJson;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import org.opengroup.osdu.crs.GeoJson.GeoJsonBase.GeoJsonVariant;

/** Accepts only the string enum values published for geoJsonVariantInternal. */
public class GeoJsonVariantDeserializer extends ValueDeserializer<GeoJsonVariant> {
    @Override
    public GeoJsonVariant deserialize(JsonParser parser, DeserializationContext context) {
        if (!parser.hasToken(JsonToken.VALUE_STRING)) {
            return (GeoJsonVariant) context.handleUnexpectedToken(GeoJsonVariant.class, parser);
        }
        String text = parser.getString();
        try {
            return GeoJsonVariant.valueOf(text);
        } catch (IllegalArgumentException ex) {
            return (GeoJsonVariant) context.handleWeirdStringValue(GeoJsonVariant.class, text,
                    "not one of the values accepted for geoJsonVariantInternal");
        }
    }

    @Override
    public GeoJsonVariant getNullValue(DeserializationContext context) {
        return context.reportInputMismatch(GeoJsonVariant.class, "geoJsonVariantInternal cannot be null");
    }
}
