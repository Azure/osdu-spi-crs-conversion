# ConvertTrajectoryResponseV4

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**trajectory_crs** | **str** | Coordinate reference system for the computed trajectory points (same as input trajectoryCRS). | required
**unit_xy** | **str** | Unit of measure for horizontal displacements (dxTN, dyTN) in output trajectory stations. | required
**unit_z** | **str** | Unit of measure for vertical displacement (dZ) and point.z in output trajectory stations. | required
**unit_dls** | **str** | Unit of measure for dog leg severity (DLS) values: deg/100ft (non-metric) or deg/30m (metric). | required
**unit_md** | **str** | Unit of measure for Measured Depth (MD) values. Defaults to unitZ if not specified in request. | [optional] 
**stations** | [**list[TrajectoryStationOut]**](TrajectoryStationOut.md) | Computed trajectory stations from original input stations. | required
**local_crs** | **str** | Local Azimuthal Equidistant CRS centered at the reference point, True North oriented. | required
**method** | **str** | Computation method used: AzimuthalEquidistant or LMP. | required
**operations_applied** | **list[str]** | List of operations performed during computation (for debugging/auditing). | [optional] 
**stations_i** | [**list[TrajectoryStationOut]**](TrajectoryStationOut.md) | Interpolated trajectory stations at MD_i depths (only present when interpolation was requested). | [optional] 
**scale_convergence_list** | [**list[ScaleConvergence]**](ScaleConvergence.md) | Scale factor and convergence values at first and last stations. | [optional] 
**input_kind** | **str** | The kind of input used (echoed from request). | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


