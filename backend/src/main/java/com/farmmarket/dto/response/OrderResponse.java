package com.farmmarket.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data @Builder
public class OrderResponse {
    private UUID id;
    private String orderNumber;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal serviceFee;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal total;
    private String status;
    private String deliveryType;
    private Map<String, String> deliveryAddress;
    private String scheduledDate;
    private String scheduledTimeSlot;
    private String paymentStatus;
    private String consumerEmail;
    private List<OrderItemResponse> items;
    private FarmSummary farm;
    private ConsumerSummary consumer;
    private List<Map<String, Object>> statusHistory;
    private LocalDateTime placedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime deliveredAt;

    @Data @Builder
    public static class OrderItemResponse {
        private UUID id;
        private UUID productId;
        private String productName;
        private String productImage;
        private String unit;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }

    @Data @Builder
    public static class FarmSummary {
        private UUID id;
        private UUID farmerId;
        private String farmName;
        private String city;
        private String state;
        private String phone;
    }

    @Data @Builder
    public static class ConsumerSummary {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
    }
}