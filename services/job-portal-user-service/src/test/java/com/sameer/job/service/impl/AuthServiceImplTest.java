package com.sameer.job.service.impl;

import com.sameer.job.config.PasswordResetProperties;
import com.sameer.job.domain.UserRole;
import com.sameer.job.domain.UserStatus;
import com.sameer.job.exception.BadRequestException;
import com.sameer.job.exception.ErrorCodes;
import com.sameer.job.exception.ForbiddenException;
import com.sameer.job.modal.User;
import com.sameer.job.payload.ForgotPasswordRequest;
import com.sameer.job.payload.ForgotPasswordResponse;
import com.sameer.job.payload.ResetPasswordRequest;
import com.sameer.job.payload.SignupRequest;
import com.sameer.job.repository.UserRepository;
import com.sameer.job.security.CustomUserDetailsService;
import com.sameer.job.security.JwtProvider;
import com.sameer.job.security.PasswordResetTokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String GENERIC_FORGOT =
            "If an account exists for that email, a password reset token has been issued.";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private CustomUserDetailsService customUserDetailsService;

    private PasswordResetProperties passwordResetProperties;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        passwordResetProperties = new PasswordResetProperties();
        passwordResetProperties.setExposeToken(true);
        passwordResetProperties.setExpiryHours(1);
        authService = new AuthServiceImpl(
                userRepository, passwordEncoder, jwtProvider, customUserDetailsService, passwordResetProperties);
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

    @Test
    void forgotPasswordUnknownEmailReturnsGenericMessageWithoutToken() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(null);

        ForgotPasswordResponse response = authService.forgotPassword(forgot("missing@example.com"));

        assertThat(response.getMessage()).isEqualTo(GENERIC_FORGOT);
        assertThat(response.getResetToken()).isNull();
        verify(userRepository, never()).save(any());
    }

    @Test
    void forgotPasswordSuspendedAccountReturnsGenericMessageWithoutIssuingToken() {
        User user = activeUser();
        user.setStatus(UserStatus.SUSPENDED);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

        ForgotPasswordResponse response = authService.forgotPassword(forgot(user.getEmail()));

        assertThat(response.getMessage()).isEqualTo(GENERIC_FORGOT);
        assertThat(response.getResetToken()).isNull();
        verify(userRepository, never()).save(any());
    }

    @Test
    void forgotPasswordStoresSha256HashNotRawTokenAndExposesTokenWhenConfigured() {
        User user = activeUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ForgotPasswordResponse response = authService.forgotPassword(forgot(user.getEmail()));

        assertThat(response.getMessage()).isEqualTo(GENERIC_FORGOT);
        assertThat(response.getResetToken()).isNotBlank();
        assertThat(user.getPasswordResetTokenHash()).isEqualTo(PasswordResetTokens.sha256Hex(response.getResetToken()));
        assertThat(user.getPasswordResetTokenHash()).isNotEqualTo(response.getResetToken());
        assertThat(user.getPasswordResetExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(50));
        assertThat(user.getPasswordResetExpiresAt()).isBefore(LocalDateTime.now().plusHours(1).plusMinutes(2));
    }

    @Test
    void forgotPasswordDoesNotExposeTokenWhenFlagIsFalse() {
        passwordResetProperties.setExposeToken(false);
        User user = activeUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ForgotPasswordResponse response = authService.forgotPassword(forgot(user.getEmail()));

        assertThat(response.getResetToken()).isNull();
        assertThat(user.getPasswordResetTokenHash()).isNotBlank();
    }

    @Test
    void resetPasswordEncodesNewPasswordAndClearsToken() {
        String rawToken = PasswordResetTokens.generateRawToken();
        User user = activeUser();
        user.setPasswordResetTokenHash(PasswordResetTokens.sha256Hex(rawToken));
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(1));
        when(userRepository.findByPasswordResetTokenHash(user.getPasswordResetTokenHash())).thenReturn(user);
        when(passwordEncoder.encode("NewPass12")).thenReturn("new-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.resetPassword(reset(rawToken, "NewPass12"));

        assertThat(user.getPassword()).isEqualTo("new-hash");
        assertThat(user.getPasswordResetTokenHash()).isNull();
        assertThat(user.getPasswordResetExpiresAt()).isNull();
    }

    @Test
    void resetPasswordRejectsUnknownToken() {
        when(userRepository.findByPasswordResetTokenHash(any())).thenReturn(null);

        assertThatThrownBy(() -> authService.resetPassword(reset("not-a-real-token", "NewPass12")))
                .isInstanceOf(BadRequestException.class)
                .extracting(ex -> ((BadRequestException) ex).getCode())
                .isEqualTo(ErrorCodes.INVALID_RESET_TOKEN);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void resetPasswordRejectsExpiredTokenAndClearsIt() {
        String rawToken = PasswordResetTokens.generateRawToken();
        User user = activeUser();
        user.setPasswordResetTokenHash(PasswordResetTokens.sha256Hex(rawToken));
        user.setPasswordResetExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByPasswordResetTokenHash(user.getPasswordResetTokenHash())).thenReturn(user);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> authService.resetPassword(reset(rawToken, "NewPass12")))
                .isInstanceOf(BadRequestException.class)
                .extracting(ex -> ((BadRequestException) ex).getCode())
                .isEqualTo(ErrorCodes.RESET_TOKEN_EXPIRED);
        assertThat(user.getPasswordResetTokenHash()).isNull();
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void resetPasswordRejectsDeletedAccount() {
        String rawToken = PasswordResetTokens.generateRawToken();
        User user = activeUser();
        user.setStatus(UserStatus.DELETED);
        user.setPasswordResetTokenHash(PasswordResetTokens.sha256Hex(rawToken));
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(1));
        when(userRepository.findByPasswordResetTokenHash(user.getPasswordResetTokenHash())).thenReturn(user);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> authService.resetPassword(reset(rawToken, "NewPass12")))
                .isInstanceOf(ForbiddenException.class)
                .extracting(ex -> ((ForbiddenException) ex).getCode())
                .isEqualTo(ErrorCodes.ACCOUNT_DISABLED);
        verify(passwordEncoder, never()).encode(any());
        assertThat(user.getPasswordResetTokenHash()).isNull();
    }

    @Test
    void resetPasswordIsSingleUse() {
        String rawToken = PasswordResetTokens.generateRawToken();
        User user = activeUser();
        String hash = PasswordResetTokens.sha256Hex(rawToken);
        user.setPasswordResetTokenHash(hash);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(1));
        when(userRepository.findByPasswordResetTokenHash(hash)).thenReturn(user);
        when(passwordEncoder.encode("NewPass12")).thenReturn("new-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.resetPassword(reset(rawToken, "NewPass12"));
        when(userRepository.findByPasswordResetTokenHash(hash)).thenReturn(null);

        assertThatThrownBy(() -> authService.resetPassword(reset(rawToken, "Another1")))
                .isInstanceOf(BadRequestException.class)
                .extracting(ex -> ((BadRequestException) ex).getCode())
                .isEqualTo(ErrorCodes.INVALID_RESET_TOKEN);
    }

    private User activeUser() {
        return User.builder()
                .id(1L)
                .fullName("Sam Seeker")
                .email("sam@example.com")
                .password("old-hash")
                .role(UserRole.ROLE_JOB_SEEKER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private ForgotPasswordRequest forgot(String email) {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(email);
        return request;
    }

    private ResetPasswordRequest reset(String token, String newPassword) {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);
        request.setNewPassword(newPassword);
        return request;
    }

    private com.sameer.job.payload.LoginRequest loginRequest(String email, String password) {
        com.sameer.job.payload.LoginRequest login = new com.sameer.job.payload.LoginRequest();
        login.setEmail(email);
        login.setPassword(password);
        return login;
    }
}
