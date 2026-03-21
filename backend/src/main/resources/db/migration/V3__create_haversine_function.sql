-- ═══════════════════════════════════════════
-- Haversine Distance Function (replaces PostGIS)
-- Returns distance in KILOMETERS
-- ═══════════════════════════════════════════
DELIMITER //

CREATE FUNCTION haversine_distance(
    lat1 DECIMAL(10,8),
    lon1 DECIMAL(11,8),
    lat2 DECIMAL(10,8),
    lon2 DECIMAL(11,8)
)
    RETURNS DECIMAL(10,4)
    DETERMINISTIC
BEGIN
    DECLARE earth_radius DECIMAL(10,4) DEFAULT 6371.0;
    DECLARE d_lat DECIMAL(20,10);
    DECLARE d_lon DECIMAL(20,10);
    DECLARE a DECIMAL(20,10);
    DECLARE c DECIMAL(20,10);

    SET d_lat = RADIANS(lat2 - lat1);
    SET d_lon = RADIANS(lon2 - lon1);

    SET a = SIN(d_lat / 2) * SIN(d_lat / 2) +
            COS(RADIANS(lat1)) * COS(RADIANS(lat2)) *
            SIN(d_lon / 2) * SIN(d_lon / 2);

    SET c = 2 * ATAN2(SQRT(a), SQRT(1 - a));

RETURN earth_radius * c;
END //

DELIMITER ;