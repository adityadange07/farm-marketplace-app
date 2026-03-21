package com.farmmarket.service;

import com.farmmarket.dto.request.CreateOrderRequest;
import com.farmmarket.dto.response.OrderResponse;
import com.farmmarket.dto.response.PagedResponse;
import com.farmmarket.entity.*;
import com.farmmarket.enums.OrderStatus;
import com.farmmarket.exception.BadRequestException;
import com.farmmarket.exception.InsufficientStockException;
import com.farmmarket.exception.ResourceNotFoundException;
import com.farmmarket.mapper.OrderMapper;
import com.farmmarket.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final FarmRepository farmRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final OrderMapper orderMapper;

    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.05");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");
    private static final BigDecimal BASE_DELIVERY_FEE = new BigDecimal("5.99");

    // ═══════════════════════════════════════════
    // CREATE ORDER
    // ═══════════════════════════════════════════

    @Transactional
    public Map<String, Object> createOrder(UUID consumerId,
                                           CreateOrderRequest request) {
        // 1. Get cart items
        List<CartItem> cartItems = cartItemRepository
                .findByUserIdWithProduct(consumerId);

        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        // 2. Group by farm
        Map<UUID, List<CartItem>> farmGroups = cartItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getFarm().getId()));

        List<OrderResponse> orderResponses = new ArrayList<>();
        List<UUID> orderIds = new ArrayList<>();

        for (Map.Entry<UUID, List<CartItem>> entry : farmGroups.entrySet()) {
            UUID farmId = entry.getKey();
            List<CartItem> items = entry.getValue();

            // 3. Verify stock
            verifyStock(items);

            // 4. Calculate totals
            BigDecimal subtotal = calculateSubtotal(items);
            BigDecimal deliveryFee = calculateDeliveryFee(
                    request.getDeliveryType());
            BigDecimal serviceFee = subtotal
                    .multiply(PLATFORM_FEE_RATE)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal tax = subtotal
                    .multiply(TAX_RATE)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = subtotal
                    .add(deliveryFee)
                    .add(serviceFee)
                    .add(tax);

            // 5. Create order
            Farm farm = farmRepository.findById(farmId)
                    .orElseThrow(() -> new ResourceNotFoundException("Farm not found"));

            User consumer = items.get(0).getUser();

            Order order = Order.builder()
                    .orderNumber(generateOrderNumber())
                    .consumer(consumer)
                    .farm(farm)
                    .subtotal(subtotal)
                    .deliveryFee(deliveryFee)
                    .serviceFee(serviceFee)
                    .tax(tax)
                    .total(total)
                    .deliveryType(request.getDeliveryType())
                    .deliveryAddress(request.getDeliveryAddress())
                    .deliveryNotes(request.getDeliveryNotes())
                    .scheduledDate(request.getScheduledDate())
                    .scheduledTimeSlot(request.getTimeSlot())
                    .status(OrderStatus.PENDING)
                    .placedAt(LocalDateTime.now())
                    .build();

            order.addStatusEntry(OrderStatus.PENDING, "Order placed");
            order = orderRepository.save(order);

            // 6. Create order items + reduce stock
            for (CartItem cartItem : items) {
                Product product = cartItem.getProduct();

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .productName(product.getName())
                        .unit(product.getUnit())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(product.getPrice())
                        .totalPrice(product.getPrice()
                                .multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                        .build();

                orderItemRepository.save(orderItem);

                // Reduce stock
                product.setStockQuantity(
                        product.getStockQuantity() - cartItem.getQuantity());
                product.setTotalSold(
                        product.getTotalSold() + cartItem.getQuantity());
                productRepository.save(product);
            }

            orderIds.add(order.getId());
            orderResponses.add(orderMapper.toResponse(order));
        }

        // 7. Create Stripe payment intent
        BigDecimal totalAmount = orderResponses.stream()
                .map(OrderResponse::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> paymentIntent = paymentService
                .createPaymentIntent(totalAmount, orderIds);

        // 8. Clear cart
        cartItemRepository.deleteByUserId(consumerId);

        // 9. Send notifications
        for (OrderResponse order : orderResponses) {
            notificationService.notifyNewOrder(order);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orders", orderResponses);
        result.put("clientSecret", paymentIntent.get("clientSecret"));
        return result;
    }

    // ═══════════════════════════════════════════
    // UPDATE ORDER STATUS (Farmer)
    // ═══════════════════════════════════════════

    @Transactional
    public OrderResponse updateOrderStatus(UUID farmerId, UUID orderId,
                                           OrderStatus newStatus,
                                           String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Verify farmer owns this order
        if (!order.getFarm().getFarmer().getId().equals(farmerId)) {
            throw new BadRequestException("Not authorized");
        }

        // Validate transition
        validateStatusTransition(order.getStatus(), newStatus);

        // Update status
        order.setStatus(newStatus);
        order.addStatusEntry(newStatus, note);

        switch (newStatus) {
            case CONFIRMED -> order.setConfirmedAt(LocalDateTime.now());
            case DELIVERED -> {
                order.setDeliveredAt(LocalDateTime.now());
                // Trigger payout to farmer
                paymentService.transferToFarmer(order);
            }
            case CANCELLED -> {
                order.setCancelledAt(LocalDateTime.now());
                order.setCancellationReason(note);
                restoreStock(order);
            }
        }

        order = orderRepository.save(order);

        // Notify consumer
        notificationService.notifyOrderStatusUpdate(order);

        return orderMapper.toResponse(order);
    }

    // ═══════════════════════════════════════════
    // GET ORDERS
    // ═══════════════════════════════════════════

    public PagedResponse<OrderResponse> getConsumerOrders(
            UUID consumerId, int page, int size) {

        Page<Order> orderPage = orderRepository
                .findByConsumerIdOrderByCreatedAtDesc(
                        consumerId,
                        PageRequest.of(page, size));

        return mapOrderPage(orderPage, page, size);
    }

    public PagedResponse<OrderResponse> getFarmerOrders(
            UUID farmerId, OrderStatus status, int page, int size) {

        Farm farm = farmRepository.findByFarmerId(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found"));

        Page<Order> orderPage;
        if (status != null) {
            orderPage = orderRepository
                    .findByFarmIdAndStatusOrderByCreatedAtDesc(
                            farm.getId(), status, PageRequest.of(page, size));
        } else {
            orderPage = orderRepository
                    .findByFarmIdOrderByCreatedAtDesc(
                            farm.getId(), PageRequest.of(page, size));
        }

        return mapOrderPage(orderPage, page, size);
    }

    public OrderResponse getOrderDetails(UUID userId, UUID orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Consumer or farmer can view
        boolean isConsumer = order.getConsumer().getId().equals(userId);
        boolean isFarmer = order.getFarm().getFarmer().getId().equals(userId);

        if (!isConsumer && !isFarmer) {
            throw new BadRequestException("Not authorized");
        }

        return orderMapper.toDetailResponse(order);
    }

    // ═══════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════

    private void verifyStock(List<CartItem> items) {
        for (CartItem item : items) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for: " + product.getName() +
                                ". Available: " + product.getStockQuantity());
            }
        }
    }

    private BigDecimal calculateSubtotal(List<CartItem> items) {
        return items.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDeliveryFee(
            com.farmmarket.enums.DeliveryType type) {
        if (type == com.farmmarket.enums.DeliveryType.PICKUP) {
            return BigDecimal.ZERO;
        }
        return BASE_DELIVERY_FEE;
    }

    private void validateStatusTransition(OrderStatus current,
                                          OrderStatus next) {
        Map<OrderStatus, List<OrderStatus>> validTransitions = Map.of(
                OrderStatus.PENDING,
                List.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
                OrderStatus.CONFIRMED,
                List.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
                OrderStatus.PROCESSING,
                List.of(OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELLED),
                OrderStatus.READY_FOR_PICKUP,
                List.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED),
                OrderStatus.OUT_FOR_DELIVERY,
                List.of(OrderStatus.DELIVERED),
                OrderStatus.DELIVERED, List.of(),
                OrderStatus.CANCELLED, List.of()
        );

        if (!validTransitions.getOrDefault(current, List.of()).contains(next)) {
            throw new BadRequestException(
                    "Cannot transition from " + current + " to " + next);
        }
    }

    private void restoreStock(Order order) {
        List<OrderItem> items = orderItemRepository
                .findByOrderId(order.getId());
        for (OrderItem item : items) {
            Product product = item.getProduct();
            product.setStockQuantity(
                    product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }

    private String generateOrderNumber() {
        return "FM-" + UUID.randomUUID().toString()
                .substring(0, 10).toUpperCase();
    }

    private PagedResponse<OrderResponse> mapOrderPage(
            Page<Order> page, int pageNum, int size) {
        return PagedResponse.<OrderResponse>builder()
                .data(page.map(orderMapper::toResponse).getContent())
                .page(pageNum)
                .size(size)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasMore(page.hasNext())
                .build();
    }
}