# Yields BPMN

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
