package com.avnish.jobpulse.service;

import com.avnish.jobpulse.dto.AuthDtos.LoginRequest;
import com.avnish.jobpulse.dto.AuthDtos.RegisterRequest;
import com.avnish.jobpulse.model.User;
import com.avnish.jobpulse.repository.UserRepository;
import com.avnish.jobpulse.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the auth flow - the part of "JWT auth" that's actually worth testing:
 * duplicate registration is rejected, and login only succeeds when the password actually matches.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_createsUserAndReturnsToken_whenEmailIsNew() {
        RegisterRequest request = new RegisterRequest("new@user.com", "password123");
        when(userRepository.existsByEmail("new@user.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-pw");
        when(jwtService.generateToken("new@user.com")).thenReturn("fake-jwt-token");

        String token = authService.register(request);

        assertThat(token).isEqualTo("fake-jwt-token");
        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("new@user.com") && u.getPasswordHash().equals("hashed-pw")));
    }

    @Test
    void register_throws_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("taken@user.com", "password123");
        when(userRepository.existsByEmail("taken@user.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsToken_whenPasswordMatches() {
        User existing = new User("user@user.com", "hashed-pw");
        LoginRequest request = new LoginRequest("user@user.com", "correct-password");

        when(userRepository.findByEmail("user@user.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("correct-password", "hashed-pw")).thenReturn(true);
        when(jwtService.generateToken("user@user.com")).thenReturn("fake-jwt-token");

        String token = authService.login(request);

        assertThat(token).isEqualTo("fake-jwt-token");
    }

    @Test
    void login_throws_whenPasswordDoesNotMatch() {
        User existing = new User("user@user.com", "hashed-pw");
        LoginRequest request = new LoginRequest("user@user.com", "wrong-password");

        when(userRepository.findByEmail("user@user.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrong-password", "hashed-pw")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_throws_whenUserDoesNotExist() {
        when(userRepository.findByEmail("ghost@user.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("ghost@user.com", "anything")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
