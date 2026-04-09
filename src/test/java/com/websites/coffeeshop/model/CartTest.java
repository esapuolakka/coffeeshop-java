package com.websites.coffeeshop.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CartTest {

    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = new Cart();
    }

    // ---- addItem ----

    @Test
    void addItemAddsNewEntryWhenProductNotInCart() {
        cart.addItem(1L, "Espresso Machine", BigDecimal.valueOf(100), 90.0, 1);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getProductId()).isEqualTo(1L);
    }

    @Test
    void addItemIncreasesQuantityWhenSameProductAddedAgain() {
        cart.addItem(1L, "Espresso Machine", BigDecimal.valueOf(100), 90.0, 1);
        cart.addItem(1L, "Espresso Machine", BigDecimal.valueOf(100), 90.0, 2);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    void addItemKeepsSeparateEntriesForDifferentProducts() {
        cart.addItem(1L, "Espresso Machine", BigDecimal.valueOf(100), 90.0, 1);
        cart.addItem(2L, "Coffee Grinder", BigDecimal.valueOf(50), 45.0, 1);

        assertThat(cart.getItems()).hasSize(2);
    }

    // ---- removeItem ----

    @Test
    void removeItemDeletesCorrectProduct() {
        cart.addItem(1L, "Espresso Machine", BigDecimal.valueOf(100), 90.0, 1);
        cart.addItem(2L, "Coffee Grinder", BigDecimal.valueOf(50), 45.0, 1);

        cart.removeItem(1L);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getProductId()).isEqualTo(2L);
    }

    @Test
    void removeItemDoesNothingWhenProductNotInCart() {
        cart.addItem(1L, "Espresso Machine", BigDecimal.valueOf(100), 90.0, 1);

        cart.removeItem(99L);

        assertThat(cart.getItems()).hasSize(1);
    }

    // ---- clear ----

    @Test
    void clearEmptiesAllItems() {
        cart.addItem(1L, "Espresso Machine", BigDecimal.valueOf(100), 90.0, 2);
        cart.addItem(2L, "Coffee Grinder", BigDecimal.valueOf(50), 45.0, 1);

        cart.clear();

        assertThat(cart.getItems()).isEmpty();
    }

    // ---- getTotalPrice without VIP ----

    @Test
    void getTotalPriceWithoutVipReturnsFullPriceTimesQuantity() {
        cart.addItem(1L, "Espresso Machine", BigDecimal.valueOf(100), 90.0, 3);

        BigDecimal total = cart.getTotalPrice(BigDecimal.valueOf(10), false);

        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(300));
    }

    @Test
    void getTotalPriceWithoutVipSumsMultipleItems() {
        cart.addItem(1L, "Espresso Machine", BigDecimal.valueOf(100), 90.0, 1);
        cart.addItem(2L, "Coffee Grinder", BigDecimal.valueOf(50), 45.0, 2);

        BigDecimal total = cart.getTotalPrice(BigDecimal.valueOf(10), false);

        // 100 + 50*2 = 200
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(200));
    }

    @Test
    void getTotalPriceEmptyCartReturnsZero() {
        BigDecimal total = cart.getTotalPrice(BigDecimal.valueOf(10), false);

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ---- getTotalPrice with VIP ----

    @Test
    void getTotalPriceWithVipAppliesDiscountPerItem() {
        // 100 - 10% = 90, quantity 1
        cart.addItem(1L, "Espresso Machine", BigDecimal.valueOf(100), 90.0, 1);

        BigDecimal total = cart.getTotalPrice(BigDecimal.valueOf(10), true);

        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(90.00));
    }

    @Test
    void getTotalPriceWithVipAppliesDiscountAndMultipliesQuantity() {
        // 100 - 10% = 90, quantity 3 → 270
        cart.addItem(1L, "Espresso Machine", BigDecimal.valueOf(100), 90.0, 3);

        BigDecimal total = cart.getTotalPrice(BigDecimal.valueOf(10), true);

        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(270.00));
    }

    @Test
    void getTotalPriceVipDiscountDifferentFromNonVip() {
        cart.addItem(1L, "Espresso Machine", BigDecimal.valueOf(200), 180.0, 1);

        BigDecimal vipTotal = cart.getTotalPrice(BigDecimal.valueOf(10), true);
        BigDecimal regularTotal = cart.getTotalPrice(BigDecimal.valueOf(10), false);

        assertThat(vipTotal).isLessThan(regularTotal);
    }
}
