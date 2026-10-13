# ConvertPointsResponse

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success_count** | **int** | Number of points successfully converted. If the number is less than the request array length conversion/transformation failures occurred. | [optional] 
**points** | [**list[Point]**](Point.md) | Converted points; length and order of the array is the same as in the request. Points, which failed to convert, are returned as NaN. | [optional] 
**operations_applied** | **list[str]** | The list of operations, which have been applied to the points | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


