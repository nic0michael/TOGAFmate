package com.togafmate.backend.config;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.togafmate.backend.entity.User;
import com.togafmate.backend.entity.UserPermission;
import com.togafmate.backend.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootstrapAdministratorTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void should_createEncodedAdministrator_whenBootstrapCredentialsAreConfigured() throws Exception {
        BootstrapAdministrator bootstrapAdministrator = new BootstrapAdministrator(userRepository, passwordEncoder,
                "admin", "secret");
        when(userRepository.existsById("admin")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bootstrapAdministrator.run(new DefaultApplicationArguments(new String[0]));

        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
                user.getPassword().equals("encoded-secret")
                        && user.getPermissions().equals(List.of(UserPermission.ADMINISTRATOR_ACCESS))));
    }
}
