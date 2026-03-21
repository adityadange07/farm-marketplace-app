package com.farmmarket.dto.response;

import com.farmmarket.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UUID userId;
    private String email;
    private UserRole role;
    private String firstName;
    private String lastName;
}
