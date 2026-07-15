package com.hutech.demo.service;

import com.hutech.demo.model.Product;
import com.hutech.demo.model.ProductImage;
import com.hutech.demo.repository.ProductImageRepository;
import com.hutech.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final FileStorageService fileStorageService;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    public Optional<Product> getProductById(Long id) {
        Optional<Product> opt = productRepository.findById(id);
        opt.ifPresent(product -> product.getImages().size());
        return opt;
    }

    public void addProduct(Product product) {
        productRepository.save(product);
    }

    public Product addProductWithImages(Product product, MultipartFile mainImage, MultipartFile[] additionalImages) {
        // Lưu ảnh chính
        if (mainImage != null && !mainImage.isEmpty()) {
            String fileName = fileStorageService.storeFile(mainImage);
            product.setImageUrl("/uploads/" + fileName);
        }
        productRepository.save(product);

        // Lưu ảnh phụ
        if (additionalImages != null) {
            for (MultipartFile file : additionalImages) {
                if (!file.isEmpty()) {
                    String fileName = fileStorageService.storeFile(file);
                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl("/uploads/" + fileName);
                    productImage.setProduct(product);
                    productImageRepository.save(productImage);
                }
            }
        }
        return product;
    }

    public void updateProduct(Product product) {
        Product existing = productRepository.findById(product.getId())
            .orElseThrow(() -> new RuntimeException("Product not found: " + product.getId()));
        existing.setName(product.getName());
        existing.setPrice(product.getPrice());
        existing.setSalePrice(product.getSalePrice());
        existing.setDescription(product.getDescription());
        existing.setImageUrl(product.getImageUrl());
        existing.setCategory(product.getCategory());
        existing.setBrand(product.getBrand());
        existing.setStockQuantity(product.getStockQuantity());
        existing.setFeatured(product.isFeatured());
        existing.setActive(product.isActive());
        productRepository.save(existing);
    }

    public void updateProductWithImages(Product product, MultipartFile mainImage, MultipartFile[] additionalImages, List<Long> deleteImageIds) {
        Product existing = productRepository.findById(product.getId())
            .orElseThrow(() -> new RuntimeException("Product not found: " + product.getId()));

        existing.setName(product.getName());
        existing.setPrice(product.getPrice());
        existing.setSalePrice(product.getSalePrice());
        existing.setDescription(product.getDescription());
        existing.setCategory(product.getCategory());
        existing.setBrand(product.getBrand());
        existing.setStockQuantity(product.getStockQuantity());
        existing.setFeatured(product.isFeatured());
        existing.setActive(product.isActive());

        if (mainImage != null && !mainImage.isEmpty()) {
            String fileName = fileStorageService.storeFile(mainImage);
            existing.setImageUrl("/uploads/" + fileName);
        }

        if (additionalImages != null) {
            for (MultipartFile file : additionalImages) {
                if (!file.isEmpty()) {
                    String fileName = fileStorageService.storeFile(file);
                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl("/uploads/" + fileName);
                    productImage.setProduct(existing);
                    productImageRepository.save(productImage);
                }
            }
        }

        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            existing.getImages().removeIf(img -> {
                if (deleteImageIds.contains(img.getId())) {
                    if (img.getImageUrl() != null && img.getImageUrl().startsWith("/uploads/")) {
                        String filename = img.getImageUrl().substring("/uploads/".length());
                        fileStorageService.deleteFile(filename);
                    }
                    return true;
                }
                return false;
            });
        }

        productRepository.save(existing);
    }

    public void deleteProductById(Long id) {
        productRepository.findById(id).ifPresent(product -> {
            product.setActive(false);
            productRepository.save(product);
        });
    }

    // Tìm kiếm & lọc đa điều kiện
    public List<Product> searchProducts(String keyword, Long categoryId,
                                         Double minPrice, Double maxPrice,
                                         String brand, String sortBy) {
        List<Product> results = productRepository.searchProducts(
            keyword != null && keyword.isBlank() ? null : keyword,
            categoryId,
            minPrice,
            maxPrice,
            brand != null && brand.isBlank() ? null : brand
        );

        // Sắp xếp
        if (sortBy != null) {
            switch (sortBy) {
                case "price_asc" -> results.sort(Comparator.comparingDouble(Product::getEffectivePrice));
                case "price_desc" -> results.sort(Comparator.comparingDouble(Product::getEffectivePrice).reversed());
                case "newest" -> results.sort(Comparator.comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
                case "popular" -> results.sort(Comparator.comparingInt(Product::getViewCount).reversed());
                case "name_asc" -> results.sort(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
            }
        }
        return results;
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndActiveTrue();
    }

    public List<Product> getNewProducts() {
        List<Product> products = productRepository.findByActiveTrueOrderByCreatedAtDesc();
        return products.stream().limit(8).toList();
    }

    public List<Product> getSaleProducts() {
        return productRepository.findBySalePriceGreaterThanAndActiveTrue(0);
    }

    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByStockQuantityLessThan(threshold);
    }

    public List<String> getAllBrands() {
        return productRepository.findAllBrands();
    }

    public void incrementViewCount(Long id) {
        productRepository.findById(id).ifPresent(product -> {
            product.setViewCount(product.getViewCount() + 1);
            productRepository.save(product);
        });
    }

    public boolean updateStock(Long productId, int quantity) {
        Optional<Product> opt = productRepository.findById(productId);
        if (opt.isEmpty()) return false;
        Product product = opt.get();
        product.setStockQuantity(quantity);
        productRepository.save(product);
        return true;
    }

    public boolean reduceStock(Long productId, int quantity) {
        Optional<Product> opt = productRepository.findById(productId);
        if (opt.isEmpty()) return false;
        Product product = opt.get();
        if (product.getStockQuantity() < quantity) return false;
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
        return true;
    }

    public void restoreStock(Long productId, int quantity) {
        productRepository.findById(productId).ifPresent(product -> {
            product.setStockQuantity(product.getStockQuantity() + quantity);
            productRepository.save(product);
        });
    }
}
