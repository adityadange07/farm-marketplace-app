package com.farmmarket.service;

import com.farmmarket.dto.response.OrderResponse;
import com.farmmarket.entity.Order;
import com.farmmarket.entity.User;
import com.farmmarket.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final UserRepository userRepository;

    @Value("${spring.mail.username:noreply@farmfresh.com}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    // ═══ 1. Verification Email ═══
    @Async
    public void sendVerificationEmail(User user) {
        try {
            String token = UUID.randomUUID().toString();
            Context ctx = new Context();
            ctx.setVariable("name", user.getFirstName());
            ctx.setVariable("verifyUrl",
                    frontendUrl + "/verify?token=" + token);
            ctx.setVariable("year", LocalDateTime.now().getYear());

            sendHtmlEmail(user.getEmail(),
                    "Welcome to FarmFresh — Verify Your Email",
                    "welcome", ctx);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}",
                    user.getEmail(), e);
        }
    }

    // ═══ 2. Order Confirmation → Consumer ═══
    @Async
    public void sendOrderConfirmationToConsumer(OrderResponse order) {
        try {
            Context ctx = buildOrderContext(order);
            ctx.setVariable("consumerName", order.getConsumer() != null
                    ? order.getConsumer().getFirstName() : "Customer");
            ctx.setVariable("orderUrl",
                    frontendUrl + "/orders/" + order.getId());

            String email = order.getConsumer() != null
                    ? order.getConsumer().getEmail()
                    : order.getConsumerEmail();

            if (email != null) {
                sendHtmlEmail(email,
                        "Order Confirmed — " + order.getOrderNumber(),
                        "order-confirmation-consumer", ctx);
                log.info("Consumer order confirmation sent: {}",
                        order.getOrderNumber());
            }
        } catch (Exception e) {
            log.error("Failed: consumer confirmation {}",
                    order.getOrderNumber(), e);
        }
    }

    // ═══ 3. Order Confirmation → Farmer ═══ ★★★
    @Async
    public void sendOrderConfirmationToFarmer(OrderResponse order) {
        try {
            // Resolve farmer email
            String farmerEmail = resolveFarmerEmail(order);
            if (farmerEmail == null) {
                log.warn("No farmer email for order {}",
                        order.getOrderNumber());
                return;
            }

            Context ctx = buildOrderContext(order);
            ctx.setVariable("farmerName", order.getFarm() != null
                    ? order.getFarm().getFarmName() : "Farmer");
            ctx.setVariable("consumerFullName", order.getConsumer() != null
                    ? order.getConsumer().getFirstName() + " "
                    + order.getConsumer().getLastName()
                    : "Customer");
            ctx.setVariable("consumerEmail", order.getConsumer() != null
                    ? order.getConsumer().getEmail() : "");
            ctx.setVariable("farmerPayout",
                    order.getTotal().subtract(
                            order.getServiceFee() != null
                                    ? order.getServiceFee()
                                    : java.math.BigDecimal.ZERO));
            ctx.setVariable("dashboardUrl",
                    frontendUrl + "/farmer/orders");

            sendHtmlEmail(farmerEmail,
                    "🆕 New Order! — " + order.getOrderNumber(),
                    "order-confirmation-farmer", ctx);
            log.info("Farmer order notification sent: {}",
                    order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed: farmer confirmation {}",
                    order.getOrderNumber(), e);
        }
    }

    // ═══ 4. Order Status Update → Consumer ═══
    @Async
    public void sendOrderStatusUpdate(Order order) {
        try {
            Context ctx = new Context();
            ctx.setVariable("consumerName",
                    order.getConsumer().getFirstName());
            ctx.setVariable("orderNumber", order.getOrderNumber());
            ctx.setVariable("status",
                    formatStatus(order.getStatus().name()));
            ctx.setVariable("statusRaw", order.getStatus().name());
            ctx.setVariable("statusMessage",
                    getStatusMessage(order.getStatus().name()));
            ctx.setVariable("statusEmoji",
                    getStatusEmoji(order.getStatus().name()));
            ctx.setVariable("farmName",
                    order.getFarm().getFarmName());
            ctx.setVariable("total", order.getTotal());
            ctx.setVariable("orderUrl",
                    frontendUrl + "/orders/" + order.getId());
            ctx.setVariable("year", LocalDateTime.now().getYear());

            if (order.getCancellationReason() != null) {
                ctx.setVariable("cancellationReason",
                        order.getCancellationReason());
            }

            sendHtmlEmail(order.getConsumer().getEmail(),
                    getStatusEmoji(order.getStatus().name())
                            + " Order Update — " + order.getOrderNumber(),
                    "order-status-update", ctx);
            log.info("Status update sent: {} → {}",
                    order.getOrderNumber(), order.getStatus());
        } catch (Exception e) {
            log.error("Failed: status update {}",
                    order.getOrderNumber(), e);
        }
    }

    // ═══ 5. Password Reset ═══
    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name", user.getFirstName());
            ctx.setVariable("resetUrl",
                    frontendUrl + "/reset-password?token=" + resetToken);
            ctx.setVariable("expiresIn", "1 hour");
            ctx.setVariable("year", LocalDateTime.now().getYear());

            sendHtmlEmail(user.getEmail(),
                    "Reset Your FarmFresh Password",
                    "password-reset", ctx);
        } catch (Exception e) {
            log.error("Failed: password reset email to {}",
                    user.getEmail(), e);
        }
    }

    // ═══ 6. Delivery + Review Prompt ═══
    @Async
    public void sendDeliveryAndReviewPrompt(Order order) {
        try {
            Context ctx = new Context();
            ctx.setVariable("consumerName",
                    order.getConsumer().getFirstName());
            ctx.setVariable("orderNumber", order.getOrderNumber());
            ctx.setVariable("farmName",
                    order.getFarm().getFarmName());
            ctx.setVariable("reviewUrl",
                    frontendUrl + "/orders/" + order.getId()
                            + "?review=true");
            ctx.setVariable("year", LocalDateTime.now().getYear());

            sendHtmlEmail(order.getConsumer().getEmail(),
                    "✅ Delivered! Rate your experience",
                    "order-delivered-review", ctx);
        } catch (Exception e) {
            log.error("Failed: delivery email {}", order.getOrderNumber(), e);
        }
    }

    // ═══ 7. Low Stock Alert → Farmer ═══
    @Async
    public void sendLowStockAlert(User farmer, String productName,
                                  int currentStock) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name", farmer.getFirstName());
            ctx.setVariable("productName", productName);
            ctx.setVariable("currentStock", currentStock);
            ctx.setVariable("productsUrl",
                    frontendUrl + "/farmer/products");
            ctx.setVariable("year", LocalDateTime.now().getYear());

            sendHtmlEmail(farmer.getEmail(),
                    "⚠️ Low Stock — " + productName,
                    "low-stock-alert", ctx);
        } catch (Exception e) {
            log.error("Failed: low stock alert to {}",
                    farmer.getEmail(), e);
        }
    }

    // ═══════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════

    private void sendHtmlEmail(String to, String subject,
                               String template, Context context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            String html = templateEngine.process(template, context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom(fromEmail, "FarmFresh Marketplace");

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Email send failed to {} — {}", to, subject, e);
        } catch (Exception e) {
            log.error("Unexpected email error to {}", to, e);
        }
    }

    private Context buildOrderContext(OrderResponse order) {
        Context ctx = new Context();
        ctx.setVariable("orderNumber", order.getOrderNumber());
        ctx.setVariable("farmName", order.getFarm() != null
                ? order.getFarm().getFarmName() : "Farm");
        ctx.setVariable("items", order.getItems());
        ctx.setVariable("itemCount", order.getItems() != null
                ? order.getItems().size() : 0);
        ctx.setVariable("subtotal", order.getSubtotal());
        ctx.setVariable("deliveryFee", order.getDeliveryFee());
        ctx.setVariable("serviceFee", order.getServiceFee());
        ctx.setVariable("tax", order.getTax());
        ctx.setVariable("discount", order.getDiscount());
        ctx.setVariable("total", order.getTotal());
        ctx.setVariable("deliveryType",
                formatDeliveryType(order.getDeliveryType()));
        ctx.setVariable("scheduledDate", order.getScheduledDate());
        ctx.setVariable("scheduledTimeSlot",
                order.getScheduledTimeSlot());
        ctx.setVariable("deliveryAddress", order.getDeliveryAddress());
        ctx.setVariable("year", LocalDateTime.now().getYear());
        return ctx;
    }

    private String resolveFarmerEmail(OrderResponse order) {
        if (order.getFarm() == null) return null;
        UUID farmerId = order.getFarm().getFarmerId();
        if (farmerId == null) return null;
        return userRepository.findById(farmerId)
                .map(User::getEmail)
                .orElse(null);
    }

    private String formatStatus(String status) {
        return switch (status) {
            case "PENDING" -> "Pending";
            case "CONFIRMED" -> "Confirmed";
            case "PROCESSING" -> "Being Prepared";
            case "READY_FOR_PICKUP" -> "Ready for Pickup";
            case "OUT_FOR_DELIVERY" -> "Out for Delivery";
            case "DELIVERED" -> "Delivered";
            case "CANCELLED" -> "Cancelled";
            case "REFUNDED" -> "Refunded";
            default -> status;
        };
    }

    private String formatDeliveryType(String type) {
        if (type == null) return "Delivery";
        return switch (type) {
            case "DELIVERY" -> "Home Delivery";
            case "PICKUP" -> "Farm Pickup";
            case "FARMER_MARKET" -> "Farmer's Market";
            default -> type;
        };
    }

    private String getStatusMessage(String status) {
        return switch (status) {
            case "CONFIRMED" ->
                    "Great news! The farmer has confirmed your order.";
            case "PROCESSING" ->
                    "Your order is being prepared. Fresh produce on the way!";
            case "READY_FOR_PICKUP" ->
                    "Your order is ready for pickup at the farm.";
            case "OUT_FOR_DELIVERY" ->
                    "Your order is on its way!";
            case "DELIVERED" ->
                    "Your order has been delivered. Enjoy!";
            case "CANCELLED" ->
                    "Your order has been cancelled. You will be refunded.";
            default -> "Your order status has been updated.";
        };
    }

    private String getStatusEmoji(String status) {
        return switch (status) {
            case "CONFIRMED" -> "✅";
            case "PROCESSING" -> "👨‍🍳";
            case "READY_FOR_PICKUP" -> "📦";
            case "OUT_FOR_DELIVERY" -> "🚚";
            case "DELIVERED" -> "🎉";
            case "CANCELLED" -> "❌";
            case "REFUNDED" -> "💰";
            default -> "📋";
        };
    }
}