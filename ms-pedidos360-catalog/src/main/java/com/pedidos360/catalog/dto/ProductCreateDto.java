package com.pedidos360.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Datos requeridos para dar de alta un producto en el catálogo")
public record ProductCreateDto(

        @NotBlank(message = "El nombre del producto no puede estar en blanco")
        @Schema(description = "Nombre comercial del producto", example = "Café Colombiano Molido 500g")
        String name,

        @Schema(description = "Descripción detallada del producto", example = "Café premium de altura tostado medio", nullable = true)
        String description,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        @Schema(description = "Precio unitario de venta", example = "18.50")
        BigDecimal price,

        @NotNull(message = "El stock inicial es obligatorio")
        @Schema(description = "Cantidad física disponible inicial", example = "50")
        Integer stock
) {}
