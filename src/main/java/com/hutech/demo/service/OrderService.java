package com.hutech.demo.service;

import com.hutech.demo.model.*;
import com.hutech.demo.repository.OrderDetailRepository;
import com.hutech.demo.repository.OrderRepository;
import com.hutech.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartService cartService;
    private final ProductService productService;
    private final UserRepository userRepository;

    // Luồng trạng thái hợp lệ
    private static final Map<String, List<String>> VALID_TRANSITIONS = Map.of(
        "PENDING", List.of("CONFIRMED", "CANCELLED"),
        "CONFIRMED", List.of("SHIPPING", "CANCELLED"),
        "SHIPPING", List.of("COMPLETED"),
        "COMPLETED", List.of(),
        "CANCELLED", List.of()
    );

    public Order createOrder(String customerName, String address, String phone,
                             String email, String note, String paymentMethod,
                             List<CartItem> cartItems, User user,
                             String couponCode, double discountAmount) {
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setAddress(address);
        order.setPhone(phone);
        order.setEmail(email);
        order.setNote(note);
        order.setPaymentMethod(paymentMethod);
        order.setStatus("PENDING");
        order.setCouponCode(couponCode);
        order.setDiscountAmount(discountAmount);

        if (user != null) {
            order.setUser(user);
        }

        // Tính tổng tiền
        double totalAmount = cartItems.stream()
            .mapToDouble(CartItem::getTotalPrice)
            .sum();
        order.setTotalAmount(totalAmount);

        orderRepository.save(order);

        // Tạo chi tiết đơn hàng và trừ tồn kho
        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(item.getProduct());
            detail.setQuantity(item.getQuantity());
            detail.setPrice(item.getProduct().getEffectivePrice());
            orderDetailRepository.save(detail);

            // Trừ tồn kho
            productService.reduceStock(item.getProduct().getId(), item.getQuantity());
        }

        cartService.clearCart();
        return order;
    }

    public boolean updateOrderStatus(Long orderId, String newStatus) {
        Optional<Order> opt = orderRepository.findById(orderId);
        if (opt.isEmpty()) return false;

        Order order = opt.get();
        String currentStatus = order.getStatus();

        // Validate luồng chuyển trạng thái
        List<String> validNext = VALID_TRANSITIONS.getOrDefault(currentStatus, List.of());
        if (!validNext.contains(newStatus)) {
            return false;
        }

        // Nếu hủy đơn -> hoàn trả tồn kho
        if ("CANCELLED".equals(newStatus) && order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                productService.restoreStock(detail.getProduct().getId(), detail.getQuantity());
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
        return true;
    }

    public boolean cancelOrder(Long orderId, Long userId) {
        Optional<Order> opt = orderRepository.findById(orderId);
        if (opt.isEmpty()) return false;

        Order order = opt.get();

        // Chỉ cho hủy khi PENDING
        if (!"PENDING".equals(order.getStatus())) return false;

        // Kiểm tra đúng user
        if (order.getUser() != null && !order.getUser().getId().equals(userId)) return false;

        return updateOrderStatus(orderId, "CANCELLED");
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    // Dashboard statistics
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orderRepository.count());
        stats.put("pendingOrders", orderRepository.countByStatus("PENDING"));
        stats.put("confirmedOrders", orderRepository.countByStatus("CONFIRMED"));
        stats.put("shippingOrders", orderRepository.countByStatus("SHIPPING"));
        stats.put("completedOrders", orderRepository.countByStatus("COMPLETED"));
        stats.put("cancelledOrders", orderRepository.countByStatus("CANCELLED"));
        stats.put("totalRevenue", orderRepository.getTotalRevenue());
        stats.put("totalSales", orderRepository.getTotalSales());
        stats.put("totalCustomers", userRepository.countByRolesName("USER"));
        return stats;
    }
}
