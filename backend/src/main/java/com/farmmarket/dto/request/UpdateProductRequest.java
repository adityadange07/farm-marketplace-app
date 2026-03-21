package com.farmmarket.dto.request;

import com.farmmarket.enums.ProductStatus;
import com.farmmarket.enums.UnitType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateProductRequest {
    private String name;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private UnitType unit;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private Integer maxOrderQuantity;
    private Boolean isOrganic;
    private Boolean isSeasonal;
    private ProductStatus status;
    private String growingMethod;
    private LocalDate harvestDate;
}