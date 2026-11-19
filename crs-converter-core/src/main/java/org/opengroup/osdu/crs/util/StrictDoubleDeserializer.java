package org.opengroup.osdu.crs.util;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/** Preserves the JSON number type required by the request contract. */
public class StrictDoubleDeserializer extends ValueDeserializer<Double> {
    @Override
    public Double deserialize(JsonParser parser, DeserializationContext context) {
        if (parser.hasToken(JsonToken.VALUE_NUMBER_INT) || parser.hasToken(JsonToken.VALUE_NUMBER_FLOAT)) {
            return parser.getDoubleValue();
        }
        return (Double) context.handleUnexpectedToken(Double.class, parser);
    }
}
