package com.quantferox.lumeo.dto.response;

import com.quantferox.lumeo.domain.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private Role role;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
