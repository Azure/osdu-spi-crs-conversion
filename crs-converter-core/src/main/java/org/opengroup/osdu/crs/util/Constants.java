package org.opengroup.osdu.crs.util;

public final class Constants {
    public static final String WGS84 = "{\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4326]]\",\"ver\":\"PE_10_3_1\",\"name\":\"GCS_WGS_1984\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"type\":\"LBC\"}";

    public static final String BASE_PATH = "/";
    public static final String SWAGGER_TAG_CRS_CONVERSION = "CRS Point Conversion";
    public static final String SWAGGER_TAG_TRJ_CONVERSION = "Trajectory Computation and Conversion";
    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final String GCLOUD_PROJECT = "GCLOUD_PROJECT";
    public static final String SWAGGER_TARGET_CRS_GEO_EXAMPLE = "\"{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"GCS_WGS_1984\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4326]]\"}\"";

    public static final String SWAGGER_TRANSFORMATION_EXAMPLE = "osdu:reference-data--CoordinateTransformation:EPSG::15851:";

    public static final String EMPTY_OBJECT_EXAMPLE = "{}";

    // Exception messages
    static final String ERROR_MSG_FIELD = "error";
    public static final String ERROR_MSG_NO_SUITABLE_CONVERSION = "Could not find a conversion method for the given input.";
    public static final String ERROR_MSG_INVALID_INPUT_CRS_SPECIFICATION = "Invalid source and/or target CRS specification.";
    public static final String ERROR_MSG_INVALID_INPUT_NO_PROJECTED_CRS = "Invalid CRS specification; only projected CRSs are supported.";
    public static final String ERROR_MSG_INCOHERENT_BOUND_TRFS = "Incoherent coordinate transformations; no hub CRS could be identified.";
    public static final String ERROR_MSG_INPUT_ARRAY_MISMATCH = "Internal error; inconsistent array lengths";
    public static final String ERROR_MSG_BAD_INPUT = "Bad input";
    public static final String ERROR_MSG_JSON_PARSE = "Error parsing spatial model; details: ";
    public static final String ERROR_MSG_SPATIAL_ENGINE_ERROR = "Spatial engine error; details: ";
    public static final String ERROR_MSG_UNIT_ERROR = "Unsupported or inconsistent unit.";
    public static final String ERROR_MSG_INVALID_INPUT_TRANSFORM_SPECIFICATION = "Invalid transform specification";
    public static final String ERROR_MSG_INVALID_TRANSFORM_CRS_MATCH = "Invalid transform specification; transform is incompatible with source and target CRS";


    // Swagger
    public static final String JSON_TYPE = "application/json";
    public static final String SWAGGER_TITLE = "CRS Conversion Service";
    public static final String SWAGGER_DESCRIPTION = "Provides conversion/transformation services from a source to a target CRS for points and trajectories. A coordinate operation is a conversion if the source and target CRS share the same datum; the operation is called transformation if datum transformations are involved, i.e. source and target CRS do not share the same datum.";
    public static final String SWAGGER_VERSION = "1.0.0";
    public static final String SWAGGER_BASE_PATH = "/api/crs/v1";
    public static final String SWAGGER_RESOURCE_PACKAGE = "org.opengroup.osdu.crs.api";

    // Swagger - Convert API
    public static final String SWAGGER_CONVERT_TITLE = "Convert a list of points";
    public static final String SWAGGER_CONVERT_NOTES = "Convert a list of points";
    public static final String SWAGGER_CONVERT_SUCCESS_RESPONSE = "Conversion performed successfully";
    public static final String SWAGGER_CONVERT_BAD_INPUT_BASE_PATH = "Bad input format";
    public static final String SWAGGER_CONVERT_OVERLOAD = "CRS-converter overloaded; try again later";
    public static final String SWAGGER_CONVERT_UNKNOWN_ERROR = "Unknown error.";

    public static final String SWAGGER_SOURCE_CRS = "Source CRS as persistable reference string";
    public static final String SWAGGER_SOURCE_CRS_EXAMPLE = "\"{\"lateBoundCRS\":{\"wkt\":\"GEOGCS[\\\"GCS_Provisional_S_American_1956\\\",DATUM[\\\"D_Provisional_S_American_1956\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4248]]\",\"ver\":\"PE_10_3_1\",\"name\":\"GCS_Provisional_S_American_1956\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4248\"},\"type\":\"LBC\"},\"singleCT\":{\"wkt\":\"GEOGTRAN[\\\"PSAD_1956_To_WGS_1984_9\\\",GEOGCS[\\\"GCS_Provisional_S_American_1956\\\",DATUM[\\\"D_Provisional_S_American_1956\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-295.0],PARAMETER[\\\"Y_Axis_Translation\\\",173.0],PARAMETER[\\\"Z_Axis_Translation\\\",-371.0],AUTHORITY[\\\"EPSG\\\",1209]]\",\"ver\":\"PE_10_3_1\",\"name\":\"PSAD_1956_To_WGS_1984_9\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1209\"},\"type\":\"ST\"},\"ver\":\"PE_10_3_1\",\"name\":\"PSAD56 * DMA-Ven [4248,1209]\",\"authCode\":{\"auth\":\"SLB\",\"code\":\"4248009\"},\"type\":\"EBC\"}\"";
    public static final String SWAGGER_TARGET_CRS = "Target CRS as persistable reference string";

    public static final String SWAGGER_TRANSFORMATION = "Explicit Transformation as persistable reference string or record id, its optional and if given it will override Bound Transformation ";
    public static final String SWAGGER_CONVERT_TARGET_CRS_EXAMPLE = "\"{\"lateBoundCRS\":{\"wkt\":\"PROJCS[\\\"Trinidad_1903_Trinidad_Grid\\\",GEOGCS[\\\"GCS_Trinidad_1903\\\",DATUM[\\\"D_Trinidad_1903\\\",SPHEROID[\\\"Clarke_1858\\\",6378293.64520876,294.260676369]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Cassini\\\"],PARAMETER[\\\"False_Easting\\\",430000.0],PARAMETER[\\\"False_Northing\\\",325000.0],PARAMETER[\\\"Central_Meridian\\\",-61.3333333333333],PARAMETER[\\\"Scale_Factor\\\",1.0],PARAMETER[\\\"Latitude_Of_Origin\\\",10.4416666666667],UNIT[\\\"Link_Clarke\\\",0.201166195164],AUTHORITY[\\\"EPSG\\\",30200]]\",\"ver\":\"PE_10_3_1\",\"name\":\"Trinidad_1903_Trinidad_Grid\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"30200\"},\"type\":\"LBC\"},\"singleCT\":{\"wkt\":\"GEOGTRAN[\\\"Trinidad_1903_To_WGS_1984_2\\\",GEOGCS[\\\"GCS_Trinidad_1903\\\",DATUM[\\\"D_Trinidad_1903\\\",SPHEROID[\\\"Clarke_1858\\\",6378293.64520876,294.260676369]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-61.0],PARAMETER[\\\"Y_Axis_Translation\\\",285.2],PARAMETER[\\\"Z_Axis_Translation\\\",471.6],AUTHORITY[\\\"EPSG\\\",10085]]\",\"ver\":\"PE_10_3_1\",\"name\":\"Trinidad_1903_To_WGS_1984_2\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"10085\"},\"type\":\"ST\"},\"ver\":\"PE_10_3_1\",\"name\":\"Trinidad 1903 * EOG-Tto Trin / Trinidad Grid [30200,10085]\",\"authCode\":{\"auth\":\"SLB\",\"code\":\"30200002\"},\"type\":\"EBC\"}\"";
    public static final String SWAGGER_TARGET_CRS_EX = "\"{\"wkt\":\"PROJCS[\\\"WGS_1984_UTM_Zone_31N\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",3.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",32631]]\",\"ver\":\"PE_10_3_1\",\"name\":\"WGS_1984_UTM_Zone_31N\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32631\"},\"type\":\"LBC\"}\"";
    public static final String SWAGGER_TARGET_CRS_EXAMPLE = "{\"authCode\":{\"auth\":\"SLB\",\"code\":\"30200002\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"30200\"},\"name\":\"Trinidad_1903_Trinidad_Grid\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"PROJCS[\\\"Trinidad_1903_Trinidad_Grid\\\",GEOGCS[\\\"GCS_Trinidad_1903\\\",DATUM[\\\"D_Trinidad_1903\\\",SPHEROID[\\\"Clarke_1858\\\",6378293.64520876,294.260676369]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Cassini\\\"],PARAMETER[\\\"False_Easting\\\",430000.0],PARAMETER[\\\"False_Northing\\\",325000.0],PARAMETER[\\\"Central_Meridian\\\",-61.3333333333333],PARAMETER[\\\"Scale_Factor\\\",1.0],PARAMETER[\\\"Latitude_Of_Origin\\\",10.4416666666667],UNIT[\\\"Link_Clarke\\\",0.201166195164],AUTHORITY[\\\"EPSG\\\",30200]]\"},\"name\":\"Trinidad 1903 * EOG-Tto Trin / Trinidad Grid [30200,10085]\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"10085\"},\"name\":\"Trinidad_1903_To_WGS_1984_2\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"Trinidad_1903_To_WGS_1984_2\\\",GEOGCS[\\\"GCS_Trinidad_1903\\\",DATUM[\\\"D_Trinidad_1903\\\",SPHEROID[\\\"Clarke_1858\\\",6378293.64520876,294.260676369]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-61.0],PARAMETER[\\\"Y_Axis_Translation\\\",285.2],PARAMETER[\\\"Z_Axis_Translation\\\",471.6],AUTHORITY[\\\"EPSG\\\",10085]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\"}";
    public static final String SWAGGER_NOF_POINTS_CONVERTED = "Number of points successfully converted. If the number is less than the request array length conversion/transformation failures occurred.";
    public static final String SWAGGER_CONVERTED_POINTS = "Converted points; length and order of the array is the same as in the request. Points, which failed to convert, are returned as NaN.";
    public static final String SWAGGER_CONVERSION_RESPONSE = "Response of a CRS conversion/transformation operation";
    public static final String SWAGGER_CONVERT_AUDIT = "The list of operations performed on the points as a list of strings";
    public static final String SWAGGER_TARGET_Z_UNIT = "Optional: the target Z-unit for the z-axis scaling.";
    public static final String SWAGGER_TARGET_Z_UNIT_EXAMPLE = "\"{\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"scaleOffset\":{\"offset\":0.0,\"scale\":1.0},\"symbol\":\"m\",\"type\":\"USO\"}\"";

    public static final String SWAGGER_GEO_JSON_CONVERT_TITLE = "Convert a GeoJSON or AnyCrsGeoJson structure";
    public static final String SWAGGER_GEO_JSON_CONVERT_NOTES = "Convert a GeoJSON or AnyCrsGeoJson structure";
    public static final String SWAGGER_GEO_JSON_CONVERT_REQUEST_DESCR = "Request to convert a GeoJSON FeatureCollection or AnyCrsFeatureCollection from WGS 84 or 'AnyCrsFeatureCollection.persistableReferenceCrs to a target CRS.' to a target CRS";
    public static final String SWAGGER_GEO_JSON_FEATURE_COLLECTION = "The GeoJSON FeatureCollection or AnyCrsFeatureCollection structure to be converted/transformed. GeoJSON is always based on WGS 84; AnyCrsFeatureCollection carries the CRS context in the persistableReferenceCrs property. GeoJSON WGS 84 or the persistableReferenceCrs are taken as the 'fromCRS'.";
    public static final String SWAGGER_GEO_JSON_FEATURE_COLLECTION_EXAMPLE = "{\"features\":[{\"geometry\":{\"coordinates\":[5.0,59.0,-1000.0],\"type\":\"Point\"},\"properties\":{},\"type\":\"Feature\"}],\"properties\":{},\"type\":\"FeatureCollection\"}";
    public static final String SWAGGER_GEO_JSON_CONVERSION_RESPONSE = "Response of a CRS conversion/transformation operation involving GeoJSON FeatureCollection or AnyCrsFeatureCollection.";
    public static final String SWAGGER_GEO_JSON_SUCCESS_COUNT = "The number of coordinates in the GeoJSON FeatureCollection or AnyCrsFeatureCollection successfully converted/transformed. If this number is less than totalCount then conversion/transformation errors have occurred.";
    public static final String SWAGGER_GEO_JSON_COORDINATE_COUNT = "The total number of coordinates in the GeoJSON FeatureCollection or AnyCrsFeatureCollection.";
    public static final String SWAGGER_GEO_JSON_CONVERTED = "The converted GeoJSON FeatureCollection or AnyCrsFeatureCollection with 'toCRS' context; length and order of the structure is the same as in the request. Points, which failed to convert, are returned as NaN.";

    public static final String SWAGGER_X_COORDINATE = "X coordinate value. For geographic CRS: longitude in degrees (-180 to +180). For projected CRS: easting in CRS units (typically meters).";
    public static final String SWAGGER_Y_COORDINATE = "Y coordinate value. For geographic CRS: latitude in degrees (-90 to +90). For projected CRS: northing in CRS units (typically meters).";
    public static final String SWAGGER_Z_COORDINATE = "Z coordinate value representing elevation or depth. Unit specified by unitZ. Positive typically indicates height above reference surface.";
    public static final String SWAGGER_X_COORDINATE_EXAMPLE = "-61.043406288714543";
    public static final String SWAGGER_Y_COORDINATE_EXAMPLE = "10.673103179456877";
    public static final String SWAGGER_Z_COORDINATE_EXAMPLE = "0.0";
    public static final String SWAGGER_POINT_DESCR = "A 3D coordinate point. Interpretation of x, y, z depends on the Coordinate Reference System (CRS). For geographic CRS: x=longitude, y=latitude. For projected CRS: x=easting, y=northing.";
    public static final String SWAGGER_CONVERT_REQUEST_DESCR = "Request to convert a set of points from a source CRS to a target CRS";
    public static final String SWAGGER_LIST_OF_POINTS_TO_BE_CONVERTED = "List of points to be converted";
    public static final String SWAGGER_LIST_OF_POINTS_TO_BE_CONVERTED_EXAMPLE = "[\n" +
            "    {\n" +
            "      \"x\": -61.04340628871454,\n" +
            "      \"y\": 10.673103179456877,\n" +
            "      \"z\": 0\n" +
            "    }\n" +
            "  ]";
    public static final String SWAGGER_TRANSFORM = "CRS Transform";
    public static final String SWAGGER_TRANSFORM_EXAMPLE = "\"{\"wkt\":\"GEOGTRAN[\\\"PSAD_1956_To_WGS_1984_9\\\",GEOGCS[\\\"GCS_Provisional_S_American_1956\\\",DATUM[\\\"D_Provisional_S_American_1956\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-295.0],PARAMETER[\\\"Y_Axis_Translation\\\",173.0],PARAMETER[\\\"Z_Axis_Translation\\\",-371.0],AUTHORITY[\\\"EPSG\\\",1209]]\",\"ver\":\"PE_10_3_1\",\"name\":\"PSAD_1956_To_WGS_1984_9\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1209\"},\"type\":\"ST\"}\"";


    public static final String SWAGGER_TRJ_CONVERT_TITLE = "Convert trajectory stations";
    public static final String SWAGGER_TRJ_CONVERT_NOTES = "Convert a list of trajectory stations, given the unit and spatial context and a reference point in 3D where MD==0.";
    public static final String SWAGGER_TRJ_CONVERT_SUCCESS_RESPONSE = "Trajectory conversion performed successfully";
    public static final String SWAGGER_TRJ_REQ_DESCRIPTION = "Input trajectory data structure; contains the context (crs, units, azimuth reference, method)";
    public static final String SWAGGER_TRJ_REQ_CRS = "Coordinate reference system for the reference point; typically the CRS is a projected CRS; if a geographic CRS is provided, the unitXY must be defined and the azimuthReference must be TRUE_NORTH.";
    public static final String SWAGGER_TRJ_REQ_AZIMUTH_REF = "Reference direction for azimuth angles. TRUE_NORTH (TN): measured from geographic true north. GRID_NORTH (GN): measured from map projection grid north. The difference is the grid convergence angle.";
    public static final String SWAGGER_TRJ_REQ_AZIMUTH_REF_EXAMPLE = "TRUE_NORTH";
    public static final String SWAGGER_TRJ_REQ_LIST_OF_INPUT_STATIONS = "The array of input trajectory stations";
    public static final String SWAGGER_TRJ_REQ_LIST_OF_INPUT_STATIONS_EX = "[\n" +
            "    {\n" +
            "      \"md\": 0,\n" +
            "      \"inclination\": 0,\n" +
            "      \"azimuth\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"md\": 1000,\n" +
            "      \"inclination\": 0,\n" +
            "      \"azimuth\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"md\": 2000,\n" +
            "      \"inclination\": 90,\n" +
            "      \"azimuth\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"md\": 3000,\n" +
            "      \"inclination\": 90,\n" +
            "      \"azimuth\": 0\n" +
            "    },\n" +
            "    {\n" +
            "      \"md\": 5000,\n" +
            "      \"inclination\": 90,\n" +
            "      \"azimuth\": 90\n" +
            "    },\n" +
            "    {\n" +
            "      \"md\": 6000,\n" +
            "      \"inclination\": 90,\n" +
            "      \"azimuth\": 90\n" +
            "    }\n" +
            "  ]";
    public static final String SWAGGER_TRF_REQ_REF_POINT = "The 3D reference point in the 'trajectoryCRS' where MD==0.";
    public static final String SWAGGER_TRJ_REQ_UNIT_XY = "Unit of measure for horizontal displacements (dx, dy). Required for dX_dY_dZ input kinds. Can be OSDU record ID (e.g., 'osdu:reference-data--UnitOfMeasure:m:') or persistable reference JSON. Auto-derived from projected CRS if not specified.";
    public static final String SWAGGER_TRJ_REQ_UNIT_XY_EXAMPLE = "\"{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}\"";
    public static final String SWAGGER_TRJ_REQ_UNIT_Z = "Unit of measure for vertical values (dz) and measured depth. Required field. Can be OSDU record ID (e.g., 'osdu:reference-data--UnitOfMeasure:m:') or persistable reference JSON.";
    public static final String SWAGGER_TRJ_REQ_UNIT_Z_EXAMPLE = "\"{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}\"";
    public static final String SWAGGER_TRJ_REQ_UNIT_EXAMPLE = "{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}";
    public static final String SWAGGER_TRJ_REQ_METHOD = "Computation method: 'AzimuthalEquidistant' (default) - standard method using azimuthal equidistant projection centered at well location. 'LMP' - Lee's Modified Proposal (SPE96813) for improved accuracy at extreme latitudes.";
    public static final String SWAGGER_TRJ_REQ_METHOD_EXAMPLE = "AzimuthalEquidistant";
    public static final String SWAGGER_TRJ_REQ_INPUT_KIND = "Format of input data: MD_Incl_Azim (default) - measured depth with inclination and azimuth angles. MD_dX_dY_dZ - measured depth with local deviations. dX_dY_dZ - deviations only (MD computed via inverse minimum curvature). MD_Incl - inclination-only surveys (no azimuth).";
    public static final String SWAGGER_TRJ_REQ_INPUT_KIND_EXAMPLE = "MD_Inclination_Azimuth";
    public static final String SWAGGER_TRJ_REQ_INTERPOLATE = "When true (default), interpolates additional stations at MD values specified in MD_i. When false, only original input stations are processed and MD_i is ignored.";
    public static final String SWAGGER_TRJ_REQ_INTERPOLATE_EX = "True";

    public static final String SWAGGER_TRJ_STN_IN_DESCRIPTION = "Input trajectory survey station. Required fields depend on inputKind: MD_Incl_Azim requires md, inclination, azimuth. dX_dY_dZ variants require dx, dy, dz.";
    public static final String SWAGGER_MD = "Measured Depth - distance along the wellbore from the reference point (e.g., kelly bushing). Unit specified by unitMD or unitZ. Required for most inputKind values.";
    public static final String SWAGGER_MD_EXAMPLE = "2563.56";
    public static final String SWAGGER_INC = "Wellbore inclination angle in degrees. 0° = vertical (down), 90° = horizontal, 180° = vertical (up). Range: 0-180. Required for MD_Incl_Azim and MD_Incl inputKind.";
    public static final String SWAGGER_INC_EXAMPLE = "15.0";
    public static final String SWAGGER_AZI = "Wellbore azimuth (direction) angle in degrees. 0°/360° = North, 90° = East, 180° = South, 270° = West. Range: 0-360. Reference (True North or Grid North) specified by azimuthReference.";
    public static final String SWAGGER_AZI_EXAMPLE = "355.0";
    public static final String SWAGGER_DX = "East-West displacement from well reference point. Positive = East. Used for dX_dY_dZ and MD_dX_dY_dZ inputKind. Unit specified by unitXY.";
    public static final String SWAGGER_DX_EXAMPLE = "55.9";
    public static final String SWAGGER_DY = "North-South displacement from well reference point. Positive = North. Y-axis alignment (True North or Grid North) depends on azimuthReference. Unit specified by unitXY.";
    public static final String SWAGGER_DY_EXAMPLE = "-145.3";
    public static final String SWAGGER_DZ = "True Vertical Depth (TVD) from well reference point. Positive = deeper/downward. Unit specified by unitZ.";
    public static final String SWAGGER_DZ_EXAMPLE = "1965.6";

    public static final String SWAGGER_TRJ_RSP_DESCRIPTION = "Trajectory response containing computed stations with positions, deviations, and WGS84 coordinates. Includes scale factor and convergence data for the GNL method.";
    public static final String SWAGGER_TRJ_RSP_LIST_OF_STATIONS = "Computed trajectory stations from original input. Each station includes absolute coordinates, local deviations, and WGS84 lat/long.";
    public static final String SWAGGER_TRJ_RSP_LIST_OF_STATIONS_I = "Interpolated trajectory stations at MD_i depths. Only present when interpolation was requested. Same structure as main stations.";
    public static final String SWAGGER_TRJ_RSP_UNIT_XY = "Unit of measure for horizontal deviations (dxTN, dyTN) in output trajectory stations.";
    public static final String SWAGGER_TRJ_RSP_UNIT_Z = "Unit of measure for vertical deviation (dZ) and point.z elevation in output trajectory stations.";
    public static final String SWAGGER_TRJ_RSP_UNIT_DLS = "Unit of measure for Dog Leg Severity (DLS) values. Automatically set to deg/100ft (non-metric) or deg/30m (metric) based on unitZ.";
    public static final String SWAGGER_TRJ_RSP_UNIT_DLS_EXAMPLE = "%7B%22ScaleOffset%22%3A%7B%22Scale%22%3A5.72614583987641E-4%2C%22Offset%22%3A0.0%7D%2C%22Symbol%22%3A%22deg%2F100ft%22%2C%22BaseMeasurement%22%3A%22%257B%2522Ancestry%2522%253A%2522Rotation_Per_Length%2522%257D%22%7D";
    public static final String SWAGGER_TRJ_RSP_METHOD = "The computation method used. AzimuthalEquidistant or LMP (Lee's Modified Proposal).";
    public static final String SWAGGER_TRJ_RSP_LOCAL_CRS = "Local Azimuthal Equidistant CRS centered at the well's surface location. True North oriented, true distance engineering CRS.";
    public static final String SWAGGER_TRJ_STN_OUT_DESCRIPTION = "Computed output trajectory station with survey angles, displacements, absolute coordinates, WGS84 position, and metadata.";
    public static final String SWAGGER_AZI_TN = "Wellbore azimuth relative to True North in degrees. Range: 0-360. 0°/360° = True North, 90° = East.";
    public static final String SWAGGER_AZI_TN_EXAMPLE = "355.96";
    public static final String SWAGGER_AZI_GN = "Wellbore azimuth relative to Grid North in degrees. Range: 0-360. Differs from TN azimuth by the grid convergence angle.";
    public static final String SWAGGER_AZI_GN_EXAMPLE = "355.0";
    public static final String SWAGGER_DX_TN = "True East-West displacement from reference point. Positive = East. Aligned with True North reference frame. Unit: unitXY.";
    public static final String SWAGGER_DX_TN_EXAMPLE = "55.9";
    public static final String SWAGGER_DY_TN = "True North-South displacement from reference point. Positive = North. Aligned with True North reference frame. Unit: unitXY.";
    public static final String SWAGGER_DY_TN_EXAMPLE = "-145.3";
    public static final String SWAGGER_TRJ_POINT = "Absolute 3D coordinates in trajectoryCRS. x/y in CRS units, z in unitZ (elevation above reference, negative for depth below).";
    public static final String SWAGGER_TRJ_RSP_ORIGINAL = "True if this station is from original input. False if generated by interpolation at an MD_i value.";
    public static final String SWAGGER_TRJ_RSP_DLS = "Dog Leg Severity - rate of change of wellbore direction. Unit specified by unitDls (typically deg/100ft or deg/30m).";
    public static final String SWAGGER_TRJ_WGS84_LONGITUDE = "WGS 84 longitude in decimal degrees. Range: -180 to +180. Computed from the station's absolute position.";
    public static final String SWAGGER_TRJ_WGS84_LATITUDE = "WGS 84 latitude in decimal degrees. Range: -90 to +90. Computed from the station's absolute position.";
    public static final String SWAGGER_TRJ_SCALE_FACTOR = "Point scale factor at this location. Ratio of grid distance to true ground distance. For UTM, typically ~0.9996 at central meridian.";
    public static final String SWAGGER_TRJ_CONVERGENCE = "Grid convergence angle in degrees. Angle between True North and Grid North at this location. Used to convert between TN and GN azimuths.";
    public static final String SWAGGER_TRJ_SCALE_CONVERGENCE = "Scale factor and convergence values computed at first and last stations. Used for precise surveying calculations.";
    public static final String SWAGGER_TRJ_MD_I = "Specifies measured depths where additional trajectory stations should be interpolated. Only used when 'interpolate' is true. Provide either explicit MD values (md_i array) or a regular interval (md_interval).";
    public static final String SWAGGER_TRJ_UNIT_MD = "Unit of measure for Measured Depth (MD) values. Optional - defaults to unitZ if not specified.";
    public static final String SWAGGER_TRJ_REQ_UNIT_MD_EXAMPLE = "\"{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}\"";
    public static final String SWAGGER_TRJ_REQ_MD_I_EX =  "{\n" +
            "    \"MD_i\": {\n" +
            "        \"md_i\": [\n" +
            "            200,\n" +
            "            400,\n" +
            "            600,\n" +
            "            800\n" +
            "        ]\n" +
            "    }\n" +
            "}";
    public static final String SWAGGER_TRJ_MINIMUM_DEPTH_INTERVAL_DESCRIPTION = "Specifies where to interpolate additional trajectory stations. Provide explicit MD values (md_i array) OR a regular interval (md_interval), not both.";
    public static final String SWAGGER_MD_I = "Explicit list of Measured Depth values where interpolated stations should be computed. Unit specified by unitMD or unitZ.";
    public static final String SWAGGER_MD_I_EXAMPLE = "200";
    public static final String SWAGGER_MD_INTERVAL = "Regular interval for generating interpolated stations. Stations created from first to last input MD at this interval.";
    public static final String SWAGGER_MD_INTERVAL_EXAMPLE = "25.0";
    public static final String SWAGGER_GEO_JSON_FEATURE_EXAMPLES = "[\n" +
            "      {\n" +
            "        \"geometry\": {\n" +
            "          \"coordinates\": [\n" +
            "            313405.9477893702,\n" +
            "            6544797.620047403,\n" +
            "            6.561679790026246\n" +
            "          ],\n" +
            "          \"type\": \"AnyCrsPoint\"\n" +
            "        },\n" +
            "        \"properties\": {},\n" +
            "        \"type\": \"AnyCrsFeature\"\n" +
            "      }\n" +
            "    ]";
    public static final String SWAGGER_GEO_PERSISTABLE_REFERENCE_CRS_EXAMPLE = "\"{\"lateBoundCRS\":{\"wkt\":\"PROJCS[\\\"ED_1950_UTM_Zone_32N\\\",GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",9.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",23032]]\",\"ver\":\"PE_10_3_1\",\"name\":\"ED_1950_UTM_Zone_32N\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"23032\"},\"type\":\"LBC\"},\"singleCT\":{\"wkt\":\"GEOGTRAN[\\\"ED_1950_To_WGS_1984_23\\\",GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-116.641],PARAMETER[\\\"Y_Axis_Translation\\\",-56.931],PARAMETER[\\\"Z_Axis_Translation\\\",-110.559],PARAMETER[\\\"X_Axis_Rotation\\\",0.893],PARAMETER[\\\"Y_Axis_Rotation\\\",0.921],PARAMETER[\\\"Z_Axis_Rotation\\\",-0.917],PARAMETER[\\\"Scale_Difference\\\",-3.52],AUTHORITY[\\\"EPSG\\\",1612]]\",\"ver\":\"PE_10_3_1\",\"name\":\"ED_1950_To_WGS_1984_23\",\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1612\"},\"type\":\"ST\"},\"ver\":\"PE_10_3_1\",\"name\":\"ED50 * EPSG-Nor N62 2001 / UTM zone 32N [23032,1612]\",\"authCode\":{\"auth\":\"SLB\",\"code\":\"23032023\"},\"type\":\"EBC\"}\"";
    public static final String SWAGGER_GEO_PERSISTABLE_REFERENCE_UNIT_Z = "\"{\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"scaleOffset\":{\"offset\":0.0,\"scale\":0.3048},\"symbol\":\"ft\",\"type\":\"USO\"}\"";
    public static final String SWAGGER_GEO_ANY_CRS_FEATURE_COLLECTION_EXAMPLE = "AnyCrsFeatureCollection";


	public static final String SWAGGER_BIN_GRID_CONVERT_TITLE = "CRS Convert service is an OSDU platform standard method for QC and conversion of Bin Grids, associated in particular with ingested seismic volumes, that describe the “real world” (Easting, Northing) of bin grid centers at (inline, crossline) local coordinates";
	public static final String SWAGGER_BIN_GRID_CONVERT_NOTES = " QC check of the “squareness” of a Bin Grid defined using 4 corner points."
			+ " Coordinate conversion of a Bin Grid to a new CRS and “square it up” (if target CRS is same as original CRS then conversion is omitted, and the squareness test is done in the original CRS)."
			+ " Calculate derived P6 parameters from the input 4 corners."
			+ " Calculate WGS 84 coordinates at the corners"
			+ " Returns converted Bin Grid and a QC of squareness of the bin grid";
	public static final String SWAGGER_BIN_GRID_CONVERT_REQUEST_DESCR = "The input and output of this method use the AbstractBinGrid:1.0.0  definition.  On input a minimum required properties can be given, which are enriched on output as indicated ";
	public static final String SWAGGER_BIN_GRID_CONVERSION_RESPONSE = "The response is essentially a measure of the computed “non-squareness” (dI,dJ) of the input BinGrid, and an output BinGrid which is essentially a copy of the input, but augmented with the derived P6 parameters filled out, and optionally (if a toCrs was given in the request) converted global coordinates that are “squared up” in the new geometry (which can be used in applications that require a square grid in a project CRS geometry; if the “squaring error” is small enough";
	public static final String SWAGGER_BIN_GRID_CONVERTED = " ";
    public static final String MD_INCL = "MD_Incl";
    public static final String MD_INCL_AZIM = "MD_Incl_Azim";
    public static final String AZIMUTHAL_EQUIDISTANT = "AzimuthalEquidistant";
    public static final String DX_DY_DZ = "dX_dY_dZ";
    public static final String INC_ONLY_OPERTN_APPL = "Original survey was inclination-only, change to Md_Incl_Az. The original inclination values were copied into the azimuth and then set to zero to force a vertical path.";

}
