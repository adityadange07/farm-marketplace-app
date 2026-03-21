-- ═══════════════════════════════════════════════
-- FARM MARKETPLACE - MySQL 8.0+ Schema
-- ═══════════════════════════════════════════════

-- ─────────────────────────────────────
-- USERS
-- ─────────────────────────────────────
CREATE TABLE users (
                       id CHAR(36) NOT NULL DEFAULT (UUID()),
                       email VARCHAR(255) NOT NULL,
                       phone VARCHAR(20) NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role ENUM('FARMER', 'CONSUMER', 'ADMIN', 'DELIVERY_PARTNER') NOT NULL,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       avatar_url TEXT NULL,
                       is_active TINYINT(1) NOT NULL DEFAULT 1,
                       email_verified TINYINT(1) NOT NULL DEFAULT 0,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_email (email),
                       UNIQUE KEY uk_users_phone (phone),
                       INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────
-- CATEGORIES
-- ─────────────────────────────────────
CREATE TABLE categories (
                            id CHAR(36) NOT NULL DEFAULT (UUID()),
                            name VARCHAR(100) NOT NULL,
                            slug VARCHAR(100) NOT NULL,
                            icon_url TEXT NULL,
                            parent_id CHAR(36) NULL,
                            sort_order INT NOT NULL DEFAULT 0,
                            is_active TINYINT(1) NOT NULL DEFAULT 1,
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_categories_slug (slug),
                            INDEX idx_categories_parent (parent_id),
                            CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id)
                                REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────
-- FARMS
-- ─────────────────────────────────────
CREATE TABLE farms (
                       id CHAR(36) NOT NULL DEFAULT (UUID()),
                       farmer_id CHAR(36) NOT NULL,
                       farm_name VARCHAR(255) NOT NULL,
                       description TEXT NULL,
                       farm_size_acres DECIMAL(10,2) NULL,
                       address_line1 VARCHAR(255) NULL,
                       address_line2 VARCHAR(255) NULL,
                       city VARCHAR(100) NULL,
                       state VARCHAR(100) NULL,
                       zip_code VARCHAR(20) NULL,
                       country VARCHAR(100) NOT NULL DEFAULT 'US',
                       latitude DECIMAL(10,8) NULL,
                       longitude DECIMAL(11,8) NULL,
                       is_organic TINYINT(1) NOT NULL DEFAULT 0,
                       certifications JSON NULL,
                       verification_status ENUM('PENDING', 'VERIFIED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
                       delivery_radius_km INT NOT NULL DEFAULT 50,
                       minimum_order DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                       rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,
                       total_reviews INT NOT NULL DEFAULT 0,
                       banner_image TEXT NULL,
                       stripe_account_id VARCHAR(255) NULL,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       INDEX idx_farms_farmer (farmer_id),
                       INDEX idx_farms_location (latitude, longitude),
                       INDEX idx_farms_status (verification_status),
                       INDEX idx_farms_city_state (city, state),
                       CONSTRAINT fk_farms_farmer FOREIGN KEY (farmer_id)
                           REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────
-- PRODUCTS
-- ─────────────────────────────────────
CREATE TABLE products (
                          id CHAR(36) NOT NULL DEFAULT (UUID()),
                          farm_id CHAR(36) NOT NULL,
                          category_id CHAR(36) NULL,
                          name VARCHAR(255) NOT NULL,
                          slug VARCHAR(255) NOT NULL,
                          description TEXT NULL,
                          short_description VARCHAR(500) NULL,
                          price DECIMAL(10,2) NOT NULL,
                          compare_at_price DECIMAL(10,2) NULL,
                          unit ENUM('KG', 'LB', 'PIECE', 'DOZEN', 'BUNCH', 'LITER', 'GALLON', 'BASKET', 'BOX') NOT NULL,
                          stock_quantity INT NOT NULL DEFAULT 0,
                          low_stock_threshold INT NOT NULL DEFAULT 5,
                          max_order_quantity INT NOT NULL DEFAULT 100,
                          is_organic TINYINT(1) NOT NULL DEFAULT 0,
                          is_seasonal TINYINT(1) NOT NULL DEFAULT 0,
                          harvest_date DATE NULL,
                          growing_method VARCHAR(100) NULL,
                          nutritional_info JSON NULL,
                          status ENUM('DRAFT', 'ACTIVE', 'OUT_OF_STOCK', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
                          featured TINYINT(1) NOT NULL DEFAULT 0,
                          total_sold INT NOT NULL DEFAULT 0,
                          avg_rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,
                          review_count INT NOT NULL DEFAULT 0,
                          view_count INT NOT NULL DEFAULT 0,
                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (id),
                          UNIQUE KEY uk_products_farm_slug (farm_id, slug),
                          INDEX idx_products_category (category_id),
                          INDEX idx_products_status (status),
                          INDEX idx_products_price (price),
                          INDEX idx_products_featured (featured, status),
                          INDEX idx_products_organic (is_organic),
                          INDEX idx_products_created (created_at),
                          FULLTEXT INDEX ft_products_search (name, description, short_description),
                          CONSTRAINT fk_products_farm FOREIGN KEY (farm_id)
                              REFERENCES farms(id) ON DELETE CASCADE,
                          CONSTRAINT fk_products_category FOREIGN KEY (category_id)
                              REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────
-- PRODUCT IMAGES
-- ─────────────────────────────────────
CREATE TABLE product_images (
                                id CHAR(36) NOT NULL DEFAULT (UUID()),
                                product_id CHAR(36) NOT NULL,
                                url TEXT NOT NULL,
                                alt_text VARCHAR(255) NULL,
                                is_primary TINYINT(1) NOT NULL DEFAULT 0,
                                sort_order INT NOT NULL DEFAULT 0,
                                PRIMARY KEY (id),
                                INDEX idx_product_images_product (product_id),
                                CONSTRAINT fk_product_images_product FOREIGN KEY (product_id)
                                    REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────
-- ORDERS
-- ─────────────────────────────────────
CREATE TABLE orders (
                        id CHAR(36) NOT NULL DEFAULT (UUID()),
                        order_number VARCHAR(20) NOT NULL,
                        consumer_id CHAR(36) NOT NULL,
                        farm_id CHAR(36) NOT NULL,
                        subtotal DECIMAL(10,2) NOT NULL,
                        delivery_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                        service_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                        tax DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                        discount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                        total DECIMAL(10,2) NOT NULL,
                        delivery_type ENUM('DELIVERY', 'PICKUP', 'FARMER_MARKET') NOT NULL,
                        delivery_address JSON NULL,
                        delivery_notes TEXT NULL,
                        scheduled_date DATE NULL,
                        scheduled_time_slot VARCHAR(50) NULL,
                        status ENUM('PENDING', 'CONFIRMED', 'PROCESSING', 'READY_FOR_PICKUP',
                'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED', 'REFUNDED')
        NOT NULL DEFAULT 'PENDING',
                        status_history JSON NULL,
                        payment_method VARCHAR(50) NULL,
                        payment_status VARCHAR(50) NOT NULL DEFAULT 'pending',
                        payment_intent_id VARCHAR(255) NULL,
                        coupon_code VARCHAR(50) NULL,
                        coupon_discount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                        placed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        confirmed_at DATETIME NULL,
                        delivered_at DATETIME NULL,
                        cancelled_at DATETIME NULL,
                        cancellation_reason TEXT NULL,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (id),
                        UNIQUE KEY uk_orders_number (order_number),
                        INDEX idx_orders_consumer (consumer_id),
                        INDEX idx_orders_farm (farm_id),
                        INDEX idx_orders_status (status),
                        INDEX idx_orders_placed (placed_at),
                        INDEX idx_orders_payment (payment_status),
                        CONSTRAINT fk_orders_consumer FOREIGN KEY (consumer_id)
                            REFERENCES users(id),
                        CONSTRAINT fk_orders_farm FOREIGN KEY (farm_id)
                            REFERENCES farms(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────
-- ORDER ITEMS
-- ─────────────────────────────────────
CREATE TABLE order_items (
                             id CHAR(36) NOT NULL DEFAULT (UUID()),
                             order_id CHAR(36) NOT NULL,
                             product_id CHAR(36) NULL,
                             product_name VARCHAR(255) NOT NULL,
                             product_image TEXT NULL,
                             unit ENUM('KG', 'LB', 'PIECE', 'DOZEN', 'BUNCH', 'LITER', 'GALLON', 'BASKET', 'BOX') NOT NULL,
                             quantity INT NOT NULL,
                             unit_price DECIMAL(10,2) NOT NULL,
                             total_price DECIMAL(10,2) NOT NULL,
                             notes TEXT NULL,
                             PRIMARY KEY (id),
                             INDEX idx_order_items_order (order_id),
                             INDEX idx_order_items_product (product_id),
                             CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
                                 REFERENCES orders(id) ON DELETE CASCADE,
                             CONSTRAINT fk_order_items_product FOREIGN KEY (product_id)
                                 REFERENCES products(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────
-- CART ITEMS
-- ─────────────────────────────────────
CREATE TABLE cart_items (
                            id CHAR(36) NOT NULL DEFAULT (UUID()),
                            user_id CHAR(36) NOT NULL,
                            product_id CHAR(36) NOT NULL,
                            quantity INT NOT NULL DEFAULT 1,
                            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_cart_user_product (user_id, product_id),
                            INDEX idx_cart_user (user_id),
                            CONSTRAINT fk_cart_user FOREIGN KEY (user_id)
                                REFERENCES users(id) ON DELETE CASCADE,
                            CONSTRAINT fk_cart_product FOREIGN KEY (product_id)
                                REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────
-- REVIEWS
-- ─────────────────────────────────────
CREATE TABLE reviews (
                         id CHAR(36) NOT NULL DEFAULT (UUID()),
                         order_id CHAR(36) NULL,
                         product_id CHAR(36) NULL,
                         farm_id CHAR(36) NULL,
                         consumer_id CHAR(36) NULL,
                         rating TINYINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
                         title VARCHAR(255) NULL,
                         comment TEXT NULL,
                         images JSON NULL,
                         is_verified TINYINT(1) NOT NULL DEFAULT 1,
                         is_visible TINYINT(1) NOT NULL DEFAULT 1,
                         farmer_reply TEXT NULL,
                         replied_at DATETIME NULL,
                         created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (id),
                         UNIQUE KEY uk_reviews_order_product_consumer (order_id, product_id, consumer_id),
                         INDEX idx_reviews_product (product_id),
                         INDEX idx_reviews_farm (farm_id),
                         INDEX idx_reviews_consumer (consumer_id),
                         INDEX idx_reviews_rating (rating),
                         CONSTRAINT fk_reviews_order FOREIGN KEY (order_id)
                             REFERENCES orders(id) ON DELETE SET NULL,
                         CONSTRAINT fk_reviews_product FOREIGN KEY (product_id)
                             REFERENCES products(id) ON DELETE SET NULL,
                         CONSTRAINT fk_reviews_farm FOREIGN KEY (farm_id)
                             REFERENCES farms(id) ON DELETE SET NULL,
                         CONSTRAINT fk_reviews_consumer FOREIGN KEY (consumer_id)
                             REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────
-- ADDRESSES
-- ─────────────────────────────────────
CREATE TABLE addresses (
                           id CHAR(36) NOT NULL DEFAULT (UUID()),
                           user_id CHAR(36) NOT NULL,
                           label VARCHAR(50) NOT NULL DEFAULT 'Home',
                           address_line1 VARCHAR(255) NOT NULL,
                           address_line2 VARCHAR(255) NULL,
                           city VARCHAR(100) NOT NULL,
                           state VARCHAR(100) NOT NULL,
                           zip_code VARCHAR(20) NOT NULL,
                           country VARCHAR(100) NOT NULL DEFAULT 'US',
                           latitude DECIMAL(10,8) NULL,
                           longitude DECIMAL(11,8) NULL,
                           is_default TINYINT(1) NOT NULL DEFAULT 0,
                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (id),
                           INDEX idx_addresses_user (user_id),
                           CONSTRAINT fk_addresses_user FOREIGN KEY (user_id)
                               REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────
-- FAVORITES
-- ─────────────────────────────────────
CREATE TABLE favorites (
                           user_id CHAR(36) NOT NULL,
                           farm_id CHAR(36) NOT NULL,
                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (user_id, farm_id),
                           CONSTRAINT fk_favorites_user FOREIGN KEY (user_id)
                               REFERENCES users(id) ON DELETE CASCADE,
                           CONSTRAINT fk_favorites_farm FOREIGN KEY (farm_id)
                               REFERENCES farms(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────
-- CONVERSATIONS
-- ─────────────────────────────────────
CREATE TABLE conversations (
                               id CHAR(36) NOT NULL DEFAULT (UUID()),
                               farmer_id CHAR(36) NOT NULL,
                               consumer_id CHAR(36) NOT NULL,
                               last_message_at DATETIME NULL,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (id),
                               INDEX idx_conversations_farmer (farmer_id),
                               INDEX idx_conversations_consumer (consumer_id),
                               CONSTRAINT fk_conversations_farmer FOREIGN KEY (farmer_id)
                                   REFERENCES users(id),
                               CONSTRAINT fk_conversations_consumer FOREIGN KEY (consumer_id)
                                   REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────
-- MESSAGES
-- ─────────────────────────────────────
CREATE TABLE messages (
                          id CHAR(36) NOT NULL DEFAULT (UUID()),
                          conversation_id CHAR(36) NOT NULL,
                          sender_id CHAR(36) NOT NULL,
                          content TEXT NOT NULL,
                          message_type VARCHAR(20) NOT NULL DEFAULT 'text',
                          is_read TINYINT(1) NOT NULL DEFAULT 0,
                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (id),
                          INDEX idx_messages_conversation (conversation_id),
                          INDEX idx_messages_sender (sender_id),
                          INDEX idx_messages_read (is_read),
                          CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id)
                              REFERENCES conversations(id) ON DELETE CASCADE,
                          CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id)
                              REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────
-- COUPONS
-- ─────────────────────────────────────
CREATE TABLE coupons (
                         id CHAR(36) NOT NULL DEFAULT (UUID()),
                         farm_id CHAR(36) NULL,
                         code VARCHAR(50) NOT NULL,
                         description TEXT NULL,
                         discount_type ENUM('percentage', 'fixed') NOT NULL,
                         discount_value DECIMAL(10,2) NOT NULL,
                         min_order_value DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                         max_uses INT NULL,
                         used_count INT NOT NULL DEFAULT 0,
                         valid_from DATETIME NULL,
                         valid_until DATETIME NULL,
                         is_active TINYINT(1) NOT NULL DEFAULT 1,
                         PRIMARY KEY (id),
                         UNIQUE KEY uk_coupons_code (code),
                         INDEX idx_coupons_farm (farm_id),
                         CONSTRAINT fk_coupons_farm FOREIGN KEY (farm_id)
                             REFERENCES farms(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────
-- NOTIFICATIONS
-- ─────────────────────────────────────
CREATE TABLE notifications (
                               id CHAR(36) NOT NULL DEFAULT (UUID()),
                               user_id CHAR(36) NOT NULL,
                               title VARCHAR(255) NOT NULL,
                               message TEXT NOT NULL,
                               type VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
                               reference_id VARCHAR(255) NULL,
                               is_read TINYINT(1) NOT NULL DEFAULT 0,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (id),
                               INDEX idx_notifications_user (user_id),
                               INDEX idx_notifications_read (user_id, is_read),
                               INDEX idx_notifications_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────
-- SUBSCRIPTION PLANS
-- ─────────────────────────────────────
CREATE TABLE subscription_plans (
                                    id CHAR(36) NOT NULL DEFAULT (UUID()),
                                    farm_id CHAR(36) NOT NULL,
                                    name VARCHAR(255) NOT NULL,
                                    description TEXT NULL,
                                    price DECIMAL(10,2) NOT NULL,
                                    frequency ENUM('weekly', 'biweekly', 'monthly') NOT NULL,
                                    items_included JSON NULL,
                                    max_subscribers INT NULL,
                                    is_active TINYINT(1) NOT NULL DEFAULT 1,
                                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (id),
                                    INDEX idx_subscription_plans_farm (farm_id),
                                    CONSTRAINT fk_subscription_plans_farm FOREIGN KEY (farm_id)
                                        REFERENCES farms(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────
-- SUBSCRIPTIONS
-- ─────────────────────────────────────
CREATE TABLE subscriptions (
                               id CHAR(36) NOT NULL DEFAULT (UUID()),
                               plan_id CHAR(36) NOT NULL,
                               consumer_id CHAR(36) NOT NULL,
                               status ENUM('active', 'paused', 'cancelled', 'expired') NOT NULL DEFAULT 'active',
                               start_date DATE NOT NULL,
                               next_delivery DATE NULL,
                               delivery_address JSON NULL,
                               stripe_subscription_id VARCHAR(255) NULL,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (id),
                               INDEX idx_subscriptions_plan (plan_id),
                               INDEX idx_subscriptions_consumer (consumer_id),
                               CONSTRAINT fk_subscriptions_plan FOREIGN KEY (plan_id)
                                   REFERENCES subscription_plans(id),
                               CONSTRAINT fk_subscriptions_consumer FOREIGN KEY (consumer_id)
                                   REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;