package com.authservice.dto;

import com.authservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer addressCount;

    public static AdminUserDTO from(User user) {
        return AdminUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    public static AdminUserDTO from(User user, int addressCount) {
        AdminUserDTO dto = from(user);
        dto.setAddressCount(addressCount);
        return dto;
    }
}
