# Quickstart Guide: ms-pedidos360-catalog

Guía rápida de arranque y validación de extremo a extremo para el CRUD de productos y control de stock.

## 1. Prerrequisitos

- Java 21 LTS (`java -version`)
- Maven 3.9+ (`mvn -version`)
- Docker Engine corriendo localmente (`docker info`)
- Herramienta HTTP: `curl`, `httpie` o Swagger UI

---

## 2. Puesta en Marcha del Entorno Local

### 2.1 Levantar PostgreSQL en Docker (Puerto 5434)

Ejecutar el contenedor de base de datos con el mapeo al puerto 5434:

```bash
docker run --name pedidos360-postgres \
  -e POSTGRES_DB=pedidos360_catalog \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5434:5432 \
  -d postgres:16-alpine
```

Verificar que el contenedor está activo:
```bash
docker ps --filter "name=pedidos360-postgres"
```

### 2.2 Compilar y Ejecutar el Microservicio

```bash
mvn clean spring-boot:run
```

El servicio iniciará en `http://localhost:8080`.

---

## 3. Escenarios de Validación End-to-End

### Escenario 1: Creación de un Producto Válido (Rol Admin)

```bash
curl -X POST http://localhost:8080/api/catalog/products \
  -H "Content-Type: application/json" \
  -H "X-User-Role: Admin" \
  -d '{
    "name": "Café Colombiano Molido 500g",
    "description": "Café premium de altura tostado medio",
    "price": 18.50,
    "stock": 50
  }'
```

**Resultado Esperado**:
- Código HTTP: `201 Created`
- Respuesta JSON con `id` asignado (e.g. `1`), nombre, precio `18.50` y stock `50`.

---

### Escenario 2: Rechazo Estricto de Creación con Stock Negativo

```bash
curl -X POST http://localhost:8080/api/catalog/products \
  -H "Content-Type: application/json" \
  -H "X-User-Role: Admin" \
  -d '{
    "name": "Producto Inválido",
    "price": 10.00,
    "stock": -5
  }'
```

**Resultado Esperado**:
- Código HTTP: `400 Bad Request`
- Mensaje: `"El stock no puede fijarse en un valor negativo"`

---

### Escenario 3: Listar Productos del Catálogo

```bash
curl -X GET http://localhost:8080/api/catalog/products
```

**Resultado Esperado**:
- Código HTTP: `200 OK`
- Arreglo JSON conteniendo los productos creados.

---

### Escenario 4: Actualización Flexible - Modificar solo Precio

```bash
curl -X PUT http://localhost:8080/api/catalog/products/1 \
  -H "Content-Type: application/json" \
  -H "X-User-Role: Admin" \
  -d '{
    "price": 22.00
  }'
```

**Resultado Esperado**:
- Código HTTP: `200 OK`
- Producto retornado con precio `22.00` y stock inalterado (`50`).

---

### Escenario 5: Actualización Flexible - Modificar solo Stock

```bash
curl -X PUT http://localhost:8080/api/catalog/products/1 \
  -H "Content-Type: application/json" \
  -H "X-User-Role: Admin" \
  -d '{
    "stock": 80
  }'
```

**Resultado Esperado**:
- Código HTTP: `200 OK`
- Producto retornado con precio `22.00` y stock actualizado a `80`.

---

### Escenario 6: Rechazo Estricto de Actualización a Stock Negativo

```bash
curl -X PUT http://localhost:8080/api/catalog/products/1 \
  -H "Content-Type: application/json" \
  -H "X-User-Role: Admin" \
  -d '{
    "stock": -10
  }'
```

**Resultado Esperado**:
- Código HTTP: `400 Bad Request`
- Mensaje explicativo; el stock en base de datos permanece en `80`.

---

### Escenario 7: Validación de Contratos y Documentación Swagger UI

Abrir en navegador:
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI Specification: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

Verificar la presencia y descripción de los endpoints y esquemas conforme a [openapi.yaml](contracts/openapi.yaml).
