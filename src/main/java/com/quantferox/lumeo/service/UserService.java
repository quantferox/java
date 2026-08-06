package com.quantferox.lumeo.service;

import com.quantferox.lumeo.domain.entity.User;
import com.quantferox.lumeo.domain.enums.Role;
import com.quantferox.lumeo.dto.request.RegisterRequest;
import com.quantferox.lumeo.dto.response.PageResponse;
import com.quantferox.lumeo.dto.response.UserResponse;
import com.quantferox.lumeo.exception.DuplicateResourceException;
import com.quantferox.lumeo.exception.ResourceNotFoundException;
import com.quantferox.lumeo.mapper.UserMapper;
import com.quantferox.lumeo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
/*
 * Class-level: readOnly = true, propagation = REQUIRED, isolation = READ_COMMITTED.
 * READ_COMMITTED is correct for user reads - we don't need snapshot isolation here.
 * Write methods use REQUIRED (join existing tx or start new one).
 */
@Transactional(readOnly = true, propagation = Propagation.REQUIRED,
               isolation = Isolation.READ_COMMITTED)
public class UserService {

    private final UserRepository  userRepository;
    private final UserMapper      userMapper;
    private final PasswordEncoder passwordEncoder;

    // ── Queries ───────────────────────────────────────────────────────────

    public PageResponse<UserResponse> findAll(Pageable pageable) {
        return PageResponse.of(
                userRepository.findAllByEnabledTrue(pageable).map(userMapper::toResponse));
    }

    public PageResponse<UserResponse> search(String query, Pageable pageable) {
        return PageResponse.of(
                userRepository.search(query, pageable).map(userMapper::toResponse));
    }

    public UserResponse findById(Long id) {
        return userMapper.toResponse(getOrThrow(id));
    }

    public UserResponse findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return userMapper.toResponse(user);
    }

    // ── Commands ──────────────────────────────────────────────────────────

    // register() - REQUIRES_NEW so it commits independently even if the
    // caller's outer transaction rolls back. User registration must persist.
    @Transactional(propagation = Propagation.REQUIRES_NEW,
                   isolation   = Isolation.READ_COMMITTED)
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);
        User saved = userRepository.save(user);
        log.info("Registered user id={} username={}", saved.getId(), saved.getUsername());
        return userMapper.toResponse(saved);
    }

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation   = Isolation.READ_COMMITTED)
    public UserResponse promoteToAdmin(Long id) {
        User user = getOrThrow(id);
        user.setRole(Role.ROLE_ADMIN);
        log.info("Promoted user id={} to ADMIN", id);
        return userMapper.toResponse(user);
    }

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation   = Isolation.READ_COMMITTED)
    public void disable(Long id) {
        User user = getOrThrow(id);
        user.setEnabled(false);
        log.info("Disabled user id={}", id);
    }

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation   = Isolation.READ_COMMITTED)
    public void enable(Long id) {
        User user = getOrThrow(id);
        user.setEnabled(true);
        log.info("Enabled user id={}", id);
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    public User getEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private User getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
