package com.sameer.job.service;

import com.sameer.job.dto.response.UserResponse;
import com.sameer.job.payload.ChangePasswordRequest;
import com.sameer.job.payload.PasswordActionResponse;
import com.sameer.job.payload.UpdateUserRequest;

import java.util.List;

public interface UserService {

    UserResponse getUserByEmail(String email) throws Exception;

    UserResponse getUserById(Long id) throws Exception;

    List<UserResponse> getAllUsers();

    UserResponse updateProfile(String email, UpdateUserRequest req) throws Exception;

    PasswordActionResponse changePassword(String email, ChangePasswordRequest req);

    UserResponse suspendUser(Long id) throws Exception;

    UserResponse activateUser(Long id) throws Exception;

    UserResponse deleteUser(Long id) throws Exception;
}
