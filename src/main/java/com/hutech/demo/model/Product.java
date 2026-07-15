package com.hutech.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @Min(value = 0, message = "Giá không được âm")
    private double price;

    @Min(value = 0, message = "Giá khuyến mãi không được âm")
    private double salePrice;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    private String brand;

    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private int stockQuantity;

    private boolean featured;

    private boolean active = true;

    private int viewCount;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (!active) active = true;
    }

    public double getEffectivePrice() {
        return (salePrice > 0 && salePrice < price) ? salePrice : price;
    }

    public boolean isOnSale() {
        return salePrice > 0 && salePrice < price;
    }

    public boolean isInStock() {
        return stockQuantity > 0;
    }

    public double getDiscountPercent() {
        if (!isOnSale()) return 0;
        return Math.round((1 - salePrice / price) * 100);
    }
}
