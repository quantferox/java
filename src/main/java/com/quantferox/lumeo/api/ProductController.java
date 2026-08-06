package com.quantferox.lumeo.api;

import com.quantferox.lumeo.dto.request.ProductRequest;
import com.quantferox.lumeo.dto.response.PageResponse;
import com.quantferox.lumeo.dto.response.ProductResponse;
import com.quantferox.lumeo.service.ProductService;
import com.quantferox.lumeo.validation.OnCreate;
import com.quantferox.lumeo.validation.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Products", description = "Product CRUD + search")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Paginated product list")
    public ResponseEntity<PageResponse<ProductResponse>> list(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ResponseEntity.ok(productService.findAll(pageable));
    }

    @GetMapping("/featured")
    @Operation(summary = "Featured products")
    public ResponseEntity<List<ProductResponse>> featured() {
        return ResponseEntity.ok(productService.findFeatured());
    }

    @GetMapping("/search")
    @Operation(summary = "Full-text search across name, description and SKU")
    public ResponseEntity<PageResponse<ProductResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.search(q, pageable));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Products by category (paginated)")
    public ResponseEntity<PageResponse<ProductResponse>> byCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.findByCategory(categoryId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by slug")
    public ResponseEntity<ProductResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.findBySlug(slug));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Products with stock at or below threshold (ADMIN)")
    public ResponseEntity<List<ProductResponse>> lowStock(
            @RequestParam(defaultValue = "5") int threshold) {
        return ResponseEntity.ok(productService.findLowStock(threshold));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create product (ADMIN)")
    public ResponseEntity<ProductResponse> create(@Validated(OnCreate.class) @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update product (ADMIN)")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id,
                                                  @Validated(OnUpdate.class) @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Adjust stock delta (ADMIN) - positive to add, negative to remove")
    public ResponseEntity<ProductResponse> adjustStock(@PathVariable Long id,
                                                       @RequestParam int delta) {
        return ResponseEntity.ok(productService.adjustStock(id, delta));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Soft-delete product (ADMIN)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
