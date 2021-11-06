FROM adoptopenjdk/openjdk11

MAINTAINER Vladyslav Yemelianov <emelyanov.vladyslav@gmail.com>

ADD ./target/parser-service.jar /app/
USER root
CMD ["java", "-Xmx1000m", "-jar", "/app/parser-service.jar"]

EXPOSE 9055