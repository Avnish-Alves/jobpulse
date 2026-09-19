package com.avnish.jobpulse.controller;

import com.avnish.jobpulse.dto.AuthDtos.AuthResponse;
import com.avnish.jobpulse.dto.AuthDtos.LoginRequest;
import com.avnish.jobpulse.dto.AuthDtos.RegisterRequest;
import com.avnish.jobpulse.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        String token = authService.register(request);
        return ResponseEntity.ok(new AuthResponse(token, request.email()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(new AuthResponse(token, request.email()));
    }
}
