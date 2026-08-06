package com.quantferox.lumeo.domain.entity;

import com.quantferox.lumeo.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email",    columnNames = "email"),
                @UniqueConstraint(name = "uk_user_username", columnNames = "username")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String username;

    @Column(nullable = false, length = 200)
    private String email;

    /**
     * BCrypt-hashed password - never store plain text.
     */
    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", length = 80)
    private String firstName;

    @Column(name = "last_name", length = 80)
    private String lastName;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.ROLE_USER;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    // ── Derived helpers ────────────────────────────────────────────────────

    public String getFullName() {
        if (firstName == null && lastName == null) return username;
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }
}
