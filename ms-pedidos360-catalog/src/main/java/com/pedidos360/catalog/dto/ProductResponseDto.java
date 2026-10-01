package com.pedidos360.catalog.dto;

import com.pedidos360.catalog.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Representación del producto en el catálogo")
public record ProductResponseDto(

        @Schema(description = "Identificador único asignado", example = "1")
        Long id,

        @Schema(description = "Nombre comercial", example = "Café Colombiano Molido 500g")
        String name,

        @Schema(description = "Descripción detallada", example = "Café premium de altura tostado medio")
        String description,

        @Schema(description = "Precio unitario", example = "18.50")
        BigDecimal price,

        @Schema(description = "Stock físico disponible", example = "50")
        Integer stock
) {
    public static ProductResponseDto fromEntity(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock()
        );
    }
}
