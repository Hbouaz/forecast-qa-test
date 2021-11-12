FROM openjdk:11
ADD target/forecast-1.0-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar","my-maven-docker-project.jar"]
EXPOSE 8080

