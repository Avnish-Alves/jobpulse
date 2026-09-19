package com.avnish.jobpulse.service;

import com.avnish.jobpulse.dto.AuthDtos.LoginRequest;
import com.avnish.jobpulse.dto.AuthDtos.RegisterRequest;
import com.avnish.jobpulse.model.User;
import com.avnish.jobpulse.repository.UserRepository;
import com.avnish.jobpulse.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("An account with that email already exists");
        }
        User user = new User(request.email(), passwordEncoder.encode(request.password()));
        userRepository.save(user);
        return jwtService.generateToken(user.getEmail());
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return jwtService.generateToken(user.getEmail());
    }
}
