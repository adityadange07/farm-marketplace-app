package com.farmmarket.controller;

import com.farmmarket.dto.request.CreateProductRequest;
import com.farmmarket.dto.request.UpdateProductRequest;
import com.farmmarket.dto.response.ApiResponse;
import com.farmmarket.dto.response.PagedResponse;
import com.farmmarket.dto.response.ProductResponse;
import com.farmmarket.enums.ProductStatus;
import com.farmmarket.security.CustomUserDetails;
import com.farmmarket.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ═══ PUBLIC ═══════════════════════════════

    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean isOrganic,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Integer radiusKm,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PagedResponse<ProductResponse> response = productService
                .getAllProducts(search, category, minPrice, maxPrice,
                        isOrganic, latitude, longitude, radiusKm,
                        sortBy, sortDir, page, size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @PathVariable String slug) {

        ProductResponse product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeatured() {
        return ResponseEntity.ok(
                ApiResponse.success(productService.getFeaturedProducts()));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getNearby(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "25") int radiusKm) {

        return ResponseEntity.ok(ApiResponse.success(
                productService.getNearbyProducts(latitude, longitude, radiusKm)));
    }

    // ═══ FARMER ═══════════════════════════════

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestPart("product") CreateProductRequest request,
            @RequestPart(value = "images", required = false)
            List<MultipartFile> images) {

        ProductResponse product = productService
                .createProduct(user.getId(), request, images);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created", product));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {

        ProductResponse product = productService
                .updateProduct(user.getId(), id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Product updated", product));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID id) {

        productService.deleteProduct(user.getId(), id);
        return ResponseEntity.ok(
                ApiResponse.success("Product archived", null));
    }
}