# TrajectoryStationIn

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**md** | **float** | MD (measured depth) from vertical reference point in &#39;unitZ&#39;. | 
**inclination** | **float** | Inclination angle in degrees of arc, 0.0 is vertical, 90.0 is horizontal. | 
**azimuth** | **float** | Azimuth angle in degrees of arc, 0.0/360.0 is North; reference given by azimuthReference (TrueNorth or GridNorth). | 
**dx** | **float** | E-W deviation in the local Cartesian engineering CRS from the well reference point; unit is given by container&#39;s &#39;unitXY&#39; or projected &#39;trajectoryCRS&#39;. | [optional] 
**dy** | **float** | N-S deviation in the local Cartesian engineering CRS from the well reference point; Y is aligned with azimuth reference (TrueNorth or projected GridNorth); unit is given by container&#39;s &#39;unitXY&#39; or projected &#39;trajectoryCRS&#39;. | [optional] 
**dz** | **float** | True vertical deviation in the local Cartesian engineering CRS from the well reference point; unit is given by container&#39;s unitZ; downwards positive. | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


