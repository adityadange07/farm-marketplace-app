package com.farmmarket.service;

import com.farmmarket.dto.response.OrderResponse;
import com.farmmarket.entity.Notification;
import com.farmmarket.entity.Order;
import com.farmmarket.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;

    // ═══ New Order → Notify Farmer + Consumer ═══
    @Async
    public void notifyNewOrder(OrderResponse order) {
        try {
            // 1. DB notification for farmer
            if (order.getFarm() != null && order.getFarm().getFarmerId() != null) {
                Notification farmerNotif = Notification.builder()
                        .userId(order.getFarm().getFarmerId())
                        .title("🆕 New Order!")
                        .message("Order " + order.getOrderNumber()
                                + " — $" + order.getTotal()
                                + " from " + (order.getConsumer() != null
                                ? order.getConsumer().getFirstName() : "Customer"))
                        .type("NEW_ORDER")
                        .referenceId(order.getId().toString())
                        .build();
                notificationRepository.save(farmerNotif);

                // 2. WebSocket push to farmer
                pushToUser(order.getFarm().getFarmerId(), farmerNotif);
            }

            // 3. DB notification for consumer
            if (order.getConsumer() != null) {
                Notification consumerNotif = Notification.builder()
                        .userId(order.getConsumer().getId())
                        .title("Order Placed!")
                        .message("Your order " + order.getOrderNumber()
                                + " has been placed with "
                                + (order.getFarm() != null
                                ? order.getFarm().getFarmName() : "the farm"))
                        .type("ORDER_PLACED")
                        .referenceId(order.getId().toString())
                        .build();
                notificationRepository.save(consumerNotif);
            }

            // 4. Emails
            emailService.sendOrderConfirmationToFarmer(order);
            emailService.sendOrderConfirmationToConsumer(order);

            log.info("New order notifications sent for {}",
                    order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to send new order notifications for {}",
                    order.getOrderNumber(), e);
        }
    }

    // ═══ Order Status Update → Notify Consumer ═══
    @Async
    public void notifyOrderStatusUpdate(Order order) {
        try {
            // 1. DB notification
            Notification notification = Notification.builder()
                    .userId(order.getConsumer().getId())
                    .title(getStatusTitle(order.getStatus().name()))
                    .message("Order " + order.getOrderNumber()
                            + " is now: " + formatStatus(order.getStatus().name()))
                    .type("ORDER_UPDATE")
                    .referenceId(order.getId().toString())
                    .build();
            notificationRepository.save(notification);

            // 2. WebSocket push
            pushToUser(order.getConsumer().getId(), notification);

            // 3. Email
            emailService.sendOrderStatusUpdate(order);

            // 4. If delivered, send review prompt
            if ("DELIVERED".equals(order.getStatus().name())) {
                emailService.sendDeliveryAndReviewPrompt(order);
            }

            log.info("Status update notification sent: {} → {}",
                    order.getOrderNumber(), order.getStatus());
        } catch (Exception e) {
            log.error("Failed to send status update for {}",
                    order.getOrderNumber(), e);
        }
    }

    // ═══ Low Stock → Notify Farmer ═══
    @Async
    public void notifyLowStock(UUID farmerId, String productName,
                               int currentStock) {
        try {
            Notification notification = Notification.builder()
                    .userId(farmerId)
                    .title("⚠️ Low Stock")
                    .message(productName + " — only " + currentStock + " left")
                    .type("LOW_STOCK")
                    .build();
            notificationRepository.save(notification);

            pushToUser(farmerId, notification);
        } catch (Exception e) {
            log.error("Failed to send low stock notification", e);
        }
    }

    // ═══ Generic Push ═══
    private void pushToUser(UUID userId, Notification notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    notification
            );
        } catch (Exception e) {
            log.warn("WebSocket push failed for user {}", userId);
        }
    }

    private String getStatusTitle(String status) {
        return switch (status) {
            case "CONFIRMED" -> "✅ Order Confirmed";
            case "PROCESSING" -> "👨‍🍳 Order Being Prepared";
            case "READY_FOR_PICKUP" -> "📦 Ready for Pickup";
            case "OUT_FOR_DELIVERY" -> "🚚 On the Way";
            case "DELIVERED" -> "🎉 Order Delivered";
            case "CANCELLED" -> "❌ Order Cancelled";
            default -> "📋 Order Update";
        };
    }

    private String formatStatus(String status) {
        return switch (status) {
            case "CONFIRMED" -> "Confirmed";
            case "PROCESSING" -> "Being Prepared";
            case "READY_FOR_PICKUP" -> "Ready for Pickup";
            case "OUT_FOR_DELIVERY" -> "Out for Delivery";
            case "DELIVERED" -> "Delivered";
            case "CANCELLED" -> "Cancelled";
            default -> status;
        };
    }
}