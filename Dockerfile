FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy pre-built JAR (run 'mvn clean package -DskipTests' before docker build)
COPY target/*.jar app.jar

EXPOSE 8080 9090

ENTRYPOINT ["java", "-jar", "app.jar"]