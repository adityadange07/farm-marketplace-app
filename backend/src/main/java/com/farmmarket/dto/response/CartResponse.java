package com.farmmarket.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class CartResponse {
    private List<CartItemResponse> items;
    private int totalItems;
    private BigDecimal subtotal;

    @Data @Builder
    public static class CartItemResponse {
        private UUID id;
        private UUID productId;
        private String productName;
        private String productSlug;
        private String productImage;
        private BigDecimal price;
        private String unit;
        private Integer quantity;
        private Integer stockQuantity;
        private BigDecimal lineTotal;
        private FarmSummary farm;
    }

    @Data @Builder
    public static class FarmSummary {
        private UUID id;
        private String farmName;
    }
}