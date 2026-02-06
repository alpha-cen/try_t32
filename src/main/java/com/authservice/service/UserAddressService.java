package com.authservice.service;

import com.authservice.dto.AddressDTO;
import com.authservice.dto.CreateAddressRequest;
import com.authservice.dto.UpdateAddressRequest;
import com.authservice.exception.ResourceNotFoundException;
import com.authservice.model.User;
import com.authservice.model.UserAddress;
import com.authservice.observability.MetricsService;
import com.authservice.repository.UserAddressRepository;
import com.authservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserAddressService {

    private static final Logger logger = LoggerFactory.getLogger(UserAddressService.class);

    @Autowired
    private UserAddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MetricsService metricsService;

    public List<AddressDTO> getUserAddresses(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        return addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user).stream()
                .map(AddressDTO::from)
                .collect(Collectors.toList());
    }

    public List<AddressDTO> getUserAddressesByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId).stream()
                .map(AddressDTO::from)
                .collect(Collectors.toList());
    }

    public AddressDTO getAddressById(Long addressId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        UserAddress address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        
        return AddressDTO.from(address);
    }

    @Transactional
    public AddressDTO createAddress(String username, CreateAddressRequest request) {
        logger.info("Creating address for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // If this is set as default, unset other defaults
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.resetAllDefaultForUser(user);
        }

        UserAddress address = new UserAddress();
        address.setUser(user);
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        address.setAddressType(request.getAddressType() != null ? request.getAddressType() : "BOTH");

        address = addressRepository.save(address);
        logger.info("Address created with id: {} for user: {}", address.getId(), username);
        
        metricsService.recordAddressCreated();

        return AddressDTO.from(address);
    }

    @Transactional
    public AddressDTO updateAddress(Long addressId, String username, UpdateAddressRequest request) {
        logger.info("Updating address {} for user: {}", addressId, username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        UserAddress address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        // If this is being set as default, unset other defaults
        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.getIsDefault()) {
            addressRepository.resetDefaultForUser(user, addressId);
        }

        // Update only provided fields
        if (request.getAddressLine1() != null) {
            address.setAddressLine1(request.getAddressLine1());
        }
        if (request.getAddressLine2() != null) {
            address.setAddressLine2(request.getAddressLine2());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getState() != null) {
            address.setState(request.getState());
        }
        if (request.getPostalCode() != null) {
            address.setPostalCode(request.getPostalCode());
        }
        if (request.getCountry() != null) {
            address.setCountry(request.getCountry());
        }
        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }
        if (request.getAddressType() != null) {
            address.setAddressType(request.getAddressType());
        }

        address = addressRepository.save(address);
        logger.info("Address {} updated for user: {}", addressId, username);
        
        metricsService.recordAddressUpdated();

        return AddressDTO.from(address);
    }

    @Transactional
    public void deleteAddress(Long addressId, String username) {
        logger.info("Deleting address {} for user: {}", addressId, username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!addressRepository.existsByIdAndUserId(addressId, user.getId())) {
            throw new ResourceNotFoundException("Address not found with id: " + addressId);
        }

        addressRepository.deleteById(addressId);
        logger.info("Address {} deleted for user: {}", addressId, username);
        
        metricsService.recordAddressDeleted();
    }

    @Transactional
    public AddressDTO setDefaultAddress(Long addressId, String username) {
        logger.info("Setting address {} as default for user: {}", addressId, username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        UserAddress address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        // Reset all other defaults
        addressRepository.resetDefaultForUser(user, addressId);

        // Set this as default
        address.setIsDefault(true);
        address = addressRepository.save(address);

        logger.info("Address {} set as default for user: {}", addressId, username);
        metricsService.recordDefaultAddressChanged();
        
        return AddressDTO.from(address);
    }

    public AddressDTO getDefaultAddress(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return addressRepository.findByUserAndIsDefaultTrue(user)
                .map(AddressDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("No default address found for user: " + username));
    }
}
