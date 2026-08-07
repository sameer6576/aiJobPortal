package com.sameer.job.service.impl;

import com.sameer.job.domain.UserRole;
import com.sameer.job.domain.UserStatus;
import com.sameer.job.mapper.UserMapper;
import com.sameer.job.modal.User;
import com.sameer.job.payload.AuthResponse;
import com.sameer.job.payload.LoginRequest;
import com.sameer.job.payload.SignupRequest;
import com.sameer.job.repository.UserRepository;
import com.sameer.job.security.CustomUserDetailsService;
import com.sameer.job.security.JwtProvider;
import com.sameer.job.service.AuthService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;


    @Override
    public AuthResponse signup(SignupRequest req) throws Exception {
        if(userRepository.existsByEmail(req.getEmail())){
            throw new Exception("Email already registered: "+req.getEmail());
        }

        if(req.getRole() == UserRole.ROLE_ADMIN){
            throw new Exception("Cannot self register as a role admin");
        }

        User user = User.builder()
                .fullName(req.getFullName())
                .password(passwordEncoder.encode(req.getPassword()))
                .email(req.getEmail())
                .role(req.getRole())
                .phone(req.getPhone())
                .lastLogin(LocalDateTime.now())
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(), user.getPassword()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateToken(authentication, savedUser.getId());

        AuthResponse response = AuthResponse.builder()
                .title("Welcome "+savedUser.getFullName())
                .message("Registered Successfully")
                .jwt(jwt)
                .user(UserMapper.toDTO(savedUser))
                .build();

        return response;
    }

    @Override
    public AuthResponse login(LoginRequest req) throws Exception {
        Authentication authentication = authenticate(
                req.getEmail(), req.getPassword()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(req.getEmail());

        String jwt = jwtProvider.generateToken(authentication, user.getId());

        user.setLastLogin(LocalDateTime.now());

        userRepository.save(user);

        AuthResponse response = AuthResponse.builder()
                .title("Welcome back -- "+user.getFullName())
                .message("Login Successfully")
                .jwt(jwt)
                .user(UserMapper.toDTO(user))
                .build();

        return response;

    }

    private Authentication authenticate(String email, String password) throws Exception{

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        if(!passwordEncoder.matches(password, userDetails.getPassword())){
            throw new Exception("Invalid Password");
        }

        return new UsernamePasswordAuthenticationToken
                (userDetails,null,userDetails.getAuthorities());
    }
}
