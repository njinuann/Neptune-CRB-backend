package com.neptune.crb.processing.service;

/**
 * @author NJINU <alexnyiha@gmail.com>
 * @version 1.0
 * @since 21-Mar-26
 */
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class CaffeineTokenService extends TokenService
{

    private Cache<String, String> tokenCache;
    private static final String TOKEN_KEY = "metropol:access_token";

    public CaffeineTokenService(AuthenticationService authenticationService) {
        super(authenticationService);
    }

    @PostConstruct
    public void init() {
        // Initialize Caffeine cache
        tokenCache = Caffeine.newBuilder()
                .initialCapacity(10)
                .maximumSize(100)
                .expireAfterWrite(3500, TimeUnit.SECONDS) // 3500 seconds as in Redis
                .recordStats()
                .build();
    }

    @Override
    public synchronized String getToken() throws IOException {
        String cachedToken = tokenCache.getIfPresent(TOKEN_KEY);
        if (cachedToken != null) {
            return cachedToken;
        }
        refreshToken();
        return tokenCache.getIfPresent(TOKEN_KEY);
    }

    @Override
    protected void refreshToken() throws IOException {
        super.refreshToken();
        tokenCache.put(TOKEN_KEY, token);
    }

    // Optional: Add method to check cache statistics
    public CacheStats getCacheStats() {
        return tokenCache.stats();
    }

    // Optional: Add method to manually clear cache
    public void clearToken() {
        tokenCache.invalidate(TOKEN_KEY);
    }
}
