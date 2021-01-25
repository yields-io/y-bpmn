# Yields BPMN

Adjust configuration and set propper values at files:

https://github.com/yields-io/y-camunda/blob/master/src/main/java/io/yields/bpm/client_name/chiron/ChironApi.java#L29
https://github.com/yields-io/y-camunda/blob/master/src/main/java/org/camunda/bpm/extension/keycloak/KeycloakGroupQuery.java#L34
https://github.com/yields-io/y-camunda/blob/master/src/main/resources/application.yaml#L25

before next step:

Build the `jar`:
```
docker run --rm \
    -v ${HOME}/.m2:/root/.m2 \
    -v ${PWD}:/workspace/source \
    -w /workspace/source \
    --entrypoint mvn maven:3.6.3-slim clean install
```

Build the `docker` image:
```
docker build --rm -t build.yields.io/y-camunda .
```

Run the container:
```
docker run --rm -d --name camunda -p 8084:8084 build.yields.io/y-camunda
```

## TODO

1. the `keycloak` config is hardcoded in the resources and needs to be made variable.
2. the repo contains certificates in the resources which is a big no no, unsuitable for production.
