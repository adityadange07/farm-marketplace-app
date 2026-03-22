package com.farmmarket.service;

import com.farmmarket.dto.request.CreateProductRequest;
import com.farmmarket.dto.request.UpdateProductRequest;
import com.farmmarket.dto.response.PagedResponse;
import com.farmmarket.dto.response.ProductResponse;
import com.farmmarket.entity.Category;
import com.farmmarket.entity.Farm;
import com.farmmarket.entity.Product;
import com.farmmarket.entity.ProductImage;
import com.farmmarket.enums.ProductStatus;
import com.farmmarket.exception.BadRequestException;
import com.farmmarket.exception.ResourceNotFoundException;
import com.farmmarket.mapper.ProductMapper;
import com.farmmarket.repository.CategoryRepository;
import com.farmmarket.repository.FarmRepository;
import com.farmmarket.repository.ProductRepository;
import com.farmmarket.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final FarmRepository farmRepository;
    private final CategoryRepository categoryRepository;
    private final FileUploadService fileUploadService;
    private final ProductMapper productMapper;

    // ═══════════════════════════════════════════
    // CONSUMER — Browse Products
    // ═══════════════════════════════════════════

    public PagedResponse<ProductResponse> getAllProducts(
            String search, String category,
            Double minPrice, Double maxPrice,
            Boolean isOrganic,
            Double latitude, Double longitude, Integer radiusKm,
            String sortBy, String sortDir,
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size, buildSort(sortBy, sortDir));
        Pageable nativePageable = PageRequest.of(page, size, buildNativeSort(sortBy, sortDir));
        Page<Product> productPage;

        if (latitude != null && longitude != null) {
            productPage = productRepository.findNearbyProducts(
                    search, category, minPrice, maxPrice, isOrganic,
                    latitude, longitude,
                    radiusKm != null ? radiusKm : 50,
                    nativePageable);
        } else if (search != null && !search.isBlank()) {
            try {
                String ftSearch = "+" + search.trim()
                        .replaceAll("\\s+", " +");
                productPage = productRepository.searchProductsFulltext(
                        ftSearch, category, minPrice, maxPrice, isOrganic,
                        nativePageable);
            } catch (Exception e) {
                log.warn("FULLTEXT failed, using LIKE: {}", e.getMessage());
                productPage = productRepository.searchProducts(
                        search, category, minPrice, maxPrice, isOrganic,
                        pageable);
            }
        } else {
            productPage = productRepository.findActiveProducts(
                    category, minPrice, maxPrice, isOrganic, pageable);
        }

        List<ProductResponse> products = productPage.getContent()
                .stream()
                .map(productMapper::toResponse)
                .toList();

        return PagedResponse.<ProductResponse>builder()
                .data(products)
                .page(page)
                .size(size)
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .hasMore(productPage.hasNext())
                .build();
    }

    // ── Get Single Product ─────────────────────
    @Cacheable(value = "products", key = "#slug")
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + slug));

        productRepository.incrementViewCount(product.getId());
        return productMapper.toDetailResponse(product);
    }

    // ════════════════════════════════════════════
    // ★★★ FEATURED PRODUCTS — FIXED METHOD ★★★
    // ════════════════════════════════════════════
    @Cacheable(value = "featured-products")
    public List<ProductResponse> getFeaturedProducts() {

        //  Uses the Spring Data derived query method name:
        //  findByFeaturedTrueAndStatusOrderByAvgRatingDesc
        //
        //  Spring auto-generates:
        //    SELECT * FROM products
        //    WHERE featured = true AND status = 'ACTIVE'
        //    ORDER BY avg_rating DESC
        //    LIMIT 12

        List<Product> products = productRepository
                .findByFeaturedTrueAndStatusOrderByAvgRatingDesc(
                        ProductStatus.ACTIVE,
                        PageRequest.of(0, 12)
                );

        return products.stream()
                .map(productMapper::toResponse)
                .toList();
    }

    // ── Nearby Products ───────────────────────
    public List<ProductResponse> getNearbyProducts(
            double lat, double lng, int radiusKm) {

        List<Product> products = productRepository
                .findNearbyActiveProducts(lat, lng, radiusKm, 50);

        return products.stream()
                .map(productMapper::toResponse)
                .toList();
    }

    // ═══════════════════════════════════════════
    // FARMER — Manage Products
    // ═══════════════════════════════════════════

    @Transactional
    public ProductResponse createProduct(
            UUID farmerId,
            CreateProductRequest request,
            List<MultipartFile> images) {

        Farm farm = farmRepository.findByFarmerId(farmerId)
                .orElseThrow(() -> new BadRequestException(
                        "Create a farm profile first"));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found"));
        }

        String slug = SlugUtil.generateSlug(request.getName());
        if (productRepository.existsByFarmIdAndSlug(farm.getId(), slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Product product = Product.builder()
                .farm(farm)
                .category(category)
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .price(request.getPrice())
                .compareAtPrice(request.getCompareAtPrice())
                .unit(request.getUnit())
                .stockQuantity(request.getStockQuantity())
                .lowStockThreshold(request.getLowStockThreshold() != null
                        ? request.getLowStockThreshold() : 5)
                .maxOrderQuantity(request.getMaxOrderQuantity() != null
                        ? request.getMaxOrderQuantity() : 100)
                .isOrganic(request.getIsOrganic() != null
                        ? request.getIsOrganic() : false)
                .isSeasonal(request.getIsSeasonal() != null
                        ? request.getIsSeasonal() : false)
                .harvestDate(request.getHarvestDate())
                .growingMethod(request.getGrowingMethod())
                .status(ProductStatus.ACTIVE)
                .build();

        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                String imageUrl = fileUploadService
                        .uploadFile(images.get(i), "products");

                ProductImage productImage = ProductImage.builder()
                        .url(imageUrl)
                        .altText(request.getName())
                        .isPrimary(i == 0)
                        .sortOrder(i)
                        .build();

                product.addImage(productImage);
            }
        }

        product = productRepository.save(product);
        return productMapper.toResponse(product);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse updateProduct(
            UUID farmerId, UUID productId, UpdateProductRequest request) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found"));

        if (!product.getFarm().getFarmer().getId().equals(farmerId)) {
            throw new BadRequestException("Not your product");
        }

        if (request.getName() != null)
            product.setName(request.getName());
        if (request.getDescription() != null)
            product.setDescription(request.getDescription());
        if (request.getShortDescription() != null)
            product.setShortDescription(request.getShortDescription());
        if (request.getPrice() != null)
            product.setPrice(request.getPrice());
        if (request.getCompareAtPrice() != null)
            product.setCompareAtPrice(request.getCompareAtPrice());
        if (request.getUnit() != null)
            product.setUnit(request.getUnit());
        if (request.getStockQuantity() != null)
            product.setStockQuantity(request.getStockQuantity());
        if (request.getLowStockThreshold() != null)
            product.setLowStockThreshold(request.getLowStockThreshold());
        if (request.getMaxOrderQuantity() != null)
            product.setMaxOrderQuantity(request.getMaxOrderQuantity());
        if (request.getIsOrganic() != null)
            product.setIsOrganic(request.getIsOrganic());
        if (request.getIsSeasonal() != null)
            product.setIsSeasonal(request.getIsSeasonal());
        if (request.getStatus() != null)
            product.setStatus(request.getStatus());
        if (request.getGrowingMethod() != null)
            product.setGrowingMethod(request.getGrowingMethod());
        if (request.getHarvestDate() != null)
            product.setHarvestDate(request.getHarvestDate());

        product = productRepository.save(product);
        return productMapper.toResponse(product);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(UUID farmerId, UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found"));

        if (!product.getFarm().getFarmer().getId().equals(farmerId)) {
            throw new BadRequestException("Not your product");
        }

        product.setStatus(ProductStatus.ARCHIVED);
        productRepository.save(product);
    }

    // ── Farmer product list ───────────────────
    public PagedResponse<ProductResponse> getFarmerProducts(
            UUID farmerId, ProductStatus status, int page, int size) {

        Farm farm = farmRepository.findByFarmerId(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Farm not found"));

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<Product> productPage;
        if (status != null) {
            productPage = productRepository
                    .findByFarmIdAndStatus(farm.getId(), status, pageable);
        } else {
            productPage = productRepository
                    .findByFarmIdAndStatusNot(
                            farm.getId(), ProductStatus.ARCHIVED, pageable);
        }

        return PagedResponse.<ProductResponse>builder()
                .data(productPage.map(productMapper::toResponse).getContent())
                .page(page)
                .size(size)
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .hasMore(productPage.hasNext())
                .build();
    }

    // ── Products by Farm (public) ─────────────
    public PagedResponse<ProductResponse> getProductsByFarmId(
            UUID farmId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<Product> productPage = productRepository
                .findByFarmIdAndStatusNot(
                        farmId, ProductStatus.ARCHIVED, pageable);

        return PagedResponse.<ProductResponse>builder()
                .data(productPage.map(productMapper::toResponse).getContent())
                .page(page)
                .size(size)
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .hasMore(productPage.hasNext())
                .build();
    }

    // ── Sort builder ──────────────────────────
    private Sort buildSort(String sortBy, String sortDir) {
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (sortBy != null ? sortBy : "newest") {
            case "price" -> Sort.by(dir, "price");
            case "rating" -> Sort.by(dir, "avgRating");
            case "popular" -> Sort.by(dir, "totalSold");
            case "name" -> Sort.by(dir, "name");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    // ── Native SQL Sort builder ───────────────────
    private Sort buildNativeSort(String sortBy, String sortDir) {
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (sortBy != null ? sortBy : "newest") {
            case "price" -> Sort.by(dir, "price");
            case "rating" -> Sort.by(dir, "avg_rating");
            case "popular" -> Sort.by(dir, "total_sold");
            case "name" -> Sort.by(dir, "name");
            default -> Sort.by(Sort.Direction.DESC, "created_at");
        };
    }
}