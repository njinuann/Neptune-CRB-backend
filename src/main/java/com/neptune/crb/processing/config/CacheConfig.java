package com.neptune.crb.processing.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;
import java.time.Duration;
/**
 * @author NJINU <alexnyiha@gmail.com>
 * @version 1.0
 * @since 21-Mar-26
 */


@Configuration
@EnableCaching
public class CacheConfig {

 @Bean
 public CacheManager cacheManager() {
  CaffeineCacheManager cacheManager = new CaffeineCacheManager("metropol:access_token");
  cacheManager.setCaffeine(caffeineCacheBuilder());
  return cacheManager;
 }

 @Bean
 public Caffeine<Object, Object> caffeineCacheBuilder() {
  return Caffeine.newBuilder()
          .initialCapacity(10)
          .maximumSize(100)
          .expireAfterWrite(Duration.ofSeconds(3500)) // Match your Redis expiration
          .recordStats();
 }
}
