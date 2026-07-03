# Java Sample Projects

This repository contains three Maven-based hello-world samples:

- `springboot-microservice`: Spring Boot HTTP service deployable with Docker.
- `micronaut-lambda`: Micronaut Lambda function plus one Java Lambda layer.
- `core-java-lambda`: core Java Lambda function plus one Java Lambda layer.

## Prerequisites

- JDK 21.
- Docker for the Spring Boot container build.
- AWS SAM CLI only if you want to validate or deploy the Lambda templates.

The checked-in Maven wrapper downloads Apache Maven 3.9.16 into `.mvn/wrapper/dists`.

Micronaut is pinned to the latest 4.x platform line (`4.10.16`) because the 5.x line targets Java 25 class files. These Lambda samples intentionally target AWS Lambda `java21`.

## Build And Test

```sh
./mvnw test
./mvnw package
```

## Local CI

The repo also exposes npm-compatible local CI wrappers for teams that use `npm run` as the command surface:

```sh
npm run ci:local:all
npm run ci:local:resume
```

The local CI runner requires JDK 21 and `actionlint`. It runs the platform workflow ref guard, GitHub Actions lint, Maven clean tests, and Maven packaging. SAM template validation runs when the `sam` CLI is installed.

## Spring Boot Microservice

```sh
./mvnw -pl springboot-microservice package
docker build -t java-sample-springboot ./springboot-microservice
docker run --rm -p 8080:8080 java-sample-springboot
curl http://localhost:8080/hello
curl http://localhost:8080/actuator/health
```

Expected hello response:

```json
{"message":"Hello from Spring Boot microservice"}
```

## Micronaut Lambda

Build artifacts:

- Function ZIP: `micronaut-lambda/function/target/micronaut-lambda-function-0.1.0-SNAPSHOT-lambda.zip`
- Layer ZIP: `micronaut-lambda/layer/target/micronaut-lambda-layer-0.1.0-SNAPSHOT-layer.zip`
- SAM template: `micronaut-lambda/template.yaml`

```sh
./mvnw -pl micronaut-lambda/function -am package
sam validate --template micronaut-lambda/template.yaml
```

## Core Java Lambda

Build artifacts:

- Function ZIP: `core-java-lambda/function/target/core-java-lambda-function-0.1.0-SNAPSHOT-lambda.zip`
- Layer ZIP: `core-java-lambda/layer/target/core-java-lambda-layer-0.1.0-SNAPSHOT-layer.zip`
- SAM template: `core-java-lambda/template.yaml`

```sh
./mvnw -pl core-java-lambda/function -am package
sam validate --template core-java-lambda/template.yaml
```
