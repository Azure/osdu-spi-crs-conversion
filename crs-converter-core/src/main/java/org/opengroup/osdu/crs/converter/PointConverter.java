package org.opengroup.osdu.crs.converter;

import org.opengroup.osdu.crs.interfaces.IPointConverter;
import org.opengroup.osdu.crs.model.Point;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PointConverter implements IPointConverter {

	@Override
	public double[] mergeXYCoordinates(List<Point> points) {

		double[] merged = new double[2 * points.size()];

		int index = 0;

		for (Point p : points) {
			merged[index] = p.getX();
			merged[index + 1] = p.getY();

			index += 2;
		}

		return merged;
	}

	@Override
	public double[] mergeZCoordinates(List<Point> points) {

		double[] merged = new double[points.size()];

		int index = 0;

		for (Point p : points) {
			merged[index] = p.getZ();

			index++;
		}

		return merged;
	}

	@Override
	public List<Point> convertValuesToPoints(double[] xyPoints, double[] zPoints) {

		List<Point> points = new ArrayList<>();

		for (int i = 0; i < xyPoints.length; i += 2) {
			points.add(new Point(xyPoints[i], xyPoints[i + 1], zPoints[i / 2]));
		}

		return points;
	}
}
