package com.queueless.model;

import jakarta.persistence.*;

// @Entity = this Java class becomes a MySQL table. Each object = one row.
// We name the table "users" because "user" is a reserved word in some databases.
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL gives 1, 2, 3... automatically
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true) // two users cannot have the same email
    private String email;

    @Column(nullable = false)
    private String password; // stored encrypted (BCrypt), never as plain text

    @Enumerated(EnumType.STRING) // saves "STUDENT" instead of 0/1/2
    @Column(nullable = false)
    private Role role;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
