# Contexto de la prueba técnica – BHD Document Gateway

Documento breve para quien evalúa la prueba: qué se pedía, qué se entregó y cómo ejecutarla.

---

## Qué es esta prueba

Prueba técnica para Banco BHD. El enunciado pide una aplicación que:

1. **Reciba subidas de documentos** (por API), guarde metadata y delegue el “envío” a un servicio interno de forma **asíncrona** (no es el repositorio final, solo orquesta).
2. **Permita buscar** documentos subidos usando filtros y **ordenación obligatoria** sobre la metadata persistida.

La especificación de la API viene en **openapi.yml**. Los requisitos detallados (Java, opcionales, ejecución) están en **README.MD**.

---

## Qué se entregó

- **Stack:** Java 21, Spring Boot 3.2, WebFlux (reactivo), R2DBC, H2 en memoria.
- **Arquitectura:** Hexagonal (domain, application/ports+services, infrastructure).
- **Endpoints:**
  - **POST** `/api/bhd/mgmt/1/documents/actions/upload` → 202 + `{ "id": "..." }`, procesamiento en background.
  - **GET** `/api/bhd/mgmt/1/documents` → 200 + array. Parámetros opcionales: `id`, `sortBy` (default uploadDate), `sortDirection`, y filtros (uploadDateStart/End, filename, contentType, documentType, status, customerId, channel).
- **Requisitos cubiertos:** Todos los filtros del OpenAPI, ordenación obligatoria, persistencia (H2), tests unitarios e integración, Docker, seguridad (HTTP Basic), resiliencia (Resilience4j), documentación visual (Swagger UI), colección Postman.
- **Ejecución:** Una sola orden para levantar la app, sin servicios externos ni configuración manual (Docker o Maven).

---

## Cómo ejecutar y probar

1. **Arrancar:**  
   `docker compose up`  
   o, con Java 21 y Maven:  
   `./mvnw spring-boot:run`

2. **Acceso:**  
   - Base: `http://localhost:8080`  
   - Swagger UI: http://localhost:8080/swagger-ui.html  
   - Usuario/contraseña HTTP Basic: `bhd` / `bhd-secret`  
   - GET búsqueda: `.../documents` (params opcionales: `id`, `sortBy`, etc.)

3. **Postman:**  
   Importar `postman/BHD-Document-Gateway.postman_collection.json` (incluye upload y search con auth).

4. **Ejecutar tests:**  
   - **Con Docker** (no hace falta Java ni Maven):  
     `./run-tests.sh`  
     o:  
   - **Con Java 21 y Maven:**  
     `./mvnw test`  
   Se ejecutan tests unitarios (servicios) e integración (controller). No requiere que la aplicación esté levantada.

---

## Dónde está cada cosa

- **README.MD** – Enunciado y requisitos de la prueba (sin modificar).
- **openapi.yml** – Especificación de la API.
- **doc/dev-doc.md** – Documentación técnica completa (archivos, librerías, flujos) para desarrolladores.
- **doc/CONTEXT.md** – Este documento (contexto para el evaluador).
