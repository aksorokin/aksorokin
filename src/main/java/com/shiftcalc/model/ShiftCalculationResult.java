package com.shiftcalc.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * Результат расчёта показателей смены.
 * Содержит все метрики, рассчитанные с учётом часового пояса.
 */
public class ShiftCalculationResult {
    private final Long shiftId;
    private final String shiftName;
    private final String timeZoneId;
    private final ZonedDateTime startTime;
    private final ZonedDateTime endTime;
    private final long durationHours;
    private final BigDecimal totalLaborCost;
    private final BigDecimal averageHourlyRate;
    private final Integer employeeCount;
    private final BigDecimal totalEmployeeHours;
    private final ZonedDateTime calculatedAt;

    public ShiftCalculationResult(Long shiftId, String shiftName, String timeZoneId,
                                   ZonedDateTime startTime, ZonedDateTime endTime,
                                   long durationHours, BigDecimal totalLaborCost,
                                   BigDecimal averageHourlyRate, Integer employeeCount,
                                   BigDecimal totalEmployeeHours, ZonedDateTime calculatedAt) {
        this.shiftId = shiftId;
        this.shiftName = shiftName;
        this.timeZoneId = timeZoneId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationHours = durationHours;
        this.totalLaborCost = totalLaborCost;
        this.averageHourlyRate = averageHourlyRate;
        this.employeeCount = employeeCount;
        this.totalEmployeeHours = totalEmployeeHours;
        this.calculatedAt = calculatedAt;
    }

    // Getters
    public Long getShiftId() { return shiftId; }
    public String getShiftName() { return shiftName; }
    public String getTimeZoneId() { return timeZoneId; }
    public ZonedDateTime getStartTime() { return startTime; }
    public ZonedDateTime getEndTime() { return endTime; }
    public long getDurationHours() { return durationHours; }
    public BigDecimal getTotalLaborCost() { return totalLaborCost; }
    public BigDecimal getAverageHourlyRate() { return averageHourlyRate; }
    public Integer getEmployeeCount() { return employeeCount; }
    public BigDecimal getTotalEmployeeHours() { return totalEmployeeHours; }
    public ZonedDateTime getCalculatedAt() { return calculatedAt; }

    @Override
    public String toString() {
        return String.format("ShiftCalculationResult{shiftId=%d, shiftName='%s', timeZone=%s, " +
                "duration=%dh, totalCost=%s, avgHourlyRate=%s, employees=%d}",
                shiftId, shiftName, timeZoneId, durationHours, totalLaborCost, 
                averageHourlyRate, employeeCount);
    }
}
