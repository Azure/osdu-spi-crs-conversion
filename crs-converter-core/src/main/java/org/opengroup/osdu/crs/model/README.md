# Model
This package contains the different **models** used by the
[converters](../converter/README.md).

* Data containers used in the API **requests** with Swagger decorations
  * [ConvertPointsRequest](./ConvertPointsRequest.java)
  * [ConvertTrajectoryRequest](./ConvertTrajectoryRequest.java)
* Data containers for API **responses** with Swagger decorations
  * [ConvertPointsResponse](./ConvertPointsResponse.java)
  * [ConvertTrajectoryResponse](./ConvertTrajectoryResponse.java)
  * [ErrorResponse](./ErrorResponse.java) - error response
* Spatial model API as interfaces; implementations are found in the
[Impl](./Impl/README.md) folder. This folder also contains the
[ItemFactory](./Impl/ItemFactory.java), which takes the de-serialized
persistable references and creates the implementations for the following types.
  * [ICrs](./ICrs.java), base class for coordinate reference systems (CRS).
    * [ILateBoundCrs](./ILateBoundCrs.java) - a simple CRS with Esri well-known text and Esri PE object references.
    * [IEarlyBoundCrs](./IEarlyBoundCrs.java) - a composite CRS binding an ILateBoundCrs to a cartographic transformation (ITrf).
  * [ITrf](./ITrf.java), base class for cartographic transformations (TRF).
    * [ISingleTrf](./ISingleTrf.java) - a simple, single cartographic transformation with Esri well-known text and Esri PE object references.
    * [ICompoundTrf](./ICompoundTrf.java) - an aggregation of single transformations associated with a policy how to be applied.
* Model sub-elements
  * [ConvertOperationState](./ConvertOperationState.java) - state container for the ```convert``` method. It keeps context and collects the information about the operations applied.
  * [Point](./Point.java) - a 3D point with Swagger decoration
  * [ProjectionCorrectionSet](./ProjectionCorrectionSet.java) - a data container for projection distortion and their corrections; not exposed to customers yet.
  * [TrajectoryComputationState](./TrajectoryComputationState.java) - state container for the ```convertTrajectory``` method. It keeps context and collects the information about the operations applied.
  * [TrajectoryStationIn](./TrajectoryStationIn.java) - data container for a trajectory station input (MD, inclination, azimuth, X,Y,Z depending on input mode. Includes Swagger decorations.
* [ReferenceConverter](./ReferenceConverter.java) - class with static methods to parse
persistable references, either Version 1 [v1.model](v1/README.md) or
Version 2 [v2.model](v2/README.md). This class is responsible for figuring
out, which version the incoming persistable reference is encoded with. Then the
de-serialization is delegated to the version specific code. Main methods:
```parseSpatialReference``` and ```parseUnitReference```.
* Enumerations
  * [AzimuthReferenceType](./AzimuthReferenceType.java) - enumerations for azimuth reference.
  * [CRSType](./CRSType.java) - enumerations for spatial types (CRS and TRF)
  * [TrajectoryComputationMethod](./TrajectoryComputationMethod.java) - enumerations for spatial trajectory computations.
  * [WellKnownTextType](./WellKnownTextType.java) - enumerations for Esri object types, i.e. well-known text keywords.

[Back to top-level](../README.md)