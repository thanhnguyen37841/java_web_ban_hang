package com.hutech.demo.service;

import com.hutech.demo.model.Coupon;
import com.hutech.demo.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {

    private final CouponRepository couponRepository;

    /**
     * Validate dữ liệu coupon
     */
    private void validateCoupon(Coupon coupon) {

        coupon.setCode(coupon.getCode().trim().toUpperCase());

        if (!coupon.getDiscountType().equalsIgnoreCase("PERCENT")
                && !coupon.getDiscountType().equalsIgnoreCase("FIXED")) {

            throw new RuntimeException("discountType chỉ được là PERCENT hoặc FIXED");
        }

        if (coupon.getDiscountValue() <= 0) {
            throw new RuntimeException("Giá trị giảm phải lớn hơn 0");
        }

        if (coupon.getDiscountType().equalsIgnoreCase("PERCENT")
                && coupon.getDiscountValue() > 100) {

            throw new RuntimeException("Giảm theo % không được vượt quá 100%");
        }

        if (coupon.getMaxDiscount() != null
                && coupon.getMaxDiscount() < 0) {

            throw new RuntimeException("Giảm tối đa phải lớn hơn hoặc bằng 0");
        }

        if (coupon.getMinOrderAmount() < 0) {
            throw new RuntimeException("Đơn tối thiểu không hợp lệ");
        }

        if (coupon.getMinOrderAmount() < 0) {
            throw new RuntimeException("Đơn tối thiểu không hợp lệ");
        }

        if (coupon.getMaxUsage() < 0) {
            throw new RuntimeException("Số lượt sử dụng không hợp lệ");
        }

        if (coupon.getStartDate() != null &&
                coupon.getEndDate() != null &&
                coupon.getStartDate().isAfter(coupon.getEndDate())) {

            throw new RuntimeException("Ngày bắt đầu phải trước ngày kết thúc");
        }

    }

    /**
     * Tạo coupon
     */
    public Coupon createCoupon(Coupon coupon) {

        validateCoupon(coupon);

        if (couponRepository.existsByCode(coupon.getCode())) {

            throw new RuntimeException(
                    "Mã giảm giá đã tồn tại: " + coupon.getCode());

        }

        coupon.setUsedCount(0);

        return couponRepository.save(coupon);

    }

    public Optional<Coupon> findByCode(String code) {

        return couponRepository.findByCode(code.trim().toUpperCase());

    }

    /**
     * Áp dụng coupon
     *
     * return:
     * >=0 : số tiền giảm
     * -1 : coupon không tồn tại
     * -2 : coupon hết hiệu lực
     * -3 : đơn hàng chưa đủ điều kiện
     */
    public double applyCoupon(String code, double orderTotal) {

        if (orderTotal <= 0)
            throw new RuntimeException("Đơn hàng không hợp lệ");

        Optional<Coupon> opt =
                couponRepository.findByCode(code.trim().toUpperCase());

        if (opt.isEmpty())
            return -1;

        Coupon coupon = opt.get();

        if (!coupon.isValid())
            return -2;

        if (orderTotal < coupon.getMinOrderAmount())
            return -3;

        double discount = coupon.calculateDiscount(orderTotal);

        coupon.setUsedCount(coupon.getUsedCount() + 1);

        couponRepository.save(coupon);

        return discount;

    }

    public List<Coupon> getAllCoupons() {

        return couponRepository.findAll();

    }

    public Optional<Coupon> getCouponById(Long id) {

        return couponRepository.findById(id);

    }

    /**
     * Cập nhật coupon
     */
    public void updateCoupon(Coupon coupon) {

        validateCoupon(coupon);

        Coupon existing = couponRepository.findById(coupon.getId())

                .orElseThrow(() ->
                        new RuntimeException("Coupon không tồn tại"));

        Optional<Coupon> other =
                couponRepository.findByCode(coupon.getCode());

        if (other.isPresent() &&
                !other.get().getId().equals(coupon.getId())) {

            throw new RuntimeException("Mã coupon đã tồn tại");

        }

        existing.setCode(coupon.getCode());
        existing.setDiscountType(coupon.getDiscountType());
        existing.setDiscountValue(coupon.getDiscountValue());
        existing.setMaxDiscount(coupon.getMaxDiscount());
        existing.setMinOrderAmount(coupon.getMinOrderAmount());
        existing.setMaxUsage(coupon.getMaxUsage());
        existing.setStartDate(coupon.getStartDate());
        existing.setEndDate(coupon.getEndDate());
        existing.setActive(coupon.isActive());

        couponRepository.save(existing);

    }

    public void deleteCoupon(Long id) {

        if (!couponRepository.existsById(id)) {

            throw new RuntimeException("Coupon không tồn tại");

        }

        couponRepository.deleteById(id);

    }

}