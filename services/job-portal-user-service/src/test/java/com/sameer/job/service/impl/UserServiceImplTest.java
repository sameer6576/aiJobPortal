package com.sameer.job.service.impl;

import com.sameer.job.domain.UserRole;
import com.sameer.job.domain.UserStatus;
import com.sameer.job.exception.ErrorCodes;
import com.sameer.job.exception.ForbiddenException;
import com.sameer.job.exception.UnauthorizedException;
import com.sameer.job.modal.User;
import com.sameer.job.payload.ChangePasswordRequest;
import com.sameer.job.payload.UpdateUserRequest;
import com.sameer.job.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void updateProfileStoresPhoneNotFullName() throws Exception {
        User user = User.builder()
                        .id(1L)
                        .fullName("Sam Seeker")
                        .email("sam@example.com")
                        .password("hashed")
                        .phone("111")
                        .build();
        when(userRepository.findByEmail("sam@example.com")).thenReturn(user);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest();
        request.setPhone("8888888888");

        assertThat(userService.updateProfile("sam@example.com", request).getPhone()).isEqualTo("8888888888");
        assertThat(user.getPhone()).isEqualTo("8888888888");
        assertThat(user.getFullName()).isEqualTo("Sam Seeker");
    }

    @Test
    void changePasswordVerifiesCurrentAndEncodesNew() {
        User user = User.builder()
                .id(1L)
                .fullName("Sam Seeker")
                .email("sam@example.com")
                .password("old-hash")
                .role(UserRole.ROLE_JOB_SEEKER)
                .status(UserStatus.ACTIVE)
                .passwordResetTokenHash("pending-hash")
                .passwordResetExpiresAt(LocalDateTime.now().plusHours(1))
                .build();
        when(userRepository.findByEmail("sam@example.com")).thenReturn(user);
        when(passwordEncoder.matches("Current1", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("NewPass12")).thenReturn("new-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.changePassword("sam@example.com", change("Current1", "NewPass12"));

        assertThat(user.getPassword()).isEqualTo("new-hash");
        assertThat(user.getPasswordResetTokenHash()).isNull();
        assertThat(user.getPasswordResetExpiresAt()).isNull();
        verify(passwordEncoder).encode("NewPass12");
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() {
        User user = User.builder()
                .id(1L)
                .fullName("Sam Seeker")
                .email("sam@example.com")
                .password("old-hash")
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByEmail("sam@example.com")).thenReturn(user);
        when(passwordEncoder.matches("WrongPass", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("sam@example.com", change("WrongPass", "NewPass12")))
                .isInstanceOf(UnauthorizedException.class)
                .extracting(ex -> ((UnauthorizedException) ex).getCode())
                .isEqualTo(ErrorCodes.INVALID_CREDENTIALS);
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePasswordRejectsSuspendedAccount() {
        User user = User.builder()
                .id(1L)
                .fullName("Sam Seeker")
                .email("sam@example.com")
                .password("old-hash")
                .status(UserStatus.SUSPENDED)
                .build();
        when(userRepository.findByEmail("sam@example.com")).thenReturn(user);

        assertThatThrownBy(() -> userService.changePassword("sam@example.com", change("Current1", "NewPass12")))
                .isInstanceOf(ForbiddenException.class)
                .extracting(ex -> ((ForbiddenException) ex).getCode())
                .isEqualTo(ErrorCodes.ACCOUNT_DISABLED);
        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository, never()).save(any());
    }

    private ChangePasswordRequest change(String currentPassword, String newPassword) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(currentPassword);
        request.setNewPassword(newPassword);
        return request;
    }
}
