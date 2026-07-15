package com.hutech.demo.controller;

import com.hutech.demo.model.Product;
import com.hutech.demo.model.User;
import com.hutech.demo.service.CategoryService;
import com.hutech.demo.service.ProductService;
import com.hutech.demo.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;

    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            Model model) {

        model.addAttribute("products", productService.searchProducts(keyword, categoryId, minPrice, maxPrice, brand, sort));
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", productService.getAllBrands());

        // Giữ lại filter values trên form
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("selectedBrand", brand);
        model.addAttribute("selectedSort", sort);

        return "products/products-list";
    }

    // Trang chi tiết sản phẩm
    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Tăng lượt xem
        productService.incrementViewCount(id);

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviewService.getApprovedReviewsByProduct(id));
        model.addAttribute("averageRating", reviewService.getAverageRating(id));

        // Sản phẩm liên quan (cùng danh mục)
        if (product.getCategory() != null) {
            model.addAttribute("relatedProducts",
                productService.searchProducts(null, product.getCategory().getId(), null, null, null, "popular")
                    .stream().filter(p -> !p.getId().equals(id)).limit(4).toList());
        }

        return "products/product-detail";
    }

    // Thêm đánh giá
    @PostMapping("/{id}/review")
    public String addReview(@PathVariable Long id,
                            @RequestParam int rating,
                            @RequestParam String comment,
                            @AuthenticationPrincipal User user,
                            RedirectAttributes redirectAttributes) {
        reviewService.addReview(user, id, rating, comment);
        redirectAttributes.addFlashAttribute("successMessage", "Đánh giá của bạn đã được gửi và đang chờ duyệt!");
        return "redirect:/products/" + id;
    }

    // === ADMIN: Quản lý sản phẩm (giữ nguyên các endpoint cũ) ===
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "products/add-product";
    }

    @PostMapping("/add")
    public String addProduct(@Valid @ModelAttribute Product product,
                             BindingResult bindingResult,
                             @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
                             @RequestParam(value = "additionalImages", required = false) MultipartFile[] additionalImages,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            return "products/add-product";
        }
        productService.addProductWithImages(product, mainImage, additionalImages);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "products/update-product";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute Product product,
                                BindingResult bindingResult,
                                @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
                                @RequestParam(value = "additionalImages", required = false) MultipartFile[] additionalImages,
                                @RequestParam(value = "deleteImageIds", required = false) java.util.List<Long> deleteImageIds,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            return "products/update-product";
        }
        product.setId(id);
        productService.updateProductWithImages(product, mainImage, additionalImages, deleteImageIds);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
        return "redirect:/admin/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProductById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
        return "redirect:/admin/products";
    }
}
