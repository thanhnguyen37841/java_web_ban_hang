package com.hutech.demo.controller;

import com.hutech.demo.model.Order;
import com.hutech.demo.model.User;
import com.hutech.demo.service.CartService;
import com.hutech.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;
    private final CartService cartService;

    @GetMapping("/checkout")
    public String showCheckout(Model model, @AuthenticationPrincipal User user) {
        if (cartService.getCartItems().isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        model.addAttribute("discountAmount", cartService.getDiscountAmount());
        model.addAttribute("finalPrice", cartService.getFinalPrice());
        model.addAttribute("appliedCoupon", cartService.getAppliedCouponCode());

        // Tự động điền thông tin user
        if (user != null) {
            model.addAttribute("userFullName", user.getFullName() != null ? user.getFullName() : user.getUsername());
            model.addAttribute("userPhone", user.getPhone());
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("userAddress", user.getAddress());
        }

        return "cart/checkout";
    }

    @PostMapping("/submit")
    public String submitOrder(@RequestParam String customerName,
                               @RequestParam String address,
                               @RequestParam String phone,
                               @RequestParam String email,
                               @RequestParam(required = false) String note,
                               @RequestParam String paymentMethod,
                               @AuthenticationPrincipal User user,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (cartService.getCartItems().isEmpty()) {
            return "redirect:/cart";
        }

        Order order = orderService.createOrder(
            customerName, address, phone, email, note, paymentMethod,
            cartService.getCartItems(), user,
            cartService.getAppliedCouponCode(),
            cartService.getDiscountAmount()
        );

        model.addAttribute("order", order);
        return "cart/order-confirmation";
    }

    @GetMapping("/history")
    public String orderHistory(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("orders", orderService.getOrdersByUser(user.getId()));
        return "order/order-history";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id,
                               @AuthenticationPrincipal User user,
                               Model model) {
        Order order = orderService.getOrderById(id)
            .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        // Kiểm tra quyền xem đơn hàng
        if (order.getUser() != null && !order.getUser().getId().equals(user.getId())) {
            // Kiểm tra nếu là admin
            boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getName()));
            if (!isAdmin) {
                return "redirect:/order/history";
            }
        }

        model.addAttribute("order", order);
        return "order/order-detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id,
                               @AuthenticationPrincipal User user,
                               RedirectAttributes redirectAttributes) {
        boolean success = orderService.cancelOrder(id, user.getId());
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng. Chỉ có thể hủy đơn đang ở trạng thái 'Chờ xác nhận'.");
        }
        return "redirect:/order/history";
    }

    @GetMapping("/confirmation")
    public String confirmation() {
        return "cart/order-confirmation";
    }
}
