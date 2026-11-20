# Builds and runs the Spring Boot API on Render (Render has no built-in Java).
# Render settings: Root Directory = server, Runtime = Docker.

FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw -B package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Render's free plan has 512 MB of memory; keep Java inside it.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
# Render tells the app which port to use through $PORT.
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
