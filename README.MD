# Business Case

There is a business need at Banco BHD to provide an internal asset/document upload capability for multiple internal systems, which implies:

    1. Managing the document upload process, including receiving the file and delegating its upload to an internal document publisher or storage service.

    2. Allowing internal users or systems to search uploaded documents based on persisted metadata.

## Considerations

- Refer to the attached openapi.yml for the API specification.
- The upload process must be handled asynchronously.
- This service is not the final repository of the documents; it acts as a proxy that stores metadata and orchestrates the upload process.

## Requirements

### JAVA
- Java >= 21.
- Spring Boot.
- HTTP/REST.
- Unit & integration tests.
- Fully functional (compiles, starts locally).
- Hexagonal design (You can go modular or package based, no worries on that). 
- Implement whichever defined filter you see fit, no need to implement them all but sorting is a must.
- Persistence.

#### Optional
- Implement all filters.
- Dockerized.
- Reactive (WebFlux). 
- Virtual Threads.
- Resilient.
- Postman collection.
- Security.

### C#
- .NET 8.
- ASP.NET Core Web API.
- HTTP/REST.
- Unit & integration tests.
- Fully functional (builds and runs locally).
- Hexagonal architecture (modular or folder/package-based).
- Implement any subset of the defined filters; sorting is mandatory.
- Persistence.

#### Optional
- Implement all available filters.
- Dockerized solution.
- Reactive/async streaming where applicable.
- Background processing (Hosted Services / Worker Service) for async upload orchestration.
- Resilience mechanisms.
- Postman collection.
- Security.
- Implement full pagination configuration across the application.

## Application Execution Requirement

The application must be fully self-contained and ready to run locally without requiring any additional manual setup. All dependencies, configurations, and infrastructure components required for its correct operation must be included and properly configured within the project.

The evaluator should only need to execute a single run command to start the application and proceed with testing its functionality. No external services, prior environment configuration, or additional installation steps should be required beyond the standard runtime for the chosen technology stack.