package com.quantferox.lumeo.dto.request;

import com.quantferox.lumeo.validation.OnCreate;
import com.quantferox.lumeo.validation.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Name is required", groups = OnCreate.class)
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @NotBlank(message = "Slug is required", groups = OnCreate.class)
    @Size(max = 120, message = "Slug must be at most 120 characters")
    private String slug;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private String imageUrl;

    private boolean active = true;

    private Long parentId;
}
