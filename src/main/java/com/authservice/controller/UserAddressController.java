package com.authservice.controller;

import com.authservice.dto.AddressDTO;
import com.authservice.dto.CreateAddressRequest;
import com.authservice.dto.UpdateAddressRequest;
import com.authservice.service.UserAddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Address Management Controller
 */
@RestController
@RequestMapping("/api/users")
public class UserAddressController {

    @Autowired
    private UserAddressService addressService;

    /**
     * Get all addresses for the current user
     */
    @GetMapping("/me/addresses")
    public ResponseEntity<List<AddressDTO>> getMyAddresses(Authentication authentication) {
        String username = authentication.getName();
        List<AddressDTO> addresses = addressService.getUserAddresses(username);
        return ResponseEntity.ok(addresses);
    }

    /**
     * Get a specific address by ID
     */
    @GetMapping("/me/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(
            @PathVariable Long addressId,
            Authentication authentication) {
        String username = authentication.getName();
        AddressDTO address = addressService.getAddressById(addressId, username);
        return ResponseEntity.ok(address);
    }

    /**
     * Get default address for current user
     */
    @GetMapping("/me/addresses/default")
    public ResponseEntity<AddressDTO> getDefaultAddress(Authentication authentication) {
        String username = authentication.getName();
        AddressDTO address = addressService.getDefaultAddress(username);
        return ResponseEntity.ok(address);
    }

    /**
     * Create a new address
     */
    @PostMapping("/me/addresses")
    public ResponseEntity<AddressDTO> createAddress(
            @Valid @RequestBody CreateAddressRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        AddressDTO address = addressService.createAddress(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    /**
     * Update an existing address
     */
    @PutMapping("/me/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody UpdateAddressRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        AddressDTO address = addressService.updateAddress(addressId, username, request);
        return ResponseEntity.ok(address);
    }

    /**
     * Delete an address
     */
    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long addressId,
            Authentication authentication) {
        String username = authentication.getName();
        addressService.deleteAddress(addressId, username);
        return ResponseEntity.noContent().build();
    }

    /**
     * Set an address as default
     */
    @PatchMapping("/me/addresses/{addressId}/default")
    public ResponseEntity<AddressDTO> setDefaultAddress(
            @PathVariable Long addressId,
            Authentication authentication) {
        String username = authentication.getName();
        AddressDTO address = addressService.setDefaultAddress(addressId, username);
        return ResponseEntity.ok(address);
    }
}
