package com.pedidos360.catalog.service.impl;

import com.pedidos360.catalog.dto.ProductCreateDto;
import com.pedidos360.catalog.dto.ProductResponseDto;
import com.pedidos360.catalog.dto.ProductUpdateDto;
import com.pedidos360.catalog.entity.Product;
import com.pedidos360.catalog.exception.InvalidStockException;
import com.pedidos360.catalog.exception.ProductNotFoundException;
import com.pedidos360.catalog.repository.ProductRepository;
import com.pedidos360.catalog.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductCreateDto dto) {
        if (dto.stock() == null || dto.stock() < 0) {
            throw new InvalidStockException("El stock no puede fijarse en un valor negativo");
        }

        Product product = Product.builder()
                .name(dto.name().trim())
                .description(dto.description())
                .price(dto.price())
                .stock(dto.stock())
                .build();

        Product saved = productRepository.save(product);
        return ProductResponseDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponseDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductUpdateDto dto) {
        if (!dto.hasAtLeastOneField()) {
            throw new IllegalArgumentException("Debe proporcionar al menos el precio o el stock para actualizar el producto");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (dto.stock() != null) {
            if (dto.stock() < 0) {
                throw new InvalidStockException("El stock no puede fijarse en un valor negativo");
            }
            product.setStock(dto.stock());
        }

        if (dto.price() != null) {
            product.setPrice(dto.price());
        }

        Product updated = productRepository.save(product);
        return ProductResponseDto.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        return productRepository.findById(id)
                .map(ProductResponseDto::fromEntity)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }
}
