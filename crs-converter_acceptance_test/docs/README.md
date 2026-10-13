### Running E2E Tests

You will need to have the following environment variables defined.

#### Required

| name                        | value                                                                   | description                                              | sensitive? | source |
|-----------------------------|-------------------------------------------------------------------------|----------------------------------------------------------|------------|--------|
| `VIRTUAL_SERVICE_HOST_NAME` | eg. `osdu.dev1.osdu-cimpl.opengroup.org` or `https://host` (ADR-046)   | Bare hostname or full instance endpoint under test       | no         | -      |
| `MY_TENANT`                 | eg. `osdu`                                                              | OSDU data partition / tenant used for testing            | no         | -      |
| Authorization               | see tables below                                                        | Bearer token or OIDC client credentials                  | yes        | -      |

#### Environment facts (required for v3/v4 storage-backed tests)

| name                | value                        | description                                                                 | sensitive? | source |
|---------------------|------------------------------|-----------------------------------------------------------------------------|------------|--------|
| `MY_REPLACE_DOMAIN` | eg. `opengroup.org`          | Entitlements domain used in ACL emails (`data.default.owners@{partition}.{domain}`) | no | - |
| `MY_LEGAL_TAG`      | eg. `osdu-public-usa-dataset`| Legal tag that must already exist in the environment                        | no         | -      |

#### Optional / derived

| name                        | value                                              | description                                                                 | sensitive? | source |
|-----------------------------|----------------------------------------------------|-----------------------------------------------------------------------------|------------|--------|
| `BASE_URL`                  | default `/api/crs/converter`                       | Service path appended to the instance origin                                | no         | -      |
| `STORAGE_URL`               | default `<origin>/api/storage/v2/records`          | Storage service URL; derived from `VIRTUAL_SERVICE_HOST_NAME` when unset    | no         | -      |
| `MY_TEST_ID`                | eg. `12345`                                        | Optional traceability prefix; always combined with `CI_JOB_ID` when set     | no         | -      |

`VENDOR` is no longer used. Unauthorized-token tests assert HTTP 401/403 only.

Authentication can be provided as OIDC config:

| name                                            | value                                   | description                   | sensitive? | source |
|-------------------------------------------------|-----------------------------------------|-------------------------------|------------|--------|
| `PRIVILEGED_USER_OPENID_PROVIDER_CLIENT_ID`     | `********`                              | PRIVILEGED_USER Client Id     | yes        | -      |
| `PRIVILEGED_USER_OPENID_PROVIDER_CLIENT_SECRET` | `********`                              | PRIVILEGED_USER Client secret | yes        | -      |
| `TEST_OPENID_PROVIDER_URL`                      | `https://keycloak.com/auth/realms/osdu` | OpenID provider url           | yes        | -      |

Or tokens can be used directly from env variables:

| name                    | value      | description           | sensitive? | source |
|-------------------------|------------|-----------------------|------------|--------|
| `PRIVILEGED_USER_TOKEN` | `********` | PRIVILEGED_USER Token | yes        | -      |

#### Group / role requirements

The privileged identity used for acceptance must be able to:

* call CRS Converter convert/info/trajectory endpoints for the target partition
* create and delete Storage records used by v3/v4 fixtures (`service.storage.user` or equivalent)
* own ACL groups referenced in fixtures: `data.default.owners@{MY_TENANT}.{MY_REPLACE_DOMAIN}` and `data.default.viewers@{MY_TENANT}.{MY_REPLACE_DOMAIN}`

#### Coverage decisions (issue #262 / epic 52)

* **API versions:** v2, v3 and v4 remain in scope. All three are still served (`/v2`, `/v3`, `/v4`).
* **Parity with `testing/crs_converter_test_core/`:** acceptance has 52 `test_` functions vs 53 in the core tree. The only missing function is `test_suite.py::test_full_suite`, a bulk data-driven runner that needs `DATA_DIR` / `DATA_PATTERN` / `REPORT_PATH`. It is deliberately not ported; the 38 integration scenarios in `test_crs_converter_v{2,3,4}.py` plus the 14 swagger API stubs already cover the served APIs. Accepted residual: 1 suite-runner function.

Execute following command to build code and run all the integration tests:

 ```bash
 # Note: this assumes that the environment variables for integration tests as outlined
 #       above are already exported in your environment.
 # run acceptance tests
 $ chmod +x ./run-acceptance-tests.sh
 $ ./run-acceptance-tests.sh
 ```
