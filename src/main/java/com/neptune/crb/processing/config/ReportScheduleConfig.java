package com.neptune.crb.processing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "report.schedule")
public class ReportScheduleConfig {
    private List<String> enabled;
    private String dailyCron;
    private String weeklyCron;
    private String monthlyCron;

    public List<String> getEnabled() {
        return enabled;
    }

    public void setEnabled(List<String> enabled) {
        this.enabled = enabled;
    }

    public String getDailyCron() {
        return dailyCron;
    }

    public void setDailyCron(String dailyCron) {
        this.dailyCron = dailyCron;
    }

    public String getWeeklyCron() {
        return weeklyCron;
    }

    public void setWeeklyCron(String weeklyCron) {
        this.weeklyCron = weeklyCron;
    }

    public String getMonthlyCron() {
        return monthlyCron;
    }

    public void setMonthlyCron(String monthlyCron) {
        this.monthlyCron = monthlyCron;
    }
}