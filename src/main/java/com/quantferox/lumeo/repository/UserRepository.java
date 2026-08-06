package com.quantferox.lumeo.repository;

import com.quantferox.lumeo.domain.entity.User;
import com.quantferox.lumeo.domain.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<User> findAllByEnabledTrue(Pageable pageable);

    Page<User> findAllByRole(Role role, Pageable pageable);

    /** Search by username or email fragment. */
    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<User> search(@Param("q") String query, Pageable pageable);

    /** Count orders per user - for admin stats. */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    long countOrdersByUserId(@Param("userId") Long userId);
}
