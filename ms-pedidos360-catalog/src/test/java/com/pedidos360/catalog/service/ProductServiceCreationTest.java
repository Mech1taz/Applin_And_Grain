package com.pedidos360.catalog.service;

import com.pedidos360.catalog.dto.ProductCreateDto;
import com.pedidos360.catalog.dto.ProductResponseDto;
import com.pedidos360.catalog.entity.Product;
import com.pedidos360.catalog.exception.InvalidStockException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceCreationTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product savedProduct;

    @BeforeEach
    void setUp() {
        savedProduct = Product.builder()
                .id(1L)
                .name("Café Colombiano Molido 500g")
                .description("Café premium de altura")
                .price(new BigDecimal("18.50"))
                .stock(50)
                .build();
    }

    @Test
    @DisplayName("Debe crear producto exitosamente cuando los datos son válidos y el stock es >= 0")
    void shouldCreateProductSuccessfully() {
        ProductCreateDto createDto = new ProductCreateDto(
                "Café Colombiano Molido 500g",
                "Café premium de altura",
                new BigDecimal("18.50"),
                50
        );

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponseDto result = productService.createProduct(createDto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Café Colombiano Molido 500g");
        assertThat(result.price()).isEqualByComparingTo("18.50");
        assertThat(result.stock()).isEqualTo(50);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe lanzar InvalidStockException cuando se intenta crear un producto con stock negativo")
    void shouldThrowExceptionWhenCreatingWithNegativeStock() {
        ProductCreateDto createDto = new ProductCreateDto(
                "Café Inválido",
                "Descripción",
                new BigDecimal("10.00"),
                -1
        );

        assertThatThrownBy(() -> productService.createProduct(createDto))
                .isInstanceOf(InvalidStockException.class)
                .hasMessage("El stock no puede fijarse en un valor negativo");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe lanzar InvalidStockException cuando el stock es null")
    void shouldThrowExceptionWhenCreatingWithNullStock() {
        ProductCreateDto createDto = new ProductCreateDto(
                "Café Inválido",
                "Descripción",
                new BigDecimal("10.00"),
                null
        );

        assertThatThrownBy(() -> productService.createProduct(createDto))
                .isInstanceOf(InvalidStockException.class)
                .hasMessage("El stock no puede fijarse en un valor negativo");

        verify(productRepository, never()).save(any(Product.class));
    }
}
