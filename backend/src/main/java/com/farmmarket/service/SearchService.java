package com.farmmarket.service;

import com.farmmarket.dto.response.PagedResponse;
import com.farmmarket.dto.response.ProductResponse;
import com.farmmarket.entity.Product;
import com.farmmarket.mapper.ProductMapper;
import com.farmmarket.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Search products using MySQL FULLTEXT index.
     * Falls back to LIKE-based search on error.
     */
    public PagedResponse<ProductResponse> searchProducts(
            String query,
            String category,
            Double minPrice,
            Double maxPrice,
            Boolean isOrganic,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> results;

        try {
            // Build MySQL boolean mode query
            // "+organic +tomato*" means must contain both
            String booleanQuery = buildBooleanQuery(query);

            results = productRepository.searchProductsFulltext(
                    booleanQuery, category, minPrice, maxPrice, isOrganic,
                    pageable);
        } catch (Exception e) {
            log.warn("FULLTEXT search failed, falling back to LIKE: {}",
                    e.getMessage());
            results = productRepository.searchProducts(
                    query, category, minPrice, maxPrice, isOrganic,
                    pageable);
        }

        return PagedResponse.<ProductResponse>builder()
                .data(results.map(productMapper::toResponse).getContent())
                .page(page)
                .size(size)
                .totalElements(results.getTotalElements())
                .totalPages(results.getTotalPages())
                .hasMore(results.hasNext())
                .build();
    }

    /**
     * Convert user query to MySQL FULLTEXT boolean mode query
     * "organic tomatoes" → "+organic* +tomatoes*"
     */
    private String buildBooleanQuery(String query) {
        if (query == null || query.isBlank()) return "";

        String[] words = query.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isBlank()) {
                sb.append("+").append(word).append("* ");
            }
        }
        return sb.toString().trim();
    }
}