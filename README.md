# CRS conversion service

This document presents useful information about common development activities for the CRS service.

## This repository contains 
1. The Java implementation of the Converter (aka crs-conversion-serice). 
1. To open the Java project, open ```pom.xml```.
1. The core module is in the crs-converter-core. The Java code is located in its ```src``` sub-folder and tests are located in ```src/test/java/org/opengroup/osdu/crs/...```
1. The provider folder contains different provider implementations.
1. Python integration and health tests in the ```testing``` folder. 
See also the test's [README.md](testing/README.md)

## Prerequisites
1. The project builds with [Maven](https://maven.apache.org/) **3.9.16+**.
1. Community modules (`crs-converter-core`, `crs-converter-core-plus`) require **JDK 25** (Epic 82).
1. The project requires the [Lombok](https://projectlombok.org/) plug-in installed for your IDE (**1.18.40+** for JDK 25).

### Make the below change for a successful local build. Do not commit this change.
- **Comment the following in "crs-converter-core/pom.xml"**
```xml
<!--    <localRepositoryPath>${basedir}/../../.m2/repository</localRepositoryPath>-->
```

## Running CRS Conversion Service locally
#### Build and run CRS Conversion Service locally using bash
- Set the required environment variables listed in [crs-converter-core-plus/README.md](crs-converter-core-plus/README.md#installation)
- Navigate to the Converter Service's root folder ```crs-conversion-service``` 
- Build core and run unit tests on command line:
    - In order to run unit tests, set the SIS_DATA variable before building
```bash
export SIS_DATA=${SRC_ROOT_DIR}/apachesis_setup/SIS_DATA
mvn clean install 
# To run without tests add -DskipTests=true
```
- Run application with command
```bash
java -jar crs-converter-core-plus/target/crs-converter-core-plus-*-spring-boot.jar
```

The core coverage job requires 80% JaCoCo instruction coverage. With `SIS_DATA`
set as above, run `mvn -f crs-converter-core/pom.xml clean test jacoco:report`
and check [the coverage report](crs-converter-core/target/site/jacoco/index.html).

### Debug CRS Conversion Service locally
- Perform build as instructed in the Build and run Converter Service locally section
- Start the application:
```sh
java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=n -jar crs-converter-core-plus/target/crs-converter-core-plus-*-spring-boot.jar
```

- Set up remote debugging configuration in Intellij and start it
- Environment variables: set the ones listed in [crs-converter-core-plus/README.md](crs-converter-core-plus/README.md#installation)

- Execute **Debug** for configured Application

In the Postman Settings / General, turn SSL certificate validation off when running locally.
Similarly, when not using Postman but client code, set the configuration  ```verify_ssl``` false (see [instructions](https://github.com/swagger-api/swagger-codegen/issues/7778))

## Open API 3.0 - Swagger
* Swagger UI : https://host/context-path/swagger (will redirect to https://host/context-path/swagger-ui/index.html)
* api-docs [All Versions] (JSON) : https://host/context-path/api-docs
* api-docs [All versions] (YAML) :https://host/context-path/api-docs.yaml
* api-docs [Version V2] (JSON) : https://host/context-path/api-docs/v2
* api-docs [Version V3] (JSON) : https://host/context-path/api-docs/v3   
All the Swagger and OpenAPI related common properties are managed here [swagger.properties](https://community.opengroup.org/osdu/platform/system/reference/crs-conversion-service/-/blob/16ad7585a2e1fa9cdf7dea613243f47d31aeac2f/crs-converter-core/src/main/resources/swagger.properties)
Headers for Postman:

| Key | Value |
|----------|----------|
| Authorization | Bearer `<token>` |
| data-partition-id | $MY_TENANT (see [testing\README.md](testing/README.md)) |
#### Server Url(full path vs relative path) configuration
- `api.server.fullUrl.enabled=true` It will generate full server url in the OpenAPI swagger
- `api.server.fullUrl.enabled=false` It will generate only the contextPath only
- default value is false
[Reference]:(https://springdoc.org/faq.html#_how_is_server_url_generated)

### Build and run the Docker container locally
1. Run the `maven run` command to have the .jar file generated.
1. Execute the following command to build the container image:
```docker build -f crs-converter-core-plus/build/Dockerfile -t crs-converter .```
1. Execute the following command to build the container image:
```docker run -t --rm -p 8080:8080 crs-converter```
1. Use Postman or curl to try out the endpoints
