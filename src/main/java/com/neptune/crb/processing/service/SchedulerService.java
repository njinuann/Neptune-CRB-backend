package com.neptune.crb.processing.service;

import com.neptune.crb.processing.config.ReportScheduleConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Value;

import static com.neptune.crb.processing.util.Logger.logError;
import static com.neptune.crb.processing.util.Logger.logEvent;

@Service
public class SchedulerService
{
    @Value("${report.schedule.daily.cron:0 0 2 * * ?}")  // Default: 2 AM daily
    private String dailyCronValue;

    @Value("${report.schedule.weekly.cron:0 0 2 * * MON}") // Default: 2 AM every Monday
    private String weeklyCronValue;

    @Value("${report.schedule.monthly.cron:0 0 2 1 * ?}")  // Default: 2 AM on 1st of month
    private String monthlyCronValue;

    private final ReportScheduleConfig scheduleConfig;
    private final Environment environment;
    private final ReportingService reportingService;


    @Autowired
    public SchedulerService(ReportScheduleConfig scheduleConfig,
                            Environment environment,
                            ReportingService reportingService)
    {
        this.scheduleConfig = scheduleConfig;
        this.environment = environment;
        this.reportingService = reportingService;
    }


    @PostConstruct
    public void init()
    {
        logEvent("Server timezone: {}", ZoneId.systemDefault().toString());
        logEvent("Current server time: {}", ZonedDateTime.now().toString());

        // Calculate next execution
        try {
            CronExpression cron = CronExpression.parse(dailyCronValue);
            ZonedDateTime next = cron.next(ZonedDateTime.now());
            logEvent("Next daily execution: {}", next);
        } catch (Exception e) {
            logError("Failed to parse cron: {} "+ dailyCronValue, e);
        }

        logEvent("Scheduler initialized with:");
        logEvent("Enabled: {}", scheduleConfig.getEnabled());
        logEvent("Daily cron: {}", scheduleConfig.getDailyCron());
        logEvent("Weekly cron: {}", scheduleConfig.getWeeklyCron());
        logEvent("Monthly cron: {}", scheduleConfig.getMonthlyCron());

        // Debug: Show what Spring resolved from properties
        logEvent("Resolved daily cron: {}", dailyCronValue);
        logEvent("Resolved weekly cron: {}", weeklyCronValue);
        logEvent("Resolved monthly cron: {}", monthlyCronValue);

        // Verify the cron expressions are valid
        validateCronExpression(dailyCronValue, "daily");
        validateCronExpression(weeklyCronValue, "weekly");
        validateCronExpression(monthlyCronValue, "monthly");
    }

    private void validateCronExpression(String cronExpression, String scheduleType)
    {
        if (cronExpression == null || cronExpression.trim().isEmpty())
        {
            logEvent("{} cron expression is null or empty", scheduleType);
            return;
        }

        try
        {
            // This will throw an exception if invalid
            CronExpression parsed = CronExpression.parse(cronExpression);
            logEvent("{} cron expression is valid: {}", scheduleType, cronExpression);

            // Optional: Log next execution time for debugging
            LocalDateTime nextExecution = parsed.next(LocalDateTime.now());
            if (nextExecution != null)
            {
                logEvent("{} next execution: {}", scheduleType, nextExecution);
            }

        } catch (IllegalArgumentException e)
        {
            logError("Invalid {} cron expression: {} - Error: {}" +  scheduleType + " " + cronExpression, e);
        } catch (Exception e)
        {
            logError("Unexpected error validating {} cron expression: {}" +
                    scheduleType + " " + cronExpression, e);
        }
    }

    @Scheduled(cron = "${report.schedule.daily.cron}")
    public void processDailyReports()
    {
        LocalDateTime now = LocalDateTime.now();
        logEvent("Daily scheduler triggered at {}", now);
        logEvent("Using cron expression: {}", dailyCronValue);
        logEvent("Daily scheduler triggered at {}", LocalDateTime.now());
        if (isScheduleEnabled("daily"))
        {
            try
            {
                reportingService.callAllReports("daily");
            } catch (Exception e)
            {
                logError("Error processing daily reports", e);
            }
        }
    }

    @Scheduled(cron = "${report.schedule.weekly.cron}")
    public void processWeeklyReports()
    {
        if (isScheduleEnabled("weekly"))
        {
            try
            {
                logEvent("Starting weekly report processing...");
                reportingService.callAllReports("weekly");
                logEvent("Weekly report processing completed.");
            } catch (Exception e)
            {
                logError("Error processing weekly reports", e);
            }
        }
    }

    @Scheduled(cron = "${report.schedule.monthly.cron}")
    public void processMonthlyReports()
    {
        if (isScheduleEnabled("monthly"))
        {
            try
            {
                logEvent("Starting monthly report processing...");
                reportingService.callAllReports("monthly");
                logEvent("Monthly report processing completed.");
            } catch (Exception e)
            {
                logError("Error processing monthly reports", e);
            }
        }
    }

    private boolean isScheduleEnabled(String scheduleType)
    {
        return scheduleConfig.getEnabled().contains(scheduleType);
    }
}
