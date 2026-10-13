# MeasuredDepthInterval

Specifies where to interpolate additional trajectory stations. Use `md_i` for explicit MD values, or `md_interval` for regular spacing.

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**md_i** | **list[float]** | Explicit list of measured depth values where stations should be interpolated. Units specified by unitMD (or unitZ if not provided). | [optional] 
**md_interval** | **float** | Regular interval for interpolation. Stations generated from 0 to max(MD) at this spacing. | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


