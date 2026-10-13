package org.opengroup.osdu.crs.sis.wkt;

import org.opengroup.osdu.crs.sis.SisParameterDefs;

public class WktParametersFactory {

    public enum WktParameterName {
        FALSE_EASTING("False_Easting", SisParameterDefs.PE_PAR_FALSE_EASTING),
        FALSE_NORTHING("False_Northing", SisParameterDefs.PE_PAR_FALSE_NORTHING),
        CENTRAL_MERIDIAN("Central_Meridian", SisParameterDefs.PE_PAR_CENTRAL_MERIDIAN),
        LATITUDE_OF_ORIGIN("Latitude_Of_Origin", SisParameterDefs.PE_PAR_LATITUDE_OF_ORIGIN);

        private final String value;
        private final int code;

        WktParameterName(final String newValue, final int newCode) {
            value = newValue;
            code = newCode;
        }

        public String getStringValue() {
            return value;
        }

        @Override
        public String toString() {
            return getStringValue();
        }

        public static WktParameterName decodeFromParameterCode(int code) {
            if (code == FALSE_EASTING.code) {
                return FALSE_EASTING;
            }
            if (code == FALSE_NORTHING.code) {
                return FALSE_NORTHING;
            }
            if (code == CENTRAL_MERIDIAN.code) {
                return CENTRAL_MERIDIAN;
            }
            if (code == LATITUDE_OF_ORIGIN.code) {
                return LATITUDE_OF_ORIGIN;
            }
            throw new IllegalArgumentException("Unsupported code: " + code);
        }

    }

    public static WktParameters parameter(int code) {
        WktParameterName parameterName = WktParameterName.decodeFromParameterCode(code);
        return new WktParameters(parameterName.getStringValue());
    }
}
