package com.farmmarket.controller;

import com.farmmarket.dto.response.ApiResponse;
import com.farmmarket.entity.Conversation;
import com.farmmarket.entity.Message;
import com.farmmarket.security.CustomUserDetails;
import com.farmmarket.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<Page<Conversation>> getConversations(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(
                chatService.getUserConversations(user.getId(), page));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Conversation>> startConversation(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody Map<String, UUID> body) {
        Conversation conv = chatService.getOrCreateConversation(
                user.getId(), body.get("farmerId"));
        return ResponseEntity.ok(ApiResponse.success(conv));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<Page<Message>> getMessages(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(
                chatService.getMessages(id, user.getId(), page));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<Message>> sendMessage(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        Message msg = chatService.sendMessage(
                id, user.getId(), body.get("content"));
        return ResponseEntity.ok(ApiResponse.success(msg));
    }
}