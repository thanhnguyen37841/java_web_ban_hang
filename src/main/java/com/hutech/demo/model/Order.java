package com.hutech.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    // Nhiệm vụ 15: Thêm thông tin địa chỉ/SĐT
    private String address;
    private String phone;
    private String email;
    private String note;
    private String paymentMethod;
    private LocalDateTime orderDate;

    private String status; // PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELLED

    private double totalAmount;

    private String couponCode;
    private double discountAmount;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }

    public String getStatusDisplay() {
        if (status == null) return "Không xác định";
        return switch (status) {
            case "PENDING" -> "Chờ xác nhận";
            case "CONFIRMED" -> "Đã xác nhận";
            case "SHIPPING" -> "Đang giao";
            case "COMPLETED" -> "Hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }

    public String getStatusBadgeClass() {
        if (status == null) return "bg-secondary";
        return switch (status) {
            case "PENDING" -> "bg-warning text-dark";
            case "CONFIRMED" -> "bg-info";
            case "SHIPPING" -> "bg-primary";
            case "COMPLETED" -> "bg-success";
            case "CANCELLED" -> "bg-danger";
            default -> "bg-secondary";
        };
    }
}
