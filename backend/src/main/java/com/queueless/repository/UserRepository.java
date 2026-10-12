package com.queueless.repository;

import com.queueless.model.Role;
import com.queueless.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// A repository talks to the database. We only write method NAMES, Spring writes the SQL for us.
// Example: findByEmail -> SELECT * FROM users WHERE email = ?
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
}
