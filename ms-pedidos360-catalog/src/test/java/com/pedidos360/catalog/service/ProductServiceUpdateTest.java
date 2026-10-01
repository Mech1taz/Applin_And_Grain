package com.pedidos360.catalog.service;

import com.pedidos360.catalog.dto.ProductResponseDto;
import com.pedidos360.catalog.dto.ProductUpdateDto;
import com.pedidos360.catalog.entity.Product;
import com.pedidos360.catalog.exception.InvalidStockException;
import com.pedidos360.catalog.exception.ProductNotFoundException;
import com.pedidos360.catalog.repository.ProductRepository;
import com.pedidos360.catalog.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceUpdateTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product existingProduct;

    @BeforeEach
    void setUp() {
        existingProduct = Product.builder()
                .id(1L)
                .name("Café Colombiano")
                .description("Café premium")
                .price(new BigDecimal("18.50"))
                .stock(50)
                .build();
    }

    @Test
    @DisplayName("Debe actualizar solo el precio manteniendo el stock existente")
    void shouldUpdateOnlyPriceSuccessfully() {
        ProductUpdateDto updateDto = new ProductUpdateDto(new BigDecimal("22.00"), null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponseDto result = productService.updateProduct(1L, updateDto);

        assertThat(result.price()).isEqualByComparingTo("22.00");
        assertThat(result.stock()).isEqualTo(50);
        verify(productRepository).save(existingProduct);
    }

    @Test
    @DisplayName("Debe actualizar solo el stock manteniendo el precio existente")
    void shouldUpdateOnlyStockSuccessfully() {
        ProductUpdateDto updateDto = new ProductUpdateDto(null, 80);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponseDto result = productService.updateProduct(1L, updateDto);

        assertThat(result.price()).isEqualByComparingTo("18.50");
        assertThat(result.stock()).isEqualTo(80);
        verify(productRepository).save(existingProduct);
    }

    @Test
    @DisplayName("Debe actualizar tanto precio como stock simultáneamente")
    void shouldUpdateBothPriceAndStockSuccessfully() {
        ProductUpdateDto updateDto = new ProductUpdateDto(new BigDecimal("25.00"), 100);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponseDto result = productService.updateProduct(1L, updateDto);

        assertThat(result.price()).isEqualByComparingTo("25.00");
        assertThat(result.stock()).isEqualTo(100);
        verify(productRepository).save(existingProduct);
    }

    @Test
    @DisplayName("Debe rechazar con InvalidStockException cuando se intenta actualizar stock a valor negativo")
    void shouldThrowInvalidStockExceptionWhenStockNegative() {
        ProductUpdateDto updateDto = new ProductUpdateDto(null, -10);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        assertThatThrownBy(() -> productService.updateProduct(1L, updateDto))
                .isInstanceOf(InvalidStockException.class)
                .hasMessage("El stock no puede fijarse en un valor negativo");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe rechazar con IllegalArgumentException cuando el payload no incluye ni precio ni stock")
    void shouldThrowIllegalArgumentExceptionWhenPayloadEmpty() {
        ProductUpdateDto updateDto = new ProductUpdateDto(null, null);

        assertThatThrownBy(() -> productService.updateProduct(1L, updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Debe proporcionar al menos el precio o el stock para actualizar el producto");

        verify(productRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Debe lanzar ProductNotFoundException cuando el producto no existe")
    void shouldThrowProductNotFoundExceptionWhenIdMissing() {
        ProductUpdateDto updateDto = new ProductUpdateDto(new BigDecimal("20.00"), 40);

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(999L, updateDto))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Producto con ID 999 no encontrado en el catálogo");
    }
}
