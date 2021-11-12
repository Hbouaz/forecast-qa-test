FROM openjdk:11
ADD target/forecast-1.0-SNAPSHOT.jar forecast-1.0-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar","forecast-1.0-SNAPSHOT.jar"]
EXPOSE 8080

