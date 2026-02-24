package com.saikumar.productservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    @Column(length = 1000)
    private String description;

    @NotNull
    @Min(value = 0, message = "Price cannot be negative")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock = 0;

    private String category;

    private String brand;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
