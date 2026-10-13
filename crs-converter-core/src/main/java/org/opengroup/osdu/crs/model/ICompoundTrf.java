package org.opengroup.osdu.crs.model;

import org.opengroup.osdu.crs.sis.transform.ISisMathTransform;

import java.util.List;

public interface ICompoundTrf extends ITrf {
    String getPolicy();
    void setPolicy(String policy);

    List<ISingleTrf> getTransformations();
    void setTransformations(List<ISingleTrf> transformations);

    int[] getForwardDirection();
    void setForwardDirection(int[] directions);

    int[] getInverseDirection();
    void setInverseDirection(int[] directions);

    boolean isFallback();
    boolean isConcatenated();

    /**
     * Retrieves the transformation operation for a compound transformation.
     *
     * <p>This method is intended for use cases where the specific steps of the
     * compound transformation are not provided explicitly. In such scenarios,
     * the method defaults to using the transformation steps as specified in the
     * EPSG (European Petroleum Survey Group) database.</p>
     *
     * <p>For example, for the compound transformation EPSG:8537 (Egypt 1907 to
     * WGS 84 (2)), the steps involved are:
     * <ul>
     *   <li>Transformation code 1545: Egypt 1907 to WGS 1972</li>
     *   <li>Transformation code 1237: WGS 1972 to WGS 84</li>
     * </ul>
     * These steps are defined in the EPSG specification and will be used by
     * this method in the absence of explicitly provided steps.</p>
     *
     * <p>The method is similar to the {@link ISingleTrf#getTransformOperation()}
     * method in the {@link ISingleTrf} interface, but it applies to compound
     * transformations instead of single transformations.</p>
     *
     * @return the transformation operation as an {@link ISisMathTransform} instance.
     */
    ISisMathTransform getTransformOperation();
}
