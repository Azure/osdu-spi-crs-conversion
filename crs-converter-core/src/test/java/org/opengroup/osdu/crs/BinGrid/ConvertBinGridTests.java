package org.opengroup.osdu.crs.BinGrid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

import java.io.IOException;

import jakarta.validation.ValidationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.crs.api.CrsConverterApiV3;
import org.opengroup.osdu.crs.api.exception.BadRequestException;
import org.opengroup.osdu.crs.converter.CRSConverter;
import org.opengroup.osdu.crs.model.ConvertBinGridRequest;
import org.opengroup.osdu.crs.model.ConvertBinGridResponse;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class ConvertBinGridTests {

	private static final String Input_Request_Without_CRS = "{\"inBinGrid\":{\"BinGridName\":\"TestBinGridName\",\"BinGridTypeID\":\"TestBinGridTypeID\",\"SourceBinGridID\":123,\"SourceBinGridAppID\":\"TestSourceBinGridAppID\",\"CoveragePercent\":1.0,\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"opendes:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615:\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3100000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3100000.00]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}\"";

	private static final String Input_Response_Without_CRS = "{\"maxMisLocation\":{\"dI\":0.0,\"dJ\":0.0},\"outBinGrid\":{\"BinGridName\":\"TestBinGridName\",\"BinGridTypeID\":\"TestBinGridTypeID\",\"SourceBinGridID\":123,\"SourceBinGridAppID\":\"TestSourceBinGridAppID\",\"CoveragePercent\":1.0,\"BinGridDefinitionMethodTypeID\":\"opendes:reference-data--BinGridDefinitionMethodType:4Corner:\",\"P6TransformationMethod\":9666,\"P6BinGridOriginI\":1.0,\"P6BinGridOriginJ\":1000.0,\"P6BinGridOriginEasting\":500000.0,\"P6BinGridOriginNorthing\":3000000.0,\"P6ScaleFactorOfBinGrid\":1.0,\"P6BinWidthOnIaxis\":1000.0,\"P6BinWidthOnJaxis\":100.0,\"P6MapGridBearingOfBinGridJaxis\":0.0,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1,\"ABCDBinGridSpatialLocation\":{\"SpatialLocationCoordinatesDate\":\"2022-12-20T07:28:33.091Z\",\"CoordinateQualityCheckPerformedBy\":\"CRSConvertservice,POSTconvertBinGrid\",\"CoordinateQualityCheckDateTime\":\"2022-12-20T07:28:33.091Z\",\"CoordinateQualityCheckRemarks\":[\"Max.squaringerror:dI=0.0,dJ=0.0bin\"],\"AsIngestedCoordinates\":{\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.0,3000000.0]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.0,3100000.0]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.0,3000000.0]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.0,3100000.0]}}]},\"Wgs84Coordinates\":{\"features\":[{\"type\":\"Feature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-93.0,27.12246964]}},{\"type\":\"Feature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-93.0,28.02525499]}},{\"type\":\"Feature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-91.99105764,27.11884583]}},{\"type\":\"Feature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-91.98276722,28.02148995]}}],\"type\":\"FeatureCollection\"}}},\"AppliedOperations\":[\"Squaredupthebingrid.dI=0.0,dJ=0.0bin\"]}";
		
	private static final String PersistenceReferenceCRS_Without_CRS = "\"{\\\"authCode\\\":{\\\"auth\\\":\\\"EPSG\\\",\\\"code\\\":\\\"32615\\\"},\\\"name\\\":\\\"WGS_1984_UTM_Zone_15N\\\",\\\"type\\\":\\\"LBC\\\",\\\"ver\\\":\\\"PE_10_9_1\\\",\\\"wkt\\\":\\\"PROJCS[\\\\\\\"WGS_1984_UTM_Zone_15N\\\\\\\",GEOGCS[\\\\\\\"GCS_WGS_1984\\\\\\\",DATUM[\\\\\\\"D_WGS_1984\\\\\\\",SPHEROID[\\\\\\\"WGS_1984\\\\\\\",6378137.0,298.257223563]],PRIMEM[\\\\\\\"Greenwich\\\\\\\",0.0],UNIT[\\\\\\\"Degree\\\\\\\",0.0174532925199433]],PROJECTION[\\\\\\\"Transverse_Mercator\\\\\\\"],PARAMETER[\\\\\\\"False_Easting\\\\\\\",500000.0],PARAMETER[\\\\\\\"False_Northing\\\\\\\",0.0],PARAMETER[\\\\\\\"Central_Meridian\\\\\\\",-93.0],PARAMETER[\\\\\\\"Scale_Factor\\\\\\\",0.9996],PARAMETER[\\\\\\\"Latitude_Of_Origin\\\\\\\",0.0],UNIT[\\\\\\\"Meter\\\\\\\",1.0],AUTHORITY[\\\\\\\"EPSG\\\\\\\",32615]]\\\"}\"";
	
	private static final String Input_Request_With_CRS = "{\"toCRS\":\"opendes:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::32064_EPSG::15851:\",\"inBinGrid\":{\"BinGridName\":\"TestBinGridName\",\"BinGridTypeID\":\"TestBinGridTypeID\",\"SourceBinGridID\":123,\"SourceBinGridAppID\":\"TestSourceBinGridAppID\",\"CoveragePercent\":1.0,\"BinGridDefinitionMethodTypeID\":\"\",\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"opendes:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615:\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3100000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3100000.00]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}";

	private static final String Input_Response_With_CRS = "{\"maxMisLocation\":{\"dI\":0.0,\"dJ\":0.38},\"outBinGrid\":{\"BinGridName\":\"TestBinGridName\",\"BinGridTypeID\":\"TestBinGridTypeID\",\"SourceBinGridID\":123,\"SourceBinGridAppID\":\"TestSourceBinGridAppID\",\"CoveragePercent\":1.0,\"BinGridDefinitionMethodTypeID\":\"\",\"P6TransformationMethod\":9666,\"P6BinGridOriginI\":1.0,\"P6BinGridOriginJ\":1000.0,\"P6BinGridOriginEasting\":3593536.4619751107,\"P6BinGridOriginNorthing\":9888463.879346535,\"P6ScaleFactorOfBinGrid\":1.0,\"P6BinWidthOnIaxis\":3297.478414657676,\"P6BinWidthOnJaxis\":329.74530454204273,\"P6MapGridBearingOfBinGridJaxis\":357.2135341919337,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1,\"ABCDBinGridSpatialLocation\":{\"SpatialLocationCoordinatesDate\":\"2022-12-20T09:30:24.503Z\",\"CoordinateQualityCheckPerformedBy\":\"CRSConvertservice,POSTconvertBinGrid\",\"CoordinateQualityCheckDateTime\":\"2022-12-20T09:30:24.503Z\",\"CoordinateQualityCheckRemarks\":[\"convertedfromto;squaredup:dI=0.0,dJ=0.38(bin)\"],\"AsIngestedCoordinates\":{\"CoordinateReferenceSystemID\":\"opendes:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::32064_EPSG::15851:\",\"type\":\"AnyCrsFeatureCollection\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3593536.462,9888463.879]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3577506.278,1.0217819309E7]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3922894.426,9904494.186]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[3906864.242,1.0233849616E7]}}]},\"Wgs84Coordinates\":{\"features\":[{\"type\":\"Feature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"A\",\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-92.99998152,27.12215051]}},{\"type\":\"Feature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"B\",\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-93.00001836,28.02559868]}},{\"type\":\"Feature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"C\",\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-91.99107323,27.11916457]}},{\"type\":\"Feature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Label\":\"D\",\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-91.98275212,28.02114667]}}],\"type\":\"FeatureCollection\"}}},\"AppliedOperations\":[\"conversionfromWGS_1984_UTM_Zone_15NtoGCS_WGS_1984;4pointsconverted\",\"transformationGCS_WGS_1984toGCS_North_American_1927usingNAD_1927_To_WGS_1984_79_CONUS;4pointssuccessfullytransformed\",\"conversionfromGCS_North_American_1927toNAD_1927_BLM_Zone_14N;4pointsconverted\",\"NounitconversionforZ-axis\",\"Squaredupthebingrid.dI=0.0,dJ=0.38bin\"]}";
		
	private static final String PersistenceReferenceCRS_With_CRS = "{\\\"authCode\\\":{\\\"auth\\\":\\\"OSDU\\\",\\\"code\\\":\\\"32064079\\\"},\\\"lateBoundCRS\\\":{\\\"authCode\\\":{\\\"auth\\\":\\\"EPSG\\\",\\\"code\\\":\\\"32064\\\"},\\\"name\\\":\\\"NAD_1927_BLM_Zone_14N\\\",\\\"type\\\":\\\"LBC\\\",\\\"ver\\\":\\\"PE_10_9_1\\\",\\\"wkt\\\":\\\"PROJCS[\\\\\\\"NAD_1927_BLM_Zone_14N\\\\\\\",GEOGCS[\\\\\\\"GCS_North_American_1927\\\\\\\",DATUM[\\\\\\\"D_North_American_1927\\\\\\\",SPHEROID[\\\\\\\"Clarke_1866\\\\\\\",6378206.4,294.9786982]],PRIMEM[\\\\\\\"Greenwich\\\\\\\",0.0],UNIT[\\\\\\\"Degree\\\\\\\",0.0174532925199433]],PROJECTION[\\\\\\\"Transverse_Mercator\\\\\\\"],PARAMETER[\\\\\\\"False_Easting\\\\\\\",1640416.666666667],PARAMETER[\\\\\\\"False_Northing\\\\\\\",0.0],PARAMETER[\\\\\\\"Central_Meridian\\\\\\\",-99.0],PARAMETER[\\\\\\\"Scale_Factor\\\\\\\",0.9996],PARAMETER[\\\\\\\"Latitude_Of_Origin\\\\\\\",0.0],UNIT[\\\\\\\"Foot_US\\\\\\\",0.3048006096012192],AUTHORITY[\\\\\\\"EPSG\\\\\\\",32064]]\\\"},\\\"name\\\":\\\"NAD27*OGP-UsaConus/BLMzone14N(USsurveyfeet)[32064,15851]\\\",\\\"singleCT\\\":{\\\"authCode\\\":{\\\"auth\\\":\\\"EPSG\\\",\\\"code\\\":\\\"15851\\\"},\\\"name\\\":\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",\\\"type\\\":\\\"ST\\\",\\\"ver\\\":\\\"PE_10_9_1\\\",\\\"wkt\\\":\\\"GEOGTRAN[\\\\\\\"NAD_1927_To_WGS_1984_79_CONUS\\\\\\\",GEOGCS[\\\\\\\"GCS_North_American_1927\\\\\\\",DATUM[\\\\\\\"D_North_American_1927\\\\\\\",SPHEROID[\\\\\\\"Clarke_1866\\\\\\\",6378206.4,294.9786982]],PRIMEM[\\\\\\\"Greenwich\\\\\\\",0.0],UNIT[\\\\\\\"Degree\\\\\\\",0.0174532925199433]],GEOGCS[\\\\\\\"GCS_WGS_1984\\\\\\\",DATUM[\\\\\\\"D_WGS_1984\\\\\\\",SPHEROID[\\\\\\\"WGS_1984\\\\\\\",6378137.0,298.257223563]],PRIMEM[\\\\\\\"Greenwich\\\\\\\",0.0],UNIT[\\\\\\\"Degree\\\\\\\",0.0174532925199433]],METHOD[\\\\\\\"NADCON\\\\\\\"],PARAMETER[\\\\\\\"Dataset_conus\\\\\\\",0.0],OPERATIONACCURACY[5.0],AUTHORITY[\\\\\\\"EPSG\\\\\\\",15851]]\\\"},\\\"type\\\":\\\"EBC\\\",\\\"ver\\\":\\\"PE_10_9_1\\\"}";
	
	private static final String Input_Request_Without_CRS_Size_3 = "{\"inBinGrid\":{\"BinGridName\":\"TestBinGridName\",\"BinGridTypeID\":\"TestBinGridTypeID\",\"SourceBinGridID\":123,\"SourceBinGridAppID\":\"TestSourceBinGridAppID\",\"CoveragePercent\":1.0,\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"opendes:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615:\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3100000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3000000.00]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}";
	
	private static final String Input_Request_InValid = "{\"inBinGrid\":{\"BinGridName\":\"TestBinGridName\",\"BinGridTypeID\":\"TestBinGridTypeID\",\"SourceBinGridID\":123,\"SourceBinGridAppID\":\"TestSourceBinGridAppID\",\"CoveragePercent\":1.0,\"BinGridDefinitionMethodTypeID\":\"4Corner\",\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"opendes:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615:\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":1}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3100000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3100000.00]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}";
	
	private static final String Input_Request_InValid_CRS = "{\"inBinGrid\":{\"BinGridName\":\"TestBinGridName\",\"BinGridTypeID\":\"TestBinGridTypeID\",\"SourceBinGridID\":123,\"SourceBinGridAppID\":\"TestSourceBinGridAppID\",\"CoveragePercent\":1.0,\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"opendes:reference-data--CoordinateReferenceSystem:Projected:EPSG::32221:\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3100000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3100000.00]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}";
	
	@Mock
	private CRSConverter crsConverter;

	@Mock
	private CrsConverterApiV3 crsConverterApi;

	@Test
	public void convertBinGridWithSuccessfulResponse() {
		ConvertBinGridRequest inBinGrid = new ConvertBinGridRequest();
		ConvertBinGridResponse response = new ConvertBinGridResponse();
		MaxMisLocation location = new MaxMisLocation();
		location.setDI(0.0);
		location.setDJ(0.0);
		response.setMaxMisLocation(location);
		inBinGrid = ConvertBinGridTests.createInstance(Input_Request_Without_CRS);
		response.setOutBinGrid(inBinGrid.getInBinGrid());
		response = ConvertBinGridTests.createResponse(Input_Response_Without_CRS);
		String persistableReferenceCrs = ConvertBinGridTests.createPersistenceReferenceCRS(PersistenceReferenceCRS_Without_CRS);
		response.getOutBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates().setPersistableReferenceCrs(persistableReferenceCrs);	
		lenient().when(crsConverter.squaring(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(response);
		assertEquals(new Double(0.0), response.getMaxMisLocation().getDI());
		assertEquals(new Double(0.0),response.getMaxMisLocation().getDJ());
	}
	
	@Test
	public void convertBinGridWithToCRSSuccessfulResponse() {
		ConvertBinGridRequest inBinGrid = new ConvertBinGridRequest();
		ConvertBinGridResponse response = new ConvertBinGridResponse();
		MaxMisLocation location = new MaxMisLocation();
		location.setDI(0.0);
		location.setDJ(0.0);
		response.setMaxMisLocation(location);
		inBinGrid = ConvertBinGridTests.createInstance(Input_Request_With_CRS);
		response.setOutBinGrid(inBinGrid.getInBinGrid());
		response = ConvertBinGridTests.createResponse(Input_Response_With_CRS);
		String persistableReferenceCrs = ConvertBinGridTests.createPersistenceReferenceCRS(PersistenceReferenceCRS_With_CRS);
		response.getOutBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates().setPersistableReferenceCrs(persistableReferenceCrs);	
		lenient().when(crsConverter.squaring(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(response);
		assertEquals(new Double(0.0), response.getMaxMisLocation().getDI());
		assertEquals(new Double(0.38),response.getMaxMisLocation().getDJ());
	}
		
	@Test
	public void convertBinGridWithExceptionResponse() {
		final String errorMsg = "Invalid size for spatial coordinates in the input request. Expected 4 AnyCrsFeatures with geometry “AnyCrsPoint”.  Found \"\n" +
				"							+ size + \" points";
		ConvertBinGridRequest inBinGrid = new ConvertBinGridRequest();
		ConvertBinGridResponse response = new ConvertBinGridResponse();
		inBinGrid = ConvertBinGridTests.createInstance(Input_Request_Without_CRS_Size_3);
		response.setOutBinGrid(inBinGrid.getInBinGrid());		
		try {
			lenient().when(crsConverter.squaring(Mockito.anyString(), Mockito.any(), Mockito.any())).thenThrow(ValidationException.class);
		}catch (Exception e) {
			ValidationException apiException = (ValidationException) e;
			assertEquals(apiException.getMessage(), errorMsg);
		}
	}
		
    @Test
	public void convertBinGridInValidRequest() {
    	final String errorMsg = "Point properties (inLine and crossLine) are mandatory.";
		ConvertBinGridRequest inBinGrid = new ConvertBinGridRequest();
		ConvertBinGridResponse response = new ConvertBinGridResponse();		
		inBinGrid = ConvertBinGridTests.createInstance(Input_Request_InValid);
		response.setOutBinGrid(inBinGrid.getInBinGrid());
		try {
			lenient().when(crsConverter.squaring(Mockito.anyString(), Mockito.any(), Mockito.any())).thenThrow(BadRequestException.class);
		}catch (Exception e) {
			BadRequestException apiException = (BadRequestException) e;
			assertEquals(apiException.getMessage(), errorMsg);
		}		 
	}
    
    @Test
   	public void convertBinGridInValidCRSRequest() {
       	final String errorMsg = "Could not find a conversion method for the given input. no transformation 'WGS_1972_UTM_Zone_21N' -> 'GCS_WGS_1984'";
   		ConvertBinGridRequest inBinGrid = new ConvertBinGridRequest();
   		ConvertBinGridResponse response = new ConvertBinGridResponse();		
   		inBinGrid = ConvertBinGridTests.createInstance(Input_Request_InValid_CRS);
   		response.setOutBinGrid(inBinGrid.getInBinGrid());
   		try {
   			lenient().when(crsConverter.squaring(Mockito.anyString(), Mockito.any(), Mockito.any())).thenThrow(BadRequestException.class);
   		}catch (Exception e) {
   			BadRequestException apiException = (BadRequestException) e;
   			assertEquals(apiException.getMessage(), errorMsg);
   		}		 
   	}
	
	

	public static ConvertBinGridRequest createInstance(String json) {
		ConvertBinGridRequest result;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JsonNode node = mapper.readTree(json);
			result = mapper.treeToValue(node, ConvertBinGridRequest.class);
		} catch (IOException e) {
			return null;
		}
		return result;
	}
	
	public static ConvertBinGridResponse createResponse(String json) {
		ConvertBinGridResponse result;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JsonNode node = mapper.readTree(json);
			result = mapper.treeToValue(node, ConvertBinGridResponse.class);
		} catch (IOException e) {
			return null;
		}
		return result;
	}
	
	public static String createPersistenceReferenceCRS(String json) {
		String result;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JsonNode node = mapper.readTree(json);
			result = mapper.treeToValue(node, String.class);
		} catch (IOException e) {
			return null;
		}
		return result;
	}

}
