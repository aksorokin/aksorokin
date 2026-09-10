package com.shiftcalc.calculator;

import com.shiftcalc.model.Shift;
import com.shiftcalc.model.ShiftCalculationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;

/**
 * Калькулятор показателей смены.
 * Реализует основную бизнес-логику расчёта с учётом часовых поясов.
 */
@Component
public class ShiftMetricsCalculator {

    /**
     * Рассчитать все показатели для смены.
     * 
     * @param shift смена для расчёта
     * @return результат расчёта
     */
    public ShiftCalculationResult calculate(Shift shift) {
        // Продолжительность смены в часах
        long durationHours = shift.getDurationHours();
        
        // Общее количество человеко-часов
        BigDecimal totalEmployeeHours = BigDecimal.valueOf(durationHours)
                .multiply(BigDecimal.valueOf(shift.getEmployeeCount()));
        
        // Общий фонд оплаты труда (baseRate * employeeCount * durationHours)
        BigDecimal totalLaborCost = shift.getBaseRate()
                .multiply(BigDecimal.valueOf(shift.getEmployeeCount()))
                .multiply(BigDecimal.valueOf(durationHours));
        
        // Средняя почасовая ставка на одного сотрудника
        BigDecimal averageHourlyRate = durationHours > 0 
                ? totalLaborCost.divide(totalEmployeeHours, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        return new ShiftCalculationResult(
                shift.getId(),
                shift.getShiftName(),
                shift.getTimeZoneId(),
                shift.getStartTime(),
                shift.getEndTime(),
                durationHours,
                totalLaborCost.setScale(2, RoundingMode.HALF_UP),
                averageHourlyRate,
                shift.getEmployeeCount(),
                totalEmployeeHours.setScale(2, RoundingMode.HALF_UP),
                ZonedDateTime.now()
        );
    }

    /**
     * Рассчитать показатель с учётом ночных часов (дополнительная логика).
     * Ночные часы определяются по локальному времени смены.
     * 
     * @param shift смена для расчёта
     * @param nightRateMultiplier коэффициент доплаты за ночные часы (например, 1.5)
     * @return результат расчёта
     */
    public ShiftCalculationResult calculateWithNightHours(Shift shift, BigDecimal nightRateMultiplier) {
        ZonedDateTime startTime = shift.getStartTime();
        ZonedDateTime endTime = shift.getEndTime();
        
        // Определяем ночные часы (22:00 - 06:00 по локальному времени смены)
        long nightHours = calculateNightHours(startTime, endTime);
        long dayHours = shift.getDurationHours() - nightHours;
        
        // Расчёт стоимости: дневные часы + ночные часы с коэффициентом
        BigDecimal dayCost = shift.getBaseRate()
                .multiply(BigDecimal.valueOf(dayHours))
                .multiply(BigDecimal.valueOf(shift.getEmployeeCount()));
        
        BigDecimal nightCost = shift.getBaseRate()
                .multiply(nightRateMultiplier)
                .multiply(BigDecimal.valueOf(nightHours))
                .multiply(BigDecimal.valueOf(shift.getEmployeeCount()));
        
        BigDecimal totalLaborCost = dayCost.add(nightCost);
        
        BigDecimal totalEmployeeHours = BigDecimal.valueOf(shift.getDurationHours())
                .multiply(BigDecimal.valueOf(shift.getEmployeeCount()));
        
        BigDecimal averageHourlyRate = totalEmployeeHours.compareTo(BigDecimal.ZERO) > 0
                ? totalLaborCost.divide(totalEmployeeHours, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        return new ShiftCalculationResult(
                shift.getId(),
                shift.getShiftName(),
                shift.getTimeZoneId(),
                startTime,
                endTime,
                shift.getDurationHours(),
                totalLaborCost.setScale(2, RoundingMode.HALF_UP),
                averageHourlyRate,
                shift.getEmployeeCount(),
                totalEmployeeHours.setScale(2, RoundingMode.HALF_UP),
                ZonedDateTime.now()
        );
    }

    /**
     * Подсчитать количество ночных часов в смене (22:00 - 06:00).
     * Учитывает часовой пояс смены.
     */
    private long calculateNightHours(ZonedDateTime startTime, ZonedDateTime endTime) {
        long nightHours = 0;
        ZonedDateTime current = startTime;
        
        while (current.isBefore(endTime)) {
            int hour = current.getHour();
            // Ночные часы: 22, 23, 0, 1, 2, 3, 4, 5
            if (hour >= 22 || hour < 6) {
                nightHours++;
            }
            current = current.plusHours(1);
        }
        
        return nightHours;
    }

    /**
     * Конвертировать время начала и окончания смены в указанный часовой пояс.
     * Полезно для отчётности в едином временном стандарте.
     */
    public Shift convertToTimeZone(Shift shift, String targetTimeZoneId) {
        ZonedDateTime startInTarget = shift.getStartTime().withZoneSameInstant(
                java.time.ZoneId.of(targetTimeZoneId));
        ZonedDateTime endInTarget = shift.getEndTime().withZoneSameInstant(
                java.time.ZoneId.of(targetTimeZoneId));
        
        return new Shift(
                shift.getId(),
                shift.getShiftName(),
                targetTimeZoneId,
                startInTarget,
                endInTarget,
                shift.getBaseRate(),
                shift.getEmployeeCount()
        );
    }
}
