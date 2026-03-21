package com.farmmarket.mapper;

import com.farmmarket.dto.response.ProductResponse;
import com.farmmarket.entity.Product;
import com.farmmarket.entity.ProductImage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        if (product == null) return null;

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .shortDescription(product.getShortDescription())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .unit(product.getUnit().name())
                .stockQuantity(product.getStockQuantity())
                .lowStockThreshold(product.getLowStockThreshold())
                .maxOrderQuantity(product.getMaxOrderQuantity())
                .isOrganic(product.getIsOrganic())
                .isSeasonal(product.getIsSeasonal())
                .status(product.getStatus().name())
                .avgRating(product.getAvgRating())
                .reviewCount(product.getReviewCount())
                .totalSold(product.getTotalSold())
                .distanceKm(product.getDistanceKm())
                .images(mapImages(product.getImages()))
                .farm(mapFarmSummary(product))
                .category(mapCategory(product))
                .createdAt(product.getCreatedAt())
                .build();
    }

    public ProductResponse toDetailResponse(Product product) {
        ProductResponse response = toResponse(product);
        if (response != null) {
            response.setDescription(product.getDescription());
            response.setGrowingMethod(product.getGrowingMethod());
            response.setHarvestDate(product.getHarvestDate());
            response.setViewCount(product.getViewCount());
        }
        return response;
    }

    private List<ProductResponse.ImageResponse> mapImages(
            List<ProductImage> images) {
        if (images == null) return List.of();
        return images.stream()
                .map(img -> ProductResponse.ImageResponse.builder()
                        .url(img.getUrl())
                        .altText(img.getAltText())
                        .isPrimary(img.getIsPrimary())
                        .build())
                .collect(Collectors.toList());
    }

    private ProductResponse.FarmSummary mapFarmSummary(Product product) {
        if (product.getFarm() == null) return null;
        return ProductResponse.FarmSummary.builder()
                .id(product.getFarm().getId())
                .farmName(product.getFarm().getFarmName())
                .city(product.getFarm().getCity())
                .state(product.getFarm().getState())
                .isOrganic(product.getFarm().getIsOrganic())
                .rating(product.getFarm().getRating())
                .build();
    }

    private ProductResponse.CategorySummary mapCategory(Product product) {
        if (product.getCategory() == null) return null;
        return ProductResponse.CategorySummary.builder()
                .id(product.getCategory().getId())
                .name(product.getCategory().getName())
                .slug(product.getCategory().getSlug())
                .build();
    }
}