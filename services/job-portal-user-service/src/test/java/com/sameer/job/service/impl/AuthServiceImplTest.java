package com.sameer.job.service.impl;

import com.sameer.job.domain.UserRole;
import com.sameer.job.domain.UserStatus;
import com.sameer.job.exception.ForbiddenException;
import com.sameer.job.modal.User;
import com.sameer.job.payload.SignupRequest;
import com.sameer.job.repository.UserRepository;
import com.sameer.job.security.CustomUserDetailsService;
import com.sameer.job.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private CustomUserDetailsService customUserDetailsService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtProvider, customUserDetailsService);
    }

    @Test
    void signupPutsRoleOnJwt() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setFullName("Sam Seeker");
        request.setEmail("sam@example.com");
        request.setPassword("secret");
        request.setPhone("9999999999");
        request.setRole(UserRole.ROLE_JOB_SEEKER);

        when(userRepository.existsByEmail("sam@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(11L);
            return user;
        });
        when(jwtProvider.generateToken(any(Authentication.class), eq(11L))).thenReturn("token");

        authService.signup(request);

        ArgumentCaptor<Authentication> authentication = ArgumentCaptor.forClass(Authentication.class);
        verify(jwtProvider).generateToken(authentication.capture(), eq(11L));
        assertThat(authentication.getValue().getAuthorities())
                .extracting(granted -> granted.getAuthority())
                .containsExactly(UserRole.ROLE_JOB_SEEKER.name());
    }

    @Test
    void signupRejectsAdminSelfRegistration() {
        SignupRequest request = new SignupRequest();
        request.setFullName("Admin");
        request.setEmail("admin@example.com");
        request.setPassword("secret");
        request.setRole(UserRole.ROLE_ADMIN);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("admin");
        verify(userRepository, never()).save(any());
        verify(jwtProvider, never()).generateToken(any(), any());
    }

    @Test
    void loginRejectsSuspendedUsers() throws Exception {
        User user = User.builder()
                .id(3L)
                .email("sam@example.com")
                .password("hashed")
                .fullName("Sam")
                .role(UserRole.ROLE_JOB_SEEKER)
                .status(UserStatus.SUSPENDED)
                .build();
        when(customUserDetailsService.loadUserByUsername("sam@example.com"))
                .thenReturn(new org.springframework.security.core.userdetails.User(
                        "sam@example.com", "hashed", java.util.List.of()
                ));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(userRepository.findByEmail("sam@example.com")).thenReturn(user);

        assertThatThrownBy(() -> authService.login(loginRequest("sam@example.com", "secret")))
                .isInstanceOf(ForbiddenException.class);
        verify(jwtProvider, never()).generateToken(any(), any());
    }

    private com.sameer.job.payload.LoginRequest loginRequest(String email, String password) {
        com.sameer.job.payload.LoginRequest login = new com.sameer.job.payload.LoginRequest();
        login.setEmail(email);
        login.setPassword(password);
        return login;
    }
}
