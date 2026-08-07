package com.sameer.job.service;

import com.sameer.job.dto.response.UserResponse;
import com.sameer.job.modal.User;
import com.sameer.job.payload.UpdateUserRequest;

import java.util.List;

public interface UserService {

    User getUserByEmail(String email) throws Exception;

    User getUserById(Long id) throws Exception;

    List<User> getAllUsers();

    UserResponse updateProfile(String email, UpdateUserRequest req) throws Exception;

    // admin actions

    UserResponse suspendUser(Long id) throws Exception;

    UserResponse activateUser(Long id) throws Exception;

    UserResponse deleteUser(Long id) throws Exception;
}
