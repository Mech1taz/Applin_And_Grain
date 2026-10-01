<!--
Sync Impact Report:
- Version change: initial template -> 1.0.0
- List of modified principles:
  - [PRINCIPLE_1_NAME] -> I. Arquitectura en Capas y Stack Base (Spring Boot 3+ / Java 21)
  - [PRINCIPLE_2_NAME] -> II. Persistencia Relacional y Entorno de Desarrollo Dockerizado
  - [PRINCIPLE_3_NAME] -> III. Control de Acceso y Segregación de Roles (Admin vs. Consulta)
  - [PRINCIPLE_4_NAME] -> IV. Invariante Crítico de Integridad de Stock (No Negatividad)
  - [PRINCIPLE_5_NAME] -> V. Contratos de API y Documentación Viva (OpenAPI / Swagger)
  - [NEW] -> VI. Higiene de Repositorio y Gestión Estricta de Artefactos
- Added sections:
  - Estándares Técnicos y Restricciones de Dominio
  - Flujo de Desarrollo, Calidad y Verificación
- Removed sections: Ninguna (reemplazo de placeholders del template inicial)
- Follow-up TODOs: Ninguno
-->

# Catálogo Pedidos360 Constitution

## Core Principles

### I. Arquitectura en Capas y Stack Base (Spring Boot 3+ / Java 21)
El microservicio DEBE construirse sobre Java 21 LTS y Spring Boot 3 o superior, respetando una estricta arquitectura en capas desacoplada:
- **Controller**: Exposición exclusiva de endpoints REST, manejo de HTTP requests/responses, validación de entrada (Bean Validation) y serialización.
- **Service**: Concentración obligatoria de la lógica de negocio, validaciones de dominio y orquestación transaccional.
- **Repository**: Interfaces Spring Data JPA dedicadas al acceso y consulta a base de datos.
- **Entity**: Modelado de persistencia JPA representativo del modelo de dominio relacional.
*Razón de ser*: Asegurar mantenibilidad, testabilidad unitaria y coherencia estructural en todo el ciclo de vida del microservicio.

### II. Persistencia Relacional y Entorno de Desarrollo Dockerizado
La persistencia de datos DEBE implementarse mediante PostgreSQL a través de Spring Data JPA / Hibernate.
- Para el desarrollo y pruebas locales, el motor PostgreSQL DEBE ejecutarse exclusivamente dentro de un contenedor Docker (mediante docker-compose o Testcontainers).
- Ningún desarrollador ni entorno local DEBE requerir una instalación nativa del motor de base de datos en el host.
*Razón de ser*: Garantizar reproducibilidad idéntica entre entornos de desarrollo, integración continua y producción, evitando inconsistencias por versiones locales.

### III. Control de Acceso y Segregación de Roles (Admin vs. Consulta)
El acceso a las operaciones del microservicio DEBE adherirse a una política estricta de mínimos privilegios:
- **Mutaciones (Crear, Editar, Eliminar)**: EXCLUSIVAMENTE autorizadas para usuarios con rol `Admin`. Cualquier intento no autorizado DEBE ser rechazado con código HTTP 403 Forbidden (o 401 Unauthorized si carece de autenticación).
- **Lectura / Consulta**: Diseñada para consumo abierto o según las reglas de orquestación y autorización que delegue el BFF (Backend for Frontend).
*Razón de ser*: Proteger la integridad del catálogo de productos y prevenir modificaciones accidentales o maliciosas en el inventario comercial.

### IV. Invariante Crítico de Integridad de Stock (No Negatividad)
El stock de cualquier producto NUNCA DEBE quedar en un valor negativo bajo ninguna circunstancia:
- Todas las operaciones de decremento o ajuste de existencias DEBEN validar la suficiencia de stock previa o atómicamente a nivel de base de datos/transacción.
- Cualquier operación que intente reducir el stock por debajo de cero DEBE ser rechazada de manera atómica, lanzando una excepción de negocio explícita (e.g., HTTP 409 Conflict o 422 Unprocessable Entity) y revirtiendo cualquier cambio de estado.
*Razón de ser*: Evitar inconsistencias operativas, sobreventas de inventario y errores de sincronización con los subsistemas de pedidos y despacho.

### V. Contratos de API y Documentación Viva (OpenAPI / Swagger)
El servicio DEBE exponer la documentación completa de sus contratos REST a través de OpenAPI 3 / Swagger UI accesible en runtime (vía `springdoc-openapi`):
- Todos los endpoints, códigos de respuesta HTTP, esquemas de payload (request/response) y requerimientos de autorización DEBEN estar debidamente descritos y sincronizados con el código.
*Razón de ser*: Facilitar la integración fluida con el BFF, otros microservicios y clientes frontend, reduciendo fricciones de comunicación mediante contratos vivos y verificables.

### VI. Higiene de Repositorio y Gestión Estricta de Artefactos
El control de versiones DEBE contener únicamente código fuente Java, configuración de compilación Maven (`pom.xml`) y documentación de especificación:
- El archivo `.gitignore` DEBE excluir rigurosamente el directorio de compilación `target/`, carpetas de IDEs (`.idea/`, `.vscode/`, `*.iml`), archivos de sistema operativo y credenciales o secretos locales.
- NUNCA se deben commitear contraseñas, tokens, llaves o variables de entorno sensibles al repositorio.
*Razón de ser*: Salvaguardar la seguridad de la infraestructura y mantener el repositorio liviano, portable e independiente de herramientas o entornos de desarrollo específicos.

## Estándares Técnicos y Restricciones de Dominio
- **Transaccionalidad**: Las operaciones que involucren modificación de inventario y datos de catálogo DEBEN estar demarcadas con `@Transactional` asegurando niveles de aislamiento adecuados contra condiciones de carrera.
- **Manejo Centralizado de Errores**: El servicio DEBE utilizar un manejador global de excepciones (`@ControllerAdvice` / `ProblemDetail`) para emitir respuestas de error estructuradas y estandarizadas (RFC 7807 o formato acordado).
- **Configuración Externalizada**: Parámetros de conexión a base de datos, credenciales y puertos DEBEN inyectarse mediante variables de entorno en `application.yml`.

## Flujo de Desarrollo, Calidad y Verificación
- **Pruebas Automatizadas**: Todo cambio en reglas de negocio (especialmente el invariante de no negatividad de stock y permisos de rol) DEBE contar con pruebas unitarias (`JUnit 5`, `Mockito`) y pruebas de integración sobre base de datos dockerizada.
- **Compilación Limpia**: Todo pull request DEBE compilar exitosamente mediante `mvn clean test` sin advertencias de vulnerabilidad crítica.
- **Revisión de Cumplimiento**: Ningún cambio de código podrá integrarse si infringe alguno de los 6 principios rectores establecidos en esta constitución.

## Governance
Esta constitución establece las reglas fundamentales de arquitectura, seguridad y desarrollo del microservicio de Catálogo de Pedidos360 y tiene precedencia sobre cualquier decisión técnica puntual o ad-hoc:
- **Procedimiento de Enmienda**: Toda modificación, agregado o eliminación de principios requiere propuesta formal, discusión y aprobación del equipo técnico, acompañada de un plan de migración si genera impacto en código preexistente.
- **Política de Versionado**:
  - `MAJOR`: Cambios incompatibles en gobernanza o eliminación/redefinición de principios fundamentales.
  - `MINOR`: Incorporación de nuevos principios o ampliación material de directivas.
  - `PATCH`: Correcciones de redacción, refinamientos tipográficos o aclaraciones menores.
- **Auditoría Continua**: Cada especificación (`/speckit-specify`), plan (`/speckit-plan`) y pull request DEBE ser validado explícitamente contra esta constitución antes de proceder a la fase de implementación.

**Version**: 1.0.0 | **Ratified**: 2026-09-30 | **Last Amended**: 2026-09-30
