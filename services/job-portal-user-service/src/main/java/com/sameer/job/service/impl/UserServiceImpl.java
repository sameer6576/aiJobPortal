package com.sameer.job.service.impl;

import com.sameer.job.domain.UserStatus;
import com.sameer.job.dto.response.UserResponse;
import com.sameer.job.exception.NotFoundException;
import com.sameer.job.mapper.UserMapper;
import com.sameer.job.modal.User;
import com.sameer.job.payload.UpdateUserRequest;
import com.sameer.job.repository.UserRepository;
import com.sameer.job.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) throws Exception {
        return UserMapper.toDTO(requireByEmail(email));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) throws Exception {
        return UserMapper.toDTO(requireById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return UserMapper.toDTOList(userRepository.findAll());
    }

    @Override
    public UserResponse updateProfile(String email, UpdateUserRequest req) throws Exception {
        User user = requireByEmail(email);
        if (req.getFullName() != null) {
            user.setFullName(req.getFullName());
        }
        if (req.getPhone() != null) {
            user.setPhone(req.getPhone());
        }
        if (req.getProfileImage() != null) {
            user.setProfileImage(req.getProfileImage());
        }
        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    public UserResponse suspendUser(Long id) throws Exception {
        User user = requireById(id);
        user.setStatus(UserStatus.SUSPENDED);
        user.setSuspendedAt(LocalDateTime.now());
        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    public UserResponse activateUser(Long id) throws Exception {
        User user = requireById(id);
        user.setStatus(UserStatus.ACTIVE);
        user.setSuspendedAt(null);
        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    public UserResponse deleteUser(Long id) throws Exception {
        User user = requireById(id);
        user.setStatus(UserStatus.DELETED);
        user.setDeletedAt(LocalDateTime.now());
        return UserMapper.toDTO(userRepository.save(user));
    }

    private User requireByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("User not found with email: " + email);
        }
        return user;
    }

    private User requireById(Long id) {
        return userRepository.findById(id)
                             .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }
}
