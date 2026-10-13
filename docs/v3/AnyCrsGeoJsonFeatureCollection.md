# AnyCrsGeoJsonFeatureCollection

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**type** | **str** |  | 
**features** | [**list[AnyCrsGeoJsonFeature]**](AnyCrsGeoJsonFeature.md) |  | 
**bbox** | **list[float]** |  | [optional] 
**persistable_reference_crs** | **str** | The spatial context of this AnyCrsGeoJsonFeatureCollection as persistable reference string. | [optional] 
**coordinate_reference_system_id** | **str** | The spatial context of this AnyCrsGeoJsonFeatureCollection as a record ID. If both CoordinateReferenceSystemID and persistableReferenceCrs exist, CoordinateReferenceSystemID takes precedence. | [optional] 
**vertical_unit_id** | **str** | The vertical axis unit for this AnyCrsGeoJsonFeatureCollection as a record ID. If both VerticalUnitID and persistableReferenceUnitZ exist, VerticalUnitID takes precedence. | [optional] 
**persistable_reference_unit_z** | **str** | The vertical axis unit for this AnyCrsGeoJsonFeatureCollection as persistable reference string. | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


