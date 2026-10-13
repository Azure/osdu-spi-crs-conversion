# ConvertGeoJsonRequest

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**feature_collection** | [**AnyCrsGeoJsonFeatureCollection**](AnyCrsGeoJsonFeatureCollection.md) |  | 
**to_crs** | **str** | The GeoJSON FeatureCollection or AnyCrsFeatureCollection structure to be converted/transformed. GeoJSON is always based on WGS 84; AnyCrsFeatureCollection carries the CRS context in the persistableReferenceCrs property | 
**to_unit_z** | **str** | The vertical axis unit for this AnyCrsGeoJsonFeatureCollection as persistable reference string. | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


