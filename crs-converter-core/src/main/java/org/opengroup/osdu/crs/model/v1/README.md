# V1 Persistable Reference Handling

This package is responsible for the de-serialization of the
persistable reference strings into java instances for version 1.
There are two main entries:
* [CRS](CRS.java) to de-serialize all spatial persistable
reference strings. This abstract base class carries the common
properties. The ```static createInstance(String)``` method creates
specific class instances - depending on the type enumeration [CRSType](CRSType.java) :
  * [LateBoundCRS](LateBoundCRS.java) for late-bound CRSs
  * [EarlyBoundCRS](EarlyBoundCRS.java) for early-bound CRSs
  (late-bound CRS associated with a transformation)
  * [SingleTRF](SingleTRF.java) for single cartographic transformations,
  extending [TRF](TRF.java).
  * [CompoundTRF](CompoundTRF.java) for fallback or
  concatenated transformations, extending [TRF](TRF.java)
  * [AuthorityCode](AuthorityCode.java) for authority and authority code container
* [Unit](Unit.java) to de-serialize units using the ```static
createInstance(String)```. There are two different unit parameterizations:
  * [Abcd](Abcd.java), the Energistics method using 4 coefficients,
  which extends the shared, abstract [UnitParameters](UnitParameters.java)
  * [ScaleOffset](ScaleOffset.java), the Oilfield Services Data Dictionary
  (OSDD) method, which extends the shared, abstract
  [UnitParameters](UnitParameters.java).


Version 1 classes are generally not used to serialize back into JSON.
Only [version 2](../v2/README.md) classes take on this responsibility.

[Back to top-level](../README.md)