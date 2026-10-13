# CRS conversion service

## Running Locally

### Requirements

In order to run this service locally, you will need the following:

- [Maven 3.8.0+](https://maven.apache.org/download.cgi)
- [Java 17](https://adoptopenjdk.net/)
- Download the [application-insights-agent](https://github.com/microsoft/ApplicationInsights-Java/releases/tag/3.5.2) jar
- Infrastructure dependencies, deployable through the relevant [infrastructure template](https://dev.azure.com/slb-des-ext-collaboration/open-data-ecosystem/_git/infrastructure-templates?path=%2Finfra&version=GBmaster&_a=contents)
- While not a strict dependency, example commands in this document use [bash](https://www.gnu.org/software/bash/)

### General Tips

**Environment Variable Management**
The following tools make environment variable configuration simpler
 - [direnv](https://direnv.net/) - for a shell/terminal environment
 - [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile) - for [Intellij IDEA](https://www.jetbrains.com/idea/)

**Lombok**
This project uses [Lombok](https://projectlombok.org/) for code generation. You may need to configure your IDE to take advantage of this tool.
 - [Intellij configuration](https://projectlombok.org/setup/intellij)
 - [VSCode configuration](https://projectlombok.org/setup/vscode)


### Environment Variables

In order to run the service locally, you will need to have the following environment variables defined.

**Note** The following command can be useful to pull secrets from keyvault:
```bash
az keyvault secret show --vault-name $KEY_VAULT_NAME --name $KEY_VAULT_SECRET_NAME --query value -otsv
```

**Required to run service**

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `KEYVAULT_URI` | ex `https://foo-keyvault.vault.azure.net/` | URI of KeyVault that holds application secrets | no | output of infrastructure deployment |
| `ENTITLEMENT_URL` | ex `https://foo-entitlements.azurewebsites.net/api/entitlements/v2` | Entitlements API endpoint | no | output of infrastructure deployment |
| `STORAGE_URL` | ex `https://foo-storage.azurewebsites.net/api/storage/v2` | Storage API endpoint | no | output of infrastructure deployment |
| `appinsights_key` | `********` | API Key for App Insights | yes | output of infrastructure deployment |
| `APPLICATIONINSIGHTS_CONNECTION_STRING` | `InstrumentationKey=${appinsights_key}` | App Insights Connection String | yes | output of infrastructure deployment |
| `aad_client_id` | `********` | AAD client application ID | yes | output of infrastructure deployment |
| `spring_application_name` | `crs-conversion-service` | Name of application. Needed by App Insights | no | -- |
| `AZURE_CLIENT_ID` | `********` | Identity to run the service locally. This enables access to Azure resources. You only need this if running locally | yes | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-username` |
| `AZURE_CLIENT_SECRET` | `********` | Secret for `$AZURE_CLIENT_ID` | yes | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-password` |
| `AZURE_TENANT_ID` | `********` | AD tenant to authenticate users from | yes | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-tenant-id` |
| `server_port` | ex `8080` | Port of the server | no | -- |
| `ACCEPT_HTTP` | `true` | TEMPORARY UNTIL HTTPS | no | -- |
| `azure_istioauth_enabled` | `true` | Flag to Disable AAD auth | no | -- |
| `server.servlet.contextPath` | `/api/crs/converter/` | Servlet context path | no | - |
| `cosmosdb_database` | ex `foo-db` | The name of the CosmosDB database | no | output of infrastructure deployment |
| `service_domain_name` | ex `contoso.com` | The name of the domain for which the service will run | no | output of infrastructure deployment |
| `SIS_DATA` | ex `E:\crs-converter\apachesis_setup\SIS_DATA` | Apache SIS setup | no | [apachesis](../../../apachesis_setup/README.md) |
| `ESRI_DATA_PATH` | ex `\crs-conversion-service` | -- | no | -- |

### Configure Maven

Check that maven is installed:
```bash
$ mvn --version
Apache Maven 3.6.0
Maven home: /usr/share/maven
Java version: 1.8.0_212, vendor: AdoptOpenJDK, runtime: /usr/lib/jvm/jdk8u212-b04/jre
...
```

### Build and run the application

After configuring your environment as specified above, you can follow these steps to build and run the application.
1. Navigate to the root of the crs-converter project, crs-converter-service. For building the project using command line, run below command :
    ```bash
    mvn clean install
    ```
    This will build the core project as well as all the underlying projects. If we want  to build projects for specific cloud vendor, we can use mvn --projects command. For example, if we want to build only for Azure, we can use below command :
    ```bash
    mvn --projects crs-converter-core,provider/crs-converter-azure/crs-converter-aks clean install
    ```
2. Run crs-converter service in command line. We need to select which cloud vendor specific crs-converter-service we want to run. For example, if we want to run crs-converter-service for Azure, run the below command :
    ```bash
    # Running Azure :
    java -jar  provider/crs-converter-azure/crs-converter-aks/target/crs-converter-aks-0.28.2-SNAPSHOT-spring-boot.jar --add-opens java.base/java.lang=ALL-UNNAMED --add-opens  java.base/java.lang.reflect=ALL-UNNAMED -javaagent:<<Absolute file path to application-insights-agent jar>> -DAPPINSIGHTS_LOGGING_ENABLED=true
3. The port and path for the service endpoint can be configured in ```application.properties``` in the provider folder as following. If not specified, then  the web container (ex. Tomcat) default is used:
    ```bash
    server.servlet.contextPath=/api/crs/converter/
    server.port=8080
    ```

    ## Open API 3.0 - Swagger
- Swagger UI:  http://localhost:8080/api/crs/converter/swagger (will redirect to  http://localhost:8080/api/crs/converter/swagger-ui/index.html)
- api-docs (JSON) :  http://localhost:8080/api/crs/converter/api-docs
- api-docs (YAML) :  http://localhost:8080/api/crs/converter/api-docs.yaml

All the Swagger and OpenAPI related common properties are managed here [swagger.properties](../../../crs-converter-core/src/main/resources/swagger.properties)

## Debugging

Jet Brains - the authors of Intellij IDEA, have written an [excellent guide](https://www.jetbrains.com/help/idea/debugging-your-first-java-application.html) on how to debug java programs.


## Deploying service to Azure

Service deployments into Azure are standardized to make the process the same for all services. The steps to deploy into
Azure can be [found here](https://dev.azure.com/slb-des-ext-collaboration/open-data-ecosystem/_git/infrastructure-templates?path=%2Fdocs%2Fosdu%2FSERVICE_DEPLOYMENTS.md&_a=preview)


## License
Copyright © Microsoft Corporation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
