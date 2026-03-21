package com.farmmarket.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateFarmRequest {
    @NotBlank
    private String farmName;

    private String description;
    private BigDecimal farmSizeAcres;

    @NotBlank
    private String addressLine1;
    private String addressLine2;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String zipCode;

    private String country = "US";
    private Double latitude;
    private Double longitude;
    private Boolean isOrganic = false;
    private Integer deliveryRadiusKm = 50;
    private BigDecimal minimumOrder;
}