package com.quantferox.lumeo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine-backed in-process cache.
 *
 * <p>Named caches and their TTL / size policies:
 * <ul>
 *   <li>{@code categories}    - rarely changes, 1 h TTL, max 200 entries</li>
 *   <li>{@code products}      - paginated list, 10 min TTL, max 500 entries</li>
 *   <li>{@code product-slug}  - individual product by slug, 30 min TTL</li>
 *   <li>{@code featured}      - featured product list, 15 min TTL</li>
 * </ul>
 *
 * <p>In a multi-node deployment swap Caffeine for Redis by adding
 * {@code spring-boot-starter-data-redis} - Spring's {@link CacheManager}
 * abstraction means zero service-layer changes.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_CATEGORIES   = "categories";
    public static final String CACHE_PRODUCTS     = "products";
    public static final String CACHE_PRODUCT_SLUG = "product-slug";
    public static final String CACHE_FEATURED     = "featured";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        // Default spec - overridden per-cache below via registerCustomCache
        manager.setCaffeine(defaultSpec());

        // Per-cache policies
        manager.registerCustomCache(CACHE_CATEGORIES,
                Caffeine.newBuilder()
                        .expireAfterWrite(60, TimeUnit.MINUTES)
                        .maximumSize(200)
                        .recordStats()
                        .build());

        manager.registerCustomCache(CACHE_PRODUCTS,
                Caffeine.newBuilder()
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .maximumSize(500)
                        .recordStats()
                        .build());

        manager.registerCustomCache(CACHE_PRODUCT_SLUG,
                Caffeine.newBuilder()
                        .expireAfterWrite(30, TimeUnit.MINUTES)
                        .maximumSize(500)
                        .recordStats()
                        .build());

        manager.registerCustomCache(CACHE_FEATURED,
                Caffeine.newBuilder()
                        .expireAfterWrite(15, TimeUnit.MINUTES)
                        .maximumSize(50)
                        .recordStats()
                        .build());

        return manager;
    }

    private Caffeine<Object, Object> defaultSpec() {
        return Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(100)
                .recordStats();
    }
}
