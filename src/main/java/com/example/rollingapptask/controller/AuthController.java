package com.example.rollingapptask.controller;

import com.example.rollingapptask.model.Role;
import com.example.rollingapptask.model.RoleName;
import com.example.rollingapptask.model.User;
import com.example.rollingapptask.payload.ApiResponse;
import com.example.rollingapptask.payload.JwtAuthenticationResponse;
import com.example.rollingapptask.payload.LoginRequest;
import com.example.rollingapptask.payload.SignUpRequest;
import com.example.rollingapptask.repository.RoleRepository;
import com.example.rollingapptask.repository.UserRepository;
import com.example.rollingapptask.security.CurrentUser;
import com.example.rollingapptask.security.JwtTokenProvider;
import com.example.rollingapptask.exception.BadRequestException;
import com.example.rollingapptask.exception.AppException;
import com.example.rollingapptask.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/sign-in")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if(userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email Address already in use!");
        }

        User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
                signUpRequest.getEmail(), signUpRequest.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AppException("User Role not set."));

        user.setRoles(Collections.singleton(userRole));

        User result = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "User registered successfully"));
    }

    @PostMapping("/logout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> logout(@CurrentUser UserPrincipal currentUser) {
        // İleride token blacklist/invalidation eklenebilir
        return ResponseEntity.ok()
                .body(new ApiResponse(true, "User logged out successfully"));
    }
} 