package com.shiftcalc.model;

import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * Результат расчёта показателей смены.
 * Содержит все метрики, рассчитанные с учётом часового пояса.
 */
@Value
@Builder(toBuilder = true)
public class ShiftCalculationResult {
    Long shiftId;
    String shiftName;
    String timeZoneId;
    ZonedDateTime startTime;
    ZonedDateTime endTime;
    long durationHours;
    BigDecimal totalLaborCost;
    BigDecimal averageHourlyRate;
    Integer employeeCount;
    BigDecimal totalEmployeeHours;
    ZonedDateTime calculatedAt;
    long nightHours;
    long dayHours;

    @Override
    public String toString() {
        return String.format(
            "ShiftCalculationResult{shiftId=%d, shiftName='%s', timeZone=%s, " +
            "duration=%dh (day=%dh, night=%dh), totalCost=%s, avgHourlyRate=%s, employees=%d}",
            shiftId, shiftName, timeZoneId, durationHours, dayHours, nightHours,
            totalLaborCost, averageHourlyRate, employeeCount
        );
    }
}
