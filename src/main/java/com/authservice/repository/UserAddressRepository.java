package com.authservice.repository;

import com.authservice.model.User;
import com.authservice.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    
    List<UserAddress> findByUserOrderByIsDefaultDescCreatedAtDesc(User user);
    
    List<UserAddress> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
    
    Optional<UserAddress> findByIdAndUserId(Long id, Long userId);
    
    Optional<UserAddress> findByUserAndIsDefaultTrue(User user);
    
    int countByUserId(Long userId);
    
    @Modifying
    @Query("UPDATE UserAddress a SET a.isDefault = false WHERE a.user = :user AND a.id != :excludeId")
    void resetDefaultForUser(@Param("user") User user, @Param("excludeId") Long excludeId);
    
    @Modifying
    @Query("UPDATE UserAddress a SET a.isDefault = false WHERE a.user = :user")
    void resetAllDefaultForUser(@Param("user") User user);
    
    boolean existsByIdAndUserId(Long id, Long userId);
}
