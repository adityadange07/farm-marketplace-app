package com.farmmarket.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data @Builder
public class ReviewResponse {
    private UUID id;
    private Integer rating;
    private String title;
    private String comment;
    private List<Map<String, String>> images;
    private String consumerName;
    private String consumerAvatar;
    private Boolean isVerified;
    private String farmerReply;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
}