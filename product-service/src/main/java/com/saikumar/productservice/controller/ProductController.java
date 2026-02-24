package com.saikumar.productservice.controller;

import com.saikumar.productservice.model.Product;
import com.saikumar.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // GET /api/products?page=0&size=10&sort=price,asc
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    // GET /api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // GET /api/products/category/{category}
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<Product>> getByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getByCategory(category, pageable));
    }

    // GET /api/products/search?keyword=laptop
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.search(keyword, pageable));
    }

    // POST /api/products/batch - internal use by order service
    @PostMapping("/batch")
    public ResponseEntity<List<Product>> getProductsByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(productService.getProductsByIds(ids));
    }

    // POST /api/products - admin
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(product));
    }

    // PUT /api/products/{id} - admin
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    // PUT /api/products/{id}/stock/reduce - internal use by order service
    @PutMapping("/{id}/stock/reduce")
    public ResponseEntity<Void> reduceStock(
            @PathVariable Long id,
            @RequestParam int quantity) {
        productService.reduceStock(id, quantity);
        return ResponseEntity.ok().build();
    }

    // PUT /api/products/{id}/stock/restore - used when order is cancelled
    @PutMapping("/{id}/stock/restore")
    public ResponseEntity<Void> restoreStock(
            @PathVariable Long id,
            @RequestParam int quantity) {
        productService.restoreStock(id, quantity);
        return ResponseEntity.ok().build();
    }

    // DELETE /api/products/{id} - admin, soft delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("product-service ok");
    }
}
