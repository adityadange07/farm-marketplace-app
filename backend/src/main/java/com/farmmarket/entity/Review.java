package com.farmmarket.entity;

import com.farmmarket.config.JsonConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"order_id", "product_id", "consumer_id"}
        ))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id")
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id")
    private User consumer;

    @Column(nullable = false, columnDefinition = "TINYINT")
    private Integer rating;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Convert(converter = JsonConverter.ListStringMapConverter.class)
    @Column(columnDefinition = "JSON")
    @Builder.Default
    private List<Map<String, String>> images = new ArrayList<>();

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = true;

    @Column(name = "is_visible")
    @Builder.Default
    private Boolean isVisible = true;

    @Column(name = "farmer_reply", columnDefinition = "TEXT")
    private String farmerReply;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}