package com.quantferox.lumeo.mapper;

import com.quantferox.lumeo.domain.entity.Product;
import com.quantferox.lumeo.dto.request.ProductRequest;
import com.quantferox.lumeo.dto.response.ProductResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryId",   source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "inStock",      expression = "java(product.isInStock())")
    @Mapping(target = "onSale",       expression = "java(product.isOnSale())")
    ProductResponse toResponse(Product product);

    List<ProductResponse> toResponseList(List<Product> products);

    /**
     * Creates a new Product from a request.
     * category and orderItems are set manually in the service layer.
     */
    @Mapping(target = "category",   ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    Product toEntity(ProductRequest request);

    /**
     * Applies non-null request fields onto an existing Product.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category",   ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    void updateEntity(ProductRequest request, @MappingTarget Product product);
}
