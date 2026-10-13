# ConvertTrajectoryRequestV4

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**trajectory_crs** | **str** | Coordinate reference system for the reference point; typically a projected CRS. If a geographic CRS is provided, unitXY must be defined and azimuthReference must be TRUE_NORTH. | required
**azimuth_reference** | **str** | Reference direction for interpreting azimuth values: TRUE_NORTH (TN) or GRID_NORTH (GN). | [optional]
**unit_xy** | **str** | Unit of measure for horizontal displacements (dx, dy). For projected CRS, derived automatically if not specified. | [optional] 
**unit_z** | **str** | Unit of measure for vertical values (dz, measured depth). | required
**reference_point** | [**Point**](Point.md) | The 3D reference point in the trajectoryCRS where MD==0. | required
**input_stations** | [**list[TrajectoryStationInV4]**](TrajectoryStationInV4.md) | The array of input trajectory stations. | required
**method** | **str** | Computation method: AzimuthalEquidistant (default) or LMP (Lee's Modified Proposal SPE96813). | required
**input_kind** | **str** | Format of input data: MD_Incl_Azim (default), MD_dX_dY_dZ, dX_dY_dZ, MD_Incl, MD_X_Y_Z, or X_Y_Z. | [optional] 
**interpolate** | **bool** | When true (default), interpolates additional stations at MD values in MD_i. When false, only original stations are processed. | [optional] 
**md_i** | [**MeasuredDepthInterval**](MeasuredDepthInterval.md) | Specifies where to interpolate (explicit MD list or regular interval). Only used when interpolate=true. | [optional] 
**unit_md** | **str** | Unit of measure for Measured Depth (MD) values. Optional - defaults to unitZ if not specified. | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


