package com.quantferox.lumeo.repository;

import com.quantferox.lumeo.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Category> findAllByActiveTrue();

    /** Root categories (no parent). */
    List<Category> findAllByParentIsNullAndActiveTrue();

    /** All active children of a given parent. */
    List<Category> findAllByParentIdAndActiveTrue(Long parentId);

    /** Category tree - parent + eager children in one query. */
    @Query("""
            SELECT c FROM Category c
            LEFT JOIN FETCH c.children ch
            WHERE c.parent IS NULL AND c.active = true
            ORDER BY c.name
            """)
    List<Category> findRootCategoriesWithChildren();

    /** Count of active products per category. */
    @Query("""
            SELECT c.id, COUNT(p.id)
            FROM Category c
            LEFT JOIN c.products p ON p.active = true
            WHERE c.active = true
            GROUP BY c.id
            """)
    List<Object[]> countActiveProductsPerCategory();

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Category> searchByName(@Param("name") String name);
}
