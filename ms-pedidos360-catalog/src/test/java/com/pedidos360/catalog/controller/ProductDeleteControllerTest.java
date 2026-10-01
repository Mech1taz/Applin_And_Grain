package com.pedidos360.catalog.controller;

import com.pedidos360.catalog.exception.GlobalExceptionHandler;
import com.pedidos360.catalog.exception.ProductNotFoundException;
import com.pedidos360.catalog.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({GlobalExceptionHandler.class})
class ProductDeleteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("Debe responder 204 No Content al eliminar producto con rol Admin")
    void shouldReturn204WhenDeletedByAdmin() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/catalog/products/1")
                        .header("X-User-Role", "Admin"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Debe responder 403 Forbidden cuando se intenta eliminar sin rol Admin")
    void shouldReturn403WhenNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/catalog/products/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Debe responder 404 Not Found cuando el producto a eliminar no existe")
    void shouldReturn404WhenProductNotFound() throws Exception {
        doThrow(new ProductNotFoundException(999L)).when(productService).deleteProduct(999L);

        mockMvc.perform(delete("/api/catalog/products/999")
                        .header("X-User-Role", "Admin"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
