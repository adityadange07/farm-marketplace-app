package com.farmmarket.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.UUID;

@Data
public class AddToCartRequest {
    @NotNull
    private UUID productId;

    @NotNull @Min(1) @Max(100)
    private Integer quantity;
}