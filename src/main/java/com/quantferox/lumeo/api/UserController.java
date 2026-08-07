package com.quantferox.lumeo.api;

import com.quantferox.lumeo.dto.response.PageResponse;
import com.quantferox.lumeo.dto.response.UserResponse;
import com.quantferox.lumeo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "User management")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

  private final UserService userService;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "List all users - paginated (ADMIN)")
  public ResponseEntity<PageResponse<UserResponse>> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String search) {
    var pageable = PageRequest.of(page, size, Sort.by("username"));
    PageResponse<UserResponse> result = (search != null && !search.isBlank())
        ? userService.search(search, pageable)
        : userService.findAll(pageable);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/me")
  @Operation(summary = "Current user profile")
  public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetails principal) {
    return ResponseEntity.ok(userService.findByUsername(principal.getUsername()));
  }

  @Scope("singleton")
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get user by ID (ADMIN)")
  public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
    return ResponseEntity.ok(userService.findById(id));
  }

  @PatchMapping("/{id}/promote")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Promote user to ADMIN role (ADMIN)")
  public ResponseEntity<UserResponse> promote(@PathVariable Long id) {
    return ResponseEntity.ok(userService.promoteToAdmin(id));
  }

  @PatchMapping("/{id}/disable")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Disable a user account (ADMIN)")
  public ResponseEntity<Void> disable(@PathVariable Long id) {
    userService.disable(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/enable")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Re-enable a user account (ADMIN)")
  public ResponseEntity<Void> enable(@PathVariable Long id) {
    userService.enable(id);
    return ResponseEntity.noContent().build();
  }
}
