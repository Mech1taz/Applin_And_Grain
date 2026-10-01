package com.pedidos360.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pedidos360.catalog.dto.ProductCreateDto;
import com.pedidos360.catalog.dto.ProductResponseDto;
import com.pedidos360.catalog.exception.GlobalExceptionHandler;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({GlobalExceptionHandler.class})
class ProductCreateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Debe responder 201 Created al crear producto con rol Admin y datos válidos")
    void shouldReturn201WhenCreatedByAdmin() throws Exception {
        ProductCreateDto createDto = new ProductCreateDto(
                "Café Colombiano Molido 500g",
                "Café premium de altura",
                new BigDecimal("18.50"),
                50
        );

        ProductResponseDto responseDto = new ProductResponseDto(
                1L,
                "Café Colombiano Molido 500g",
                "Café premium de altura",
                new BigDecimal("18.50"),
                50
        );

        when(productService.createProduct(any(ProductCreateDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/catalog/products")
                        .header("X-User-Role", "Admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Café Colombiano Molido 500g"))
                .andExpect(jsonPath("$.price").value(18.50))
                .andExpect(jsonPath("$.stock").value(50));
    }

    @Test
    @DisplayName("Debe responder 403 Forbidden cuando el usuario carece del rol Admin")
    void shouldReturn403WhenNotAdmin() throws Exception {
        ProductCreateDto createDto = new ProductCreateDto(
                "Café Colombiano",
                "Desc",
                new BigDecimal("18.50"),
                50
        );

        mockMvc.perform(post("/api/catalog/products")
                        .header("X-User-Role", "Customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Acceso restringido: Se requiere el rol Admin para crear, editar o eliminar productos"));
    }

    @Test
    @DisplayName("Debe responder 400 Bad Request cuando el stock es negativo")
    void shouldReturn400WhenStockIsNegative() throws Exception {
        String invalidPayload = """
                {
                  "name": "Café Inválido",
                  "description": "Desc",
                  "price": 10.00,
                  "stock": -5
                }
                """;

        when(productService.createProduct(any()))
                .thenThrow(new com.pedidos360.catalog.exception.InvalidStockException("El stock no puede fijarse en un valor negativo"));

        mockMvc.perform(post("/api/catalog/products")
                        .header("X-User-Role", "Admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
