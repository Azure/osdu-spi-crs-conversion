package org.opengroup.osdu.crs.sis.wkt;

public interface IWktAttribute<T> extends IWktElement {

    public T getValue();

    public void setValue(T newValue);
}
