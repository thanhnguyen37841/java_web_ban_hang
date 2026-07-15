package com.hutech.demo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return product.getEffectivePrice() * quantity;
    }

    public double getOriginalTotalPrice() {
        return product.getPrice() * quantity;
    }
}
