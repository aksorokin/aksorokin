package com.shiftcalc.calculator;

import com.shiftcalc.model.Shift;
import com.shiftcalc.model.ShiftCalculationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Component
public class ShiftMetricsCalculator {

    private static final int NIGHT_START_HOUR = 22;
    private static final int NIGHT_END_HOUR = 6;
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    public ShiftCalculationResult calculate(Shift shift) {
        validateShift(shift);
        
        long durationHours = shift.getDurationHours();
        BigDecimal totalEmployeeHours = BigDecimal.valueOf(durationHours)
                .multiply(BigDecimal.valueOf(shift.getEmployeeCount()));

        BigDecimal totalLaborCost = shift.getBaseRate()
                .multiply(BigDecimal.valueOf(shift.getEmployeeCount()))
                .multiply(BigDecimal.valueOf(durationHours));

        BigDecimal averageHourlyRate = durationHours > 0
                ? totalLaborCost.divide(totalEmployeeHours, 2, RoundingMode.HALF_UP)
                : ZERO;

        return ShiftCalculationResult.builder()
                .shiftId(shift.getId())
                .shiftName(shift.getShiftName())
                .timeZoneId(shift.getTimeZoneId())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .durationHours(durationHours)
                .totalLaborCost(totalLaborCost.setScale(2, RoundingMode.HALF_UP))
                .averageHourlyRate(averageHourlyRate)
                .employeeCount(shift.getEmployeeCount())
                .totalEmployeeHours(totalEmployeeHours.setScale(2, RoundingMode.HALF_UP))
                .calculatedAt(ZonedDateTime.now())
                .nightHours(0)
                .dayHours(durationHours)
                .build();
    }

    public ShiftCalculationResult calculateWithNightHours(Shift shift, BigDecimal nightRateMultiplier) {
        validateShift(shift);
        
        ZonedDateTime startTime = shift.getStartTime();
        ZonedDateTime endTime = shift.getEndTime();

        NightHoursResult nightHoursResult = calculateNightHoursEfficient(startTime, endTime);
        long nightHours = nightHoursResult.nightHours;
        long dayHours = nightHoursResult.dayHours;

        BigDecimal dayCost = shift.getBaseRate()
                .multiply(BigDecimal.valueOf(dayHours))
                .multiply(BigDecimal.valueOf(shift.getEmployeeCount()));

        BigDecimal nightCost = shift.getBaseRate()
                .multiply(nightRateMultiplier)
                .multiply(BigDecimal.valueOf(nightHours))
                .multiply(BigDecimal.valueOf(shift.getEmployeeCount()));

        BigDecimal totalLaborCost = dayCost.add(nightCost);
        long totalHours = shift.getDurationHours();
        
        BigDecimal totalEmployeeHours = BigDecimal.valueOf(totalHours)
                .multiply(BigDecimal.valueOf(shift.getEmployeeCount()));

        BigDecimal averageHourlyRate = totalEmployeeHours.compareTo(ZERO) > 0
                ? totalLaborCost.divide(totalEmployeeHours, 2, RoundingMode.HALF_UP)
                : ZERO;

        log.debug("Расчёт для смены {}: дневные={}ч, ночные={}ч, стоимость={}", 
                shift.getShiftName(), dayHours, nightHours, totalLaborCost);

        return ShiftCalculationResult.builder()
                .shiftId(shift.getId())
                .shiftName(shift.getShiftName())
                .timeZoneId(shift.getTimeZoneId())
                .startTime(startTime)
                .endTime(endTime)
                .durationHours(totalHours)
                .totalLaborCost(totalLaborCost.setScale(2, RoundingMode.HALF_UP))
                .averageHourlyRate(averageHourlyRate)
                .employeeCount(shift.getEmployeeCount())
                .totalEmployeeHours(totalEmployeeHours.setScale(2, RoundingMode.HALF_UP))
                .calculatedAt(ZonedDateTime.now())
                .nightHours(nightHours)
                .dayHours(dayHours)
                .build();
    }

    private NightHoursResult calculateNightHoursEfficient(ZonedDateTime start, ZonedDateTime end) {
        long totalHours = Duration.between(start, end).toHours();
        if (totalHours <= 0) {
            return new NightHoursResult(0, 0);
        }

        long nightHours = 0;
        ZonedDateTime current = start;

        while (current.isBefore(end)) {
            int hour = current.getHour();
            if (hour >= NIGHT_START_HOUR || hour < NIGHT_END_HOUR) {
                nightHours++;
            }
            current = current.plusHours(1);
        }

        return new NightHoursResult(nightHours, totalHours - nightHours);
    }

    public Shift convertToTimeZone(Shift shift, String targetTimeZoneId) {
        validateTimeZone(targetTimeZoneId);
        
        ZonedDateTime startInTarget = shift.getStartTime().withZoneSameInstant(
                ZoneId.of(targetTimeZoneId));
        ZonedDateTime endInTarget = shift.getEndTime().withZoneSameInstant(
                ZoneId.of(targetTimeZoneId));

        return shift.toBuilder()
                .timeZoneId(targetTimeZoneId)
                .startTime(startInTarget)
                .endTime(endInTarget)
                .build();
    }

    private void validateShift(Shift shift) {
        if (shift == null) {
            throw new IllegalArgumentException("Смена не может быть null");
        }
        if (!shift.isValidTimeRange()) {
            throw new IllegalArgumentException("Некорректный временной диапазон смены");
        }
    }

    private void validateTimeZone(String timeZoneId) {
        try {
            ZoneId.of(timeZoneId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Некорректный часовой пояс: " + timeZoneId, e);
        }
    }

    private static class NightHoursResult {
        final long nightHours;
        final long dayHours;

        NightHoursResult(long nightHours, long dayHours) {
            this.nightHours = nightHours;
            this.dayHours = dayHours;
        }
    }
}
