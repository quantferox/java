package com.quantferox.lumeo.service;

import com.quantferox.lumeo.config.CacheConfig;
import com.quantferox.lumeo.domain.entity.Category;
import com.quantferox.lumeo.dto.request.CategoryRequest;
import com.quantferox.lumeo.dto.response.CategoryResponse;
import com.quantferox.lumeo.exception.DuplicateResourceException;
import com.quantferox.lumeo.exception.ResourceNotFoundException;
import com.quantferox.lumeo.mapper.CategoryMapper;
import com.quantferox.lumeo.repository.CategoryRepository;
import com.quantferox.lumeo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository  productRepository;
    private final CategoryMapper     categoryMapper;

    // ── Queries ───────────────────────────────────────────────────────────

    @Cacheable(value = CacheConfig.CACHE_CATEGORIES, key = "'all'")
    public List<CategoryResponse> findAll() {
        log.debug("Cache MISS - loading all categories from DB");
        return categoryMapper.toResponseList(categoryRepository.findAllByActiveTrue());
    }

    @Cacheable(value = CacheConfig.CACHE_CATEGORIES, key = "'tree'")
    public List<CategoryResponse> findTree() {
        log.debug("Cache MISS - loading category tree from DB");
        return categoryMapper.toResponseList(
                categoryRepository.findRootCategoriesWithChildren());
    }

    @Cacheable(value = CacheConfig.CACHE_CATEGORIES, key = "#id")
    public CategoryResponse findById(Long id) {
        return categoryMapper.toResponse(getOrThrow(id));
    }

    @Cacheable(value = CacheConfig.CACHE_CATEGORIES, key = "'slug:' + #slug")
    public CategoryResponse findBySlug(String slug) {
        Category cat = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return categoryMapper.toResponse(cat);
    }

    // Search is not cached - results vary too much
    public List<CategoryResponse> search(String name) {
        return categoryMapper.toResponseList(categoryRepository.searchByName(name));
    }

    // ── Commands ──────────────────────────────────────────────────────────

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, key = "'all'"),
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, key = "'tree'")
    })
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("Category", "slug", request.getSlug());
        }
        Category category = categoryMapper.toEntity(request);
        resolveParent(category, request.getParentId());
        Category saved = categoryRepository.save(category);
        log.info("Created category id={} slug={}", saved.getId(), saved.getSlug());
        return categoryMapper.toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, key = "'all'"),
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, key = "'tree'"),
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, key = "'slug:' + #result.slug",
                        condition = "#result != null")
    })
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getOrThrow(id);
        boolean slugChanged = !category.getSlug().equals(request.getSlug());
        if (slugChanged && categoryRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("Category", "slug", request.getSlug());
        }
        categoryMapper.updateEntity(request, category);
        resolveParent(category, request.getParentId());
        log.info("Updated category id={}", id);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_PRODUCTS,   allEntries = true)
    })
    public void delete(Long id) {
        Category category = getOrThrow(id);
        category.setActive(false);
        productRepository.deactivateByCategoryId(id);
        log.info("Soft-deleted category id={}", id);
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private Category getOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    private void resolveParent(Category category, Long parentId) {
        if (parentId != null) {
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category (parent)", parentId));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
    }
}
