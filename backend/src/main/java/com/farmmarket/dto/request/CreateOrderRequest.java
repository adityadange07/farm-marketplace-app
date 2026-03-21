package com.farmmarket.dto.request;

import com.farmmarket.enums.DeliveryType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
public class CreateOrderRequest {
    @NotNull
    private DeliveryType deliveryType;

    private Map<String, String> deliveryAddress;
    private String deliveryNotes;
    private LocalDate scheduledDate;
    private String timeSlot;
    private String couponCode;
}
