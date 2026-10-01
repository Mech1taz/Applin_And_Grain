# Tasks: Product Catalog CRUD (ms-pedidos360-catalog)

**Feature**: `001-product-catalog-crud` | **Spec**: [spec.md](spec.md) | **Plan**: [plan.md](plan.md)

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Inicialización del proyecto Maven, configuración de dependencias y datasource de desarrollo local.

- [X] T001 Configurar `.gitignore` para excluir compilados `target/`, carpetas de IDEs (`.idea/`, `.vscode/`, `*.iml`), variables de entorno locales y secretos en `.gitignore`
- [X] T002 Inicializar `pom.xml` con Java 21, Spring Boot 3.3.x, `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `postgresql`, `springdoc-openapi-starter-webmvc-ui` y Lombok en `pom.xml`
- [X] T003 [P] Configurar el datasource de PostgreSQL apuntando por defecto al contenedor Docker local en `localhost:5434/pedidos360_catalog` y ajustes JPA en `src/main/resources/application.yml`
- [X] T004 [P] Crear la clase principal de arranque de Spring Boot en `src/main/java/com/pedidos360/catalog/MsCatalogApplication.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infraestructura central de dominio, persistencia, manejo de errores y documentación que bloquea a las historias de usuario.

- [X] T005 Crear la entidad JPA `Product` con campos: `id` (Long, PK identity), `name` (String, `VARCHAR(255)`, `@NotBlank`, not null), `description` (String, `VARCHAR(1000)`, nullable), `price` (BigDecimal, `NUMERIC(12,2)`, `@NotNull`, `@DecimalMin("0.01")`), y `stock` (Integer, not null, regla estricta: `stock >= 0`) en `src/main/java/com/pedidos360/catalog/entity/Product.java`
- [X] T006 [P] Crear la interfaz de repositorio `ProductRepository` extendiendo `JpaRepository<Product, Long>` en `src/main/java/com/pedidos360/catalog/repository/ProductRepository.java`
- [X] T007 [P] Crear el DTO estándar de error `ErrorResponseDto` con campos `timestamp`, `status`, `error`, `message` y `path` en `src/main/java/com/pedidos360/catalog/dto/ErrorResponseDto.java`
- [X] T008 [P] Implementar las excepciones de dominio `InvalidStockException` y `ProductNotFoundException` en `src/main/java/com/pedidos360/catalog/exception/InvalidStockException.java` y `src/main/java/com/pedidos360/catalog/exception/ProductNotFoundException.java`
- [X] T009 Implementar el manejador global de excepciones `@RestControllerAdvice` en `src/main/java/com/pedidos360/catalog/exception/GlobalExceptionHandler.java` mapeando `InvalidStockException` y errores de validación a HTTP `400 Bad Request`, y `ProductNotFoundException` a HTTP `404 Not Found`
- [X] T010 [P] Configurar la metadata y OpenAPI 3 / Swagger en `src/main/java/com/pedidos360/catalog/config/OpenApiConfig.java`
- [X] T011 [P] Implementar filtro o interceptor de seguridad para verificar el rol `Admin` en peticiones mutantes (`POST`, `PUT`, `DELETE`) en `src/main/java/com/pedidos360/catalog/config/SecurityFilterConfig.java`

---

## Phase 3: User Story 1 - Creación de Producto por Administrador (Priority: P1) 🎯 MVP

**Goal**: Permitir a usuarios con rol `Admin` dar de alta productos en el catálogo con validación de stock inicial no negativo.

**Independent Test**: Enviar `POST /api/catalog/products` con datos válidos y verificar la creación con HTTP 201; enviar con stock negativo y verificar rechazo con HTTP 400.

### Tests para User Story 1
- [X] T012 [P] [US1] Crear pruebas unitarias para creación de producto y validación de stock no negativo en `src/test/java/com/pedidos360/catalog/service/ProductServiceCreationTest.java`
- [X] T013 [P] [US1] Crear prueba de integración para el endpoint `POST /api/catalog/products` validando respuesta 201, rechazo 400 ante stock negativo y control de rol Admin en `src/test/java/com/pedidos360/catalog/controller/ProductCreateControllerTest.java`

### Implementación para User Story 1
- [X] T014 [P] [US1] Crear `ProductCreateDto` con validaciones `@NotBlank` para name, `@DecimalMin("0.01")` para price y stock inicial en `src/main/java/com/pedidos360/catalog/dto/ProductCreateDto.java`
- [X] T015 [P] [US1] Crear `ProductResponseDto` con id, name, description, price y stock en `src/main/java/com/pedidos360/catalog/dto/ProductResponseDto.java`
- [X] T016 [US1] Definir la interfaz `ProductService` e implementar `createProduct(ProductCreateDto dto)` en `src/main/java/com/pedidos360/catalog/service/ProductService.java` y `src/main/java/com/pedidos360/catalog/service/impl/ProductServiceImpl.java` validando stock >= 0 antes de guardar
- [X] T017 [US1] Implementar el endpoint `POST /api/catalog/products` con documentación OpenAPI en `src/main/java/com/pedidos360/catalog/controller/ProductController.java`

---

## Phase 4: User Story 2 - Consulta de Productos del Catálogo (Priority: P1)

**Goal**: Permitir listar todos los productos disponibles en el catálogo para el BFF y clientes finales.

**Independent Test**: Invocar `GET /api/catalog/products` y verificar que responde HTTP 200 OK con la lista de productos y sus detalles.

### Tests para User Story 2
- [X] T018 [P] [US2] Crear prueba de integración para `GET /api/catalog/products` en `src/test/java/com/pedidos360/catalog/controller/ProductListControllerTest.java`

### Implementación para User Story 2
- [X] T019 [US2] Implementar método `getAllProducts()` en `ProductService` y `ProductServiceImpl` en `src/main/java/com/pedidos360/catalog/service/impl/ProductServiceImpl.java` retornando `List<ProductResponseDto>`
- [X] T020 [US2] Implementar el endpoint `GET /api/catalog/products` en `src/main/java/com/pedidos360/catalog/controller/ProductController.java`

---

## Phase 5: User Story 3 & 4 - Actualización Flexible de Precio y/o Stock y Protección de Invariante (Priority: P1)

**Goal**: Permitir a usuarios `Admin` ajustar de forma independiente o conjunta el precio y stock vía `PUT /api/catalog/products/{id}`, rechazando taxativamente stocks negativos con HTTP 400 Bad Request.

**Independent Test**: Actualizar solo precio, actualizar solo stock, actualizar ambos simultáneamente y verificar respuesta 200 OK; enviar stock < 0 y comprobar respuesta 400 Bad Request sin mutación de estado.

### Tests para User Story 3 & 4
- [X] T021 [P] [US3] Crear pruebas unitarias para actualización flexible (solo precio, solo stock, ambos) y rechazo de stock negativo en `src/test/java/com/pedidos360/catalog/service/ProductServiceUpdateTest.java`
- [X] T022 [P] [US3] Crear pruebas de integración para `PUT /api/catalog/products/{id}` verificando actualización parcial, total y rechazo con HTTP 400 ante stock negativo en `src/test/java/com/pedidos360/catalog/controller/ProductUpdateControllerTest.java`

### Implementación para User Story 3 & 4
- [X] T023 [P] [US3] Crear `ProductUpdateDto` con campos opcionales `price` y `stock` y validador de presencia mínima en `src/main/java/com/pedidos360/catalog/dto/ProductUpdateDto.java`
- [X] T024 [US3] Implementar `updateProduct(Long id, ProductUpdateDto dto)` en `ProductService` y `ProductServiceImpl` en `src/main/java/com/pedidos360/catalog/service/impl/ProductServiceImpl.java` con validación estricta de stock no negativo antes de persistir
- [X] T025 [US3] Implementar el endpoint `PUT /api/catalog/products/{id}` en `src/main/java/com/pedidos360/catalog/controller/ProductController.java` con documentación OpenAPI

---

## Phase 6: User Story 5 - Consulta de Producto Individual por ID (Priority: P2)

**Goal**: Permitir consultar la información detallada de un producto individual a partir de su ID.

**Independent Test**: Invocar `GET /api/catalog/products/1` retornando 200 OK; invocar con ID inexistente y recibir 404 Not Found.

### Tests para User Story 5
- [X] T026 [P] [US5] Crear prueba de integración para `GET /api/catalog/products/{id}` verificando respuestas 200 OK y 404 Not Found en `src/test/java/com/pedidos360/catalog/controller/ProductGetByIdControllerTest.java`

### Implementación para User Story 5
- [X] T027 [US5] Implementar `getProductById(Long id)` en `ProductService` y `ProductServiceImpl` en `src/main/java/com/pedidos360/catalog/service/impl/ProductServiceImpl.java` lanzando `ProductNotFoundException` si no existe
- [X] T028 [US5] Implementar el endpoint `GET /api/catalog/products/{id}` en `src/main/java/com/pedidos360/catalog/controller/ProductController.java`

---

## Phase 7: User Story 6 - Eliminación de Producto del Catálogo (Priority: P2)

**Goal**: Permitir a usuarios con rol `Admin` dar de baja productos obsoletos del catálogo.

**Independent Test**: Ejecutar `DELETE /api/catalog/products/{id}` con rol Admin y verificar respuesta HTTP 204 No Content y que el producto ya no se encuentra en el catálogo.

### Tests para User Story 6
- [X] T029 [P] [US6] Crear prueba de integración para `DELETE /api/catalog/products/{id}` verificando 204 No Content y control de rol Admin en `src/test/java/com/pedidos360/catalog/controller/ProductDeleteControllerTest.java`

### Implementación para User Story 6
- [X] T030 [US6] Implementar `deleteProduct(Long id)` en `ProductService` y `ProductServiceImpl` en `src/main/java/com/pedidos360/catalog/service/impl/ProductServiceImpl.java`
- [X] T031 [US6] Implementar el endpoint `DELETE /api/catalog/products/{id}` en `src/main/java/com/pedidos360/catalog/controller/ProductController.java`

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Validación de infraestructura Docker, verificación de compilación y pruebas end-to-end.

- [X] T032 [P] Crear archivo `docker-compose.yml` para levantar PostgreSQL en el puerto `5434` en desarrollo local en `docker-compose.yml`
- [X] T033 Ejecutar la suite completa de pruebas unitarias e integración mediante `mvn clean test`
- [X] T034 Ejecutar y validar los 7 escenarios descritos en la guía rápida en `specs/001-product-catalog-crud/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias, inicio inmediato.
- **Foundational (Phase 2)**: Depende de Phase 1 completada. Bloquea todas las historias de usuario.
- **User Stories (Phase 3+)**: Dependen de Foundational (Phase 2) completada.
  - Phase 3 (US1 - Creación) es el MVP inicial.
  - Phase 4 (US2 - Listado) puede ejecutarse en paralelo o inmediatamente después de US1.
  - Phase 5 (US3 & US4 - Actualización y control de stock) depende de contar con entidades y servicio base.
  - Phase 6 (US5) y Phase 7 (US6) completan el ciclo CRUD (P2).
- **Polish (Phase 8)**: Depende de la finalización de las historias de usuario.

### Parallel Opportunities

- **Phase 1**: T003 y T004 pueden ejecutarse en paralelo tras T002.
- **Phase 2**: T006, T007, T008, T010 y T011 pueden ejecutarse en paralelo tras T005.
- **Phase 3 (US1)**: Las pruebas unitarias T012 y de integración T013 pueden escribirse en paralelo antes de la implementación de T016 y T017.
- **Phase 5 (US3)**: T021, T022 y T023 pueden redactarse en paralelo.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Phase 1: Setup (`pom.xml`, `.gitignore`, `application.yml`).
2. Completar Phase 2: Foundational (Entidad `Product`, `ProductRepository`, manejo global de excepciones).
3. Completar Phase 3: User Story 1 (Creación de producto y rechazo de stock negativo).
4. **Validación de Checkpoint**: Ejecutar pruebas de creación y verificar respuesta 201 y 400.

### Entrega Incremental

1. MVP: Creación de productos disponible y persistida.
2. Incremento 1: Consulta y listado del catálogo (`GET /api/catalog/products`) listo para integración con el BFF.
3. Incremento 2: Actualización flexible de precio y stock (`PUT /api/catalog/products/{id}`) con salvaguarda de stock no negativo.
4. Incremento 3: Consulta individual y baja de productos para cerrar el ciclo CRUD completo.
