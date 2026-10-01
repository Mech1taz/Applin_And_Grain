# Data Model: Product Catalog CRUD

**Feature**: `001-product-catalog-crud` | **Microservice**: `ms-pedidos360-catalog`

## 1. Entidad JPA: `Product`

- **Tabla en BD**: `products`
- **Paquete**: `com.pedidos360.catalog.entity`

### Estructura de Campos

| Campo | Tipo Java | Columna BD | Nullable | Restricciones / Reglas |
|---|---|---|---|---|
| `id` | `Long` | `id` | NO | Clave primaria, autoincremental (`GenerationType.IDENTITY`). |
| `name` | `String` | `name` | NO | `VARCHAR(255)`, `@NotBlank`, longitud máxima 255. |
| `description` | `String` | `description` | SÍ | `VARCHAR(1000)`, texto explicativo opcional. |
| `price` | `BigDecimal` | `price` | NO | `NUMERIC(12,2)`, `@NotNull`, `@DecimalMin(value = "0.01")`. |
| `stock` | `Integer` | `stock` | NO | `INTEGER`, `@NotNull`, regla estricta: `stock >= 0`. |

### Definición de Entidad (Referencia de Diseño)

```java
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;
}
```

---

## 2. Repositorio Spring Data JPA

- **Interfaz**: `ProductRepository`
- **Paquete**: `com.pedidos360.catalog.repository`
- **Extiende**: `JpaRepository<Product, Long>`

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Métodos estándar provistos por JpaRepository:
    // findAll(), findById(Long id), save(Product entity), deleteById(Long id)
}
```

---

## 3. Data Transfer Objects (DTOs)

### 3.1 `ProductCreateDto` (Request para creación)
```java
public record ProductCreateDto(
    @NotBlank(message = "El nombre del producto es obligatorio")
    String name,

    String description,

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    BigDecimal price,

    @NotNull(message = "El stock inicial es obligatorio")
    Integer stock
) {}
```

### 3.2 `ProductUpdateDto` (Request para actualización flexible de precio y/o stock)
```java
public record ProductUpdateDto(
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    BigDecimal price,

    Integer stock
) {
    public boolean hasAtLeastOneField() {
        return price != null || stock != null;
    }
}
```

### 3.3 `ProductResponseDto` (Response estándar)
```java
public record ProductResponseDto(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Integer stock
) {}
```

### 3.4 `ErrorResponseDto` (Payload de error RFC 7807 compatible)
```java
public record ErrorResponseDto(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path
) {}
```

---

## 4. Invariantes de Dominio y Transiciones de Estado

```
┌──────────────────┐
│  Solicitud POST  │──► [Validar stock >= 0] ──► (Si stock < 0: Excepción 400)
└──────────────────┘            │
                                ▼ (Si válido)
                     [Persistir en BD vía JPA] ──► [Estado: ACTIVO en Catálogo]
                                                          │
                                                          ▼
┌──────────────────┐                            ┌────────────────────────┐
│  Solicitud PUT   │──► [Validar stock >= 0] ──►│ Actualizar Atributos   │
└──────────────────┘   (Si stock < 0: Error 400)│ (precio, stock o ambos)│
                                                └────────────────────────┘
```

1. **Invariante de No Negatividad**:
   - `stock >= 0` es verificado por `ProductService` antes de cualquier llamada a `ProductRepository.save(...)`.
   - Si `stock < 0`, se lanza `InvalidStockException("El stock no puede fijarse en un valor negativo")`.
2. **Invariante de Precio Positivo**:
   - `price > 0.00` validado en la entrada mediante anotaciones Bean Validation y en el servicio de actualización.
