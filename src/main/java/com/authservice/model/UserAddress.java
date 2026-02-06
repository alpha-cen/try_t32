package com.authservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;
    
    @Column(name = "address_line2", length = 255)
    private String addressLine2;
    
    @Column(nullable = false, length = 100)
    private String city;
    
    @Column(nullable = false, length = 100)
    private String state;
    
    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;
    
    @Column(nullable = false, length = 100)
    private String country;
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @Column(name = "address_type", length = 50)
    private String addressType; // SHIPPING, BILLING, BOTH
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isDefault == null) {
            isDefault = false;
        }
        if (addressType == null) {
            addressType = "BOTH";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
