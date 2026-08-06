package com.quantferox.lumeo.repository;

import com.quantferox.lumeo.domain.entity.Order;
import com.quantferox.lumeo.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findAllByUserId(Long userId, Pageable pageable);

    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

    /** All orders between two timestamps - used in revenue reports. */
    @Query("""
            SELECT o FROM Order o
            WHERE o.createdAt BETWEEN :from AND :to
            ORDER BY o.createdAt DESC
            """)
    List<Order> findBetween(@Param("from") Instant from, @Param("to") Instant to);

    /** Revenue total for a given status (e.g. DELIVERED). */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal sumTotalByStatus(@Param("status") OrderStatus status);

    /** Latest N orders for a user - used in account dashboard. */
    @Query("""
            SELECT o FROM Order o
            WHERE o.user.id = :userId
            ORDER BY o.createdAt DESC
            """)
    List<Order> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    /** Count orders per status - admin KPI card. */
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countByStatus();
}
