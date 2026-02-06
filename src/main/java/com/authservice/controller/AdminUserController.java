package com.authservice.controller;

import com.authservice.dto.AddressDTO;
import com.authservice.dto.AdminUserDTO;
import com.authservice.dto.AdminUserUpdateRequest;
import com.authservice.service.UserAddressService;
import com.authservice.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin User Management Controller
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private UserAddressService addressService;

    /**
     * Get all users (with optional search)
     */
    @GetMapping
    public ResponseEntity<List<AdminUserDTO>> getAllUsers(
            @RequestParam(required = false) String search) {
        List<AdminUserDTO> users = userManagementService.getAllUsers(search);
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDTO> getUserById(@PathVariable Long userId) {
        AdminUserDTO user = userManagementService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Get user with full details (including addresses)
     */
    @GetMapping("/{userId}/full")
    public ResponseEntity<Map<String, Object>> getUserFull(@PathVariable Long userId) {
        AdminUserDTO user = userManagementService.getUserById(userId);
        List<AddressDTO> addresses = addressService.getUserAddressesByUserId(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("addresses", addresses);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update user details
     */
    @PutMapping("/{userId}")
    public ResponseEntity<AdminUserDTO> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        AdminUserDTO user = userManagementService.updateUser(userId, request);
        return ResponseEntity.ok(user);
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        userManagementService.deleteUserById(userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User successfully deleted");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get user statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        Map<String, Object> stats = userManagementService.getUserStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get user addresses (admin view)
     */
    @GetMapping("/{userId}/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddresses(@PathVariable Long userId) {
        List<AddressDTO> addresses = addressService.getUserAddressesByUserId(userId);
        return ResponseEntity.ok(addresses);
    }
}
