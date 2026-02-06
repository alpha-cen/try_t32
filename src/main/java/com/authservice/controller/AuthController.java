package com.authservice.controller;

import com.authservice.dto.*;
import com.authservice.observability.MetricsService;
import com.authservice.service.CognitoService;
import com.authservice.service.UserService;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller using AWS Cognito with Observability
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private CognitoService cognitoService;

    @Autowired
    private UserService userService;

    @Autowired
    private MetricsService metricsService;

    /**
     * Login with AWS Cognito
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Timer.Sample sample = metricsService.startTimer();
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Login attempt for user: {}", request.getUsername());
            LoginResponse response = cognitoService.authenticateUser(request);
            
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordLoginSuccess();
            metricsService.recordLoginDuration(duration);
            logger.info("Login successful for user: {} (duration: {}ms)", request.getUsername(), duration);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordLoginFailure();
            logger.error("Login failed for user: {} (duration: {}ms)", request.getUsername(), duration, e);
            throw e;
        }
    }

    /**
     * Register new user with AWS Cognito
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Registration attempt for user: {}", request.getUsername());
            UserDTO user = cognitoService.registerUser(request);
            
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordRegistrationSuccess();
            metricsService.recordRegistrationDuration(duration);
            logger.info("Registration successful for user: {} (duration: {}ms)", request.getUsername(), duration);
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("message", "User registered successfully. Please check your email for verification.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordRegistrationFailure();
            logger.error("Registration failed for user: {} (duration: {}ms)", request.getUsername(), duration, e);
            throw e;
        }
    }

    /**
     * Get current authenticated user
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String username = extractUsernameFromAuthentication(authentication);
        UserDTO user = userService.findByUsername(username);
        
        return ResponseEntity.ok(user);
    }

    /**
     * Refresh access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        logger.info("Token refresh request");
        String newIdToken = cognitoService.refreshToken(refreshToken);
        metricsService.recordTokenRefresh();
        
        Map<String, String> response = new HashMap<>();
        response.put("idToken", newIdToken);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Sign out user
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            cognitoService.signOut(accessToken);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Signed out successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Signed out locally");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        
        String accessToken = authHeader.replace("Bearer ", "");
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().build();
        }

        logger.info("Password change request");
        cognitoService.changePassword(accessToken, oldPassword, newPassword);
        metricsService.recordPasswordChange();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Initiate forgot password flow
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        logger.info("Password reset request for user: {}", username);
        cognitoService.forgotPassword(username);
        metricsService.recordPasswordReset();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset code sent to your email");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Confirm forgot password with verification code
     */
    @PostMapping("/confirm-forgot-password")
    public ResponseEntity<Map<String, String>> confirmForgotPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String confirmationCode = request.get("confirmationCode");
        String newPassword = request.get("newPassword");

        if (username == null || confirmationCode == null || newPassword == null) {
            return ResponseEntity.badRequest().build();
        }

        cognitoService.confirmForgotPassword(username, confirmationCode, newPassword);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Extract username from JWT authentication
     */
    private String extractUsernameFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String username = jwt.getClaimAsString("cognito:username");
            if (username != null) {
                return username;
            }
            
            username = jwt.getClaimAsString("username");
            if (username != null) {
                return username;
            }
            
            return jwt.getSubject();
        }
        
        return authentication.getName();
    }
}
