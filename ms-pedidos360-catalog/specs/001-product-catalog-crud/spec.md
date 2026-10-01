# Feature Specification: Product Catalog CRUD (ms-pedidos360-catalog)

**Feature Branch**: `001-product-catalog-crud`

**Created**: 2026-09-30

**Status**: Draft

**Input**: User description: "Construye el CRUD de productos del microservicio ms-pedidos360-catalog. Un producto tiene: id, nombre, descripción, precio y stock disponible. Se puede crear un producto, listarlo (GET /api/catalog/products), y actualizar su precio o stock (PUT /api/catalog/products/{id}). El stock no puede fijarse en un valor negativo; si se intenta, el sistema responde con un error claro (400 Bad Request). El endpoint de actualización debe permitir tanto ajustar precio como stock, de forma independiente o conjunta. Todos los productos se persisten en una base de datos PostgreSQL."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Creación de Producto por Administrador (Priority: P1)

Como usuario con rol Administrador, quiero registrar un nuevo producto con su nombre, descripción, precio y existencias iniciales en el catálogo para ponerlo a disposición de la plataforma de ventas.

**Why this priority**: Es el punto de entrada fundamental para alimentar el catálogo del negocio; sin productos creados no hay inventario consultable ni comercializable.

**Independent Test**: Se puede verificar enviando una solicitud de creación con datos válidos de producto y comprobando que se genera un nuevo registro persistente con identificador único y datos coincidentes.

**Acceptance Scenarios**:

1. **Given** un usuario autenticado con rol `Admin` y datos válidos (nombre "Café Gourmet", descripción "Tostado medio 250g", precio 15.50, stock 100), **When** solicita crear el producto, **Then** el sistema registra el producto, asigna un ID único y retorna código HTTP 201 Created con el producto creado.
2. **Given** un usuario autenticado con rol `Admin` que envía un producto con stock negativo (e.g. -5), **When** solicita crear el producto, **Then** el sistema rechaza la creación con código HTTP 400 Bad Request y un mensaje de error que explica que el stock no puede ser negativo.
3. **Given** un usuario sin rol `Admin` (o sin autenticación), **When** solicita crear un producto, **Then** el sistema rechaza la operación con código HTTP 403 Forbidden (o 401 si no está autenticado).

---

### User Story 2 - Consulta de Productos del Catálogo (Priority: P1)

Como consumidor del catálogo (BFF, cliente web/móvil o servicio consumidor), quiero listar los productos registrados para visualizar las opciones de compra y su disponibilidad.

**Why this priority**: Es la funcionalidad principal de lectura que consumen los clientes finales a través del BFF para navegar el catálogo comercial.

**Independent Test**: Se puede verificar insertando productos de prueba y solicitando el listado en `GET /api/catalog/products`, confirmando que retorna la lista completa con los campos esperados.

**Acceptance Scenarios**:

1. **Given** que existen productos registrados en el sistema, **When** un cliente realiza una petición `GET /api/catalog/products`, **Then** el sistema responde con código HTTP 200 OK y la lista de todos los productos conteniendo id, nombre, descripción, precio y stock disponible.
2. **Given** que no existen productos registrados aún, **When** un cliente realiza una petición `GET /api/catalog/products`, **Then** el sistema responde con código HTTP 200 OK y una lista vacía.

---

### User Story 3 - Actualización Flexible de Precio y/o Stock (Priority: P1)

Como usuario con rol Administrador, quiero modificar el precio, el stock disponible, o ambos valores de un producto existente para mantener actualizada la información comercial y el inventario físico.

**Why this priority**: Los precios y el stock varían dinámicamente; permitir actualizaciones parciales o totales sobre el mismo endpoint simplifica la integración y la operativa de gestión.

**Independent Test**: Se puede verificar actualizando únicamente el precio de un producto, únicamente el stock, y ambos a la vez, comprobando que solo los campos especificados se actualizan y los no especificados permanecen inalterados.

**Acceptance Scenarios**:

1. **Given** un producto existente con precio 10.00 y stock 20, **When** el Administrador envía `PUT /api/catalog/products/{id}` indicando solo un nuevo precio de 12.50, **Then** el sistema actualiza el precio a 12.50 manteniendo el stock en 20 y responde 200 OK con el producto actualizado.
2. **Given** un producto existente con precio 10.00 y stock 20, **When** el Administrador envía `PUT /api/catalog/products/{id}` indicando solo un nuevo stock de 35, **Then** el sistema actualiza el stock a 35 manteniendo el precio en 10.00 y responde 200 OK.
3. **Given** un producto existente, **When** el Administrador envía `PUT /api/catalog/products/{id}` con nuevo precio 14.00 y nuevo stock 50, **Then** el sistema actualiza ambos atributos simultáneamente y responde 200 OK.

---

### User Story 4 - Validación y Protección de Invariante de Stock No Negativo (Priority: P1)

Como responsable de negocio e inventario, quiero que cualquier intento de fijar o decrementar el stock a un valor negativo sea rechazado con un error descriptivo para prevenir inconsistencias y sobreventas.

**Why this priority**: Cumple con el principio de integridad constitucional que prohíbe taxativamente la existencia de existencias en negativo.

**Independent Test**: Se puede verificar enviando peticiones con stock negativo (< 0) tanto en creación como en actualización, comprobando que el sistema emite HTTP 400 Bad Request y no altera el estado del producto.

**Acceptance Scenarios**:

1. **Given** un producto existente con stock 15, **When** un usuario intenta actualizar su stock a -1 mediante `PUT /api/catalog/products/{id}`, **Then** el sistema rechaza la petición con código HTTP 400 Bad Request, detalla que el stock debe ser mayor o igual a 0, y el stock en base de datos permanece en 15.

---

### User Story 5 - Consulta de Producto Individual por ID (Priority: P2)

Como consumidor del catálogo, quiero consultar la información detallada de un producto específico mediante su identificador para conocer sus datos actualizados antes de realizar una acción sobre él.

**Why this priority**: Necesario para páginas de detalle de producto y para verificar el estado de un producto concreto sin listar todo el catálogo.

**Independent Test**: Se puede verificar solicitando `GET /api/catalog/products/{id}` con un identificador existente y con uno inexistente.

**Acceptance Scenarios**:

1. **Given** un producto existente con ID `prod-101`, **When** se solicita `GET /api/catalog/products/prod-101`, **Then** el sistema retorna HTTP 200 OK con los datos del producto.
2. **Given** un ID que no corresponde a ningún producto, **When** se solicita `GET /api/catalog/products/prod-999`, **Then** el sistema responde con código HTTP 404 Not Found.

---

### User Story 6 - Eliminación de Producto del Catálogo (Priority: P2)

Como Administrador, quiero poder eliminar un producto del catálogo cuando ya no se comercialice en la plataforma.

**Why this priority**: Cierra el ciclo de vida del CRUD garantizando la limpieza del catálogo obsoleto.

**Independent Test**: Se puede verificar llamando a `DELETE /api/catalog/products/{id}` con rol Admin y validando que el producto deja de aparecer en las consultas.

**Acceptance Scenarios**:

1. **Given** un producto existente, **When** un Administrador solicita `DELETE /api/catalog/products/{id}`, **Then** el sistema elimina el producto y responde con HTTP 204 No Content (o 200 OK).
2. **Given** un usuario sin rol `Admin`, **When** solicita eliminar un producto, **Then** el sistema rechaza la petición con HTTP 403 Forbidden.

---

### Edge Cases

- **Producto inexistente en actualización**: Al ejecutar `PUT /api/catalog/products/{id}` con un identificador que no existe, el sistema debe responder 404 Not Found.
- **Payload vacío en actualización**: Si el cuerpo del `PUT` no contiene ni precio ni stock, el sistema debe responder 400 Bad Request indicando que al menos un campo a modificar debe ser suministrado.
- **Precio inválido**: Si se suministra un precio negativo o igual a cero, el sistema debe responder 400 Bad Request con un mensaje de validación claro.
- **Concurrencia en actualización de stock**: Si dos peticiones intentan actualizar el inventario casi simultáneamente, las transacciones deben aislarse para evitar condiciones de carrera o sobreescrituras desfasadas.
- **Entrada con tipos de datos inválidos**: Si se envían caracteres alfanuméricos en campos numéricos como precio o stock, el sistema debe responder 400 Bad Request estructurado.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE permitir a usuarios autorizados con rol `Admin` registrar nuevos productos con: nombre (obligatorio, no vacío), descripción (opcional), precio unitario (obligatorio, > 0) y stock disponible (obligatorio, >= 0).
- **FR-002**: El sistema DEBE generar y asignar un identificador único e inmutable a cada producto registrado.
- **FR-003**: El sistema DEBE permitir listar todos los productos a través del endpoint `GET /api/catalog/products`, retornando el listado completo en formato JSON accesible para consumidores y BFF.
- **FR-004**: El sistema DEBE permitir consultar el detalle de un producto individual por su ID a través del endpoint `GET /api/catalog/products/{id}`.
- **FR-005**: El sistema DEBE permitir a usuarios con rol `Admin` actualizar el precio, el stock disponible, o ambos atributos de un producto existente mediante `PUT /api/catalog/products/{id}`.
- **FR-006**: El sistema DEBE validar de forma estricta que el stock disponible nunca sea menor a 0. Cualquier solicitud que especifique un valor de stock < 0 DEBE ser rechazada inmediatamente con código HTTP 400 Bad Request y un mensaje de error explícito.
- **FR-007**: El sistema DEBE permitir a usuarios con rol `Admin` eliminar un producto del catálogo mediante `DELETE /api/catalog/products/{id}`.
- **FR-008**: El sistema DEBE restringir los endpoints de mutación (creación, actualización y eliminación) exclusivamente al rol `Admin`, respondiendo con HTTP 403 Forbidden cuando el usuario carezca del rol y HTTP 401 Unauthorized cuando no se provean credenciales válidas.
- **FR-009**: El sistema DEBE persistir todos los productos y sus modificaciones de estado en una base de datos PostgreSQL garantizando integridad transaccional (`@Transactional`).
- **FR-010**: El sistema DEBE exponer la documentación completa e interactiva de los endpoints según OpenAPI 3 / Swagger en runtime.

### Key Entities *(include if feature involves data)*

- **Producto (`Product`)**:
  - `id`: Identificador único del producto en el catálogo (inmutable).
  - `nombre`: Nombre o denominación comercial del producto (texto, no nulo, no vacío).
  - `descripcion`: Descripción detallada del producto (texto libre, opcional).
  - `precio`: Precio monetario unitario de venta (decimal positivo, mayor a cero).
  - `stockDisponible`: Cantidad de unidades físicas disponibles en inventario (entero mayor o igual a cero).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de las solicitudes que intenten fijar un stock negativo son rechazadas con HTTP 400 Bad Request, sin producirse mutaciones en el almacenamiento.
- **SC-002**: Las operaciones de consulta del catálogo (`GET /api/catalog/products`) devuelven el listado en menos de 500 ms en condiciones normales de operación.
- **SC-003**: Los administradores pueden actualizar precio de manera aislada, stock de manera aislada, o ambos atributos conjuntamente con una tasa de éxito del 100% para solicitudes con datos válidos.
- **SC-004**: El 100% de las solicitudes de mutación no autorizadas (sin rol `Admin`) son bloqueadas con HTTP 403 Forbidden o 401 Unauthorized.
- **SC-005**: La documentación OpenAPI / Swagger está disponible y refleja con exactitud la totalidad de los esquemas de petición, respuesta y códigos de estado HTTP implementados.

## Assumptions

- La autenticación y propagación de roles (`Admin`) es provista mediante tokens o cabeceras HTTP validadas por el microservicio en coordinación con el BFF / API Gateway.
- Los clientes consumen la API mediante `application/json` estándar en codificación UTF-8.
- La precisión monetaria del precio utiliza 2 decimales para garantizar consistencia comercial.
- En caso de que se intente consultar o actualizar un ID de producto que no existe en el catálogo, el sistema responde consistentemente con código HTTP 404 Not Found.
