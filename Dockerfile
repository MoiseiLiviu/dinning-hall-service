FROM openjdk:11
COPY target/dinning-hall-service-0.0.1-SNAPSHOT.jar dinning-hall-service.jar
EXPOSE ${port}
ENTRYPOINT exec java -jar dinning-hall-service.jar