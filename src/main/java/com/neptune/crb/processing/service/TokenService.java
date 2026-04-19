package com.neptune.crb.processing.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;

@Service
public class TokenService {

    private final AuthenticationService authenticationService;

    protected String token;
    private Instant tokenExpiryTime;

    public TokenService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Get the valid token, refreshing it if necessary.
     *
     * @return a valid OAuth token
     * @throws IOException if token retrieval fails
     */
    public synchronized String getToken() throws IOException {
        if (token == null || isTokenExpired()) {
            refreshToken();
        }
        return token;
    }

    /**
     * Check if the current token is expired.
     *
     * @return true if expired, false otherwise
     */
    private boolean isTokenExpired() {
        return tokenExpiryTime == null || Instant.now().isAfter(tokenExpiryTime);
    }

    /**
     * Refresh the token by calling the authentication service.
     *
     * @throws IOException if token generation fails
     */
    protected void refreshToken() throws IOException {
        String response = authenticationService.authenticate();
        this.token = response; // Assign the token from the authentication service
        this.tokenExpiryTime = Instant.now().plusSeconds(3500); // Assuming a 3599-second expiry
    }
}
