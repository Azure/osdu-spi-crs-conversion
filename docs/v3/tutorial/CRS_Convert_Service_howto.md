# CRS Convert helper service tutorial

*The OSDU has two CRS helper services: "`CRS Convert`" and "`CRS Catalog`".
This tutorial provides examples and background to help
application developers accomplish typical tasks using the "`CRS Convert`" endpoints.


**Revision Log**

| **Version** | **Reason for change** | **Author** | **Date**   |
|-------------|-----------------------|------------|------------|
| 1.0         | Initial version for Convert v3     | Geomatics Integration  | 2022-06-15 |
| 1.1         | Added convertBinGrid               |                        | 2022-11-30 |


**Table of Contents**

* [1. Introduction](#1-introduction)  
* [2. CRS Convert Overview](#2-crs-convert-overview)  
* [3. Check if the service is running](#3-check-if-the-service-is-running)  
* [4. Performing coordinate operations](#4-performing-coordinate-operations)  
  * [4.1 Context](#41-context)  
    * [4.1.1 Known issues](#411-known-issues)  
  * [4.2 Converting a list of points from one CRS to another](42-converting-a-list-of-points-from-one-crs-to-another)  
    * [4.2.1 Simple example with POST /convert](#421-simple-example-with-post-convert)  
    * [4.2.2 Simple example with POST /convertGeoJson](#422-simple-example-with-post-convertgeojson)  
    * [4.2.3 Correctness and performance tests](#423-correctness-and-performance-tests)  
* [5. Computing a wellbore trajectory from directional survey data](#5-computing-a-wellbore-trajectory-from-directional-survey-data)  
  * [5.1 Basic example](#51-basic-example)  
  * [5.2 Realistic example](#52-realistic-example)  
    * [5.2.1 Python script to help generate the Request for test data](#521-python-script-to-help-generate-the-request-for-test-data)  
    * [5.2.2 Correctness and performance](#522-correctness-and-performance)  
  * [5.3 “Unscaling” the calculated wellbore path](#53-unscaling-the-calculated-wellbore-path)  
  * [5.4 A trick to get scale and convergence at any location](#54-a-trick-to-get-scale-and-convergence-at-any-location)  
* [6. Wellbore interpolation on MD](#6-wellbore-interpolation-on-md)  
* [7. QC and Convert Bin Grid](#7-qc-and-convert-bin-grid)


# 1. Introduction

-   This "How To" tutorial is intended as quick start for developers by
    describing common tasks with examples how to accomplish them (and
    background not easily described in swagger documentation or postman
    collections).
-   The [Apache SIS - The Apache SIS™ library](https://sis.apache.org/) is
    used in OSDU as public domain geodetic engine. The correctness of this
    engine has been assessed by OSDU Geomatics Integration workstream using
    the GIGS framework (to link to report).
-   CRS definitions are centrally managed as reference data. The CRS record-id is critical.


# 2. CRS Convert Overview

Applications built on top of OSDU require coordinates to be converted from
one CRS to another, e.g., for normalization to a global CRS (WGS 84),
or for delivery in a common CRS to work in a project.

The CRS Convert service enables such conversions (/transformations) of coordinates.

The endpoints for CRS Convert v3 {{osduonaws_base_url}}/api/crs/converter/ are fully specified via 
the [Swagger documentation](https://community.opengroup.org/osdu/platform/system/reference/crs-conversion-service/-/blob/master/docs/v3/api_spec/crs_converter_openapi.json). 
Its endpoints are:

<table>
<colgroup>
<col style="width: 10%" />
<col style="width: 22%" />
<col style="width: 65%" />
</colgroup>
<thead>
<tr class="header">
<th><strong>Method</strong></th>
<th><strong>Endpoint</strong></th>
<th><strong>Description</strong></th>
</tr>
</thead>
<tbody>
<tr class="even">
<td>GET</td>
<td><strong>.../actuator/health</strong></td>
<td>Checks if service is up and running.</td>
</tr>
<tr class="odd">
<td>GET</td>
<td><strong>.../v3/info</strong></td>
<td>Provides info, including of underlying geodetic engine built.</td>
</tr>
<tr class="even">
<td>POST</td>
<td><strong>.../v3/convert</strong></td>
<td><p>Convert coordinates from one CRS to another.</p>
<p>CT is taken from the BoundCRS.</p></td>
</tr>
<tr class="odd">
<td>POST</td>
<td><strong>.../v3/convertGeoJson</strong></td>
<td><p>Convert coordinates from one CRS to another.</p>
<p>Same as /convert but the parameters in the request/response bodies
are GeoJson (WGS 84) or AnyCrsGeoJson structure as stored in
AbstractSpatialLocation.</p></td>
</tr>
<tr class="even">
<td>POST</td>
<td><strong>.../v3/convertTrajectory</strong></td>
<td>Compute the wellbore trajectory based on directional survey observables.</td>
</tr>
<tr class="even">
<td>POST</td>
<td><strong>.../v3/convertBinGrid</strong></td>
<td>Convert a bin grid to a new CRS, square it up, and provide a measure of the non-orthogonality.
observables.</td>
</tr>
</tbody>
</table>

2D and 3D locations are expressed using coordinates that are (required to be) associated with a CRS.
* [Chapter 4 of the Guide](https://community.opengroup.org/osdu/data/data-definitions/-/blob/master/Guides/Chapters/04-FrameOfReference.md) 
explains how the CRS is stored in OSDU.
* [AbstractSpatialLocation](https://community.opengroup.org/osdu/data/data-definitions/-/blob/master/Generated/abstract/AbstractSpatialLocation.1.1.0.json) holds the coordinates of points and geometries such as Points, Lines and Polygons.
* [AbstractFeatureCollection](https://community.opengroup.org/osdu/data/data-definitions/-/blob/master/Generated/abstract/AbstractFeatureCollection.1.0.0.json) 
and [AnyCrsFeatureCollection](https://community.opengroup.org/osdu/data/data-definitions/-/blob/master/Generated/abstract/AbstractAnyCrsFeatureCollection.1.1.0.json)
hold the coordinates in a GeoJson (like) structures for WGS 84 (lat,lon) and arbitrary CRS.


## Axes (order) definition

The API assumes "XYZ" order, positive east, north, up.

The context, i.e., the measurement and unit associated with the axes,
is given by the CRS definitions. In most of the cases, the CRS definition
is 2D. In both the geographic and projected CRS types, the Z-axis is passed through unchanged, and its unit is
only known to the client.


Axis|CRS type|Measurement|Unit|Sign and Directions
----|--------|-----------|----|---------------
x| Geographic 2D CRS| (geodetic) longitude ```Plane_Angle``` |from CRS| positive values ```E```, negative values ```W``` hemisphere
y| Geographic 2D CRS| (geodetic) latitude  ```Plane_Angle``` |from CRS| positive values ```N```, negative values ```S``` hemisphere
z| Vertical CRS| ```Length``` |pass-through | typically elevation relative to an implicit Mean Sea Level surface
x| Projected CRS| ```Length``` |from CRS| direction given by the CRS, normally easting
y| Projected CRS| ```Length``` |from CRS| direction given by the CRS, normally northing
z| Vertical CRS| ```Length``` |pass-through| typically elevation relative to an implicit Mean Sea Level surface



# 3. Check if the service is running

The GET /info and /actuator/health endpoints are self-explanatory and can be used to check the version and if the service is up.

_{{osduonaws_base_url}}/api/crs/converter/actuator/health_ response is
```json
{
    "status": "UP",
    "groups": [
        "liveness",
        "readiness"
    ]
}
```


_{{osduonaws_base_url}}/api/crs/converter/v3/info_ response is like
```json
{
    "groupId": "org.opengroup.osdu.crs-converter-service",
    "artifactId": "crs-converter-aws",
    "version": "0.16.2-SNAPSHOT",
    "buildTime": "2022-08-18T20:58:49.957Z",
    "branch": "refs/heads/release/r3-m13",
    "commitId": "Id",
    "commitMessage": "Update version of release branch to 0.16.2-SNAPSHOT",
    "connectedOuterServices": []
}
```


# 4. Performing coordinate operations

*Use case: As a user I want to convert coordinates from one CRS to
another, e.g., to normalize to WGS 84 or to provide the geographic
coordinates in the base geographic CRS of a projected CRS.*

## 4.1 Context 

- OSDU uses the term “CRS convert” for any operation that changes
  coordinates. In Geodesy (ISO 19111) the term **coordinate conversion** means
  operations with equations with zero or more
  <u>defined</u> parameters. Typically these are map projections and conversions
  from geocentric to geographic coordinates. The term **coordinate transformation**
  is used when a change of datum is enacted, which is based on 
  <u>empirically</u> derived parameters.  
  The name for the service "CRS conversion" is a misnomer, because not the CRS but the coordinates are converted.

- Coordinate operations (i.e., both conversions and transformations) are commonly
  needed in workflows. A coordinate operation has an input
  CRS and an output CRS. In case that there is a change of datum then
  there is the issue of transformation multiplicity, i.e., various
  coordinate transformations can be used and a common issue is how to
  select the appropriate CT for the given data? In the OSDU this is
  resolved by using a BoundCRSs and the "WGS 84 hub" concept 
  (see [IOGP Guidance Note 373-07-1](epsg.org) for detail). 
  This means that **when data is ingested it must be associated with a BoundCRS**
  and the BoundCRS must have a transform to WGS 84 (and not some other binding) - unless the data is already (based on) WGS 84.

- The geodetic engine relies on the actionable explicit definition of the (Bound)
  CRS in the form of a `PersistableReference` string (a.k.a.
  “*stringified ESRI WKT1*”). If the WKT is not correct or the method is
  not supported by Apache SIS then the convert operation will fail. An example for a
  BoundProjected CRS is (where the variable {{NAMESPACE}} is generally “osdu”):

```
"id": "{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::32248_EPSG::1237"
```

```
"PersistableReference": "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"32248001\"},
\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32248\"},\"name\":\"WGS_1972_UTM_Zone_48N\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"WGS_1972_UTM_Zone_48N\\\",GEOGCS[\\\"GCS_WGS_1972\\\",DATUM[\\\"D_WGS_1972\\\",SPHEROID[\\\"WGS_1972\\\",6378135.0,298.26]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",105.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",32248]]\"},\"name\":\"WGS 72 * DMA1 / UTM zone 48N [32248,1237]\",
\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1237\"},\"name\":\"WGS_1972_To_WGS_1984_1\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"WGS_1972_To_WGS_1984_1\\\",GEOGCS[\\\"GCS_WGS_1972\\\",DATUM[\\\"D_WGS_1972\\\",SPHEROID[\\\"WGS_1972\\\",6378135.0,298.26]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.0],PARAMETER[\\\"Y_Axis_Translation\\\",0.0],PARAMETER[\\\"Z_Axis_Translation\\\",4.5],PARAMETER[\\\"X_Axis_Rotation\\\",0.0],PARAMETER[\\\"Y_Axis_Rotation\\\",0.0],PARAMETER[\\\"Z_Axis_Rotation\\\",0.554],PARAMETER[\\\"Scale_Difference\\\",0.2263],OPERATIONACCURACY[2.0],AUTHORITY[\\\"EPSG\\\",1237]]\"},
\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
```



- Actually, the situation is slightly more subtle for Apache SIS. The
  OSDU api/SIS engine does not blindly interpret the provided explicit
  definition, but attempts to match the EPSG code with its internal library:

  - First the authCode is extracted for the lateBoundCRS and singleCT.
    In the above BoundCRS example these are:

    - `\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32248\"}`

    - `\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1237\"}`

    - If the SIS engine can find the codes (32248 and/or 1237) in its
      internal library then it will use this internal definition. If
      not, then the WKT is passed on. If the WKT is not understood then
      an error status is returned.

    - Hence the OSDU implementation using SIS is actually a mix of
      an implicit identification based on EPSG code and an explicit
      definition using the parameters stored in the OSDU
      PersistableReference string in ESRI WKT1 format.

  - If the input and output are on the same datum with the same CT then
    no CT is executed (that would be unnecessary) - i.e., this is for
    example to compute a map projection.

### **4.1.1 Known issues**

- An issue was identified in M3.R12 (2022.1); namely all CTs in the OSDU
  reference data for BoundCRSs must be defined “to WGS 84” (as opposed
  to “from WGS 84”). This is theoretically not guaranteed in the EPSG
  Dataset which is the basis for OSDU. However, in practice the potential impact is small because
  practically all CTs that are in use are defined as “to WGS 84”. There are a few
  transformations that are not, but those are generally EPSG null
  transformations, i.e., there is no difference whether it is “to” or
  “from” WGS 84. 
  Nonetheless this is an issue with the design, and it
  will become a larger issue if desired direct operations are enabled,
  not through the hub WGS 84.

  - No check is done in EpsgManifestGenerator that the input/output
    systems are “to WGS 84” (and not “from WGS 84”). The issue can be
    solved in Apache SIS or in the Reference Data. At the moment the CRS
    Convert service calls Apache SIS simply assuming the operation is
    defined with WGS 84 as target, and it reverses the operation if it
    has to convert “from WGS 84”.

- The manifests stored in OSDU for BoundCRSs (and other CRSs and CTs)
  are created by the EpsgManifestGenerator.py program. This uses a
  lookup file to fetch the PersistableReference for a given CRS or CT
  definition. If the lookup fails then it attempts to create custom WKT
  dynamically.

  - R3.M13 was the first release in which Apache SIS could read custom
    (explicitly generated) GEOGTRAN cards.

  - There is a known issue that for CT the dynamically generated WKT
    names are not “ESRI compliant” (i.e., if not read from the lookup).
    The engine will assume that the CT is defined “to WGS 84” and hence
    the name is irrelevant. However, for direct transformations this
    will pose an issue that is not trivial to solve (see next item).

- Direct transformations are not supported in R3.M13. To convert from
  CRS A to CRS B both need to be Bound to WGS 84, and the transformation
  is a daisy chained path through that hub system (i.e., from CRS A to
  WGS 84 and then from WGS 84 to CRS B).

  - *An issue is logged for this gap, i.e., to provide a function to enable
    a direct transformation. When this gets implemented it is important
    to consider how to recognize the source and target defined in the CT
    (i.e., whether the CT is defined from A to B or from B to A). This
    should be done based on the code of the CRSs, but since SIS receives
    the ESRI WKT it can only be done on the GEOGCS names. Hence it is
    important that the GEOGCS names used in the GEOGTRAN (CT) are
    consistent with the GEOGCS names in the CRSs.*

- All horizontal transformations are assumed to have Geographic2D as
  source and target. This in general is how the EPSG Dataset models it,
  but not always. This is considered a feature of OSDU and simply a
  limitation. If in future any direct transformations to geocentric
  systems or between projected systems are needed this may need to be
  enhanced.

  - No check is done in EpsgManifestGenerator that the input/output
    systems are geog2d.

- Vertical transforms are not supported in R3.M13.

- Grid files need to be configured (stored) with the SIS engine. There
  is a directory *DatumChanges*, in the SIS_DATA directory that contain
  any mapping files (including NTv2 files).

  - This is briefly described in the [CRS Conversion Service repo](https://community.opengroup.org/osdu/platform/system/reference/crs-conversion-service/-/tree/master/apachesis_setup).

  - Note that the ESRI WKT1 format is used to describe the filename of a grid file method. 
    The generated ESRI WKT1 for grid files is not understood by Apache
    SIS. In general this will not have an impact when the EPSG code is
    used and recognized by SIS because then SIS will utilize the configured
    grid files per the definition in its internal library (linked to the
    given EPSG code). If for some reason Apache SIS would not recognize
    the EPSG code for a grid file based method then it will not be able
    to understand which grid file to use - even if that file would have
    been deployed in the proper location. This is a small risk to
    operators who want to use a recently released (horizontal or
    vertical) grid file in their projects, because somehow SIS will need
    to be configured for it (the file copied and the CT defined).

## 4.2 Converting a list of points from one CRS to another

There are two endpoints that can be used to convert a list of points:

1.  POST /convert

2.  POST /convertGeoJson

The difference lies only in the structures used in the request (and
response). The GeoJson variant can deal with (an
`AbstractSpatialLocation`) schema fragment, whereas the simple convert
requires a list as input.

Both endpoints can (since R3.M12) as input parameter recognize either
the `record id` of stored geodetic reference data and unit of measure, or
`Persistable Reference`. It will be clear from the above example that the
`record id` is the more readable variant. If the `record id` is provided
then the service retrieves the `PersistableReference` from the reference
data based on the id. Hence for high-performance reasons it may be
considered to pass on the `Persistable Reference`, though for list of
points there will not be a noticeable difference.

### 4.2.1 Simple example with POST /convert

A simple example to convert a single point is provided below. The input is an array of 3D points.

**Request** _{{osduonaws_base_url}}/api/crs/converter/v3/convert_

```json
{
  "fromCRS" : "{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::28992_EPSG::1672:",
  "toCRS" : "{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32632:",
  "points" : [ {
    "x" : 400000,
    "y" : 190000,
    "z" : 0
  } ]
}
```

**Response**
```json
{
    "successCount": 1,
    "points": [
        {
            "x": 484074.5862320617,
            "y": 5499819.191561815,
            "z": 0.0
        }
    ],
    "operationsApplied": [
        "conversion from RD_New to GCS_Amersfoort; 1 points converted",
        "transformation GCS_Amersfoort to GCS_WGS_1984 using Amersfoort_To_WGS_1984_2; 1 points successfully transformed",
        "conversion from GCS_WGS_1984 to WGS_1984_UTM_Zone_32N; 1 points converted"
    ]
} 
```



**Alternatively using PersistableReference as input**

Alternatively, the request body could have specified the
`PersistableReference` instead of the `record ids`. That call would execute
slightly faster because when the `record id` is provided the API creates
two queries to retrieve the `PersistableReference` from the reference
data.

```json
"fromCRS": "{\"authCode\":{\"auth\":\"osdu\",\"code\":\"28992002\"},
    \"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"28992\"},\"name\":\"RD_New\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",
    \"wkt\":\"PROJCS[\\\"RD_New\\\",GEOGCS[\\\"GCS_Amersfoort\\\",DATUM[\\\"D_Amersfoort\\\",SPHEROID[\\\"Bessel_1841\\\",6377397.155,299.1528128]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Double_Stereographic\\\"],PARAMETER[\\\"False_Easting\\\",155000.0],PARAMETER[\\\"False_Northing\\\",463000.0],PARAMETER[\\\"Central_Meridian\\\",5.38763888888889],PARAMETER[\\\"Scale_Factor\\\",0.9999079],PARAMETER[\\\"Latitude_Of_Origin\\\",52.15616055555555],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",28992]]\"},
    \"name\":\"Amersfoort / RD New [1672_28992]\",
    \"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1672\"},\"name\":\"Amersfoort_To_WGS_1984_2\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"Amersfoort_To_WGS_1984_2\\\",GEOGCS[\\\"GCS_Amersfoort\\\",DATUM[\\\"D_Amersfoort\\\",SPHEROID[\\\"Bessel_1841\\\",6377397.155,299.1528128]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Coordinate_Frame\\\"],PARAMETER[\\\"X_Axis_Translation\\\",565.04],PARAMETER[\\\"Y_Axis_Translation\\\",49.91],PARAMETER[\\\"Z_Axis_Translation\\\",465.84],PARAMETER[\\\"X_Axis_Rotation\\\",0.4093943874392368],PARAMETER[\\\"Y_Axis_Rotation\\\",-0.3597051956143113],PARAMETER[\\\"Z_Axis_Rotation\\\",1.868491000350572],PARAMETER[\\\"Scale_Difference\\\",4.0772],OPERATIONACCURACY[1.1],AUTHORITY[\\\"EPSG\\\",1672]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",

"toCRS": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32632\"},\"name\":\"WGS_1984_UTM_Zone_32N\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",
    \"wkt\":\"PROJCS[\\\"WGS_1984_UTM_Zone_32N\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",9.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",32632]]\"}",
```

**What actually happened?**

The source CRS reference, ```fromCRS```, is a projected CRS "Amersfoort / RD New" with EPSG CRS code 28992, "bound" to a coordinate transformation to *WGS 84*, "Amersfoort to WGS 84 (2)", EPSG CT code 1672.

The target CRS reference, ```toCRS```, is a projected CRS "WGS 84 / UTM zone 32N", EPSG CRS code 32632.
This CRS is based on the geographic CRS *WGS 84*, which is different from the source CRS (hence, a transformation will be needed).

1. The ```fromCRS``` is a projected CRS; the first step is to convert the input projected coordinates (easting, northing)
from the projected ```fromCRS``` to its base geographic CRS (yielding geographic coordinates latitude, longitude).
1. Step 2 is to transform the ```points``` from the geographic CRS, *Amersfoort* to *WGS 84* using
the given coordinate transformation ```EPSG,1672```. The intermediate result is in *WGS 84* (latitude, longitude).
1. Step 3 is to transform the intermediate coordinates to the target datum. Step 3 is skipped in this example, because there is no need to transform the intermediate points from *WGS 84* to the final target geographic CRS (the target is already *WGS 84* in this example).  If the target would have been in a different datum, e.g., "ETRS89 / UTM zone 32N" then this step would have been needed, where the transformation "ETRS89 to WGS 84" would have executed in its reverse direction.  This requires the targetCRS to be of "Bound" type, such that the transformation path can be created with "WGS 84" acting as the "hub".
1. Convert (project) the point from *WGS 84" geographic coordinates to "WGS 84 / UTM zone 32N" projected coordinates.

The human readable summary of this is captured in the `operationsApplied` block:
```
    "operationsApplied": [
        "conversion from RD_New to GCS_Amersfoort; 1 points converted",
        "transformation GCS_Amersfoort to GCS_WGS_1984 using Amersfoort_To_WGS_1984_2; 1 points successfully transformed",
        "conversion from GCS_WGS_1984 to WGS_1984_UTM_Zone_32N; 1 points converted"
    ]
```

__Note the terminology:__
* Conversion: an operation between a projected CRS and its base geographic CRS.
* Transformation: an operation between different geographic CRSs (colloquially referred to as datums).

**Failures and partial failures**

Coordinate conversions and transformations can fail.

A ```successCount``` less than the input list length indicates that
some of the points failed to convert/transform (it is typically the
cartographic transformation failing). Points, which failed to convert/transform,
are returned as "NaN"

<details><summary>Request leading to partial success example</summary>

```json
{
  "fromCRS": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"GCS_WGS_1984\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4326]]\"}",
  "toCRS": "{\"authCode\":{\"auth\":\"SLB\",\"code\":\"20256017\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"20256\"},\"name\":\"AGD_1966_AMG_Zone_56\",\"type\":\"LBC\",\"ver\":\"PE_10_3_1\",\"wkt\":\"PROJCS[\\\"AGD_1966_AMG_Zone_56\\\",GEOGCS[\\\"GCS_Australian_1966\\\",DATUM[\\\"D_Australian_1966\\\",SPHEROID[\\\"Australian\\\",6378160.0,298.25]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",10000000.0],PARAMETER[\\\"Central_Meridian\\\",153.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",20256]]\"},\"name\":\"AGD66 * OGP-Aus 0.1m / AMG zone 56 [20256,15786]\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15786\"},\"name\":\"AGD_1966_To_WGS_1984_17_NTv2\",\"type\":\"ST\",\"ver\":\"PE_10_3_1\",\"wkt\":\"GEOGTRAN[\\\"AGD_1966_To_WGS_1984_17_NTv2\\\",GEOGCS[\\\"GCS_Australian_1966\\\",DATUM[\\\"D_Australian_1966\\\",SPHEROID[\\\"Australian\\\",6378160.0,298.25]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NTv2\\\"],PARAMETER[\\\"Dataset_australia/A66_National_13_09_01\\\",0.0],AUTHORITY[\\\"EPSG\\\",15786]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_3_1\"}",
  "points": [
    {
      "x": 150.0,
      "y": -43.7,
      "z": 0
    },
    {
      "x": 153.69,
      "y": -43.7,
      "z": 0
    },
    {
      "x": 150.0,
      "y": -9.86,
      "z": 0
    },
    {
      "x": 153.69,
      "y": -9.86,
      "z": 0
    },
    {
      "x": 151.845,
      "y": -26.78,
      "z": 0
    }
  ]
}
```

</details>


<details><summary>Response example</summary>

```json
{
  "successCount": 3,
  "points": [
    {
      "x": 258161.26023540544,
      "y": 5156882.004961207,
      "z": 0
    },
    {
      "x": 555494.9173497866,
      "y": 5161025.50936315,
      "z": 0
    },
    {
      "x": "NaN",
      "y": "NaN",
      "z": "NaN"
    },
    {
      "x": "NaN",
      "y": "NaN",
      "z": "NaN"
    },
    {
      "x": 385073.99901601864,
      "y": 7037224.258110141,
      "z": 0
    }
  ],
  "operationsApplied": [
    "transformation GCS_WGS_1984 to GCS_Australian_1966 using AGD_1966_To_WGS_1984_17_NTv2; 3 points successfully transformed",
    "conversion from GCS_Australian_1966 to AGD_1966_AMG_Zone_56; 3 points converted"
  ]
}
```

</details>

**Other failures**

Inappropriate CRS combinations will lead to unsuccessful responses. The response will contain the reasons why the operation failed. The list below lists the known error conditions:
* Could not find a conversion method for the given input.
* Invalid source and/or target CRS specification.
* Incoherent coordinate transformations; no hub CRS could be identified.



### 4.2.2 Simple example with POST /convertGeoJson

The exact same converted coordinates can be computed with `convertGeoJson`. 
The only difference between convert and convertGeoJson is the payload, 
i.e., `convert` uses a list of coordinates, `convertGeoJson` a _schema fragment_.
Note that this is not true GeoJson (which is always in WGS 84); the
featureCollection in the Request body specifies the CRS of the input
coordinates.

This is a more convenient form compare to `convert` because no manipulation is needed for these
schema fragment “objects” of the AbstractSpatialLocation data definition
used in OSDU to store locations.

### Example 1: Conversion NOT to WGS 84 (returning an AnyCrsFeatureCollection)

**Request** _{{osduonaws_base_url}}/api/crs/converter/v3/convertGeoJson_

```json
{
  "toCRS": "{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32632:",
  "toUnitZ": "{{NAMESPACE}}:reference-data--UnitOfMeasure:ft:",
  "featureCollection": {
    "features": [
      {
        "geometry": {
          "coordinates": [
            313405.9477893702,
            6944797.620047403,
            2
          ],
          "bbox": null,
          "type": "AnyCrsPoint"
        },
        "bbox": null,
        "properties": {},
        "type": "AnyCrsFeature"
      }
    ],
    "bbox": null,
    "properties": {},
    "CoordinateReferenceSystemID": "{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::23032_EPSG::1612:",
    "VerticalUnitID": "{{NAMESPACE}}:reference-data--UnitOfMeasure:m:",
    "type": "AnyCrsFeatureCollection"
  }
}
```

**Response**

```json
{
    "successCount": 1,
    "totalCount": 1,
    "featureCollection": {
        "type": "AnyCrsFeatureCollection",
        "features": [
            {
                "type": "AnyCrsFeature",
                "geometry": {
                    "type": "AnyCrsPoint",
                    "coordinates": [
                        313327.53831136867,
                        6944590.577356114,
                        6.561679790026246
                    ]
                },
                "properties": {}
            }
        ],
        "properties": {},
        "persistableReferenceCrs": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32632\"},\"name\":\"WGS_1984_UTM_Zone_32N\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"WGS_1984_UTM_Zone_32N\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",9.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",32632]]\"}",
        "persistableReferenceUnitZ": "{\"abcd\":{\"a\":0.0,\"b\":0.3048,\"c\":1.0,\"d\":0.0},\"symbol\":\"ft\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}"
    },
    "operationsApplied": [
        "conversion from ED_1950_UTM_Zone_32N to GCS_European_1950; 1 points converted",
        "transformation GCS_European_1950 to GCS_WGS_1984 using ED_1950_To_WGS_1984_23; 1 points successfully transformed",
        "conversion from GCS_WGS_1984 to WGS_1984_UTM_Zone_32N; 1 points converted",
        "Z-axis unit conversion from m to ft"
    ]
}
```

### Example 2: Conversion to WGS 84 (returning a normal GeoJSON FeatureCollection)

**Request** _{{osduonaws_base_url}}/api/crs/converter/v3/convertGeoJson_

```json
{
  "toCRS": "{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4326:",
  "toUnitZ": "{{NAMESPACE}}:reference-data--UnitOfMeasure:ft:",
  "featureCollection": {
    "features": [
      {
        "geometry": {
          "coordinates": [
            313405.9477893702,
            6944797.620047403,
            2
          ],
          "bbox": null,
          "type": "AnyCrsPoint"
        },
        "bbox": null,
        "properties": {},
        "type": "AnyCrsFeature"
      }
    ],
    "bbox": null,
    "properties": {},
    "CoordinateReferenceSystemID": "{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::23032_EPSG::1612:",
    "VerticalUnitID": "{{NAMESPACE}}:reference-data--UnitOfMeasure:m:",
    "type": "AnyCrsFeatureCollection"
  }
}
```

**Response**

```json
{
    "successCount": 1,
    "totalCount": 1,
    "featureCollection": {
        "type": "FeatureCollection",
        "features": [
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [
                        5.364754854438938,
                        62.58480904234893,
                        2.0
                    ]
                },
                "properties": {}
            }
        ],
        "properties": {},
        "persistableReferenceUnitZ": "{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}"
    },
    "operationsApplied": [
        "conversion from ED_1950_UTM_Zone_32N to GCS_European_1950; 1 points converted",
        "transformation GCS_European_1950 to GCS_WGS_1984 using ED_1950_To_WGS_1984_23; 1 points successfully transformed",
        "Z-axis unit conversion from m to m"
    ]
}
```




### 4.2.3 Correctness and performance tests

The SIS engine was checked extensively for mathematical correctness 
using [GIGS](https://epsg.org/) and individual tests
by geodesists in several Operator companies in pre-ship testing of R3.M13.

- The standard EPSG projections and transformations are all working
  within expected boundaries.
- There currently is 1 small bug on the board for a Mercator projection
  in an area spanning the antimeridian (2022-06-18; supposed to be fixed
  in Apache SIS 1.3 to be deployed with M14).
- Custom WKT for projections is supported in EpsgManifestGenerator.py
  for the most common methods (LCC1SP, LCC2SP, TM, Mercator (A), Albers,
  Oblique Mercator) and confirmed to work correctly.
- Custom WKT for transformation is supported in EpsgManifestGenerator.py
  for the most common methods (geocentric translations (geog2D); PVT,
  CFR, geographic offsets).

### Performance check

To get a feeling for throughput and potential bottlenecks a simple experiment was done to measured the time it takes to convert a steadily increasing number of points.

- The following table show the average results (wall clock times)
  obtained on a dev system using a slow and low bandwidth connection.
  The performance is acceptable and no reason for concern.

![Table 1: Simplistic performance check](CrsConvertPicture-table1.png 'Table 1: Simplistic performance check')

- The main conclusion is that the service is not overly slow, and that there is
  no point at which performance suddenly degrades. Deployment and
  testing in a production environment will yield better numbers.
- 1.5 seconds for single point is long; but this is mainly due to latency in the
  environment. Network speed appears to have been 200 KB/sec during this test and with a
  large request body some time gets lost there.
- 1000 points in 2.5s would mean it takes less than 1 second to
  mathematically do the 1000 points (1 ms/point).
- 10000 points in 6.5s similarly would mean it took the engine 5 seconds
  for 10000 points (0.5 ms/point).
- 50000 pnts in 22s would then correspond to 20 seconds to compute 50000
  points (0.4 ms/point).

Latency needs to be taken into account for the timeout definition and - more
important - for the application design. Some conversions may be slow.

To ensure proper scaling of the infrastructure it is recommended to submit many request jobs with smaller
payloads, e.g., ~5000-50000 points.  
If these jobs are simultaneously submitted then scaling of the server needs to be considered to handle these requests.
  


# 5. Computing a wellbore trajectory from directional survey data

The endpoint POST /convertTrajectory is the main function to calculate a
trajectory based on MDINCAZI directional survey observables. Two
standard calculation algorithms are implemented, LMP (Lee’s Modified
Proposal) and GNL with scale factor correction (called
"AzimuthalEquidistant" in the API).

- These methods are described in [IOGP Guidance Note 373-07, Part 2](https://epsg.org).
  Both methods require minimum curvature calculated offsets as input.

- A calculation spreadsheet and description of the minimum curvature math are at
- [OSDU_wellbore_trajectory_calculations.docx](OSDU_wellbore_trajectory_calculations.docx), and 
- [OSDU_wellbore_trajectory_calculations.xlsx](OSDU_wellbore_trajectory_calculations.xlsx).


## 5.1 Basic example

A very simplified example is given below with 3 survey stations to
introduce the request and response. This is not a realistic example due
to the large distance between the survey stations and survey only containing 3 points 
(normally one expects there to be stations every 90 ft or so).

- The input in this example uses record id for CRS and UOM (these records need to exist, the API retrieves the persistableReference).
- The MD unit is given by `unitZ`.
- The input unitXY is only used for the algorithm option to input
  dx,dy,dz and not MD,INC,AZI in the InputStations. However it cannot be
  omitted.
  - An issue has been logged. It seems unitXY cannot be omitted. 
- There also is currently a bug if the unit of the projected CRS is not
    equal to the unit of the MD.
    - An issue has been logged 2022-04-04.
- The output “Z” coordinates are always heights and not depths (i.e.,
  they are positive station.points.z values when above the “permanent”
  geodetic vertical datum surface).

**Request** _{{osduonaws_base_url}}/api/crs/converter/v3/convertTrajectory_

```json
{
  "azimuthReference": "GN",
  "interpolate": false,
  "referencePoint": {
    "x": 2000000,
    "y": 10000000,
    "z": 0
  },
  "unitZ": "osdu:reference-data--UnitOfMeasure:m:",
  "inputStations": [
    {
      "md": 0,
      "azimuth": 100,
      "inclination": 10
    },
    {
      "md": 1000,
      "azimuth": 100,
      "inclination": 10
    },
    {
      "md": 1200,
      "azimuth": 120,
      "inclination": 12
    }
  ],
  "trajectoryCRS": "{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::32066_EPSG::15851:",
  "inputKind": "MD_Incl_Azim",
  "unitXY": "osdu:reference-data--UnitOfMeasure:m:",
  "method": "AzimuthalEquidistant"
}
```


**Response**

```json
{
    "trajectoryCRS": "{\"authCode\":{\"auth\":\"SHELL\",\"code\":\"532066079\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32066\"},\"name\":\"NAD_1927_BLM_Zone_16N\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"NAD_1927_BLM_Zone_16N\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",1640416.666666667],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",-87.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Foot_US\\\",0.3048006096012192],AUTHORITY[\\\"EPSG\\\",32066]]\"},\"name\":\"NAD27 / UTM zone 16N (ftUS) [1241_32066]\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15851\"},\"name\":\"NAD_1927_To_WGS_1984_79_CONUS\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],OPERATIONACCURACY[5.0],AUTHORITY[\\\"EPSG\\\",15851]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
    "unitXY": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "unitZ": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "unitDls": "{\"scaleOffset\":{\"scale\":5.81776417331443E-4,\"offset\":0.0},\"symbol\":\"deg/30m\",\"baseMeasurement\":{\"ancestry\":\"Rotation_Per_Length\",\"type\":\"UM\"},\"type\":\"USO\"}",
    "stations": [
        {
            "md": 0.0,
            "inclination": 10.0,
            "azimuthTN": 100.51354989131318,
            "azimuthGN": 100.0,
            "dxTN": 0.0,
            "dyTN": 0.0,
            "point": {
                "x": 1999999.99999999,
                "y": 9999999.99999969,
                "z": 0.0
            },
            "wgs84Longitude": -85.88980921169528,
            "wgs84Latitude": 27.553258329196616,
            "dls": 0.0,
            "original": true,
            "dz": 0.0
        },
        {
            "md": 1000.0,
            "inclination": 10.0,
            "azimuthTN": 100.51354989131318,
            "azimuthGN": 100.0,
            "dxTN": 170.732934404631,
            "dyTN": -31.68524446220624,
            "point": {
                "x": 2000170.9670364524,
                "y": 9999969.85389616,
                "z": -984.807753012208
            },
            "wgs84Longitude": -85.88928230486448,
            "wgs84Latitude": 27.553171177913015,
            "dls": 2.5613209387547815E-8,
            "original": true,
            "dz": 984.807753012208
        },
        {
            "md": 1200.0,
            "inclination": 12.000000000000002,
            "azimuthTN": 120.51354989131318,
            "azimuthGN": 119.99999999999999,
            "dxTN": 205.73427353766394,
            "dyTN": -45.41670171219168,
            "point": {
                "x": 2000206.0812087534,
                "y": 9999956.440082304,
                "z": -1181.1945438868763
            },
            "wgs84Longitude": -85.88917428558662,
            "wgs84Latitude": 27.5531334082539,
            "dls": 0.6417379492258135,
            "original": true,
            "dz": 1181.1945438868763
        }
    ],
    "localCRS": "{\"lateBoundCRS\":{\"name\":\"Azimuthal Equidistant\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"Azimuthal Equidistant Lng=-85.88989730;Lat=27.55297419\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Modified Azimuthal_Equidistant\\\"],PARAMETER[\\\"False_Easting\\\",0.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",-85.8898973001242],PARAMETER[\\\"Latitude_Of_Origin\\\",27.55297419069309],UNIT[\\\"Foot_US\\\",0.3048006096012192]]\"},\"name\":\"Azimuthal Equidistant - NAD_1927_To_WGS_1984_79_CONUS\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15851\"},\"name\":\"NAD_1927_To_WGS_1984_79_CONUS\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],OPERATIONACCURACY[5.0],AUTHORITY[\\\"EPSG\\\",15851]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
    "method": "AzimuthalEquidistant",
    "operationsApplied": [
        "derived TN from GN azimuth by grid convergence 0.513550",
        "computed deflections via minimum curvature method",
        "computation method: AzimuthalEquidistant",
        "conversion from 'Azimuthal Equidistant' to 'GCS_North_American_1927'",
        "conversion from 'GCS_North_American_1927' to 'NAD_1927_BLM_Zone_16N'",
        "to WGS 84: conversion from NAD_1927_BLM_Zone_16N to GCS_North_American_1927; 3 points converted",
        "to WGS 84: transformation GCS_North_American_1927 to GCS_WGS_1984 using NAD_1927_To_WGS_1984_79_CONUS; 3 points successfully transformed"
    ],
    "inputKind": "MD_Incl_Azim"
}
```


## 5.2 Realistic example

A more realistic example is shown below for a directional survey with 86
survey stations down the wellbore.
This example demonstrates usage of PersistableReference to define the TrajectoryCRS,
unitXY and unitZ. It is recommended to use record id as in the simplified example.

<details><summary>Click to expand Request body (86 survey stations)</summary>

```json
{
  "azimuthReference": "GN",
  "interpolate": false,
  "referencePoint": {
    "y": 10228686.35,
    "x": 1009773.17,
    "z": 102
  },
  "unitZ": "{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}",
  "inputStations": [
    {
        "md": 0.0,
        "azimuth": 0.9247,
        "inclination": 0.0
    },
    {
        "md": 4600.0,
        "azimuth": 106.7747,
        "inclination": 0.0
    },
    {
        "md": 4902.0,
        "azimuth": 106.7747,
        "inclination": 0.41
    },
    {
        "md": 5033.0,
        "azimuth": 143.3347,
        "inclination": 0.84
    },
    {
        "md": 5170.0,
        "azimuth": 110.6047,
        "inclination": 0.66
    },
    {
        "md": 5270.0,
        "azimuth": 103.7147,
        "inclination": 0.37
    },
    {
        "md": 5337.0,
        "azimuth": 194.4147,
        "inclination": 0.3
    },
    {
        "md": 5473.0,
        "azimuth": 149.3847,
        "inclination": 0.74
    },
    {
        "md": 5604.0,
        "azimuth": 152.6347,
        "inclination": 0.47
    },
    {
        "md": 5739.0,
        "azimuth": 147.4547,
        "inclination": 0.3
    },
    {
        "md": 5876.0,
        "azimuth": 256.5647,
        "inclination": 0.03
    },
    {
        "md": 6010.0,
        "azimuth": 219.4547,
        "inclination": 0.13
    },
    {
        "md": 6143.0,
        "azimuth": 100.1047,
        "inclination": 0.16
    },
    {
        "md": 6278.0,
        "azimuth": 74.3147,
        "inclination": 0.13
    },
    {
        "md": 6358.0,
        "azimuth": 158.7747,
        "inclination": 0.09
    },
    {
        "md": 6455.0,
        "azimuth": 282.4547,
        "inclination": 0.13
    },
    {
        "md": 6856.0,
        "azimuth": 210.8347,
        "inclination": 0.04
    },
    {
        "md": 7260.0,
        "azimuth": 1.9747,
        "inclination": 0.13
    },
    {
        "md": 7669.0,
        "azimuth": 134.9147,
        "inclination": 0.22
    },
    {
        "md": 8060.0,
        "azimuth": 219.8847,
        "inclination": 0.03
    },
    {
        "md": 8422.0,
        "azimuth": 340.2747,
        "inclination": 0.13
    },
    {
        "md": 8480.0,
        "azimuth": 274.0447,
        "inclination": 0.18
    },
    {
        "md": 8749.0,
        "azimuth": 55.6547,
        "inclination": 0.03
    },
    {
        "md": 9152.0,
        "azimuth": 58.7647,
        "inclination": 0.03
    },
    {
        "md": 9552.0,
        "azimuth": 165.7047,
        "inclination": 0.03
    },
    {
        "md": 9686.0,
        "azimuth": 308.7047,
        "inclination": 0.72
    },
    {
        "md": 9820.0,
        "azimuth": 277.2947,
        "inclination": 0.74
    },
    {
        "md": 9955.0,
        "azimuth": 268.7047,
        "inclination": 0.76
    },
    {
        "md": 10089.0,
        "azimuth": 293.7947,
        "inclination": 2.51
    },
    {
        "md": 10222.0,
        "azimuth": 295.9247,
        "inclination": 4.63
    },
    {
        "md": 10355.0,
        "azimuth": 295.3247,
        "inclination": 6.44
    },
    {
        "md": 10489.0,
        "azimuth": 290.7247,
        "inclination": 8.42
    },
    {
        "md": 10622.0,
        "azimuth": 280.5147,
        "inclination": 10.27
    },
    {
        "md": 10757.0,
        "azimuth": 276.0347,
        "inclination": 12.05
    },
    {
        "md": 10891.0,
        "azimuth": 275.6147,
        "inclination": 14.05
    },
    {
        "md": 11025.0,
        "azimuth": 274.2347,
        "inclination": 16.07
    },
    {
        "md": 11159.0,
        "azimuth": 274.1847,
        "inclination": 17.18
    },
    {
        "md": 11293.0,
        "azimuth": 274.0847,
        "inclination": 18.67
    },
    {
        "md": 11427.0,
        "azimuth": 276.3847,
        "inclination": 19.82
    },
    {
        "md": 11562.0,
        "azimuth": 278.6047,
        "inclination": 21.3
    },
    {
        "md": 11696.0,
        "azimuth": 278.9847,
        "inclination": 22.66
    },
    {
        "md": 11829.0,
        "azimuth": 277.8847,
        "inclination": 23.24
    },
    {
        "md": 12096.0,
        "azimuth": 277.1347,
        "inclination": 23.19
    },
    {
        "md": 12499.0,
        "azimuth": 276.9347,
        "inclination": 24.09
    },
    {
        "md": 12901.0,
        "azimuth": 281.0947,
        "inclination": 24.29
    },
    {
        "md": 13025.0,
        "azimuth": 281.3947,
        "inclination": 24.31
    },
    {
        "md": 13434.0,
        "azimuth": 280.4147,
        "inclination": 24.21
    },
    {
        "md": 13519.0,
        "azimuth": 279.2447,
        "inclination": 24.38
    },
    {
        "md": 13605.0,
        "azimuth": 279.1047,
        "inclination": 24.09
    },
    {
        "md": 14007.0,
        "azimuth": 282.7547,
        "inclination": 24.08
    },
    {
        "md": 14407.0,
        "azimuth": 279.6347,
        "inclination": 24.13
    },
    {
        "md": 14809.0,
        "azimuth": 276.8247,
        "inclination": 24.11
    },
    {
        "md": 15215.0,
        "azimuth": 273.8447,
        "inclination": 24.16
    },
    {
        "md": 15615.0,
        "azimuth": 275.6547,
        "inclination": 24.11
    },
    {
        "md": 16015.0,
        "azimuth": 280.7847,
        "inclination": 24.28
    },
    {
        "md": 16419.0,
        "azimuth": 282.0447,
        "inclination": 24.15
    },
    {
        "md": 16820.0,
        "azimuth": 280.8747,
        "inclination": 24.11
    },
    {
        "md": 17222.0,
        "azimuth": 278.6147,
        "inclination": 24.14
    },
    {
        "md": 17624.0,
        "azimuth": 278.2347,
        "inclination": 24.12
    },
    {
        "md": 18024.0,
        "azimuth": 277.2247,
        "inclination": 24.13
    },
    {
        "md": 18425.0,
        "azimuth": 275.6947,
        "inclination": 24.11
    },
    {
        "md": 18827.0,
        "azimuth": 278.8847,
        "inclination": 24.09
    },
    {
        "md": 19228.0,
        "azimuth": 277.8447,
        "inclination": 24.11
    },
    {
        "md": 19629.0,
        "azimuth": 275.9647,
        "inclination": 24.03
    },
    {
        "md": 20030.0,
        "azimuth": 278.2447,
        "inclination": 24.03
    },
    {
        "md": 20432.0,
        "azimuth": 280.2547,
        "inclination": 24.03
    },
    {
        "md": 20833.0,
        "azimuth": 278.6447,
        "inclination": 24.24
    },
    {
        "md": 21235.0,
        "azimuth": 279.0147,
        "inclination": 24.2
    },
    {
        "md": 21438.0,
        "azimuth": 281.2247,
        "inclination": 24.24
    },
    {
        "md": 21841.0,
        "azimuth": 280.6547,
        "inclination": 24.18
    },
    {
        "md": 22241.0,
        "azimuth": 281.4047,
        "inclination": 24.25
    },
    {
        "md": 22643.0,
        "azimuth": 278.8747,
        "inclination": 24.27
    },
    {
        "md": 23043.0,
        "azimuth": 276.6047,
        "inclination": 24.27
    },
    {
        "md": 23445.0,
        "azimuth": 276.5647,
        "inclination": 24.09
    },
    {
        "md": 23847.0,
        "azimuth": 277.3047,
        "inclination": 24.05
    },
    {
        "md": 24246.0,
        "azimuth": 278.8247,
        "inclination": 24.11
    },
    {
        "md": 24649.0,
        "azimuth": 278.2847,
        "inclination": 24.11
    },
    {
        "md": 24701.0,
        "azimuth": 278.2647,
        "inclination": 24.24
    },
    {
        "md": 25102.0,
        "azimuth": 281.3147,
        "inclination": 24.06
    },
    {
        "md": 25461.0,
        "azimuth": 279.9847,
        "inclination": 23.89
    },
    {
        "md": 25864.0,
        "azimuth": 278.6047,
        "inclination": 24.09
    },
    {
        "md": 26266.0,
        "azimuth": 278.8247,
        "inclination": 24.13
    },
    {
        "md": 26668.0,
        "azimuth": 274.8047,
        "inclination": 23.99
    },
    {
        "md": 27067.0,
        "azimuth": 277.6547,
        "inclination": 24.08
    },
    {
        "md": 27295.0,
        "azimuth": 278.0047,
        "inclination": 24.11
    },
    {
        "md": 27386.0,
        "azimuth": 278.0047,
        "inclination": 24.11
    }
  ],
  "trajectoryCRS": "{\"authCode\":{\"auth\":\"SHELL\",\"code\":\"532066079\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32066\"},\"name\":\"NAD_1927_BLM_Zone_16N\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"NAD_1927_BLM_Zone_16N\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",1640416.666666667],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",-87.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Foot_US\\\",0.3048006096012192],AUTHORITY[\\\"EPSG\\\",32066]]\"},\"name\":\"NAD27 / UTM zone 16N (ftUS) [1241_32066]\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15851\"},\"name\":\"NAD_1927_To_WGS_1984_79_CONUS\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],OPERATIONACCURACY[5.0],AUTHORITY[\\\"EPSG\\\",15851]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
  "inputKind": "MD_Incl_Azim",
  "unitXY": "{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}",
  "method": "AzimuthalEquidistant"
}
```
</details>


<details><summary>Click to expand Response (86 survey stations)</summary>
```
{
    "trajectoryCRS": "{\"authCode\":{\"auth\":\"SHELL\",\"code\":\"532066079\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32066\"},\"name\":\"NAD_1927_BLM_Zone_16N\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"NAD_1927_BLM_Zone_16N\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",1640416.666666667],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",-87.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Foot_US\\\",0.3048006096012192],AUTHORITY[\\\"EPSG\\\",32066]]\"},\"name\":\"NAD27 / UTM zone 16N (ftUS) [1241_32066]\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15851\"},\"name\":\"NAD_1927_To_WGS_1984_79_CONUS\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],OPERATIONACCURACY[5.0],AUTHORITY[\\\"EPSG\\\",15851]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
    "unitXY": "{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}",
    "unitZ": "{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}",
    "unitDls": "{\"scaleOffset\":{\"scale\":5.81776417331443E-4,\"offset\":0.0},\"symbol\":\"deg/30m\",\"baseMeasurement\":{\"ancestry\":\"Rotation_Per_Length\",\"type\":\"UM\"},\"type\":\"USO\"}",
    "stations": [
        {
            "md": 0.0,
            "inclination": 0.0,
            "azimuthTN": 4.1021980223376886E-5,
            "azimuthGN": 0.924699999999973,
            "dxTN": 0.0,
            "dyTN": 0.0,
            "point": {
                "x": 1009773.1700000245,
                "y": 1.0228686349999696E7,
                "z": 102.0
            },
            "wgs84Longitude": -88.9578984522382,
            "wgs84Latitude": 28.172935420826686,
            "dls": 0.0,
            "original": true,
            "dz": 0.0
        },
        {
            "md": 4600.0,
            "inclination": 0.0,
            "azimuthTN": 105.85004102198025,
            "azimuthGN": 106.7747,
            "dxTN": 0.0,
            "dyTN": 0.0,
            "point": {
                "x": 1009773.1700000245,
                "y": 1.0228686349999696E7,
                "z": -4498.0
            },
            "wgs84Longitude": -88.9578984522382,
            "wgs84Latitude": 28.172935420826686,
            "dls": 0.0,
            "original": true,
            "dz": 4600.0
        },
        {
            "md": 4902.0,
            "inclination": 0.41,
            "azimuthTN": 105.85004102198025,
            "azimuthGN": 106.7747,
            "dxTN": 1.0394468347826642,
            "dyTN": -0.29511457442461336,
            "point": {
                "x": 1009774.204607042,
                "y": 1.0228686038131852E7,
                "z": -4799.997422628456
            },
            "wgs84Longitude": -88.95789522599455,
            "wgs84Latitude": 28.17293460913737,
            "dls": 0.04072847682119448,
            "original": true,
            "dz": 4901.997422628456
        },
        {
            "md": 5033.0,
            "inclination": 0.8400000000000001,
            "azimuthTN": 142.4100410219803,
            "azimuthGN": 143.3347,
            "dxTN": 2.0760951878479146,
            "dyTN": -1.1840299655736677,
            "point": {
                "x": 1009775.2268326968,
                "y": 1.0228685132552395E7,
                "z": -4930.989772032392
            },
            "wgs84Longitude": -88.95789200839803,
            "wgs84Latitude": 28.17293216426475,
            "dls": 0.12963298195100387,
            "original": true,
            "dz": 5032.989772032392
        },
        {
            "md": 5170.0,
            "inclination": 0.66,
            "azimuthTN": 109.68004102198029,
            "azimuthGN": 110.6047,
            "dxTN": 3.4316429320911825,
            "dyTN": -2.245506026303322,
            "point": {
                "x": 1009776.5651492131,
                "y": 1.0228684049278507E7,
                "z": -5067.978590735491
            },
            "wgs84Longitude": -88.9578878009938,
            "wgs84Latitude": 28.172929244780164,
            "dls": 0.09997435478739265,
            "original": true,
            "dz": 5169.978590735491
        },
        {
            "md": 5270.0,
            "inclination": 0.37,
            "azimuthTN": 102.79004102198019,
            "azimuthGN": 103.7147,
            "dxTN": 4.288820781630359,
            "dyTN": -2.5109460167813182,
            "point": {
                "x": 1009777.4179796586,
                "y": 1.0228683770024601E7,
                "z": -5167.974453369109
            },
            "wgs84Longitude": -88.95788514047695,
            "wgs84Latitude": 28.17292851470892,
            "dls": 0.08880557098537961,
            "original": true,
            "dz": 5269.974453369109
        },
        {
            "md": 5337.0,
            "inclination": 0.3,
            "azimuthTN": 193.49004102198023,
            "azimuthGN": 194.41470000000007,
            "dxTN": 4.4588683780929586,
            "dyTN": -2.7294041187554803,
            "point": {
                "x": 1009777.5844890466,
                "y": 1.0228683548838384E7,
                "z": -5234.973686165022
            },
            "wgs84Longitude": -88.95788461267075,
            "wgs84Latitude": 28.172927913862576,
            "dls": 0.2145570179724616,
            "original": true,
            "dz": 5336.973686165022
        },
        {
            "md": 5473.0,
            "inclination": 0.74,
            "azimuthTN": 148.46004102198026,
            "azimuthGN": 149.3847,
            "dxTN": 4.835208028691541,
            "dyTN": -3.824125459502624,
            "point": {
                "x": 1009777.9431335768,
                "y": 1.0228682448124675E7,
                "z": -5370.968200471651
            },
            "wgs84Longitude": -88.95788344452036,
            "wgs84Latitude": 28.172924902949493,
            "dls": 0.12552352795862423,
            "original": true,
            "dz": 5472.968200471651
        },
        {
            "md": 5604.0,
            "inclination": 0.47,
            "azimuthTN": 151.71004102198026,
            "azimuthGN": 152.63470000000007,
            "dxTN": 5.532355072767719,
            "dyTN": -5.018219083882049,
            "point": {
                "x": 1009778.6209579769,
                "y": 1.022868124286869E7,
                "z": -5501.960779962983
            },
            "wgs84Longitude": -88.95788128064504,
            "wgs84Latitude": 28.17292161872092,
            "dls": 0.062304688375061736,
            "original": true,
            "dz": 5603.960779962983
        },
        {
            "md": 5739.0,
            "inclination": 0.3,
            "azimuthTN": 146.5300410219802,
            "azimuthGN": 147.4547,
            "dxTN": 5.989687571738633,
            "dyTN": -5.800606010543754,
            "point": {
                "x": 1009779.0656299657,
                "y": 1.022868045315912E7,
                "z": -5636.957686648459
            },
            "wgs84Longitude": -88.95787986113044,
            "wgs84Latitude": 28.172919466848178,
            "dls": 0.03852315557325753,
            "original": true,
            "dz": 5738.957686648459
        },
        {
            "md": 5876.0,
            "inclination": 0.03,
            "azimuthTN": 255.64004102198035,
            "azimuthGN": 256.5647,
            "dxTN": 6.15274486715918,
            "dyTN": -6.108690305354285,
            "point": {
                "x": 1009779.2237031346,
                "y": 1.0228680142466178E7,
                "z": -5773.957074894352
            },
            "wgs84Longitude": -88.95787935501491,
            "wgs84Latitude": 28.172918619495,
            "dls": 0.0681274919763518,
            "original": true,
            "dz": 5875.957074894352
        },
        {
            "md": 6010.0,
            "inclination": 0.13,
            "azimuthTN": 218.5300410219802,
            "azimuthGN": 219.4547,
            "dxTN": 6.0240639375198795,
            "dyTN": -6.2363118433075595,
            "point": {
                "x": 1009779.0929721239,
                "y": 1.0228680016930824E7,
                "z": -5907.956932639962
            },
            "wgs84Longitude": -88.9578797544051,
            "wgs84Latitude": 28.172918268487315,
            "dls": 0.02409153511159901,
            "original": true,
            "dz": 6009.956932639962
        },
        {
            "md": 6143.0,
            "inclination": 0.16,
            "azimuthTN": 99.18004102198029,
            "azimuthGN": 100.10469999999998,
            "dxTN": 6.113399231930273,
            "dyTN": -6.3839720628369365,
            "point": {
                "x": 1009779.1799177757,
                "y": 1.0228679867839824E7,
                "z": -6040.956714504921
            },
            "wgs84Longitude": -88.95787947711756,
            "wgs84Latitude": 28.172917862363455,
            "dls": 0.05656661105660011,
            "original": true,
            "dz": 6142.956714504921
        },
        {
            "md": 6278.0,
            "inclination": 0.13,
            "azimuthTN": 73.39004102198032,
            "azimuthGN": 74.31470000000002,
            "dxTN": 6.446242222615968,
            "dyTN": -6.37026469526561,
            "point": {
                "x": 1009779.5129572921,
                "y": 1.0228679876174578E7,
                "z": -6175.956294853549
            },
            "wgs84Longitude": -88.95787844404398,
            "wgs84Latitude": 28.172917900060913,
            "dls": 0.015781801269903837,
            "original": true,
            "dz": 6277.956294853549
        },
        {
            "md": 6358.0,
            "inclination": 0.09,
            "azimuthTN": 157.85004102198025,
            "azimuthGN": 158.77469999999994,
            "dxTN": 6.5569019115647045,
            "dyTN": -6.4025162179798025,
            "point": {
                "x": 1009779.6230882761,
                "y": 1.0228679842139559E7,
                "z": -6255.956188726737
            },
            "wgs84Longitude": -88.95787810057756,
            "wgs84Latitude": 28.172917811355617,
            "dls": 0.05655036691181904,
            "original": true,
            "dz": 6357.956188726737
        },
        {
            "md": 6455.0,
            "inclination": 0.13,
            "azimuthTN": 281.5300410219802,
            "azimuthGN": 282.4547,
            "dxTN": 6.477803234103241,
            "dyTN": -6.451081959432445,
            "point": {
                "x": 1009779.5432116892,
                "y": 1.0228679794853956E7,
                "z": -6352.9560975630075
            },
            "wgs84Longitude": -88.95787834608,
            "wgs84Latitude": 28.172917677781676,
            "dls": 0.06027084712706198,
            "original": true,
            "dz": 6454.9560975630075
        },
        {
            "md": 6856.0,
            "inclination": 0.04,
            "azimuthTN": 209.9100410219803,
            "azimuthGN": 210.8347,
            "dxTN": 5.962266334906529,
            "dyTN": -6.481483838683541,
            "point": {
                "x": 1009779.0272223912,
                "y": 1.0228679772774346E7,
                "z": -6753.955687547978
            },
            "wgs84Longitude": -88.95787994619585,
            "wgs84Latitude": 28.17291759416957,
            "dls": 0.009229838185718828,
            "original": true,
            "dz": 6855.955687547978
        },
        {
            "md": 7260.0,
            "inclination": 0.13,
            "azimuthTN": 1.050041021980178,
            "azimuthGN": 1.9746999999999841,
            "dxTN": 5.900345901739131,
            "dyTN": -6.145477205593576,
            "point": {
                "x": 1009778.9707292055,
                "y": 1.0228680109755358E7,
                "z": -7157.955401506401
            },
            "wgs84Longitude": -88.9578801384057,
            "wgs84Latitude": 28.172918518319104,
            "dls": 0.012338433205565847,
            "original": true,
            "dz": 7259.955401506401
        },
        {
            "md": 7669.0,
            "inclination": 0.22,
            "azimuthTN": 133.99004102198023,
            "azimuthGN": 134.91470000000004,
            "dxTN": 6.473786458649236,
            "dyTN": -6.226922595839654,
            "point": {
                "x": 1009779.5428128162,
                "y": 1.0228680019061515E7,
                "z": -7566.954450132284
            },
            "wgs84Longitude": -88.9578783585618,
            "wgs84Latitude": 28.172918294307166,
            "dls": 0.023684826283089006,
            "original": true,
            "dz": 7668.954450132284
        },
        {
            "md": 8060.0,
            "inclination": 0.03,
            "azimuthTN": 218.96004102198026,
            "azimuthGN": 219.88470000000007,
            "dxTN": 6.949496376364788,
            "dyTN": -6.827881012430675,
            "point": {
                "x": 1009780.0087888516,
                "y": 1.0228679410470415E7,
                "z": -7957.953459996327
            },
            "wgs84Longitude": -88.95787688201948,
            "wgs84Latitude": 28.172916641433197,
            "dls": 0.016834860224856887,
            "original": true,
            "dz": 8059.953459996327
        },
        {
            "md": 8422.0,
            "inclination": 0.13,
            "azimuthTN": 339.35004102198036,
            "azimuthGN": 340.27469999999994,
            "dxTN": 6.745078161251673,
            "dyTN": -6.517282914251759,
            "point": {
                "x": 1009779.8093984029,
                "y": 1.0228679724344486E7,
                "z": -8319.953169117505
            },
            "wgs84Longitude": -88.95787751651069,
            "wgs84Latitude": 28.17291749570075,
            "dls": 0.012220845601995111,
            "original": true,
            "dz": 8421.953169117505
        },
        {
            "md": 8480.0,
            "inclination": 0.18,
            "azimuthTN": 273.1200410219802,
            "azimuthGN": 274.0446999999999,
            "dxTN": 6.630902608995729,
            "dyTN": -6.450752698985665,
            "point": {
                "x": 1009779.696305019,
                "y": 1.022867979271239E7,
                "z": -8377.952996173895
            },
            "wgs84Longitude": -88.95787787089186,
            "wgs84Latitude": 28.17291767868581,
            "dls": 0.09023817579540702,
            "original": true,
            "dz": 8479.952996173895
        },
        {
            "md": 8749.0,
            "inclination": 0.03,
            "azimuthTN": 54.73004102198024,
            "azimuthGN": 55.65469999999999,
            "dxTN": 6.266482073769799,
            "dyTN": -6.387089476362832,
            "point": {
                "x": 1009779.3329389469,
                "y": 1.0228679862252105E7,
                "z": -8646.95259919922
            },
            "wgs84Longitude": -88.9578790019805,
            "wgs84Latitude": 28.1729178537879,
            "dls": 0.022791637237176456,
            "original": true,
            "dz": 8748.95259919922
        },
        {
            "md": 9152.0,
            "inclination": 0.03,
            "azimuthTN": 57.840041021980255,
            "azimuthGN": 58.764700000000005,
            "dxTN": 6.441937749288456,
            "dyTN": -6.2700088919463814,
            "point": {
                "x": 1009779.5102711197,
                "y": 1.022867997649241E7,
                "z": -9049.95254398397
            },
            "wgs84Longitude": -88.95787845741069,
            "wgs84Latitude": 28.172918175803396,
            "dls": 1.2120537711541479E-4,
            "original": true,
            "dz": 9151.95254398397
        },
        {
            "md": 9552.0,
            "inclination": 0.03,
            "azimuthTN": 164.7800410219802,
            "azimuthGN": 165.7047,
            "dxTN": 6.558081455764355,
            "dyTN": -6.315314880546437,
            "point": {
                "x": 1009779.6256750339,
                "y": 1.0228679929315388E7,
                "z": -9449.952512755264
            },
            "wgs84Longitude": -88.95787809692217,
            "wgs84Latitude": 28.17291805119318,
            "dls": 0.003615953800218485,
            "original": true,
            "dz": 9551.952512755264
        },
        {
            "md": 9686.0,
            "inclination": 0.72,
            "azimuthTN": 307.7800410219802,
            "azimuthGN": 308.7047,
            "dxTN": 5.901851196432249,
            "dyTN": -5.833368956255055,
            "point": {
                "x": 1009778.9772713633,
                "y": 1.022868042181616E7,
                "z": -9583.949097278099
            },
            "wgs84Longitude": -88.95788013375387,
            "wgs84Latitude": 28.17291937673819,
            "dls": 0.16660703616359995,
            "original": true,
            "dz": 9685.949097278099
        },
        {
            "md": 9820.0,
            "inclination": 0.74,
            "azimuthTN": 276.3700410219802,
            "azimuthGN": 277.2946999999999,
            "dxTN": 4.376446599208424,
            "dyTN": -5.2215711730352545,
            "point": {
                "x": 1009777.4618534412,
                "y": 1.022868105818636E7,
                "z": -9717.938751690624
            },
            "wgs84Longitude": -88.95788486832805,
            "wgs84Latitude": 28.172921059434426,
            "dls": 0.08858017785026828,
            "original": true,
            "dz": 9819.938751690624
        },
        {
            "md": 9955.0,
            "inclination": 0.76,
            "azimuthTN": 267.7800410219802,
            "azimuthGN": 268.7047,
            "dxTN": 2.615404834584203,
            "dyTN": -5.159530462213424,
            "point": {
                "x": 1009775.7019435527,
                "y": 1.022868114864311E7,
                "z": -9852.927228461076
            },
            "wgs84Longitude": -88.95789033423456,
            "wgs84Latitude": 28.172921230086843,
            "dls": 0.025353566081201737,
            "original": true,
            "dz": 9954.927228461076
        },
        {
            "md": 10089.0,
            "inclination": 2.51,
            "azimuthTN": 292.8700410219802,
            "azimuthGN": 293.7946999999999,
            "dxTN": -0.9764576348129204,
            "dyTN": -4.0535119751525475,
            "point": {
                "x": 1009772.1281970323,
                "y": 1.022868231254698E7,
                "z": -9986.868690781332
            },
            "wgs84Longitude": -88.95790148269118,
            "wgs84Latitude": 28.172924272100623,
            "dls": 0.4141745703494756,
            "original": true,
            "dz": 10088.868690781332
        },
        {
            "md": 10222.0,
            "inclination": 4.63,
            "azimuthTN": 295.0000410219802,
            "azimuthGN": 295.92470000000003,
            "dxTN": -8.525667848762469,
            "dyTN": -0.6526950162364757,
            "point": {
                "x": 1009764.634430948,
                "y": 1.0228685834944982E7,
                "z": -10119.603082076188
            },
            "wgs84Longitude": -88.95792491407445,
            "wgs84Latitude": 28.172933625735936,
            "dls": 0.4790480449237356,
            "original": true,
            "dz": 10221.603082076188
        },
        {
            "md": 10355.0,
            "inclination": 6.44,
            "azimuthTN": 294.4000410219803,
            "azimuthGN": 295.3247,
            "dxTN": -20.184259998556406,
            "dyTN": 4.697617053322981,
            "point": {
                "x": 1009753.0630495971,
                "y": 1.0228691373013096E7,
                "z": -10251.977471227205
            },
            "wgs84Longitude": -88.95796110025066,
            "wgs84Latitude": 28.17294834126907,
            "dls": 0.40847375233916244,
            "original": true,
            "dz": 10353.977471227205
        },
        {
            "md": 10489.0,
            "inclination": 8.42,
            "azimuthTN": 289.8000410219802,
            "azimuthGN": 290.7247,
            "dxTN": -36.2603915479963,
            "dyTN": 11.126019194826782,
            "point": {
                "x": 1009737.0918549704,
                "y": 1.0228698060384305E7,
                "z": -10384.846912469875
            },
            "wgs84Longitude": -88.9580109976271,
            "wgs84Latitude": 28.172966021989026,
            "dls": 0.46250789696853295,
            "original": true,
            "dz": 10486.846912469875
        },
        {
            "md": 10622.0,
            "inclination": 10.270000000000001,
            "azimuthTN": 279.59004102198026,
            "azimuthGN": 280.51469999999995,
            "dxTN": -57.11584931115305,
            "dyTN": 16.40049571697151,
            "point": {
                "x": 1009716.3230652362,
                "y": 1.0228703671047222E7,
                "z": -10516.085146492804
            },
            "wgs84Longitude": -88.95807572896888,
            "wgs84Latitude": 28.172980528971237,
            "dls": 0.5588065309531866,
            "original": true,
            "dz": 10618.085146492804
        },
        {
            "md": 10757.0,
            "inclination": 12.05,
            "azimuthTN": 275.11004102198024,
            "azimuthGN": 276.03469999999993,
            "dxTN": -83.02026188412424,
            "dyTN": 19.660841989068743,
            "point": {
                "x": 1009690.4731888159,
                "y": 1.022870734921235E7,
                "z": -10648.529551112659
            },
            "wgs84Longitude": -88.95815613108509,
            "wgs84Latitude": 28.172989496298186,
            "dls": 0.4397067835761621,
            "original": true,
            "dz": 10750.529551112659
        },
        {
            "md": 10891.0,
            "inclination": 14.05,
            "azimuthTN": 274.6900410219803,
            "azimuthGN": 275.6147000000001,
            "dxTN": -113.16603523641169,
            "dyTN": 22.236886579095433,
            "point": {
                "x": 1009660.371220932,
                "y": 1.0228710411576279E7,
                "z": -10779.062187280762
            },
            "wgs84Longitude": -88.95824969746302,
            "wgs84Latitude": 28.172996581487297,
            "dls": 0.44826140063480674,
            "original": true,
            "dz": 10881.062187280762
        },
        {
            "md": 11025.0,
            "inclination": 16.07,
            "azimuthTN": 273.31004102198017,
            "azimuthGN": 274.2347,
            "dxTN": -147.89620830879963,
            "dyTN": 24.637944360888408,
            "point": {
                "x": 1009625.68236707,
                "y": 1.0228713372952359E7,
                "z": -10908.453577469856
            },
            "wgs84Longitude": -88.9583574928611,
            "wgs84Latitude": 28.17300318532517,
            "dls": 0.45927720365169367,
            "original": true,
            "dz": 11010.453577469856
        },
        {
            "md": 11159.0,
            "inclination": 17.18,
            "azimuthTN": 273.2600410219802,
            "azimuthGN": 274.1847,
            "dxTN": -186.17090970392542,
            "dyTN": 26.83428020927546,
            "point": {
                "x": 1009587.4459411945,
                "y": 1.0228716186825652E7,
                "z": -11036.850079187358
            },
            "wgs84Longitude": -88.95847628973385,
            "wgs84Latitude": 28.173009225999337,
            "dls": 0.24852807705063815,
            "original": true,
            "dz": 11138.850079187358
        },
        {
            "md": 11293.0,
            "inclination": 18.67,
            "azimuthTN": 273.1600410219803,
            "azimuthGN": 274.0847,
            "dxTN": -227.34652774729838,
            "dyTN": 29.142144255140412,
            "point": {
                "x": 1009546.310610705,
                "y": 1.0228719159035938E7,
                "z": -11164.342156949404
            },
            "wgs84Longitude": -88.958604090471,
            "wgs84Latitude": 28.173015573296464,
            "dls": 0.3336531265568124,
            "original": true,
            "dz": 11266.342156949404
        },
        {
            "md": 11427.0,
            "inclination": 19.82,
            "azimuthTN": 275.46004102198026,
            "azimuthGN": 276.38470000000007,
            "dxTN": -271.3782439553996,
            "dyTN": 32.48622027164835,
            "point": {
                "x": 1009502.3361133675,
                "y": 1.0228723213475546E7,
                "z": -11290.853660888684
            },
            "wgs84Longitude": -88.9587407560485,
            "wgs84Latitude": 28.173024770431958,
            "dls": 0.30832602513149954,
            "original": true,
            "dz": 11392.853660888684
        },
        {
            "md": 11562.0,
            "inclination": 21.3,
            "azimuthTN": 277.6800410219803,
            "azimuthGN": 278.6047,
            "dxTN": -318.46423603554484,
            "dyTN": 37.94114252797017,
            "point": {
                "x": 1009455.3416286738,
                "y": 1.022872942789809E7,
                "z": -11417.253261444986
            },
            "wgs84Longitude": -88.95888690167962,
            "wgs84Latitude": 28.173039773038877,
            "dls": 0.3716771144976584,
            "original": true,
            "dz": 11519.253261444986
        },
        {
            "md": 11696.0,
            "inclination": 22.66,
            "azimuthTN": 278.06004102198017,
            "azimuthGN": 278.9847,
            "dxTN": -368.14367410596634,
            "dyTN": 44.81319651250762,
            "point": {
                "x": 1009405.7767564083,
                "y": 1.0228737101203505E7,
                "z": -11541.510559204717
            },
            "wgs84Longitude": -88.95904109700558,
            "wgs84Latitude": 28.173058673109857,
            "dls": 0.30613657143459777,
            "original": true,
            "dz": 11643.510559204717
        },
        {
            "md": 11829.0,
            "inclination": 23.24,
            "azimuthTN": 276.96004102198026,
            "azimuthGN": 277.88470000000007,
            "dxTN": -419.55762614972684,
            "dyTN": 51.585136376873564,
            "point": {
                "x": 1009354.4758785949,
                "y": 1.022874470239597E7,
                "z": -11663.983057006752
            },
            "wgs84Longitude": -88.95920067594946,
            "wgs84Latitude": 28.173077297619056,
            "dls": 0.16270963327846094,
            "original": true,
            "dz": 11765.983057006752
        },
        {
            "md": 12096.0,
            "inclination": 23.19,
            "azimuthTN": 276.21004102198026,
            "azimuthGN": 277.13470000000007,
            "dxTN": -524.1079292313923,
            "dyTN": 63.65506363935956,
            "point": {
                "x": 1009250.1280516377,
                "y": 1.0228758458737252E7,
                "z": -11909.365184093156
            },
            "wgs84Longitude": -88.95952517981814,
            "wgs84Latitude": 28.173110492089172,
            "dls": 0.033689137669927705,
            "original": true,
            "dz": 12011.365184093156
        },
        {
            "md": 12499.0,
            "inclination": 24.09,
            "azimuthTN": 276.01004102198027,
            "azimuthGN": 276.9347,
            "dxTN": -684.7870429763209,
            "dyTN": 80.85009860087109,
            "point": {
                "x": 1009089.7382211954,
                "y": 1.0228778245660674E7,
                "z": -12278.542901864188
            },
            "wgs84Longitude": -88.96002389685992,
            "wgs84Latitude": 28.17315777974468,
            "dls": 0.06726289330238122,
            "original": true,
            "dz": 12380.542901864188
        },
        {
            "md": 12901.0,
            "inclination": 24.29,
            "azimuthTN": 280.1700410219803,
            "azimuthGN": 281.0947,
            "dxTN": -847.7739945612011,
            "dyTN": 104.04113833474717,
            "point": {
                "x": 1008927.1374525214,
                "y": 1.0228804065406218E7,
                "z": -12645.270798292635
            },
            "wgs84Longitude": -88.96052977819825,
            "wgs84Latitude": 28.17322155681296,
            "dls": 0.1280587464580071,
            "original": true,
            "dz": 12747.270798292635
        },
        {
            "md": 13025.0,
            "inclination": 24.31,
            "azimuthTN": 280.47004102198025,
            "azimuthGN": 281.39470000000006,
            "dxTN": -897.9761077194711,
            "dyTN": 113.18260188345444,
            "point": {
                "x": 1008877.0865316236,
                "y": 1.022881401640236E7,
                "z": -12758.28484774543
            },
            "wgs84Longitude": -88.96068559666239,
            "wgs84Latitude": 28.173246696764956,
            "dls": 0.030257352236303853,
            "original": true,
            "dz": 12860.28484774543
        },
        {
            "md": 13434.0,
            "inclination": 24.21,
            "azimuthTN": 279.49004102198023,
            "azimuthGN": 280.41470000000004,
            "dxTN": -1063.47643848984,
            "dyTN": 142.3081602667757,
            "point": {
                "x": 1008712.0682936058,
                "y": 1.0228845810814753E7,
                "z": -13131.167686486051
            },
            "wgs84Longitude": -88.96119928072928,
            "wgs84Latitude": 28.17332679345826,
            "dls": 0.03043183191685789,
            "original": true,
            "dz": 13233.167686486051
        },
        {
            "md": 13519.0,
            "inclination": 24.38,
            "azimuthTN": 278.3200410219803,
            "azimuthGN": 279.24469999999997,
            "dxTN": -1098.0254264226967,
            "dyTN": 147.72031389879768,
            "point": {
                "x": 1008677.6091601914,
                "y": 1.0228851780154865E7,
                "z": -13208.640444457385
            },
            "wgs84Longitude": -88.96130651473686,
            "wgs84Latitude": 28.173341676671697,
            "dls": 0.18017850361331178,
            "original": true,
            "dz": 13310.640444457385
        },
        {
            "md": 13605.0,
            "inclination": 24.09,
            "azimuthTN": 278.1800410219803,
            "azimuthGN": 279.1047,
            "dxTN": -1132.9613001740272,
            "dyTN": 152.78604505857737,
            "point": {
                "x": 1008642.75757651,
                "y": 1.022885740934212E7,
                "z": -13287.061147614324
            },
            "wgs84Longitude": -88.96141494953712,
            "wgs84Latitude": 28.17335560697734,
            "dls": 0.10312985434560104,
            "original": true,
            "dz": 13389.061147614324
        },
        {
            "md": 14007.0,
            "inclination": 24.080000000000002,
            "azimuthTN": 281.83004102198026,
            "azimuthGN": 282.75469999999996,
            "dxTN": -1294.4465536073972,
            "dyTN": 181.2738662890954,
            "point": {
                "x": 1008481.7437791332,
                "y": 1.0228888501285836E7,
                "z": -13654.084113578565
            },
            "wgs84Longitude": -88.96191617254689,
            "wgs84Latitude": 28.173433947239296,
            "dls": 0.1111460794994771,
            "original": true,
            "dz": 13756.084113578565
        },
        {
            "md": 14407.0,
            "inclination": 24.13,
            "azimuthTN": 278.71004102198026,
            "azimuthGN": 279.63470000000007,
            "dxTN": -1455.1410587217501,
            "dyTN": 210.38580820601712,
            "point": {
                "x": 1008321.5307095506,
                "y": 1.0228920204557946E7,
                "z": -14019.218564807485
            },
            "wgs84Longitude": -88.9624149420705,
            "wgs84Latitude": 28.173514002314988,
            "dls": 0.0956316335187206,
            "original": true,
            "dz": 14121.218564807485
        },
        {
            "md": 14809.0,
            "inclination": 24.11,
            "azimuthTN": 275.9000410219803,
            "azimuthGN": 276.8247,
            "dxTN": -1618.040856497011,
            "dyTN": 231.269904302935,
            "point": {
                "x": 1008158.9796810809,
                "y": 1.0228943716181228E7,
                "z": -14386.132860621765
            },
            "wgs84Longitude": -88.96292055554669,
            "wgs84Latitude": 28.17357142556753,
            "dls": 0.08570015757767241,
            "original": true,
            "dz": 14488.132860621765
        },
        {
            "md": 15215.0,
            "inclination": 24.16,
            "azimuthTN": 272.9200410219803,
            "azimuthGN": 273.8447,
            "dxTN": -1783.50847116562,
            "dyTN": 244.02689712086524,
            "point": {
                "x": 1007993.7298188324,
                "y": 1.0228959142736243E7,
                "z": -14756.656147244652
            },
            "wgs84Longitude": -88.96343413789396,
            "wgs84Latitude": 28.173606493960403,
            "dls": 0.09010321036100899,
            "original": true,
            "dz": 14858.656147244652
        },
        {
            "md": 15615.0,
            "inclination": 24.11,
            "azimuthTN": 274.73004102198024,
            "azimuthGN": 275.65470000000005,
            "dxTN": -1946.6814125395165,
            "dyTN": 254.9339417287922,
            "point": {
                "x": 1007830.7445705238,
                "y": 1.022897268244873E7,
                "z": -15121.695039635824
            },
            "wgs84Longitude": -88.96394059793903,
            "wgs84Latitude": 28.17363647264413,
            "dls": 0.05563113059170797,
            "original": true,
            "dz": 15223.695039635824
        },
        {
            "md": 16015.0,
            "inclination": 24.28,
            "azimuthTN": 279.86004102198024,
            "azimuthGN": 280.78469999999993,
            "dxTN": -2109.143949673302,
            "dyTN": 275.7560053029731,
            "point": {
                "x": 1007668.6296565317,
                "y": 1.0228996125016062E7,
                "z": -15486.598184591438
            },
            "wgs84Longitude": -88.96444485561679,
            "wgs84Latitude": 28.173693719711657,
            "dls": 0.15815728239064927,
            "original": true,
            "dz": 15588.598184591438
        },
        {
            "md": 16419.0,
            "inclination": 24.15,
            "azimuthTN": 281.1200410219802,
            "azimuthGN": 282.0446999999999,
            "dxTN": -2272.0718732523,
            "dyTN": 305.9189453874417,
            "point": {
                "x": 1007506.2001049989,
                "y": 1.0229028915319609E7,
                "z": -15855.053761559655
            },
            "wgs84Longitude": -88.96495056060175,
            "wgs84Latitude": 28.173776655894777,
            "dls": 0.039571299828543335,
            "original": true,
            "dz": 15957.053761559655
        },
        {
            "md": 16820.0,
            "inclination": 24.11,
            "azimuthTN": 279.95004102198027,
            "azimuthGN": 280.8747000000001,
            "dxTN": -2433.2329606667995,
            "dyTN": 335.89167440926377,
            "point": {
                "x": 1007345.5341582061,
                "y": 1.0229061486925766E7,
                "z": -16221.016599939376
            },
            "wgs84Longitude": -88.96545078238279,
            "wgs84Latitude": 28.17385906734233,
            "dls": 0.03590785953728616,
            "original": true,
            "dz": 16323.016599939376
        },
        {
            "md": 17222.0,
            "inclination": 24.14,
            "azimuthTN": 277.6900410219803,
            "azimuthGN": 278.6147000000001,
            "dxTN": -2595.5711498070345,
            "dyTN": 361.0791838805374,
            "point": {
                "x": 1007183.6139270542,
                "y": 1.0229089292662079E7,
                "z": -16587.91222279462
            },
            "wgs84Longitude": -88.96595465714097,
            "wgs84Latitude": 28.173928315544106,
            "dls": 0.0689674563788402,
            "original": true,
            "dz": 16689.91222279462
        },
        {
            "md": 17624.0,
            "inclination": 24.12,
            "azimuthTN": 277.31004102198017,
            "azimuthGN": 278.23470000000003,
            "dxTN": -2758.5053121601823,
            "dyTN": 382.53020614274243,
            "point": {
                "x": 1007021.0374235343,
                "y": 1.022911337180511E7,
                "z": -16954.78578649507
            },
            "wgs84Longitude": -88.96646038127851,
            "wgs84Latitude": 28.173987284989185,
            "dls": 0.011688737179314497,
            "original": true,
            "dz": 17056.78578649507
        },
        {
            "md": 18024.0,
            "inclination": 24.13,
            "azimuthTN": 276.3000410219802,
            "azimuthGN": 277.2247,
            "dxTN": -2920.83944669837,
            "dyTN": 401.9015827488683,
            "point": {
                "x": 1006859.0273043392,
                "y": 1.0229135361775355E7,
                "z": -17319.849734293355
            },
            "wgs84Longitude": -88.96696424297276,
            "wgs84Latitude": 28.174040532850583,
            "dls": 0.030969948768323066,
            "original": true,
            "dz": 17421.849734293355
        },
        {
            "md": 18425.0,
            "inclination": 24.11,
            "azimuthTN": 274.7700410219802,
            "azimuthGN": 275.6947,
            "dxTN": -3083.9306697551947,
            "dyTN": 417.7070124864153,
            "point": {
                "x": 1006696.202560095,
                "y": 1.0229153798276616E7,
                "z": -17685.842690679463
            },
            "wgs84Longitude": -88.96747045392783,
            "wgs84Latitude": 28.174083970960734,
            "dls": 0.04679830668071333,
            "original": true,
            "dz": 17787.842690679463
        },
        {
            "md": 18827.0,
            "inclination": 24.09,
            "azimuthTN": 277.96004102198026,
            "azimuthGN": 278.88470000000007,
            "dxTN": -3247.0116713288085,
            "dyTN": 435.89693509357295,
            "point": {
                "x": 1006533.4264826999,
                "y": 1.0229174618948814E7,
                "z": -18052.817830543943
            },
            "wgs84Longitude": -88.96797663433736,
            "wgs84Latitude": 28.174133965484447,
            "dls": 0.09720801654225414,
            "original": true,
            "dz": 18154.817830543943
        },
        {
            "md": 19228.0,
            "inclination": 24.11,
            "azimuthTN": 276.9200410219803,
            "azimuthGN": 277.8447,
            "dxTN": -3409.3677708654923,
            "dyTN": 457.09811261847943,
            "point": {
                "x": 1006371.4238190074,
                "y": 1.0229198438977787E7,
                "z": -18418.866009440953
            },
            "wgs84Longitude": -88.96848056626072,
            "wgs84Latitude": 28.1741922404262,
            "dls": 0.0318051989022189,
            "original": true,
            "dz": 18520.866009440953
        },
        {
            "md": 19629.0,
            "inclination": 24.03,
            "azimuthTN": 275.0400410219802,
            "azimuthGN": 275.9647,
            "dxTN": -3572.0066961678235,
            "dyTN": 474.1390898226489,
            "point": {
                "x": 1006209.0711685763,
                "y": 1.022921810366945E7,
                "z": -18785.003632881635
            },
            "wgs84Longitude": -88.96898537511998,
            "wgs84Latitude": 28.174239071277142,
            "dls": 0.057672955986540195,
            "original": true,
            "dz": 18887.003632881635
        },
        {
            "md": 20030.0,
            "inclination": 24.03,
            "azimuthTN": 277.3200410219803,
            "azimuthGN": 278.24469999999997,
            "dxTN": -3734.322322334331,
            "dyTN": 491.71500186753156,
            "point": {
                "x": 1006047.0503908123,
                "y": 1.0229238298050595E7,
                "z": -19151.25792508856
            },
            "wgs84Longitude": -88.96948918114757,
            "wgs84Latitude": 28.17428737162816,
            "dls": 0.06945627777070754,
            "original": true,
            "dz": 19253.25792508856
        },
        {
            "md": 20432.0,
            "inclination": 24.03,
            "azimuthTN": 279.33004102198026,
            "azimuthGN": 280.25469999999996,
            "dxTN": -3896.2755807538774,
            "dyTN": 515.4137195460914,
            "point": {
                "x": 1005885.4907372198,
                "y": 1.0229264608978745E7,
                "z": -19518.423779731376
            },
            "wgs84Longitude": -88.96999186519567,
            "wgs84Latitude": 28.174352510317206,
            "dls": 0.061079624955079494,
            "original": true,
            "dz": 19620.423779731376
        },
        {
            "md": 20833.0,
            "inclination": 24.24,
            "azimuthTN": 277.72004102198025,
            "azimuthGN": 278.64470000000006,
            "dxTN": -4058.4151437866085,
            "dyTN": 539.708562541846,
            "point": {
                "x": 1005723.7543759414,
                "y": 1.0229291519010728E7,
                "z": -19884.374013848534
            },
            "wgs84Longitude": -88.97049512834238,
            "wgs84Latitude": 28.174419286685858,
            "dls": 0.0516935496177001,
            "original": true,
            "dz": 19986.374013848534
        },
        {
            "md": 21235.0,
            "inclination": 24.2,
            "azimuthTN": 278.09004102198026,
            "azimuthGN": 279.01469999999995,
            "dxTN": -4221.764337488532,
            "dyTN": 562.3893678619809,
            "point": {
                "x": 1005560.7823794575,
                "y": 1.0229316834650509E7,
                "z": -20250.98896295329
            },
            "wgs84Longitude": -88.97100214596055,
            "wgs84Latitude": 28.1744816216259,
            "dls": 0.011714258648538969,
            "original": true,
            "dz": 20352.98896295329
        },
        {
            "md": 21438.0,
            "inclination": 24.24,
            "azimuthTN": 280.3000410219802,
            "azimuthGN": 281.2247,
            "dxTN": -4303.959440466983,
            "dyTN": 575.6960315556023,
            "point": {
                "x": 1005478.8076309419,
                "y": 1.022933146700306E7,
                "z": -20436.124146826463
            },
            "wgs84Longitude": -88.97125727135625,
            "wgs84Latitude": 28.174518196221136,
            "dls": 0.1341086390276417,
            "original": true,
            "dz": 20538.124146826463
        },
        {
            "md": 21841.0,
            "inclination": 24.18,
            "azimuthTN": 279.73004102198024,
            "azimuthGN": 280.65470000000005,
            "dxTN": -4466.702357059678,
            "dyTN": 604.4370236203215,
            "point": {
                "x": 1005316.5396230301,
                "y": 1.0229362832652573E7,
                "z": -20803.68020837203
            },
            "wgs84Longitude": -88.97176241064415,
            "wgs84Latitude": 28.17459719639309,
            "dls": 0.017964545840061342,
            "original": true,
            "dz": 20905.68020837203
        },
        {
            "md": 22241.0,
            "inclination": 24.25,
            "azimuthTN": 280.4800410219803,
            "azimuthGN": 281.40470000000005,
            "dxTN": -4628.218731002288,
            "dyTN": 633.2236150146359,
            "point": {
                "x": 1005155.4987740328,
                "y": 1.022939422411714E7,
                "z": -21168.48616893021
            },
            "wgs84Longitude": -88.9722637437022,
            "wgs84Latitude": 28.174676320505615,
            "dls": 0.023661232974607517,
            "original": true,
            "dz": 21270.48616893021
        },
        {
            "md": 22643.0,
            "inclination": 24.27,
            "azimuthTN": 277.95004102198027,
            "azimuthGN": 278.8747000000001,
            "dxTN": -4791.224959355783,
            "dyTN": 659.6673484409741,
            "point": {
                "x": 1004992.930321372,
                "y": 1.0229423296941916E7,
                "z": -21534.995738440084
            },
            "wgs84Longitude": -88.9727697007261,
            "wgs84Latitude": 28.17474899852435,
            "dls": 0.07758532819385967,
            "original": true,
            "dz": 21636.995738440084
        },
        {
            "md": 23043.0,
            "inclination": 24.27,
            "azimuthTN": 275.68004102198034,
            "azimuthGN": 276.6047,
            "dxTN": -4954.449681780705,
            "dyTN": 679.1741778393875,
            "point": {
                "x": 1004830.0313919659,
                "y": 1.0229445436867824E7,
                "z": -21899.651244435518
            },
            "wgs84Longitude": -88.97327633342158,
            "wgs84Latitude": 28.174802595418072,
            "dls": 0.06997526141735128,
            "original": true,
            "dz": 22001.651244435518
        },
        {
            "md": 23445.0,
            "inclination": 24.09,
            "azimuthTN": 275.6400410219803,
            "azimuthGN": 276.5647,
            "dxTN": -5118.307853172504,
            "dyTN": 695.4141919625326,
            "point": {
                "x": 1004666.4462921848,
                "y": 1.0229464320430899E7,
                "z": -22266.3808827173
            },
            "wgs84Longitude": -88.97378493120767,
            "wgs84Latitude": 28.174847205203932,
            "dls": 0.013488367387192701,
            "original": true,
            "dz": 22368.3808827173
        },
        {
            "md": 23847.0,
            "inclination": 24.05,
            "azimuthTN": 276.38004102198033,
            "azimuthGN": 277.3047,
            "dxTN": -5281.36041895318,
            "dyTN": 712.5797662231128,
            "point": {
                "x": 1004503.6816442957,
                "y": 1.0229484116499197E7,
                "z": -22633.426961048004
            },
            "wgs84Longitude": -88.97429102938884,
            "wgs84Latitude": 28.17489435904305,
            "dls": 0.022720007997705665,
            "original": true,
            "dz": 22735.426961048004
        },
        {
            "md": 24246.0,
            "inclination": 24.11,
            "azimuthTN": 277.9000410219803,
            "azimuthGN": 278.8247,
            "dxTN": -5442.881733378979,
            "dyTN": 732.8154992148867,
            "point": {
                "x": 1004342.4976601197,
                "y": 1.0229506957817847E7,
                "z": -22997.708186574735
            },
            "wgs84Longitude": -88.97479237677145,
            "wgs84Latitude": 28.17494995571373,
            "dls": 0.04684646789082107,
            "original": true,
            "dz": 23099.708186574735
        },
        {
            "md": 24649.0,
            "inclination": 24.11,
            "azimuthTN": 277.36004102198024,
            "azimuthGN": 278.28469999999993,
            "dxTN": -5606.043954283092,
            "dyTN": 754.6730650456681,
            "point": {
                "x": 1004179.6990169701,
                "y": 1.0229531447356999E7,
                "z": -23365.552088095134
            },
            "wgs84Longitude": -88.9752988186274,
            "wgs84Latitude": 28.175010010587343,
            "dls": 0.01642063009914125,
            "original": true,
            "dz": 23467.552088095134
        },
        {
            "md": 24701.0,
            "inclination": 24.24,
            "azimuthTN": 277.34004102198026,
            "azimuthGN": 278.26469999999995,
            "dxTN": -5627.164269453176,
            "dyTN": 757.3973812299407,
            "point": {
                "x": 1004158.6240670707,
                "y": 1.0229534512373228E7,
                "z": -23412.991620384313
            },
            "wgs84Longitude": -88.97536437430273,
            "wgs84Latitude": 28.175017495362656,
            "dls": 0.07514870723753861,
            "original": true,
            "dz": 23514.991620384313
        },
        {
            "md": 25102.0,
            "inclination": 24.06,
            "azimuthTN": 280.3900410219803,
            "azimuthGN": 281.3147,
            "dxTN": -5789.215591059379,
            "dyTN": 782.657168790362,
            "point": {
                "x": 1003996.9911191234,
                "y": 1.022956238599345E7,
                "z": -23778.909405642728
            },
            "wgs84Longitude": -88.97586737051708,
            "wgs84Latitude": 28.17508690599476,
            "dls": 0.0943109605865223,
            "original": true,
            "dz": 23880.909405642728
        },
        {
            "md": 25461.0,
            "inclination": 23.890000000000004,
            "azimuthTN": 279.06004102198017,
            "azimuthGN": 279.9847,
            "dxTN": -5932.985039209643,
            "dyTN": 807.3025359232693,
            "point": {
                "x": 1003853.6288919606,
                "y": 1.0229589350177217E7,
                "z": -24106.93821784778
            },
            "wgs84Longitude": -88.97631362279049,
            "wgs84Latitude": 28.17515463235992,
            "dls": 0.047341854979299776,
            "original": true,
            "dz": 24208.93821784778
        },
        {
            "md": 25864.0,
            "inclination": 24.09,
            "azimuthTN": 277.6800410219803,
            "azimuthGN": 278.6047,
            "dxTN": -6095.080991391844,
            "dyTN": 831.1443629643811,
            "point": {
                "x": 1003691.9283606899,
                "y": 1.0229615806675304E7,
                "z": -24475.12839544613
            },
            "wgs84Longitude": -88.97681675800685,
            "wgs84Latitude": 28.175220139544237,
            "dls": 0.044340571283351504,
            "original": true,
            "dz": 24577.12839544613
        },
        {
            "md": 26266.0,
            "inclination": 24.13,
            "azimuthTN": 277.9000410219803,
            "azimuthGN": 278.8247,
            "dxTN": -6257.778124755035,
            "dyTN": 853.4025366907377,
            "point": {
                "x": 1003529.6010899324,
                "y": 1.0229640689338844E7,
                "z": -24842.059147500608
            },
            "wgs84Longitude": -88.97732175892621,
            "wgs84Latitude": 28.17528128895906,
            "dls": 0.007340871093596684,
            "original": true,
            "dz": 24944.059147500608
        },
        {
            "md": 26668.0,
            "inclination": 23.99,
            "azimuthTN": 273.88004102198033,
            "azimuthGN": 274.8047,
            "dxTN": -6420.714644675607,
            "dyTN": 870.2275790262621,
            "point": {
                "x": 1003366.9467191906,
                "y": 1.0229660143099142E7,
                "z": -25209.15792593414
            },
            "wgs84Longitude": -88.97782750031578,
            "wgs84Latitude": 28.175327493184092,
            "dls": 0.1227320183787899,
            "original": true,
            "dz": 25311.15792593414
        },
        {
            "md": 27067.0,
            "inclination": 24.080000000000002,
            "azimuthTN": 276.73004102198024,
            "azimuthGN": 277.65470000000005,
            "dxTN": -6582.483909050996,
            "dyTN": 885.2559636840452,
            "point": {
                "x": 1003205.4304921271,
                "y": 1.022967778148753E7,
                "z": -25573.575784575307
            },
            "wgs84Longitude": -88.97832961808528,
            "wgs84Latitude": 28.175368754565024,
            "dls": 0.08753173003256008,
            "original": true,
            "dz": 25675.575784575307
        },
        {
            "md": 27295.0,
            "inclination": 24.11,
            "azimuthTN": 277.08004102198026,
            "azimuthGN": 278.00469999999996,
            "dxTN": -6674.8895476363305,
            "dyTN": 896.446691512397,
            "point": {
                "x": 1003113.2114413108,
                "y": 1.0229690462934008E7,
                "z": -25781.710206136308
            },
            "wgs84Longitude": -88.97861643890018,
            "wgs84Latitude": 28.175399491165578,
            "dls": 0.01921091257938385,
            "original": true,
            "dz": 25883.710206136308
        },
        {
            "md": 27386.0,
            "inclination": 24.11,
            "azimuthTN": 277.08004102198026,
            "azimuthGN": 278.00469999999996,
            "dxTN": -6711.7786737639535,
            "dyTN": 901.0284259417351,
            "point": {
                "x": 1003076.3986440353,
                "y": 1.0229695639769737E7,
                "z": -25864.771629692368
            },
            "wgs84Longitude": -88.97873094035195,
            "wgs84Latitude": 28.17541207568148,
            "dls": 0.0,
            "original": true,
            "dz": 25966.771629692368
        }
    ],
    "localCRS": "{\"lateBoundCRS\":{\"name\":\"Azimuthal Equidistant\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"Azimuthal Equidistant Lng=-88.95787999;Lat=28.17268029\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Modified Azimuthal_Equidistant\\\"],PARAMETER[\\\"False_Easting\\\",0.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",-88.95787999066273],PARAMETER[\\\"Latitude_Of_Origin\\\",28.172680287395558],UNIT[\\\"Foot_US\\\",0.3048006096012192]]\"},\"name\":\"Azimuthal Equidistant - NAD_1927_To_WGS_1984_79_CONUS\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15851\"},\"name\":\"NAD_1927_To_WGS_1984_79_CONUS\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],OPERATIONACCURACY[5.0],AUTHORITY[\\\"EPSG\\\",15851]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
    "method": "AzimuthalEquidistant",
    "operationsApplied": [
        "derived TN from GN azimuth by grid convergence 359.075341",
        "computed deflections via minimum curvature method",
        "computation method: AzimuthalEquidistant",
        "conversion from 'Azimuthal Equidistant' to 'GCS_North_American_1927'",
        "conversion from 'GCS_North_American_1927' to 'NAD_1927_BLM_Zone_16N'",
        "to WGS 84: conversion from NAD_1927_BLM_Zone_16N to GCS_North_American_1927; 86 points converted",
        "to WGS 84: transformation GCS_North_American_1927 to GCS_WGS_1984 using NAD_1927_To_WGS_1984_79_CONUS; 86 points successfully transformed"
    ],
    "inputKind": "MD_Incl_Azim"
}
```

</details>



### 5.2.1 Python script to help generate the Request for test data

A simply python script is provided below that was used to generate the
above request from an Excel file.

```python
# Script to help create the request body for convertTrajectory.
# Input is an MS Excel file with 3 columns with header row "md", "azimuth", "inclination" (followed by N data rows with the directional survey data).
# Output are the inputStations parameter. The user needs to copy/paste this in an existing request body for convertTrajectory containing the other required parameters.

import xlrd
from collections import OrderedDict
import json

# Open the workbook and select first sheet in the excel spreadsheet(Sheet1) 
wb = xlrd.open_workbook("data.xlsx")
sheet = wb.sheet_by_name("Sheet1")

# Create a list to hold dictionaries
data_list = []

no_of_rows = sheet.nrows

# Iterate through each row in worksheet and fetch values into dictionary
# OrderedDict is used since it preserves the order in which keys are inserted
for rownum in range(1,no_of_rows):
    data = OrderedDict()
    row_values = sheet.row_values(rownum)
    data["md"] = row_values[0]
    data["azimuth"] = row_values[1]
    data["inclination"] = row_values[2]
    data_list.append(data)

# Write to json file
with open("request.json","w",encoding="utf-8") as write_json_file:
    json.dump(data_list,write_json_file,indent=4,default=str)
```


### 5.2.2 Correctness and performance

The output of the OSDU *convertTrajectory* has been successfully
compared against the spreadsheet which contains deemed correct results.
The results are as expected and close to each other. Of note is that
compared to the method documentation in IOGP GN 373-07-2:

- Minimum curvature is used to compute unscaled local coordinates
  (called *deflections* in the OSDU output). These are not output in the
  response body. However “True North” (TN) offsets are output, which are
  unscaled. It is not possible to change this calculation method with an
  input option.

- The `AzimuthalEquidistant` method is equal to `GNL` method with
  *psf_flag=1*, i.e., a constant correction is made for the point scale
  factor of the map projection computed at the tie-in point and applied
  to all horizontal offsets. A constant grid convergence (computed at
  the tie-in point) is applied in OSDU to convert between GN and TN
  azimuths in this method.

- The `LMP` method always uses the depth correction factor (*dcf_flag=1*).
  This is a very small correction to account for the fact that a
  distance between points over the ellipsoid surface (of the map
  projection) gets smaller with depth inside the earth. There is no
  option to turn this off with a parameter. It could be removed if so
  desired similar to unscaling for *psf*.

**Performance**

A simple test shows that for a wellbore of 30km extreme reach using 1016
survey stations the response time (wall clock) was 3.4 seconds the first
time and 0.6s and 0.7s thereafter (`AzimuthalEquidistant` method;
Interpolation was set to false). Regular surveys have typically around
100 stations (1 stations every 90 ft).

## 5.3 “Unscaling” the calculated wellbore path

The following example is given to demonstrate how to “back out” the
applied point scale factor. However, in practice this is an undesired
way of doing things. There should be an option in the API to not apply
constant point scale factor correction, such that application developers
can present such option to users. However, if in the meantime until such
options comes available it is required to perform this correction in a
slightly awkward way then this can be achieved as follows.

The `LMP` and `AzimuthalEquidistant` methods always apply the local point
scale factor (*psf*) at the SHL in the projected CRS. There is no option
to turn this off. If an operator would wants unscaled results then this
can be reversed engineered from the response. Namely, the returned
offsets aligned with True North are unscaled and the calculated grid
coordinates are scaled. Hence, the *psf* can be determined by the ratio
of the length of the TN vector and the GN vector. Once this is
calculated, the wellbore path in the projected CRS can be (inversely)
scaled to remove the application of the *psf* by the algorithm.

- This requires the unitXY to be set to that of the projected CRS, at
  least both should be in meters or feet (the type of foot is not
  relevant because these are small offsets).

  - Possible issue: if using the record id for the UOM as obtained from
    the projCRS this may go wrong with ft versions because the parsing
    seems very finicky. The closing “:” is not there and “ft\[US\]” is
    written in url encoding it appears as ft%5BUS%5D but this does not
    seem to work.

Copied from the example presented above, the output for the first point
was:

```json
{
            "md": 0.0,
            "inclination": 0.0,
            "azimuthTN": 4.1021980223376886E-5,
            "azimuthGN": 0.924699999999973,
            "dxTN": 0.0,
            "dyTN": 0.0,
            "point": {
                "x": 1009773.1700000245,
                "y": 1.0228686349999696E7,
                "z": 102.0
            },
            "wgs84Longitude": -88.9578984522382,
            "wgs84Latitude": 28.172935420826686,
            "dls": 0.0,
            "original": true,
            "dz": 0.0
        },
```


And for the last point:

```json
        {
            "md": 27386.0,
            "inclination": 24.11,
            "azimuthTN": 277.08004102198026,
            "azimuthGN": 278.00469999999996,
            "dxTN": -6711.7786737639535,
            "dyTN": 901.0284259417351,
            "point": {
                "x": 1003076.3986440353,
                "y": 1.0229695639769737E7,
                "z": -25864.771629692368
            },
            "wgs84Longitude": -88.97873094035195,
            "wgs84Latitude": 28.17541207568148,
            "dls": 0.0,
            "original": true,
            "dz": 25966.771629692368
        }
    ],
```


The 2D (horizontal) length between the first and last point computed
from the dxTN and dyTN: dTN = 6771.99 ft (by applying Pythagoras).

The 2D (horizontal) length between the first and last point computed
from the dxGN and dyGN: dGN = 6772.40 ft (where dxGN = x\[N\]-x\[1\] and
dyGN = y\[N\]-y\[1\]).

- Hence, *psf* = dGN / dTN = 1.0000609.

  - This was checked with a geodetic calculator to indeed correspond to
    the point scale factor at the location in the given projected CRS.

To calculated path can be “unscaled” by applying this factor in reverse
as follows for i=1:N:

- *x_unscaled\[i\] = x\[1\] + (x\[i\] - x\[1\]) / psf*

- *y_unscaled\[i\] = y\[1\] + (y\[i\] - y\[1\]) / psf*

Issue: Create an enhancement request to add an option to output unscaled GNL method.

## 5.4 A trick to get scale and convergence at any location

There is no endpoint to compute point scale factor and grid convergence.
However, as shown above, the convertTrajectory endpoint can be used to
obtain the point scale factor of the map projection at the tie-in point
location. Aside from the *psf*, *grid convergence* can be computed from
the output of convertTrajectory by simply subtracting the azimuthGN from
the azimuthTN (The Gauss-Bomford convention is used in OSDU):

- Grid convergence = GC = azimuthTN - azimuthGN (in degrees)

  - *if GC\>180 then GC -= 360; (force it to be close to 0 by
    convention. GC is always a small angle)*

Note that in /convertTrajectory AzimuthalEquidistant method the grid
convergence is applied as a constant (of the first station tie-in point)
along the full trajectory. This can be seen from the output of the full
example above:

- First station (top hole): 0.0 - 0.9247 = -0.9247

- Last station (bottom hole): 277.0800 - 278.0047 = -0.9247

If it is desirable to know the *psf* at a specific location, e.g., at
the BHL of the wellbore then the following can be done:

- Create a dummy (fake/artificial) directional survey at the desired
  referencePoint location, creating a "path", going due north (GN) with
  a (true) horizontal length of 100m (i.e., set inclination=90 and
  azimuth=0).

  - Here, the unitXY is set to meters, regardless of the unit of the CRS.

  - If the desired location is the BHL then use those coordinates, e.g.,
    in the above example "x": 1003076.3986440353, "y": 1.0229695639769737E7.

    - *(The accuracy of the result can insignificantly be improved by
      centering the dummy stations around the desired point, i.e., by
      subtracting 50 from* referencePoint.y*. The result will be
      insignificantly different for this purpose which only requires 2
      decimals of precision.)*

- Compute the psf(location) analogously to what was done above by the
  ratio of the GN and TN distance.

  - dGN = sqrt(x[2]-x[1])^2 + y[2]-y[1])^2) = 100.0066

  - dTN = 100.0 (by definition, but this can be demonstrated by
    Pythagoras on the dxTN and dyTN of the second point, which computes
    to this exactly)

  - *psf* = dGN / dTN = 100.0066 / 100 = 1.00007

    - Note that this is the correct psf at this location in the map, and
      this is slightly different than the value at the surface location
      (1.00006). The reason is that psf varies with map location.
      Strictly this trick assumed a conformal projection method, where
      the local scale factor psf_x=psf_y is the same in all directions,
      but this is generally the case and irrelevant for this borehole
      survey application.

- Calcualte the grid convergence at point 1:

  - *grid convergence* = azimuthTN - azimuthGN = 359.06542119937706 -
    0.0 = -0.9346 (deg)

    - Alternatively or as check: (in this case) GC = atan(dxTN[2] /
      dyTN[2]) = -0.9346 (deg)

    - Note that this is the correct GC at this location. It differs
      slightly (about 0.01 degrees) from the surface location. The
      reason is that grid convergence, like psf, slowly varies with the
      location in the map.

**Request**

```json
{
  "azimuthReference": "GN",
  "interpolate": false,
  "referencePoint": {
    "x": 1003076.3986440353,
    "y": 1.0229695639769737E7,
    "z": 0
  },
  "unitZ": "osdu:reference-data--UnitOfMeasure:m",
  "inputStations": [
    {
      "md": 0,
      "azimuth": 0,
      "inclination": 90
    },
    {
      "md": 100,
      "azimuth": 0,
      "inclination": 90
    }
  ],
  "trajectoryCRS": "{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::32066_EPSG::15851:",
  "inputKind": "MD_Incl_Azim",
  "unitXY": "osdu:reference-data--UnitOfMeasure:m",
  "method": "AzimuthalEquidistant"
}
```


**Response**

```json
{
    "trajectoryCRS": "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"32066079\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32066\"},\"name\":\"NAD_1927_BLM_Zone_16N\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"NAD_1927_BLM_Zone_16N\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",1640416.666666667],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",-87.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Foot_US\\\",0.3048006096012192],AUTHORITY[\\\"EPSG\\\",32066]]\"},\"name\":\"NAD27 * OGP-Usa Conus / BLM zone 16N (US survey feet) [32066,15851]\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15851\"},\"name\":\"NAD_1927_To_WGS_1984_79_CONUS\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],OPERATIONACCURACY[5.0],AUTHORITY[\\\"EPSG\\\",15851]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
    "unitXY": "{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}",
    "unitZ": "{\"scaleOffset\":{\"scale\":1.0,\"offset\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"Length\",\"type\":\"UM\"},\"type\":\"USO\"}",
    "unitDls": "{\"scaleOffset\":{\"scale\":5.81776417331443E-4,\"offset\":0.0},\"symbol\":\"deg/30m\",\"baseMeasurement\":{\"ancestry\":\"Rotation_Per_Length\",\"type\":\"UM\"},\"type\":\"USO\"}",
    "stations": [
        {
            "md": 0.0,
            "inclination": 90.0,
            "azimuthTN": 359.06542119937706,
            "azimuthGN": 0.0,
            "dxTN": 0.0,
            "dyTN": 0.0,
            "point": {
                "x": 1003076.3986440588,
                "y": 1.0229695639769427E7,
                "z": 0.0
            },
            "wgs84Longitude": -88.97873094035187,
            "wgs84Latitude": 28.175412075680626,
            "dls": 0.0,
            "original": true,
            "dz": 0.0
        },
        {
            "md": 100.0,
            "inclination": 90.0,
            "azimuthTN": 359.06542119937706,
            "azimuthGN": 0.0,
            "dxTN": -1.6310753882904785,
            "dyTN": 99.98669708055023,
            "point": {
                "x": 1003076.3986513687,
                "y": 1.0229795646348633E7,
                "z": -6.123233995736766E-15
            },
            "wgs84Longitude": -88.97873600956741,
            "wgs84Latitude": 28.175687077949142,
            "dls": 0.0,
            "original": true,
            "dz": 6.123233995736766E-15
        }
    ],
    "localCRS": "{\"lateBoundCRS\":{\"name\":\"Azimuthal Equidistant\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"Azimuthal Equidistant Lng=-88.97871162;Lat=28.17515695\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Modified Azimuthal_Equidistant\\\"],PARAMETER[\\\"False_Easting\\\",0.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",-88.97871161844904],PARAMETER[\\\"Latitude_Of_Origin\\\",28.175156948067688],UNIT[\\\"Foot_US\\\",0.3048006096012192]]\"},\"name\":\"Azimuthal Equidistant - NAD_1927_To_WGS_1984_79_CONUS\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"15851\"},\"name\":\"NAD_1927_To_WGS_1984_79_CONUS\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"NAD_1927_To_WGS_1984_79_CONUS\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_conus\\\",0.0],OPERATIONACCURACY[5.0],AUTHORITY[\\\"EPSG\\\",15851]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
    "method": "AzimuthalEquidistant",
    "operationsApplied": [
        "derived TN from GN azimuth by grid convergence 359.065421",
        "computed deflections via minimum curvature method",
        "computation method: AzimuthalEquidistant",
        "conversion from 'Azimuthal Equidistant' to 'GCS_North_American_1927'",
        "conversion from 'GCS_North_American_1927' to 'NAD_1927_BLM_Zone_16N'",
        "to WGS 84: conversion from NAD_1927_BLM_Zone_16N to GCS_North_American_1927; 2 points converted",
        "to WGS 84: transformation GCS_North_American_1927 to GCS_WGS_1984 using NAD_1927_To_WGS_1984_79_CONUS; 2 points successfully transformed"
    ],
    "inputKind": "MD_Incl_Azim"
}
```


- Issue: Enhancement request for a new endpoint that outputs scale and
  convergence at a specific point(s) (using SIS or the above trick).

- Issue: what is record id for ftUS and does it guarantee to return ft
  if “ft” is used or ft[US] or ft[GC] ? (“ft%5BUS%5D” in url encoding)


# 6. Wellbore interpolation on MD

Interpolation on MD is not currently possible and identified as a gap in
the OSDU.

ConvertTrajectory has an interpolation option, but that is a Boolean.
This option will add interpolated points if the distance between survey
stations is more than 1000 (z-units). It may be the easiest to extent the
request to include interpolation points, as well as to prescribe an
interpolation interval as value.

- Issue: Enhancement request: change interpolation to a regular interval that can be specified as a number value 
  (e.g., "1.0" to interpolate every 1 z-unit).
- Issue: New feature: add option to convertTrajectory to interpolate at given input MD values.
- Issue: Enhancement suggested: improve request body to use a record id for the
  wellbore (that seems more tricky since the survey data are stored file
  based and not in a relation database).


# 7. QC and convert Bin Grid

## 7.1 Context

A Bin Grid describe the
“real world” (Easting, Northing) of bin grid centers at (inline,
crossline) local coordinates.  The math formulas are defined in 
- [SDU geometric aspects of bin grids.docx](SDU_geometric_aspects_of_bin_grids.docx), and 
- [SDU geometric aspects of bin grids.xlsx](SDU_geometric_aspects_of_bin_grids.xlsx).

The figure below shows the four-point bin grid definition using the projected and bin grid local coordinates at 4 corner tie points. 
The main advantage of the 4 Corner definition is that it is very straightforward and unlikely to be misinterpreted.
The disadvantage is that one cannot calculate with the corners, and must derive the P6 parameters. The derived (calculated) spacings may not be exactly an integer or multiple of 6.25 m.

The formulas to compute (inline,crossline) at a given (Easting, Northing) with the P6 parameters are described in section 4 and 5 in the above referenced document.

In this definition:
* Point A is the point with minimum inline and crossline. 
* Line A->B is a constant inline (increasing crossline coordinates).
* Line A->C is a constant crossline (increasing inline coordinates). 
* Point D complements a rectangle (in the (inline,crossline) space and in the (Easting,Northing) space). Point D is redundant and used for QC. It would be trivial to extend the SDU format to include a three-point definition method by omitting the D point, but this is not recommended because the D point facilitates QC and spatializing the grids.


![Figure 1: Bin Grid terminology.](ABCDBinGrid.jpg 'Figure 1: Bin Grid terminology.')



### 7.2 Description of CRS Convert POST v3/convertBinGrid

The CRS Convert service POST v3/convertBinGrid endpoint is an OSDU
platform standard method for QC and conversion of Bin Grids, associated
in particular with ingested seismic volumes. 
This endpoint takes an AbstractBinGrid as
input and “enriches” it by returning computed properties on output:

- Optionally, convert the Bin Grid to a new CRS and “square it up” (if
  target CRS is same as original CRS then conversion is omitted, and the
  squareness test is done in the original CRS).
- The derived P6 parameters (calculated from the input 4 corners
  coordinates).
- Sorts the 4 corners in order ABCD on output. This order is (inl, xln) =
  (min, min), (min, max), (max, min), (max, max), for the ABCDBinGridSpatialLocation Points.
- Computes the WGS 84 (lat,lon) coordinates at the corners.
- Output a QC check of the “squareness” of the input Bin Grid defined using 4
  corner points, expressed in easily human interpretable metric (partial
  bins).
- Additionally, output an AbstractSpatialLocation that maps the BinGrid as a Polygon,
  to be stored as the SeismicBinGrid SpatialArea, and picked up by the
  Geospatial Consumption Zone transformer.

### 7.2.1 Request Body

The input and output of this method use the
[AbstractBinGrid:1.0.0](https://community.opengroup.org/osdu/data/data-definitions/-/blob/master/E-R/abstract/AbstractBinGrid.1.0.0.md)
definition. On input a minimum required properties can be given, which
are enriched on output as indicated.

<table>
<colgroup>
<col style="width: 11%" />
<col style="width: 16%" />
<col style="width: 70%" />
<col style="width: 1%" />
</colgroup>
<thead>
<tr class="header">
<th><strong>Parameter name</strong></th>
<th><strong>Data type</strong></th>
<th><strong>Description</strong></th>
<th></th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>inBinGrid</td>
<td>AbstractBinGrid (1.0.0)</td>
<td>Mandatory. See table below for required properties on input and
expected structure of the AsIngestedCoordinates array.</td>
<td></td>
</tr>
<tr class="even">
<td>toCrs</td>
<td>string (record-id)</td>
<td><p>Optional. If omitted, then no conversion is performed and only
the P6 parameters are computed and the check for “squareness” that
indicates non-orthogonality, as well as the WGS 84 cordinates.</p>
<p>Record-id of the desired output CRS of bin grid to convert to,
e.g.,</p>
<p>"{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::32064_EPSG::15851:".</p></td>
<td></td>
</tr>
</tbody>
</table>

**NOTE**: Usage of ABCDBinGridLocalCoordinates and
AbstractCoordinates is **deprecated**. Instead the AnyCrsFeatureCollection
GeoJson construct with Feature properties should be used as show below (`Inline`, `Crossline`).

Click on expand to show an example ABCDBinGridSpatialLocation containing the local and global
coordinates on input. 
This is not a numerically realistic example.
It is using Coordinate Reference System "NAD83 / UTM zone 15N", CRS code EPSG::26915, 
bound with Coordinate Transformation "NAD83 to WGS 84 (1)", CT code EPSG::1188.

<details><summary>Click to expand</summary>

```json
{
    "BinGridName": "ST0202R08_PS_PSDM_RAW_PP_TIME.MIG_RAW.POST_STACK.3D.JS-017534",
    "ABCDBinGridSpatialLocation": {
        "AsIngestedCoordinates": {
            "type": "AnyCrsFeatureCollection",
            "CoordinateReferenceSystemID": "{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::26915_EPSG::1188:",
            "features": [
                {
                    "type": "AnyCrsFeature",
                    "properties": {
                        "Kind": "osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0",
                        "PointProperties": [
                            {
                                "Label": "A",
                                "Inline": 1,
                                "Crossline": 1000
                            }
                        ]
                    },
                    "geometry": {
                        "type": "AnyCrsPoint",
                        "coordinates": [
                            500000.0,
                            3000000.0
                        ]
                    }
                },
                {
                    "type": "AnyCrsFeature",
                    "properties": {
                        "Kind": "osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0",
                        "PointProperties": [
                            {
                                "Label": "B",
                                "Inline": 1,
                                "Crossline": 2000
                            }
                        ]
                    },
                    "geometry": {
                        "type": "AnyCrsPoint",
                        "coordinates": [
                            500000.0,
                            3100000.0
                        ]
                    }
                },
                {
                    "type": "AnyCrsFeature",
                    "properties": {
                        "Kind": "osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0",
                        "PointProperties": [
                            {
                                "Label": "C",
                                "Inline": 101,
                                "Crossline": 1000
                            }
                        ]
                    },
                    "geometry": {
                        "type": "AnyCrsPoint",
                        "coordinates": [
                            600000.0,
                            3000000.0
                        ]
                    }
                },
                {
                    "type": "AnyCrsFeature",
                    "properties": {
                        "Kind": "osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0",
                        "PointProperties": [
                            {
                                "Label": "D",
                                "Inline": 101,
                                "Crossline": 2000
                            }
                        ]
                    },
                    "geometry": {
                        "type": "AnyCrsPoint",
                        "coordinates": [
                            600000.0,
                            3100000.0
                        ]
                    }
                }
            ]
        }
    }
}
```

</details>


### 7.2.2 Response Body

The response is a measure of the computed “non-squareness” (dI,dJ) of the input BinGrid, 
and an output BinGrid which is enriched with derived information 
(augmented with the derived P6 parameters filled out, 
and optionally (if a toCrs was given in the request) converted
global coordinates that are “squared up” in the new geometry (which can
be used in applications that require a square grid in a project CRS
geometry; if the “squaring error” is small enough).

<table>
<colgroup>
<col style="width: 24%" />
<col style="width: 18%" />
<col style="width: 55%" />
<col style="width: 1%" />
</colgroup>
<thead>
<tr class="header">
<th><strong>Parameter name</strong></th>
<th><strong>Data type</strong></th>
<th><strong>Description</strong></th>
<th></th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><strong>outBinGrid</strong></td>
<td>AbstractBinGrid</td>
<td>See table below for required properties. The main properties are
those that hold the (inline, crossline) coordinates at the four ABCD
corners (in that order), and those that hold the global (Easting,
Northing) coordinates.</td>
<td></td>
</tr>
<tr class="even">
<td><p><strong>maxMislocation[].dI</strong></p>
<p><strong>maxMislocation[].dJ</strong></p></td>
<td><p>Float</p>
<p>Float</p></td>
<td>Max mis-location (dI, dJ) expressed in fractional bins at increment
1 in direction of the inline and crossline, respectively. This should be
compared to the real spacing (in bins) of the volume loaded. For
example, if the volume uses incr=2 then an error of 1 bin is only half
an increment "real" bin. For interpretation, normally acceptable would
be 1 real (incremented) bin mis-placement. Moreover, since the
mis-location is zero in the middle and the maximum at the corners of the
grid, the strict criteria of 1 bin could be relaxed slightly.</td>
<td></td>
</tr>
</tbody>
</table>


`outBinGrid` properties are populated as shown below, depending on whether
a conversion was requested using the optional "toCrs" parameter.

Click on "expand" to show the example for global coordinates on output, showing the relevant
geometry properties (the converted and “squared up” x,y coordinates).
These are the output, "squared up" 4 corners in order A, B, C, D as defined above.
Additionally the Wgs84Coordinates are populated, as well as the various calculated P6 parameters.

<details><summary>Click to expand Output Example (no realistic values)</summary>

```json
{
    "ABCDBinGridSpatialLocation": {
        "AsIngestedCoordinates": {
            "type": "AnyCrsFeatureCollection",
            "CoordinateReferenceSystemID": "{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::32064_EPSG::15851:",
            "features": [
                {
                    "type": "AnyCrsFeature",
                    "properties": {
                        "Kind": "osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0",
                        "PointProperties": [
                            {
                                "Label": "A",
                                "Inline": 1,
                                "Crossline": 1000
                            }
                        ]
                    },
                    "geometry": {
                        "type": "AnyCrsPoint",
                        "coordinates": [
                            3593536.4609,
                            9888463.8749
                        ]
                    }
                },
                {
                    "type": "AnyCrsFeature",
                    "properties": {
                        "Kind": "osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0",
                        "PointProperties": [
                            {
                                "Label": "B",
                                "Inline": 1,
                                "Crossline": 2000
                            }
                        ]
                    },
                    "geometry": {
                        "type": "AnyCrsPoint",
                        "coordinates": [
                            3577506.2747,
                            10217819.3106
                        ]
                    }
                },
                {
                    "type": "AnyCrsFeature",
                    "properties": {
                        "Kind": "osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0",
                        "PointProperties": [
                            {
                                "Label": "C",
                                "Inline": 101,
                                "Crossline": 1000
                            }
                        ]
                    },
                    "geometry": {
                        "type": "AnyCrsPoint",
                        "coordinates": [
                            3922894.4303,
                            9904494.1844
                        ]
                    }
                },
                {
                    "type": "AnyCrsFeature",
                    "properties": {
                        "Kind": "osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0",
                        "PointProperties": [
                            {
                                "Label": "D",
                                "Inline": 101,
                                "Crossline": 2000
                            }
                        ]
                    },
                    "geometry": {
                        "type": "AnyCrsPoint",
                        "coordinates": [
                            3906864.2441,
                            10233849.6201
                        ]
                    }
                }
            ]
        },
        "Wgs84Coordinates": {
            "type": "FeatureCollection",
            "features": [
                {
                    "type": "Feature",
                    "properties": {
                        "Kind": "osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0",
                        "PointProperties": [
                            {
                                "Label": "A",
                                "Inline": 1,
                                "Crossline": 1000
                            }
                        ]
                    },
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            1.9496875,
                            58.4141503
                        ]
                    }
                },
                {
                    "type": "Feature",
                    "properties": {
                        "Kind": "osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0",
                        "PointProperties": [
                            {
                                "Label": "B",
                                "Inline": 1,
                                "Crossline": 2000
                            }
                        ]
                    },
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            1.9683358,
                            58.4561357
                        ]
                    }
                },
                {
                    "type": "Feature",
                    "properties": {
                        "Kind": "osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0",
                        "PointProperties": [
                            {
                                "Label": "C",
                                "Inline": 101,
                                "Crossline": 1000
                            }
                        ]
                    },
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            1.8237808,
                            58.4294624
                        ]
                    }
                },
                {
                    "type": "Feature",
                    "properties": {
                        "Kind": "osdu:wks:AbstractGeoJson.PropertiesBinGridCorners:1.0.0",
                        "PointProperties": [
                            {
                                "Label": "D",
                                "Inline": 101,
                                "Crossline": 2000
                            }
                        ]
                    },
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            1.8422867,
                            58.4714655
                        ]
                    }
                }
            ]
        },
        "AppliedOperations": [
            "AsIngestedCoordinates converted to Wgs84Coordinates: Input CRS EPSG 23031 (ED50 / UTM zone 31N) to Target CRS EPSG 4326 (WGS84) using CT EPSG 1613 (ED50 to WGS 84 (24) - Norway - offshore south of 62°N - North Sea.)"
        ]
    },
    "P6TransformationMethod": 9666,
    "P6BinGridOriginI": 9985.0,
    "P6BinGridOriginJ": 1932.0,
    "P6BinGridOriginEasting": 3593536.4609,
    "P6BinGridOriginNorthing": 9888463.8749,
    "P6BinWidthOnIaxis": 25.0,
    "P6BinWidthOnJaxis": 25.0,
    "P6MapGridBearingOfBinGridJaxis": 284.12345,
    "P6BinNodeIncrementOnIaxis": 1,
    "P6BinNodeIncrementOnJaxis": 1
} 
```
</details>



### 7.2.3 Input and output AbstractBinGrid properties

Properties of
[AbstractBinGrid:1.0.0](https://community.opengroup.org/osdu/data/data-definitions/-/blob/master/E-R/abstract/AbstractBinGrid.1.0.0.md)
on input and output are summarized in the following table.


| **AbstractBinGrid property** | **On input**          | **On output if toCRS is not given** | **On output if toCRS is given**   |
|------------------------------|-----------------------|-------------------------------------|-----------------------------------|
| BinGridName                  | ignored               | copy of input (leave blank if empty on input)  | same |
| BinGridTypeID                | ignored               | copy of input (leave blank if empty on input)  | same |
| SourceBinGridID              | ignored               | copy of input (leave blank if empty on input)  | same |
| SourceBinGridAppID           | ignored               | copy of input (leave blank if empty on input)  | same |
| CoveragePercent              | ignored               | copy of input (leave blank if empty on input)  | same | 
| BinGridDefinitionMethodTypeID   | ignored               | “4Corner”  | “4Corner” |
|                                 |                       |                                                |      |
| ~~ABCDBinGridLocalCoordinates~~ | **deprecated usage**  | ignored  | ignored |
|                                 |                       |                                                |      |
| **ABCDBinGridSpatialLocation**      | Required            |    _(type AbstractSpatialLocation:1.1.0)_   |   |
| > **_.AsIngestedCoordinates_**      | Required           |    _(type AbstractAnyCrsFeatureCollection:1.1.0)_  |  |
| >> .CoordinateReferenceSystemID     | Required CRS of given features[]/geometry.coordinates[]  |  copy of input    | the “toCRS” record-id |
| >> .persistableReferenceCrs         | Ignored (use CRS record-id) |  copy of input (leave blank if empty on input)    | populate with looked up PR |
| >> .features[1:4].geometry.type     | Ignored          |  **“AnyCrsPoint”**    | **“AnyCrsPoint”** |
| >> .features[1:4].geometry.coordinates[]  | Required corner coordinates (4)  |  “Squared up” global coordinates    | Converted and “squared up” global coordinates |
| > **_.Wgs84Coordinates_**                 | Ignored on input          |      |      |
| >> .features[1:4].geometry.type           | Ignored on input          |  **“Point”**    | **“Point”** |
| >> .features[1:4].geometry.coordinates[]  | Ignored on input          |  “Squared up” coordinates transformed to WGS 84.    | same |
| > .SpatialLocationCoordinatesDate     | Ignored on input          |  copy of input (if given)    | same |
| > .QualitativeSpatialAccuracyTypeID   | Ignored on input          |  copy of input (if given)    | null out |
| > .QuantitativeAccuracyBandID         | Ignored on input          |  copy of input (if given)    | null out |
| > .CoordinateQualityCheckPerformedBy  | Ignored on input          |  “CRS Convert service, POST convertBinGrid”    | same |
| >  .CoordinateQualityCheckDateTime   | Ignored on input          |   `now()`   | same |
| >  .CoordinateQualityCheckRemarks[]  | Ignored on input          |  append “Max. squaring error: dI=0.0, dJ=0.4 bin    | same |
| >  .AppliedOperations[]              | Ignored on input          |  append “squareness tested: dI=x.x, dJ=x.x bin”    | append “converted from <origCRS> to <toCRS>; "squared up": dI=x.x, dJ=x.x (bin)” |
| >  .SpatialParameterTypeID           | Ignored on input          |  copy of input (if given)    | same |
| >  .SpatialGeometryTypeID           | Ignored on input          |  copy of input (if given)    | same |
|                                |                           |                                                |      |
| P6TransformationMethod         | Ignored on input          |  populate with derived value on output    | same. 9666 for right-handed grids, 1049 for left-handed. |
| P6BinGridOriginI               | Ignored on input          |  populate with derived value on output    | same |
| P6BinGridOriginJ               | Ignored on input          |  populate with derived value on output    | same |
| P6BinGridOriginEasting         | Ignored on input          |  populate with derived value on output    | same |
| P6BinGridOriginNorthing        | Ignored on input          |  populate with derived value on output    | same |
| P6ScaleFactorOfBinGrid         | Ignored on input          |  populate with derived value on output    | same |
| P6BinWidthOnIaxis              | Ignored on input          |  populate with derived value on output    | same |
| P6BinWidthOnJaxis              | Ignored on input          |  populate with derived value on output    | same |
| P6MapGridBearingOfBinGridJaxis | Ignored on input          |  populate with derived value on output    | same |
| P6BinNodeIncrementOnIaxis      | Ignored on input          |  copy on output. If not present on input, then set to 1    | same |
| P6BinNodeIncrementOnJaxis      | Ignored on input          |  copy on output. If not present on input, then set to 1    | same |



### 7.3 Exception handling / Error codes

Error checking is performed with following exception handling and
response messages when parsing the input:

1.  Checks for ABCDBinGridSpatialLocation
    1.  Check that a `CRS record-id` is given and exists.
        * Note: BinGrids should use type BoundProjected or Projected (if based on WGS 84).
    2.  Four points are given.  
    3.  Using PropertiesBinGridCorners to give local coordinates.  
        * Note: Usage of ABCDBinGridLocalCoordinates is deprecated.
    4.  Local coordinates are sortable as A,B,C,D, i.e., in order (inline,crossline) = (I,J) =
        (minI,minJ), (minI,maxJ), (maxI,maxJ), (maxI,minJ).  
        Check that the numbers with same symbols are the same and 
        minI \< maxI and minJ \< maxJ.
2.  If toCrs is given
    1. Check that the given `CRS record-id` exists.


### 7.4 How to use CRS Convert method POST v3/convertBinGrid?

- When storing a seismic volume file (e.g., a SEGY file) into OSDU, then a `SeismicTraceData WPC` is created
  which references a `SeismicBinGrid`, which references an `AbstractBinGrid`.
  -  Create a basic AbstractBinGrid and set the 
     `CRS record-id` and 4 corner coordinates in its ABCDBinGridSpatialLocation.
  -  Call this endpoint without the optional toCrs parameter to fill out
     the `P6 parameters` and Wgs84 coordinates and to get the (`dI`,`dJ`) QC metric
     for squareness.   
  -  Check the squareness, and ingest the data into OSDU platform if it passes. That enables a systematic checking of
     ingested seismic volumes, and avoid loading wrong data to the platform. 

- The increments of the `SeismicTraceData`
  referencing a bin grid are supposed to be kept with the data, overwriting
  the SeismicBinGrid values, such that an efficient loading and referencing the
  same bin grid for data output at different increments (spacings) is enabled.
  However, a company can elect to create multiple Bin Grids and use the 
  P6BinNodeIncrementOnIaxis and P6BinNodeIncrementOnIaxis.  
- Similarly, there could be multiple seismic volumes on the same AbstractBinGrid, 
  but using a different inline, crossline range as stored with the `SeismicTraceData`. 
  These min/max inline/crossline ranges are supposed to be kept with the data, but 
  a company could elect to create a seismicBinGrid for each dataset.

- OSDU should store only the original SEGY data, and only the original
  bin grid in the original CRS. Applications that require a conversion
  to a (different) project CRS can call this endpoint and check that the
  approximation error (“squaring error”) is small enough to merge with
  other project data. 
  However, it is also possible to use the toCrs
  parameter in a second call, and store the converted BinGrid in a
  lineage as child of the original geometry. Applications can then
  search for such child with the desired `CRS record-id`.

<!---

### 7.5 Examples

Example 1:

- SourceCRS: WGS 84 / UTM zone 15N (EPSG::32615) - Not Bound (already WGS 84 based):
  {{NAMESPACE}}:reference-data--CoordinateReferenceSystem:Projected:EPSG::32615

- toCrs = NULL

*(put example here in expandable code widget)*

Example 2:

- SourceCRS: NAD83 / UTM zone 15N (EPSG::26915) - Bound with CT “NAD83
  to WGS 84 (1)”, EPSG::1188:
  {{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::26915_EPSG::1188

- TargetCRS: NAD27 / BLM 14N (ftUS) (EPSG::32064) - Bound with CT “NAD27
  to WGS 84 (79)”, EPSG::15851:
  {{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::32615_EPSG::15851

*(put example here in expandable code widget)*

--->




### 7.5 Seismic Bin Grid Spatial Area

The spatial area of the SeismicBinGrid should be written as an
(AnyCrs) Polygon with 5 nodes (the last node is a copy of the first point to close the polygon), representing the outer edges of the bin
grid. 
The outer rim is written counterclockwise, per the OGC/GeoJSON convention followed by OSDU.
The four corner points defined in the AbstractBinGrid corner points are used as follows to create the SpatialArea:

1.  Retrieve the 4 corner points from
    `AbstractBinGrid.ABCDBinGridSpatialLocation.AsIngestedCoordinates`

2.  Identify the “A”, “B”, “C”, and “D” points from the
    `ABCDBinGridSpatialLocation.AsIngestedCoordinates`, by finding the
    (minI,minJ), (minI,maxJ), (maxI,minJ), and (maxI,maxJ),
    respectively.

    * _(where minI is the minimum Inline and minJ is the minimum crossline)_

3.  Order the points for the polygon as A,B,D,C,A. 
    Test if this is a clockwise or counterclockwise simple convex polygon:
    * If Det(B) = (Xb-Xa)(Yd-Ya) - (Xd-Xa)(Yb-Ya) is positive then this simple polygon is counterclockwise.
    * _(where Xb is the Easting global coordinates for the B point, etc.)_

4.  If A,B,D,C,A is clockwise then reorder the nodes as A,C,D,B,A.

5.  Output the AnyCrsPolygon

The following example shows the closed polygon, using 5 points (last point is copy of
first point), an outer rim, meaning re-ordered to be drawable in
counter-clockwise point order.
Technically the edges might have to be densified for correct mapping the locations drawn in WGS 84 / Web Mercator.
However, this is not considered necessary because that representation is only for search and discovery.

<details><summary>SeismicBinGrid Spatial Area (click to expand)</summary>

```json
  "SpatialArea": {
    "AsIngestedCoordinates": {
      "CoordinateReferenceSystemID": "{{NAMESPACE}}:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::32064_EPSG::15851:",
      "features": [
          {
          "type": "AnyCrsFeature"
          "properties": {
              "Label": "SeismicBinGrid NAME outer rim"
          },
          "geometry": {
            "type": "AnyCrsPolygon"
            "coordinates": [  
              [
                [3593536.4609,
                 9888463.8749
                ],
                [3577506.2747,
                 10217819.3106
                ],
                [3922894.4303,
                 9904494.1844
                ],
                [3906864.2441,
                 10233849.6201
                ],
                [3593536.4609,
                 9888463.8749
                ]
              ]
            ]
          }      
        }
      ]
    },
    "Wgs84Coordinates": {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "properties": {
                      "Label": "SeismicBinGrid NAME outer rim"
                  },
                  "geometry": {
                    "type": "Polygon",
                    "coordinates": [
                      [
                        [
                          1.9496875,
                          58.4141503
                        ],
                        [
                          1.9683358,
                          58.4561357
                        ],
                        [
                          1.8422867,
                          58.4714655
                        ],
                        [
                          1.8237808,
                          58.4294624
                        ],
                        [
                          1.9496875,
                          58.4141503
                        ]
                      ]
                    ]
                  }
                }
             ]
    }
    "AppliedOperations": [
              "AsIngestedCoordinates converted to Wgs84Coordinates: Input CRS EPSG 23031 (ED50 / UTM zone 31N) to Target CRS EPSG 4326 (WGS84) using CT EPSG 1613 (ED50 to WGS 84 (24) - Norway - offshore south of 62°N - North Sea.)"
    ],
    "SpatialParameterTypeID": "osdu:reference-data--SpatialParameterType:Outline:",
    "SpatialGeometryTypeID": "osdu:reference-data--SpatialGeometryType:Polygon:"
  }
```

</details>



**FOLLOWING VOLVE EXAMPLE NEEDS TO BE aligned**

See also [Volve example](https://community.opengroup.org/osdu/platform/data-flow/data-loading/open-test-data/-/blob/master/rc--3.0.0/4-instances/Volve/work-products/seismics/load_seismic_bingrid_ST0202R08_PS_PSDM_RAW_PP_TIME.MIG_RAW.POST_STACK.3D.JS-017534.json).
