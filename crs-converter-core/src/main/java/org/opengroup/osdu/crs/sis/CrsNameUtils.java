package org.opengroup.osdu.crs.sis;

public class CrsNameUtils {

    public static String findSourceCrsNameFromTransformWkt(String wkt) {
        int searchIndex = wkt.indexOf("GEOGCS");
        int firstQuoteIndex = wkt.indexOf("\"", searchIndex);
        int secondQuoteIndex = wkt.indexOf("\"", firstQuoteIndex + 1);
        if (firstQuoteIndex > -1 && secondQuoteIndex > -1) {
            return wkt.substring(firstQuoteIndex + 1, secondQuoteIndex);
        }
        return null;
    }

    public static String findDestinationCrsNameFromTransformWkt(String wkt) {
        int firstLocationIndex = wkt.indexOf("GEOGCS");
        int searchIndex = wkt.indexOf("GEOGCS", firstLocationIndex + 1);
        int firstQuoteIndex = wkt.indexOf("\"", searchIndex);
        int secondQuoteIndex = wkt.indexOf("\"", firstQuoteIndex + 1);
        if (firstQuoteIndex > -1 && secondQuoteIndex > -1) {
            return wkt.substring(firstQuoteIndex + 1, secondQuoteIndex);
        }
        return null;
    }


    public static String findCrsName(String wkt, String defaultValue) {
        //try to find the crs name from the wkt first, the names specified by CoordinateReferenceSystem
        //may not be ban exact match to what is listed in the wkt
        String crsName = getCrsNameFromWKT(wkt);
        if (crsName != null) {
            return crsName;
        }

        return defaultValue;
    }

    public static String getCrsNameFromWKT(String wkt) {
        int searchIndex = 0;
        if (wkt == null) {
            return null;
        }
        if (!wkt.startsWith("PROJCS")) {
            searchIndex = wkt.indexOf("GEOGCS");
        }
        int firstQuoteIndex = wkt.indexOf("\"", searchIndex);
        int secondQuoteIndex = wkt.indexOf("\"", firstQuoteIndex + 1);
        if (firstQuoteIndex > -1 && secondQuoteIndex > -1) {
            return wkt.substring(firstQuoteIndex + 1, secondQuoteIndex);
        }
        return null;
    }

    public static String findBaseCrsName(String projectionWKT, String baseWKT, String defaultValue) {
        String name = findBaseCrsNameFromProjectionWkt(projectionWKT);
        if (name != null) {
            return name;
        }
        return findCrsName(baseWKT, defaultValue);
    }

    public static String findBaseCrsNameFromProjectionWkt(String wkt) {
        if (wkt == null) {
            return null;
        }
        if (!wkt.startsWith("PROJCS")) {
            return null;
        }
        int searchIndex = wkt.indexOf("GEOGCS");
        int firstQuoteIndex = wkt.indexOf("\"", searchIndex);
        int secondQuoteIndex = wkt.indexOf("\"", firstQuoteIndex + 1);
        if (firstQuoteIndex > -1 && secondQuoteIndex > -1) {
            return wkt.substring(firstQuoteIndex + 1, secondQuoteIndex);
        }
        return null;
    }
}
