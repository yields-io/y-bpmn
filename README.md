# Yields BPMN

### Adjust configuration and set propper values at files:

- [`BASE_URL`](https://github.com/yields-io/y-camunda/blob/master/src/main/java/io/yields/bpm/client_name/chiron/ChironApi.java#L29)

- [`client_name`](https://github.com/yields-io/y-camunda/blob/master/src/main/java/org/camunda/bpm/extension/keycloak/KeycloakGroupQuery.java#L34)

- [`section: plugin.identity.keycloak`](https://github.com/yields-io/y-camunda/blob/master/src/main/resources/application.yaml#L25)

By default there are hardcoded group permission set for `validation` to `France` [here](https://github.com/yields-io/y-camunda/blob/master/src/main/java/org/camunda/bpm/extension/keycloak/KeycloakGroupQuery.java#L38), and `development` user to `Belgium` [here](https://github.com/yields-io/y-camunda/blob/master/src/main/java/org/camunda/bpm/extension/keycloak/KeycloakGroupQuery.java#L42).

After setting code up please proceed to building the `jar`:


### Build the `jar`:
```
docker run --rm \
    -v ${HOME}/.m2:/root/.m2 \
    -v ${PWD}:/workspace/source \
    -w /workspace/source \
    --entrypoint mvn maven:3.6.3-slim clean install
```

### Build the `docker` image:
```
docker build --rm -t build.yields.io/y-camunda .
```

### Run the container:
```
docker run --rm -d --name camunda -p 80:8084 build.yields.io/y-camunda
```

## Camunda authentication setup
### Grant Basic Permissions:
Login to Camunda portal using admin user: `yields` and setup access for additional user:

#### 1. Access for user to view Tasklist:

`Admin / Authorizations / Application`,
and create new authorization:

| Type | User / Group | Permissions | Resource ID |
| --- | --- | --- | --- |
| ALLOW | test | ALL | tasklist |


#### 2. Grant Permission to Start Processes from Tasklist:

`Admin / Authorizations / Process Definition`,
and create new authorization:

| Type | User/ Group | Permissions | Resource ID |
| --- | --- | --- | --- |
| ALLOW | test | READ, CREATE | * |

`Admin / Authorizations / Process Instance`, and create new authorization:

| Type | User/ Group | Permissions | Resource ID |
| --- | --- | --- | --- |
| ALLOW | test | CREATE | * |


#### 3. Restrict member/group to see only phrase-related entries:

Like above but at `Resource ID` use filter feature:

| Type | User/ Group | Permissions | Resource ID |
| --- | --- | --- | --- |
| ALLOW | francemember | CREATE | `*fran*` |


> ### Make sure that the user you want to add rights have the correct Keycloak configuration (proper group assigned).


For more information about configuration, resourcess and permissions please follow [Camunda official documentation: authorization-service](https://docs.camunda.org/manual/7.14/user-guide/process-engine/authorization-service/#permissions).


## TODO

1. the `keycloak` config is hardcoded in the resources and needs to be made variable.
2. the repo contains certificates in the resources which is a big no no, unsuitable for production.
