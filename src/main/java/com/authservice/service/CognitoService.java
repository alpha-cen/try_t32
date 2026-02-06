package com.authservice.service;

import com.authservice.config.AwsCognitoConfig;
import com.authservice.dto.LoginRequest;
import com.authservice.dto.LoginResponse;
import com.authservice.dto.RegisterRequest;
import com.authservice.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * AWS Cognito Service for user authentication and management
 */
@Service
public class CognitoService {

    private static final Logger logger = LoggerFactory.getLogger(CognitoService.class);

    @Autowired
    private CognitoIdentityProviderClient cognitoClient;

    @Autowired
    private AwsCognitoConfig cognitoConfig;

    @Autowired
    private UserService userService;

    /**
     * Authenticate user with AWS Cognito
     */
    public LoginResponse authenticateUser(LoginRequest request) {
        try {
            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", request.getUsername());
            authParams.put("PASSWORD", request.getPassword());

            // Add SECRET_HASH if client secret is configured
            if (cognitoConfig.getClientSecret() != null && !cognitoConfig.getClientSecret().isEmpty()) {
                String secretHash = calculateSecretHash(
                        cognitoConfig.getClientId(),
                        cognitoConfig.getClientSecret(),
                        request.getUsername()
                );
                authParams.put("SECRET_HASH", secretHash);
            }

            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(cognitoConfig.getClientId())
                    .authParameters(authParams)
                    .build();

            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);

            AuthenticationResultType authResult = authResponse.authenticationResult();
            String idToken = authResult.idToken();
            // Note: accessToken and refreshToken are available but not used in this response
            // They can be returned to the client if needed for token refresh or other operations

            // Get user information from local database or create/sync with Cognito
            UserDTO user = userService.findByUsername(request.getUsername());

            logger.info("User authenticated successfully: {}", request.getUsername());

            return new LoginResponse(idToken, user);

        } catch (NotAuthorizedException e) {
            logger.error("Authentication failed for user: {}", request.getUsername(), e);
            throw new IllegalArgumentException("Invalid username or password");
        } catch (UserNotFoundException e) {
            logger.error("User not found: {}", request.getUsername(), e);
            throw new IllegalArgumentException("User not found");
        } catch (Exception e) {
            logger.error("Error during authentication", e);
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Register new user in AWS Cognito and local database
     */
    public UserDTO registerUser(RegisterRequest request) {
        try {
            // Create attributes for Cognito user
            List<AttributeType> attributes = new ArrayList<>();
            attributes.add(AttributeType.builder()
                    .name("email")
                    .value(request.getEmail())
                    .build());
            attributes.add(AttributeType.builder()
                    .name("given_name")
                    .value(request.getFirstName())
                    .build());
            attributes.add(AttributeType.builder()
                    .name("family_name")
                    .value(request.getLastName())
                    .build());
            
            if (request.getPhone() != null && !request.getPhone().isEmpty()) {
                attributes.add(AttributeType.builder()
                        .name("phone_number")
                        .value(request.getPhone())
                        .build());
            }

            // Build sign up request
            SignUpRequest.Builder signUpRequestBuilder = SignUpRequest.builder()
                    .clientId(cognitoConfig.getClientId())
                    .username(request.getUsername())
                    .password(request.getPassword())
                    .userAttributes(attributes);

            // Add SECRET_HASH if client secret is configured
            if (cognitoConfig.getClientSecret() != null && !cognitoConfig.getClientSecret().isEmpty()) {
                String secretHash = calculateSecretHash(
                        cognitoConfig.getClientId(),
                        cognitoConfig.getClientSecret(),
                        request.getUsername()
                );
                signUpRequestBuilder.secretHash(secretHash);
            }

            SignUpResponse signUpResponse = cognitoClient.signUp(signUpRequestBuilder.build());

            logger.info("User created in Cognito: {}, UserSub: {}", 
                    request.getUsername(), signUpResponse.userSub());

            // Also create user in local database for relational data
            UserDTO localUser = userService.registerUser(request);

            // Auto-confirm user if needed (in production, you'd use email/SMS verification)
            autoConfirmUser(request.getUsername());

            return localUser;

        } catch (UsernameExistsException e) {
            logger.error("Username already exists: {}", request.getUsername(), e);
            throw new IllegalArgumentException("Username already exists");
        } catch (InvalidPasswordException e) {
            logger.error("Invalid password for user: {}", request.getUsername(), e);
            throw new IllegalArgumentException("Password does not meet requirements: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error during user registration", e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Auto-confirm user (useful for development/testing)
     */
    private void autoConfirmUser(String username) {
        try {
            AdminConfirmSignUpRequest confirmRequest = AdminConfirmSignUpRequest.builder()
                    .userPoolId(cognitoConfig.getUserPoolId())
                    .username(username)
                    .build();

            cognitoClient.adminConfirmSignUp(confirmRequest);
            logger.info("User auto-confirmed: {}", username);
        } catch (Exception e) {
            logger.warn("Could not auto-confirm user: {}", username, e);
        }
    }

    /**
     * Refresh access token using refresh token
     */
    public String refreshToken(String refreshToken) {
        try {
            Map<String, String> authParams = new HashMap<>();
            authParams.put("REFRESH_TOKEN", refreshToken);

            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                    .clientId(cognitoConfig.getClientId())
                    .authParameters(authParams)
                    .build();

            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
            return authResponse.authenticationResult().idToken();

        } catch (Exception e) {
            logger.error("Error refreshing token", e);
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }

    /**
     * Sign out user from Cognito
     */
    public void signOut(String accessToken) {
        try {
            GlobalSignOutRequest signOutRequest = GlobalSignOutRequest.builder()
                    .accessToken(accessToken)
                    .build();

            cognitoClient.globalSignOut(signOutRequest);
            logger.info("User signed out successfully");

        } catch (Exception e) {
            logger.error("Error during sign out", e);
            throw new RuntimeException("Sign out failed: " + e.getMessage());
        }
    }

    /**
     * Change user password
     */
    public void changePassword(String accessToken, String oldPassword, String newPassword) {
        try {
            ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                    .accessToken(accessToken)
                    .previousPassword(oldPassword)
                    .proposedPassword(newPassword)
                    .build();

            cognitoClient.changePassword(changePasswordRequest);
            logger.info("Password changed successfully");

        } catch (Exception e) {
            logger.error("Error changing password", e);
            throw new RuntimeException("Password change failed: " + e.getMessage());
        }
    }

    /**
     * Initiate forgot password flow
     */
    public void forgotPassword(String username) {
        try {
            ForgotPasswordRequest.Builder requestBuilder = ForgotPasswordRequest.builder()
                    .clientId(cognitoConfig.getClientId())
                    .username(username);

            if (cognitoConfig.getClientSecret() != null && !cognitoConfig.getClientSecret().isEmpty()) {
                String secretHash = calculateSecretHash(
                        cognitoConfig.getClientId(),
                        cognitoConfig.getClientSecret(),
                        username
                );
                requestBuilder.secretHash(secretHash);
            }

            cognitoClient.forgotPassword(requestBuilder.build());
            logger.info("Forgot password initiated for user: {}", username);

        } catch (Exception e) {
            logger.error("Error initiating forgot password", e);
            throw new RuntimeException("Forgot password failed: " + e.getMessage());
        }
    }

    /**
     * Confirm forgot password with verification code
     */
    public void confirmForgotPassword(String username, String confirmationCode, String newPassword) {
        try {
            ConfirmForgotPasswordRequest.Builder requestBuilder = ConfirmForgotPasswordRequest.builder()
                    .clientId(cognitoConfig.getClientId())
                    .username(username)
                    .confirmationCode(confirmationCode)
                    .password(newPassword);

            if (cognitoConfig.getClientSecret() != null && !cognitoConfig.getClientSecret().isEmpty()) {
                String secretHash = calculateSecretHash(
                        cognitoConfig.getClientId(),
                        cognitoConfig.getClientSecret(),
                        username
                );
                requestBuilder.secretHash(secretHash);
            }

            cognitoClient.confirmForgotPassword(requestBuilder.build());
            logger.info("Password reset confirmed for user: {}", username);

        } catch (Exception e) {
            logger.error("Error confirming forgot password", e);
            throw new RuntimeException("Confirm forgot password failed: " + e.getMessage());
        }
    }

    /**
     * Calculate SECRET_HASH for Cognito authentication
     */
    private String calculateSecretHash(String clientId, String clientSecret, String username) {
        try {
            String message = username + clientId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    clientSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating secret hash", e);
        }
    }
}
