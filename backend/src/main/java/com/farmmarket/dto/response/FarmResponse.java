package com.farmmarket.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data @Builder
public class FarmResponse {
    private UUID id;
    private UUID farmerId;
    private String farmName;
    private String description;
    private String addressLine1;
    private String city;
    private String state;
    private String zipCode;
    private Double latitude;
    private Double longitude;
    private Boolean isOrganic;
    private String verificationStatus;
    private Integer deliveryRadiusKm;
    private BigDecimal minimumOrder;
    private BigDecimal rating;
    private Integer totalReviews;
    private String bannerImage;
    private List<Map<String, String>> certifications;
    private Double distanceKm;
    private Integer productCount;
    private LocalDateTime createdAt;
}