//package com.ryleq.crb.crb.processing.service;
//
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.time.Duration;
//
//@Service
//public class RedisTokenService extends TokenService {
//
//    private final StringRedisTemplate redisTemplate;
//    private static final String TOKEN_KEY = "metropol:access_token";
//
//    public RedisTokenService(AuthenticationService authenticationService, StringRedisTemplate redisTemplate) {
//        super(authenticationService);
//        this.redisTemplate = redisTemplate;
//    }
//
//    @Override
//    public synchronized String getToken() throws IOException {
//        String cachedToken = redisTemplate.opsForValue().get(TOKEN_KEY);
//        if (cachedToken != null) {
//            return cachedToken;
//        }
//        refreshToken();
//        return redisTemplate.opsForValue().get(TOKEN_KEY);
//    }
//
//    @Override
//    protected void refreshToken() throws IOException {
//        super.refreshToken();
//        redisTemplate.opsForValue().set(TOKEN_KEY, token, Duration.ofSeconds(3500));
//    }
//}
//
