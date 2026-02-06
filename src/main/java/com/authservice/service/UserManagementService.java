package com.authservice.service;

import com.authservice.dto.AdminUserDTO;
import com.authservice.dto.AdminUserUpdateRequest;
import com.authservice.dto.UpdateUserProfileRequest;
import com.authservice.dto.UserDTO;
import com.authservice.exception.ResourceNotFoundException;
import com.authservice.model.User;
import com.authservice.observability.MetricsService;
import com.authservice.repository.UserAddressRepository;
import com.authservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserManagementService {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAddressRepository addressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private MetricsService metricsService;

    public UserDTO getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return UserDTO.from(user);
    }

    @Transactional
    public UserDTO updateUserProfile(String username, UpdateUserProfileRequest request) {
        logger.info("Updating profile for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        boolean updated = false;

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(request.getEmail());
            updated = true;
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
            updated = true;
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
            updated = true;
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
            updated = true;
        }

        if (updated) {
            user.setUpdatedAt(LocalDateTime.now());
            user = userRepository.save(user);
            logger.info("Profile updated for user: {}", username);
            metricsService.recordUserProfileUpdate();
        }

        return UserDTO.from(user);
    }

    public List<AdminUserDTO> getAllUsers(String search) {
        List<User> users;
        
        if (search == null || search.trim().isEmpty()) {
            users = userRepository.findAll();
        } else {
            String query = search.trim();
            users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
        }

        return users.stream()
                .map(user -> {
                    int addressCount = addressRepository.countByUserId(user.getId());
                    return AdminUserDTO.from(user, addressCount);
                })
                .collect(Collectors.toList());
    }

    public AdminUserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        int addressCount = addressRepository.countByUserId(userId);
        return AdminUserDTO.from(user, addressCount);
    }

    @Transactional
    public AdminUserDTO updateUser(Long userId, AdminUserUpdateRequest request) {
        logger.info("Admin updating user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        boolean updated = false;

        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            String newUsername = request.getUsername().trim();
            if (!newUsername.equals(user.getUsername()) && userRepository.existsByUsername(newUsername)) {
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(newUsername);
            updated = true;
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String newEmail = request.getEmail().trim();
            if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(newEmail);
            updated = true;
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName().trim().isEmpty() ? null : request.getFirstName().trim());
            updated = true;
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName().trim().isEmpty() ? null : request.getLastName().trim());
            updated = true;
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim().isEmpty() ? null : request.getPhone().trim());
            updated = true;
        }

        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            user.setRole(request.getRole().trim().toUpperCase());
            updated = true;
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
            updated = true;
        }

        if (updated) {
            user.setUpdatedAt(LocalDateTime.now());
            user = userRepository.save(user);
            logger.info("User {} updated by admin", userId);
            metricsService.recordAdminUserUpdate();
        }

        int addressCount = addressRepository.countByUserId(userId);
        return AdminUserDTO.from(user, addressCount);
    }

    @Transactional
    public void deleteUser(String username) {
        logger.info("Deleting user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        userRepository.delete(user);
        logger.info("User deleted: {}", username);
        metricsService.recordUserDeletion();
    }

    @Transactional
    public void deleteUserById(Long userId) {
        logger.info("Admin deleting user: {}", userId);
        
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        userRepository.deleteById(userId);
        logger.info("User {} deleted by admin", userId);
        metricsService.recordUserDeletion();
    }

    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = userRepository.count();
        long adminCount = userRepository.countByRole("ADMIN");
        long userCount = userRepository.countByRole("USER");
        
        stats.put("totalUsers", totalUsers);
        stats.put("adminCount", adminCount);
        stats.put("userCount", userCount);
        stats.put("totalAddresses", addressRepository.count());
        
        return stats;
    }
}
