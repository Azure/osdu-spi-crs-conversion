# V2 Persistable Reference Handling

This package is responsible for the de-serialization of the
persistable reference strings into java instances for version 2.
There is one single main entry:
* [PersistableReference](PersistableReference.java) to de-serialize
all persistable reference strings. The class specific types resolve into
concrete specializations via ```static createInstance(String)``` method creates
specific class instances:
  * Spatial classes
    * [LateBoundCrs](LateBoundCrs.java) for late-bound CRSs,
    extending [PersistableReference](PersistableReference.java)
    * [EarlyBoundCrs](EarlyBoundCrs.java) for early-bound CRSs
    (late-bound CRS associated with a transformation),
    extending [PersistableReference](PersistableReference.java)
    * [CompoundCrs](CompoundCrs.java) for compound CRSs, i.e. 2D and
    1D CRS combinations,
    extending [PersistableReference](PersistableReference.java);
    not supported yet.
    * [SingleTrf](SingleTrf.java) for single cartographic transformations,
    extending [PersistableReference](PersistableReference.java)
    * [CompoundTrf](CompoundTrf.java) for fallback or
    concatenated transformations,
    extending [PersistableReference](PersistableReference.java)
    * [AuthorityCode](AuthorityCode.java) for authority and authority
    code container
    * [AreaOfUse](AreaOfUse.java) for completeness (not in use in the
    converter), extending [PersistableReference](PersistableReference.java)
    * [Wgs84BoundingBox](Wgs84BoundingBox.java) for completeness (not
    in use in the converter)
  * Unit classes
    * [UnitEnergistics](UnitEnergistics.java),
    extending [PersistableReference](PersistableReference.java)
     with the parameter class
    [Abcd](Abcd.java)
    * [UnitScaleOffset](UnitScaleOffset.java),
     extending [PersistableReference](PersistableReference.java)
     with the parameter class
    [ScaleOffset](ScaleOffset.java)
    * [Measurement](Measurement.java) for the measurement (or dimension),
    extending [PersistableReference](PersistableReference.java)

The version 2 classes can be assembled from scratch and serialized into
JSON strings via ```String PersistableReference.toJsonString()```.

[Back to top-level](../README.md)