package org.opengroup.osdu.crs.interfaces;

import java.util.List;
import org.opengroup.osdu.crs.model.Point;

public interface IPointConverter {

	double[] mergeXYCoordinates(List<Point> points);

	double[] mergeZCoordinates(List<Point> points);

	List<Point> convertValuesToPoints(double[] xyPoints, double[] zPoints);
}
