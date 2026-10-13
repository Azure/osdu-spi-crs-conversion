# State of the CRS Services in OSDU 2023-04-17

_This document provides an overview of the "State of the CRS" in OSDU R3.M17, April 2023. The data definitions that model Coordinate Reference Systems and locations and REST services are workable and "definition of done" was reached. This document is intended to summarize limitations, known deficiencies and gotchas._

**Revision Log**

| **Version** | **Reason for change** | **Author** | **Date** |
|-------------|-----------------------|------------|----------|
| 1\.0 | Initial version | OSDU Geomatics Integration workstream | 2023-11-11 |
|      |                 |                                       |            |

**Table of Contents**

- [1\. CRS and CT data definition](#1-crs-and-ct-data-definition)
  - [1\.1 Status](#11-status)
    - [1\.1.1 Coordinate Reference System](111-coordinate-reference-system)
    - [1\.1.2 Coordinate Transformation](112-coordinate-transformation)
    - [1\.1.3 Maintaining CRSs on OSDU instances](113-maintaining-crss-on-osdu-instances)
  - [1\.2 Gaps & Issues](#12-gapsissues)
- [2\. Modeling of Locations](#2-modeling-of-locations)
  - [2\.1 Status](#21-status)
  - [2\.2 Gaps & Issues](#22-gapsissues)
- [3\. CRS Catalog Service](#3-crs-catalog-service)
  - [3\.1 Status](#31-status)
  - [3\.2 Gaps & Issues](#32-gapsissues)
- [4\. CRS Conversion Service](#4-crs-conversion-service)
  - [4\.1 Status](#41-status)
  - [4\.2 Gaps & Issues](#42-gapsissues)
- [5\. CSP specifics](#5-csp-pecifics)
  - [5\.1 Status](#51-status)
  - [5\.2 Gaps & Issues](#52-gapsissues)

# 1\. CRS and CT data definition

## 1\.1 Status

### 1\.1.1 Coordinate Reference System

_A CRS contains the metadata needed to unambiguously interpret coordinates._

- CRSs are modeled in CoordinateReferenceSystem:1.1.0. ([entity schema definition](https://gitlab.opengroup.org/osdu/subcommittees/data-def/work-products/schema/-/tree/master/E-R/reference-data)).
  - OSDU supports various types of coordinate reference systems, and the type is identified by both `CoordinateReferenceSystemType` (used by the system) and `Kind` (facing the end-user). The `Code` for the CRS is a string according to OSDU standard constructed from the respective Authority Code (e.g. EPSG) which can be accessed via the `CodeAsNumber` property. Additional metadata such as `Description`, `AttributionAuthority`, and `AliasNames` are also included.
- CRSs are centrally managed in OSDU as reference data. This means that:
  - In OSDU, ingested data should use a CRS **`record id`** to point to the CRS definition, and not include the definition.
  - In OSDU, using a ** standardized CRS `record id`** is critical. This is described in the [Guide on Frame of Reference](https://community.opengroup.org/osdu/data/data-definitions/-/blob/master/Guides/Chapters/04-FrameOfReference.md#423-conventions-for-coordinatereferencesystem-and-coordinatetransformation).
- A **`PersistableReference`** (PR) stored as property of the CRS record (and CT) contains the actionable numeric definition of the entity.
  - OSDU has standardized on (escaped) Esri WKT v1 strings for the PR. Note that Esri WKT v1 is different than OGC WKT 1 (ISO).
  - The PR string is for numerical computations only to pass to the geodetic engine. Metadata such as the Name and Code for the entity are stored as properties and may be different in the WKT.
- OSDU in practice uses the early-binding geodetic data model (a BoundCRS includes the definition of the Coordinate Transformation, whereas in the ISO 19111 late-binding data model the CRS and CT are modeled as independent entities).
  - In OSDU, a BoundCRS is defined with a transform "to WGS 84". It cannot be defined "from WGS 84" (fixed in convert v4 in 2023) and cannot be to another "target "hub" (or "pivot") CRS.
  - In OSDU a BoundCRS should be associated with coordinate data, unless the data is already (based on) WGS 84.
- Compound CRSs are not used in OSDU (A compound CRS entity combines a horizontal CRS and vertical CRS).
- Geocentric and Geographic3D are created by the EpsgManifestGenerator.py script (it resolves dependencies upwards in the GeoRepository, e.g., from projected to geog2D to geog3D to geocentric) and then stored in OSDU as reference data. These are not used in Bound definitions, and this may become an issue if ellipsoidal height is stored and data needs to be normalized, or if geocentric is used for 3D models or calcs.
- Non-standard, custom schemas in OSDU may use individually named coordinate properties matching imported data structures. In such cases, the metadata can still provide the frame of reference context, including units.

### 1\.1.2 Coordinate Transformation

_Coordinate Transformations (CT) enact a change of datum. They are modeled in the OSDU, though in general hidden as part of the BoundCRS definition as described above._

- CTs are modeled in CoordinateTransformation:1.1.0 ([entity schema definition](https://gitlab.opengroup.org/osdu/subcommittees/data-def/work-products/schema/-/tree/master/E-R/reference-data)).
- CT in OSDU are modeled based on ISO 19111 which is a widely recognized international standard for coordinate transformations. However, OSDU uses Esri WKT 1 which makes it not EPSG/ISO complete/compliant in terminology, etc.
- CT do not explicitly store parameters as individual properties in the manifest (OSDU uses the ESRI WKT1 Persistable Reference). As noted above, the ISO 19111 data model includes a reversible flag for the method and parameters to indicate how to reverse. If this ever is modeled in more detail in OSDU then this should be added.
- CT in OSDU are assumed to be reversible. There is no property to indicate whether a transformation method is reversible or not (nor for reversibility of parameters).
- Horizontal CTs must be between geog2D CRSs in OSDU.
- Concatenated Transformations are able to be stored and M24 (Aug2024) implements it if target is WGS 84.
  - Concatenated operations should not be used in OSDU. However, the tool EpsgManifestGenerator supports multi-step/concatenated transformations. Such transformations are stored with "ConcatenatedOperation" as Kind. The details for each step are stored in the Persistable Reference string, and each step is also added to the transformation manifest as individual records (CoordinateTransformation:1.1.0).
- Vertical Transformations are able to be stored (but Convert API cannot execute them).
- Polygons for CT extent area of use are not stored in OSDU. This implies that automatically selecting a CT between 2 CRSs is never going to work on BBOX rectangular areas. (Hence OSDU relies on data to be associated with a BoundCRS assigned by computer/user during ingest and relies on the ingest procedures to identify the appropriate CT by the competent loader).
- The OSDU data model incorrectly has: `Usages[i].Name` and `PreferredUsage.Name`, which are not part of EPSG and duplicated from the extent name. This property should be deleted from OSDU. I guess we will leave it.
- CT entity is not used in the geodetic engine or other models (instead the BoundCRS includes the definition of the CT as a copy). If the OSDU changes to enable direct transformation then the CT entity becomes important to call the SIS engine properly, with correct forward or reverse direction.
- Grid file names are to be as in the ESRI WKT1 (and would need to be added to the Apache SIS definitions file folder on the cloud instance).


### 1\.1.3 Maintaining CRSs on OSDU instances

- Geodetic definitions are reference data under LOCAL governance. This allows Operators to replace the distributed definitions with their company (naming) conventions and add their custom definitions. This flexibility is useful for organizations with specific geodetic requirements.
- An OSDU release distribution comes with approximately 3000 CRS and CT definitions based on EPSG and Bound CRS definitions in use by Operators for worldwide operations.
  - To add new definitions, contact OSDU Geomatics Integration workgroup via slack or log an issue with the schema reference data repo.
- OSDU requires the definition of BoundCRSs, i.e., an end-user decision to associate a certain CT in a certain area with a given CRS. This selection is somewhat arbitrary and different Operators/users may prefer to use different CTs in the same area, which complicates distributing a standard set.
- For Operators who need to customize the content there are two tools available in the [schema repository](https://gitlab.opengroup.org/osdu/subcommittees/data-def/work-products/schema/-/tree/master/ReferenceValues/Resources/IOGP)
  - EpsgManifestGenerator.py
    - The EpsgManifestGenerator is a non-commercial Python utility used in OSDU to create CoordinateReferenceSystem and CoordinateTransformation reference-value manifest records. It allows users to install Python package requirements, set environment settings, and load data from the EPSG (or Operator) GeoRepository. Users can either augment the manifests with single elements using EPSG codes or perform complete catalog processing by parsing an Excel workbook. The resulting records are added to CoordinateReferenceSystem.1.1.0.json and CoordinateTransformation.1.1.0.json files. The BoundCRS PreferredUsage and Extents are determined based on heuristics, considering the intersection or containment of the CRS and CT extents. Additionally, the schema supports spatial discovery through the data.Wgs84Coordinates property for bounding boxes and extents crossing the anti-meridian. For more information see the README.md file in the above link.
  - EpsgManifestPublisher.py
    - The EpsgManifestPublisher is a python utility that make it more convenient to load the generated manifests to an OSDU instance. It enables a user to check which records already exists and whether they are synchronized with the definitions in the external database (EPSG or Operator GeoRepository). For more information see the [README.md](https://gitlab.opengroup.org/osdu/subcommittees/data-def/work-products/schema/-/tree/master/ReferenceValues/Resources/IOGP) file.

## 1\.2 Gaps/Issues

**Persistable Reference WKT strings**

- The PR used to normalize is not returned/added to a stored manifest at time of data ingestion. This is desirable for completeness to provide a snapshot in time of the definition used to normalize at the time of ingest. It is not so much for self-containedness, because the CRS record id points to the definition, and even if that definition does not exist or was somehow corrupted, the CRS record id has the EPSG codes that permanently identify the entities.
- Non-standard ESRI WKT1 is used to define the CRS numerically (in order to ensure operability with the current configuration of the geodetic engine). Using ESRI WKT1 instead of the standard ISO/OGC WKT2 format can be confusing due to its non-standard format, lack of complete compliance with EPSG or ISO standards, and limited support in some geospatial tools and libraries. Additionally, long-term maintenance and futureproofing could be uncertain for ESRI WKT1, as it is proprietary and not part of the evolving ISO/OGC standard. In the future, change to ISO WKT2 aligns with industry standards, ensures better interoperability, and allows seamless integration with other geospatial systems. However, this would be a significant effort to change and has not been undertaken for risk/reward ratio.

**Coordinate Reference Systems**

- CRS definitions do not have properties to store parameters (except as PR string). Long term that is an issue because it is a cumbersome format if CRS definitions would be maintained directly on the platform.
- The BaseCRS of a BoundProjected should (logically) be a `BoundGeographic2d`, but is of kind `Geographic2D`. There is no anticipated impact and this will not be changed (a change will have some impact and may break applications).

**Coordinate Transformations**

- CT definitions do not have properties to store the parameters (except as PR string).
- CT that use grid files (mainly NADCON and NTv2, ignoring vertical transforms) only work if that grid file is physically present and available to the convert Services. There is a gap in knowledge what files are distributed, what happens when the file is missing, and documentation or processes how to add a new grid file to a distribution or deployed instance.
- Concatenated CT did not work prior to M24 for SIS. This was added to the code but is limited to operations with WGS 84 as the target.

**Maintenance Process**

- Before each release a Geomatics maintainer should review the defined OSDU "out of the box" CRSs that ship, and consider adding new definitions. This is now done ad-hoc at best and not a defined process.

# 2\. Modeling of locations using coordinates

## 2\.1 Status

- Geometries are modeled in AbstractSpatialLocation, using AnyCrsFeatureCollections.
  - Geometries use coordinates.
  - Geometries can be 2D or 3D.
  - Geometries are based on GeoJSON, but with an "OSDU extension" for "AnyCrs".
  - GeoJSON assumed gravity-based heights "EGM2008" (MSL) for vertical. Before the [2022 Errata](https://www.rfc-editor.org/errata/eid7261) it was documented that GeoJSON used ellipsoidal heights. OSDU uses "MSL" as default which can be considered equivalent (to accuracy of ~1.5m).  
  - Respect the ring order as in GeoJSON.
  - Features can have geometries of various types, e.g., lines and points. 
- For 2D a "horizontal" CRS is defined.
  - This should be a BoundCRS for normalization to work to WGS 84.  There is no property to store a transformation otherwise as part of AbstractSpatialLocation which would be required.
  - The unit for horizontal coordinates is stored with the CRS.
  - The positive direction of the axes is stored with the CRS
    - **(is that respected? can we have westings? or polar projection?  We assume for now that only "Easting,Northing" type of projected CRSs are guaranteed to work in OSDU; use CAUTION for other cases!)**
  - The order of the axes is not stored in the Esri WKT 1. The model and API use a fixed "x", "y", ("z") order, where "x" is easting or longitude. Note that an app could swap to the "correct" (EPSG defined) order by interpreting the axes property that is part of the CRS record.
- For 3D ("z" coordinates), additionally a vertical CRS must be identified.
  - The +ve direction of the vertical CRS is respected, i.e., if the CRS is a height system, then a positive value is above the vertical datum reference surface and increasing values go "up".
  - The unit of the vertical CRS is respected. Developers must look up the vertical unit of the CRS definition. The only exception is when a CRS is not defined, then the property for the vertical unit is defining. In practice this implies that the explicit property should have the same unit as the vertical CRS used to store the value, and any value needs to be converted to the CRS UOM before storing.
- VerticalMeasurements entity is handled differently than SpatialLocations. VerticalMeasurement have a property to indicate if the value given is along-hole, or a (true vertical) height or depth.  It also has a property to define the unit.

## 2\.2 Gaps/Issues

- It is not very clear to developers how vertical data are handled, i.e., unit and orientation as attributes, or pointing to a vertical CRS that defines these. The problem is that OSDU does not use the concept of vertical datum but vertical CRS. And a CRS comes with the issue that the unit is defined by the CRS, as well as its positive direction. It is cumbersome to load data e.g., a location 100ft above LAT can only be stored in meters and as a depth (downward positive), i.e., in the example: "z = -30.48m LAT depth" is awkward and error prone.
- It is not possible to describe vertical accuracy with 3D location (except by generic QC remarks). An update to the data model with specific properties for vertical accuracy is desired.
- It is not possible to describe the source of coordinates via a type. An update to the data model is desired (e.g., associate it with a survey plat).
- Remarks/trail is not a string array but a string. When versions are updated there is a risk of losing remarks.
- 4D coordinates and transformations (temporal) are not supported. There is a field for a coordinate epoch with SL.
- AsIngestedCoordinates are not indexed and not returned by Search. Original coordinates must be retrieved from Storage or by a DDMS Service, or from a metadata file.  In M23, a single point of the AsIngested coordinates are returned as "FirstPoint" properties.
- There is no property `TransformationID` in `AbstractSpatialLocation`. It only works with an implicitly encapsulated CT as part of the EBCRS. But this property should be added to the schema definition so that the transformation can be explicitly stored.  Note that V4 of convert and convertGeoJson have the ability to transform with an explicitly specified transformation (which overrides the implicit one if the source and or target CRSs are Bound).


# 3\. CRS Catalog Service

## 3\.1 Status

- The main goal of the OSDU CRS Catalog Service is for end-users to make CRS selections, search for CRSs based on constraints, download the entire catalog for local caching and refreshing, and access various sub-sets of the catalog. See [tutorial](https://community.opengroup.org/osdu/platform/system/reference/crs-catalog-service/-/blob/master/docs/v3/tutorial/CRS_Catalog_Service_howto.md) and [wiki](https://community.opengroup.org/osdu/platform/system/reference/crs-catalog-service/-/wikis/home).
- The v3 endpoints act as domain helpers to facilitate querying CRS and CT reference data residing in Elasticsearch. Each response, except for the area of use endpoint, includes a query string to help users directly query the Search service for the same results with reduced latency. See [openapi spec and Java code](https://community.opengroup.org/osdu/platform/system/reference/crs-catalog-service/-/blob/master/docs/api_spec/crs-catalog-openapi-v3.yaml).
- The service does enable producing Persistable References to be stored with data, describing the CRS and making it catalog-independent, so any consumer can understand the CRS definition even if a different catalog is used in the future. However this is not encouraged and a reference to CRS record with standard id should be the basis of all developments.
- The service supports intersectional queries for Search service spatial filters, which allows passing coordinates in a polygon and retrieving documents with polygons that intersect with it.
- v2 of the catalog service should not be used and is deprecated and not compatible with CRS records managed on OSDU as in v3.
  - **Any usage for any purpose of v2 CRS services should be deprecated/removed from OSDU and postman collections.**

## 3\.2 Gaps/Issues

- Area of Use polygons are not stored, and hence automatic CT selection based on data locations cannot work properly. It is still useful to check coordinates for validity against the rectangular BBOX extent of the CT/CRS and an API is available for that.

# 4\. CRS Conversion Service

## 4\.1 Status

- OSDU CRS Conversion Service facilitates converting spatial coordinates represented by an array of 3D points (x, y, z) between different coordinate reference systems (CRS). See [tutorial](https://community.opengroup.org/osdu/platform/system/reference/crs-conversion-service/-/blob/master/docs/v3/tutorial/CRS_Convert_Service_howto.md) and [wiki](https://community.opengroup.org/osdu/platform/system/reference/crs-conversion-service/-/wikis/home).
- Conversions require specifying the source and target CRS along with the list of points, and the service can also compute wellbore trajectories and convert bin grids. See [openapi spec and code](https://community.opengroup.org/osdu/platform/system/reference/crs-conversion-service/-/blob/master/docs/v3/api_spec/crs_converter_openapi.json).
- There is an API to convert a point(s) with either a list or geojson like i/o.
- There is an API to convert Bin Grids that should be used when loading data to OSDU.
- There is an API to compute a wellbore trajectory from directional survey data.
  - **Trajectory Interpolation (v4)**: The `convertTrajectory` endpoint supports interpolating additional stations at specified measured depths:
    - Set `interpolate: true` (default) to enable interpolation
    - Use `MD_i.md_i: [50, 75, 150]` for explicit MD values
    - Use `MD_i.md_interval: 25` for regular spacing (every 25 units)
    - Interpolated stations use minimum curvature interpolation for inc/azi and position
    - Response stations include `original: true/false` to distinguish survey vs interpolated stations
    - Set `interpolate: false` to process only original survey stations (MD_i is ignored)
- Geodetic Engine
  - Apache SIS v1.3 is the engine.
  - GIGS tested and passed (Apache SIS engine is "OSDU certified").
  - For grid files Dataset keyword is important.
  - Grid files need to be deployed on the system in `apachesis_setup/SIS_DATA/DatumChanges`, e.g. https://community.opengroup.org/osdu/platform/system/reference/crs-conversion-service/-/tree/v0.23.0/apachesis_setup/SIS_DATA/DatumChanges?ref_type=tags
- "Secretly" implicit identification is used and not the explicit WKT for definitions that can be found in the Apache SIS database by lookup of the EPSG code.

## 4\.2 Gaps/Issues

- Need integration/continuous testing to be defined better, such that GIGS type tests are run before a distribution.
- For (World and Web) Mercator, there is a bug (or feature) that if lon=+181 is used the easting keeps counting, whereas lon=-179 it gives a big negative easting. Geodesists consider this a bug because it is the same physical input location.
- GIGS testing was completed and passed. However, note that the engine Apache SIS library is reading the EPSG codes, i.e., not the WKT that is stored. The SIS engine is trusted to have a proper definition aligned with the EPSG database, but the WKT as created in OSDU is therefore not 100% validated for its correctness because the engine uses the EPSG codes (Equinor has been testing and found WKT correct in 2023/07).
  * Further information on GIGS testing: CRS Services may be ultimately used to handle or manipulate position-critical data with safety implications. Therefore it is essential that the integrity and accuracy of Geomatics components is demonstrated. IOGP [GIGS](https://www.gigs.iogp.org) (Geospatial Integrity of Geoscience Software) is an open-source digital testing framework used to benchmark and guide the development and implementation of high quality geospatial applications. It offers a comprehensive package of checklists, test data and guidance documentation to help identify and troubleshoot data integrity issues and mitigate the risks associated with positioning errors. The geodetic engine underlying the OSDU CRS Catalog and CRS Convert services (Apache SIS 1.1) has been fully GIGS-tested in order to assure its integrity and quality. A summary of the remaining issues to be resolved as identified by GIGS testing can be found [here](https://github.com/michaelarnesonint/ApacheSIS_GIGS_Validation/blob/main/README.md). GIGS Testing is also embedded in the OSDU Milestone Release [CICD pipeline](https://community.opengroup.org/osdu/qa/-/tree/main/Postman%20Collection/18_CICD_Setup_CRSConversionAPI)
- It is not possible in OSDU Convert Service (using Apache SIS) to execute:
  - A direct transformation from CRS A to B with CT C (must always go through WGS 84 hub, using a BoundCRS definition).  V4 of `convert `and `convertGeoJson` have the ability to transform with an explicitly specified transformation (which overrides the implicit one if the source and or target CRSs are Bound). This is not implemented for `convertBinGrid` nor `convertTrajectory`.
  - A vertical transformation.
  - A concatenated (horizontal) transformation.
  - Time-specific (PMO) and Time-dependent Dynamic transformations are not supported.
  - Transformations directly between projected CRSs as some countries have defined.
- Not all EPSG defined operation methods are supported in the OSDU (e.g., PMO, but also some projection methods and ).
- RESOLVED in M22 2023-12-19: There is an assumption that all transformations used in BoundCRSs are defined "from sourceCRS to WGS 84". This is used when determining to apply the CT "forward" or "in reverse" (i.e., the transform is hardcoded to be applied in reverse when the target CRS is not WGS 84, without checking source and target of the defined CT).  There is an issue logged to check the target is WGS 84 and not the source.


# 5\. CSP specifics

_There are no known limitations from the reference implementation or installation between Cloud Service Providers. However, for normalization some CSP used convert v2 API which has been deprecated in 2022 and will not continue to work and does not use the CRS records_

## 5\.1 Status
- none

## 5\.2 Gaps/Issues
- none

<span dir="">\#</span>EOF.