package com.hutech.demo.service;

import com.hutech.demo.model.CartItem;
import com.hutech.demo.model.Product;
import com.hutech.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CartService {
    private List<CartItem> cartItems = new ArrayList<>();
    private String appliedCouponCode;
    private double discountAmount;

    @Autowired
    private ProductRepository productRepository;

    public void addToCart(Long productId, int quantity) {
        Optional<Product> optProduct = productRepository.findById(productId);
        if (optProduct.isEmpty()) return;
        Product product = optProduct.get();

        // Kiểm tra tồn kho
        int currentInCart = 0;
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(productId)) {
                currentInCart = item.getQuantity();
                break;
            }
        }

        // Ngăn chặn thêm quá số lượng tồn kho
        if (currentInCart + quantity > product.getStockQuantity()) {
            quantity = product.getStockQuantity() - currentInCart;
            if (quantity <= 0) return;
        }

        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        cartItems.add(new CartItem(product, quantity));
    }

    public boolean updateQuantity(Long productId, int quantity) {
        if (quantity <= 0) {
            removeFromCart(productId);
            return true;
        }

        // Kiểm tra tồn kho
        Optional<Product> optProduct = productRepository.findById(productId);
        if (optProduct.isEmpty()) return false;
        Product product = optProduct.get();

        if (quantity > product.getStockQuantity()) {
            return false; // Vượt quá tồn kho
        }

        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(quantity);
                return true;
            }
        }
        return false;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void removeFromCart(Long productId) {
        cartItems.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void clearCart() {
        cartItems.clear();
        appliedCouponCode = null;
        discountAmount = 0;
    }

    public double getTotalPrice() {
        return cartItems.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    public double getFinalPrice() {
        return Math.max(0, getTotalPrice() - discountAmount);
    }

    public int getCartCount() {
        return cartItems.stream().mapToInt(CartItem::getQuantity).sum();
    }

    // Coupon support
    public String getAppliedCouponCode() {
        return appliedCouponCode;
    }

    public void setAppliedCouponCode(String couponCode) {
        this.appliedCouponCode = couponCode;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public void removeCoupon() {
        this.appliedCouponCode = null;
        this.discountAmount = 0;
    }
}
