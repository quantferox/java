package com.quantferox.lumeo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private boolean active;
    private Long parentId;
    private String parentName;
    private List<CategoryResponse> children;
    private Instant createdAt;
    private Instant updatedAt;
}
