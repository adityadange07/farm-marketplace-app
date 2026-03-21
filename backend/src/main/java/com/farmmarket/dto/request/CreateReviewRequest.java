package com.farmmarket.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateReviewRequest {
    @NotNull
    private UUID orderId;

    @NotNull
    private UUID productId;

    @NotNull @Min(1) @Max(5)
    private Integer rating;

    private String title;

    @Size(max = 2000)
    private String comment;
}