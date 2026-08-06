package com.quantferox.lumeo.repository;

import com.quantferox.lumeo.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySlug(String slug);

    Page<Product> findAllByActiveTrue(Pageable pageable);

    Page<Product> findAllByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    List<Product> findAllByFeaturedTrueAndActiveTrue();

    /** Full-text-style search across name, description, and SKU. */
    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true
              AND (LOWER(p.name)        LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.sku)         LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Product> search(@Param("q") String query, Pageable pageable);

    /** Products within a price band. */
    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true
              AND p.price BETWEEN :min AND :max
            ORDER BY p.price ASC
            """)
    List<Product> findByPriceRange(@Param("min") BigDecimal min,
                                   @Param("max") BigDecimal max);

    /** Products with low stock to show alerts in admin dashboard. */
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockQuantity <= :threshold ORDER BY p.stockQuantity ASC")
    List<Product> findLowStock(@Param("threshold") int threshold);

    /** Bulk deactivate products by category - used when category is deleted. */
    @Modifying
    @Query("UPDATE Product p SET p.active = false WHERE p.category.id = :categoryId")
    int deactivateByCategoryId(@Param("categoryId") Long categoryId);
}
