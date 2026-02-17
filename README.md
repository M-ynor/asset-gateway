# BHD Document Gateway – Documentación para desarrolladores

Guía técnica del proyecto para quien va a leer o modificar el código. Incluye estructura, responsabilidad de cada archivo, librerías y flujos. No se asume conocimiento previo de Java ni Spring Boot.

---

## Cómo ejecutar la aplicación

- **Sin instalar Java ni Maven:** `docker compose up` (puerto 8080).
- **Con Java 21 y Maven instalados:** `./mvnw spring-boot:run`.

**Ejecutar tests:**  
- **Sin Java ni Maven:** `./run-tests.sh` (usa Docker: imagen Maven + Java 21, monta el proyecto y ejecuta `mvn test`).  
- **Con Java 21 y Maven:** `./mvnw test`  
Ejecuta tests unitarios (UploadDocumentService, SearchDocumentsService) e integración (DocumentController). Usa H2 en memoria; no hace falta tener la app corriendo.

Credenciales HTTP Basic: usuario `bhd`, contraseña `bhd-secret`.  
Swagger UI: http://localhost:8080/swagger-ui.html  
Colección Postman: `postman/BHD-Document-Gateway.postman_collection.json`

**Rutas de la API:**  
- POST upload: `/api/bhd/mgmt/1/documents/actions/upload`  
- GET search: `/api/bhd/mgmt/1/documents` con query params opcionales: `id`, `uploadDateStart`, `uploadDateEnd`, `filename`, `contentType`, `documentType`, `status`, `customerId`, `channel`, `sortBy` (por defecto `uploadDate`), `sortDirection` (por defecto `ASC`). Sin barra final antes del `?`.

---

## Estructura del proyecto (carpetas y archivos)

El código Java vive en `src/main/documentgateway/` (paquete `com.bhd.documentgateway`). Maven compila esa carpeta gracias a **build-helper-maven-plugin** en el `pom.xml`. Los recursos estándar están en `src/main/resources/`.

```
bhd-technical-test/
├── pom.xml                          # Dependencias y build (Maven + build-helper para fuentes)
├── mvnw                             # Wrapper para ejecutar Maven
├── .mvn/wrapper/                    # Config del wrapper
├── src/main/
│   ├── documentgateway/             # Código Java (paquete com.bhd.documentgateway)
│   │   ├── BhdDocumentGatewayApplication.java
│   │   ├── domain/
│   │   ├── application/port/
│   │   ├── application/service/
│   │   └── infrastructure/
│   └── resources/
│       ├── application.yml          # Configuración de la app
│       └── schema.sql               # Creación de tablas (H2)
├── src/test/java/com/bhd/documentgateway/   # Tests
├── Dockerfile
├── docker-compose.yml
└── postman/
```

---

## Qué es cada archivo (por paquete)

### Punto de entrada

| Archivo | Para qué sirve |
|---------|-----------------|
| `BhdDocumentGatewayApplication.java` | Clase con `main(String[])`. Arranca Spring Boot y escanea los paquetes del proyecto. `@EnableR2dbcRepositories` indica dónde están los repositorios R2DBC. |

### `domain/` – Núcleo del negocio

Aquí solo hay tipos de datos y enums. No hay dependencias de Spring, base de datos ni HTTP.

| Archivo | Para qué sirve |
|---------|-----------------|
| `enums/DocumentType.java` | Valores: KYC, CONTRACT, FORM, SUPPORTING_DOCUMENT, OTHER. |
| `enums/Channel.java` | Valores: BRANCH, DIGITAL, BACKOFFICE, OTHER. |
| `enums/DocumentStatus.java` | Valores: RECEIVED, SENT, FAILED. |
| `DocumentAsset.java` | Record con los datos de un documento persistido (id, filename, contentType, documentType, channel, customerId, status, url, size, uploadDate, correlationId). Es el modelo que se guarda y se devuelve en búsquedas. |
| `DocumentUploadRequest.java` | Record del body del POST de upload: filename, encodedFile (base64), contentType, documentType, channel, customerId, correlationId. Validación con Jakarta Validation (`@NotBlank`, `@NotNull`). |
| `DocumentUploadResponse.java` | Record de la respuesta 202: solo `id`. |
| `SearchCriteria.java` | Record con los filtros y orden de la búsqueda: id, uploadDateStart/End, filename, contentType, documentType, status, customerId, channel, sortBy (opcional, por defecto uploadDate), sortDirection. |

### `application/port/` – Contratos (interfaces)

Definen qué debe poder hacer la infraestructura, sin decir cómo. Así el dominio y los servicios no dependen de REST, H2 ni del publisher real.

| Archivo | Para qué sirve |
|---------|-----------------|
| `DocumentRepository.java` | Guardar documento, buscar por id, actualizar estado y URL, buscar con filtros y orden. Devuelve `Mono`/`Flux` (tipos reactivos). |
| `DocumentPublisher.java` | “Publicar” un documento (en este proyecto es un stub). Devuelve `Mono<PublishResult>`. |
| `PublishResult.java` | Record: éxito o no y URL opcional. Se usa para actualizar estado a SENT o FAILED. |
| `UploadOrchestrator.java` | Orquestar en segundo plano: dado un id, cargar documento, llamar al publisher y actualizar estado. Devuelve `Mono<Void>`. |

### `application/service/` – Casos de uso

Contienen la lógica de negocio y usan solo los puertos (interfaces). Spring los instancia e inyecta los adaptadores.

| Archivo | Para qué sirve |
|---------|-----------------|
| `UploadDocumentService.java` | Recibe `DocumentUploadRequest`, genera id, calcula tamaño del base64, crea `DocumentAsset` en estado RECEIVED, lo guarda, dispara la orquestación en background y devuelve `DocumentUploadResponse(id)`. |
| `SearchDocumentsService.java` | Recibe `SearchCriteria` y delega en `DocumentRepository.search(criteria)`. Devuelve `Flux<DocumentAsset>`. |

### `infrastructure/persistence/` – Base de datos (R2DBC + H2)

| Archivo | Para qué sirve |
|---------|-----------------|
| `DocumentEntity.java` | Clase que mapea la tabla `document_asset` (columnas en snake_case). Usada por Spring Data R2DBC. |
| `R2dbcDocumentRepository.java` | Interfaz que extiende `ReactiveCrudRepository<DocumentEntity, String>`. Spring genera la implementación; usamos `findById`. |
| `DocumentRepositoryAdapter.java` | Implementa el puerto `DocumentRepository`: traduce entre `DocumentAsset` y `DocumentEntity`, usa el repo y `R2dbcEntityTemplate` para `save`, `updateStatus` y `search` con filtros dinámicos y orden. |

### `infrastructure/publisher/` – “Publicador” y orquestación

| Archivo | Para qué sirve |
|---------|-----------------|
| `StubDocumentPublisher.java` | Implementa `DocumentPublisher`. Simula éxito y devuelve una URL ficticia. La llamada se envuelve con Resilience4j (retry y circuit breaker). |
| `UploadOrchestratorAdapter.java` | Implementa `UploadOrchestrator`: busca el documento por id, llama a `DocumentPublisher.publish`, luego actualiza estado (y URL) en el repositorio. |

### `infrastructure/web/` – API REST

| Archivo | Para qué sirve |
|---------|-----------------|
| `DocumentController.java` | Controlador WebFlux. **POST** `/api/bhd/mgmt/1/documents/actions/upload`: valida el body, responde 202 con `id`. **GET** `/api/bhd/mgmt/1/documents`: params opcionales `id`, fechas, filename, contentType, documentType, status, customerId, channel; `sortBy` opcional (default `uploadDate`), `sortDirection` (default `ASC`). Construye `SearchCriteria` y devuelve 200 con el array. |

### `infrastructure/config/` – Configuración

| Archivo | Para qué sirve |
|---------|-----------------|
| `SecurityConfig.java` | Configura Spring Security: Swagger/OpenAPI públicos; `/api/bhd/mgmt/1/documents/**` exige autenticación. HTTP Basic. Define un usuario en memoria (`bhd` / `bhd-secret`). |
| `R2dbcInitializer.java` | Al arrancar, ejecuta `schema.sql` contra la base H2 para crear la tabla `document_asset`. |

### Tests

| Archivo | Para qué sirve |
|---------|-----------------|
| `UploadDocumentServiceTest.java` | Unit: mock de `DocumentRepository` y `UploadOrchestrator`; comprueba que al subir se guarda y se devuelve un id. |
| `SearchDocumentsServiceTest.java` | Unit: mock de `DocumentRepository`; comprueba que la búsqueda delega en el repo. |
| `DocumentControllerIntegrationTest.java` | Integración con `WebTestClient`: en `@BeforeEach` construye el cliente con `WebTestClient.bindToApplicationContext(context).apply(springSecurity()).configureClient().build()`; tests: upload con `mockUser` (202 + id), upload sin auth (401), search con sortBy (200), search sin sortBy (400). |

---

## Librerías instaladas (`pom.xml`) y para qué se usan

| Dependencia | Uso en el proyecto |
|-------------|--------------------|
| **spring-boot-starter-webflux** | Servidor HTTP reactivo (no bloqueante). Controladores que devuelven `Mono`/`Flux`. |
| **spring-boot-starter-data-r2dbc** | Acceso a base de datos de forma reactiva (R2DBC). Repositorios y `R2dbcEntityTemplate`. |
| **spring-boot-starter-validation** | Validación del body (ej. `@NotBlank`, `@NotNull`) en el controller. |
| **spring-boot-starter-security** | Autenticación/autorización. Aquí: HTTP Basic y rutas protegidas. |
| **r2dbc-h2** | Driver R2DBC para H2 (base en memoria o archivo). |
| **h2** | Motor de base de datos embebida (runtime). |
| **resilience4j-spring-boot3** | Retry y circuit breaker en la llamada al publisher (config en `application.yml`). |
| **resilience4j-reactor** | Operadores Reactor para Resilience4j (`RetryOperator`, `CircuitBreakerOperator`) usados en `StubDocumentPublisher`. |
| **springdoc-openapi-starter-webflux-ui** | Genera OpenAPI y Swagger UI para documentar y probar la API. |
| **spring-boot-starter-test** | JUnit 5, Mockito, `WebTestClient` para tests. |
| **reactor-test** | `StepVerifier` para probar `Mono`/`Flux` en tests. |
| **spring-security-test** | `mockUser()`, `SecurityMockServerConfigurers` para tests con seguridad. |

---

## Configuración relevante (`application.yml`)

- **spring.r2dbc:** URL de H2 en memoria, usuario y contraseña.
- **spring.codec.max-in-memory-size:** Límite para bodies grandes (p. ej. base64).
- **spring.threads.virtual.enabled:** Habilita virtual threads (Java 21).
- **springdoc:** Rutas de API docs y Swagger UI.
- **resilience4j.retry / circuitbreaker:** Instancia `documentPublisher`: reintentos y umbral de fallos.

---

## Flujo de un upload (resumido)

1. Cliente hace **POST** a `/api/bhd/mgmt/1/documents/actions/upload` con JSON (filename, encodedFile, contentType, documentType, channel, etc.).
2. **DocumentController** valida el body y llama a **UploadDocumentService.upload**.
3. El servicio genera un id, crea un **DocumentAsset** en estado RECEIVED y lo guarda con **DocumentRepository.save**.
4. El servicio programa en background **UploadOrchestrator.orchestrateAsync(id)** (no espera a que termine).
5. El controller responde **202** con `{ "id": "..." }`.
6. En background, el orquestador carga el documento, llama a **DocumentPublisher** (stub con Resilience4j), y actualiza el documento a SENT (o FAILED) con **DocumentRepository.updateStatus**.

---

## Flujo de búsqueda (resumido)

1. Cliente hace **GET** a `/api/bhd/mgmt/1/documents` con query params opcionales (p. ej. `id`, `sortBy=uploadDate`, `sortDirection=ASC`, filtros por fecha, filename, etc.). `sortBy` es opcional y por defecto `uploadDate`.
2. **DocumentController** valida `sortBy` (obligatorio) y `sortDirection`, parsea fechas y construye **SearchCriteria**.
3. Llama a **SearchDocumentsService.search(criteria)**.
4. El servicio llama a **DocumentRepository.search(criteria)**.
5. **DocumentRepositoryAdapter** arma la query con filtros opcionales y orden, usa **R2dbcEntityTemplate** y devuelve **Flux&lt;DocumentAsset&gt;**.
6. El controller responde **200** con el array JSON.

---

## Arquitectura en capas (hexagonal)

- **Domain:** entidades, value objects y enums. Sin dependencias externas.
- **Application (ports):** interfaces que definen “qué necesita el negocio” (guardar, buscar, publicar, orquestar).
- **Application (services):** lógica de negocio que usa solo esos puertos.
- **Infrastructure:** implementaciones concretas: REST (WebFlux), base de datos (R2DBC + H2), publisher (stub), seguridad (Spring Security). Traducen entre el mundo exterior y el dominio/application.

Así se puede cambiar la base de datos, el transporte o el publisher sin tocar la lógica de negocio.
