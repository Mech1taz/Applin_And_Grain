package com.pedidos360.catalog.controller;

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
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({GlobalExceptionHandler.class})
class ProductListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("Debe responder 200 OK con la lista de productos disponibles sin requerir rol Admin")
    void shouldReturnAllProductsSuccessfully() throws Exception {
        List<ProductResponseDto> products = List.of(
                new ProductResponseDto(1L, "Café 1", "Desc 1", new BigDecimal("15.00"), 30),
                new ProductResponseDto(2L, "Café 2", "Desc 2", new BigDecimal("20.00"), 45)
        );

        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/catalog/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Café 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Café 2"));
    }

    @Test
    @DisplayName("Debe responder 200 OK con lista vacía cuando no hay productos registrados")
    void shouldReturnEmptyListWhenNoProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/catalog/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
