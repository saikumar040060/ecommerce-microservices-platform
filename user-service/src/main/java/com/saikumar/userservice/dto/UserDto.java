package com.saikumar.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class UserDto {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @Email(message = "Enter a valid email")
        @NotBlank
        private String email;

        @NotBlank
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        private String phone;
        private String address;
    }

    @Data
    public static class LoginRequest {
        @Email
        @NotBlank
        private String email;

        @NotBlank
        private String password;
    }

    @Data
    public static class UpdateRequest {
        private String name;
        private String phone;
        private String address;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String email;
        private String name;
        private String role;
        private Long userId;

        public AuthResponse(String token, String email, String name, String role, Long userId) {
            this.token = token;
            this.email = email;
            this.name = name;
            this.role = role;
            this.userId = userId;
        }
    }

    @Data
    public static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String address;
        private String role;
        private boolean active;

        public static UserResponse from(com.saikumar.userservice.model.User user) {
            UserResponse res = new UserResponse();
            res.id = user.getId();
            res.name = user.getName();
            res.email = user.getEmail();
            res.phone = user.getPhone();
            res.address = user.getAddress();
            res.role = user.getRole().name();
            res.active = user.isActive();
            return res;
        }
    }
}
