# Implementation Plan: Product Catalog CRUD (ms-pedidos360-catalog)

**Branch**: `001-product-catalog-crud` | **Date**: 2026-09-30 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/001-product-catalog-crud/spec.md`

## Summary

Implementar el microservicio `ms-pedidos360-catalog` para el catálogo de productos y control de stock de Pedidos360 sobre Java 21 y Spring Boot 3+. La solución adopta una arquitectura en cuatro capas (`controller`, `service`, `repository`, `entity`), persistencia en PostgreSQL mediante Spring Data JPA (apuntando a un contenedor Docker local en `localhost:5434`), validación estricta de stock no negativo en la capa `service` con respuestas `400 Bad Request`, endpoint `PUT` flexible para modificar precio y/o stock de forma conjunta o individual, y documentación OpenAPI 3 / Swagger UI.

## Technical Context

**Language/Version**: Java 21 LTS

**Primary Dependencies**:
- Spring Boot 3.3.x (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`)
- PostgreSQL JDBC Driver (`org.postgresql:postgresql`)
- Springdoc OpenAPI (`org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0`)
- Project Lombok (`org.projectlombok:lombok`)

**Storage**: PostgreSQL 16+ vía Spring Data JPA / Hibernate (Docker local en `localhost:5434`, base `pedidos360_catalog`)

**Testing**: JUnit 5, Mockito, Spring Boot Test (`@WebMvcTest`, `@DataJpaTest`)

**Target Platform**: JVM 21 / Docker container

**Project Type**: REST Web Service (Spring Boot Microservice con Maven)

**Performance Goals**: Latencia p95 < 500 ms en consultas y mutaciones de catálogo

**Constraints**:
- Stock estrictamente no negativo (HTTP 400 ante cualquier intento de asignar valor < 0)
- Mutaciones (crear, editar, eliminar) restringidas a rol `Admin` (HTTP 403)
- Consultas abiertas para consumo del BFF
- Documentación OpenAPI 3 expuesta en runtime

**Scale/Scope**: Microservicio de catálogo transaccional con persistencia relacional completa

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio Constitucional | Estado | Evidencia y Mecanismo de Cumplimiento |
|---|---|---|
| **I. Arquitectura en Capas y Stack Base** | PASS | Java 21, Spring Boot 3+, capas estrictamente desacopladas: `controller` (REST), `service` (negocio/transaccionalidad), `repository` (Spring Data JPA), `entity` (modelo relacional JPA). |
| **II. Persistencia PostgreSQL y Docker Local** | PASS | Spring Data JPA conectado a PostgreSQL 16 corriendo en Docker local mapeado a `localhost:5434` configurado en `application.yml`. |
| **III. Control de Acceso y Segregación de Roles** | PASS | Endpoints de mutación (`POST`, `PUT`, `DELETE`) protegidos para rol `Admin` (`403 Forbidden` si no autorizado). Consultas `GET` abiertas para integración con BFF. |
| **IV. Invariante de Stock No Negativo** | PASS | Validación en `ProductService` previa a persistencia. Lanza `InvalidStockException` capturada por `GlobalExceptionHandler` retornando `400 Bad Request`. |
| **V. Contratos OpenAPI / Swagger** | PASS | `springdoc-openapi` integrado, exponiendo Swagger UI en `/swagger-ui.html` y contrato OpenAPI 3 en `/api-docs`. Contrato documentado en `contracts/openapi.yaml`. |
| **VI. Higiene de Repositorio** | PASS | Estructura estándar Maven, `.gitignore` configurado excluyendo `target/`, carpetas de IDE (`.idea/`, `.vscode/`) y secretos. |

## Project Structure

### Documentation (this feature)

```text
specs/001-product-catalog-crud/
├── spec.md              # Especificación de requisitos funcionales
├── plan.md              # Este plan de implementación técnica
├── research.md          # Investigación y decisiones de arquitectura (Fase 0)
├── data-model.md        # Modelo de datos, DTOs y reglas de validación (Fase 1)
├── quickstart.md        # Guía de validación y escenarios de prueba (Fase 1)
├── contracts/           # Contratos OpenAPI de la API REST (Fase 1)
│   └── openapi.yaml
└── checklists/
    └── requirements.md  # Checklist de calidad de especificación
```

### Source Code (repository root)

```text
pom.xml
.gitignore
src/
├── main/
│   ├── java/com/pedidos360/catalog/
│   │   ├── MsCatalogApplication.java
│   │   ├── controller/
│   │   │   └── ProductController.java
│   │   ├── service/
│   │   │   ├── ProductService.java
│   │   │   └── impl/ProductServiceImpl.java
│   │   ├── repository/
│   │   │   └── ProductRepository.java
│   │   ├── entity/
│   │   │   └── Product.java
│   │   ├── dto/
│   │   │   ├── ProductCreateDto.java
│   │   │   ├── ProductUpdateDto.java
│   │   │   ├── ProductResponseDto.java
│   │   │   └── ErrorResponseDto.java
│   │   ├── exception/
│   │   │   ├── InvalidStockException.java
│   │   │   ├── ProductNotFoundException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   └── config/
│   │       ├── OpenApiConfig.java
│   │       └── SecurityFilterConfig.java
│   └── resources/
│       └── application.yml
└── test/
    └── java/com/pedidos360/catalog/
        ├── controller/ProductControllerTest.java
        └── service/ProductServiceTest.java
```

**Structure Decision**: Se adopta la estructura estándar de microservicio Java/Maven con paquetes temáticos para cada capa de la arquitectura, garantizando total alineación con el Principio I de la constitución.

## Complexity Tracking

*No se registran violaciones ni excepciones a la constitución del proyecto.*

| Elemento | Justificación Técnica |
|---|---|
| DTOs separados (Create, Update, Response) | Desacopla la entidad JPA de los contratos expuestos al exterior y permite que el DTO de actualización maneje campos opcionales sin comprometer la integridad del modelo. |
| Validación en Service + Bean Validation | Asegura defensa en profundidad: Bean Validation filtra datos malformados en transporte y el Service garantiza la regla de negocio independientemente del canal de invocación. |
