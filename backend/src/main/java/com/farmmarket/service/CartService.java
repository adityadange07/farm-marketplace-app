package com.farmmarket.service;

import com.farmmarket.dto.request.AddToCartRequest;
import com.farmmarket.dto.response.CartResponse;
import com.farmmarket.entity.CartItem;
import com.farmmarket.entity.Product;
import com.farmmarket.entity.User;
import com.farmmarket.exception.BadRequestException;
import com.farmmarket.exception.ResourceNotFoundException;
import com.farmmarket.repository.CartItemRepository;
import com.farmmarket.repository.ProductRepository;
import com.farmmarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartResponse getCart(UUID userId) {
        List<CartItem> items = cartItemRepository.findByUserIdWithProduct(userId);

        List<CartResponse.CartItemResponse> cartItems = items.stream()
                .map(this::mapCartItem)
                .collect(Collectors.toList());

        BigDecimal subtotal = cartItems.stream()
                .map(CartResponse.CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(cartItems)
                .totalItems(items.stream()
                        .mapToInt(CartItem::getQuantity).sum())
                .subtotal(subtotal)
                .build();
    }

    @Transactional
    public CartResponse addToCart(UUID userId, AddToCartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Not enough stock");
        }

        CartItem existing = cartItemRepository
                .findByUserIdAndProductId(userId, request.getProductId())
                .orElse(null);

        if (existing != null) {
            int newQty = existing.getQuantity() + request.getQuantity();
            if (newQty > product.getMaxOrderQuantity()) {
                throw new BadRequestException(
                        "Max " + product.getMaxOrderQuantity() + " per order");
            }
            existing.setQuantity(newQty);
            cartItemRepository.save(existing);
        } else {
            User user = userRepository.getReferenceById(userId);
            CartItem cartItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(cartItem);
        }

        return getCart(userId);
    }

    @Transactional
    public CartResponse updateQuantity(UUID userId, UUID itemId, int quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not your cart item");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            if (quantity > item.getProduct().getStockQuantity()) {
                throw new BadRequestException("Not enough stock");
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return getCart(userId);
    }

    @Transactional
    public CartResponse removeItem(UUID userId, UUID itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not your cart item");
        }

        cartItemRepository.delete(item);
        return getCart(userId);
    }

    @Transactional
    public void clearCart(UUID userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartResponse.CartItemResponse mapCartItem(CartItem item) {
        Product p = item.getProduct();
        String image = (p.getImages() != null && !p.getImages().isEmpty())
                ? p.getImages().stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsPrimary()))
                .findFirst()
                .orElse(p.getImages().get(0))
                .getUrl()
                : null;

        return CartResponse.CartItemResponse.builder()
                .id(item.getId())
                .productId(p.getId())
                .productName(p.getName())
                .productSlug(p.getSlug())
                .productImage(image)
                .price(p.getPrice())
                .unit(p.getUnit().name())
                .quantity(item.getQuantity())
                .stockQuantity(p.getStockQuantity())
                .lineTotal(p.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .farm(CartResponse.FarmSummary.builder()
                        .id(p.getFarm().getId())
                        .farmName(p.getFarm().getFarmName())
                        .build())
                .build();
    }
}