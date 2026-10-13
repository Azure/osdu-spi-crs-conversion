# API Definitions
This folder contains the service API interface definitions, which either are or
may be relevant for the public API.

## Most important, used by the service
* [ICRSConverter](./ICRSConverter.java) - the interface for the ```/convert``` point conversion service, simply declaring: \
```
int convertPoint(String from, String to, double[] xyCoordinates, double[] zCoordinates);
```
* [ITrajectoryConverter](ITrajectoryConverter.java) - the interface for the ```convertTrajectory``` trajectory calculation and processing service, simply declaring:
```
ConvertTrajectoryResponse convertTrajectory(ConvertTrajectoryRequest request);
```

## Candidate
* [IAzimuthCorrector](IAzimuthCorrector.java) - not yet published API, but this is internally used by the trajectory converter.
```
int correctAzimuth(String crs, String azimuthReference, double[] xyCoordinates, double[] azimuths);
```

## Internal only
* [IPointConverter](IPointConverter.java) - helper interface to manipulate arrays sent to the Esri engine.

[Back to top-level](../README.md)