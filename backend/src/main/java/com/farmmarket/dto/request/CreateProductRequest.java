package com.farmmarket.dto.request;

import com.farmmarket.enums.UnitType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateProductRequest {
    @NotBlank
    private String name;

    private String description;
    private String shortDescription;

    @NotNull
    @Positive
    private BigDecimal price;

    private BigDecimal compareAtPrice;

    @NotNull
    private UnitType unit;

    @NotNull @Min(0)
    private Integer stockQuantity;

    private Integer lowStockThreshold = 5;
    private Integer maxOrderQuantity = 100;
    private UUID categoryId;
    private Boolean isOrganic = false;
    private Boolean isSeasonal = false;
    private LocalDate harvestDate;
    private String growingMethod;
}
