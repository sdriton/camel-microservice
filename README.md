## Introduction
This project contains a few examples how to use Apache Camel to resolve fictious use cases. The examples can be run using Maven. When using the Maven command, Maven will attempt to download the required dependencies from a central repository to your local repository.

The sample node applications are required to run TCP Socket routes.

## Getting Started

1. Clone the project:

    `$ git clone https://github.com/sdriton/camel-microservice.git`

2. Install the dependencies:

    `$ mvn install`

3. Add this environment variable in your terminal shell 
    
    `export CAMEL_KEYSTORE_PASSWORD=Camel123!`

4. Copy sample CSV file to /tmp directory (or the preferred directory)

    `$ cp ./src/main/resources/static/employees-basic.csv /tmp`
    
5. Execute:

    `$ mvn clean compile exec:java`

## Run the application from the executable spring boot jar
1. Package

```
$ mvn clean compile package
```

2. Run

Using default parameters:

``````    
$ java -Xms1g -Xmx2g -XX:MaxMetaspaceSize=2g -XX:+UseG1GC -XX:MaxGCPauseMillis=500 -jar ./target/camel-microservice-0.0.1.jar
``````
Using custom parameters:

``````    
$ java -Xms1g -Xmx2g -XX:MaxMetaspaceSize=2g -XX:+UseG1GC -XX:MaxGCPauseMillis=500 -Dserver.port=8090 -Dspring.profiles.active=dev -jar ./target/camel-microservice-0.0.1.jar
``````

Parameters:
```
1. server.port=8091 - The port of the HTTP server
2. spring.profiles.active=dev - The active profile for which the yaml file will be used. In this case application-dev.yaml will be used.
``` 

## Certificates
To create a self signed cerificate and import it into the keystore follow these steps:
### Create a self signed certificate
Using KeyChain Access create certificate and export it as PKCS12 - [Here are the steps](https://support.apple.com/en-ca/guide/keychain-access/kyca8916/mac). 

Other  tools such as OpenSSL or Java keytool can be used to create the certificate.

### Import the certificate in Java Truststore

1. Import the certificate:

    ```
    keytool -importkeystore -srckeystore apache-camel-poc.p12 -srcstoretype PKCS12 -destkeystore camel-keystore.jks  -deststoretype JKS -srcstorepass <password> -deststorepass <password> -destkeypass <password> -noprompt
    ```
2. List the contents of the truststore to check if the certificate has been imported:
    ```
    keytool -list -v -keystore camel-keystore.jks
    ```
3. Configure in SSLContextParamsConfiguration class the SSL Context.

## Dockerize the application

### Using the <strong>docker-compose.yaml</strong> file in the root directory

```
$ docker-compose up
```

### Using the docker build and docker run

Build the docker image
```
$ docker build -f .docker/Dockerfile -t driton/camel-microservice . --build-arg SERVER_PORT=8090
```
Create a bridge network

```
$ docker network create -d bridge camel_microservice_internal_network
```

Run the container on port 8090

```
$ docker run --env CAMEL_KEYSTORE_PASSWORD=Camel123! \
    -p 127.0.0.1:8090:8090/tcp \
    --name driton-camel-microservice \
    -v tempVolume:/tmp \
    --network=camel_microservice_internal_network \ 
    --rm -it driton/camel-microservice:latest
```

## Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.7/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.7.7/maven-plugin/reference/html/#build-image)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/2.7.7/reference/htmlsingle/#actuator)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/2.7.7/reference/htmlsingle/#using.devtools)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.7.7/reference/htmlsingle/#web)

## Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Using Apache Camel with Spring Boot](https://camel.apache.org/camel-spring-boot/latest/spring-boot.html)
* [More examples from Apache Camel](https://github.com/apache/camel-examples)

