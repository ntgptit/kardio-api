package com.kardio.mapper;

import org.springframework.stereotype.Component;

import com.kardio.dto.UserResponse;
import com.kardio.entity.Role;
import com.kardio.entity.User;

@Component
public class UserMapper {
	public UserResponse toUserResponse(User user) {
		return UserResponse.builder().id(user.getId()).email(user.getEmail()).firstName(user.getFirstName())
				.lastName(user.getLastName()).roles(user.getRoles().stream().map(Role::getName).toList()).build();
	}
}
