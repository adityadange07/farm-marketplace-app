package com.farmmarket.service;

import com.farmmarket.entity.Farm;
import com.farmmarket.entity.Order;
import com.farmmarket.repository.FarmRepository;
import com.farmmarket.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Transfer;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.TransferCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final FarmRepository farmRepository;
    private final OrderRepository orderRepository;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    // ── Create Payment Intent ─────────────────
    public Map<String, Object> createPaymentIntent(
            BigDecimal amount, List<UUID> orderIds) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency("usd")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .putMetadata("orderIds",
                            orderIds.stream()
                                    .map(UUID::toString)
                                    .reduce((a, b) -> a + "," + b)
                                    .orElse(""))
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            Map<String, Object> result = new HashMap<>();
            result.put("clientSecret", intent.getClientSecret());
            result.put("paymentIntentId", intent.getId());
            return result;
        } catch (Exception e) {
            log.error("Stripe payment intent creation failed", e);
            throw new RuntimeException("Payment processing failed");
        }
    }

    // ══════════════════════════════════════════
    // ★★★ STRIPE CONNECT — FIXED JPA STYLE ★★★
    // ══════════════════════════════════════════

    @Transactional
    public String createConnectedAccount(String email, UUID farmerId) {
        try {
            // 1. Create Stripe Express account
            AccountCreateParams params = AccountCreateParams.builder()
                    .setType(AccountCreateParams.Type.EXPRESS)
                    .setEmail(email)
                    .setCapabilities(
                            AccountCreateParams.Capabilities.builder()
                                    .setCardPayments(
                                            AccountCreateParams.Capabilities.CardPayments
                                                    .builder()
                                                    .setRequested(true).build())
                                    .setTransfers(
                                            AccountCreateParams.Capabilities.Transfers
                                                    .builder()
                                                    .setRequested(true).build())
                                    .build())
                    .putMetadata("farmerId", farmerId.toString())
                    .build();

            Account account = Account.create(params);

            // 2. ★★★ FIXED: Save Stripe account ID using JPA ★★★
            //    Old (wrong):  farmsRepo.update({ farmerId }, { stripeAccountId: ... })
            //    New (correct): find entity → set field → save

            Farm farm = farmRepository.findByFarmerId(farmerId)
                    .orElseThrow(() -> new RuntimeException(
                            "Farm not found for farmer: " + farmerId));

            farm.setStripeAccountId(account.getId());
            farmRepository.save(farm);

            log.info("Stripe account {} created for farm {}",
                    account.getId(), farm.getId());

            // 3. Generate onboarding link
            AccountLinkCreateParams linkParams =
                    AccountLinkCreateParams.builder()
                            .setAccount(account.getId())
                            .setRefreshUrl(frontendUrl + "/farmer/settings")
                            .setReturnUrl(frontendUrl
                                    + "/farmer/settings?stripe=success")
                            .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                            .build();

            AccountLink link = AccountLink.create(linkParams);
            return link.getUrl();

        } catch (Exception e) {
            log.error("Stripe Connect onboarding failed for farmer {}",
                    farmerId, e);
            throw new RuntimeException("Payment onboarding failed: "
                    + e.getMessage());
        }
    }

    // ── Transfer funds to farmer after delivery ──
    public void transferToFarmer(Order order) {
        try {
            String farmStripeId = order.getFarm().getStripeAccountId();
            if (farmStripeId == null || farmStripeId.isBlank()) {
                log.warn("Farm {} has no Stripe account, skipping transfer",
                        order.getFarm().getId());
                return;
            }

            BigDecimal platformFee = order.getServiceFee() != null
                    ? order.getServiceFee() : BigDecimal.ZERO;
            BigDecimal farmerAmount = order.getTotal().subtract(platformFee);
            long amountCents = farmerAmount
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            if (amountCents <= 0) {
                log.warn("Transfer amount <= 0 for order {}",
                        order.getOrderNumber());
                return;
            }

            TransferCreateParams params = TransferCreateParams.builder()
                    .setAmount(amountCents)
                    .setCurrency("usd")
                    .setDestination(farmStripeId)
                    .putMetadata("orderId", order.getId().toString())
                    .putMetadata("orderNumber", order.getOrderNumber())
                    .build();

            Transfer transfer = Transfer.create(params);
            log.info("Transfer {} (${}), order {}",
                    transfer.getId(),
                    farmerAmount,
                    order.getOrderNumber());

        } catch (Exception e) {
            log.error("Stripe transfer failed, order {}",
                    order.getOrderNumber(), e);
        }
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }
}