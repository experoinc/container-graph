# Container Graph
Container Graph is demo Dropwizard service that runs against [JanusGraph](janusgraph.org).

# Building
This demo application requires Java 8 and Maven. To build run:
`mvn clean install`
This will produce the following uberjar `target/container-graph-0.1.0.jar`.

# Running
Before running, update the `config.yml` `contactPoints` property to point to your JanusGraph cluster.
```
tinkerPop:
  contactPoints:
    - 127.0.0.1
```
Start the application: `java -jar container-graph-0.1.0.jar server config.yml`.

# Endpoints
The application provides the following endpoints:
* `/containers`
* `/containers/{id}`
* `/containers/{id}/connectedTo?hops={hopCount}`
* `/containers/{id}/dependsOn`
* `/containers/{id}/dependencyOf`

As an example, you can retrieve a list of containers from the container graph:
```
curl http://localhost:8080/containers
[{"application":["JanusGraph"]},{"application":["JanusGraph"]},{"application":["JanusGraph"]},{"application":["API"],"ip_address":["10.1.0.1"]},{"application":["Elasticsearch"]},{"application":["API"],"ip_address":["10.1.0.2"]},{"application":["API"],"ip_address":["10.1.0.3"]}]
```