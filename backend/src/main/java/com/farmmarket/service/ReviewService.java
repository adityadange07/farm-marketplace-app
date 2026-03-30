package com.farmmarket.service;

import com.farmmarket.dto.request.CreateReviewRequest;
import com.farmmarket.dto.response.PagedResponse;
import com.farmmarket.dto.response.ReviewResponse;
import com.farmmarket.entity.*;
import com.farmmarket.enums.OrderStatus;
import com.farmmarket.exception.BadRequestException;
import com.farmmarket.exception.ResourceNotFoundException;
import com.farmmarket.monitoring.metrics.BusinessMetricsService;
import com.farmmarket.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BusinessMetricsService metricsService;

    @Transactional
    public ReviewResponse createReview(UUID consumerId,
                                       CreateReviewRequest request) {
        // Verify order belongs to consumer and is delivered
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getConsumer().getId().equals(consumerId)) {
            throw new BadRequestException("Not your order");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Can only review delivered orders");
        }

        // Check duplicate
        if (reviewRepository.existsByOrderIdAndProductIdAndConsumerId(
                request.getOrderId(), request.getProductId(), consumerId)) {
            throw new BadRequestException("Already reviewed this product");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        User consumer = userRepository.getReferenceById(consumerId);

        Review review = Review.builder()
                .order(order)
                .product(product)
                .farm(product.getFarm())
                .consumer(consumer)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .build();

        review = reviewRepository.save(review);

        // Update product average rating
        Double avgRating = reviewRepository
                .getAverageRatingByProductId(product.getId());
        int count = reviewRepository.countByProductId(product.getId());
        product.setAvgRating(BigDecimal.valueOf(avgRating != null ? avgRating : 0));
        product.setReviewCount(count);
        productRepository.save(product);

        metricsService.recordReviewCreated(request.getRating());

        return mapToResponse(review);
    }

    public PagedResponse<ReviewResponse> getProductReviews(
            UUID productId, int page, int size) {
        Page<Review> reviewPage = reviewRepository
                .findByProductIdAndIsVisibleTrueOrderByCreatedAtDesc(
                        productId, PageRequest.of(page, size));

        return PagedResponse.<ReviewResponse>builder()
                .data(reviewPage.map(this::mapToResponse).getContent())
                .page(page)
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .hasMore(reviewPage.hasNext())
                .build();
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .images(review.getImages())
                .consumerName(review.getConsumer().getFirstName() + " "
                        + review.getConsumer().getLastName().charAt(0) + ".")
                .consumerAvatar(review.getConsumer().getAvatarUrl())
                .isVerified(review.getIsVerified())
                .farmerReply(review.getFarmerReply())
                .repliedAt(review.getRepliedAt())
                .createdAt(review.getCreatedAt())
                .build();
    }
}