package org.opengroup.osdu.crs.converter;

import org.opengroup.osdu.crs.model.Point;
import org.opengroup.osdu.crs.converter.PointConverter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PointConverterTests {

	@Test
	public void mergeXYCoordinatesSuccessfull() {
		List<Point> points = new ArrayList<>();
		points.add(new Point(1.0, 1.0, 1.0));
		points.add(new Point(2.0, 2.0, 2.0));
		points.add(new Point(3.0, 3.0, 3.0));

		double[] expectedXY = new double[] { 1.0, 1.0, 2.0, 2.0, 3.0, 3.0 };

		double[] result = new PointConverter().mergeXYCoordinates(points);

		assertThat(result, is(expectedXY));
	}

	@Test
	public void mergeZCoordinatesSuccessfull() {
		List<Point> points = new ArrayList<>();
		points.add(new Point(1.0, 1.0, 1.0));
		points.add(new Point(2.0, 2.0, 2.0));
		points.add(new Point(3.0, 3.0, 3.0));

		double[] expectedZ = new double[] { 1.0, 2.0, 3.0 };

		double[] result = new PointConverter().mergeZCoordinates(points);

		assertThat(result, is(expectedZ));
	}

	@Test
	public void convertValuesToPointsSuccessfull() {
		List<Point> expectedPoints = new ArrayList<>();
		expectedPoints.add(new Point(1.0, 1.0, 1.0));
		expectedPoints.add(new Point(2.0, 2.0, 2.0));
		expectedPoints.add(new Point(3.0, 3.0, 3.0));

		double[] xyPoints = new double[] { 1.0, 1.0, 2.0, 2.0, 3.0, 3.0 };
		double[] zPoints = new double[] { 1.0, 2.0, 3.0 };

		List<Point> result = new PointConverter().convertValuesToPoints(xyPoints, zPoints);

		assertThat(result, is(expectedPoints));
	}
}
