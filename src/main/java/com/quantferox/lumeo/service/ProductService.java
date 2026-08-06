package com.quantferox.lumeo.service;

import com.quantferox.lumeo.config.CacheConfig;
import com.quantferox.lumeo.domain.entity.Category;
import com.quantferox.lumeo.domain.entity.Product;
import com.quantferox.lumeo.dto.request.ProductRequest;
import com.quantferox.lumeo.dto.response.PageResponse;
import com.quantferox.lumeo.dto.response.ProductResponse;
import com.quantferox.lumeo.exception.DuplicateResourceException;
import com.quantferox.lumeo.exception.ResourceNotFoundException;
import com.quantferox.lumeo.mapper.ProductMapper;
import com.quantferox.lumeo.repository.CategoryRepository;
import com.quantferox.lumeo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository    productRepository;
    private final CategoryRepository   categoryRepository;
    private final ProductMapper        productMapper;
    private final ApplicationEventPublisher eventPublisher;

    // ── Queries ───────────────────────────────────────────────────────────

    public PageResponse<ProductResponse> findAll(Pageable pageable) {
        Page<ProductResponse> page = productRepository
                .findAllByActiveTrue(pageable)
                .map(productMapper::toResponse);
        return PageResponse.of(page);
    }

    public PageResponse<ProductResponse> findByCategory(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", categoryId);
        }
        Page<ProductResponse> page = productRepository
                .findAllByCategoryIdAndActiveTrue(categoryId, pageable)
                .map(productMapper::toResponse);
        return PageResponse.of(page);
    }

    public PageResponse<ProductResponse> search(String query, Pageable pageable) {
        Page<ProductResponse> page = productRepository
                .search(query, pageable)
                .map(productMapper::toResponse);
        return PageResponse.of(page);
    }

    @Cacheable(value = CacheConfig.CACHE_PRODUCTS, key = "#id")
    public ProductResponse findById(Long id) {
        return productMapper.toResponse(getOrThrow(id));
    }

    @Cacheable(value = CacheConfig.CACHE_PRODUCT_SLUG, key = "#slug")
    public ProductResponse findBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        return productMapper.toResponse(product);
    }

    @Cacheable(value = CacheConfig.CACHE_FEATURED, key = "'featured'")
    public List<ProductResponse> findFeatured() {
        log.debug("Cache MISS - loading featured products from DB");
        return productMapper.toResponseList(
                productRepository.findAllByFeaturedTrueAndActiveTrue());
    }

    public List<ProductResponse> findLowStock(int threshold) {
        return productMapper.toResponseList(
                productRepository.findLowStock(threshold));
    }

    // ── Commands ──────────────────────────────────────────────────────────

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_PRODUCTS,  allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_FEATURED,  allEntries = true)
    })
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product", "sku", request.getSku());
        }
        if (productRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("Product", "slug", request.getSlug());
        }
        Product product = productMapper.toEntity(request);
        product.setCategory(resolveCategory(request.getCategoryId()));
        Product saved = productRepository.save(product);
        log.info("Created product id={} sku={}", saved.getId(), saved.getSku());
        return productMapper.toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_PRODUCTS,     key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_PRODUCT_SLUG, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_FEATURED,     allEntries = true)
    })
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getOrThrow(id);
        boolean skuChanged = !product.getSku().equals(request.getSku());
        if (skuChanged && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product", "sku", request.getSku());
        }
        boolean slugChanged = !product.getSlug().equals(request.getSlug());
        if (slugChanged && productRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("Product", "slug", request.getSlug());
        }
        productMapper.updateEntity(request, product);
        product.setCategory(resolveCategory(request.getCategoryId()));
        log.info("Updated product id={}", id);
        return productMapper.toResponse(product);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_PRODUCTS,     key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_PRODUCT_SLUG, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_FEATURED,     allEntries = true)
    })
    public void delete(Long id) {
        Product product = getOrThrow(id);
        product.setActive(false);
        log.info("Soft-deleted product id={}", id);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_PRODUCTS, key = "#id")
    public ProductResponse adjustStock(Long id, int delta) {
        Product product = getOrThrow(id);
        int newQty = product.getStockQuantity() + delta;
        if (newQty < 0) {
            throw new IllegalArgumentException(
                    "Stock adjustment would result in negative quantity for product id=" + id);
        }
        product.setStockQuantity(newQty);
        log.info("Adjusted stock for product id={}: delta={} newQty={}", id, delta, newQty);

        // Publish low-stock event - listeners handle notifications asynchronously
        if (newQty <= 5) {
            eventPublisher.publishEvent(
                    new com.quantferox.lumeo.event.LowStockEvent(this, productMapper.toResponse(product)));
        }
        return productMapper.toResponse(product);
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private Product getOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private Category resolveCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    }
}
