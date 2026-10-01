package com.pedidos360.catalog.controller;

import com.pedidos360.catalog.dto.ProductResponseDto;
import com.pedidos360.catalog.exception.GlobalExceptionHandler;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({GlobalExceptionHandler.class})
class ProductGetByIdControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("Debe responder 200 OK cuando el producto existe")
    void shouldReturn200WhenProductExists() throws Exception {
        ProductResponseDto responseDto = new ProductResponseDto(
                1L, "Café Gourmet", "Desc", new BigDecimal("19.99"), 30
        );

        when(productService.getProductById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/catalog/products/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Café Gourmet"))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.stock").value(30));
    }

    @Test
    @DisplayName("Debe responder 404 Not Found cuando el producto no existe")
    void shouldReturn404WhenProductNotFound() throws Exception {
        when(productService.getProductById(999L)).thenThrow(new ProductNotFoundException(999L));

        mockMvc.perform(get("/api/catalog/products/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
