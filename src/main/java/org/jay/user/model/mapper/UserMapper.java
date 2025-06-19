package org.jay.user.model.mapper;

import org.jay.user.model.dto.UserProfileResponse;
import org.jay.user.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi") // 讓 Quarkus 可以注入
public interface UserMapper {
    UserProfileResponse toUserProfileResponse(User user);
}