package org.opengroup.osdu.crs.util;

public class Config {

    private static final String METHOD = "api/trajectory/v1/compute";
	private static boolean trajectoryFeatureFlag = true;
	private Config() {
		// private constructor
    }
    
    public static String getEntitlementsHostUrl() {
        return getEnvironmentVariable("ENTITLEMENT_URL");
    }

    private static String getEnvironmentVariable(String propertyKey) {
        String property = System.getProperty(propertyKey, System.getenv(propertyKey));
        if ((property == null) || (property.isEmpty()) ) {
            return null;
        }
        return property;
    }

    public static String getStorageURL() {
        String property = getEnvironmentVariable("STORAGE_URL");
        if ((property == null) || (property.isEmpty()) ) {
            return "NOT_FOUND";
        }
        return property;
    }

    static String getTrajectoryMethod() {
	    return METHOD;
    }

    private static Boolean isPermittedEnvironment(){
        return true;
    }

    public static Boolean useDpsTrajectoryInUnitTests() {
        return getEnvironmentVariable("TEST_HTTP_CLIENT") != null;
    }

	public static void setDpsTrajectoryFeature(boolean on) {
		trajectoryFeatureFlag = on;
	}

    /**
     * Checks whether a string is null or empty
     * @param value string value to be checked.
     * @return true if the string is null or empty;
     * otherwise, returns false;
     */
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

}
