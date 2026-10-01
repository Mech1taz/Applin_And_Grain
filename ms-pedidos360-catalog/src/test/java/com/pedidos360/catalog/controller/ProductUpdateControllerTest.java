package com.pedidos360.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pedidos360.catalog.dto.ProductResponseDto;
import com.pedidos360.catalog.dto.ProductUpdateDto;
import com.pedidos360.catalog.exception.GlobalExceptionHandler;
import com.pedidos360.catalog.exception.InvalidStockException;
import com.pedidos360.catalog.exception.ProductNotFoundException;
import com.pedidos360.catalog.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({GlobalExceptionHandler.class})
class ProductUpdateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Debe responder 200 OK al actualizar precio y/o stock con rol Admin")
    void shouldReturn200WhenUpdatedByAdmin() throws Exception {
        ProductUpdateDto updateDto = new ProductUpdateDto(new BigDecimal("22.00"), 80);
        ProductResponseDto responseDto = new ProductResponseDto(
                1L, "Café", "Desc", new BigDecimal("22.00"), 80
        );

        when(productService.updateProduct(eq(1L), any(ProductUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/catalog/products/1")
                        .header("X-User-Role", "Admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.price").value(22.00))
                .andExpect(jsonPath("$.stock").value(80));
    }

    @Test
    @DisplayName("Debe responder 400 Bad Request cuando el stock es negativo en PUT")
    void shouldReturn400WhenStockIsNegativeOnUpdate() throws Exception {
        ProductUpdateDto updateDto = new ProductUpdateDto(null, -5);

        when(productService.updateProduct(eq(1L), any(ProductUpdateDto.class)))
                .thenThrow(new InvalidStockException("El stock no puede fijarse en un valor negativo"));

        mockMvc.perform(put("/api/catalog/products/1")
                        .header("X-User-Role", "Admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El stock no puede fijarse en un valor negativo"));
    }

    @Test
    @DisplayName("Debe responder 404 Not Found cuando el producto a actualizar no existe")
    void shouldReturn404WhenProductNotFound() throws Exception {
        ProductUpdateDto updateDto = new ProductUpdateDto(new BigDecimal("25.00"), null);

        when(productService.updateProduct(eq(999L), any(ProductUpdateDto.class)))
                .thenThrow(new ProductNotFoundException(999L));

        mockMvc.perform(put("/api/catalog/products/999")
                        .header("X-User-Role", "Admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Debe responder 403 Forbidden cuando se intenta actualizar sin rol Admin")
    void shouldReturn403WhenNotAdmin() throws Exception {
        ProductUpdateDto updateDto = new ProductUpdateDto(new BigDecimal("22.00"), null);

        mockMvc.perform(put("/api/catalog/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }
}
