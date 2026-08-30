package com.sameer.job.service.impl;

import com.sameer.job.modal.User;
import com.sameer.job.payload.UpdateUserRequest;
import com.sameer.job.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
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
}
