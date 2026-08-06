package com.quantferox.lumeo.mapper;

import com.quantferox.lumeo.domain.entity.Category;
import com.quantferox.lumeo.dto.request.CategoryRequest;
import com.quantferox.lumeo.dto.response.CategoryResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "parentId",   source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "children",   source = "children")
    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);

    /**
     * Creates a new Category from a request.
     * parent and collections are set manually in the service layer.
     */
    @Mapping(target = "parent",   ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "products", ignore = true)
    Category toEntity(CategoryRequest request);

    /**
     * Applies non-null request fields onto an existing Category.
     * parent and collections are handled in the service layer.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "parent",   ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "products", ignore = true)
    void updateEntity(CategoryRequest request, @MappingTarget Category category);
}
