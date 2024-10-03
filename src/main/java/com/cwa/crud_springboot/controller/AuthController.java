// AuthController.java
package com.cwa.crud_springboot.controller;

import com.cwa.crud_springboot.dto.ApiResponse;
import com.cwa.crud_springboot.dto.JwtAuthenticationResponse;
import com.cwa.crud_springboot.dto.LoginRequest;
import com.cwa.crud_springboot.dto.SignUpRequest;
import com.cwa.crud_springboot.entity.User;
import com.cwa.crud_springboot.repository.UserRepository;
import com.cwa.crud_springboot.service.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (signUpRequest.getUsername() == null || signUpRequest.getUsername().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Username is required"));
        }

        if (signUpRequest.getPassword() == null || signUpRequest.getPassword().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Password is required"));
        }

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Username is already taken!"));
        }

        User user = new User(signUpRequest.getUsername(), signUpRequest.getPassword());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse(true, "User registered successfully"));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Username is required"));
        }

        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Password is required"));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Générer les tokens
        Map<String, String> tokens = tokenProvider.generateTokens(authentication);

        // Retourner les tokens (tu peux aussi créer un DTO pour la réponse)
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody JwtAuthenticationResponse jwtAuthenticationResponse) {
        if (tokenProvider.validateToken(jwtAuthenticationResponse.getRefreshToken())) {
            Long userId = tokenProvider.getUserIdFromJWT(jwtAuthenticationResponse.getRefreshToken());
            // Here you should check if the refresh token is in your database and not revoked

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            String newAccessToken = tokenProvider.generateAccessToken(authentication);

            return ResponseEntity.ok(new JwtAuthenticationResponse(newAccessToken, jwtAuthenticationResponse.getRefreshToken()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "Invalid refresh token"));
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<Optional<User>> getUserById(@RequestBody String username) {
        Optional<User> existUser = userRepository.findByUsername(username);
        return ResponseEntity.ok(existUser);
    }
}