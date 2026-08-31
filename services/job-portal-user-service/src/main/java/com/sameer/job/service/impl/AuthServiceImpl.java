package com.sameer.job.service.impl;

import com.sameer.job.domain.UserRole;
import com.sameer.job.domain.UserStatus;
import com.sameer.job.exception.ConflictException;
import com.sameer.job.exception.ErrorCodes;
import com.sameer.job.exception.ForbiddenException;
import com.sameer.job.exception.UnauthorizedException;
import com.sameer.job.mapper.UserMapper;
import com.sameer.job.modal.User;
import com.sameer.job.payload.AuthResponse;
import com.sameer.job.payload.LoginRequest;
import com.sameer.job.payload.SignupRequest;
import com.sameer.job.repository.UserRepository;
import com.sameer.job.security.CustomUserDetailsService;
import com.sameer.job.security.JwtProvider;
import com.sameer.job.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;


    @Override
    @Transactional
    public AuthResponse signup(SignupRequest req) throws Exception {
        if(userRepository.existsByEmail(req.getEmail())){
            throw new ConflictException(ErrorCodes.EMAIL_REGISTERED, "Email already registered");
        }

        if(req.getRole() == UserRole.ROLE_ADMIN){
            throw new ForbiddenException(ErrorCodes.ADMIN_SELF_SIGNUP, "Cannot self register as a role admin");
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

        UserDetails principal = new org.springframework.security.core.userdetails.User(
                savedUser.getEmail(),
                savedUser.getPassword(),
                List.of(new SimpleGrantedAuthority(savedUser.getRole().name()))
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
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
    @Transactional
    public AuthResponse login(LoginRequest req) throws Exception {
        Authentication authentication = authenticate(
                req.getEmail(), req.getPassword()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(req.getEmail());
        if (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.DELETED) {
            throw new ForbiddenException(ErrorCodes.ACCOUNT_DISABLED, "This account cannot log in");
        }

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

        UserDetails userDetails;
        try {
            userDetails = customUserDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            throw new UnauthorizedException(ErrorCodes.INVALID_CREDENTIALS, "Invalid credentials");
        }

        if(!passwordEncoder.matches(password, userDetails.getPassword())){
            throw new UnauthorizedException(ErrorCodes.INVALID_CREDENTIALS, "Invalid credentials");
        }

        return new UsernamePasswordAuthenticationToken
                (userDetails,null,userDetails.getAuthorities());
    }
}
