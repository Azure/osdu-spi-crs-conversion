package org.opengroup.osdu.crs.model;

public interface IItem {
    // base interface to inherit the shared methods
    boolean isValid();

    String createPersistableReference();
}
