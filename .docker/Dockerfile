########Maven build stage########
FROM maven:3.8.7-openjdk-18-slim as maven_build
ARG ENV_PROFILE=dev
ARG SERVER_PORT=8090

WORKDIR /app

# copy pom
COPY pom.xml .
# copy source
COPY src ./src

# resolve maven dependencies
RUN mvn clean package -Dmaven.test.skip -Dmaven.main.skip -Dspring-boot.repackage.skip && rm -r target/

# build the app (no dependency download here)
RUN mvn clean package  -Dmaven.test.skip
# RUN --mount=type=cache,target=/root/.m2  mvn clean package -Dmaven.test.skip

# split the built app into multiple layers to improve layer rebuild
RUN mkdir -p target/docker-packaging && cd target/docker-packaging && jar -xf ../camel-microservice-0.0.1.jar

########JDK run stage########
FROM openjdk:21-jdk-slim
WORKDIR /app
ARG ENV_PROFILE
ARG SERVER_PORT
ENV server.port=${SERVER_PORT}
ENV spring.profiles.active=${ENV_PROFILE}
# copy spring boot executable jar to work directory
ARG DOCKER_PACKAGING_DIR=/app/target/docker-packaging
COPY --from=maven_build /app/target/camel-microservice-0.0.1.jar app.jar
COPY --from=maven_build ${DOCKER_PACKAGING_DIR}/BOOT-INF/classes/static /app/static
COPY --from=maven_build ${DOCKER_PACKAGING_DIR}/BOOT-INF/classes/keystore /app/keystore
RUN cp -r /app/static/* /tmp && \
    rm /bin/sh && ln -s /bin/bash /bin/sh && \
    echo "America/Montreal" > /etc/timezone && dpkg-reconfigure -f noninteractive tzdata && \
    printf "\\nalias ll=\"ls -la\"\\n" >> ~/.bashrc


VOLUME /tmp
EXPOSE ${SERVER_PORT}
EXPOSE 3000

# run the app
# CMD java -Xms1024m -Xmx2048m -Dserver.port=${SERVER_PORT} -Dspring.profiles.active=${ENV_PROFILE} -jar app.jar
CMD java -Xms1024m -Xmx2048m -jar app.jar

