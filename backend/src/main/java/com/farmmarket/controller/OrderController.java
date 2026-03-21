package com.farmmarket.controller;

import com.farmmarket.dto.request.CreateOrderRequest;
import com.farmmarket.dto.request.UpdateOrderStatusRequest;
import com.farmmarket.dto.response.ApiResponse;
import com.farmmarket.dto.response.OrderResponse;
import com.farmmarket.dto.response.PagedResponse;
import com.farmmarket.enums.OrderStatus;
import com.farmmarket.security.CustomUserDetails;
import com.farmmarket.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ═══ CONSUMER ═════════════════════════════

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOrder(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody CreateOrderRequest request) {

        Map<String, Object> result = orderService
                .createOrder(user.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed", result));
    }

    @GetMapping("/orders")
    public ResponseEntity<PagedResponse<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                orderService.getConsumerOrders(user.getId(), page, size));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID id) {

        return ResponseEntity.ok(ApiResponse.success(
                orderService.getOrderDetails(user.getId(), id)));
    }

    // ═══ FARMER ═══════════════════════════════

    @GetMapping("/farmer/orders")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<PagedResponse<OrderResponse>> getFarmerOrders(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                orderService.getFarmerOrders(user.getId(), status, page, size));
    }

    @PutMapping("/farmer/orders/{id}/status")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        OrderResponse order = orderService.updateOrderStatus(
                user.getId(), id, request.getStatus(), request.getNote());

        return ResponseEntity.ok(
                ApiResponse.success("Status updated", order));
    }
}