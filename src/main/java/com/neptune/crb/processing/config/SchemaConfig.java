package com.neptune.crb.processing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "io.ci")
@Configuration("schemaConfig")
public class SchemaConfig {
    private String coreSchema;
    private int defaultLimit;
    private int yearsToKeepLog;
    private String enableDebug;
    private Integer idleTimeout;
    private String jwtSecret;
    private int jwtExpirationMs;
    private String smtpServer;
    private String smtpPort;
    private String emailAddress;
    private String emailUserName;
    private String emailPassword;
    private String billingEmailAddress;
    private String billingEmailPassword;
}
