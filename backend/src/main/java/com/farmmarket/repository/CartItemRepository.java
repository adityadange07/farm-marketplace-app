package com.farmmarket.repository;

import com.farmmarket.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    @Query("SELECT c FROM CartItem c " +
            "JOIN FETCH c.product p " +
            "JOIN FETCH p.farm " +
            "LEFT JOIN FETCH p.images " +
            "WHERE c.user.id = :userId")
    List<CartItem> findByUserIdWithProduct(@Param("userId") UUID userId);

    List<CartItem> findByUserId(UUID userId);

    Optional<CartItem> findByUserIdAndProductId(UUID userId, UUID productId);

    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    @Modifying
    @Transactional
    void deleteByUserId(UUID userId);

    @Modifying
    @Transactional
    void deleteByUserIdAndProductId(UUID userId, UUID productId);

    @Query("SELECT COUNT(c) FROM CartItem c WHERE c.user.id = :userId")
    int countByUserId(@Param("userId") UUID userId);
}