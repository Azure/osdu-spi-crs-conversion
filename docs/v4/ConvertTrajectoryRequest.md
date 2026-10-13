# ConvertTrajectoryRequest

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**trajectory_crs** | **str** | Coordinate reference system for the reference point; typically the CRS is a projected CRS; if a geographic CRS is provided, the unitXY must be defined and the azimuthReference must be TrueNorth. The property value could be a persistable reference string or a record ID | 
**azimuth_reference** | **str** | Azimuth reference for the input trajectory station azimuth values (TrueNorth or GridNorth) | 
**unit_xy** | **str** | The horizontal unit of the dx, dy in the input trajectory stations; the unit must be a length unit in persistable reference or a record ID, see example. | [optional] 
**unit_z** | **str** | The vertical unit of the dz in the input trajectory stations; the unit could be a persistable reference or a record ID | 
**reference_point** | [**Point**](Point.md) | The 3D reference point in the &#39;trajectoryCRS&#39; where MD&#x3D;&#x3D;0. | 
**input_stations** | [**list[TrajectoryStationIn]**](TrajectoryStationIn.md) | The array of input trajectory stations | 
**method** | **str** | The computation method - &#39;AzimuthalEquidistant&#39; (default) or &#39;LMP&#39; (Lee&#39;s modified proposal SPE96813) | 
**input_kind** | **str** | The kind of input; one of MD_Incl_Azim (default), MD_X_Y_Z, MD_dX_dY_dZ, X_Y_Z, dX_dY_dZ. MD stands for measured depth; MD_X_Y_Z/X_Y_Z stand for absolute coordinates in the reference CRS, MD_dX_dY_dZ/dX_dY_dZ stand for deviations relative to the reference point. | [optional] 
**interpolate** | **bool** | Perform trajectory interpolation on demand; default is true. | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


