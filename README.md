# Yields BPMN

Build jar:
```
mvn clean install
```

Build docker image:

```
docker build -t yields-bpmn .
```


Run:
```
docker run -p 8084:8084 yields-bpmn
```

