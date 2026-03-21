package com.farmmarket.controller;

import com.farmmarket.dto.request.CreateReviewRequest;
import com.farmmarket.dto.response.ApiResponse;
import com.farmmarket.dto.response.PagedResponse;
import com.farmmarket.dto.response.ReviewResponse;
import com.farmmarket.security.CustomUserDetails;
import com.farmmarket.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        reviewService.createReview(user.getId(), request)));
    }

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<PagedResponse<ReviewResponse>> getProductReviews(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                reviewService.getProductReviews(productId, page, size));
    }
}