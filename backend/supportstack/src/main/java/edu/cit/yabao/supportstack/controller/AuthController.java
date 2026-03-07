package edu.cit.yabao.supportstack.controller;

import edu.cit.yabao.supportstack.dto.ApiResponse;
import edu.cit.yabao.supportstack.dto.AuthResponse;
import edu.cit.yabao.supportstack.dto.AuthUser;
import edu.cit.yabao.supportstack.dto.LoginRequest;
import edu.cit.yabao.supportstack.dto.RegisterRequest;
import edu.cit.yabao.supportstack.dto.RegisterResponse;
import edu.cit.yabao.supportstack.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthUser>> me(Authentication authentication) {
        AuthUser user = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/db-check")
    public ApiResponse<Map<String, Object>> dbCheck() {
        return ApiResponse.success(Map.of(
                "databaseConnected", true,
                "registeredUsers", authService.userCount()
        ));
    }
}
