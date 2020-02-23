# Mars Weather REST Server

The Mars Weather REST Server implements a simple REST API providing access to temperature measurements in the surface of planet Mars. The server is shipped as a standalone Java application based on the [Jersey framework][1] ([JAX-RS / JSR-370 specification][2]).

The server implementation relies on the [NASA's InSight API][3], version 1.0. Therefore, a few parameters relative to the underlining API can be found at the server configuration options. The server also counts on a local cache to prevent the NASA API from receiving unnecessary accesses. In the configuration section you will find a parameter for adjusting its invalidation period.

This software is distributed as a pre-built executable JAR file as well as Java source code, allowing users to configure, compile and install the application. The pre-build version was set up to fit the majority of usage scenarios. If the default configuration meets your needs (check the configuration section below), you can [download the executable JAR file][5] and skip the building section in this documentation. 

## Getting Started

The project's dependency management and build process is controlled by [Maven][4]. You will need a properly configured Maven installation in your workstation in order to build the project. Once you have the project built, all you need to run the server is a regular Java Runtime Environment, as well as, the appropriated permission to bind the REST server to a HTTP port on the server machine.

### Prerequisites

The following software must be properly installed and configured prior to building the project: 

- [Apache Maven 3.6.3](http://maven.apache.org/download.cgi)
- [Java Development Kit (JDK) 1.8.0_241](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html)

And to run the server:

- [Java Runtime Enviroment (JRE) 1.8.0_241](https://www.oracle.com/java/technologies/javase-jre8-downloads.html)

### Building

Command line build of the project:

1. Clone this git repository

    ```
    git clone https://github.com/avimeney/marsweather.git
    ```
1. Change to your local repository directory:

    ```
    cd marsweather
    ```
1. Build and package the project:

    ```
    mvn clean compile assembly:single
    ```

The build process will generate a `target` directory where you should find a ``marsweather-1.0.0.jar`` file. The latter is a JRE runnable JAR file containing all the dependencies necessary to run the server.

### Configuration

Check bellow the default server configurations. If they meet your requirements, no additional setup is needed and you can skip to the "Running the server" section.

- Once the server has been started, the REST service will be available at:

    ```
    http://localhost:8080/marsweather/
    ```
- The authorization token is set to ``PREODAY_TEST_TOKEN`` (check the authorization section below)
- The cache invalidation period is set to ``4`` hours.
- The server will look up for the NASA InSight API at:

    ```
    https://api.nasa.gov/insight_weather/
    ```
- The server will use the following key to connect to the NASA API: ``DEMO_KEY``

If you need to change some of these parameters, you can do it by editing the `application.properties` file. Once you downloaded the project's source code, you can find the configuration file at the following directory:

```
/src/main/resources/application.properties
```

In the configuration file you will find the default values for each property commented out. Uncomment the desired lines in the configuration file and adjust its values according to your needs.

## Running the server

If you built the software from the source, locate the generated ``marsweather-1.0.0.jar`` file at the ``target`` directory. If you chose downloading the [binary version][5], look at your downloads folder. Start the server from the command line using:

```
java -jar marsweather-1.0.0.jar
```

## Consuming the API

The API provided by the Mars Weather Service consists in a single method responsible for providing all the temperature measurements available at the moment:

```
/api/v1/weather/list
```

The method above returns an array containing one JSON object for each martian day (called Sol) available. These objects have the following structure:

    {
      "type": "object",
      "properties": {
        "id": {
          "type": "integer",
          "description": "The Sol identification number",
        },
        "avg": {
          "type": "number",
          "description": "Average temperature on this Sol in degree Celsius",
        },
        "min": {
          "type": "number",
          "description": "Minimum temperature measured on this Sol in degree Celsius",
        },
        "max": {
          "type": "number",
          "description": "Maximum temperature measured on this Sol in degree Celsius",
        }
      }
    } 

Example of a response array:

    [
     {"id": 437,
      "avg": -37,
      "min": -104,
      "max": -10},
      ...
     {"id": 431,
      "avg": -42,
      "min": -116,
      "max": -23}
    ]


### Authentication

The REST service implementation contains a primitive authentication skeleton that requires a ``Authorization`` header in the API requests.
The authentication scheme is token based, thus requires the _Bearer_ scheme to be chosen. The only token value accepted
by this simple implementation is that one set in the application configuration. By default, it is ``PREODAY_TEST_TOKEN``. Make sure your client implementation defines the request header properly:

```
Authentication: Bearer PREODAY_TEST_TOKEN
```

### Example Client

Check out the [Mars Weather Web Client][6] project for an example of a client application that uses this REST API.

## Support

You need any help using this application please contact me at [avimeney@gmail.com](mailto:avimeney@gmail.com).

[1]: https://eclipse-ee4j.github.io/jersey/
[2]: https://jcp.org/aboutJava/communityprocess/final/jsr370/index.html
[3]: https://api.nasa.gov/
[4]: http://maven.apache.org/
[5]: https://github.com/avimeney/marsweather/packages/135476
[6]: https://github.com/avimeney/marsweathercli
