package org.opengroup.osdu.crs.util;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/** Preserves the JSON integer type required by the request contract. */
public class StrictIntegerDeserializer extends ValueDeserializer<Integer> {
    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) {
        if (parser.hasToken(JsonToken.VALUE_NUMBER_INT) || parser.hasToken(JsonToken.VALUE_NUMBER_FLOAT)) {
            try {
                return parser.getDecimalValue().intValueExact();
            } catch (ArithmeticException ex) {
                return (Integer) context.handleUnexpectedToken(Integer.class, parser);
            }
        }
        return (Integer) context.handleUnexpectedToken(Integer.class, parser);
    }
}
