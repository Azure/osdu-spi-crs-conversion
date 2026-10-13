# ConvertTrajectoryResponse

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**trajectory_crs** | **str** | Coordinate reference system for the reference point; typically the CRS is a projected CRS; if a geographic CRS is provided, the unitXY must be defined and the azimuthReference must be TrueNorth. | 
**unit_xy** | **str** | The horizontal unit of the dx, dy in the output trajectory stations. | [optional] 
**unit_z** | **str** | The vertical unit of the dz in the output trajectory stations. | 
**unit_dls** | **str** | The unit of the dog leg severity (DLS) in the output trajectory stations. | 
**stations** | [**list[TrajectoryStationOut]**](TrajectoryStationOut.md) | Computed trajectory stations. | 
**local_crs** | **str** | Coordinate Reference System for the local, True North oriented, true distance, engineering CRS with origin at the well&#39;s surface location. | 
**method** | **str** | The computation method used - &#39;AzimuthalEquidistant&#39; (default) or &#39;LMP&#39; (Lee&#39;s modified proposal SPE96813). | 
**operations_applied** | **list[str]** | The list of operations, which have been applied to the points | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


