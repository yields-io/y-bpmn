FROM openjdk:9

ADD target/bpmn-campaing-spring-boot-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-jar","-Dspring.profiles.active=qa","app.jar"]
