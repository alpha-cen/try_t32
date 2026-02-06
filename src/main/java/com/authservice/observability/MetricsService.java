package com.authservice.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for tracking custom application metrics
 */
@Service
public class MetricsService {

    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Counter registrationSuccessCounter;
    private final Counter registrationFailureCounter;
    private final Counter passwordResetCounter;
    private final Counter passwordChangeCounter;
    private final Counter tokenRefreshCounter;
    private final Timer loginTimer;
    private final Timer registrationTimer;
    
    // User Management Metrics
    private final Counter userProfileUpdateCounter;
    private final Counter userDeletionCounter;
    private final Counter adminUserUpdateCounter;
    
    // Address Management Metrics
    private final Counter addressCreatedCounter;
    private final Counter addressUpdatedCounter;
    private final Counter addressDeletedCounter;
    private final Counter defaultAddressChangedCounter;

    public MetricsService(MeterRegistry meterRegistry) {
        // Authentication Metrics
        this.loginSuccessCounter = Counter.builder("auth.login.success")
                .description("Number of successful login attempts")
                .tag("status", "success")
                .register(meterRegistry);

        this.loginFailureCounter = Counter.builder("auth.login.failure")
                .description("Number of failed login attempts")
                .tag("status", "failure")
                .register(meterRegistry);

        this.registrationSuccessCounter = Counter.builder("auth.registration.success")
                .description("Number of successful user registrations")
                .tag("status", "success")
                .register(meterRegistry);

        this.registrationFailureCounter = Counter.builder("auth.registration.failure")
                .description("Number of failed user registrations")
                .tag("status", "failure")
                .register(meterRegistry);

        this.passwordResetCounter = Counter.builder("auth.password.reset")
                .description("Number of password reset requests")
                .register(meterRegistry);

        this.passwordChangeCounter = Counter.builder("auth.password.change")
                .description("Number of password change requests")
                .register(meterRegistry);

        this.tokenRefreshCounter = Counter.builder("auth.token.refresh")
                .description("Number of token refresh requests")
                .register(meterRegistry);

        // Timing Metrics
        this.loginTimer = Timer.builder("auth.login.duration")
                .description("Login request duration")
                .register(meterRegistry);

        this.registrationTimer = Timer.builder("auth.registration.duration")
                .description("Registration request duration")
                .register(meterRegistry);
        
        // User Management Metrics
        this.userProfileUpdateCounter = Counter.builder("user.profile.update")
                .description("Number of user profile updates")
                .register(meterRegistry);
        
        this.userDeletionCounter = Counter.builder("user.deletion")
                .description("Number of user deletions")
                .register(meterRegistry);
        
        this.adminUserUpdateCounter = Counter.builder("admin.user.update")
                .description("Number of admin user updates")
                .register(meterRegistry);
        
        // Address Management Metrics
        this.addressCreatedCounter = Counter.builder("address.created")
                .description("Number of addresses created")
                .register(meterRegistry);
        
        this.addressUpdatedCounter = Counter.builder("address.updated")
                .description("Number of addresses updated")
                .register(meterRegistry);
        
        this.addressDeletedCounter = Counter.builder("address.deleted")
                .description("Number of addresses deleted")
                .register(meterRegistry);
        
        this.defaultAddressChangedCounter = Counter.builder("address.default.changed")
                .description("Number of default address changes")
                .register(meterRegistry);
    }

    // Authentication Methods
    public void recordLoginSuccess() {
        loginSuccessCounter.increment();
    }

    public void recordLoginFailure() {
        loginFailureCounter.increment();
    }

    public void recordRegistrationSuccess() {
        registrationSuccessCounter.increment();
    }

    public void recordRegistrationFailure() {
        registrationFailureCounter.increment();
    }

    public void recordPasswordReset() {
        passwordResetCounter.increment();
    }

    public void recordPasswordChange() {
        passwordChangeCounter.increment();
    }

    public void recordTokenRefresh() {
        tokenRefreshCounter.increment();
    }

    public void recordLoginDuration(long durationMillis) {
        loginTimer.record(durationMillis, TimeUnit.MILLISECONDS);
    }

    public void recordRegistrationDuration(long durationMillis) {
        registrationTimer.record(durationMillis, TimeUnit.MILLISECONDS);
    }
    
    // User Management Methods
    public void recordUserProfileUpdate() {
        userProfileUpdateCounter.increment();
    }
    
    public void recordUserDeletion() {
        userDeletionCounter.increment();
    }
    
    public void recordAdminUserUpdate() {
        adminUserUpdateCounter.increment();
    }
    
    // Address Management Methods
    public void recordAddressCreated() {
        addressCreatedCounter.increment();
    }
    
    public void recordAddressUpdated() {
        addressUpdatedCounter.increment();
    }
    
    public void recordAddressDeleted() {
        addressDeletedCounter.increment();
    }
    
    public void recordDefaultAddressChanged() {
        defaultAddressChangedCounter.increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start();
    }

    public void stopTimer(Timer.Sample sample, Timer timer) {
        sample.stop(timer);
    }
}
