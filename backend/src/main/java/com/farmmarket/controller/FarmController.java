package com.farmmarket.controller;

import com.farmmarket.dto.request.CreateFarmRequest;
import com.farmmarket.dto.response.ApiResponse;
import com.farmmarket.dto.response.FarmResponse;
import com.farmmarket.dto.response.PagedResponse;
import com.farmmarket.dto.response.ProductResponse;
import com.farmmarket.security.CustomUserDetails;
import com.farmmarket.service.FarmService;
import com.farmmarket.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/farms")
@RequiredArgsConstructor
public class FarmController {

    private final FarmService farmService;
    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FarmResponse>> getFarm(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success(farmService.getFarmById(id)));
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<PagedResponse<ProductResponse>> getFarmProducts(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                productService.getProductsByFarmId(id, page, size));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<FarmResponse>>> getNearby(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "50") int radiusKm) {
        return ResponseEntity.ok(ApiResponse.success(
                farmService.getNearbyFarms(latitude, longitude, radiusKm)));
    }

    @PostMapping
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<FarmResponse>> createFarm(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody CreateFarmRequest request) {

        FarmResponse farm = farmService.createFarm(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Farm created", farm));
    }

    @PutMapping
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<FarmResponse>> updateFarm(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody CreateFarmRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                farmService.updateFarm(user.getId(), request)));
    }
}