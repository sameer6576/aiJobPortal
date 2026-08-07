package com.sameer.job.mapper;

import com.sameer.job.dto.response.UserResponse;
import com.sameer.job.modal.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserResponse toDTO(User user){
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setProfileImage(user.getProfileImage());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setLastLogin(user.getLastLogin());
        dto.setCreatedAt(user.getCreatedAt());

        return dto;
    }


    public static List<UserResponse> toDTOList(List<User> user) {
        return user.stream().map(UserMapper::toDTO).collect(Collectors.toList());
    }
}
