package com.queueless.service;

import com.queueless.model.Role;
import com.queueless.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Runs once when the backend starts. Creates the first admin account if none exists,
// because nobody can register as admin from the website.
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final AuthService authService;

    public DataSeeder(UserRepository userRepo, AuthService authService) {
        this.userRepo = userRepo;
        this.authService = authService;
    }

    @Override
    public void run(String... args) {
        if (userRepo.findByRole(Role.ADMIN).isEmpty()) {
            authService.createUser("Admin", "admin@queueless.com", "admin123", Role.ADMIN);
            System.out.println("First admin created -> email: admin@queueless.com  password: admin123");
        }
    }
}
