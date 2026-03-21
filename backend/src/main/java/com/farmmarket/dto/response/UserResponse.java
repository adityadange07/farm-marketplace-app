package com.farmmarket.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String role;
    private String avatarUrl;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
}