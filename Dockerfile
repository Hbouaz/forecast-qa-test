FROM openjdk:11
ADD target/forecast-1.0-SNAPSHOT-jar-with-dependencies.jar forecast.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar","forecast.jar"]

