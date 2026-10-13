# Main entry points crs-converter
* [CRSApplication](./CRSApplication.java) does the initial setup and creates the singletons, implementations of the
  * [ICRSConverter](../interfaces/ICRSConverter.java) - convert/transform a list of points between two defined CRSs
  * [ITrajectoryConverter](../interfaces/ITrajectoryConverter.java) - trajectory computation and spatially correct placement
  * [IPointConverter](../interfaces/IPointConverter.java) - array handling 'utility'
* [CRSApi](CRSApi.java) takes the implementations of three implementations from
[CRSApplication](./CRSApplication.java) handling ```/convert``` and ```/convertTrajectory```
* [HealthCheck](HealthCheck.java) handles ```/liveness_check``` and ```/readiness_check```

[Back to top-level](../README.md)