package edu.cit.yabao.supportstack.service;

import edu.cit.yabao.supportstack.dto.AuthResponse;
import edu.cit.yabao.supportstack.dto.AuthUser;
import edu.cit.yabao.supportstack.dto.LoginRequest;
import edu.cit.yabao.supportstack.dto.RegisterRequest;
import edu.cit.yabao.supportstack.dto.RegisterResponse;
import edu.cit.yabao.supportstack.exception.InvalidCredentialsException;
import edu.cit.yabao.supportstack.model.User;
import edu.cit.yabao.supportstack.repository.UserRepository;
import edu.cit.yabao.supportstack.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        String normalizedUsername = normalizeUsername(request);

        normalizedUsername = ensureUniqueUsername(normalizedUsername);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        User savedUser = userRepository.save(user);
        return new RegisterResponse(savedUser.getId(), resolvePrincipal(savedUser), savedUser.getName(), savedUser.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedInput = request.username().trim().toLowerCase();

        User user = userRepository
                .findByUsernameIgnoreCaseOrEmailIgnoreCase(normalizedInput, normalizedInput)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username/email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username/email or password");
        }

        String principal = resolvePrincipal(user);
        String token = jwtService.generateToken(principal);

        return new AuthResponse(
                toAuthUser(user),
                token
        );
    }

    public AuthUser getCurrentUser(String principal) {
        User user = userRepository
                .findByUsernameIgnoreCaseOrEmailIgnoreCase(principal, principal)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        return toAuthUser(user);
    }

    public long userCount() {
        return userRepository.count();
    }

    private AuthUser toAuthUser(User user) {
        return new AuthUser(
                user.getId(),
                resolvePrincipal(user),
                user.getName(),
                user.getEmail()
        );
    }

    private String resolvePrincipal(User user) {
        return user.getUsername() != null && !user.getUsername().isBlank()
                ? user.getUsername()
                : user.getEmail();
    }

    private String normalizeUsername(RegisterRequest request) {
        String requested = request.username();
        if (requested != null && !requested.isBlank()) {
            return requested.trim().toLowerCase();
        }

        String base = request.name().trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        if (base.isBlank()) {
            base = "user";
        }
        return base;
    }

    private String ensureUniqueUsername(String baseUsername) {
        if (!userRepository.existsByUsernameIgnoreCase(baseUsername)) {
            return baseUsername;
        }

        int suffix = 1;
        String candidate = baseUsername + suffix;
        while (userRepository.existsByUsernameIgnoreCase(candidate)) {
            suffix++;
            candidate = baseUsername + suffix;
        }
        return candidate;
    }
}
