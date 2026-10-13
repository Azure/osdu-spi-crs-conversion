# swagger_client.InfoApiApi

All URIs are relative to *https://az-osdu1.evd.csp.slb.com/api/crs/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**info_using_get**](InfoApiApi.md#info_using_get) | **GET** /v2/info | info


# **info_using_get**
> VersionInfo info_using_get(data_partition_id)

info

### Example 
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = swagger_client.InfoApiApi()
data_partition_id = 'opendes' # str | tenant (default to opendes)

try: 
    # info
    api_response = api_instance.info_using_get(data_partition_id)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling InfoApiApi->info_using_get: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **data_partition_id** | **str**| tenant | [default to opendes]

### Return type

[**VersionInfo**](VersionInfo.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

