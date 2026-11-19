package org.opengroup.osdu.crs.util;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The horizontal unit of a projected CRS, read from the last linear UNIT clause of its WKT.
 */
public final class ProjectedAxisUnit {

    private static final Pattern UNIT = Pattern.compile("UNIT\\[\"([^\"]+)\",\\s*([-+0-9.eE]+)\\]");

    private ProjectedAxisUnit() {
    }

    public static String fromPersistableReference(String persistableReference) {
        if (persistableReference == null || persistableReference.isBlank()) {
            return null;
        }
        String wkt = wkt(persistableReference);
        Matcher matcher = UNIT.matcher(wkt);
        String name = null;
        String scale = null;
        while (matcher.find()) {
            name = matcher.group(1);
            scale = matcher.group(2);
        }
        if (name == null || name.toLowerCase(Locale.ROOT).startsWith("deg")) {
            return null;
        }
        String symbol = name.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"scaleOffset\":{\"scale\":" + scale + ",\"offset\":0.0},\"symbol\":\"" + symbol
                + "\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}";
    }

    private static String wkt(String persistableReference) {
        try {
            JsonNode node = JsonMapper.shared().readTree(persistableReference);
            JsonNode wkt = node.get("wkt");
            if (wkt != null && wkt.isTextual()) {
                return wkt.asText();
            }
        } catch (Exception ignored) {
            // A bare WKT string is matched directly.
        }
        return persistableReference;
    }
}
