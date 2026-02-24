package com.saikumar.productservice.service;

import com.saikumar.productservice.exception.ProductNotFoundException;
import com.saikumar.productservice.exception.InsufficientStockException;
import com.saikumar.productservice.model.Product;
import com.saikumar.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    @Cacheable(value = "products", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    @Cacheable(value = "product", key = "#id")
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
    }

    public Page<Product> getByCategory(String category, Pageable pageable) {
        return productRepository.findByCategoryAndActiveTrue(category, pageable);
    }

    public Page<Product> search(String keyword, Pageable pageable) {
        return productRepository.searchByKeyword(keyword, pageable);
    }

    public List<Product> getProductsByIds(List<Long> ids) {
        return productRepository.findByIdIn(ids);
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public Product createProduct(Product product) {
        Product saved = productRepository.save(product);
        log.info("Product created: {} (id={})", saved.getName(), saved.getId());
        return saved;
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public Product updateProduct(Long id, Product updated) {
        Product existing = getProductById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setStock(updated.getStock());
        existing.setCategory(updated.getCategory());
        existing.setBrand(updated.getBrand());
        existing.setImageUrl(updated.getImageUrl());
        return productRepository.save(existing);
    }

    @Transactional
    @CacheEvict(value = "product", key = "#productId")
    public void reduceStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));

        if (product.getStock() < quantity) {
            throw new InsufficientStockException(
                "Not enough stock for product " + productId + ". Available: " + product.getStock());
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        log.info("Stock reduced for product {}: {} -> {}", productId, product.getStock() + quantity, product.getStock());
    }

    @Transactional
    @CacheEvict(value = "product", key = "#productId")
    public void restoreStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
        log.info("Stock restored for product {}: +{}", productId, quantity);
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.setActive(false); // soft delete
        productRepository.save(product);
    }
}
