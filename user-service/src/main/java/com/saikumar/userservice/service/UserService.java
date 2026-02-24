package com.saikumar.userservice.service;

import com.saikumar.userservice.dto.UserDto;
import com.saikumar.userservice.exception.UserAlreadyExistsException;
import com.saikumar.userservice.exception.UserNotFoundException;
import com.saikumar.userservice.model.User;
import com.saikumar.userservice.repository.UserRepository;
import com.saikumar.userservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserDto.AuthResponse register(UserDto.RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered: " + req.getEmail());
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setPhone(req.getPhone());
        user.setAddress(req.getAddress());

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new UserDto.AuthResponse(token, user.getEmail(), user.getName(), user.getRole().name(), user.getId());
    }

    public UserDto.AuthResponse login(UserDto.LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        log.info("User logged in: {}", user.getEmail());
        return new UserDto.AuthResponse(token, user.getEmail(), user.getName(), user.getRole().name(), user.getId());
    }

    public UserDto.UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        return UserDto.UserResponse.from(user);
    }

    public UserDto.UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
        return UserDto.UserResponse.from(user);
    }

    public UserDto.UserResponse updateUser(Long id, UserDto.UpdateRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        if (req.getName() != null) user.setName(req.getName());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getAddress() != null) user.setAddress(req.getAddress());

        userRepository.save(user);
        return UserDto.UserResponse.from(user);
    }

    public List<UserDto.UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDto.UserResponse::from)
                .collect(Collectors.toList());
    }

    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}", user.getEmail());
    }
}
