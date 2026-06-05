package com.neptune.crb.processing.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "creditinfo")
public class CreditInfoConfig {

    private String username;
    private String password;
    private String baseUrl;
}
