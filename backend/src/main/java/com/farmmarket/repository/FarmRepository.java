package com.farmmarket.repository;

import com.farmmarket.entity.Farm;
import com.farmmarket.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FarmRepository extends JpaRepository<Farm, UUID> {

    Optional<Farm> findByFarmerId(UUID farmerId);

    boolean existsByFarmerId(UUID farmerId);

    Page<Farm> findByVerificationStatus(VerificationStatus status, Pageable pageable);

    // ═══ Nearby Farms — MySQL Haversine ═══
    @Query(value = """
        SELECT f.*,
            haversine_distance(f.latitude, f.longitude, :lat, :lng) AS distance_km
        FROM farms f
        WHERE f.verification_status = 'VERIFIED'
        AND f.latitude IS NOT NULL
        AND f.longitude IS NOT NULL
        HAVING distance_km <= :radiusKm
        ORDER BY distance_km ASC
        LIMIT :lim
        """, nativeQuery = true)
    List<Farm> findNearbyFarms(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") int radiusKm,
            @Param("lim") int limit);
}