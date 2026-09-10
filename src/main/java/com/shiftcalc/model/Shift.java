package com.shiftcalc.model;

import java.time.ZonedDateTime;
import java.math.BigDecimal;

/**
 * Модель смены с поддержкой часовых поясов.
 * Все временные метки хранятся с информацией о часовом поясе (ZonedDateTime).
 */
public class Shift {
    private final Long id;
    private final String shiftName;
    private final String timeZoneId;
    private final ZonedDateTime startTime;
    private final ZonedDateTime endTime;
    private final BigDecimal baseRate;
    private final Integer employeeCount;

    public Shift(Long id, String shiftName, String timeZoneId, 
                 ZonedDateTime startTime, ZonedDateTime endTime, 
                 BigDecimal baseRate, Integer employeeCount) {
        this.id = id;
        this.shiftName = shiftName;
        this.timeZoneId = timeZoneId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.baseRate = baseRate;
        this.employeeCount = employeeCount;
    }

    // Getters
    public Long getId() { return id; }
    public String getShiftName() { return shiftName; }
    public String getTimeZoneId() { return timeZoneId; }
    public ZonedDateTime getStartTime() { return startTime; }
    public ZonedDateTime getEndTime() { return endTime; }
    public BigDecimal getBaseRate() { return baseRate; }
    public Integer getEmployeeCount() { return employeeCount; }

    /**
     * Продолжительность смены в часах
     */
    public long getDurationHours() {
        return java.time.Duration.between(startTime, endTime).toHours();
    }
}
