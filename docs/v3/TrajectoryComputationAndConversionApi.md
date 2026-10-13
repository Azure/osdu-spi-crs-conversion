# swagger_client.TrajectoryComputationAndConversionApi

All URIs are relative to *https://az-osdu1.evd.csp.slb.com/api/crs/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**convert_trajectory**](TrajectoryComputationAndConversionApi.md#convert_trajectory) | **POST** /v3/convertTrajectory | Convert trajectory stations


# **convert_trajectory**
> ConvertTrajectoryResponse convert_trajectory(body=body, data_partition_id=data_partition_id)

Convert trajectory stations

Convert a list of trajectory stations, given the unit and spatial context and a reference point in 3D where MD==0.

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
api_instance = swagger_client.TrajectoryComputationAndConversionApi()
body = swagger_client.ConvertTrajectoryRequest() # ConvertTrajectoryRequest |  (optional)
data_partition_id = 'data_partition_id_example' # str |  (optional)

try: 
    # Convert trajectory stations
    api_response = api_instance.convert_trajectory(body=body, data_partition_id=data_partition_id)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling TrajectoryComputationAndConversionApi->convert_trajectory: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**ConvertTrajectoryRequest**](ConvertTrajectoryRequest.md)|  | [optional] 
 **data_partition_id** | **str**|  | [optional] 

### Return type

[**ConvertTrajectoryResponse**](ConvertTrajectoryResponse.md)

### Authorization

[Bearer](../README.md#Bearer)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

