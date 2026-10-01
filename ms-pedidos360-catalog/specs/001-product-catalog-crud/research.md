# Research & Architecture Decisions: Product Catalog CRUD

**Feature**: `001-product-catalog-crud` | **Microservice**: `ms-pedidos360-catalog`

## 1. Stack Tecnológico y Arquitectura en Capas

### Decisión
- **Lenguaje**: Java 21 LTS.
- **Framework**: Spring Boot 3.3.x.
- **Arquitectura**: Arquitectura clásica en capas con estricta separación de responsabilidades:
  - `controller`: Exposición REST, validación de DTOs (`@Valid`), negociación de respuestas HTTP y Swagger annotations.
  - `service`: Lógica de negocio, reglas de dominio (invariante de stock), orquestación y transaccionalidad (`@Transactional`).
  - `repository`: Acceso a datos con `ProductRepository` extendiendo `JpaRepository<Product, Long>`.
  - `entity`: Modelado ORM JPA con anotaciones de hibernate/jakarta.persistence.

### Justificación
- Alineado 100% con el Principio I de la constitución.
- Java 21 ofrece soporte a largo plazo y rendimiento optimizado en Spring Boot 3.
- Separación desacoplada que facilita pruebas unitarias aisladas con Mockito.

### Alternativas Consideradas
- Arquitectura Hexagonal / Clean Architecture completa: Descartada por sobre-ingeniería innecesaria para un CRUD acotado; la constitución prescribe explícitamente capas estándar (`controller`, `service`, `repository`, `entity`).

---

## 2. Invariante de Stock No Negativo y Manejo de Errores

### Decisión
- La validación del stock no negativo (`stock >= 0`) se realiza en la capa `ProductService` antes de cualquier llamada al repositorio o guardado en base de datos.
- Si una petición de creación o actualización incluye un valor de stock menor a 0:
  1. `ProductService` lanza una excepción de negocio `InvalidStockException("El stock no puede ser un valor negativo")`.
  2. Un `@RestControllerAdvice` (`GlobalExceptionHandler`) intercepta `InvalidStockException` y serializa una respuesta estructurada con código HTTP `400 Bad Request`.
- Como defensa en profundidad secundaria:
  - En DTOs: Anotación `@Min(value = 0, message = "El stock debe ser mayor o igual a 0")`.
  - En Base de Datos: Restricción de columna CHECK (`stock >= 0`).

### Justificación
- Cumple directamente el requerimiento del usuario y el Principio IV de la constitución.
- Garantiza que bajo ninguna condición de entrada o llamada interna el inventario quede inconsistente.

### Alternativas Consideradas
- Validar únicamente en el DTO con `@Min(0)`: Rechazada porque la regla de negocio debe estar encapsulada en el servicio y no depender exclusivamente del transporte web.
- Responder con HTTP 422 Unprocessable Entity: Rechazada porque el requerimiento del usuario especifica taxativamente `400 Bad Request`.

---

## 3. Actualización Flexible de Precio y/o Stock (PUT)

### Decisión
- El endpoint `PUT /api/catalog/products/{id}` aceptará un DTO de actualización (`ProductUpdateDto`) con campos opcionales:
  - `price`: `BigDecimal` (opcional, debe ser > 0 si se envía).
  - `stock`: `Integer` (opcional, debe ser >= 0 si se envía).
- En el servicio `ProductService.updateProduct(Long id, ProductUpdateDto dto)`:
  1. Si `dto.getStock() != null`: valida que sea `>= 0` (lanza `InvalidStockException` si `< 0`) y actualiza `product.setStock(...)`.
  2. Si `dto.getPrice() != null`: valida que sea `> 0` y actualiza `product.setPrice(...)`.
  3. Si ambos son nulos: rechaza con `400 Bad Request` indicando que al menos un campo debe ser especificado.
  4. Si el producto con `id` no existe: lanza `ProductNotFoundException` mapeada a `404 Not Found`.

### Justificación
- Satisface el requisito: "El endpoint de actualización debe permitir tanto ajustar precio como stock, de forma independiente o conjunta".

---

## 4. Persistencia en PostgreSQL y Entorno Docker Local

### Decisión
- Base de datos relacional PostgreSQL 16+.
- Configuración en `application.yml`:
  ```yaml
  spring:
    datasource:
      url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5434/pedidos360_catalog}
      username: ${SPRING_DATASOURCE_USERNAME:postgres}
      password: ${SPRING_DATASOURCE_PASSWORD:postgres}
      driver-class-name: org.postgresql.Driver
    jpa:
      hibernate:
        ddl-auto: update
      show-sql: false
      properties:
        hibernate:
          format_sql: true
  ```
- Desarrollo local: Contenedor Docker mapeando el puerto `5434:5432` en localhost para evitar conflictos con instancias locales estándar en 5432.

### Justificación
- Cumple con el Principio II de la constitución y el requerimiento de conexión a Docker local en `localhost:5434`.

---

## 5. Documentación OpenAPI / Swagger

### Decisión
- Dependencia: `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0`.
- Swagger UI disponible en `/swagger-ui.html` y OpenAPI v3 spec en `/api-docs`.
- Anotaciones `@Operation`, `@ApiResponse`, `@Schema` en controladores y DTOs.

### Justificación
- Cumple con el Principio V de la constitución.
