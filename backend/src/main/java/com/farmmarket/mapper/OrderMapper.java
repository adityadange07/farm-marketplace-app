package com.farmmarket.mapper;

import com.farmmarket.dto.response.OrderResponse;
import com.farmmarket.entity.Order;
import com.farmmarket.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        if (order == null) return null;

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .serviceFee(order.getServiceFee())
                .tax(order.getTax())
                .discount(order.getDiscount())
                .total(order.getTotal())
                .status(order.getStatus().name())
                .deliveryType(order.getDeliveryType().name())
                .deliveryAddress(order.getDeliveryAddress())
                .scheduledDate(order.getScheduledDate() != null
                        ? order.getScheduledDate().toString() : null)
                .scheduledTimeSlot(order.getScheduledTimeSlot())
                .paymentStatus(order.getPaymentStatus())
                .statusHistory(order.getStatusHistory())
                .placedAt(order.getPlacedAt())
                .confirmedAt(order.getConfirmedAt())
                .deliveredAt(order.getDeliveredAt())
                .farm(mapFarm(order))
                .consumer(mapConsumer(order))
                .build();
    }

    public OrderResponse toDetailResponse(Order order) {
        OrderResponse response = toResponse(order);
        if (response != null && order.getItems() != null) {
            response.setItems(order.getItems().stream()
                    .map(this::mapItem)
                    .collect(Collectors.toList()));
        }
        return response;
    }

    private OrderResponse.OrderItemResponse mapItem(OrderItem item) {
        return OrderResponse.OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct() != null
                        ? item.getProduct().getId() : null)
                .productName(item.getProductName())
                .productImage(item.getProductImage())
                .unit(item.getUnit().name())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    private OrderResponse.FarmSummary mapFarm(Order order) {
        if (order.getFarm() == null) return null;
        return OrderResponse.FarmSummary.builder()
                .id(order.getFarm().getId())
                .farmName(order.getFarm().getFarmName())
                .farmerId(order.getFarm().getFarmer().getId())
                .city(order.getFarm().getCity())
                .state(order.getFarm().getState())
                .build();
    }

    private OrderResponse.ConsumerSummary mapConsumer(Order order) {
        if (order.getConsumer() == null) return null;
        return OrderResponse.ConsumerSummary.builder()
                .id(order.getConsumer().getId())
                .firstName(order.getConsumer().getFirstName())
                .lastName(order.getConsumer().getLastName())
                .email(order.getConsumer().getEmail())
                .build();
    }
}