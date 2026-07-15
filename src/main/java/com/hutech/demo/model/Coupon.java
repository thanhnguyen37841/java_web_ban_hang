package com.hutech.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Mã giảm giá không được để trống")
    @Column(unique = true, nullable = false)
    private String code;

    @NotBlank(message = "Loại giảm giá không được để trống")
    private String discountType; // PERCENT hoặc FIXED

    @Min(value = 1, message = "Giá trị giảm phải lớn hơn 0")
    private double discountValue;

    @Min(value = 0, message = "Đơn tối thiểu không hợp lệ")
    private double minOrderAmount;

    // Giảm tối đa (chỉ áp dụng với PERCENT)
    private Double maxDiscount;

    @Min(value = 0)
    private int maxUsage;

    @Min(value = 0)
    private int usedCount;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private boolean active = true;

    @Version
    private Long version;

    /**
     * Kiểm tra coupon còn hợp lệ không
     */
    public boolean isValid() {

        if (!active)
            return false;

        LocalDateTime now = LocalDateTime.now();

        if (startDate != null && now.isBefore(startDate))
            return false;

        if (endDate != null && now.isAfter(endDate))
            return false;

        if (maxUsage > 0 && usedCount >= maxUsage)
            return false;

        return true;
    }

    /**
     * Tính tiền được giảm
     */
    public double calculateDiscount(double orderTotal) {

        if (!isValid())
            return 0;

        if (orderTotal < minOrderAmount)
            return 0;

        double discount;

        if ("PERCENT".equalsIgnoreCase(discountType)) {

            discount = orderTotal * discountValue / 100.0;

            if (maxDiscount != null) {
                discount = Math.min(discount, maxDiscount);
            }



        } else if ("FIXED".equalsIgnoreCase(discountType)) {

            discount = discountValue;

        } else {

            return 0;

        }

        return Math.min(discount, orderTotal);
    }

}