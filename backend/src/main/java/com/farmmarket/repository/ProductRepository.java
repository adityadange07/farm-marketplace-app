package com.farmmarket.repository;

import com.farmmarket.entity.Product;
import com.farmmarket.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    // ═══════════════════════════════════════════
    // FIND / SEARCH
    // ═══════════════════════════════════════════

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.farm f " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.slug = :slug AND p.status = 'ACTIVE'")
    Optional<Product> findBySlugWithDetails(@Param("slug") String slug);

    @Query("SELECT p FROM Product p " +
            "JOIN FETCH p.farm f " +
            "LEFT JOIN p.category c " +
            "WHERE p.status = 'ACTIVE' " +
            "AND (:category IS NULL OR c.slug = :category) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:isOrganic IS NULL OR p.isOrganic = :isOrganic)")
    Page<Product> findActiveProducts(
            @Param("category") String category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("isOrganic") Boolean isOrganic,
            Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "JOIN FETCH p.farm f " +
            "LEFT JOIN p.category c " +
            "WHERE p.status = 'ACTIVE' " +
            "AND (LOWER(p.name) LIKE LOWER(CONCAT('%',:search,'%')) " +
            "     OR LOWER(p.description) LIKE LOWER(CONCAT('%',:search,'%'))) " +
            "AND (:category IS NULL OR c.slug = :category) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:isOrganic IS NULL OR p.isOrganic = :isOrganic)")
    Page<Product> searchProducts(
            @Param("search") String search,
            @Param("category") String category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("isOrganic") Boolean isOrganic,
            Pageable pageable);

    // MySQL FULLTEXT
    @Query(value = """
        SELECT p.* FROM products p
        LEFT JOIN categories c ON p.category_id = c.id
        WHERE p.status = 'ACTIVE'
        AND MATCH(p.name, p.description, p.short_description)
            AGAINST(:search IN BOOLEAN MODE)
        AND (:category IS NULL OR c.slug = :category)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:isOrganic IS NULL OR p.is_organic = :isOrganic)
        """,
            countQuery = """
        SELECT COUNT(*) FROM products p
        LEFT JOIN categories c ON p.category_id = c.id
        WHERE p.status = 'ACTIVE'
        AND MATCH(p.name, p.description, p.short_description)
            AGAINST(:search IN BOOLEAN MODE)
        AND (:category IS NULL OR c.slug = :category)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:isOrganic IS NULL OR p.is_organic = :isOrganic)
        """,
            nativeQuery = true)
    Page<Product> searchProductsFulltext(
            @Param("search") String search,
            @Param("category") String category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("isOrganic") Boolean isOrganic,
            Pageable pageable);

    // MySQL Haversine
    @Query(value = """
        SELECT p.*,
            haversine_distance(f.latitude, f.longitude, :lat, :lng) AS distance_km
        FROM products p
        JOIN farms f ON p.farm_id = f.id
        LEFT JOIN categories c ON p.category_id = c.id
        WHERE p.status = 'ACTIVE'
        AND f.latitude IS NOT NULL AND f.longitude IS NOT NULL
        AND (:search IS NULL OR p.name LIKE CONCAT('%', :search, '%'))
        AND (:category IS NULL OR c.slug = :category)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:isOrganic IS NULL OR p.is_organic = :isOrganic)
        HAVING distance_km <= :radiusKm
        ORDER BY distance_km ASC
        """,
            countQuery = """
        SELECT COUNT(*) FROM (
            SELECT p.id,
                haversine_distance(f.latitude, f.longitude, :lat, :lng) AS distance_km
            FROM products p
            JOIN farms f ON p.farm_id = f.id
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE p.status = 'ACTIVE'
            AND f.latitude IS NOT NULL AND f.longitude IS NOT NULL
            AND (:search IS NULL OR p.name LIKE CONCAT('%', :search, '%'))
            AND (:category IS NULL OR c.slug = :category)
            AND (:minPrice IS NULL OR p.price >= :minPrice)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            AND (:isOrganic IS NULL OR p.is_organic = :isOrganic)
            HAVING distance_km <= :radiusKm
        ) AS cnt
        """,
            nativeQuery = true)
    Page<Product> findNearbyProducts(
            @Param("search") String search,
            @Param("category") String category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("isOrganic") Boolean isOrganic,
            @Param("lat") double latitude,
            @Param("lng") double longitude,
            @Param("radiusKm") int radiusKm,
            Pageable pageable);

    @Query(value = """
        SELECT p.*,
            haversine_distance(f.latitude, f.longitude, :lat, :lng) AS distance_km
        FROM products p
        JOIN farms f ON p.farm_id = f.id
        WHERE p.status = 'ACTIVE'
        AND f.latitude IS NOT NULL AND f.longitude IS NOT NULL
        HAVING distance_km <= :radiusKm
        ORDER BY distance_km ASC
        LIMIT :lim
        """, nativeQuery = true)
    List<Product> findNearbyActiveProducts(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") int radiusKm,
            @Param("lim") int limit);

    // ═══════════════════════════════════════════
    // FARMER QUERIES
    // ═══════════════════════════════════════════

    Page<Product> findByFarmIdAndStatus(
            UUID farmId, ProductStatus status, Pageable pageable);

    Page<Product> findByFarmIdAndStatusNot(
            UUID farmId, ProductStatus status, Pageable pageable);

    Page<Product> findByFarmId(UUID farmId, Pageable pageable);

    boolean existsByFarmIdAndSlug(UUID farmId, String slug);

    @Modifying
    @Query("UPDATE Product p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    // ═══════════════════════════════════════════
    // ★★★ FEATURED PRODUCTS — BOTH FORMS ★★★
    // ═══════════════════════════════════════════

    /**
     * Spring Data derived query method.
     * Auto-generated by Spring from method name.
     *
     * Called by: ProductService.getFeaturedProducts()
     *
     * Equivalent SQL:
     *   SELECT * FROM products
     *   WHERE featured = true AND status = ?
     *   ORDER BY avg_rating DESC
     */
    List<Product> findByFeaturedTrueAndStatusOrderByAvgRatingDesc(
            ProductStatus status, Pageable pageable);

    /**
     * Custom @Query version (same logic, explicit JPQL).
     * Can be used interchangeably.
     *
     * Called by: AnalyticsService or alternative usage.
     */
    @Query("SELECT p FROM Product p " +
            "WHERE p.featured = true AND p.status = :status " +
            "ORDER BY p.avgRating DESC")
    List<Product> findFeaturedByStatus(
            @Param("status") ProductStatus status, Pageable pageable);

    // ═══════════════════════════════════════════
    // COUNTING — DASHBOARD
    // ═══════════════════════════════════════════

    @Query("SELECT COUNT(p) FROM Product p " +
            "WHERE p.farm.farmer.id = :farmerId " +
            "AND p.status = :status")
    int countByFarmerAndStatus(
            @Param("farmerId") UUID farmerId,
            @Param("status") ProductStatus status);

    @Query("SELECT COUNT(p) FROM Product p " +
            "WHERE p.farm.farmer.id = :farmerId " +
            "AND p.status = 'ACTIVE'")
    int countActiveByFarmer(@Param("farmerId") UUID farmerId);

    @Query("SELECT COUNT(p) FROM Product p " +
            "WHERE p.farm.farmer.id = :farmerId " +
            "AND p.status = 'ACTIVE' " +
            "AND p.stockQuantity <= p.lowStockThreshold " +
            "AND p.stockQuantity > 0")
    int countLowStockProducts(@Param("farmerId") UUID farmerId);

    @Query("SELECT COUNT(p) FROM Product p " +
            "WHERE p.farm.farmer.id = :farmerId " +
            "AND p.status = 'ACTIVE' " +
            "AND p.stockQuantity = 0")
    int countOutOfStockProducts(@Param("farmerId") UUID farmerId);

    // ═══════════════════════════════════════════
    // TOP PRODUCTS — ANALYTICS
    // ═══════════════════════════════════════════

    @Query("SELECT p FROM Product p " +
            "WHERE p.farm.farmer.id = :farmerId " +
            "AND p.status = 'ACTIVE' " +
            "ORDER BY p.totalSold DESC")
    List<Product> findTopSellingByFarmer(
            @Param("farmerId") UUID farmerId, Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "WHERE p.farm.farmer.id = :farmerId " +
            "AND p.status = 'ACTIVE' " +
            "AND p.reviewCount > 0 " +
            "ORDER BY p.avgRating DESC")
    List<Product> findTopRatedByFarmer(
            @Param("farmerId") UUID farmerId, Pageable pageable);

    @Query(value = """
        SELECT p.id, p.name, p.total_sold, p.avg_rating, p.price,
            (p.total_sold * p.price) AS revenue
        FROM products p
        JOIN farms f ON p.farm_id = f.id
        WHERE f.farmer_id = :farmerId
        AND p.status = 'ACTIVE'
        AND p.total_sold > 0
        ORDER BY revenue DESC
        LIMIT :lim
        """, nativeQuery = true)
    List<Object[]> findTopProductsByRevenue(
            @Param("farmerId") String farmerId,
            @Param("lim") int limit);
}