package com.hutech.demo.controller;

import com.hutech.demo.model.Coupon;
import com.hutech.demo.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final CouponService couponService;

    // === Dashboard ===
    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("stats", orderService.getDashboardStats());
        model.addAttribute("recentOrders", orderService.getAllOrders().stream().limit(10).toList());
        model.addAttribute("lowStockProducts", productService.getLowStockProducts(5));
        return "admin/dashboard";
    }

    // === Quản lý sản phẩm ===
    @GetMapping("/products")
    public String manageProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/products";
    }

    // === Quản lý đơn hàng ===
    @GetMapping("/orders")
    public String manageOrders(@RequestParam(required = false) String status, Model model) {
        if (status != null && !status.isBlank()) {
            model.addAttribute("orders", orderService.getOrdersByStatus(status));
            model.addAttribute("selectedStatus", status);
        } else {
            model.addAttribute("orders", orderService.getAllOrders());
        }
        return "admin/orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                     @RequestParam String status,
                                     RedirectAttributes redirectAttributes) {
        boolean success = orderService.updateOrderStatus(id, status);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể chuyển sang trạng thái này!");
        }
        return "redirect:/admin/orders";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers().stream()
                .filter(u -> u.getRoles().stream().noneMatch(r -> r.getName().equals("ADMIN")))
                .toList());
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle-lock")
    public String toggleUserLock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean success = userService.toggleLock(id);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái tài khoản thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thay đổi trạng thái tài khoản Admin!");
        }
        return "redirect:/admin/users";
    }

    // === Quản lý đánh giá ===
    @GetMapping("/reviews")
    public String manageReviews(Model model) {
        model.addAttribute("reviews", reviewService.getAllReviews());
        model.addAttribute("pendingCount", reviewService.getPendingReviews().size());
        return "admin/reviews";
    }

    @PostMapping("/reviews/{id}/approve")
    public String approveReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reviewService.approveReview(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt đánh giá!");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reviewService.deleteReview(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đánh giá!");
        return "redirect:/admin/reviews";
    }

    // === Quản lý khuyến mãi ===
    @GetMapping("/coupons")
    public String manageCoupons(Model model) {
        model.addAttribute("coupons", couponService.getAllCoupons());
        model.addAttribute("coupon", new Coupon());
        return "admin/coupons";
    }

    @PostMapping("/coupons/add")
    public String addCoupon(@Valid @ModelAttribute Coupon coupon,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("coupons", couponService.getAllCoupons());
            return "admin/coupons";
        }
        try {
            couponService.createCoupon(coupon);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo mã giảm giá thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/coupons/{id}/delete")
    public String deleteCoupon(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        couponService.deleteCoupon(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa mã giảm giá!");
        return "redirect:/admin/coupons";
    }

    @PostMapping("/coupons/{id}/toggle")
    public String toggleCoupon(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        couponService.getCouponById(id).ifPresent(coupon -> {
            coupon.setActive(!coupon.isActive());
            couponService.updateCoupon(coupon);
        });
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái mã giảm giá!");
        return "redirect:/admin/coupons";
    }

    // === Quản lý tồn kho ===
    @GetMapping("/inventory")
    public String manageInventory(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("lowStockProducts", productService.getLowStockProducts(5));
        return "admin/inventory";
    }

    @PostMapping("/inventory/{id}/update")
    public String updateStock(@PathVariable Long id,
                               @RequestParam int stockQuantity,
                               RedirectAttributes redirectAttributes) {
        boolean success = productService.updateStock(id, stockQuantity);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tồn kho thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm!");
        }
        return "redirect:/admin/inventory";
    }
}
