# TrajectoryStationOut

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**md** | **float** | MD (measured depth) from vertical reference point in &#39;unitZ&#39;. | 
**inclination** | **float** | Inclination angle in degrees of arc, 0.0 is vertical, 90.0 is horizontal. | 
**azimuth_tn** | **float** | True North azimuth angle in degrees of arc, 0.0/360.0 is North. | 
**azimuth_gn** | **float** | Grid North azimuth angle in degrees of arc, 0.0/360.0 is North. | 
**dx_tn** | **float** | True E-W deviation in the local Cartesian engineering CRS from the well reference point; unit is given by container&#39;s &#39;unitXY&#39;. | 
**dy_tn** | **float** | True N-S deviation in the local Cartesian engineering CRS from the well reference point; Y is aligned with TrueNorth; unit is given by container&#39;s &#39;unitXY&#39;. | 
**point** | [**Point**](Point.md) | Trajectory station point in trajectoryCRS and vertical unit as defined in container&#39;s &#39;unitZ&#39;. | 
**wgs84_longitude** | **float** | WGS 84 longitude in dega | [optional] 
**wgs84_latitude** | **float** | WGS 84 latitude in dega | [optional] 
**dls** | **float** | Curvature, Dog Leg Severity, measured in &#39;unitDls&#39;. | [optional] 
**original** | **bool** | Original trajectory station if true, interpolated trajectory station if false. | [optional] 
**dz** | **float** |  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


