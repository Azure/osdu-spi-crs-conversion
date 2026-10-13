# OSDU CRS Conversion Service for Azure

[![Release](https://img.shields.io/github/v/release/Azure/osdu-spi-crs-conversion)](https://github.com/Azure/osdu-spi-crs-conversion/releases)
[![Validate](https://github.com/Azure/osdu-spi-crs-conversion/actions/workflows/validate.yml/badge.svg?branch=main)](https://github.com/Azure/osdu-spi-crs-conversion/actions/workflows/validate.yml)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

> [!NOTE]
> Shared service code comes from the [OSDU community upstream](https://community.opengroup.org/osdu/platform/system/reference/crs-conversion-service); the [OSDU documentation](https://osdu.pages.opengroup.org/platform/system/reference/crs-conversion-service/) covers the API.

CRS Conversion converts points, GeoJSON features, wellbore trajectories, and seismic bin grids from one coordinate reference system to another.

## At a glance

| | |
|---|---|
| API base path | `/api/crs/converter/` (versions `v2`, `v3`, `v4`) |
| Swagger UI | `/api/crs/converter/swagger` |
| Health | `:8080/api/crs/converter/actuator/health` |
| Depends on | Storage, for CRS records referenced by ID in `v3` and `v4` requests |
| Azure resources | None of its own; `KEYVAULT_URI` must still resolve at startup |
| Deployed by | [OSDU SPI Stack](https://github.com/Azure/osdu-spi-stack) (`software/stacks/osdu/services-reference/crs-conversion.yaml`), defined but not deployed today |

## Repository layout

[CONTRIBUTING.md](CONTRIBUTING.md) explains where each kind of change belongs.

| Path | Owner | Contents |
|---|---|---|
| `crs-converter-core/` | OSDU upstream | Shared service code |
| `provider/crs-converter-azure/` | This repository | Azure provider, in the nested module `crs-converter-aks` |
| `apachesis_setup/SIS_DATA/` | OSDU upstream | Apache SIS reference data the conversion engine reads |
| `crs-converter_acceptance_test/` | OSDU upstream | End-to-end pytest suite run against a deployed environment |
| `testing/crs_converter_test_core/` | OSDU upstream | Shared integration tests |
| `testing/crs_converter_test_azure/` | This repository | Azure runner for the integration tests |

## Build

Requires Java 17 and Maven 3.6.3+. OSDU dependencies resolve from the public community registry through the settings file in `.mvn`. The unit tests in `crs-converter-core` read Apache SIS data from the directory named in `SIS_DATA`, so point it at this checkout before building, as upstream CI does:

```bash
export SIS_DATA="$PWD/apachesis_setup/SIS_DATA"
mvn --settings .mvn/community-maven.settings.xml -P core,azure clean install
```

The runnable jar lands at `provider/crs-converter-azure/crs-converter-aks/target/crs-converter-aks-*-spring-boot.jar`.

## Configuration

SPI Stack defines the service's environment in two places: the shared `osdu-config` ConfigMap and the service's own entry in [`services-reference/crs-conversion.yaml`](https://github.com/Azure/osdu-spi-stack/blob/main/software/stacks/osdu/services-reference/crs-conversion.yaml). The reference layer's kustomization leaves that entry out, so no SPI Stack environment runs the service today. The tables below list what CRS Conversion actually reads from those files.

**Shared, from `osdu-config`:**

| Variable | Purpose |
|---|---|
| `KEYVAULT_URI` | Central Key Vault |
| `AAD_CLIENT_ID` | Application ID that caller tokens are issued for |
| `SERVER_PORT` | HTTP port (`8080`) |

**Specific to CRS Conversion**, from `services-reference/crs-conversion.yaml`:

| Variable and value on SPI Stack | Purpose |
|---|---|
| `SPRING_APPLICATION_NAME`<br>`crs-conversion` | Application name |
| `SERVER_SERVLET_CONTEXTPATH`<br>`/api/crs/converter/` | API base path |
| `AZURE_PAAS_WORKLOADIDENTITY_ISENABLED`<br>`true` | Authenticate to Azure with workload identity |
| `SIS_DATA`<br>`/app/sis-data` | Apache SIS reference data, filled by an init container |
| `ENTITLEMENT_URL`<br>`http://entitlements/api/entitlements/v2` | Entitlements client; the property has no default, so it must stay set |
| `STORAGE_URL`<br>`http://storage/api/storage/v2` | CRS records referenced by ID |

The stack file also sets `ACCEPT_HTTP`, `AZURE_ISTIOAUTH_ENABLED`, `SERVICE_DOMAIN_NAME`, and `PARTITION_SERVICE_ENDPOINT`; nothing in this service reads them. The service authenticates to Azure with workload identity, which injects `AZURE_CLIENT_ID` and a federated token; there are no client secrets.

## Test

| Suite | Where | Runs in CI | Run it yourself |
|---|---|---|---|
| Unit | `crs-converter-core` | Pull requests (Java Build) | `mvn ... install` from [Build](#build) |
| Acceptance | [`crs-converter_acceptance_test`](crs-converter_acceptance_test/docs/README.md) | No | `./run-acceptance-tests.sh` from that directory |
| Integration | `testing/crs_converter_test_azure`, `testing/crs_converter_test_core` | No | See below |

CI runs the unit tests on pull requests from this repository that change code. Documentation-only changes skip the build.

**Acceptance** is a pytest suite covering the `v2`, `v3`, and `v4` APIs. It does not run in CI yet: SPI Stack does not deploy the service, and the template's suite image builds Maven suites only. Its README lists the inputs: the host in `VIRTUAL_SERVICE_HOST_NAME`, the partition in `MY_TENANT`, the entitlements domain and an existing legal tag for the Storage-backed `v3` and `v4` tests, and a bearer token in `PRIVILEGED_USER_TOKEN`, which `spi token` can supply.

**Integration** is the older suite carried from upstream. Its Azure runner, `run-integration-tests.sh`, runs the shared `v2` and `v3` tests and accepts a bearer token in `INTEGRATION_TESTER_ACCESS_TOKEN`. The runner hardcodes its entitlements domain and legal tag, and the suite has not been proven against SPI Stack. Acceptance covers the same API surface.

To call the API by hand, `spi token` mints a bearer token:

```bash
curl -H "Authorization: Bearer $(spi token)" -H "data-partition-id: <partition>" \
  https://<gateway>/api/crs/converter/v3/info
```

## Deploy

For a pull request from this repository that changes code, CI builds the service image, `osdu-spi-crs-conversion`, and publishes it to GHCR. The Deploy and Test lane skips this repository until `.spi/service.yaml` declares its suites and SPI Stack deploys the service. This repository does not own infrastructure; SPI Stack does.

Pinning `crs-conversion` to an image such as `ghcr.io/azure/osdu-spi-crs-conversion@sha256:<digest>` with `spi service pin` changes nothing that runs while the reference layer leaves the HelmRelease out.

## Service notes

**SIS reference data.** The service image carries no Apache SIS data. On SPI Stack, an init container downloads `apachesis_setup/SIS_DATA` from the upstream project's `master` branch at pod start and mounts it at `/app/sis-data`. Changes to `apachesis_setup/` in this repository do not reach a deployment.

**Authorization.** The provider sets `azure.istio.auth.enabled=true` in `application.properties`, so the service trusts the mesh's token validation and makes no Entitlements call per request. `AZURE_ISTIOAUTH_ENABLED` binds to a different property and does not change this.

## License

Copyright © Microsoft Corporation

Licensed under the [Apache License 2.0](LICENSE).
