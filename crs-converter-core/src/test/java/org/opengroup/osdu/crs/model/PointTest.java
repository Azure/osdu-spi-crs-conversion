package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.model.Point;
import org.junit.Test;

import static junit.framework.TestCase.*;


public class PointTest {
    @Test
    public void testPoint(){
        Point p = new Point();
        assertNotNull(p);
        assertFalse(Point.isValid(p)); // all values null
        assertFalse(Point.isValid(null));
        p = new Point(Double.NaN, 0.0, 0.0);
        assertFalse(Point.isValid(p));
        p = new Point(0.0, Double.NaN, 0.0);
        assertFalse(Point.isValid(p));
        p = new Point(0.0, 0.0, Double.NaN);
        assertFalse(Point.isValid(p));
        p = new Point(0.0,0.0,0.0);
        assertTrue(Point.isValid(p));
        Point.setNaN(p);
        assertFalse(Point.isValid(p));
    }
}
