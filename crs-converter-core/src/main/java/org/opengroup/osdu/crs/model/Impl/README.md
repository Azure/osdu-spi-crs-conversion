# Spatial and Unit implementations

This package contains the implementations of the spatial model elements and the unit.
* [LateBoundCrs](./LateBoundCrs.java), implementing
[ILateBoundCrs](../ILateBoundCrs.java) - for late-bound CRSs
with resolved Esri PE class instances.
* [EarlyBoundCrs](./EarlyBoundCrs.java), implementing
[IEarlyBoundCrs](../IEarlyBoundCrs.java) - for early-bound CRSs
containing references to a late-bound CRS and a transformation.
* [SingleTrf](./SingleTrf.java), implementing
[ISingleTrf](../ISingleTrf.java) - for simple cartographic
transformations with resolved PE class instances. This instance
also implements the ```transform``` method.
* [CompoundTrf](./CompoundTrf.java), implementing
[ICompoundTrf](../ICompoundTrf.java) - for compound cartographic
transformations with resolved PE class instances. This instance
also implements the ```transform``` method for compound transformations,
delegating to the [SingleTrf](./SingleTrf.java) instances.
* [Unit](./Unit.java) implementing [IUnit](../IUnit.java)

The implementations above are instantiated by the
[ItemFactory](./ItemFactory.java) depending on the
parsed/de-serialized persistable reference (version1 or version 2).

[Back to top-level](../../README.md)