<h1 align="center" style="color:#674ea7">Explore-With-Me</h1>

<p align="center">A microservice application that lets users share events and find company to join in activities.</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/java.png" alt="Java" width="60" height="60"/>
  <img src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/spring_boot.png" alt="Spring Boot" width="60" height="60"/>
  <img src="https://github.com/devicons/devicon/blob/master/icons/maven/maven-original.svg" title="Maven" alt="Maven" width="60" height="60"/>
  <img src="https://img.icons8.com/?size=100&id=22813&format=png&color=000000.png" title="Docker" alt="Docker" width="60" height="60"/>
  <img src="https://img.icons8.com/?size=100&id=IoYmHUxgvrFB&format=png&color=000000.png" title="Postman" alt="Postman" width="60" height="60"/>
  <img src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/git.png" alt="Git" width="60" height="60"/>
  <img src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/github.png" alt="GitHub" width="60" height="60"/>
  <img src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/lombok.png" alt="Lombok" width="60" height="60"/>
  <img src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/postgresql.png" alt="PostgreSQL" width="60" height="60"/>
  <img src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/docker.png" alt="Docker" width="60" height="60"/>
  <img src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/rest.png" alt="REST" width="60" height="60"/>
  <img src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/hibernate.png" alt="Hibernate" width="60" height="60"/>
  <img src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/swagger.png" alt="Swagger" width="60" height="60"/>
  <img src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/grpc.png" alt="gRPC" width="60" height="60"/>
<img src="https://raw.githubusercontent.com/marwin1991/profile-technology-icons/refs/heads/main/icons/kafka.png" alt="Kafka" width="60" height="60"/>
</p>




------------------------------------------------------------------------------------------

## Table of Contents

- [Introduction](#introduction)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [API Documentation](#api-documentation)
- [Testing](#testing)


------------------------------------------------------------------------------------------


## Introduction

The application allows users to:

- Create and manage events (exhibitions, parties, concerts, hikes, etc.)
- Submit requests to participate in events
- Browse event compilations
- Comment on events
- Allow administrators to manage categories, events, and users
- Receive event recommendations based on interests and behavior
- Like events, they have participated in

------------------------------------------------------------------------------------------
## Architecture
The system consists of three main services:

### 1. Core Service (Main Business Logic)

| event-service                                                                                         | user-service                                                       | request-service                                                                                                                   | comments-service                                                                      |
|-------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| • Event lifecycle management<br> • Event creation, updates, deletion<br> • Event search and filtering | • User registration and authentication<br>• Profile management<br> | • User participation request processing<br> • Request approval and rejection by event organizerst<br> • Event capacity management | • User comments management<br>• Comment moderation<br>• Comment threading and replies |


### 2. Stats Service (Recommendations)

| collector                                                 | aggregator                                                 | analyzer                                                                                                                                                                            |
|:----------------------------------------------------------|------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| • Collects user actions via gRPC<br> • Publishes to Kafka | • Consumes Kafka topics<br> • Computes event similarities  | • Reads user actions & similarities from Kafka<br> • Stores all received computed data into DB<br> • Serves gRPC for:<br> $~~~~~~~~~~$recommendations<br>  $~~~~~~~~~~$ratings data |


### 3. Infrastructure Module (infra)

| config-serve r                                                                      | discovery-server                                                  | gateway-server                                                       |
|-------------------------------------------------------------------------------------|-------------------------------------------------------------------|----------------------------------------------------------------------|
| • Centralized configuration<br>• Environment-specific settings<br>• Dynamic updates | • Service registration<br>• Load balancing<br>• Health monitoring | • Request routing<br>• Request filtering<br>• Cross-cutting concerns |

### Communication Between Services

All services interact via **REST over HTTP**, using **OpenFeign clients** for internal communication.

- External requests are routed **only through the API Gateway**, which acts as the single entry point.
- Internal service-to-service communication is performed via **Eureka service discovery** and **Feign clients**, using internal endpoints.

Configuration parameters for each service (ports, DB connections, Feign URLs) are defined in:

- local `application.yml` → points to `config-server`
- `application.yml` → contains fallback or local config

### Service Interaction Matrix

| From → To         | Core Service                                      | Stats Service                                                     | Infrastructure                             |
|-------------------|---------------------------------------------------|-------------------------------------------------------------------|--------------------------------------------|
| **Core Service**  | -                                                 | • Sends User Action data<br>• Requests recommendation and ratings | • Fetches config<br>• Service registration |
| **Stats Service** | • Provides analytics<br>• Returns recommendations | -                                                                 | • Fetches config<br>• Service registration |
| **Gateway**       | • Routes requests<br>• Load balancing             | -                                                                 | -                                          |

-----------------------------------------------------------------------------------

## Technology Stack

- **Spring Boot** – Foundation for creating microservices.
- **Spring Cloud**:
    - **Spring Cloud Config** – For externalized centralized configuration (`config-server`).
    - **Spring Cloud Netflix Eureka** – For service registration and discovery (`discovery-server`).
    - **Spring Cloud Gateway** – Acts as an API Gateway for routing and filtering requests.
- **OpenFeign** – Declarative REST clients for inter-service communication.
- **REST** – Primary protocol for API communication between services.
- **gRPC** - Inter-service communication for high-performance data exchange
- **Apache Kafka** - Asynchronous messaging and stream processing
- **Docker & Docker Compose** – For containerization and local orchestration of all services.
- **PostgreSQL** – Main database used by various microservices.
- **Lombok** – Reduces boilerplate code (getters, setters, etc.).
- **SLF4J** – Logging facade used across services for consistent logging.
- **Maven** – Build and dependency management tool.

-----------------------------------------------------------------------------------

## API Documentation

API specifications for the Event Management System are provided in OpenAPI 3.0 format.

### API Specifications
- [Main Service API Specification][main-spec]
- [Stats Service API Specification][stats-spec]

[main-spec]: /ewm-main-service-spec.json
[stats-spec]: /ewm-stats-service-spec.json


-----------------------------------------------------------------------------------

## Testing

### Postman Collections
The project includes comprehensive Postman collections for API testing, located in the `/postman` package:

- [Main Service Collection][main-collection]
- [Feature Tests Collection][feature-collection]

[main-collection]: ./postman/microservices/ewm-main-service.json
[feature-collection]: ./postman/microservices/feature.json


### Running Tests

You can run the API tests in two ways:

#### Option 1: Using Postman GUI
1. Import the collections into Postman:
2. Set up environment variables in Postman:
    - Create a new environment
    - Add required variables:
        - `BASE_URL`: http://localhost:8080
3. Run collections through Postman interface:

#### Option 2: Using Newman (CLI)
1. Install Newman:
   ```bash
   npm install -g newman
   ```
2. Create environment file `environment.json`:
   ```json
   {
     "values": [
       {
         "key": "BASE_URL",
         "value": "http://localhost:8080"
       }
     ]
   }
   ```
3. Run collections via the command line:
   ```bash
   newman run ./postman/microservices/ewm-main-service.json -e environment.json
   newman run ./postman/microservices/feature.json -e environment.json
   ```

-----------------------------------------------------------------------------------
### Tester Recommendations Service:
#### Download:
- [Tester][tester]

[tester]:./tester-0.0.1.jar

#### Run via the command line:
```bash
java -jar tester.jar --tester.execution.mode=COLLECTION --tester.execution.output.file-path=./report.txt
```
#### Testing  mode:
  - COLLECTION   – только collector
  - AGGREGATION  – collector + aggregator
  - ANALYZE      – collector + aggregator + analyzer
tester.execution.mode: ANALYZE

#### Output Settings
- `tester.execution.immediate-logging.enabled=true`
- `tester.execution.output.info-enabled`: **true**
- `tester.execution.output.trace-enabled`: **true**
- `tester.execution.output.print`: **true**
- `tester.execution.output.file`: **true**
- `tester.execution.output.file-path`: `"./execution-report.txt"`



