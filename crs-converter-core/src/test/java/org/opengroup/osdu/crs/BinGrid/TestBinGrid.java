package org.opengroup.osdu.crs.BinGrid;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.opengroup.osdu.crs.model.ConvertBinGridRequest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestBinGrid {

	private static final String Input_Request = "{\"inBinGrid\":{\"BinGridName\":\"TestBinGridName\",\"BinGridTypeID\":\"TestBinGridTypeID\",\"SourceBinGridID\":123,\"SourceBinGridAppID\":\"TestSourceBinGridAppID\",\"CoveragePercent\":1.0,\"ABCDBinGridSpatialLocation\":{\"AsIngestedCoordinates\":{\"type\":\"AnyCrsFeatureCollection\",\"CoordinateReferenceSystemID\":\"opendes:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615\",\"features\":[{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":1,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":1,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[500000.00,3100000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":101,\"Crossline\":1000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3000000.00]}},{\"type\":\"AnyCrsFeature\",\"properties\":{\"Kind\":\"osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0\",\"PointProperties\":[{\"Inline\":101,\"Crossline\":2000}]},\"geometry\":{\"type\":\"AnyCrsPoint\",\"coordinates\":[600000.00,3100000.00]}}]}},\"P6ScaleFactorOfBinGrid\":1,\"P6BinNodeIncrementOnIaxis\":1,\"P6BinNodeIncrementOnJaxis\":1}}\"";

	@Test
	public void testBinGridInputRequest() {
		ConvertBinGridRequest inBinGridRequest;
		inBinGridRequest = TestBinGrid.createInstance(Input_Request);
		assertNotNull(inBinGridRequest);
		assertEquals(1, inBinGridRequest.getInBinGrid().getP6ScaleFactorOfBinGrid());
		assertEquals(1, inBinGridRequest.getInBinGrid().getP6BinNodeIncrementOnIaxis());
		assertEquals(1, inBinGridRequest.getInBinGrid().getP6BinNodeIncrementOnJaxis());
		assertEquals(4, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().size());
		assertEquals(1, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().get(0).getProperties().getPointPropertiesList().get(0).getInline());
		assertEquals(1000, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().get(0).getProperties().getPointPropertiesList().get(0).getCrossline());

		assertEquals(1, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().get(1).getProperties().getPointPropertiesList().get(0).getInline());
		assertEquals(2000, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().get(1).getProperties().getPointPropertiesList().get(0).getCrossline());

		assertEquals(101, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().get(2).getProperties().getPointPropertiesList().get(0).getInline());
		assertEquals(1000, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().get(2).getProperties().getPointPropertiesList().get(0).getCrossline());

		assertEquals(101, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().get(3).getProperties().getPointPropertiesList().get(0).getInline());
		assertEquals(2000, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation().getAsIngestedcoordinates()
				.getFeatures().get(3).getProperties().getPointPropertiesList().get(0).getCrossline());

		assertEquals(500000.00, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation()
				.getAsIngestedcoordinates().getFeatures().get(0).getGeometry().getCoordinates().get(0));
		assertEquals(3000000.00, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation()
				.getAsIngestedcoordinates().getFeatures().get(0).getGeometry().getCoordinates().get(1));

		assertEquals(500000.00, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation()
				.getAsIngestedcoordinates().getFeatures().get(1).getGeometry().getCoordinates().get(0));
		assertEquals(3100000.00, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation()
				.getAsIngestedcoordinates().getFeatures().get(1).getGeometry().getCoordinates().get(1));

		assertEquals(600000.00, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation()
				.getAsIngestedcoordinates().getFeatures().get(2).getGeometry().getCoordinates().get(0));
		assertEquals(3000000.00, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation()
				.getAsIngestedcoordinates().getFeatures().get(2).getGeometry().getCoordinates().get(1));

		assertEquals(600000.00, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation()
				.getAsIngestedcoordinates().getFeatures().get(3).getGeometry().getCoordinates().get(0));
		assertEquals(3100000.00, inBinGridRequest.getInBinGrid().getABCDBinGridSpatialLocation()
				.getAsIngestedcoordinates().getFeatures().get(3).getGeometry().getCoordinates().get(1));
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

}
