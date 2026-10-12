package com.queueless.service;

import com.queueless.dto.Dtos.*;
import com.queueless.model.Role;
import com.queueless.model.User;
import com.queueless.repository.UserRepository;
import com.queueless.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// Register and login logic.
@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // Anyone who registers on the website becomes a STUDENT.
    public AuthResponse register(RegisterRequest request) {
        User user = createUser(request.name(), request.email(), request.password(), Role.STUDENT);
        return toAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // compare the typed password with the encrypted one in the database
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return toAuthResponse(user);
    }

    // Used by register (STUDENT), by the admin when creating staff (STAFF) and by the seeder (ADMIN)
    public User createUser(String name, String email, String password, Role role) {
        String cleanEmail = email.trim().toLowerCase();
        if (userRepo.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("This email is already registered");
        }
        User user = new User();
        user.setName(name.trim());
        user.setEmail(cleanEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        return userRepo.save(user);
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
