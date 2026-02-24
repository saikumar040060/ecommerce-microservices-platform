package com.saikumar.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    private String password; // stored as bcrypt hash

    private String phone;

    private String address;

    @Enumerated(EnumType.STRING)
    private Role role = Role.CUSTOMER;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role {
        CUSTOMER, ADMIN
    }
}
