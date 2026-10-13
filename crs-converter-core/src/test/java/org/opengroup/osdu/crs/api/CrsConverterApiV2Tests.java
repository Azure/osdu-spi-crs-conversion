package org.opengroup.osdu.crs.api;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengroup.osdu.crs.GeoJson.GeoJsonBase;
import org.opengroup.osdu.crs.GeoJson.GeoJsonFeatureCollection;
import org.opengroup.osdu.crs.converter.CRSConverter;
import org.opengroup.osdu.crs.converter.PointConverter;
import org.opengroup.osdu.crs.api.exception.BadRequestException;
import org.opengroup.osdu.crs.model.*;
import org.opengroup.osdu.crs.util.ConstantsTests;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class CrsConverterApiV2Tests {

	@Mock
	PointConverter pointConverter;

	@Mock
	CRSConverter crsConverter;

	@InjectMocks
    private CrsConverterApiV2 crsConverterApi;

    @Before
    public void setUp() {
		MockitoAnnotations.initMocks(this);
    }

	private ConvertPointsResponse makeResponse(int nofPoints) {
		ConvertPointsResponse response = new ConvertPointsResponse();
		response.setSuccessCount(nofPoints);
		return response;
	}

    private ConvertGeoJsonResponse makeGeoJsonResponse(int nofPoints) {
        ConvertGeoJsonResponse response = new ConvertGeoJsonResponse();
        response.setFeatureCollection((GeoJsonFeatureCollection)GeoJsonBase.createInstance(ConstantsTests.GEO_JSON_01));
        response.setSuccessCount(nofPoints);
        response.setTotalCount(nofPoints);
        return response;
    }

    @Test
	public void convertPointWithSuccessfulResponse() {
		List<Point> points = new ArrayList<>();
		points.add(new Point(1.0, 1.0, 1.0));
		points.add(new Point(2.0, 2.0, 2.0));
		points.add(new Point(3.0, 3.0, 3.0));

		ConvertPointsRequest request = new ConvertPointsRequest();
		request.setFromCRS("sourceCRS");
		request.setToCRS("targetCRS");
		request.setPoints(points);

		double[] expectedXY = new double[] { 1.0, 1.0, 2.0, 2.0, 3.0, 3.0 };
		double[] expectedZ = new double[] { 1.0, 2.0, 3.0 };

		when(pointConverter.mergeXYCoordinates(request.getPoints())).thenReturn(expectedXY);
		when(pointConverter.mergeZCoordinates(request.getPoints())).thenReturn(expectedZ);
		when(pointConverter.convertValuesToPoints(expectedXY, expectedZ)).thenReturn(points);
		when(crsConverter.convertPoint(request.getFromCRS(), request.getToCRS(), expectedXY, expectedZ)).thenReturn(makeResponse(3));

		ConvertPointsResponse response = crsConverterApi.convertPoint(request);

		assertEquals(new Integer(3), response.getSuccessCount());
		assertThat(response.getPoints(), is(points));
	}

    @Test
    public void convertGeoJsonWithSuccessfulResponse() {
        List<Point> points = new ArrayList<>();
        points.add(new Point(1.0, 1.0, 1.0));

        ConvertGeoJsonRequest request = new ConvertGeoJsonRequest();
        request.setToCRS("targetCRS");
        request.setFeatureCollection((GeoJsonFeatureCollection) GeoJsonBase.createInstance(ConstantsTests.GEO_JSON_01));

        double[] expectedXY = new double[] { 1.0, 1.0 };
        double[] expectedZ = new double[] { 1.0 };

        when(pointConverter.mergeXYCoordinates(points)).thenReturn(expectedXY);
        when(pointConverter.mergeZCoordinates(points)).thenReturn(expectedZ);
        when(pointConverter.convertValuesToPoints(expectedXY, expectedZ)).thenReturn(points);
        when(crsConverter.convertGeoJson(request.getFeatureCollection(), request.getToCRS(), null)).thenReturn(makeGeoJsonResponse(1));

		ConvertGeoJsonResponse response = crsConverterApi.convertGeoJson(request);

        assertEquals(new Integer(1), response.getSuccessCount());
        assertEquals(new Integer(1), response.getTotalCount());
        GeoJsonFeatureCollection fc = response.getFeatureCollection();
        assertThat(response.getFeatureCollection(), is(fc));
    }

	@Test(expected = IllegalArgumentException.class)
	public void convertPointWithIllegalArgumentException() {
		final String errorMsg = "Failure during CRS conversion!";
		List<Point> points = new ArrayList<>();
		points.add(new Point(1.0, 1.0, 1.0));
		points.add(new Point(2.0, 2.0, 2.0));
		points.add(new Point(3.0, 3.0, 3.0));

		ConvertPointsRequest request = new ConvertPointsRequest();
		request.setFromCRS("sourceCRS");
		request.setToCRS("targetCRS");
		request.setPoints(points);

		double[] expectedXY = new double[] { 1.0, 1.0, 2.0, 2.0, 3.0, 3.0 };
		double[] expectedZ = new double[] { 1.0, 2.0, 3.0 };

		when(pointConverter.mergeXYCoordinates(request.getPoints())).thenReturn(expectedXY);
		when(pointConverter.mergeZCoordinates(request.getPoints())).thenReturn(expectedZ);
		when(pointConverter.convertValuesToPoints(expectedXY, expectedZ)).thenReturn(points);

		when(crsConverter.convertPoint(request.getFromCRS(), request.getToCRS(), expectedXY, expectedZ)).thenThrow(new IllegalArgumentException(errorMsg));

		crsConverterApi.convertPoint(request);
	}

    @Test(expected = IllegalArgumentException.class)
    public void convertGeoJsonWithIllegalArgumentException() {
        final String errorMsg = "Failure during CRS conversion!";
        List<Point> points = new ArrayList<>();
        points.add(new Point(1.0, 1.0, 1.0));

        ConvertGeoJsonRequest request = new ConvertGeoJsonRequest();
        request.setToCRS("targetCRS");
        request.setFeatureCollection((GeoJsonFeatureCollection) GeoJsonBase.createInstance(ConstantsTests.GEO_JSON_01));

        double[] expectedXY = new double[]{1.0, 1.0};
        double[] expectedZ = new double[]{1.0};

        when(pointConverter.mergeXYCoordinates(points)).thenReturn(expectedXY);
        when(pointConverter.mergeZCoordinates(points)).thenReturn(expectedZ);
        when(pointConverter.convertValuesToPoints(expectedXY, expectedZ)).thenReturn(points);

        when(crsConverter.convertGeoJson(request.getFeatureCollection(), request.getToCRS(), null)).thenThrow(new IllegalArgumentException(errorMsg));

		crsConverterApi.convertGeoJson(request);
    }

    @Test(expected = BadRequestException.class)
	public void convertPointWithBadRequestException() {
		final String errorMsg = "Failure during CRS conversion!";
		List<Point> points = new ArrayList<>();
		points.add(new Point(1.0, 1.0, 1.0));
		points.add(new Point(2.0, 2.0, 2.0));
		points.add(new Point(3.0, 3.0, 3.0));

		ConvertPointsRequest request = new ConvertPointsRequest();
		request.setFromCRS("sourceCRS");
		request.setToCRS("targetCRS");
		request.setPoints(points);

		double[] expectedXY = new double[] { 1.0, 1.0, 2.0, 2.0, 3.0, 3.0 };
		double[] expectedZ = new double[] { 1.0, 2.0, 3.0 };

		when(pointConverter.mergeXYCoordinates(request.getPoints())).thenReturn(expectedXY);
		when(pointConverter.mergeZCoordinates(request.getPoints())).thenReturn(expectedZ);
		when(pointConverter.convertValuesToPoints(expectedXY, expectedZ)).thenReturn(points);

		when(crsConverter.convertPoint(request.getFromCRS(), request.getToCRS(), expectedXY, expectedZ)).thenThrow(new BadRequestException(errorMsg));

		crsConverterApi.convertPoint(request);
	}

    @Test(expected = BadRequestException.class)
    public void convertGeoJsonWithBadRequestException() {
        final String errorMsg = "Failure during CRS conversion!";
        List<Point> points = new ArrayList<>();
        points.add(new Point(1.0, 1.0, 1.0));

        ConvertGeoJsonRequest request = new ConvertGeoJsonRequest();
        request.setToCRS("targetCRS");
        request.setFeatureCollection((GeoJsonFeatureCollection) GeoJsonBase.createInstance(ConstantsTests.GEO_JSON_01));

        double[] expectedXY = new double[] { 1.0, 1.0 };
        double[] expectedZ = new double[] { 1.0 };

        when(pointConverter.mergeXYCoordinates(points)).thenReturn(expectedXY);
        when(pointConverter.mergeZCoordinates(points)).thenReturn(expectedZ);
        when(pointConverter.convertValuesToPoints(expectedXY, expectedZ)).thenReturn(points);

        when(crsConverter.convertGeoJson(request.getFeatureCollection(), request.getToCRS(), null)).thenThrow(new BadRequestException(errorMsg));

		crsConverterApi.convertGeoJson(request);
    }
}
