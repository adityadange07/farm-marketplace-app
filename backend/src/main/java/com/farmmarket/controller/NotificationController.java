package com.farmmarket.controller;

import com.farmmarket.dto.response.ApiResponse;
import com.farmmarket.entity.Notification;
import com.farmmarket.repository.NotificationRepository;
import com.farmmarket.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<Page<Notification>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(
                        user.getId(), PageRequest.of(page, 20)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails user) {
        int count = notificationRepository
                .countByUserIdAndIsReadFalse(user.getId());
        return ResponseEntity.ok(
                ApiResponse.success(Map.of("count", count)));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @AuthenticationPrincipal CustomUserDetails user) {
        notificationRepository.markAllAsRead(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Done", null));
    }
}