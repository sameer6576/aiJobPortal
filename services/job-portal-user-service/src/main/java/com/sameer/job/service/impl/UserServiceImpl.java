package com.sameer.job.service.impl;

import com.sameer.job.domain.UserStatus;
import com.sameer.job.dto.response.UserResponse;
import com.sameer.job.mapper.UserMapper;
import com.sameer.job.modal.User;
import com.sameer.job.payload.UpdateUserRequest;
import com.sameer.job.repository.UserRepository;
import com.sameer.job.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getUserByEmail(String email) throws Exception{
        User user = userRepository.findByEmail(email);
        if(user == null){
            throw new Exception("User not found with email: "+email);
        }
        return user;
    }

    @Override
    public User getUserById(Long id) throws Exception{
        return userRepository.findById(id).orElseThrow(() -> new Exception("User not found with id: "+id));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserResponse updateProfile(String email, UpdateUserRequest req) throws Exception{
        User user = getUserByEmail(email);
        if(req.getFullName()!=null){
            user.setFullName(req.getFullName());
        }
        if(req.getPhone()!=null){
            user.setPhone(req.getFullName());
        }
        if(req.getProfileImage()!=null){
            user.setProfileImage(req.getProfileImage());
        }
        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    public UserResponse suspendUser(Long id) throws Exception{
        User user = getUserById(id);
        user.setStatus(UserStatus.SUSPENDED);
        user.setSuspendedAt(LocalDateTime.now());
        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    public UserResponse activateUser(Long id) throws Exception{
        User user = getUserById(id);
        user.setStatus(UserStatus.ACTIVE);
        user.setSuspendedAt(null);
        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    public UserResponse deleteUser(Long id) throws Exception{
        User user = getUserById(id);
        user.setStatus(UserStatus.DELETED);
        user.setDeletedAt(LocalDateTime.now());
        return UserMapper.toDTO(userRepository.save(user));
    }
}
