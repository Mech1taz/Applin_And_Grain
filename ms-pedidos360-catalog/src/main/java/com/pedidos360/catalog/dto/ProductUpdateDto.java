package com.pedidos360.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

@Schema(description = "Datos para actualizar precio y/o stock de forma independiente o conjunta")
public record ProductUpdateDto(

        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        @Schema(description = "Nuevo precio unitario de venta (opcional)", example = "22.00", nullable = true)
        BigDecimal price,

        @Schema(description = "Nuevo stock disponible (opcional, debe ser >= 0)", example = "80", nullable = true)
        Integer stock
) {
    public boolean hasAtLeastOneField() {
        return price != null || stock != null;
    }
}
