package com.farmmarket.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class ProductResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private String unit;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private Integer maxOrderQuantity;
    private Boolean isOrganic;
    private Boolean isSeasonal;
    private String status;
    private BigDecimal avgRating;
    private Integer reviewCount;
    private Integer totalSold;
    private Integer viewCount;
    private String growingMethod;
    private LocalDate harvestDate;
    private Double distanceKm;
    private List<ImageResponse> images;
    private FarmSummary farm;
    private CategorySummary category;
    private LocalDateTime createdAt;

    @Data @Builder
    public static class ImageResponse {
        private String url;
        private String altText;
        private Boolean isPrimary;
    }

    @Data @Builder
    public static class FarmSummary {
        private UUID id;
        private String farmName;
        private String city;
        private String state;
        private Boolean isOrganic;
        private BigDecimal rating;
    }

    @Data @Builder
    public static class CategorySummary {
        private UUID id;
        private String name;
        private String slug;
    }
}