# CRS Convert helper service tutorial

*The OSDU has two CRS helper services: "`CRS Convert`" and "`CRS Catalog`".
This tutorial provides examples and background to help
application developers accomplish typical tasks using the "`CRS Convert`" endpoints.


**Table of Contents**

* [1. Introduction](#1-introduction)  
* [2. CRS Convert Overview](#2-crs-convert-overview)  
* [3. Computing a wellbore trajectory from directional survey data](#3-computing-a-wellbore-trajectory-from-directional-survey-data)  
    * [Request Parameters](#request-parameters-converttrajectoryrequest)
    * [Unit Parameters Reference](#unit-parameters-reference)
  * [3.1 Basic example](#31-basic-example)   
  * [3.2 Return point scale factor and grid convergence](#32-return-point-scale-factor-and-grid-convergence)  
  * [3.3 GNL Method](#33-gnl-method)
    * [3.3.1 Unscaling” the calculated wellbore path](#331-unscaling-the-calculated-wellbore-path)
  * [3.4 Wellbore interpolation on MD](#34-wellbore-interpolation-on-md)   
    * [3.4.1 Interpolation at a list of MD](#341-interpolation-at-a-list-of-md)
    * [3.4.2 Interpolation at a regular MD interval](#342-interpolation-at-a-regular-md-interval)
  * [3.5 Inclination-only surveys (stations have no azimuth)](#35-handle-inclination-only-surveys-stations-have-no-azimuth)  
  * [3.6 Inverse minimum curvature](#36-inverse-minimum-curvature)
* [4. Explicit Transform](#4-explicit-transform)
* [5. Compound Transform](#5-compound-transform)

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

The endpoints for CRS Convert v4 {{osduonaws_base_url}}/api/crs/converter/ are fully specified via 
the [Swagger documentation](https://community.opengroup.org/osdu/platform/system/reference/crs-conversion-service/-/blob/master/docs/v4/api_spec/crs_converter_openapi.json)
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
<td>POST</td>
<td><strong>.../v4/convertTrajectory</strong></td>
<td>Compute the wellbore trajectory based on directional survey observables. This endpoint is the enhanced version of the v3/convertTrajectory.</td>
</tr>
</tbody>
</table>


# 3. Computing a wellbore trajectory from directional survey data

The endpoint POST /convertTrajectory is the main function to calculate a
trajectory based on MDINCAZI directional survey observables. Two
standard calculation algorithms are implemented, LMP (Lee’s Modified
Proposal) and GNL with scale factor correction (called
"AzimuthalEquidistant" in the API).This endpoint have enhanced features 
like interpolate MD, MD_Incl only and inverse minimumcurvature implementations.

- These methods are described in [IOGP Guidance Note 373-07, Part 2](https://epsg.org).
  Both methods require minimum curvature calculated offsets as input.

- A calculation spreadsheet and description of the minimum curvature math are at
- [OSDU_wellbore_trajectory_calculations.docx](OSDU_wellbore_trajectory_calculations.docx), and 
- [OSDU_wellbore_trajectory_calculations.xlsx](OSDU_wellbore_trajectory_calculations.xlsx).


### Request Parameters (`ConvertTrajectoryRequest`)

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `trajectoryCRS` | string | **Yes** | Coordinate Reference System for the trajectory (e.g., `EPSG::32631` for UTM 31N) |
| `azimuthReference` | string | **Yes** | Reference for input azimuths: `"TN"` (True North) or `"GN"` (Grid North) |
| `unitZ` | string | **Yes** | Vertical unit (e.g., `"osdu:reference-data--UnitOfMeasure:m:"`) |
| `inputStations` | TrajectoryStationIn[] | **Yes** | Array of survey stations with MD, inclination, azimuth |
| `method` | string | **Yes** | Computation method: `"AzimuthalEquidistant"` (default), `"LeesModifiedProposal"` / `"LMP"`, or `"GridNorthLocal"` / `"GNL"` |
| `referencePoint` | Point | No | Wellhead location in `trajectoryCRS` coordinates (x, y, z) |
| `unitXY` | string | No | Horizontal unit (defaults to CRS unit) |
| `inputKind` | string | No | Input format: `"MD_Incl_Azim"`, `"MD_X_Y_Z"`, `"MD_dX_dY_dZ"`, `"X_Y_Z"`, `"dX_dY_dZ"` |
| `interpolate` | boolean | No | If `true`, auto-interpolate between stations (see [Interpolation Options](#34-wellbore-interpolation-on-md)) |
| `MD_i` | MinimumDepthInterval | No | Specific measured depths for interpolation (see [Interpolation Options](#34-wellbore-interpolation-on-md)) |

### Unit Parameters Reference

The `convertTrajectory` endpoint uses several unit parameters to control input interpretation and output formatting:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `unitZ` | string | **Yes** | Unit of measure for **vertical/depth values** (MD, TVD, z-coordinates). Pass as OSDU record ID, e.g., `"osdu:reference-data--UnitOfMeasure:m:"` for meters or `"osdu:reference-data--UnitOfMeasure:ft:"` for feet. |
| `unitMD` | string | No | Unit of measure for **Measured Depth**. If not provided, defaults to `unitZ`. Only specify if MD uses different units than depth. |
| `unitXY` | string | No | Unit of measure for **horizontal coordinates** (x, y, dx, dy). For projected CRS (e.g., UTM), this is automatically derived from the CRS definition. **Required only for inverse minimum curvature** when input contains dx/dy/dz values. |

#### Output Units in Response

The response echoes the resolved unit definitions:

| Response Field | Description |
|----------------|-------------|
| `unitZ` | Full persistableReference JSON for the vertical unit |
| `unitXY` | Full persistableReference JSON for the horizontal unit (derived from CRS or input) |
| `unitDls` | Dog Leg Severity unit, always `"deg/30m"` (degrees per 30 meters) |

#### Common Unit Record IDs

| Unit | OSDU Record ID |
|------|----------------|
| Meters | `osdu:reference-data--UnitOfMeasure:m:` |
| Feet | `osdu:reference-data--UnitOfMeasure:ft:` |
| US Survey Feet | `osdu:reference-data--UnitOfMeasure:ftUS:` |

> **Note:** The service logs unit resolution in `operationsApplied`, e.g., `"UnitMD set to be equal to unitZ m:"`.


## 3.1 Basic example

A very simplified example is given below with 4 survey stations to
introduce the request and response.

- The input in this example uses record id for CRS and UOM (these records need to exist, the API retrieves the persistableReference).
- The MD unit is given by unitMD.
- The input unitXY for the Projected trajectoryCrs we no need to pass the unitXY. It will be set from the code except for the inverse minimum curvature..
- The output “Z” coordinates are always heights and not depths (i.e.,
  they are positive station.points.z values when above the “permanent”
  geodetic vertical datum surface).
- The MD_I is used to calculate the interpolate  .

<details>
<summary><strong>Request</strong> (click to expand)</summary>

_{{osduonaws_base_url}}/api/crs/converter/v4/convertTrajectory_

```json
{
    "azimuthReference": "GN",
    "interpolate": false,
    "referencePoint": {
        "y": 6500000,
        "x": 400000,
        "z": 0
    },
    "unitZ": "osdu:reference-data--UnitOfMeasure:m:",
    "inputStations": [
        {
            "md": 0,
            "azimuth": 20,
            "inclination": 0
        },
        {
            "md": 100,
            "azimuth": 40,
            "inclination": 10
        },
            {
            "md": 150,
            "azimuth": 60,
            "inclination": 20
        },
        {
            "md": 200,
            "azimuth": 80,
            "inclination": 40
        }
    ],
    "trajectoryCRS": "osdu:reference-data--CoordinateReferenceSystem:Projected:EPSG::32631:",
    "inputKind": "MD_Incl_Azim",
    "MD_i": {
        "md_i": [
            50,
            125,
            175
        ]
    },
    "method": "AzimuthalEquidistant"
}
```


</details>

<details>
<summary><strong>Response</strong> (click to expand)</summary>

```json
{
    "trajectoryCRS": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32631\"},\"name\":\"WGS_1984_UTM_Zone_31N\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"WGS_1984_UTM_Zone_31N\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",3.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",32631]]\"}",
    "unitXY": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "unitZ": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "unitDls": "{\"scaleOffset\":{\"scale\":5.81776417331443E-4,\"offset\":0.0},\"symbol\":\"deg/30m\",\"baseMeasurement\":{\"ancestry\":\"Rotation_Per_Length\",\"type\":\"UM\"},\"type\":\"USO\"}",
    "stations": [
        {
            "md": 0.0,
            "inclination": 0.0,
            "azimuthTN": 18.529449533844684,
            "azimuthGN": 20.0,
            "dxTN": 0.0,
            "dyTN": 0.0,
            "point": {
                "x": 399999.99999999936,
                "y": 6499999.999999927,
                "z": 0.0
            },
            "wgs84Longitude": 1.2778067531835464,
            "wgs84Latitude": 58.62877104865894,
            "dls": 0.0,
            "original": true,
            "dz": 0.0
        },
        {
            "md": 100.0,
            "inclination": 10.0,
            "azimuthTN": 38.529449533844684,
            "azimuthGN": 40.0,
            "dxTN": 5.422189533665302,
            "dyTN": 6.80943942815664,
            "point": {
                "x": 400005.59360307665,
                "y": 6500006.666196379,
                "z": -99.4930770045299
            },
            "wgs84Longitude": 1.2779000901442394,
            "wgs84Latitude": 58.62883218086717,
            "dls": 3.000000000000004,
            "original": true,
            "dz": 99.4930770045299
        },
        {
            "md": 150.0,
            "inclination": 20.0,
            "azimuthTN": 58.529449533844684,
            "azimuthGN": 59.99999999999999,
            "dxTN": 15.450694491703878,
            "dyTN": 14.694154320922348,
            "point": {
                "x": 400015.8183142198,
                "y": 6500014.288835904,
                "z": -147.7571745971745
            },
            "wgs84Longitude": 1.2780727202384188,
            "wgs84Latitude": 58.628902966231905,
            "dls": 6.671888495594808,
            "original": true,
            "dz": 147.7571745971745
        },
        {
            "md": 200.0,
            "inclination": 40.0,
            "azimuthTN": 78.52944953384468,
            "azimuthGN": 80.0,
            "dxTN": 38.78259483297417,
            "dyTN": 22.450245403482143,
            "point": {
                "x": 400039.3350486971,
                "y": 6500021.441616548,
                "z": -190.93799689959891
            },
            "wgs84Longitude": 1.278474355086862,
            "wgs84Latitude": 58.62897259565018,
            "dls": 13.26861992489263,
            "original": true,
            "dz": 190.93799689959891
        }
    ],
    "localCRS": "{\"name\":\"Azimuthal Equidistant\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"Azimuthal Equidistant Lng=1.27780675;Lat=58.62877105\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Modified Azimuthal_Equidistant\\\"],PARAMETER[\\\"False_Easting\\\",0.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",1.2778067531835273],PARAMETER[\\\"Latitude_Of_Origin\\\",58.62877104865958],UNIT[\\\"Meter\\\",1.0]]\"}",
    "method": "AzimuthalEquidistant",
    "operationsApplied": [
        "derived TN from GN azimuth by grid convergence -1.470550",
        "UnitMD set to be equal to unitZ m:",
        "computed deflections via minimum curvature method",
        "computation method: AzimuthalEquidistant",
        "conversion from 'Azimuthal Equidistant' to 'GCS_WGS_1984'",
        "conversion from 'GCS_WGS_1984' to 'WGS_1984_UTM_Zone_31N'",
        "to WGS 84: conversion from WGS_1984_UTM_Zone_31N to GCS_WGS_1984; 3 points converted",
        "Interpolation for MD_i input stations;3 points interpolated",
        "to WGS 84: conversion from WGS_1984_UTM_Zone_31N to GCS_WGS_1984; 4 points converted"
    ],
    "stations_i": [
        {
            "md": 50.0,
            "inclination": 4.999999999999992,
            "azimuthTN": 38.52944953384469,
            "azimuthGN": 40.00000000000001,
            "dxTN": 1.358131433484072,
            "dyTN": 1.705605028073185,
            "point": {
                "x": 400001.40145529207,
                "y": 6500001.670189307,
                "z": -49.936562197687465
            },
            "wgs84Longitude": 1.2778301383620794,
            "wgs84Latitude": 58.628786365100225,
            "dls": 5.0,
            "original": false,
            "dz": 49.936562197687465
        },
        {
            "md": 125.0,
            "inclination": 14.805570564960638,
            "azimuthTN": 51.824496263261445,
            "azimuthGN": 53.29504672941676,
            "dxTN": 9.28839375669778,
            "dyTN": 10.484635825636033,
            "point": {
                "x": 400009.55285099795,
                "y": 6500010.240963441,
                "z": -123.90731684984351
            },
            "wgs84Longitude": 1.2779666611674094,
            "wgs84Latitude": 58.62886518430379,
            "dls": 5.559907079662333,
            "original": false,
            "dz": 123.90731684984351
        },
        {
            "md": 175.0,
            "inclination": 29.657310048104343,
            "azimuthTN": 71.61193836787965,
            "azimuthGN": 73.08248883403496,
            "dxTN": 24.996075985416454,
            "dyTN": 18.89023535878774,
            "point": {
                "x": 400025.46823649155,
                "y": 6500018.23857059,
                "z": -170.43626776626326
            },
            "wgs84Longitude": 1.2782370797587084,
            "wgs84Latitude": 58.628940646870596,
            "dls": 11.057183270743854,
            "original": false,
            "dz": 170.43626776626326
        }
    ],
    "scaleConvergenceList": [
        {
            "scalefactor": 0.999723,
            "convergence": -1.47055,
            "point": {
                "x": 399999.99999999936,
                "y": 6499999.999999927,
                "z": 0.0
            }
        },
        {
            "scalefactor": 0.999722,
            "convergence": -1.46998,
            "point": {
                "x": 400039.3350486971,
                "y": 6500021.441616548,
                "z": -190.93799689959891
            }
        }
    ],
    "inputKind": "MD_Incl_Azim"
}
```

</details>

## 3.2 Return point scale factor and grid convergence

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

<details>
<summary><strong>Request</strong> (click to expand)</summary>

```json
{
    "azimuthReference": "GN",
    "interpolate": false,
    "referencePoint": {
        "y": 6500000,
        "x": 400000,
        "z": 0
    },
    "unitZ": "osdu:reference-data--UnitOfMeasure:m:",
    "inputStations": [
        {
            "md": 0,
            "azimuth": 90,
            "inclination": 90
        },
        {
            "md": 2000,
            "azimuth": 90,
            "inclination": 90
        },
        {
            "md": 4000,
            "azimuth": 90,
            "inclination": 90
        },
        {
            "md": 6000,
            "azimuth": 90,
            "inclination": 90
        },
        {
            "md": 8000,
            "azimuth": 90,
            "inclination": 90
        },
        {
            "md": 10000,
            "azimuth": 90,
            "inclination": 90
        }
    ],
    "trajectoryCRS": "osdu:reference-data--CoordinateReferenceSystem:Projected:EPSG::32631:",
    "inputKind": "MD_Incl_Azim",
    "method": "AzimuthalEquidistant"
}
}
```

</details>

<details>
<summary><strong>Response</strong> (click to expand)</summary>

```
{
    "trajectoryCRS": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32631\"},\"name\":\"WGS_1984_UTM_Zone_31N\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"WGS_1984_UTM_Zone_31N\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",3.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",32631]]\"}",
    "unitXY": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "unitZ": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "unitDls": "{\"scaleOffset\":{\"scale\":5.81776417331443E-4,\"offset\":0.0},\"symbol\":\"deg/30m\",\"baseMeasurement\":{\"ancestry\":\"Rotation_Per_Length\",\"type\":\"UM\"},\"type\":\"USO\"}",
    "stations": [
        {
            "md": 0.0,
            "inclination": 90.0,
            "azimuthTN": 88.52944953384468,
            "azimuthGN": 90.0,
            "dxTN": 0.0,
            "dyTN": 0.0,
            "point": {
                "x": 399999.99999999936,
                "y": 6499999.999999927,
                "z": 0.0
            },
            "wgs84Longitude": 1.2778067531835464,
            "wgs84Latitude": 58.62877104865894,
            "dls": 0.0,
            "original": true,
            "dz": 0.0
        },
        {
            "md": 2000.0,
            "inclination": 90.0,
            "azimuthTN": 88.52944953384468,
            "azimuthGN": 90.0,
            "dxTN": 1999.3412953022676,
            "dyTN": 51.326259351830245,
            "point": {
                "x": 401999.4402974973,
                "y": 6500000.000000381,
                "z": -1.2246467991473532E-13
            },
            "wgs84Longitude": 1.312223579337627,
            "wgs84Latitude": 58.629227231049995,
            "dls": 0.0,
            "original": true,
            "dz": 1.2246467991473532E-13
        },
        {
            "md": 4000.0,
            "inclination": 90.0,
            "azimuthTN": 88.52944953384468,
            "azimuthGN": 90.0,
            "dxTN": 3998.682590604535,
            "dyTN": 102.65251870366049,
            "point": {
                "x": 403998.87098692835,
                "y": 6500000.000001728,
                "z": -2.4492935982947065E-13
            },
            "wgs84Longitude": 1.346641293684646,
            "wgs84Latitude": 58.629674207432956,
            "dls": 0.0,
            "original": true,
            "dz": 2.4492935982947065E-13
        },
        {
            "md": 6000.0,
            "inclination": 90.0,
            "azimuthTN": 88.52944953384468,
            "azimuthGN": 90.0,
            "dxTN": 5998.023885906803,
            "dyTN": 153.97877805549075,
            "point": {
                "x": 405998.2922643785,
                "y": 6500000.000003942,
                "z": -3.6739403974420595E-13
            },
            "wgs84Longitude": 1.381059878154834,
            "wgs84Latitude": 58.63011197741336,
            "dls": 0.0,
            "original": true,
            "dz": 3.6739403974420595E-13
        },
        {
            "md": 8000.0,
            "inclination": 90.0,
            "azimuthTN": 88.52944953384468,
            "azimuthGN": 90.0,
            "dxTN": 7997.36518120907,
            "dyTN": 205.30503740732098,
            "point": {
                "x": 407997.7043259288,
                "y": 6500000.000006994,
                "z": -4.898587196589413E-13
            },
            "wgs84Longitude": 1.4154793146753326,
            "wgs84Latitude": 58.63054054060476,
            "dls": 0.0,
            "original": true,
            "dz": 4.898587196589413E-13
        },
        {
            "md": 10000.0,
            "inclination": 90.0,
            "azimuthTN": 88.52944953384468,
            "azimuthGN": 90.0,
            "dxTN": 9996.706476511337,
            "dyTN": 256.6312967591512,
            "point": {
                "x": 409997.107367656,
                "y": 6500000.000010856,
                "z": -6.123233995736766E-13
            },
            "wgs84Longitude": 1.4498995851702636,
            "wgs84Latitude": 58.63095989662879,
            "dls": 0.0,
            "original": true,
            "dz": 6.123233995736766E-13
        }
    ],
    "localCRS": "{\"name\":\"Azimuthal Equidistant\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"Azimuthal Equidistant Lng=1.27780675;Lat=58.62877105\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Modified Azimuthal_Equidistant\\\"],PARAMETER[\\\"False_Easting\\\",0.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",1.2778067531835273],PARAMETER[\\\"Latitude_Of_Origin\\\",58.62877104865958],UNIT[\\\"Meter\\\",1.0]]\"}",
    "method": "AzimuthalEquidistant",
    "operationsApplied": [
        "derived TN from GN azimuth by grid convergence -1.470550",
        "UnitMD set to be equal to unitZ m:",
        "computed deflections via minimum curvature method",
        "computation method: AzimuthalEquidistant",
        "conversion from 'Azimuthal Equidistant' to 'GCS_WGS_1984'",
        "conversion from 'GCS_WGS_1984' to 'WGS_1984_UTM_Zone_31N'",
        "to WGS 84: conversion from WGS_1984_UTM_Zone_31N to GCS_WGS_1984; 6 points converted"
    ],
    "scaleConvergenceList": [
        {
            "scalefactor": 0.999723,
            "convergence": -1.47055,
            "point": {
                "x": 399999.99999999936,
                "y": 6499999.999999927,
                "z": 0.0
            }
        },
        {
            "scalefactor": 0.999699,
            "convergence": -1.32361,
            "point": {
                "x": 409997.107367656,
                "y": 6500000.000010856,
                "z": -6.123233995736766E-13
            }
        }
    ],
    "inputKind": "MD_Incl_Azim"
}
```

</details>

## 3.3 GNL Method
The "GNL" method requires the input survey observables to be grid north referenced, i.e.,
-method: "GNL"
-inputKind: "MD_Incl_Azim"
-azimuthReference: "GridNorth"

The implementation of GNL is to internally call "AzimuthalEquidistant", to compute scale factor, and "unscale" the results:
### 3.3.1 “Unscaling” the calculated wellbore path

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

<details>
<summary><strong>Request</strong> (click to expand)</summary>
```
{
    "azimuthReference": "GN",
    "interpolate": false,
    "referencePoint": {
        "y": 6500000,
        "x": 400000,
        "z": 0
    },
    "unitZ": "osdu:reference-data--UnitOfMeasure:m:",
    "inputStations": [
        {
            "md": 0,
            "azimuth": 90,
            "inclination": 90
        },
        {
            "md": 2000,
            "azimuth": 90,
            "inclination": 90
        },
        {
            "md": 4000,
            "azimuth": 90,
            "inclination": 90
        },
        {
            "md": 6000,
            "azimuth": 90,
            "inclination": 90
        },
        {
            "md": 8000,
            "azimuth": 90,
            "inclination": 90
        },
        {
            "md": 10000,
            "azimuth": 90,
            "inclination": 90
        }
    ],
    "trajectoryCRS": "osdu:reference-data--CoordinateReferenceSystem:Projected:EPSG::32631:",
    "inputKind": "MD_Incl_Azim",
    "method": "GNL"
}
```

</details>

<details>
<summary><strong>Response</strong> (click to expand)</summary>

```
{
    "trajectoryCRS": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32631\"},\"name\":\"WGS_1984_UTM_Zone_31N\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"WGS_1984_UTM_Zone_31N\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",3.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",32631]]\"}",
    "unitXY": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "unitZ": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "unitDls": "{\"scaleOffset\":{\"scale\":5.81776417331443E-4,\"offset\":0.0},\"symbol\":\"deg/30m\",\"baseMeasurement\":{\"ancestry\":\"Rotation_Per_Length\",\"type\":\"UM\"},\"type\":\"USO\"}",
    "stations": [
        {
            "md": 0.0,
            "inclination": 90.0,
            "azimuthTN": 88.52944953384468,
            "azimuthGN": 90.0,
            "dxTN": 0.0,
            "dyTN": 0.0,
            "point": {
                "x": 399999.99999999936,
                "y": 6499999.999999927,
                "z": 0.0
            },
            "wgs84Longitude": 1.2778067531835464,
            "wgs84Latitude": 58.62877104865894,
            "dls": 0.0,
            "original": true,
            "dz": 0.0
        },
        {
            "md": 2000.0,
            "inclination": 90.0,
            "azimuthTN": 88.52944953384468,
            "azimuthGN": 90.0,
            "dxTN": 1999.3412953022676,
            "dyTN": 51.326259351830245,
            "point": {
                "x": 401999.9942959173,
                "y": 6500000.000000381,
                "z": -1.2246467991473532E-13
            },
            "wgs84Longitude": 1.3122331155869649,
            "wgs84Latitude": 58.62922735617207,
            "dls": 0.0,
            "original": true,
            "dz": 1.2246467991473532E-13
        },
        {
            "md": 4000.0,
            "inclination": 90.0,
            "azimuthTN": 88.52944953384468,
            "azimuthGN": 90.0,
            "dxTN": 3998.682590604535,
            "dyTN": 102.65251870366049,
            "point": {
                "x": 403999.97898110613,
                "y": 6500000.000001729,
                "z": -2.4492935982947065E-13
            },
            "wgs84Longitude": 1.346660366715479,
            "wgs84Latitude": 58.6296744525754,
            "dls": 0.0,
            "original": true,
            "dz": 2.4492935982947065E-13
        },
        {
            "md": 6000.0,
            "inclination": 90.0,
            "azimuthTN": 88.52944953384468,
            "azimuthGN": 90.0,
            "dxTN": 5998.023885906803,
            "dyTN": 153.97877805549075,
            "point": {
                "x": 405999.95425170625,
                "y": 6500000.000003943,
                "z": -3.6739403974420595E-13
            },
            "wgs84Longitude": 1.3810884884824297,
            "wgs84Latitude": 58.63011233747395,
            "dls": 0.0,
            "original": true,
            "dz": 3.6739403974420595E-13
        },
        {
            "md": 8000.0,
            "inclination": 90.0,
            "azimuthTN": 88.52944953384468,
            "azimuthGN": 90.0,
            "dxTN": 7997.36518120907,
            "dyTN": 205.30503740732098,
            "point": {
                "x": 407999.920303853,
                "y": 6500000.000006996,
                "z": -4.898587196589413E-13
            },
            "wgs84Longitude": 1.4155174627980622,
            "wgs84Latitude": 58.63054101048091,
            "dls": 0.0,
            "original": true,
            "dz": 4.898587196589413E-13
        },
        {
            "md": 10000.0,
            "inclination": 90.0,
            "azimuthTN": 88.52944953384468,
            "azimuthGN": 90.0,
            "dxTN": 9996.706476511337,
            "dyTN": 256.6312967591512,
            "point": {
                "x": 409999.87733367743,
                "y": 6500000.000010859,
                "z": -6.123233995736766E-13
            },
            "wgs84Longitude": 1.4499472715695958,
            "wgs84Latitude": 58.63096047121759,
            "dls": 0.0,
            "original": true,
            "dz": 6.123233995736766E-13
        }
    ],
    "localCRS": "{\"name\":\"Azimuthal Equidistant\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"Azimuthal Equidistant Lng=1.27780675;Lat=58.62877105\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Modified Azimuthal_Equidistant\\\"],PARAMETER[\\\"False_Easting\\\",0.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",1.2778067531835273],PARAMETER[\\\"Latitude_Of_Origin\\\",58.62877104865958],UNIT[\\\"Meter\\\",1.0]]\"}",
    "method": "GNL",
    "operationsApplied": [
        "derived TN from GN azimuth by grid convergence -1.470550",
        "UnitMD set to be equal to unitZ m:",
        "computed deflections via minimum curvature method",
        "computation method: GridNorthLocal",
        "conversion from 'Azimuthal Equidistant' to 'GCS_WGS_1984'",
        "conversion from 'GCS_WGS_1984' to 'WGS_1984_UTM_Zone_31N'",
        "to WGS 84: conversion from WGS_1984_UTM_Zone_31N to GCS_WGS_1984; 6 points converted"
    ],
    "scaleConvergenceList": [
        {
            "scalefactor": 0.999723,
            "convergence": -1.47055,
            "point": {
                "x": 399999.99999999936,
                "y": 6499999.999999927,
                "z": 0.0
            }
        },
        {
            "scalefactor": 0.999699,
            "convergence": -1.32357,
            "point": {
                "x": 409999.87733367743,
                "y": 6500000.000010859,
                "z": -6.123233995736766E-13
            }
        }
    ],
    "inputKind": "MD_Incl_Azim"
}
```

</details>

## 3.4 Wellbore interpolation on MD
Interpolation refers to the computation of local coordinates at some given Measured Depth in between two survey stations, on the arc computed by the minimum curvature algorithm.
We can Pass the interpolate points in the request as a List of MD or interpolation_interval.

### 3.4.1 Interpolation at a list of MD
- The input in this example uses record id for CRS and UOM (these records need to exist, the API retrieves the persistableReference).
- The MD unit is given by `unitZ` (or `unitMD` if different).
- The input unitXY for the Projected trajectoryCrs we no need to pass the unitXY. It will be set from the code except for the inverse minimum curvature..
- The output “Z” coordinates are always heights and not depths (i.e.,
  they are positive station.points.z values when above the “permanent”
  geodetic vertical datum surface).
- The MD_I is used to calculate the interpolate. We passed as a list of array from the input
- Output calculated (incl. interpolated) stations in an array stations_i.
<details>
<summary><strong>Request</strong> (click to expand)</summary>
```
{
    "azimuthReference": "GN",
    "interpolate": false,
    "referencePoint": {
        "y": 6500000,
        "x": 400000,
        "z": 0
    },
    "unitZ": "osdu:reference-data--UnitOfMeasure:m:",
    "inputStations": [
        {
            "md": 0,
            "azimuth": 20,
            "inclination": 0
        },
        {
            "md": 100,
            "azimuth": 40,
            "inclination": 10
        },
            {
            "md": 150,
            "azimuth": 60,
            "inclination": 20
        },
        {
            "md": 200,
            "azimuth": 80,
            "inclination": 40
        }
    ],
    "trajectoryCRS": "osdu:reference-data--CoordinateReferenceSystem:Projected:EPSG::32631:",
    "inputKind": "MD_Incl_Azim",
    "MD_i": {
        "md_i": [
            50,
            125,
            175
        ]
    },
    "method": "AzimuthalEquidistant"
}
```

</details>

<details>
<summary><strong>Response</strong> (click to expand)</summary>

```
{
    "trajectoryCRS": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32631\"},\"name\":\"WGS_1984_UTM_Zone_31N\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"WGS_1984_UTM_Zone_31N\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",3.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",32631]]\"}",
    "unitXY": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "unitZ": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "unitDls": "{\"scaleOffset\":{\"scale\":5.81776417331443E-4,\"offset\":0.0},\"symbol\":\"deg/30m\",\"baseMeasurement\":{\"ancestry\":\"Rotation_Per_Length\",\"type\":\"UM\"},\"type\":\"USO\"}",
    "stations": [
        {
            "md": 0.0,
            "inclination": 0.0,
            "azimuthTN": 18.529449533844684,
            "azimuthGN": 20.0,
            "dxTN": 0.0,
            "dyTN": 0.0,
            "point": {
                "x": 399999.99999999936,
                "y": 6499999.999999927,
                "z": 0.0
            },
            "wgs84Longitude": 1.2778067531835464,
            "wgs84Latitude": 58.62877104865894,
            "dls": 0.0,
            "original": true,
            "dz": 0.0
        },
        {
            "md": 100.0,
            "inclination": 10.0,
            "azimuthTN": 38.529449533844684,
            "azimuthGN": 40.0,
            "dxTN": 5.422189533665302,
            "dyTN": 6.80943942815664,
            "point": {
                "x": 400005.59360307665,
                "y": 6500006.666196379,
                "z": -99.4930770045299
            },
            "wgs84Longitude": 1.2779000901442394,
            "wgs84Latitude": 58.62883218086717,
            "dls": 3.000000000000004,
            "original": true,
            "dz": 99.4930770045299
        },
        {
            "md": 150.0,
            "inclination": 20.0,
            "azimuthTN": 58.529449533844684,
            "azimuthGN": 59.99999999999999,
            "dxTN": 15.450694491703878,
            "dyTN": 14.694154320922348,
            "point": {
                "x": 400015.8183142198,
                "y": 6500014.288835904,
                "z": -147.7571745971745
            },
            "wgs84Longitude": 1.2780727202384188,
            "wgs84Latitude": 58.628902966231905,
            "dls": 6.671888495594808,
            "original": true,
            "dz": 147.7571745971745
        },
        {
            "md": 200.0,
            "inclination": 40.0,
            "azimuthTN": 78.52944953384468,
            "azimuthGN": 80.0,
            "dxTN": 38.78259483297417,
            "dyTN": 22.450245403482143,
            "point": {
                "x": 400039.3350486971,
                "y": 6500021.441616548,
                "z": -190.93799689959891
            },
            "wgs84Longitude": 1.278474355086862,
            "wgs84Latitude": 58.62897259565018,
            "dls": 13.26861992489263,
            "original": true,
            "dz": 190.93799689959891
        }
    ],
    "localCRS": "{\"name\":\"Azimuthal Equidistant\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"Azimuthal Equidistant Lng=1.27780675;Lat=58.62877105\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Modified Azimuthal_Equidistant\\\"],PARAMETER[\\\"False_Easting\\\",0.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",1.2778067531835273],PARAMETER[\\\"Latitude_Of_Origin\\\",58.62877104865958],UNIT[\\\"Meter\\\",1.0]]\"}",
    "method": "AzimuthalEquidistant",
    "operationsApplied": [
        "derived TN from GN azimuth by grid convergence -1.470550",
        "UnitMD set to be equal to unitZ m:",
        "computed deflections via minimum curvature method",
        "computation method: AzimuthalEquidistant",
        "conversion from 'Azimuthal Equidistant' to 'GCS_WGS_1984'",
        "conversion from 'GCS_WGS_1984' to 'WGS_1984_UTM_Zone_31N'",
        "to WGS 84: conversion from WGS_1984_UTM_Zone_31N to GCS_WGS_1984; 3 points converted",
        "Interpolation for MD_i input stations;3 points interpolated",
        "to WGS 84: conversion from WGS_1984_UTM_Zone_31N to GCS_WGS_1984; 4 points converted"
    ],
    "stations_i": [
        {
            "md": 50.0,
            "inclination": 4.999999999999992,
            "azimuthTN": 38.52944953384469,
            "azimuthGN": 40.00000000000001,
            "dxTN": 1.358131433484072,
            "dyTN": 1.705605028073185,
            "point": {
                "x": 400001.40145529207,
                "y": 6500001.670189307,
                "z": -49.936562197687465
            },
            "wgs84Longitude": 1.2778301383620794,
            "wgs84Latitude": 58.628786365100225,
            "dls": 5.0,
            "original": false,
            "dz": 49.936562197687465
        },
        {
            "md": 125.0,
            "inclination": 14.805570564960638,
            "azimuthTN": 51.824496263261445,
            "azimuthGN": 53.29504672941676,
            "dxTN": 9.28839375669778,
            "dyTN": 10.484635825636033,
            "point": {
                "x": 400009.55285099795,
                "y": 6500010.240963441,
                "z": -123.90731684984351
            },
            "wgs84Longitude": 1.2779666611674094,
            "wgs84Latitude": 58.62886518430379,
            "dls": 5.559907079662333,
            "original": false,
            "dz": 123.90731684984351
        },
        {
            "md": 175.0,
            "inclination": 29.657310048104343,
            "azimuthTN": 71.61193836787965,
            "azimuthGN": 73.08248883403496,
            "dxTN": 24.996075985416454,
            "dyTN": 18.89023535878774,
            "point": {
                "x": 400025.46823649155,
                "y": 6500018.23857059,
                "z": -170.43626776626326
            },
            "wgs84Longitude": 1.2782370797587084,
            "wgs84Latitude": 58.628940646870596,
            "dls": 11.057183270743854,
            "original": false,
            "dz": 170.43626776626326
        }
    ],
    "scaleConvergenceList": [
        {
            "scalefactor": 0.999723,
            "convergence": -1.47055,
            "point": {
                "x": 399999.99999999936,
                "y": 6499999.999999927,
                "z": 0.0
            }
        },
        {
            "scalefactor": 0.999722,
            "convergence": -1.46998,
            "point": {
                "x": 400039.3350486971,
                "y": 6500021.441616548,
                "z": -190.93799689959891
            }
        }
    ],
    "inputKind": "MD_Incl_Azim"
}
```
</details>

### 3.4.2 Interpolation at a regular MD interval

- The input in this example uses record id for CRS and UOM (these records need to exist, the API retrieves the persistableReference).
- The MD unit is given by unitZ.
- The input unitXY for the Projected trajectoryCrs we no need to pass the unitXY. It will be set from the code except for the inverse minimum curvature..
- The output “Z” coordinates are always heights and not depths (i.e.,
  they are positive station.points.z values when above the “permanent”
  geodetic vertical datum surface).
- The MD_I is used to calculate the interpolate. We passed as interval i.e.interpolation_interval = Number
- Output calculated (incl. interpolated) stations in an array stations_i.

<details>
<summary><strong>Request</strong> (click to expand)</summary>
```
{
    "azimuthReference": "TN",
    "interpolate": false,
    "referencePoint": {
        "y": 6500000,
        "x": 400000,
        "z": 0
    },
    "unitZ": "osdu:reference-data--UnitOfMeasure:m:",
    "inputStations": [
        {
            "md": 0,
            "azimuth": 20,
            "inclination": 0
        },
        {
            "md": 100,
            "azimuth": 40,
            "inclination": 10
        },
        {
            "md": 150,
            "azimuth": 60,
            "inclination": 20
        },
        {
            "md": 200,
            "azimuth": 80,
            "inclination": 40
        }
    ],
    "trajectoryCRS": "osdu:reference-data--CoordinateReferenceSystem:Projected:EPSG::32631:",
    "inputKind": "MD_Incl_Azim",
    "MD_i": {
        "md_interval": 60
    },
    "method": "AzimuthalEquidistant"
}
```

</details>

<details>
<summary><strong>Response</strong> (click to expand)</summary>

```
{
    "trajectoryCRS": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32631\"},\"name\":\"WGS_1984_UTM_Zone_31N\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"WGS_1984_UTM_Zone_31N\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",3.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",32631]]\"}",
    "unitXY": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "unitZ": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "unitDls": "{\"scaleOffset\":{\"scale\":5.81776417331443E-4,\"offset\":0.0},\"symbol\":\"deg/30m\",\"baseMeasurement\":{\"ancestry\":\"Rotation_Per_Length\",\"type\":\"UM\"},\"type\":\"USO\"}",
    "stations": [
        {
            "md": 0.0,
            "inclination": 0.0,
            "azimuthTN": 20.0,
            "azimuthGN": 21.470550466155316,
            "dxTN": 0.0,
            "dyTN": 0.0,
            "point": {
                "x": 399999.99999999936,
                "y": 6499999.999999927,
                "z": 0.0
            },
            "wgs84Longitude": 1.2778067531835464,
            "wgs84Latitude": 58.62877104865894,
            "dls": 0.0,
            "original": true,
            "dz": 0.0
        },
        {
            "md": 100.0,
            "inclination": 10.0,
            "azimuthTN": 40.0,
            "azimuthGN": 41.470550466155316,
            "dxTN": 5.595155249871464,
            "dyTN": 6.668046370156673,
            "point": {
                "x": 400005.7628362719,
                "y": 6500006.520451491,
                "z": -99.4930770045299
            },
            "wgs84Longitude": 1.277903067553126,
            "wgs84Latitude": 58.62883091149845,
            "dls": 3.000000000000004,
            "original": true,
            "dz": 99.4930770045299
        },
        {
            "md": 150.0,
            "inclination": 20.0,
            "azimuthTN": 59.99999999999999,
            "azimuthGN": 61.470550466155316,
            "dxTN": 15.82270375699716,
            "dyTN": 14.292801590358605,
            "point": {
                "x": 400016.1798006573,
                "y": 6500013.8781823935,
                "z": -147.7571745971745
            },
            "wgs84Longitude": 1.2780791239489757,
            "wgs84Latitude": 58.62889936304495,
            "dls": 6.671888495594808,
            "original": true,
            "dz": 147.7571745971745
        },
        {
            "md": 200.0,
            "inclination": 40.0,
            "azimuthTN": 80.0,
            "azimuthGN": 81.47055046615532,
            "dxTN": 39.34596525331648,
            "dyTN": 21.447568602058794,
            "point": {
                "x": 400039.87235254253,
                "y": 6500020.425094282,
                "z": -190.93799689959891
            },
            "wgs84Longitude": 1.2784840527466057,
            "wgs84Latitude": 58.62896359399517,
            "dls": 13.26861992489263,
            "original": true,
            "dz": 190.93799689959891
        }
    ],
    "localCRS": "{\"name\":\"Azimuthal Equidistant\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"Azimuthal Equidistant Lng=1.27780675;Lat=58.62877105\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Modified Azimuthal_Equidistant\\\"],PARAMETER[\\\"False_Easting\\\",0.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",1.2778067531835273],PARAMETER[\\\"Latitude_Of_Origin\\\",58.62877104865958],UNIT[\\\"Meter\\\",1.0]]\"}",
    "method": "AzimuthalEquidistant",
    "operationsApplied": [
        "derived GN from TN azimuth by grid convergence 1.470550",
        "UnitMD set to be equal to unitZ m:",
        "computed deflections via minimum curvature method",
        "computation method: AzimuthalEquidistant",
        "conversion from 'Azimuthal Equidistant' to 'GCS_WGS_1984'",
        "conversion from 'GCS_WGS_1984' to 'WGS_1984_UTM_Zone_31N'",
        "to WGS 84: conversion from WGS_1984_UTM_Zone_31N to GCS_WGS_1984; 5 points converted",
        "Interpolation for MD_i input stations;5 points interpolated",
        "to WGS 84: conversion from WGS_1984_UTM_Zone_31N to GCS_WGS_1984; 4 points converted"
    ],
    "stations_i": [
        {
            "md": 0.0,
            "inclination": 0.0,
            "azimuthTN": 20.0,
            "azimuthGN": 21.470550466155313,
            "dxTN": 0.0,
            "dyTN": 0.0,
            "point": {
                "x": 399999.99999999936,
                "y": 6499999.999999927,
                "z": 0.0
            },
            "wgs84Longitude": 1.2778067531835464,
            "wgs84Latitude": 58.62877104865894,
            "dls": 0.0,
            "original": false,
            "dz": 0.0
        },
        {
            "md": 60.0,
            "inclination": 5.999999999999966,
            "azimuthTN": 40.0,
            "azimuthGN": 41.470550466155316,
            "dxTN": 2.0175320947704836,
            "dyTN": 2.4044011221168464,
            "point": {
                "x": 400002.07857207296,
                "y": 6500002.351832966,
                "z": -59.89039784224798
            },
            "wgs84Longitude": 1.2778414923520274,
            "wgs84Latitude": 58.628792640331795,
            "dls": 6.0,
            "original": false,
            "dz": 59.89039784224798
        },
        {
            "md": 120.0,
            "inclination": 13.799876365883316,
            "azimuthTN": 51.37338819928859,
            "azimuthGN": 52.84393866544391,
            "dxTN": 8.576321588981623,
            "dyTN": 9.488699477260308,
            "point": {
                "x": 400008.8154075433,
                "y": 6500009.26366955,
                "z": -119.06233141522378
            },
            "wgs84Longitude": 1.2779543993603775,
            "wgs84Latitude": 58.62885624109766,
            "dls": 4.447925663729867,
            "original": false,
            "dz": 119.06233141522378
        },
        {
            "md": 180.0,
            "inclination": 31.691997716443105,
            "azimuthTN": 74.78149752158167,
            "azimuthGN": 76.25204798773699,
            "dxTN": 27.92374993733538,
            "dyTN": 18.94735618292831,
            "point": {
                "x": 400028.39631176775,
                "y": 6500018.220653281,
                "z": -174.73651392904975
            },
            "wgs84Longitude": 1.2782874887826199,
            "wgs84Latitude": 58.62894116064186,
            "dls": 13.268619924892624,
            "original": false,
            "dz": 174.73651392904975
        },
        {
            "md": 200.0,
            "inclination": 40.00000000000001,
            "azimuthTN": 80.0,
            "azimuthGN": 81.47055046615532,
            "dxTN": 39.34596525331648,
            "dyTN": 21.447568602058794,
            "point": {
                "x": 400039.87892842584,
                "y": 6500020.426912456,
                "z": -190.93799689959891
            },
            "wgs84Longitude": 1.2784841651345953,
            "wgs84Latitude": 58.62896361183197,
            "dls": 22.11436654148771,
            "original": false,
            "dz": 190.93799689959891
        }
    ],
    "scaleConvergenceList": [
        {
            "scalefactor": 0.999723,
            "convergence": -1.47055,
            "point": {
                "x": 399999.99999999936,
                "y": 6499999.999999927,
                "z": 0.0
            }
        },
        {
            "scalefactor": 0.999722,
            "convergence": -1.46998,
            "point": {
                "x": 400039.87235254253,
                "y": 6500020.425094282,
                "z": -190.93799689959891
            }
        }
    ],
    "inputKind": "MD_Incl_Azim"
}
```
</details>

## 3.5 Handle inclination-only surveys (stations have no azimuth)
Inclination-only, undefined azimuth, survey data do not have recorded azimuth values. No information is available on the horizontal direction (azimuth, e.g., North or East) in which the wellbore is departing.This survey data are common in both legacy and modern data sets.
Some software applications cannot deal with missing azimuth and set it to zero during calculations. This results in a path that is going due north, which is obviously incorrect. The path may go North, but it could just as well go into any other direction or have a more or less random azimuth at each station. The horizontal error is given by this offset (and pretty much an indication of the maximum offset that the wellbore could have, assuming random inclination errors).

We calculate the max_horizontal_error and TVD_correction and returned as a part of the operationsApplied and create a final path to be output by forcing it to be perfectly vertical below the first station.

- The input in this example uses record id for CRS and UOM (these records need to exist, the API retrieves the persistableReference).
- The MD unit is given by unitMD.
-The inputKind is MD_Incl
- The input unitXY for the Projected trajectoryCrs we no need to pass the unitXY. It will be set from the code except for the inverse minimum curvature..
- The output “Z” coordinates are always heights and not depths (i.e.,
  they are positive station.points.z values when above the “permanent”
  geodetic vertical datum surface).
- In response operationsApplied have the max_horizontal_error and TVD_correction values.
<details>
<summary><strong>Request</strong> (click to expand)</summary>
```
{
    "interpolate": false,
    "referencePoint": {
        "x": 2000000,
        "y": 10000000,
        "z": 100
    },
    "unitZ": "osdu:reference-data--UnitOfMeasure:m:",
    "unitMD": "osdu:reference-data--UnitOfMeasure:m:",
    "inputStations": [
        {
            "md": 0,
            "inclination": 0.0
        },
        {
            "md": 1000,
            "inclination": 1.5
        },
        {
            "md": 2000,
            "inclination": 0.5
        },
        {
            "md": 3000,
            "inclination": 1.0
        }
    ],
    "trajectoryCRS": "osdu:reference-data--CoordinateReferenceSystem:Projected:EPSG::32631:",
    "inputKind": "MD_Incl",
    "method": "AzimuthalEquidistant"
}
```

</details>

<details>
<summary><strong>Response</strong> (click to expand)</summary>

```
{
    "unitMD": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "trajectoryCRS": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"32631\"},\"name\":\"WGS_1984_UTM_Zone_31N\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"WGS_1984_UTM_Zone_31N\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",3.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",32631]]\"}",
    "unitXY": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "unitZ": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "unitDls": "{\"scaleOffset\":{\"scale\":5.81776417331443E-4,\"offset\":0.0},\"symbol\":\"deg/30m\",\"baseMeasurement\":{\"ancestry\":\"Rotation_Per_Length\",\"type\":\"UM\"},\"type\":\"USO\"}",
    "stations": [
        {
            "md": 0.0,
            "inclination": 0.0,
            "azimuthTN": 0.0,
            "azimuthGN": 0.0,
            "dxTN": 0.0,
            "dyTN": 0.0,
            "point": {
                "x": 2000000.0000005513,
                "y": 9995929.88604158,
                "z": 100.0
            },
            "wgs84Longitude": 92.92297422737474,
            "wgs84Latitude": 76.68411439708218,
            "dls": 0.0,
            "original": false,
            "dz": 0.0
        },
        {
            "md": 999.885772382169,
            "inclination": 0.0,
            "azimuthTN": 1.5000000000000002,
            "azimuthGN": 1.5000000000000002,
            "dxTN": 0.0,
            "dyTN": 0.0,
            "point": {
                "x": 2000000.0000005513,
                "y": 9995929.88604158,
                "z": -899.885772382169
            },
            "wgs84Longitude": 92.92297422737474,
            "wgs84Latitude": 76.68411439708218,
            "dls": 0.0,
            "original": false,
            "dz": 999.885772382169
        },
        {
            "md": 1999.7207771275034,
            "inclination": 0.0,
            "azimuthTN": 0.5,
            "azimuthGN": 0.5,
            "dxTN": 0.0,
            "dyTN": 0.0,
            "point": {
                "x": 2000000.0000005513,
                "y": 9995929.88604158,
                "z": -1899.7207771275034
            },
            "wgs84Longitude": 92.92297422737474,
            "wgs84Latitude": 76.68411439708218,
            "dls": 0.0,
            "original": false,
            "dz": 1999.7207771275034
        },
        {
            "md": 2999.6319318782553,
            "inclination": 0.0,
            "azimuthTN": 1.0,
            "azimuthGN": 1.0,
            "dxTN": 0.0,
            "dyTN": 0.0,
            "point": {
                "x": 2000000.0000005513,
                "y": 9995929.88604158,
                "z": -2899.6319318782553
            },
            "wgs84Longitude": 92.92297422737474,
            "wgs84Latitude": 76.68411439708218,
            "dls": 0.0,
            "original": false,
            "dz": 2999.6319318782553
        }
    ],
    "localCRS": "{\"name\":\"Azimuthal Equidistant\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"Azimuthal Equidistant Lng=92.92297423;Lat=76.68411440\\\",GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Modified Azimuthal_Equidistant\\\"],PARAMETER[\\\"False_Easting\\\",0.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",92.92297422752142],PARAMETER[\\\"Latitude_Of_Origin\\\",76.68411439708704],UNIT[\\\"Meter\\\",1.0]]\"}",
    "method": "AzimuthalEquidistant",
    "operationsApplied": [
        "Original survey was inclination-only, change to Md_Incl_Az. The original inclination values were copied into the azimuth and then set to zero to force a vertical path.",
        "unitMD Factor value: 1.0 is used for computation of MD",
        "computed deflections via minimum curvature method",
        "computation method: AzimuthalEquidistant",
        "conversion from 'Azimuthal Equidistant' to 'GCS_WGS_1984'",
        "conversion from 'GCS_WGS_1984' to 'WGS_1984_UTM_Zone_31N'",
        "to WGS 84: conversion from WGS_1984_UTM_Zone_31N to GCS_WGS_1984; 4 points converted",
        "TVD_correction applied = 0.4 m",
        "max_horizontal_error = 43 m"
    ],
    "scaleConvergenceList": [
        {
            "scalefactor": 1.027206,
            "convergence": 89.92085,
            "point": {
                "x": 2000000.0000005513,
                "y": 9995929.88604158,
                "z": 100.0
            }
        },
        {
            "scalefactor": 1.027206,
            "convergence": 89.92085,
            "point": {
                "x": 2000000.0000005513,
                "y": 9995929.88604158,
                "z": -2899.6319318782553
            }
        }
    ],
    "inputKind": "MD_Incl_Azim"
}
```

</details>


## 3.6 inverse minimum curvature
The normal scenario is to convert directional survey observables to local coordinates and then to a wellpath in 3D geodetic space.  However, sometimes we may only have local coordinates and want to back-compute the (possible) M,I,A observables, e.g., for inertial surveys (for better or worse, and note the requirement to preserve the original inertial survey local coordinates at original frequency) or because only a path was loaded in a subsurface application.

- The `inputKind` is `dX_dY_dZ`.
- The `unitXY` parameter is **mandatory** for `dX_dY_dZ` input.
- For calculation of MD, Inclination, and Azimuth using inverse minimum curvature equations, dx/dy/dz values are normalized to SI (meters) internally.
- The MD values are denormalized to the requested unit on output.
We create a dummy request with InputKind "MD_Incl_Azim" and values for md,inc,azi inputStations which are calculated uisng inverse minimumcurvature.
unitXY : should be removed from the dummy if trajectoryCRS is projected.
unitMD : (is an optional parameter defaulting to unitZ) - MDs should be converted to unitZ unless unitMD is given by user in request, then convert MDs to that unit.
Finally on output unitXY is set to projCRS unit, and the stations.XYZ and the output dxTN and dyTN are in that output unit

<details>
<summary><strong>Request</strong> (click to expand)</summary>
```
{
    "trajectoryCRS": "osdu:reference-data--CoordinateReferenceSystem:BoundProjected:EPSG::23032_EPSG::1612:",
    "azimuthReference": "GN",
    "unitZ": "osdu:reference-data--UnitOfMeasure:m:",
    "unitMD": "osdu:reference-data--UnitOfMeasure:m:",
     "unitXY": "osdu:reference-data--UnitOfMeasure:m:",
    "referencePoint": {
        "x": 400000,
        "y": 6500000,
        "z": 100
    },
    "inputKind": "dX_dY_dZ",
    "inputStations": [
        {
            "dx": 0,
            "dy": 0,
            "dz": 0
        },
	    {
            "dx": -1.5115234,
            "dy": 8.5722752,
            "dz": 99.4930770
        }
    ],
    "method": "AzimuthalEquidistant",
    "interpolate": false
}
```

</details>

<details>
<summary><strong>Response</strong> (click to expand)</summary>

```
{
    "unitMD": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "trajectoryCRS": "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"23032023\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"23032\"},\"name\":\"ED_1950_UTM_Zone_32N\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"ED_1950_UTM_Zone_32N\\\",GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Transverse_Mercator\\\"],PARAMETER[\\\"False_Easting\\\",500000.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",9.0],PARAMETER[\\\"Scale_Factor\\\",0.9996],PARAMETER[\\\"Latitude_Of_Origin\\\",0.0],UNIT[\\\"Meter\\\",1.0],AUTHORITY[\\\"EPSG\\\",23032]]\"},\"name\":\"ED50 * EPSG-Nor N62 2001 / UTM zone 32N [23032,1612]\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1612\"},\"name\":\"ED_1950_To_WGS_1984_23\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"ED_1950_To_WGS_1984_23\\\",GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-116.641],PARAMETER[\\\"Y_Axis_Translation\\\",-56.931],PARAMETER[\\\"Z_Axis_Translation\\\",-110.559],PARAMETER[\\\"X_Axis_Rotation\\\",0.893],PARAMETER[\\\"Y_Axis_Rotation\\\",0.921],PARAMETER[\\\"Z_Axis_Rotation\\\",-0.917],PARAMETER[\\\"Scale_Difference\\\",-3.52],OPERATIONACCURACY[1.0],AUTHORITY[\\\"EPSG\\\",1612]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
    "unitXY": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "unitZ": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}",
    "unitDls": "{\"scaleOffset\":{\"scale\":5.81776417331443E-4,\"offset\":0.0},\"symbol\":\"deg/30m\",\"baseMeasurement\":{\"ancestry\":\"Rotation_Per_Length\",\"type\":\"UM\"},\"type\":\"USO\"}",
    "stations": [
        {
            "md": 0.0,
            "inclination": 0.0,
            "azimuthTN": 358.53,
            "azimuthGN": 0.0,
            "dxTN": 0.0,
            "dyTN": 0.0,
            "point": {
                "x": 399999.9999999993,
                "y": 6499999.999999926,
                "z": 100.0
            },
            "wgs84Longitude": 7.276532126322325,
            "wgs84Latitude": 58.62690776508143,
            "dls": 0.0,
            "original": false,
            "dz": 0.0
        },
        {
            "md": 100.0,
            "inclination": 10.0,
            "azimuthTN": 348.53,
            "azimuthGN": 350.0,
            "dxTN": -1.7309946188662695,
            "dyTN": 8.530666018707992,
            "point": {
                "x": 399998.4888960214,
                "y": 6500008.569896974,
                "z": 0.5069230000000005
            },
            "wgs84Longitude": 7.276502327935908,
            "wgs84Latitude": 58.62698434915697,
            "dls": 3.000000008626449,
            "original": false,
            "dz": 99.493077
        }
    ],
    "localCRS": "{\"lateBoundCRS\":{\"name\":\"Azimuthal Equidistant\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"PROJCS[\\\"Azimuthal Equidistant Lng=7.27795820;Lat=58.62743374\\\",GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],PROJECTION[\\\"Modified Azimuthal_Equidistant\\\"],PARAMETER[\\\"False_Easting\\\",0.0],PARAMETER[\\\"False_Northing\\\",0.0],PARAMETER[\\\"Central_Meridian\\\",7.277958201851517],PARAMETER[\\\"Latitude_Of_Origin\\\",58.6274337418261],UNIT[\\\"Meter\\\",1.0]]\"},\"name\":\"Azimuthal Equidistant - ED_1950_To_WGS_1984_23\",\"singleCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1612\"},\"name\":\"ED_1950_To_WGS_1984_23\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"ED_1950_To_WGS_1984_23\\\",GEOGCS[\\\"GCS_European_1950\\\",DATUM[\\\"D_European_1950\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Position_Vector\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-116.641],PARAMETER[\\\"Y_Axis_Translation\\\",-56.931],PARAMETER[\\\"Z_Axis_Translation\\\",-110.559],PARAMETER[\\\"X_Axis_Rotation\\\",0.893],PARAMETER[\\\"Y_Axis_Rotation\\\",0.921],PARAMETER[\\\"Z_Axis_Rotation\\\",-0.917],PARAMETER[\\\"Scale_Difference\\\",-3.52],OPERATIONACCURACY[1.0],AUTHORITY[\\\"EPSG\\\",1612]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
    "method": "AzimuthalEquidistant",
    "operationsApplied": [
        "Input dX_dY_dZ .  Applying inverse minimum curvature to compute Md_Incl_Azim",
        "derived TN from GN azimuth by grid convergence -1.470400",
        "unitMD Factor value: 1.0 is used for computation of MD",
        "computed deflections via minimum curvature method",
        "computation method: AzimuthalEquidistant",
        "conversion from 'Azimuthal Equidistant' to 'GCS_European_1950'",
        "conversion from 'GCS_European_1950' to 'ED_1950_UTM_Zone_32N'",
        "to WGS 84: conversion from ED_1950_UTM_Zone_32N to GCS_European_1950; 2 points converted",
        "to WGS 84: transformation GCS_European_1950 to GCS_WGS_1984 using ED_1950_To_WGS_1984_23; 2 points successfully transformed"
    ],
    "scaleConvergenceList": [
        {
            "scalefactor": 0.999723,
            "convergence": -1.4704,
            "point": {
                "x": 399999.9999999993,
                "y": 6499999.999999926,
                "z": 100.0
            }
        },
        {
            "scalefactor": 0.999723,
            "convergence": -1.47043,
            "point": {
                "x": 399998.4888960214,
                "y": 6500008.569896974,
                "z": 0.5069230000000005
            }
        }
    ],
    "inputKind": "MD_Incl_Azim"
}
```

</details>

# 4. Explicit Transform

- CRS Converter service will now support explicit transformations (overriding any bound transformations) for convert and convertGeoJson APIs.
- OSDU Geomatics wants to be able to support on-demand transformation-bindings (late-bindings) for projects and work maps using local, static datums.
- Both SIS and Esri support this in principle, however, the CRS Converter's API has been designed to support early-bindings or BoundCRSs.
- Specifying an explicit transformation will override any early binding transformations in the `fromCRS` and `toCRS`. The CRS Converter will validate that the explicit transformation is valid for the `fromCRS` and `toCRS`.
- The explicit transformation can be specified by setting the optional `"transformation"` parameter of a CRS conversion request.
- The `"transformation"` parameter as well as `"fromCRS"` & `"toCRS"` params will be able to accept both recordId and PR string formats in payload.

<details>
<summary><strong>Request for v4/convert</strong> (click to expand)</summary>

```
{
    "fromCRS": "osdu:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4283:",
    "toCRS": "osdu:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::7844:",
    "transformation": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"8048\"},\"name\":\"GDA94 to GDA2020 (1)\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"GDA94 to GDA2020 (1)\\\",GEOGCS[\\\"GCS_GDA_1994\\\",DATUM[\\\"D_GDA_1994\\\",SPHEROID[\\\"GRS_1980\\\",6378137,298.257222101,AUTHORITY[\\\"EPSG\\\",\\\"7019\\\"]],AUTHORITY[\\\"EPSG\\\",\\\"6283\\\"]],PRIMEM[\\\"Greenwich\\\",0,AUTHORITY[\\\"EPSG\\\",\\\"8901\\\"]],UNIT[\\\"degree\\\",0.0174532925199433,AUTHORITY[\\\"EPSG\\\",\\\"9102\\\"]],AXIS[\\\"Lat\\\",north],AXIS[\\\"Lon\\\",east],AUTHORITY[\\\"EPSG\\\",\\\"4283\\\"]],GEOGCS[\\\"GDA2020\\\",DATUM[\\\"GDA2020\\\",SPHEROID[\\\"GRS_1980\\\",6378137.0,298.257222101]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",7844]],METHOD[\\\"Coordinate_Frame\\\"],PARAMETER[\\\"X_Axis_Translation\\\",0.06155],PARAMETER[\\\"Y_Axis_Translation\\\",-0.01087],PARAMETER[\\\"Z_Axis_Translation\\\",-0.04019],PARAMETER[\\\"X_Axis_Rotation\\\",-0.0394924],PARAMETER[\\\"Y_Axis_Rotation\\\",-0.0327221],PARAMETER[\\\"Z_Axis_Rotation\\\",-0.0328979],PARAMETER[\\\"Scale_Difference\\\",-9.994],AUTHORITY[\\\"EPSG\\\",\\\"8048\\\"]]\"}",
    "points": [
        {
            "x": 120,
            "y": -20,
            "z": 0
        }
    ]
}
```

</details>

<details>
<summary><strong>Response for v4/convert</strong> (click to expand)</summary>

```
{
    "successCount": 1,
    "points": [
        {
            "x": 120.00000954373071,
            "y": -19.999986348895753,
            "z": 0.0
        }
    ],
    "operationsApplied": [
        "transformation GCS_GDA_1994 to GDA2020 using GDA94 to GDA2020 (1); 1 points successfully transformed"
    ]
}
```

</details>

<details>
<summary><strong>Request for v4/convertGeoJson</strong> (click to expand)</summary>

```
{
  "toCRS": "osdu:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4267:",
  "toUnitZ": "osdu:reference-data--UnitOfMeasure:m:",
  "transformation": "osdu:reference-data--CoordinateTransformation:EPSG::15851:",
  "featureCollection": {
    "type": "AnyCrsFeatureCollection",
    "CoordinateReferenceSystemID": "osdu:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4326:",
    "persistableReferenceCrs": "osdu:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4326:",
    "persistableReferenceUnitZ": "osdu:reference-data--UnitOfMeasure:m:",
    "features": [
      {
        "type": "AnyCrsFeature",
        "properties": {},
        "geometry": {
          "type": "AnyCrsPolygon",
          "coordinates": [
            [
              [
                697339.525,
                7239989.403,
                0
              ],
              [
                697339.525,
                7239989.5,
                0
              ],
              [
                697339.525,
                7239989.6,
                0
              ],
              [
                697339.525,
                7239989.403,
                0
              ]
            ]
          ],
          "bbox": null
        },
        "bbox": null
      }
    ],
    "bbox": null,
    "properties": {}
  }
}
```

</details>

<details>
<summary><strong>Response for v4/convertGeoJson</strong> (click to expand)</summary>

```
{
  "successCount": 4,
  "totalCount": 4,
  "featureCollection": {
    "type": "AnyCrsFeatureCollection",
    "features": [
      {
        "type": "AnyCrsFeature",
        "geometry": {
          "type": "AnyCrsPolygon",
          "coordinates": [
            [
              [
                697339.5244002484,
                7239989.402995734,
                0
              ],
              [
                697339.5244002484,
                7239989.499995734,
                0
              ],
              [
                697339.5244002484,
                7239989.599995733,
                0
              ],
              [
                697339.5244002484,
                7239989.402995734,
                0
              ]
            ]
          ]
        },
        "properties": {}
      }
    ],
    "properties": {},
    "persistableReferenceCrs": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4267\"},\"name\":\"GCS_North_American_1927\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.9786982]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4267]]\"}",
    "persistableReferenceUnitZ": "{\"abcd\":{\"a\":0.0,\"b\":1.0,\"c\":1.0,\"d\":0.0},\"symbol\":\"m\",\"baseMeasurement\":{\"ancestry\":\"L\",\"type\":\"UM\"},\"type\":\"UAD\"}"
  },
  "operationsApplied": [
    "transformation GCS_WGS_1984 to GCS_North_American_1927 using NAD_1927_To_WGS_1984_79_CONUS; 4 points successfully transformed",
    "No unit conversion for Z-axis"
  ]
}
```
### 5. Compound Transform

CRS Converter service will now support compound transormations for convert and convertGeoJson apis. The compound transormation needs to be a to WGS84 transformation and can be specified in the fromCRS or toCRS parameter

</details>

<details>
<summary><strong>Request for v4/convert</strong> (click to expand)</summary>

```
{
    "fromCRS": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4160\"},\"compoundCT\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"8517\"},\"cts\":[{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1528\"},\"name\":\"Chos_Malal_1914_To_Campo_Inchauspe\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"Chos_Malal_1914_To_Campo_Inchauspe\\\",GEOGCS[\\\"GCS_Chos_Malal_1914\\\",DATUM[\\\"D_Chos_Malal_1914\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_Campo_Inchauspe\\\",DATUM[\\\"D_Campo_Inchauspe\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",160.0],PARAMETER[\\\"Y_Axis_Translation\\\",26.0],PARAMETER[\\\"Z_Axis_Translation\\\",41.0],OPERATIONACCURACY[10.0],AUTHORITY[\\\"EPSG\\\",1528]]\"},{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"1527\"},\"name\":\"Campo_Inchauspe_To_WGS_1984_2\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"Campo_Inchauspe_To_WGS_1984_2\\\",GEOGCS[\\\"GCS_Campo_Inchauspe\\\",DATUM[\\\"D_Campo_Inchauspe\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"Geocentric_Translation\\\"],PARAMETER[\\\"X_Axis_Translation\\\",-154.5],PARAMETER[\\\"Y_Axis_Translation\\\",150.7],PARAMETER[\\\"Z_Axis_Translation\\\",100.4],OPERATIONACCURACY[0.5],AUTHORITY[\\\"EPSG\\\",1527]]\"}],\"name\":\"Chos Malal 1914 to WGS 84 (1)\",\"policy\":\"Concatenated\",\"type\":\"CT\",\"ver\":\"PE_10_9_1\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4160\"},\"name\":\"GCS_Chos_Malal_1914\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_Chos_Malal_1914\\\",DATUM[\\\"D_Chos_Malal_1914\\\",SPHEROID[\\\"International_1924\\\",6378388.0,297.0]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4160]]\"},\"name\":\"Chos Malal 1914 to WGS 84 (1)\",\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
    "toCRS": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"GCS_WGS_1984\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4326]]\"}",
    "points": [
        {
            "x": -69.00077939,
            "y": -38.00089782,
            "z": 0
        }
    ]
}
```

</details>

<details>
<summary><strong>Response for v4/convert</strong> (click to expand)</summary>

```
{
    "successCount": 1,
    "points": [
        {
            "x": 120.00000954373071,
            "y": -37.99999999579002,
            "z": 0.0
        }
    ],
    "operationsApplied": [
        "transformation GCS_Chos_Malal_1914 to GCS_WGS_1984 using Chos Malal 1914 to WGS 84 (1); 1 points successfully transformed"
    ]
}
```

</details>


### 6.2.1 Python script to help generate the Request for test data
<details>
<summary><strong>Python script to help generate the Request for test data</strong> (click to expand)</summary>


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

