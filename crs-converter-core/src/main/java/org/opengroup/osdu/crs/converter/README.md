# API Implementations
This folder contains the service API interface implementation, which either are or
may be relevant for the public API.

## Most important, used by the service
* [CRSConverter](./CRSConverter.java) - the implementation for the ```/convert``` point conversion service, simply declaring: \
```
ConvertPointsResponse convertPoint(String from, String to, double[] xyCoordinates, double[] zCoordinates);
```
* [TrajectoryConverter](./TrajectoryConverter.java) - the implementation for the ```convertTrajectory``` trajectory calculation and processing service, simply declaring:
```
ConvertTrajectoryResponse convertTrajectory(ConvertTrajectoryRequest request);
```

## Candidate
* [AzimuthCorrector](./AzimuthCorrector.java) - not yet published API, but this is internally used by the trajectory converter.
```
int correctAzimuth(String crs, String azimuthReference, double[] xyCoordinates, double[] azimuths);
```

## Internal only
* [PointConverter](./PointConverter.java) - helper implementation to manipulate arrays sent to the Esri engine.

[Back to top-level](../README.md)