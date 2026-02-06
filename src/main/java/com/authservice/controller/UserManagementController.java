package com.authservice.controller;

import com.authservice.dto.AddressDTO;
import com.authservice.dto.UpdateUserProfileRequest;
import com.authservice.dto.UserDTO;
import com.authservice.service.UserAddressService;
import com.authservice.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User Profile Management Controller (for authenticated users)
 */
@RestController
@RequestMapping("/api/users")
public class UserManagementController {

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private UserAddressService addressService;

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        UserDTO user = userManagementService.getUserProfile(username);
        return ResponseEntity.ok(user);
    }

    /**
     * Update current user profile
     */
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateMyProfile(
            @Valid @RequestBody UpdateUserProfileRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        UserDTO user = userManagementService.updateUserProfile(username, request);
        return ResponseEntity.ok(user);
    }

    /**
     * Get current user profile with addresses
     */
    @GetMapping("/me/full")
    public ResponseEntity<Map<String, Object>> getFullProfile(Authentication authentication) {
        String username = authentication.getName();
        UserDTO user = userManagementService.getUserProfile(username);
        List<AddressDTO> addresses = addressService.getUserAddresses(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("addresses", addresses);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete current user account (soft delete / deactivation could be implemented)
     */
    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteMyAccount(Authentication authentication) {
        String username = authentication.getName();
        userManagementService.deleteUser(username);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Account successfully deleted");
        
        return ResponseEntity.ok(response);
    }
}
