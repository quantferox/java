package com.quantferox.lumeo.mapper;

import com.quantferox.lumeo.domain.entity.User;
import com.quantferox.lumeo.dto.request.RegisterRequest;
import com.quantferox.lumeo.dto.response.UserResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    /**
     * Creates a new User from a registration request.
     * password (encoded), role, enabled, and orders are set in the service layer.
     */
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role",     ignore = true)
    @Mapping(target = "enabled",  ignore = true)
    @Mapping(target = "orders",   ignore = true)
    User toEntity(RegisterRequest request);
}
