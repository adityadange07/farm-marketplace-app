package com.farmmarket.controller;

import com.farmmarket.dto.request.AddToCartRequest;
import com.farmmarket.dto.response.ApiResponse;
import com.farmmarket.dto.response.CartResponse;
import com.farmmarket.security.CustomUserDetails;
import com.farmmarket.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                ApiResponse.success(cartService.getCart(user.getId())));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.addToCart(user.getId(), request)));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID itemId,
            @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.updateQuantity(
                        user.getId(), itemId, body.get("quantity"))));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.removeItem(user.getId(), itemId)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal CustomUserDetails user) {
        cartService.clearCart(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}