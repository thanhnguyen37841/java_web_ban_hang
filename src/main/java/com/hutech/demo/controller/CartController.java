package com.hutech.demo.controller;

import com.hutech.demo.service.CartService;
import com.hutech.demo.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private final CouponService couponService;

    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        model.addAttribute("discountAmount", cartService.getDiscountAmount());
        model.addAttribute("finalPrice", cartService.getFinalPrice());
        model.addAttribute("appliedCoupon", cartService.getAppliedCouponCode());
        return "cart/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                             @RequestParam(defaultValue = "1") int quantity,
                             RedirectAttributes redirectAttributes) {
        cartService.addToCart(productId, quantity);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng!");
        // Quay lại trang trước (referer) hoặc trang sản phẩm
        return "redirect:/products";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long productId,
                                 @RequestParam int quantity,
                                 RedirectAttributes redirectAttributes) {
        boolean success = cartService.updateQuantity(productId, quantity);
        if (!success) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số lượng vượt quá tồn kho!");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật số lượng!");
        }
        return "redirect:/cart";
    }

    @GetMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId, RedirectAttributes redirectAttributes) {
        cartService.removeFromCart(productId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng!");
        return "redirect:/cart";
    }

    @GetMapping("/clear")
    public String clearCart() {
        cartService.clearCart();
        return "redirect:/cart";
    }

    @PostMapping("/apply-coupon")
    public String applyCoupon(@RequestParam String couponCode,
                               RedirectAttributes redirectAttributes) {
        double totalPrice = cartService.getTotalPrice();
        double discount = couponService.applyCoupon(couponCode, totalPrice);

        if (discount == -1) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã giảm giá không hợp lệ hoặc đã hết hạn!");
        } else if (discount == -2) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng chưa đủ điều kiện để sử dụng mã giảm giá này!");
        } else {
            cartService.setAppliedCouponCode(couponCode.toUpperCase().trim());
            cartService.setDiscountAmount(discount);
            redirectAttributes.addFlashAttribute("successMessage",
                String.format("Áp dụng mã giảm giá thành công! Giảm %,.0fđ", discount));
        }
        return "redirect:/cart";
    }

    @GetMapping("/remove-coupon")
    public String removeCoupon(RedirectAttributes redirectAttributes) {
        cartService.removeCoupon();
        redirectAttributes.addFlashAttribute("successMessage", "Đã gỡ mã giảm giá!");
        return "redirect:/cart";
    }
}
