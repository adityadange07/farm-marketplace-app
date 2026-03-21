┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                             │
│                                                                 │
│   ┌──────────────────┐          ┌──────────────────────┐        │
│   │   React.js SPA   │          │   React Native App   │        │
│   │   (JSX + Vite)   │          │   (Future Phase)     │        │
│   └────────┬─────────┘          └──────────┬───────────┘        │
└────────────┼────────────────────────────────┼────────────────────┘
             │         REST / WebSocket       │
             ▼                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT BACKEND                           │
│                                                                  │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌─────────────┐  │
│  │ Controller │ │  Service   │ │ Repository │ │  Security   │  │
│  │   Layer    │ │   Layer    │ │   Layer    │ │  (JWT/OAuth)│  │
│  └────────────┘ └────────────┘ └────────────┘ └─────────────┘  │
│                                                                  │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌─────────────┐  │
│  │  WebSocket │ │  Scheduler │ │   Email    │ │   File      │  │
│  │  (STOMP)   │ │  (Cron)    │ │  Service   │ │   Upload    │  │
│  └────────────┘ └────────────┘ └────────────┘ └─────────────┘  │
└──────────────────────────┬───────────────────────────────────────┘
                           │
┌──────────────────────────▼───────────────────────────────────────┐
│                       DATA LAYER                                 │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────┐      │
│  │  PostgreSQL  │  │    Redis     │  │    AWS S3 /       │      │
│  │  (Primary DB)│  │   (Cache +   │  │    MinIO          │      │
│  │  + PostGIS   │  │   Sessions)  │  │   (File Storage)  │      │
│  └──────────────┘  └──────────────┘  └───────────────────┘      │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐                              │
│  │ Elasticsearch│  │  RabbitMQ /  │                              │
│  │  (Search)    │  │   Kafka      │                              │
│  └──────────────┘  └──────────────┘                              │
└──────────────────────────────────────────────────────────────────┘



┌──────────────────┬────────────────────────────────────┐
│ Layer            │ Technology                         │
├──────────────────┼────────────────────────────────────┤
│ Frontend         │ React 18 + JSX + Vite              │
│ Routing          │ React Router v6                    │
│ State Management │ Zustand + React Query              │
│ HTTP Client      │ Axios                              │
│ UI Framework     │ Tailwind CSS + Headless UI         │
│ Maps             │ Leaflet / Google Maps              │
│ Forms            │ React Hook Form                    │
│ Charts           │ Recharts                           │
├──────────────────┼────────────────────────────────────┤
│ Backend          │ Spring Boot 3.2+ (Java 17+)       │
│ Security         │ Spring Security + JWT              │
│ ORM              │ Spring Data JPA + Hibernate        │
│ Validation       │ Jakarta Validation                 │
│ API Docs         │ SpringDoc OpenAPI (Swagger)        │
│ WebSocket        │ Spring WebSocket + STOMP           │
│ Email            │ Spring Mail + Thymeleaf            │
│ Scheduling       │ Spring Scheduler                   │
│ File Upload      │ AWS SDK / MinIO                    │
├──────────────────┼────────────────────────────────────┤
│ Database         │ PostgreSQL 15 + PostGIS            │
│ Cache            │ Redis                              │
│ Search           │ Elasticsearch                      │
│ Message Queue    │ RabbitMQ                           │
│ Payment          │ Stripe Java SDK                    │
│ Containerization │ Docker + Docker Compose            │
│ Build Tool       │ Maven / Gradle                     │
└──────────────────┴────────────────────────────────────┘


# ═══════════════════════════════════════
# 1. Start MySQL via Docker
# ═══════════════════════════════════════
docker run -d \
  --name farmmarket-mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=farmmarket \
  -e MYSQL_USER=farmuser \
  -e MYSQL_PASSWORD=farmpass123 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci \
  --log-bin-trust-function-creators=1 \
  mysql:8.0

# Wait for MySQL to be ready
sleep 15

# ═══════════════════════════════════════
# 2. Start Redis
# ═══════════════════════════════════════
docker run -d \
  --name farmmarket-redis \
  -p 6379:6379 \
  redis:7-alpine

# ═══════════════════════════════════════
# 3. Start Spring Boot
# ═══════════════════════════════════════
cd backend

# Create dev config
cat > src/main/resources/application-dev.yml << 'EOF'
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/farmmarket?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: farmuser
    password: farmpass123
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: validate
EOF

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# API:     http://localhost:8080/api
# Swagger: http://localhost:8080/api/swagger-ui.html

# ═══════════════════════════════════════
# 4. Start React Frontend
# ═══════════════════════════════════════
cd ../frontend
npm install
npm run dev

# Frontend: http://localhost:5173

# ═══════════════════════════════════════
# 5. Full Docker Compose
# ═══════════════════════════════════════
docker-compose up -d --build

# With phpMyAdmin (dev mode):
docker-compose --profile dev up -d

# phpMyAdmin: http://localhost:8888



-- Connect to MySQL and verify
mysql -u farmuser -pfarmpass123 farmmarket

-- Check tables created by Flyway
SHOW TABLES;

-- Verify Haversine function
SELECT haversine_distance(40.7128, -74.0060, 34.0522, -118.2437);
-- Should return ~3944 km (NYC to LA)

-- Check FULLTEXT index
SHOW INDEX FROM products WHERE Index_type = 'FULLTEXT';

-- Test full-text search
INSERT INTO products (id, farm_id, name, slug, description, price, unit, status, stock_quantity)
VALUES (UUID(), '<farm-id>', 'Organic Tomatoes', 'organic-tomatoes',
        'Fresh organic tomatoes from our garden', 4.99, 'KG', 'ACTIVE', 100);

SELECT * FROM products
WHERE MATCH(name, description, short_description)
AGAINST('+organic +tomato*' IN BOOLEAN MODE);

-- Check JSON support
SELECT JSON_EXTRACT(delivery_address, '$.city') AS city
FROM orders
WHERE delivery_address IS NOT NULL;

