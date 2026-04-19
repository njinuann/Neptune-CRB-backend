package com.neptune.crb.processing;

import com.neptune.crb.processing.config.SchemaConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(SchemaConfig.class)
@EnableScheduling
@ComponentScan(basePackages = "com.neptune.crb")
public class CrbProcessingApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrbProcessingApplication.class, args);
	}

}
