FROM java:8-jdk-alpine

COPY ./target/bpn-campaing-spring-boot-0.0.1-SNAPSHOT.jar /usr/app/

WORKDIR /usr/app

RUN sh -c 'touch bpn-campaing-spring-boot-0.0.1-SNAPSHOT.jar'

ENTRYPOINT ["java","-jar","bpn-campaing-spring-boot-0.0.1-SNAPSHOT.jar"]

