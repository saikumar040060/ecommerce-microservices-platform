package com.saikumar.userservice.controller;

import com.saikumar.userservice.dto.UserDto;
import com.saikumar.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // POST /api/users/register
    @PostMapping("/register")
    public ResponseEntity<UserDto.AuthResponse> register(@Valid @RequestBody UserDto.RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(req));
    }

    // POST /api/users/login
    @PostMapping("/login")
    public ResponseEntity<UserDto.AuthResponse> login(@Valid @RequestBody UserDto.LoginRequest req) {
        return ResponseEntity.ok(userService.login(req));
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserDto.UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // GET /api/users/email/{email} - used internally by other services
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto.UserResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    // PUT /api/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<UserDto.UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UserDto.UpdateRequest req) {
        return ResponseEntity.ok(userService.updateUser(id, req));
    }

    // GET /api/users - admin only
    @GetMapping
    public ResponseEntity<List<UserDto.UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // DELETE /api/users/{id} - soft delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("user-service ok");
    }
}
