# ConvertGeoJsonResponse

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**total_count** | **int** | The total number of coordinates in the GeoJSON FeatureCollection or AnyCrsFeatureCollection. | [optional] 
**success_count** | **int** | The number of coordinates in the GeoJSON FeatureCollection or AnyCrsFeatureCollection successfully converted/transformed. If this number is less than totalCount then conversion/transformation errors have occurred. | [optional] 
**feature_collection** | [**AnyCrsGeoJsonFeatureCollection**](AnyCrsGeoJsonFeatureCollection.md) |  | [optional] 
**operations_applied** | **list[str]** | The list of operations, which have been applied to the points | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


