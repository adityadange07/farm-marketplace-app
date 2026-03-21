package com.farmmarket.controller;

import com.farmmarket.dto.response.ApiResponse;
import com.farmmarket.entity.Order;
import com.farmmarket.repository.OrderRepository;
import com.farmmarket.security.CustomUserDetails;
import com.farmmarket.service.PaymentService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;

    @PostMapping("/connect")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ApiResponse<Map<String, String>>> connectStripe(
            @AuthenticationPrincipal CustomUserDetails user) {
        String url = paymentService.createConnectedAccount(
                user.getEmail(), user.getId());
        return ResponseEntity.ok(
                ApiResponse.success(Map.of("onboardingUrl", url)));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            Event event = Webhook.constructEvent(
                    payload, sigHeader, paymentService.getWebhookSecret());

            if ("payment_intent.succeeded".equals(event.getType())) {
                PaymentIntent intent = (PaymentIntent)
                        event.getDataObjectDeserializer()
                                .getObject().orElse(null);

                if (intent != null) {
                    String orderIdsStr = intent.getMetadata().get("orderIds");
                    if (orderIdsStr != null) {
                        Arrays.stream(orderIdsStr.split(","))
                                .map(UUID::fromString)
                                .forEach(orderId -> {
                                    orderRepository.findById(orderId)
                                            .ifPresent(order -> {
                                                order.setPaymentStatus("paid");
                                                orderRepository.save(order);
                                            });
                                });
                    }
                }
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Webhook error", e);
            return ResponseEntity.badRequest().build();
        }
    }
}