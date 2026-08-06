package com.quantferox.lumeo.mapper;

import com.quantferox.lumeo.domain.entity.Order;
import com.quantferox.lumeo.domain.entity.OrderItem;
import com.quantferox.lumeo.dto.response.OrderItemResponse;
import com.quantferox.lumeo.dto.response.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "userId",       source = "user.id")
    @Mapping(target = "userFullName", expression = "java(order.getUser().getFullName())")
    @Mapping(target = "items",        source = "items")
    OrderResponse toResponse(Order order);

    List<OrderResponse> toResponseList(List<Order> orders);

    @Mapping(target = "productId",   source = "product.id")
    @Mapping(target = "productSlug", source = "product.slug")
    @Mapping(target = "subtotal",    expression = "java(item.getSubtotal())")
    OrderItemResponse toItemResponse(OrderItem item);
}
