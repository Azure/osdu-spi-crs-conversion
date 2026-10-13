# swagger_client.CRSConversionApi

All URIs are relative to *https://az-osdu1.evd.csp.slb.com/api/crs/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**convert_bin_grid**](CRSConversionApi.md#convert_bin_grid) | **POST** /v3/convertBinGrid | CRS Convert service is an OSDU platform standard method for QC and conversion of Bin Grids, associated in particular with ingested seismic volumes, that describe the “real world” (Easting, Northing) of bin grid centers at (inline, crossline) local coordinates
[**convert_geo_json**](CRSConversionApi.md#convert_geo_json) | **POST** /v3/convertGeoJson | Convert a GeoJSON or AnyCrsGeoJson structure
[**convert_point**](CRSConversionApi.md#convert_point) | **POST** /v3/convert | Convert a list of points


# **convert_bin_grid**
> ConvertBinGridResponse convert_bin_grid(body=body, data_partition_id=data_partition_id)

CRS Convert service is an OSDU platform standard method for QC and conversion of Bin Grids, associated in particular with ingested seismic volumes, that describe the “real world” (Easting, Northing) of bin grid centers at (inline, crossline) local coordinates

QC check of the “squareness” of a Bin Grid defined using 4 corner points. Coordinate conversion of a Bin Grid to a new CRS and “square it up” (if target CRS is same as original CRS then conversion is omitted, and the squareness test is done in the original CRS). Calculate derived P6 parameters from the input 4 corners. Calculate WGS 84 coordinates at the corners. Returns converted Bin Grid and a QC of squareness of the bin grid

### Example 
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# Configure API key authorization: Bearer
swagger_client.configuration.api_key['Authorization'] = 'YOUR_API_KEY'
# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# swagger_client.configuration.api_key_prefix['Authorization'] = 'Bearer'

# create an instance of the API class
api_instance = swagger_client.CRSConversionApi()
body = swagger_client.ConvertBinGridRequest() # ConvertBinGridRequest |  (optional)
data_partition_id = 'data_partition_id_example' # str |  (optional)

try: 
    # CRS Convert service is an OSDU platform standard method for QC and conversion of Bin Grids, associated in particular with ingested seismic volumes, that describe the “real world” (Easting, Northing) of bin grid centers at (inline, crossline) local coordinates
    api_response = api_instance.convert_bin_grid(body=body, data_partition_id=data_partition_id)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling CRSConversionApi->convert_bin_grid: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**ConvertBinGridRequest**](ConvertBinGridRequest.md)|  | [optional] 
 **data_partition_id** | **str**|  | [optional] 

### Return type

[**ConvertBinGridResponse**](ConvertBinGridResponse.md)

### Authorization

[Bearer](../README.md#Bearer)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **convert_geo_json**
> ConvertGeoJsonResponse convert_geo_json(body=body, data_partition_id=data_partition_id)

Convert a GeoJSON or AnyCrsGeoJson structure

Convert a GeoJSON or AnyCrsGeoJson structure to a specified target CRS. GeoJSON is declared - by definition - to be in the context of WGS 84.

### Example 
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# Configure API key authorization: Bearer
swagger_client.configuration.api_key['Authorization'] = 'YOUR_API_KEY'
# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# swagger_client.configuration.api_key_prefix['Authorization'] = 'Bearer'

# create an instance of the API class
api_instance = swagger_client.CRSConversionApi()
body = swagger_client.ConvertGeoJsonRequest() # ConvertGeoJsonRequest |  (optional)
data_partition_id = 'data_partition_id_example' # str |  (optional)

try: 
    # Convert a GeoJSON or AnyCrsGeoJson structure
    api_response = api_instance.convert_geo_json(body=body, data_partition_id=data_partition_id)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling CRSConversionApi->convert_geo_json: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**ConvertGeoJsonRequest**](ConvertGeoJsonRequest.md)|  | [optional] 
 **data_partition_id** | **str**|  | [optional] 

### Return type

[**ConvertGeoJsonResponse**](ConvertGeoJsonResponse.md)

### Authorization

[Bearer](../README.md#Bearer)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **convert_point**
> ConvertPointsResponse convert_point(body=body, data_partition_id=data_partition_id)

Convert a list of points

Convert a list of points

### Example 
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# Configure API key authorization: Bearer
swagger_client.configuration.api_key['Authorization'] = 'YOUR_API_KEY'
# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# swagger_client.configuration.api_key_prefix['Authorization'] = 'Bearer'

# create an instance of the API class
api_instance = swagger_client.CRSConversionApi()
body = swagger_client.ConvertPointsRequest() # ConvertPointsRequest |  (optional)
data_partition_id = 'data_partition_id_example' # str |  (optional)

try: 
    # Convert a list of points
    api_response = api_instance.convert_point(body=body, data_partition_id=data_partition_id)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling CRSConversionApi->convert_point: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**ConvertPointsRequest**](ConvertPointsRequest.md)|  | [optional] 
 **data_partition_id** | **str**|  | [optional] 

### Return type

[**ConvertPointsResponse**](ConvertPointsResponse.md)

### Authorization

[Bearer](../README.md#Bearer)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

