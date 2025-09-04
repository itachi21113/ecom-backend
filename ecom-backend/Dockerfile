# Stage 1: Build the application using a Maven image
# We use a specific version of Maven and Java (temurin-17) to ensure consistency.
# 'AS build' gives this stage a name we can refer to later.
FROM maven:3.9-eclipse-temurin-17 AS build

# Set the working directory inside the container. All subsequent commands will run from here.
WORKDIR /app

# Copy the pom.xml file first. Docker is smart and layers its builds.
# If the pom.xml hasn't changed, it won't re-download dependencies, saving a lot of time.
COPY pom.xml .

# Copy the rest of the source code into the container.
COPY . .

# Run the Maven command to build the project and create the executable .jar file.
# '-DskipTests' is used to skip running tests during the build, which is common for deployment builds.
RUN mvn clean package -DskipTests

# Stage 2: Create the final, smaller image for running the application
# We use a JRE (Java Runtime Environment) image, which is much smaller than a full JDK.
# This makes our final container more lightweight and secure.
FROM eclipse-temurin:17-jre-jammy

# Set the working directory for the final image.
WORKDIR /app

# Copy ONLY the built .jar file from our 'build' stage into this final image.
# This is the key to a small and secure image - no source code is included.
COPY --from=build /app/target/*.jar app.jar

# Tell Docker that our application will listen for connections on port 8080.
# This doesn't actually open the port, it's just documentation for the person running the container.
EXPOSE 8080

# This is the final command that will be executed when the container starts.
# It's the equivalent of running 'java -jar app.jar' in your terminal.
ENTRYPOINT ["java", "-jar", "app.jar"]