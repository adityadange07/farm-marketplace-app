package com.farmmarket.repository;

import com.farmmarket.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByProductIdAndIsVisibleTrueOrderByCreatedAtDesc(
            UUID productId, Pageable pageable);

    Page<Review> findByFarmIdAndIsVisibleTrueOrderByCreatedAtDesc(
            UUID farmId, Pageable pageable);

    boolean existsByOrderIdAndProductIdAndConsumerId(
            UUID orderId, UUID productId, UUID consumerId);

    @Query("SELECT AVG(r.rating) FROM Review r " +
            "WHERE r.product.id = :productId AND r.isVisible = true")
    Double getAverageRatingByProductId(@Param("productId") UUID productId);

    @Query("SELECT COUNT(r) FROM Review r " +
            "WHERE r.product.id = :productId AND r.isVisible = true")
    int countByProductId(@Param("productId") UUID productId);

    @Query("SELECT AVG(r.rating) FROM Review r " +
            "WHERE r.farm.id = :farmId AND r.isVisible = true")
    Double getAverageRatingByFarmId(@Param("farmId") UUID farmId);
}