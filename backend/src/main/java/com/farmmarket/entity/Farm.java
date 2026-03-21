package com.farmmarket.entity;

import com.farmmarket.config.JsonConverter;
import com.farmmarket.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "farms")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Farm {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false, columnDefinition = "CHAR(36)")
    private User farmer;

    @Column(name = "farm_name", nullable = false)
    private String farmName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "farm_size_acres", precision = 10, scale = 2)
    private BigDecimal farmSizeAcres;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(length = 100)
    @Builder.Default
    private String country = "US";

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "is_organic", nullable = false)
    @Builder.Default
    private Boolean isOrganic = false;

    // MySQL JSON column — stored as String, parsed manually or use converter
    @Convert(converter = JsonConverter.ListStringMapConverter.class)
    @Column(columnDefinition = "JSON")
    @Builder.Default
    private List<Map<String, String>> certifications = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "delivery_radius_km", nullable = false)
    @Builder.Default
    private Integer deliveryRadiusKm = 50;

    @Column(name = "minimum_order", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minimumOrder = BigDecimal.ZERO;

    @Column(nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "total_reviews", nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "banner_image", columnDefinition = "TEXT")
    private String bannerImage;

    @Column(name = "stripe_account_id")
    private String stripeAccountId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL)
    private List<Product> products;
}