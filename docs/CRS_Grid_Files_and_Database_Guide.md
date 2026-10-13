# CRS Conversion Service: Grid Files and Database Guide

This document explains how the EPSG database and grid files work in the OSDU CRS Conversion Service, and how operators can add custom grid files.

---

## Table of Contents

1. [Apache SIS Database Architecture](#apache-sis-database-architecture)
2. [Grid Files for Datum Transformations](#grid-files-for-datum-transformations)
3. [Adding Custom Grid Files](#adding-custom-grid-files)
4. [Cloud Provider Deployments](#cloud-provider-deployments)
5. [ESRI WKT to Apache SIS Grid File Name Mapping](#esri-wkt-to-apache-sis-grid-file-name-mapping)

---

## Apache SIS Database Architecture

The CRS Conversion Service uses **Apache SIS** (Spatial Information System) with an embedded **HSQLDB** database containing the EPSG geodetic registry.

### Database Structure

```
SIS_DATA/
├── Databases/
│   ├── ExternalSources/           # Source SQL files (EPSG registry dump)
│   │   ├── EPSG_Tables.sql        # Table schema definitions
│   │   ├── EPSG_Data.sql          # EPSG dataset (~56k lines)
│   │   └── EPSG_FKeys.sql         # Foreign key constraints
│   ├── SpatialMetadata.data       # HSQLDB binary data (runtime cache)
│   ├── SpatialMetadata.properties # HSQLDB configuration
│   ├── SpatialMetadata.script     # HSQLDB DDL script
│   └── SpatialMetadata/           # Derby-format cache directory
└── DatumChanges/                  # Grid files for transformations
    ├── conus.las, conus.los       # US NADCON grids
    ├── NTv2_0.gsb                 # Canadian NTv2 grid
    └── ... (130+ grid files)
```

### How It Works

1. **Source**: SQL files in `ExternalSources/` are dumps from the [EPSG Geodetic Registry](https://epsg.org/)
2. **Initialization**: On first startup, Apache SIS reads these SQL files and creates an embedded HSQLDB database
3. **Runtime**: The service queries HSQLDB for CRS definitions, transformations, and parameters
4. **Grid Resolution**: Grid file names referenced in EPSG are resolved from the `DatumChanges/` directory

---

## Grid Files for Datum Transformations

Grid files contain precomputed shift values for datum transformations. They are stored in `SIS_DATA/DatumChanges/`.

### Supported Formats

| Format | Extension | Description | Example |
|--------|-----------|-------------|---------|
| **NTv2** | `.gsb` | Canadian/International binary grid | `NTv2_0.gsb` |
| **NADCON** | `.las`, `.los` | US latitude/longitude ASCII grids | `conus.las`, `conus.los` |
| **GEOCON** | `.b` | US binary grids | Various |

### Example: EPSG:15851 (NAD27 to WGS84 CONUS)

This transformation uses NADCON method with the `conus` dataset:

```
Grid files: conus.las (latitude shifts) + conus.los (longitude shifts)
Method: NADCON
WKT: METHOD["NADCON"], PARAMETER["Dataset_conus", 0.0]
```

### Available Grid Files

The service includes grids for:
- **US**: NADCON (conus, alaska, hawaii) + HPGN state grids
- **Canada**: NTv2 national and provincial grids
- **Australia**: GDA94 to GDA2020 grids
- **Europe**: France (ntf_r93), Germany (BETA2007), UK (OSTN15)
- **New Zealand, South America**, and more

---

## Adding Custom Grid Files

You can add custom grid files **without modifying the EPSG database** by using WKT in API requests.

### Step 1: Add the Grid File

Copy your grid file to the `DatumChanges/` directory:



### Step 2: Use WKT to Reference the Grid

Create a transformation WKT that references your grid:

```
GEOGTRAN["My_Custom_Transform",
  GEOGCS["Source_CRS", ...],
  GEOGCS["Target_CRS", ...],
  METHOD["NTv2"],
  PARAMETER["Dataset_my_custom_grid", 0.0]
]
```

**Important**: The `Dataset_` parameter value must match the filename (without extension).

### Step 3: Call the API

```json
{
  "fromCRS": "osdu:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4267:",
  "toCRS": "osdu:reference-data--CoordinateReferenceSystem:Geographic2D:EPSG::4326:",
  "transformation": "{\"authCode\":{\"auth\":\"CUSTOM\",\"code\":\"99999\"},\"name\":\"My_Custom_Transform\",\"type\":\"ST\",\"wkt\":\"GEOGTRAN[...METHOD[\\\"NTv2\\\"],PARAMETER[\\\"Dataset_my_custom_grid\\\",0.0]...]\"}",
  "points": [{"x": -100.0, "y": 35.0}]
}
```

### Key Points

| Requirement | Details |
|-------------|---------|
| Grid location | `SIS_DATA/DatumChanges/` |
| Naming | WKT parameter must match filename (without extension) |
| Auth code | Use `"auth": "CUSTOM"` to bypass EPSG lookup |
| No restart needed | Grid files are resolved at runtime (if using mounted storage) |

---

## Cloud Provider Deployments

Each cloud provider handles grid files differently:

### Comparison

| Provider | Storage Method | Custom Grid Addition | Restart Required |
|----------|---------------|---------------------|------------------|
| **Azure** | Azure File Share (mounted PVC) | Upload to file share | No |
| **AWS** | Baked into Docker image | Rebuild image | Yes |
| **GCP** | Baked into Docker image | Rebuild image | Yes |
| **IBM** | Persistent Volume Claim | Upload to PVC | No |

---

### Azure 

- **Architecture**: Grid files stored in Azure File Share, mounted as a PersistentVolume.


- **Adding Custom Grids**:
Upload to Azure File Share. **No image rebuild or pod restart required**

---

### AWS 

The AWS provider has been removed from this repository.

---

### GCP

**Architecture**: Similar to AWS - grid files baked into Docker image.
**Adding Custom Grids**: Same as AWS (rebuild image).

---

### IBM

- **Architecture**: Uses PersistentVolumeClaim with init container.
- **Adding Custom Grids**: Upload to the PVC-backed storage.

---

## ESRI WKT to Apache SIS Grid File Name Mapping

The CRS converter includes logic to translate ESRI WKT grid file parameters to Apache SIS-compatible format. This is handled by the `SingleTrf.correctFileParametersIfNeeded()` method in the Java codebase.

### ESRI WKT Input Format

Grid files are referenced in ESRI WKT using the `Dataset_` parameter:

```
PARAMETER["Dataset_<grid_name>", 0.0]
```

Examples:
- `PARAMETER["Dataset_conus", 0.0]` 
- `PARAMETER["Dataset_australia/A66_National_13_09_01", 0.0]`
- `PARAMETER["Dataset_canada/Ntv2_0", 0.0]`

### Mapping Process

1. **Method Detection**: The code checks the `METHOD` section to determine if it's `NADCON` or `NTv2`

2. **File Name Extraction**: The `getFileNameFromParameter()` method extracts the grid name:


3. **Extension Addition Based on Method**:

| Method | ESRI WKT Parameter | Apache SIS Output |
|--------|-------------------|-------------------|
| **NTv2** | `Dataset_gridname` | `PARAMETER["Latitude and longitude difference file", "gridname.gsb"]` |
| **NADCON** | `Dataset_gridname` | `PARAMETER["Latitude difference file", "gridname.las"]` + `PARAMETER["Longitude difference file", "gridname.los"]` |

### Complete Transformation Example

**Input ESRI WKT:**
```
GEOGTRAN["My_Transform",
  GEOGCS["NAD27", ...],
  GEOGCS["NAD83", ...],
  METHOD["NADCON"],
  PARAMETER["Dataset_conus", 0.0]]
```

**After processing (what Apache SIS receives):**
```
GEOGTRAN["My_Transform",
  GEOGCS["NAD27", ...],
  GEOGCS["NAD83", ...],
  METHOD["NADCON"],
  PARAMETER["Latitude difference file", "conus.las"],
  PARAMETER["Longitude difference file", "conus.los"]]
```

### Path Handling

For paths with subdirectories like `Dataset_australia/A66_National_13_09_01`:
- Only the filename after the last `/` is extracted: `A66_National_13_09_01`
- The subdirectory path is **not preserved** in the parameter output
- Apache SIS searches for the file in `SIS_DATA/DatumChanges/` and its subdirectories

### Custom Grid File Naming

When creating custom grid files, the name you use in the `Dataset_` parameter must match the file basename:

| Your Parameter | Required Files (NADCON) | Required Files (NTv2) |
|---------------|------------------------|----------------------|
| `Dataset_MY_CUSTOM_GRID` | `MY_CUSTOM_GRID.las` + `MY_CUSTOM_GRID.los` | `MY_CUSTOM_GRID.gsb` |
| `Dataset_custom/MyGrid` | `MyGrid.las` + `MyGrid.los` | `MyGrid.gsb` |

---

## Example payload

This example demonstrates a custom NADCON grid transformation using the following configuration:

| Property | Value |
|----------|-------|
| **From CRS** | NAD27 (EPSG:4267) |
| **To CRS** | WGS84 (EPSG:4326) |
| **Transformation Name** | `My_Custom_GRID_Transform` |
| **Transformation Auth** | `CUSTOM` |
| **Transformation Code** | `9999` |
| **Method** | `NADCON` |
| **Grid Parameter** | `Dataset_CUSTOM_GRID` |
| **Required Grid Files** | `CUSTOM_GRID.las` + `CUSTOM_GRID.los` |

```json
{
    "fromCRS": "{\"authCode\":{\"auth\":\"OSDU\",\"code\":\"1100000\"},\"lateBoundCRS\":{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4267\"},\"name\":\"GCS_North_American_1927\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698213901]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4267]]\"},\"name\":\"OSDU_NAD27_T15851\",\"singleCT\":{\"authCode\":{\"auth\":\"CUSTOM\",\"code\":\"9999\"},\"name\":\"My_Custom_GRID_Transform\",\"type\":\"ST\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGTRAN[\\\"CUSTOM_NAD_1927_To_WGS_1984_79\\\",GEOGCS[\\\"GCS_North_American_1927\\\",DATUM[\\\"D_North_American_1927\\\",SPHEROID[\\\"Clarke_1866\\\",6378206.4,294.978698213901]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433]],METHOD[\\\"NADCON\\\"],PARAMETER[\\\"Dataset_CUSTOM_GRID\\\",0.0],OPERATIONACCURACY[5.0],AUTHORITY[\\\"EPSG\\\",15851]]\"},\"type\":\"EBC\",\"ver\":\"PE_10_9_1\"}",
    "toCRS": "{\"authCode\":{\"auth\":\"EPSG\",\"code\":\"4326\"},\"name\":\"GCS_WGS_1984\",\"type\":\"LBC\",\"ver\":\"PE_10_9_1\",\"wkt\":\"GEOGCS[\\\"GCS_WGS_1984\\\",DATUM[\\\"D_WGS_1984\\\",SPHEROID[\\\"WGS_1984\\\",6378137.0,298.257223563]],PRIMEM[\\\"Greenwich\\\",0.0],UNIT[\\\"Degree\\\",0.0174532925199433],AUTHORITY[\\\"EPSG\\\",4326]]\"}",
    "points": [
        {
            "x": 10.13581065,
            "y": 60.45639161
        }
    ]
}

```



## Quick Reference

### File Locations

| Component | Path |
|-----------|------|
| EPSG SQL source | `SIS_DATA/Databases/ExternalSources/` |
| HSQLDB cache | `SIS_DATA/Databases/SpatialMetadata.*` |
| Grid files | `SIS_DATA/DatumChanges/` |

### Environment Variable

```bash
SIS_DATA=/path/to/apachesis_setup/SIS_DATA
```

### Troubleshooting

| Issue | Solution |
|-------|----------|
| Grid not found | Verify filename matches WKT `Dataset_` parameter |
| HSQLDB not initializing | Delete `SpatialMetadata/` directory and restart |
| New grid not recognized | For Azure/IBM: check file upload; For AWS/GCP: rebuild image |

---

## References

- [EPSG Geodetic Registry](https://epsg.org/)
- [Apache SIS Documentation](https://sis.apache.org/)
- [OSDU CRS Conversion Service](https://community.opengroup.org/osdu/platform/system/reference/crs-conversion-service)
